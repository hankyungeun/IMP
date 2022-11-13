package com.bootest.controller;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bootest.dto.StatisticDataDto;
import com.bootest.dto.optimizer.CompareOptimizationDto;
import com.bootest.dto.optimizer.OptimizationCompareResultDto;
import com.bootest.dto.optimizer.OptimizationRequestDataDto;
import com.bootest.dto.optimizer.OptimizationRequestFormDto;
import com.bootest.dto.optimizer.OptimizationTargetDataDto;
import com.bootest.dto.optimizer.OptimizationTargetDto;
import com.bootest.dto.optimizer.OptimizedBillingDto;
import com.bootest.dto.optimizer.OptimizerRegisterFromDto;
import com.bootest.dto.optimizer.UsageMetricDto;
import com.bootest.model.AwsInstanceType;
import com.bootest.model.Optimizer;
import com.bootest.model.ResourceUsage;
import com.bootest.model.ResultObject;
import com.bootest.repository.AwsInstanceTypeRepo;
import com.bootest.repository.OptimizerRepo;
import com.bootest.repository.ResourceUsageRepo;
import com.bootest.searcher.SearchBuilder;
import com.bootest.searcher.SearchOperationType;
import com.bootest.service.OptimizationService;
import com.bootest.service.UsageService;
import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;
import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/optimizer")
public class VrmOptimizerController {

    private final OptimizerRepo optimizerRepo;
    private final AwsInstanceTypeRepo awsInstanceTypeRepo;
    private final OptimizationService service;
    private final ResourceUsageRepo resourceUsageRepo;
    private final ObjectMapper mapper;
    private final UsageService usageService;

    @GetMapping
    public List<Optimizer> findAll(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String resourceName,
            @RequestParam(required = false) ServiceType serviceType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) OptimizationType optimizationType,
            @RequestParam(required = false) RecommendedAction recommendedAction,
            @RequestParam(required = false) Boolean optimized) {
        SearchBuilder<Optimizer> searchBuilder = SearchBuilder.builder();

        if (region != null) {
            searchBuilder.with("region", SearchOperationType.EQUAL, region);
        }
        if (accountId != null) {
            searchBuilder.with("accountId", SearchOperationType.EQUAL, accountId);
        }
        if (accountName != null) {
            searchBuilder.with("accountName", SearchOperationType.EQUAL, accountName);
        }
        if (resourceId != null) {
            searchBuilder.with("resourceId", SearchOperationType.EQUAL, resourceId);
        }
        if (resourceName != null) {
            searchBuilder.with("resourceName", SearchOperationType.EQUAL, resourceName);
        }
        if (serviceType != null) {
            searchBuilder.with("serviceType", SearchOperationType.EQUAL, serviceType);
        }
        if (resourceType != null) {
            searchBuilder.with("resourceType", SearchOperationType.EQUAL, resourceType);
        }
        if (optimizationType != null) {
            searchBuilder.with("optimizationType", SearchOperationType.EQUAL, optimizationType);
        }
        if (recommendedAction != null) {
            searchBuilder.with("recommendedAction", SearchOperationType.EQUAL, recommendedAction);
        }
        if (optimized != null) {
            searchBuilder.with("optimized", SearchOperationType.EQUAL, optimized);
        }
        return optimizerRepo.findAll(searchBuilder.build());
    }

    @PostMapping("/request")
    public OptimizationTargetDto request(@RequestBody OptimizationRequestFormDto temp) {
        List<OptimizationTargetDataDto> optimizationData = new ArrayList<>();

        for (OptimizationRequestDataDto optData : temp.getData()) {

            Optimizer target = optimizerRepo
                    .findByResourceIdAndOptimizationType(optData.getResourceId(), optData.getType())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Resource ID " + optData.getResourceId()));

            OptimizationTargetDataDto data = service.getOptTargetData(target);

            optimizationData.add(data);
        }

        OptimizationTargetDto result = new OptimizationTargetDto();
        result.setTotalTargetCnt(optimizationData.size());
        result.setData(optimizationData);
        return result;
    }

    @PostMapping("/compare-changes")
    public OptimizationCompareResultDto compareUsage(@RequestBody CompareOptimizationDto compare)
            throws JsonMappingException, JsonProcessingException {

        AwsInstanceType currInstanceType = awsInstanceTypeRepo
                .findByRegionAndInstanceType(compare.getRegion(), compare.getResourceType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Instance Type"));

        AwsInstanceType recoInstanceType = awsInstanceTypeRepo
                .findByRegionAndInstanceType(compare.getRegion(), compare.getRecommendedSuggestion())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Instance Type"));

        LocalDate now = LocalDate.now();
        LocalDate until = now.minusDays(compare.getDaysToCompare());

        Instant from = Instant.now().minus(compare.getDaysToCompare(), ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
        Instant to = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        List<LocalDate> dates = until.datesUntil(now).collect(Collectors.toList());

        Set<String> dateSet = new HashSet<>();
        for (LocalDate ld : dates) {
            String[] ldArray = ld.toString().split("-");
            dateSet.add(ldArray[0] + "-" + ldArray[1]);
        }

        List<ResourceUsage> mergedUsages = new ArrayList<>();
        for (String date : dateSet) {

            String[] dateArr = date.split("-");

            List<ResourceUsage> usages = resourceUsageRepo.findAllByResourceIdAndAnnuallyAndMonthly(
                    compare.getResourceId(), Short.parseShort(dateArr[0]), Short.parseShort(dateArr[1]));
            mergedUsages.addAll(usages);
        }

        List<StatisticDataDto> cpuStatistics = new ArrayList<>();
        List<StatisticDataDto> maxCpuStatistics = new ArrayList<>();
        List<StatisticDataDto> memStatistics = new ArrayList<>();
        List<StatisticDataDto> maxMemStatistics = new ArrayList<>();
        List<StatisticDataDto> netInStatistics = new ArrayList<>();
        List<StatisticDataDto> netOutStatistics = new ArrayList<>();
        for (ResourceUsage usage : mergedUsages) {
            if (usage.getDataType().equals(UsageDataType.CPU)) {
                cpuStatistics.add(mapper.readValue(usage.getAverage(), StatisticDataDto.class));
                maxCpuStatistics.add(mapper.readValue(usage.getMaximum(), StatisticDataDto.class));
            } else if (usage.getDataType().equals(UsageDataType.MEMORY)) {
                memStatistics.add(mapper.readValue(usage.getAverage(), StatisticDataDto.class));
                maxMemStatistics.add(mapper.readValue(usage.getMaximum(), StatisticDataDto.class));
            } else if (usage.getDataType().equals(UsageDataType.NET_IN)) {
                netInStatistics.add(mapper.readValue(usage.getAverage(), StatisticDataDto.class));
            } else if (usage.getDataType().equals(UsageDataType.NET_OUT)) {
                netOutStatistics.add(mapper.readValue(usage.getAverage(), StatisticDataDto.class));
            }
        }

        Map<String, Double> cpuMap = usageService.getAvgUsageMap(cpuStatistics);
        Map<String, Double> maxCpuMap = usageService.getAvgUsageMap(maxCpuStatistics);
        Map<String, Double> memMap = usageService.getAvgUsageMap(memStatistics);
        Map<String, Double> maxMemMap = usageService.getAvgUsageMap(maxMemStatistics);
        Map<String, Double> netInMap = usageService.getAvgUsageMap(netInStatistics);
        Map<String, Double> netOutMap = usageService.getAvgUsageMap(netOutStatistics);

        Double cpuValue = usageService.getAvgUsage(cpuMap, from, to);
        Double maxCpuValue = usageService.getAvgUsage(maxCpuMap, from, to);
        Double memValue = usageService.getAvgUsage(memMap, from, to);
        Double maxMemValue = usageService.getAvgUsage(maxMemMap, from, to);
        Double netInValue = usageService.getAvgUsage(netInMap, from, to);
        Double netOutValue = usageService.getAvgUsage(netOutMap, from, to);

        List<UsageMetricDto> cpuUsageMetrics = usageService.getUsageMetrics(cpuMap, from, to);
        List<UsageMetricDto> maxCpuUsageMetrics = usageService.getUsageMetrics(maxCpuMap, from, to);
        List<UsageMetricDto> memUsageMetrics = usageService.getUsageMetrics(memMap, from, to);
        List<UsageMetricDto> maxMemUsageMetrics = usageService.getUsageMetrics(maxMemMap, from, to);
        List<UsageMetricDto> netInUsageMetrics = usageService.getUsageMetrics(netInMap, from, to);
        List<UsageMetricDto> netOutUsageMetrics = usageService.getUsageMetrics(netOutMap, from, to);

        Integer vcpuCompare = currInstanceType.getVcpus() / recoInstanceType.getVcpus();
        Double memoryCompare = currInstanceType.getMemoryGib().doubleValue()
                / recoInstanceType.getMemoryGib().doubleValue();

        List<UsageMetricDto> cpuUsageChange = getEstimatedUsageMetric(cpuUsageMetrics, vcpuCompare, memoryCompare,
                "cpu");
        List<UsageMetricDto> maxCpuUsageChange = getEstimatedUsageMetric(maxCpuUsageMetrics, vcpuCompare, memoryCompare,
                "cpu");
        List<UsageMetricDto> memUsageChange = getEstimatedUsageMetric(memUsageMetrics, vcpuCompare, memoryCompare,
                "mem");
        List<UsageMetricDto> maxMemUsageChange = getEstimatedUsageMetric(maxMemUsageMetrics, vcpuCompare, memoryCompare,
                "mem");

        OptimizationCompareResultDto result = new OptimizationCompareResultDto();
        result.setResourceId(compare.getResourceId());
        result.setRegion(compare.getRegion());
        result.setResourceType(compare.getResourceType());
        result.setRecommendedSuggestion(compare.getRecommendedSuggestion());
        result.setComparedDate(until + "~" + now);
        result.setCpuUsage(cpuValue);
        result.setMaxCpuUsage(maxCpuValue);
        result.setMemUsage(memValue);
        result.setMaxMemUsage(maxMemValue);
        result.setNetInUsage(netInValue);
        result.setNetOutUsage(netOutValue);
        result.setCpuUsageMetrics(cpuUsageMetrics);
        result.setMaxCpuUsageMetrics(maxCpuUsageMetrics);
        result.setMemUsageMetrics(memUsageMetrics);
        result.setMaxMemUsageMetrics(maxMemUsageMetrics);
        result.setNetInUsageMetrics(netInUsageMetrics);
        result.setNetOutUsageMetrics(netOutUsageMetrics);
        result.setCpuUsageChangeMetrics(cpuUsageChange);
        result.setMaxCpuUsageChangeMetrics(maxCpuUsageChange);
        result.setMemUsageChangeMetrics(memUsageChange);
        result.setMaxMemUsageChangeMetrics(maxMemUsageChange);
        return result;
    }

    public List<UsageMetricDto> getEstimatedUsageMetric(List<UsageMetricDto> metrics, Integer cpuDiff, Double memDiff,
            String target) {

        List<UsageMetricDto> results = new ArrayList<>();

        for (UsageMetricDto metric : metrics) {

            Double value = 0d;

            if (target.equals("cpu")) {
                value = Double.parseDouble(metric.getValue()) * cpuDiff;
            } else if (target.equals("mem")) {
                value = Double.parseDouble(metric.getValue()) * memDiff;
            }

            UsageMetricDto estimate = new UsageMetricDto();
            estimate.setDate(metric.getDate());
            estimate.setValue(value.toString());
            results.add(estimate);
        }
        return results;
    }

    @PostMapping("/optimize")
    public ResponseEntity<ResultObject> optimize(
            @RequestBody OptimizationRequestFormDto temp)
            throws JsonParseException, JsonMappingException, IOException, InterruptedException {
        ResultObject result = new ResultObject();
        List<String> failedResources = new ArrayList<>();

        for (OptimizationRequestDataDto optData : temp.getData()) {
            try {
                service.optimize(optData);
            } catch (Exception e) {
                failedResources.add(optData.getResourceId());
            }
        }

        Integer succeeded = temp.getData().size() - failedResources.size();

        result.setResult(true);
        result.setMessage("Optimization Task End! Succeeded: " + succeeded + " Failed: " + failedResources.size());
        result.setData(failedResources);
        return new ResponseEntity<ResultObject>(result, HttpStatus.OK);
    }
}
