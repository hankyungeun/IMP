package com.bootest.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bootest.dto.StatisticDataDto;
import com.bootest.dto.StatisticDataValueDto;
import com.bootest.dto.optimizer.RegisterOptimizerDto;
import com.bootest.model.AwsInstanceType;
import com.bootest.model.ResourceUsage;
import com.bootest.repository.AwsInstanceTypeRepo;
import com.bootest.repository.ResourceUsageRepo;
import com.bootest.searcher.SearchBuilder;
import com.bootest.searcher.SearchOperationType;
import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;
import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RightSizeOpportunityService {

    private final ResourceUsageRepo vesourceUsageRepo;
    private final ObjectMapper mapper;
    private final AwsInstanceTypeRepo vrmInstanceTypeRepo;
    private final UnusedAndVersionOpportunityService oppService;

    @Value("${task.optimizer.avg-cpu}")
    private Float avgCpuThreshold;

    @Value("${task.optimizer.max-cpu}")
    private Float maxCpuThreshold;

    @Value("${task.optimizer.avg-mem}")
    private Float avgMemThreshold;

    @Value("${task.optimizer.max-mem}")
    private Float maxMemThreshold;

    public void rightSizeOpportunityTask(
            Set<String> dateSet,
            ResourceUsage usage,
            Instant to,
            Instant from) throws JsonMappingException, JsonProcessingException {

        List<ResourceUsage> mergedCpuUsageData = getUsagesByPeriodAndDataType(dateSet, usage, UsageDataType.CPU);
        List<ResourceUsage> mergedMemUsageData = getUsagesByPeriodAndDataType(dateSet, usage, UsageDataType.MEMORY);

        Double avgCpu = null;
        Double maxCpu = null;
        if (!mergedCpuUsageData.isEmpty()) {
            Map<String, Double> avgCpuMap = getAvgStatistics(mergedCpuUsageData, "avg");

            avgCpu = getAvgUsage(avgCpuMap, from, to);

            Map<String, Double> maxCpuMap = getAvgStatistics(mergedCpuUsageData, "max");

            maxCpu = getAvgUsage(maxCpuMap, from, to);
        }

        Double avgMem = null;
        Double maxMem = null;
        if (!mergedMemUsageData.isEmpty()) {
            Map<String, Double> avgMemMap = getAvgStatistics(mergedMemUsageData, "avg");

            avgMem = getAvgUsage(avgMemMap, from, to);

            Map<String, Double> maxMemMap = getAvgStatistics(mergedMemUsageData, "max");

            maxMem = getAvgUsage(maxMemMap, from, to);
        }

        instanceTypeRecommendation(usage, avgCpu, maxCpu, avgMem, maxMem);
    }

    public void instanceTypeRecommendation(
            ResourceUsage usage,
            Double avgCpu,
            Double maxCpu,
            Double avgMem,
            Double maxMem) throws JsonMappingException,
            JsonProcessingException {
        Integer cnt = 0;
        Boolean modify = false;
        String recommendedInstancetype = null;

        if (avgCpu != null) {
            if (avgCpu < avgCpuThreshold) {
                modify = true;
            } else {
                modify = false;
            }
        }

        if (maxCpu != null) {
            if (maxCpu < maxCpuThreshold) {
                modify = true;
            } else {
                modify = false;
            }
        }

        if (avgMem != null) {
            if (avgMem < avgMemThreshold) {
                modify = true;
            } else {
                modify = false;
            }
        }

        if (maxMem != null) {
            if (maxMem < maxMemThreshold) {
                modify = true;
            } else {
                modify = false;
            }
        }

        if (modify) {

            String[] instanceTypeFamily = usage.getInstanceType().split("\\.");

            AwsInstanceType srcInstanceType = vrmInstanceTypeRepo
                    .findByRegionAndInstanceType(usage.getRegion(), usage.getInstanceType())
                    .orElse(null);

            if (srcInstanceType != null) {

                SearchBuilder<AwsInstanceType> searchBuilder = SearchBuilder.builder();
                searchBuilder.with("region", SearchOperationType.EQUAL, usage.getRegion());
                searchBuilder.with("instanceType", SearchOperationType.START_WITH,
                        instanceTypeFamily[0]);

                List<AwsInstanceType> instanceTypes = vrmInstanceTypeRepo.findAll(searchBuilder.build());

                List<Float> instanceSpecs = new ArrayList<>();
                for (AwsInstanceType type : instanceTypes) {
                    instanceSpecs.add(type.getVcpus() + type.getMemoryGib());
                }

                Collections.sort(instanceSpecs);

                for (Float f : instanceSpecs) {
                    if (srcInstanceType.getVcpus() + srcInstanceType.getMemoryGib() == f) {
                        cnt--;
                        break;
                    } else {
                        cnt++;
                    }
                }

                if (cnt >= 0) {
                    for (AwsInstanceType type : instanceTypes) {
                        if (type.getVcpus() + type.getMemoryGib() == instanceSpecs.get(cnt)) {
                            recommendedInstancetype = type.getInstanceType();
                        }
                    }
                }
            }

            RegisterOptimizerDto registerTemp = new RegisterOptimizerDto();

            if (recommendedInstancetype != null) {

                Float cost = oppService.getMonthlySavings(usage.getOs(), usage.getInstanceType(),
                        usage.getRegion(), recommendedInstancetype);

                if (cost == null) {

                    Float terminationCost = oppService.getMonthlySavings(usage.getOs(),
                            usage.getInstanceType(), usage.getRegion(), null);

                    registerTemp.setResourceId(usage.getResourceId());
                    registerTemp.setResourceName(usage.getResourceName());
                    registerTemp.setServiceType(ServiceType.INSTANCE);
                    registerTemp.setRegion(usage.getRegion());
                    registerTemp.setAccountId(usage.getAccountId());
                    registerTemp.setAccountName(usage.getAccountName());
                    registerTemp.setOptType(OptimizationType.UNUSED);
                    registerTemp.setAction(RecommendedAction.TERMINATE);
                    registerTemp.setResourceType(usage.getInstanceType());
                    registerTemp.setRecommendation(null);
                    registerTemp.setEstimatedMonthlySavings(terminationCost);
                    registerTemp.setInstanceOs(usage.getOs());
                    registerTemp.setOptimizationReason("Underutilized Resource");

                    oppService.registerOptimizer(registerTemp);

                } else {
                    registerTemp.setResourceId(usage.getResourceId());
                    registerTemp.setResourceName(usage.getResourceName());
                    registerTemp.setServiceType(ServiceType.INSTANCE);
                    registerTemp.setRegion(usage.getRegion());
                    registerTemp.setAccountId(usage.getAccountId());
                    registerTemp.setAccountName(usage.getAccountName());
                    registerTemp.setOptType(OptimizationType.RIGHT_SIZE);
                    registerTemp.setAction(RecommendedAction.MODIFY);
                    registerTemp.setResourceType(usage.getInstanceType());
                    registerTemp.setRecommendation(recommendedInstancetype);
                    registerTemp.setEstimatedMonthlySavings(cost);
                    registerTemp.setInstanceOs(usage.getOs());
                    registerTemp.setOptimizationReason("Instance Scaling");

                    oppService.registerOptimizer(registerTemp);

                }
            } else {

                Float cost = oppService.getMonthlySavings(usage.getOs(),
                        usage.getInstanceType(), usage.getRegion(), null);

                registerTemp.setResourceId(usage.getResourceId());
                registerTemp.setResourceName(usage.getResourceName());
                registerTemp.setServiceType(ServiceType.INSTANCE);
                registerTemp.setRegion(usage.getRegion());
                registerTemp.setAccountId(usage.getAccountId());
                registerTemp.setAccountName(usage.getAccountName());
                registerTemp.setOptType(OptimizationType.UNUSED);
                registerTemp.setAction(RecommendedAction.TERMINATE);
                registerTemp.setResourceType(usage.getInstanceType());
                registerTemp.setRecommendation(null);
                registerTemp.setEstimatedMonthlySavings(cost);
                registerTemp.setInstanceOs(usage.getOs());
                registerTemp.setOptimizationReason("No Recommendation Available");

                oppService.registerOptimizer(registerTemp);

            }
        }
    }

    public List<ResourceUsage> getUsagesByPeriodAndDataType(
            Set<String> dates,
            ResourceUsage data,
            UsageDataType dataType) {
        List<ResourceUsage> results = new ArrayList<>();

        for (String date : dates) {
            String[] dateArr = date.split("-");
            ResourceUsage usage = vesourceUsageRepo
                    .findByResourceIdAndAnnuallyAndMonthlyAndDataType(data.getResourceId(),
                            Short.parseShort(dateArr[0]), Short.parseShort(dateArr[1]), dataType)
                    .orElse(null);

            if (usage != null) {
                results.add(usage);
            }
        }
        return results;
    }

    public Map<String, Double> getAvgStatistics(List<ResourceUsage> usages,
            String measurement)
            throws JsonMappingException, JsonProcessingException {

        List<StatisticDataDto> statistics = new ArrayList<>();

        for (ResourceUsage usage : usages) {
            if (measurement.equals("avg")) {
                statistics.add(mapper.readValue(usage.getAverage(), StatisticDataDto.class));
            } else {
                statistics.add(mapper.readValue(usage.getMaximum(), StatisticDataDto.class));
            }
        }

        Map<String, Double> results = new LinkedHashMap<>();

        for (StatisticDataDto s : statistics) {
            for (StatisticDataValueDto data : s.getData()) {
                results.put(data.getTime(), data.getValue());
            }
        }
        return results;
    }

    public Double getAvgUsage(Map<String, Double> avgUsageMap, Instant from, Instant to) {

        Double sum = 0d;
        int cnt = 0;

        if (avgUsageMap != null) {
            for (Entry<String, Double> entry : avgUsageMap.entrySet()) {
                Instant dateTime = Instant.parse(entry.getKey());

                if (dateTime.isAfter(from) && dateTime.isBefore(to)) {
                    cnt++;
                    sum += entry.getValue();
                }
            }
        }

        Double val = sum / cnt;
        if (!val.isNaN()) {
            BigDecimal bd = BigDecimal.valueOf(val);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            val = bd.doubleValue();
        }
        return val;
    }

}
