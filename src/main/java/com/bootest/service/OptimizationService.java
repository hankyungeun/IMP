package com.bootest.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.optimizer.GetInstanceOptResponse;
import com.bootest.dto.optimizer.GetInstanceRightSizeOptResponse;
import com.bootest.dto.optimizer.GetResourceOptResponse;
import com.bootest.dto.optimizer.GetRightSizeOptResponse;
import com.bootest.dto.optimizer.OptimizationRequestDataDto;
import com.bootest.dto.optimizer.RightSizeThresholdRequest;
import com.bootest.model.Account;
import com.bootest.model.ResourceUsage;
import com.bootest.repository.ResourceUsageRepo;
import com.bootest.type.OptimizationType;
import com.bootest.type.ServiceType;
import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypeOfferingsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypeOfferingsResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceTypeOffering;
import software.amazon.awssdk.services.ec2.model.LocationType;
import software.amazon.awssdk.services.ec2.model.Volume;

@Slf4j
@RequiredArgsConstructor
@Service
public class OptimizationService {

    private final Ec2ResDescribeService ec2Desc;
    private final Ec2ResStateChange state;
    private final Ec2ClientManager awscm;
    private final InstancesService instanceService;
    private final VolumeService volumeService;
    private final ResourceUsageRepo resourceUsageRepo;
    private final UnusedAndVersionOptService unusedAndVersionOptService;

    public GetResourceOptResponse findOptimizable(Account a, String regionId)
            throws InterruptedException, IOException, ExecutionException {

        Region region = Region.of(regionId);

        Ec2Client ec2 = awscm.getEc2WithAccount(region, a);

        CompletableFuture<GetInstanceOptResponse> instanceOpts = unusedAndVersionOptService.getInstanceOptimizable(a,
                ec2, regionId);

        // CompletableFuture<GetVolOptResponse> volOpts =
        // unusedAndVersionOptService.getVolumeOptimizable(a, ec2,
        // regionId);

        return new GetResourceOptResponse(regionId, instanceOpts.get());
    }

    public GetRightSizeOptResponse findRightSizable(Account credential, String regionId,
            RightSizeThresholdRequest rstRequest, Integer days)
            throws InterruptedException, ExecutionException, JsonMappingException, JsonProcessingException {
        List<GetInstanceRightSizeOptResponse> results = new ArrayList<>();

        Integer year = 2023;
        Integer month = 3;

        LocalDate now = LocalDate.of(year, month, 23);

        // 사용량 조회
        List<ResourceUsage> usages = resourceUsageRepo
                .findAllByAccountIdAndRegionAndResourceStateAndAnnuallyAndMonthlyAndDataType(
                        credential.getAccountId(), regionId, "running", year.shortValue(), month.shortValue(),
                        UsageDataType.CPU);

        LocalDate minusPeriod = now.minusDays(days);

        Instant to = now.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant from = minusPeriod.atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<LocalDate> dates = minusPeriod.datesUntil(now).collect(Collectors.toList());

        Set<String> dateSet = new HashSet<>();
        for (LocalDate ld : dates) {
            String[] ldArray = ld.toString().split("-");
            dateSet.add(ldArray[0] + "-" + ldArray[1]);
        }

        for (ResourceUsage usage : usages) {
            CompletableFuture<GetInstanceRightSizeOptResponse> rightSizingData = unusedAndVersionOptService
                    .rightSizeOpt(dateSet, usage, to, from, rstRequest);

            if (rightSizingData.get() != null) {
                results.add(rightSizingData.get());
            }
        }

        return new GetRightSizeOptResponse(regionId, results);
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Boolean> optimizer(OptimizationRequestDataDto temp, Account credential)
            throws JsonParseException, JsonMappingException, IOException, InterruptedException {

        Boolean resourceOptimized = false;

        Region region = Region.of(temp.getRegion());

        Ec2Client ec2 = awscm.getEc2WithAccount(region, credential);

        if (temp.getServiceType().equals(ServiceType.INSTANCE)) {

            List<Instance> instances = ec2Desc.getInstanceDesc(ec2, temp.getResourceId());

            if (instances != null && !instances.isEmpty()) {

                String lifecycle = instances.get(0).instanceLifecycleAsString() == null ? "on-demand" : "spot";
                String resourceName = ec2Desc.getResourceName(instances.get(0).tags());

                if (temp.getOptimizationType().equals(OptimizationType.UNUSED)) {

                    try {
                        instanceService.terminateInstance(ec2, List.of(temp.getResourceId()));

                        resourceOptimized = true;
                    } catch (Exception e) {
                        log.error("Unable to terminate instance id: {}, due to reason: {}", temp.getResourceId(),
                                e.getMessage(), e);
                    }

                } else if (temp.getOptimizationType().equals(OptimizationType.RIGHT_SIZE)) {

                    try {
                        Boolean rightSized = doInstanceRightSize(ec2, instances.get(0), lifecycle, temp.getResourceId(),
                                temp.getRecommendation(), resourceName);

                        if (rightSized) {
                            resourceOptimized = true;
                        }
                    } catch (Exception e) {
                        log.error("Right-Size request failed. message: {}", e.getMessage(), e);
                    }

                } else if (temp.getOptimizationType().equals(OptimizationType.VERSION_UP)) {

                    try {
                        Boolean versionUp = doInstanceRightSize(ec2, instances.get(0), lifecycle, temp.getResourceId(),
                                temp.getRecommendation(), resourceName);

                        if (versionUp) {
                            resourceOptimized = true;
                        }
                    } catch (Exception e) {
                        log.error("Version-Up request failed. message: {}", e.getMessage(), e);
                    }

                }

            }

        } else if (temp.getServiceType().equals(ServiceType.VOLUME)) {

            List<Volume> volumes = ec2Desc.getVolumeDesc(ec2, temp.getResourceId(), null);

            if (volumes != null && !volumes.isEmpty()) {
                if (temp.getOptimizationType().equals(OptimizationType.UNUSED)) {

                    try {
                        volumeService.deleteVolume(ec2, temp.getResourceId());

                        resourceOptimized = true;
                    } catch (Exception e) {
                        log.error("Delete volume request failed. message: {}", e.getMessage(), e);
                    }

                } else if (temp.getOptimizationType().equals(OptimizationType.VERSION_UP)) {

                    try {
                        Boolean optimized = volumeService.modifyVolumeType(ec2, temp.getResourceId(),
                                temp.getRecommendation());

                        if (optimized) {
                            resourceOptimized = true;
                        }
                    } catch (Exception e) {
                        log.error("Modify volume request failed. message: {}", e.getMessage(), e);
                    }
                }
            }

        }
        return CompletableFuture.completedFuture(resourceOptimized);
    }

    public Boolean doInstanceRightSize(Ec2Client ec2, Instance i, String lifecycle, String instanceId,
            String recommendedType, String region) throws InterruptedException {
        Boolean optimized = false;

        if (!lifecycle.equals("spot")) {
            List<String> aZones = getInstanceTypeOffering(ec2, recommendedType);

            for (String zone : aZones) {
                if (i.placement().availabilityZone().equals(zone)) {
                    state.stopInstance(ec2, instanceId);
                    instanceService.modifyInstanceAttribute(ec2, instanceId, recommendedType);
                    optimized = true;

                    state.startInstance(instanceId, ec2);
                }
            }
        }
        return optimized;
    }

    public List<String> getInstanceTypeOffering(Ec2Client ec2, String type) {
        List<String> results = new ArrayList<>();

        Filter filter = Filter.builder().name("instance-type").values(type).build();

        DescribeInstanceTypeOfferingsRequest request = DescribeInstanceTypeOfferingsRequest.builder()
                .filters(filter)
                .locationType(LocationType.AVAILABILITY_ZONE)
                .build();

        try {
            DescribeInstanceTypeOfferingsResponse response = ec2.describeInstanceTypeOfferings(request);
            for (InstanceTypeOffering ito : response.instanceTypeOfferings()) {
                results.add(ito.location());
            }
        } catch (Exception e) {
            log.error("Describe Instance Type Offering Request Failed (Message: {})", e.getMessage(), e);
        }
        return results;
    }

}
