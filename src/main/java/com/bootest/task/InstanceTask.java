package com.bootest.task;

//인스턴스 사용량
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import com.bootest.aws.CloudWatchClientManager;
import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.MetricStatisticDto;
import com.bootest.dto.StatisticDataDto;
import com.bootest.dto.StatisticDataValueDto;
import com.bootest.model.ResourceUsage;
import com.bootest.repository.ResourceUsageRepo;
import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bootest.model.Account;
import com.bootest.repository.AccountRepo;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ec2.model.Tag;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class InstanceTask {

    private final Ec2ClientManager ec2cm;
    private final CloudWatchClientManager cwccm;
    private final AccountRepo accountRepo;
    private final ResourceUsageRepo resourceUsageRepo;
    private final ObjectMapper objectMapper;

    @Value("${task.usage}")
    private boolean isScheduledTaskEnabled;

    //정해진 시간 간격으로 updateResourceUsage 함수 호출
    @Scheduled(cron = "0 0 * * * *")
    public void doResourceUsageInfoCache() throws JsonProcessingException {
        if (isScheduledTaskEnabled) {
            updateResourceUsage(List.of(LocalDate.now()));

        } else {
            log.debug("doResourceUsageInfoCache Shceduled Task is disabled");
        }
    }

//    리소스 사용 현황 업데이트 디비저장
    public void updateResourceUsage(List<LocalDate> dates) throws JsonProcessingException {
        log.info("Update Resource Usage Request Start");

        ZoneId zone = ZoneId.systemDefault();

        for (LocalDate ld : dates) {
            // 매월 한시간 마다 월 1일부터 월말의 데이터를 조회 후 업데이트 한다
            Instant startTime = ld.withDayOfMonth(1).atStartOfDay(zone).toInstant();
            Instant endTime = ld.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant();

            short year = (short) ld.getYear();
            short month = (short) ld.getMonthValue();

            List<Account> accounts = accountRepo.findAll();

            for (Account a : accounts) {

                String[] regionArr = a.getRegions().split(", ");

                for (String regionStr : regionArr) {

                    Region region = Region.of(regionStr);

                    //아마존 키를 가지고 api를 사용할수있는 클라이언트 생성
                    Ec2Client ec2 = ec2cm.getEc2WithAccount(region, a);
                    CloudWatchClient cwc = cwccm.getCwc(region, a);

//                    만들어진 인스턴스 저장
                    List<Instance> instances = getInstances(ec2);

                    if (instances == null || instances.isEmpty()) {
                        continue;
                    }

                    for (Instance i : instances) {
                        // 각 항목에 맞게 cpu, net in/out 사용량을 조회 후 avg, min, max 로 나눠 DTO 에 담는다
                        MetricStatisticDto cpu = getUsageMetricStatistics(cwc, i.instanceId(), startTime, endTime,
                                "CPUUtilization");
                        MetricStatisticDto netIn = getUsageMetricStatistics(cwc, i.instanceId(), startTime, endTime,
                                "NetworkIn");
                        MetricStatisticDto netOut = getUsageMetricStatistics(cwc, i.instanceId(), startTime, endTime,
                                "NetworkOut");
                        MetricStatisticDto mem = getUsageMetricStatistics(cwc, i.instanceId(), startTime, endTime,
                                "mem_used_percent");
                        MetricStatisticDto disk = getUsageMetricStatistics(cwc, i.instanceId(), startTime, endTime,
                                "disk_used_percent");

                        if (cpu == null && netIn == null && netOut == null) {
                            continue;
                        }

                        String resourceName = null;
                        for (Tag t : i.tags()) {
                            if ("Name".equals(t.key())) {
                                resourceName = t.value();
                            }
                        }

                        // DTO 의 값이 널이 아닐 경우 해당 내용을 저장한다.
                        if (cpu != null) {
                            saveUsage(a, regionStr, i, year, month, UsageDataType.CPU, resourceName, cpu);
                        }
                        if (netIn != null) {
                            saveUsage(a, regionStr, i, year, month, UsageDataType.NET_IN, resourceName, netIn);
                        }
                        if (netOut != null) {
                            saveUsage(a, regionStr, i, year, month, UsageDataType.NET_OUT, resourceName, netOut);
                        }
                        if (mem != null) {
                            saveUsage(a, regionStr, i, year, month, UsageDataType.MEMORY, resourceName, mem);
                        }
                        if (disk != null) {
                            saveUsage(a, regionStr, i, year, month, UsageDataType.DISK, resourceName, disk);
                        }
                    }
                }
            }
        }
    }

    //모든 인스턴스 정보 가져오기
    public List<Instance> getInstances(Ec2Client ec2) {
        List<Instance> results = new ArrayList<>();

        String nextToken = null;

        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .nextToken(nextToken)
                .build();

        try {
            do {
                DescribeInstancesResponse response = ec2.describeInstances(request);
                for (Reservation reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        results.add(instance);
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
        } catch (Exception e) {
            log.error("Describe Instance Request Failed: {}", e.getMessage(), e);
            return null;
        }
        return results;
    }

    public MetricStatisticDto getUsageMetricStatistics(
            CloudWatchClient cwc,
            String instanceId,
            Instant start,
            Instant end,
            String metricName) {
        Dimension dimension = Dimension.builder()
                .name("InstanceId")
                .value(instanceId)
                .build();

        GetMetricStatisticsRequest.Builder requestBuilder = GetMetricStatisticsRequest.builder()
                .dimensions(dimension)
                .period(3600)
                .startTime(start)
                .endTime(end)
                .statistics(Statistic.AVERAGE, Statistic.MAXIMUM, Statistic.MINIMUM);

        if (metricName.equals("CPUUtilization")) {
            requestBuilder.namespace("AWS/EC2").metricName(metricName);
        } else if (metricName.equals("NetworkIn")) {
            requestBuilder.namespace("AWS/EC2").metricName(metricName);
        } else if (metricName.equals("NetworkOut")) {
            requestBuilder.namespace("AWS/EC2").metricName(metricName);
        } else if (metricName.equals("mem_used_percent")) {
            requestBuilder.namespace("CWAgent").metricName(metricName);
        } else if (metricName.equals("disk_used_percent")) {
            requestBuilder.namespace("CWAgent").metricName(metricName);
        }

        try {
            GetMetricStatisticsResponse response = cwc.getMetricStatistics(requestBuilder.build());

            if (response.datapoints().isEmpty()) {
                return null;
            }

            Map<Instant, Double> avgMap = new TreeMap<>();
            Map<Instant, Double> minMap = new TreeMap<>();
            Map<Instant, Double> maxMap = new TreeMap<>();

            for (Datapoint dp : response.datapoints()) {
                avgMap.put(dp.timestamp(), dp.average());
                minMap.put(dp.timestamp(), dp.minimum());
                maxMap.put(dp.timestamp(), dp.maximum());
            }

            StatisticDataDto avgResult = getStatisticData(avgMap);
            StatisticDataDto minResult = getStatisticData(minMap);
            StatisticDataDto maxResult = getStatisticData(maxMap);

            MetricStatisticDto metricStats = new MetricStatisticDto();

            metricStats.setLabel(metricName);
            metricStats.setAverage(avgResult);
            metricStats.setMin(minResult);
            metricStats.setMax(maxResult);

            return metricStats;
        } catch (Exception e) {
            log.error("Get Metric Statistics Request Failed (Message: {})", e.getMessage(), e);
            return null;
        }
    }

    public StatisticDataDto getStatisticData(Map<Instant, Double> usageMap) {
        List<StatisticDataValueDto> dataList = new ArrayList<>();

        for (Map.Entry<Instant, Double> entry : usageMap.entrySet()) {
            StatisticDataValueDto data = new StatisticDataValueDto();
            data.setTime(entry.getKey().toString());
            data.setValue(entry.getValue());
            dataList.add(data);
        }

        StatisticDataDto result = new StatisticDataDto();
        result.setData(dataList);
        return result;
    }

    public void saveUsage(
            Account account,
            String regionId,
            Instance i,
            short year,
            short month,
            UsageDataType dataType,
            String resourceName,
            MetricStatisticDto data) throws JsonProcessingException {

        String os = i.platformAsString() != null ? "Windows" : "Linux";

        ResourceUsage usage = resourceUsageRepo
                .findByAccountIdAndRegionAndResourceIdAndAnnuallyAndMonthlyAndDataType(account.getAccountId(), regionId,
                        i.instanceId(), year, month, dataType)
                .orElseGet(() -> {
                    ResourceUsage ru = new ResourceUsage();
                    ru.setId(UUID.randomUUID().toString());
                    ru.setAccountId(account.getAccountId());
                    ru.setAccountName(account.getName());
                    ru.setResourceId(i.instanceId());
                    ru.setRegion(regionId);
                    ru.setAnnually(year);
                    ru.setMonthly(month);
                    return ru;
                });

        usage.setResourceName(resourceName);
        usage.setOs(os);
        usage.setImageId(i.imageId());
        usage.setLifeCycle(i.instanceLifecycleAsString() == null ? "on-demand" : "spot");
        usage.setDataType(dataType);
        usage.setResourceState(i.state().nameAsString());
        usage.setInstanceType(i.instanceTypeAsString());
        usage.setAverage(objectMapper.writeValueAsString(data.getAverage()));
        usage.setMinimum(objectMapper.writeValueAsString(data.getMin()));
        usage.setMaximum(objectMapper.writeValueAsString(data.getMax()));

        resourceUsageRepo.save(usage);
    }

}
