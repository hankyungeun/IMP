package com.bootest.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bootest.aws.CloudWatchClientManager;
import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.cloudwatch.DescribeResourceUsage;
import com.bootest.dto.cloudwatch.ResourceUsageSpecification;
import com.bootest.model.Account;
import com.bootest.repository.AccountRepo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.Metric;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataQuery;
import software.amazon.awssdk.services.cloudwatch.model.MetricDataResult;
import software.amazon.awssdk.services.cloudwatch.model.MetricStat;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/usage")
public class UsageMetricController {

    private final CloudWatchClientManager cwccm;
    private final Ec2ClientManager ec2cm;
    private final AccountRepo accountRepo;

    @GetMapping
    public List<DescribeResourceUsage> findAll() {

        List<DescribeResourceUsage> results = new ArrayList<>();

        List<Account> accounts = accountRepo.findAll();

        for (Account account : accounts) {

            String[] regionArray = account.getRegions().split(", ");

            for (String regionStr : regionArray) {

                Region region = Region.of(regionStr);

                CloudWatchClient cwc = cwccm.getCwc(region, account);

                Ec2Client ec2 = ec2cm.getEc2WithAccount(region, account);

                List<String> instanceIds = getResourceIds(ec2);

                DescribeResourceUsage dru = new DescribeResourceUsage();
                dru.setAccountId(account.getAccountId());
                dru.setSpecifications(getCloudUsageMetrics(cwc, instanceIds));

                results.add(dru);
            }

        }
        return results;
    }
    // if the searched date is same as the local date get it directly from AWS

    public List<String> getResourceIds(Ec2Client ec2) {

        List<String> instanceIds = new ArrayList<>();

        String nextToken = null;

        try {

            do {

                DescribeInstancesResponse response = ec2.describeInstances();

                for (Reservation r : response.reservations()) {
                    for (Instance i : r.instances()) {

                        instanceIds.add(i.instanceId());

                    }
                }

                nextToken = response.nextToken();

            } while (nextToken != null);

        } catch (Exception e) {
            log.error("Cannot describe instance for the following account. Message: {}", e.getMessage());
        }

        return instanceIds;
    }

    public List<ResourceUsageSpecification> getCloudUsageMetrics(CloudWatchClient cwc, List<String> instanceIds) {

        List<ResourceUsageSpecification> results = new ArrayList<>();

        ZoneId zone = ZoneId.systemDefault();

        Instant start = LocalDate.now().atStartOfDay(zone).toInstant();
        Instant end = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant();

        for (String instanceId : instanceIds) {

            Metric metric = Metric.builder()
                    .metricName("CPUUtilization")
                    .namespace("AWS/EC2")
                    .dimensions(
                            Dimension.builder()
                                    .name("InstanceId")
                                    .value(instanceId)
                                    .build())
                    .build();

            MetricDataQuery mdq = MetricDataQuery.builder()
                    .id("averageCpuUsage")
                    .returnData(true)
                    .metricStat(
                            MetricStat.builder()
                                    .stat("Average")
                                    .period(300)
                                    .metric(metric)
                                    .build())
                    .build();

            GetMetricDataRequest request = GetMetricDataRequest.builder()
                    .startTime(start)
                    .endTime(end)
                    .metricDataQueries(mdq)
                    .build();

            try {

                GetMetricDataResponse response = cwc.getMetricData(request);

                for (MetricDataResult mdr : response.metricDataResults()) {

                    ResourceUsageSpecification rus = new ResourceUsageSpecification();
                    rus.setResourceId(instanceId);
                    rus.setUsageId(mdr.id());
                    rus.setLabel(mdr.label());
                    rus.setTimeAndValues(setUsageSpecifics(mdr.timestamps(), mdr.values()));

                    results.add(rus);
                }

            } catch (Exception e) {
                log.error("Cannot get cloudwatch metric data. Message: {}", e.getMessage());
            }

        }
        return results;
    }

    public Map<Instant, Double> setUsageSpecifics(List<Instant> timeStamps, List<Double> values) {

        Map<Instant, Double> results = new LinkedHashMap<>();

        for (int i = 0; i < timeStamps.size(); i++) {
            results.put(timeStamps.get(i), values.get(i));
        }

        return results;
    }

}
