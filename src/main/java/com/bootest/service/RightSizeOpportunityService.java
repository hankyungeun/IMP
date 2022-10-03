// package com.gytni.vrm.service;

// import java.time.Instant;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.LinkedHashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;

// import org.springframework.stereotype.Service;

// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonMappingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.gytni.vrm.dto.config.OptimizeOpportunityConfigDto;
// import com.gytni.vrm.dto.optimizer.RegisterOptimizerDto;
// import com.gytni.vrm.dto.usage.StatisticDataDto;
// import com.gytni.vrm.dto.usage.StatisticDataValueDto;
// import com.gytni.vrm.model.VrmInstanceType;
// import com.gytni.vrm.model.VrmResourceUsage;
// import com.gytni.vrm.repo.VrmInstanceTypeRepo;
// import com.gytni.vrm.repo.VrmResourceUsageRepo;
// import com.gytni.vrm.searcher.SearchBuilder;
// import com.gytni.vrm.searcher.SearchOperationType;
// import com.gytni.vrm.service.ec2Resource.UsageService;
// import com.gytni.vrm.task.LoadConfig;
// import com.gytni.vrm.type.OptimizationType;
// import com.gytni.vrm.type.RecommendedAction;
// import com.gytni.vrm.type.ServiceType;
// import com.gytni.vrm.type.UsageDataType;

// import lombok.RequiredArgsConstructor;

// @RequiredArgsConstructor
// @Service
// public class RightSizeOpportunityService {

// private final VrmResourceUsageRepo vrmResourceUsageRepo;
// private final LoadConfig loadConfig;
// private final UsageService usageService;
// private final ObjectMapper mapper;
// private final AwsInstanceTypeRepo vrmInstanceTypeRepo;
// private final UnusedAndVersionOpportunityService oppService;

// public void rightSizeOpportunityTask(
// Set<String> dateSet,
// VrmResourceUsage usage,
// Instant to,
// Instant from) throws JsonMappingException, JsonProcessingException {

// OptimizeOpportunityConfigDto optConfig = loadConfig.getOptimizationConfig();

// List<VrmResourceUsage> mergedCpuUsageData =
// getUsagesByPeriodAndDataType(dateSet, usage, UsageDataType.CPU);
// List<VrmResourceUsage> mergedMemUsageData =
// getUsagesByPeriodAndDataType(dateSet, usage, UsageDataType.MEMORY);
// List<VrmResourceUsage> mergedNetInUsageData =
// getUsagesByPeriodAndDataType(dateSet, usage,
// UsageDataType.NET_IN);
// List<VrmResourceUsage> mergedNetOutUsageData =
// getUsagesByPeriodAndDataType(dateSet, usage,
// UsageDataType.NET_OUT);

// Double avgCpu = null;
// Double maxCpu = null;
// if (!mergedCpuUsageData.isEmpty()) {
// if (optConfig.isEnableCpu()) {

// Map<String, Double> avgCpuMap = getAvgStatistics(mergedCpuUsageData, "avg");

// avgCpu = usageService.getAvgUsage(avgCpuMap, from, to);

// }

// if (optConfig.isEnableMaxCpu()) {

// Map<String, Double> maxCpuMap = getAvgStatistics(mergedCpuUsageData, "max");

// maxCpu = usageService.getAvgUsage(maxCpuMap, from, to);

// }
// }

// Double avgMem = null;
// Double maxMem = null;
// if (!mergedMemUsageData.isEmpty()) {
// if (optConfig.isEnableMem()) {

// Map<String, Double> avgMemMap = getAvgStatistics(mergedMemUsageData, "avg");

// avgMem = usageService.getAvgUsage(avgMemMap, from, to);

// }

// if (optConfig.isEnableMaxMem()) {

// Map<String, Double> maxMemMap = getAvgStatistics(mergedMemUsageData, "max");

// maxMem = usageService.getAvgUsage(maxMemMap, from, to);

// }
// }

// Double avgNetIn = null;
// Double avgNetOut = null;
// if (!mergedNetInUsageData.isEmpty()) {
// if (optConfig.isEnableNetInOut()) {

// Map<String, Double> avgNetInMap = getAvgStatistics(mergedNetInUsageData,
// "avg");

// Map<String, Double> avgNetOutMap = getAvgStatistics(mergedNetOutUsageData,
// "avg");

// avgNetIn = usageService.getAvgUsage(avgNetInMap, from, to);

// avgNetOut = usageService.getAvgUsage(avgNetOutMap, from, to);

// }
// }
// instanceTypeRecommendation(usage, avgCpu, maxCpu, avgMem, maxMem, avgNetIn,
// avgNetOut, optConfig);
// }

// public void instanceTypeRecommendation(
// VrmResourceUsage usage,
// Double avgCpu,
// Double maxCpu,
// Double avgMem,
// Double maxMem,
// Double avgNetIn,
// Double avgNetOut,
// OptimizeOpportunityConfigDto config) throws JsonMappingException,
// JsonProcessingException {
// Integer cnt = 0;
// Boolean modify = false;
// String recommendedInstancetype = null;

// if (avgCpu != null) {
// if (avgCpu < config.getCpuThreshold()) {
// modify = true;
// } else {
// modify = false;
// }
// }

// if (maxCpu != null) {
// if (maxCpu < config.getMaxCpuThreshold()) {
// modify = true;
// } else {
// modify = false;
// }
// }

// if (avgMem != null) {
// if (avgMem < config.getMemThreshold()) {
// modify = true;
// } else {
// modify = false;
// }
// }

// if (maxMem != null) {
// if (maxMem < config.getMaxMemThreshold()) {
// modify = true;
// } else {
// modify = false;
// }
// }

// if (avgNetIn != null) {
// Double netInMbs = avgNetIn / 3600 / 10000;
// if (netInMbs < config.getNetInThreshold()) {
// modify = true;
// } else {
// modify = false;
// }
// }

// if (avgNetOut != null) {
// Double netOutMbs = avgNetOut / 3600 / 10000;
// if (netOutMbs < config.getNetOutThreshold()) {
// modify = true;
// } else {
// modify = false;
// }
// }

// if (modify) {

// String[] instanceTypeFamily = usage.getInstanceType().split("\\.");

// AwsInstanceType srcInstanceType = vrmInstanceTypeRepo
// .findByRegionAndInstanceType(usage.getRegion(), usage.getInstanceType())
// .orElse(null);

// if (srcInstanceType != null) {

// SearchBuilder<AwsInstanceType> searchBuilder = SearchBuilder.builder();
// searchBuilder.with("region", SearchOperationType.EQUAL, usage.getRegion());
// searchBuilder.with("instanceType", SearchOperationType.START_WITH,
// instanceTypeFamily[0]);

// List<AwsInstanceType> instanceTypes =
// vrmInstanceTypeRepo.findAll(searchBuilder.build());

// List<Float> instanceSpecs = new ArrayList<>();
// for (AwsInstanceType type : instanceTypes) {
// instanceSpecs.add(type.getVCpus() + type.getMemoryGiB());
// }

// Collections.sort(instanceSpecs);

// for (Float f : instanceSpecs) {
// if (srcInstanceType.getVCpus() + srcInstanceType.getMemoryGiB() == f) {
// cnt--;
// break;
// } else {
// cnt++;
// }
// }

// if (cnt >= 0) {
// for (AwsInstanceType type : instanceTypes) {
// if (type.getVCpus() + type.getMemoryGiB() == instanceSpecs.get(cnt)) {
// recommendedInstancetype = type.getInstanceType();
// }
// }
// }
// }

// RegisterOptimizerDto registerTemp = new RegisterOptimizerDto();

// if (recommendedInstancetype != null) {

// Float cost = oppService.getMonthlySavings(usage.getOs(),
// usage.getLifeCycle(), usage.getInstanceType(),
// usage.getRegion(), recommendedInstancetype);

// if (cost == null) {

// Float terminationCost = oppService.getMonthlySavings(usage.getOs(),
// usage.getLifeCycle(),
// usage.getInstanceType(), usage.getRegion(), null);

// registerTemp.setResourceId(usage.getResourceId());
// registerTemp.setResourceName(usage.getResourceName());
// registerTemp.setServiceType(ServiceType.INSTANCE);
// registerTemp.setRegion(usage.getRegion());
// registerTemp.setAccountId(usage.getAccountId());
// registerTemp.setAccountName(usage.getAccountName());
// registerTemp.setOptType(OptimizationType.UNUSED);
// registerTemp.setAction(RecommendedAction.TERMINATE);
// registerTemp.setResourceType(usage.getInstanceType());
// registerTemp.setRecommendation(null);
// registerTemp.setEstimatedMonthlySavings(terminationCost);
// registerTemp.setInstanceOs(usage.getOs());
// registerTemp.setOptimizationReason("Underutilized Resource");

// oppService.registerOptimizer(registerTemp);

// } else {
// registerTemp.setResourceId(usage.getResourceId());
// registerTemp.setResourceName(usage.getResourceName());
// registerTemp.setServiceType(ServiceType.INSTANCE);
// registerTemp.setRegion(usage.getRegion());
// registerTemp.setAccountId(usage.getAccountId());
// registerTemp.setAccountName(usage.getAccountName());
// registerTemp.setOptType(OptimizationType.RIGHT_SIZE);
// registerTemp.setAction(RecommendedAction.MODIFY);
// registerTemp.setResourceType(usage.getInstanceType());
// registerTemp.setRecommendation(recommendedInstancetype);
// registerTemp.setEstimatedMonthlySavings(cost);
// registerTemp.setInstanceOs(usage.getOs());
// registerTemp.setOptimizationReason("Instance Scaling");

// oppService.registerOptimizer(registerTemp);

// }
// } else {

// Float cost = oppService.getMonthlySavings(usage.getOs(),
// usage.getLifeCycle(), usage.getInstanceType(),
// usage.getRegion(), null);

// registerTemp.setResourceId(usage.getResourceId());
// registerTemp.setResourceName(usage.getResourceName());
// registerTemp.setServiceType(ServiceType.INSTANCE);
// registerTemp.setRegion(usage.getRegion());
// registerTemp.setAccountId(usage.getAccountId());
// registerTemp.setAccountName(usage.getAccountName());
// registerTemp.setOptType(OptimizationType.UNUSED);
// registerTemp.setAction(RecommendedAction.TERMINATE);
// registerTemp.setResourceType(usage.getInstanceType());
// registerTemp.setRecommendation(null);
// registerTemp.setEstimatedMonthlySavings(cost);
// registerTemp.setInstanceOs(usage.getOs());
// registerTemp.setOptimizationReason("No Recommendation Available");

// oppService.registerOptimizer(registerTemp);

// }
// }
// }

// public List<VrmResourceUsage> getUsagesByPeriodAndDataType(
// Set<String> dates,
// VrmResourceUsage data,
// UsageDataType dataType) {
// List<VrmResourceUsage> results = new ArrayList<>();

// for (String date : dates) {
// String[] dateArr = date.split("-");
// VrmResourceUsage usage = vrmResourceUsageRepo
// .findByResourceIdAndAnnuallyAndMonthlyAndDataType(data.getResourceId(),
// Short.parseShort(dateArr[0]), Short.parseShort(dateArr[1]), dataType)
// .orElse(null);

// if (usage != null) {
// results.add(usage);
// }
// }
// return results;
// }

// public Map<String, Double> getAvgStatistics(List<VrmResourceUsage> usages,
// String measurement)
// throws JsonMappingException, JsonProcessingException {

// List<StatisticDataDto> statistics = new ArrayList<>();

// for (VrmResourceUsage usage : usages) {
// if (measurement.equals("avg")) {
// statistics.add(mapper.readValue(usage.getAverage(), StatisticDataDto.class));
// } else {
// statistics.add(mapper.readValue(usage.getMaximum(), StatisticDataDto.class));
// }
// }

// Map<String, Double> results = new LinkedHashMap<>();

// for (StatisticDataDto s : statistics) {
// for (StatisticDataValueDto data : s.getData()) {
// results.put(data.getTime(), data.getValue());
// }
// }
// return results;
// }

// }
