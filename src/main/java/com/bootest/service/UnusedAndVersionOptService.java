package com.bootest.service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bootest.dto.StatisticDataDto;
import com.bootest.dto.StatisticDataValueDto;
import com.bootest.dto.optimizer.GetInstanceOptResponse;
import com.bootest.dto.optimizer.GetInstanceRightSizeOptResponse;
import com.bootest.dto.optimizer.GetModernInstanceOptResponse;
import com.bootest.dto.optimizer.GetModernVolOptResponse;
import com.bootest.dto.optimizer.GetUnusedInstanceOptResponse;
import com.bootest.dto.optimizer.GetUnusedVolOptResponse;
import com.bootest.dto.optimizer.GetVolOptResponse;
import com.bootest.dto.optimizer.GetVolOptSavingResponse;
import com.bootest.dto.optimizer.RightSizeThresholdRequest;
import com.bootest.model.Account;
import com.bootest.model.AwsInstanceType;
import com.bootest.model.AwsServicePricing;
import com.bootest.model.ResourceUsage;
import com.bootest.repository.AwsInstanceTypeRepo;
import com.bootest.repository.AwsServicePricingRepo;
import com.bootest.repository.ResourceUsageRepo;
import com.bootest.searcher.SearchBuilder;
import com.bootest.searcher.SearchOperationType;
import com.bootest.type.InstanceSqlType;
import com.bootest.type.ServiceType;
import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceLifecycleType;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeState;
import software.amazon.awssdk.services.ec2.model.VolumeType;

@Service
@RequiredArgsConstructor
public class UnusedAndVersionOptService {

    // private final VrmResourceUsageRepo vrmResourceUsageRepo;
    private final Ec2ResourceDescribeService ec2Desc;
    private final AwsInstanceTypeRepo awsInstanceTypeRepo;
    private final ObjectMapper mapper;
    private final UsageService usageService;
    private final AwsServicePricingRepo awsServicePricingRepo;
    private final ResourceUsageRepo resourceUsageRepo;
    private final InstanceTypePricingService itPricingService;

    //한달치 가격 예상
    public Float getInstanceMonthlySavings(String lifeCycle, String instanceType, String regionId, InstanceSqlType os,
            String availZone) throws JsonMappingException, JsonProcessingException {
        Float result = 0f;

        AwsInstanceType od = awsInstanceTypeRepo.findByRegionAndInstanceType(regionId, instanceType).orElse(null);
        if (od != null) {
            result = itPricingService.getOdPriceByOs(os, od) * 24 * 30;
        }

        return result;
    }

    public AwsInstanceType getModernizeRecommendation(InstanceSqlType os, AwsInstanceType currentInstanceType,
            List<AwsInstanceType> itsInSameFam, String architecture)
            throws JsonMappingException, JsonProcessingException {
        AwsInstanceType result = null;

        List<AwsInstanceType> filteredInstanceTypes = new ArrayList<>();
        String currentFamily = currentInstanceType.getInstanceType().split("\\.")[0];

        for (AwsInstanceType ait : itsInSameFam) {

            String replaceFamily = ait.getInstanceType().split("\\.")[0];

            if (!replaceFamily.contains(currentFamily)) {

                if (os != null) {
                    //해당 os를 지원하는 인스턴스인지 찾아줌
                    if (os.equals(InstanceSqlType.LINUX)) {
                        if (ait.getOdLinuxPricing() != null) {
                            if (ait.getOdLinuxPricing() < currentInstanceType.getOdLinuxPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.LINUX_SQL)) {
                        if (ait.getOdLinuxPricing() != null) {
                            if (ait.getOdLinuxPricing() < currentInstanceType.getOdLinuxPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.LINUX_SQL_ENT)) {
                        if (ait.getOdLinuxPricing() != null) {
                            if (ait.getOdLinuxPricing() < currentInstanceType.getOdLinuxPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.LINUX_SQL_WEB)) {
                        if (ait.getOdLinuxPricing() != null) {
                            if (ait.getOdLinuxPricing() < currentInstanceType.getOdLinuxPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.WINDOWS)) {
                        if (ait.getOdWindowsPricing() != null) {
                            if (ait.getOdWindowsPricing() < currentInstanceType.getOdWindowsPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.WINDOWS_SQL)) {
                        if (ait.getOdWindowsPricing() != null) {
                            if (ait.getOdWindowsPricing() < currentInstanceType.getOdWindowsPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.WINDOWS_SQL_ENT)) {
                        if (ait.getOdWindowsPricing() != null) {
                            if (ait.getOdWindowsPricing() < currentInstanceType.getOdWindowsPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.WINDOWS_SQL_WEB)) {
                        if (ait.getOdWindowsPricing() != null) {
                            if (ait.getOdWindowsPricing() < currentInstanceType.getOdWindowsPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.RHEL)) {
                        if (ait.getOdLinuxPricing() != null) {
                            if (ait.getOdLinuxPricing() < currentInstanceType.getOdLinuxPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.RHEL_SQL)) {
                        if (ait.getOdLinuxPricing() != null) {
                            if (ait.getOdLinuxPricing() < currentInstanceType.getOdLinuxPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.RHEL_SQL_ENT)) {
                        if (ait.getOdLinuxPricing() != null) {
                            if (ait.getOdLinuxPricing() < currentInstanceType.getOdLinuxPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.RHEL_SQL_WEB)) {
                        if (ait.getOdLinuxPricing() != null) {
                            if (ait.getOdLinuxPricing() < currentInstanceType.getOdLinuxPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    } else if (os.equals(InstanceSqlType.SUSE)) {
                        if (ait.getOdLinuxPricing() != null) {
                            if (ait.getOdLinuxPricing() < currentInstanceType.getOdLinuxPricing()) {
                                filteredInstanceTypes.add(ait);
                            }
                        }
                    }
                }
            }

        }

        List<AwsInstanceType> sortedTypes = filteredInstanceTypes.stream()
                .sorted(Comparator.comparing(AwsInstanceType::getOdLinuxPricing)).collect(Collectors.toList());

        if (!sortedTypes.isEmpty()) {
            if (!sortedTypes.get(0).getInstanceType().equals(currentInstanceType.getInstanceType())) {
                result = sortedTypes.get(0);
            }
        }
        return result;
    }

    public String raiseGeneration(String generation) {
        String result = "";

        for (int i = 0; i < generation.length(); i++) {
            char character = generation.charAt(i);
            String charToStr = Character.toString(character);

            try {
                Integer genNumber = Integer.parseInt(charToStr);
                Integer versionUp = genNumber + 1;
                result += versionUp.toString();
            } catch (Exception e) {
                result += charToStr;
            }
        }
        return result;
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<GetInstanceOptResponse> getInstanceOptimizable(Account credential, Ec2Client ec2,
            String regionId) throws JsonMappingException, JsonProcessingException {

        List<GetUnusedInstanceOptResponse> unusedResults = new ArrayList<>();
        List<GetModernInstanceOptResponse> outdatedResults = new ArrayList<>();

        // 인스턴스 정보 조회
        List<Instance> instances = ec2Desc.getInstanceDesc(ec2, null);

        if (instances != null && !instances.isEmpty()) {
            for (Instance i : instances) {

                InstanceSqlType os = itPricingService.getInstanceOperatingSystem(i.usageOperation());

                String architecture = i.architectureAsString();
                String lifeCycle = i.instanceLifecycle() == InstanceLifecycleType.SPOT ? "spot" : "on-demand";
                String resourceName = ec2Desc.getResourceName(i.tags());

                // Unused instances collector  미사용 인스턴스 조회
                if (i.state().name().equals(InstanceStateName.STOPPED) && os != null) {

                    Float savings = getInstanceMonthlySavings(lifeCycle, i.instanceTypeAsString(), regionId, os,
                            i.placement().availabilityZone());

                    String reason = "Unused Resource";

                    unusedResults.add(new GetUnusedInstanceOptResponse(i, os.toString(), regionId,
                            credential.getAccountId(), resourceName, reason, savings, lifeCycle));
                }

                //현 인스턴스 유형 정보 조회
                AwsInstanceType currentInstanceType = awsInstanceTypeRepo
                        .findByRegionAndInstanceType(regionId, i.instanceTypeAsString()).orElse(null);

                // Modernize instances collector
                if (currentInstanceType != null) {
                    String generation = null;
                    String[] instanceTypeSplit = i.instanceTypeAsString().split("\\.");
                    String size = null;
                    String reason = "Outdated Resource Type";

                    if (instanceTypeSplit.length >= 2) {
                        size = instanceTypeSplit[1];  // micro, small, medium
                        generation = instanceTypeSplit[0]; // t2, t3, c5
                    }

                    // 현 세대를 전달 후 한세대 오려서 값을 받는다 t2 -> t3
                    String targetGen = raiseGeneration(generation);

                    // 조회한 세대가 실제로 있는지 찾아봄
                    SearchBuilder<AwsInstanceType> searchBuilder = new SearchBuilder<>();
                    searchBuilder.with("region", SearchOperationType.EQUAL, regionId);
                    searchBuilder.with("instanceType", SearchOperationType.CONTAINS_IGNORE_CASE, size);
                    searchBuilder.with("instanceType", SearchOperationType.START_WITH_IGNORE_CASE, targetGen);

                    List<AwsInstanceType> awsInsTypes = awsInstanceTypeRepo.findAll(searchBuilder.build());

                    if (!awsInsTypes.isEmpty()) {
                        AwsInstanceType recommendation = getModernizeRecommendation(os, currentInstanceType,
                                awsInsTypes, architecture);

                        if (recommendation != null) {
                            // 원가
                            Float originMntlyCost = 0f;
                            // 추천 가격
                            Float recommendedMntlyCost = 0f;

                            if (os != null) {
                                originMntlyCost = itPricingService.getOdPriceByOs(os, currentInstanceType) * 24 * 30;
                                recommendedMntlyCost = itPricingService.getOdPriceByOs(os, recommendation) * 24 * 30;
                            }

                            if (recommendedMntlyCost > 0) {
                                Float savings = originMntlyCost - recommendedMntlyCost;

                                String osValue = null;

                                if (os != null) {
                                    osValue = os.toString();
                                }

                                outdatedResults.add(new GetModernInstanceOptResponse(i, lifeCycle, reason, savings,
                                        credential.getAccountId(), resourceName, osValue, currentInstanceType,
                                        recommendation, regionId));
                            }
                        }
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(new GetInstanceOptResponse(unusedResults, outdatedResults));
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<GetInstanceRightSizeOptResponse> rightSizeOpt(Set<String> dateSet, ResourceUsage usage,
            Instant to, Instant from, RightSizeThresholdRequest rstRequest)
            throws JsonMappingException, JsonProcessingException {

        List<ResourceUsage> mergedCpuUsageData = getUsagesByPeriodAndDataType(dateSet, usage, UsageDataType.CPU);
        List<ResourceUsage> mergedMemUsageData = getUsagesByPeriodAndDataType(dateSet, usage, UsageDataType.MEMORY);
        List<ResourceUsage> mergedNetInUsageData = getUsagesByPeriodAndDataType(dateSet, usage, UsageDataType.NET_IN);
        List<ResourceUsage> mergedNetOutUsageData = getUsagesByPeriodAndDataType(dateSet, usage, UsageDataType.NET_OUT);

        Double avgCpu = null;
        Double maxCpu = null;
        if (!mergedCpuUsageData.isEmpty()) {
            if (!rstRequest.getAvgCpu().isNaN() && rstRequest.getAvgCpu() != null && rstRequest.getAvgCpu() > 0) {

                // 평균 cpu 사용량 맵핑
                Map<String, Double> avgCpuMap = getAvgStatistics(mergedCpuUsageData, "avg");
                // 평균 맵핑의 평균치 구함
                avgCpu = usageService.getAvgUsage(avgCpuMap, from, to);

            }

            if (rstRequest.getMaxCpu() != null && !rstRequest.getMaxCpu().isNaN() && rstRequest.getMaxCpu() > 0) {

                Map<String, Double> maxCpuMap = getAvgStatistics(mergedCpuUsageData, "max");

                maxCpu = usageService.getAvgUsage(maxCpuMap, from, to);

            }
        }

        Double avgMem = null;
        Double maxMem = null;
        if (!mergedMemUsageData.isEmpty()) {
            if (!rstRequest.getAvgMem().isNaN() && rstRequest.getAvgMem() != null && rstRequest.getAvgMem() > 0) {

                Map<String, Double> avgMemMap = getAvgStatistics(mergedMemUsageData, "avg");

                avgMem = usageService.getAvgUsage(avgMemMap, from, to);

            }

            if (!rstRequest.getMaxMem().isNaN() && rstRequest.getMaxMem() != null && rstRequest.getMaxMem() > 0) {

                Map<String, Double> maxMemMap = getAvgStatistics(mergedMemUsageData, "max");

                maxMem = usageService.getAvgUsage(maxMemMap, from, to);

            }
        }

        Double avgNetIn = null;
        Double avgNetOut = null;
        if (!mergedNetInUsageData.isEmpty()) {
            if (rstRequest.getAvgNetIn() != null && !rstRequest.getAvgNetIn().isNaN() && rstRequest.getAvgNetIn() > 0) {

                Map<String, Double> avgNetInMap = getAvgStatistics(mergedNetInUsageData, "avg");

                Map<String, Double> avgNetOutMap = getAvgStatistics(mergedNetOutUsageData, "avg");

                avgNetIn = usageService.getAvgUsage(avgNetInMap, from, to);

                avgNetOut = usageService.getAvgUsage(avgNetOutMap, from, to);

            }
        }
        return CompletableFuture.completedFuture(
                rightSizeRecommendation(usage, avgCpu, maxCpu, avgMem, maxMem, avgNetIn, avgNetOut, rstRequest));
    }

    public GetInstanceRightSizeOptResponse rightSizeRecommendation(
            ResourceUsage usage,
            Double avgCpu,
            Double maxCpu,
            Double avgMem,
            Double maxMem,
            Double avgNetIn,
            Double avgNetOut,
            RightSizeThresholdRequest rstRequest) throws JsonMappingException, JsonProcessingException {
        GetInstanceRightSizeOptResponse result = null;

        Boolean modify = false;

        if (avgCpu != null) {
            if (avgCpu < rstRequest.getAvgCpu()) {
                modify = true;
            }
        }

        if (maxCpu != null) {
            if (maxCpu < rstRequest.getMaxCpu()) {
                modify = true;
            }
        }

        if (avgMem != null) {
            if (avgMem < rstRequest.getAvgMem()) {
                modify = true;
            }
        }

        if (maxMem != null) {
            if (maxMem < rstRequest.getMaxMem()) {
                modify = true;
            }
        }

        if (avgNetIn != null) {
            Double netInMbs = avgNetIn / 3600 / 10000;
            if (netInMbs < rstRequest.getAvgNetIn()) {
                modify = true;
            }
        }

        if (avgNetOut != null) {
            Double netOutMbs = avgNetOut / 3600 / 10000;
            if (netOutMbs < rstRequest.getAvgNetOut()) {
                modify = true;
            }
        }

        if (modify) {

            GetInstanceRightSizeOptResponse recommendedResult = getRecommendedInstanceType(usage);

            if (recommendedResult != null) {
                result = recommendedResult;
            }

        }
        return result;
    }

    public List<ResourceUsage> getUsagesByPeriodAndDataType(
            Set<String> dates,
            ResourceUsage data,
            UsageDataType dataType) {
        List<ResourceUsage> results = new ArrayList<>();

        for (String date : dates) {
            String[] dateArr = date.split("-");
            ResourceUsage usage = resourceUsageRepo
                    .findByResourceIdAndAnnuallyAndMonthlyAndDataType(data.getResourceId(),
                            Short.parseShort(dateArr[0]), Short.parseShort(dateArr[1]), dataType)
                    .orElse(null);

            if (usage != null) {
                results.add(usage);
            }
        }
        return results;
    }

    public Map<String, Double> getAvgStatistics(List<ResourceUsage> usages, String measurement)
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

    // 바꿔야 할 경우 사이즈 한단계 낮춰서 비용 비교 후 추천 전달
    public GetInstanceRightSizeOptResponse getRecommendedInstanceType(ResourceUsage usage)
            throws JsonMappingException, JsonProcessingException {
        GetInstanceRightSizeOptResponse rightSizeResult = null;

        List<AwsInstanceType> recommendedResults = new ArrayList<>();

        AwsInstanceType currentInstancetype = awsInstanceTypeRepo
                .findByRegionAndInstanceType(usage.getRegion(), usage.getInstanceType()).orElse(null);

        if (currentInstancetype != null) {
            SearchBuilder<AwsInstanceType> searchBuilder = SearchBuilder.builder();
            searchBuilder.with("region", SearchOperationType.EQUAL, usage.getRegion());
            searchBuilder.with("instanceType", SearchOperationType.START_WITH_IGNORE_CASE,
                    currentInstancetype.getInstanceType().split("\\.")[0]);
            searchBuilder.with("vcpus", SearchOperationType.LESS_THAN_OR_EQUAL, currentInstancetype.getVcpus());
            searchBuilder.with("memoryGib", SearchOperationType.LESS_THAN_OR_EQUAL, currentInstancetype.getMemoryGib());

            if (currentInstancetype.getOdLinuxPricing() != null) {
                searchBuilder.with("odLinuxPricing", SearchOperationType.LESS_THAN,
                        currentInstancetype.getOdLinuxPricing());
            }

            List<AwsInstanceType> recommendedTypes = awsInstanceTypeRepo.findAll(searchBuilder.build());

            if (!recommendedTypes.isEmpty()) {

                List<AwsInstanceType> sortedTypes = recommendedTypes.stream()
                        .sorted(Comparator.comparing(AwsInstanceType::getVcpus, Comparator.reverseOrder()))
                        .collect(Collectors.toList());

                for (AwsInstanceType ait : sortedTypes) {
                    if (ait.getVcpus() == sortedTypes.get(0).getVcpus()) {
                        recommendedResults.add(ait);
                    }
                }
            }
        }

        if (!recommendedResults.isEmpty()) {
            String reason = "Underutilized Resource";
            Float originalMonthlyPrice = 0f;
            Float changedMonthlyPrice = 0f;

            if (currentInstancetype != null) {
                if (usage.getOs().equals("Linux")) {
                    originalMonthlyPrice = currentInstancetype.getOdLinuxPricing() * 24 * 30;
                    changedMonthlyPrice = recommendedResults.get(0).getOdLinuxPricing() * 24 * 30;
                } else {
                    originalMonthlyPrice = currentInstancetype.getOdWindowsPricing() * 24 * 30;
                    changedMonthlyPrice = recommendedResults.get(0).getOdWindowsPricing() * 24 * 30;
                }
            }

            Float savings = originalMonthlyPrice - changedMonthlyPrice;

            if (savings > 0) {
                rightSizeResult = new GetInstanceRightSizeOptResponse(usage, usage.getResourceId(),
                        originalMonthlyPrice, changedMonthlyPrice, recommendedResults.get(0).getInstanceType(), reason,
                        savings, currentInstancetype, recommendedResults);
            }
        }

        return rightSizeResult;
    }

    public GetVolOptSavingResponse getVolumeMonthlySavings(String resourceType, String regionId, Volume v) {

        Float sizeCost = 0f;
        Float iopsCost = 0f;
        Float throughputCost = 0f;

        Integer size = v.size();
        Integer iops = v.iops();
        Integer throughput = v.throughput();

        List<AwsServicePricing> volServicePricing = awsServicePricingRepo
                .findByServiceTypeAndResourceTypeAndRegion(ServiceType.VOLUME, resourceType, regionId);

        for (AwsServicePricing vsp : volServicePricing) {

            if (resourceType.equals("gp3")) {

                if (vsp.getPricingUnit().equals("GiBps-mo")) {

                    if (throughput != null) {
                        if (throughput > 125) {

                            throughput = throughput - 125;

                            if (throughput < 0) {
                                throughput = 0;
                            }

                            String[] throughputDescArr = vsp.getPricingDescription().split(" ");

                            if (throughputDescArr.length >= 1) {
                                String throughputPrice = throughputDescArr[0];
                                throughputCost = Float.parseFloat(throughputPrice.replace("$", ""));
                            }
                        }
                    }

                } else if (vsp.getPricingUnit().equals("IOPS-Mo")) {
                    if (iops > 3000) {
                        Integer actualIops = iops - 3000;
                        if (actualIops > 0) {
                            iopsCost = actualIops * vsp.getPricePerUnit();
                        }
                    }
                } else if (vsp.getPricingUnit().equals("GB-Mo")) {
                    sizeCost = size * vsp.getPricePerUnit();
                }

            } else if (resourceType.equals("io1")) {

                if (vsp.getPricingUnit().equals("IOPS-Mo")) {
                    iopsCost = iops * vsp.getPricePerUnit();
                } else if (vsp.getPricingUnit().equals("GB-Mo")) {
                    sizeCost = size * vsp.getPricePerUnit();
                }

            } else if (resourceType.equals("io2")) {

                if (vsp.getPricingUnit().equals("IOPS-Mo")) {

                    if (iops <= 32000) {
                        if (vsp.getUsageType().equals("VolumeP-IOPS.io2")) {
                            iopsCost = iops * vsp.getPricePerUnit();
                        }
                    } else if (iops <= 64000) {
                        Integer tier2Iops = iops - 32000;

                        Float tier1IopsCost = 0f;
                        if (vsp.getUsageType().equals("VolumeP-IOPS.io2")) {
                            tier1IopsCost = 32000 * vsp.getPricePerUnit();
                        }

                        Float tier2IopsCost = 0f;
                        if (vsp.getUsageType().equals("VolumeP-IOPS.io2.tier2")) {
                            tier2IopsCost = tier2Iops * vsp.getPricePerUnit();
                        }

                        iopsCost = tier1IopsCost + tier2IopsCost;

                    } else {
                        Integer tier3Iops = iops - 64000;

                        Float tier1IopsCost = 0f;
                        if (vsp.getUsageType().equals("VolumeP-IOPS.io2")) {
                            tier1IopsCost = 32000 * vsp.getPricePerUnit();
                        }

                        Float tier2IopsCost = 0f;
                        if (vsp.getUsageType().equals("VolumeP-IOPS.io2.tier2")) {
                            tier2IopsCost = 32000 * vsp.getPricePerUnit();
                        }

                        Float tier3IopsCost = 0f;
                        if (vsp.getUsageType().equals("VolumeP-IOPS.io2.tier3")) {
                            iopsCost = tier3Iops * vsp.getPricePerUnit();
                        }

                        iopsCost = tier1IopsCost + tier2IopsCost + tier3IopsCost;

                    }
                } else if (vsp.getPricingUnit().equals("GB-Mo")) {
                    sizeCost = size * vsp.getPricePerUnit();
                }

            } else {

                if (vsp.getPricingUnit().equals("GB-Mo")) {
                    sizeCost = size * vsp.getPricePerUnit();
                }

            }

        }

        Float totalEstimatedSavings = sizeCost + iopsCost + throughputCost;

        return new GetVolOptSavingResponse(totalEstimatedSavings, sizeCost, iopsCost, throughputCost);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<GetVolOptResponse> getVolumeOptimizable(Account credential, Ec2Client ec2, String regionId)
            throws JsonParseException, JsonMappingException, IOException {

        List<GetUnusedVolOptResponse> unusedResults = new ArrayList<>();
        List<GetModernVolOptResponse> outDatedResults = new ArrayList<>();

        List<Volume> volumes = ec2Desc.getVolumeDesc(ec2, null, null);

        if (volumes != null && !volumes.isEmpty()) {
            for (Volume v : volumes) {
                String resourceName = ec2Desc.getResourceName(v.tags());

                // Unused volume collector
                if (v.state().equals(VolumeState.AVAILABLE)) {
                    String reason = "Unused Resource";

                    GetVolOptSavingResponse savings = getVolumeMonthlySavings(v.volumeTypeAsString(), regionId, v);

                    unusedResults.add(new GetUnusedVolOptResponse(v, regionId, credential.getAccountId(), resourceName,
                            reason, savings.getTotalEstimatedSavings(), savings));

                } else if (v.state().equals(VolumeState.IN_USE)) {
                    if (v.volumeType().equals(VolumeType.GP2)) {
                        if (v.size() < 1000) {
                            String reason = "Updatable Resource Type";

                            AwsServicePricing gp2ServicePricing = awsServicePricingRepo
                                    .findByServiceTypeAndUsageTypeAndRegion(ServiceType.VOLUME, "VolumeUsage.gp2",
                                            regionId)
                                    .orElse(null);
                            AwsServicePricing gp3ServicePricing = awsServicePricingRepo
                                    .findByServiceTypeAndUsageTypeAndRegion(ServiceType.VOLUME, "VolumeUsage.gp3",
                                            regionId)
                                    .orElse(null);

                            Float originalCost = gp2ServicePricing.getPricePerUnit() * v.size();
                            Float changedCost = gp3ServicePricing.getPricePerUnit() * v.size();

                            Float estimatedMonthlySavings = originalCost - changedCost;

                            if (estimatedMonthlySavings > 0) {
                                outDatedResults.add(new GetModernVolOptResponse(v, reason, estimatedMonthlySavings,
                                        credential.getAccountId(), resourceName, originalCost, changedCost, regionId));
                            }
                        }
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(new GetVolOptResponse(unusedResults, outDatedResults));
    }

}
