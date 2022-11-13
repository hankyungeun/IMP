package com.bootest.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.optimizer.InstanceResourceDetailsDto;
import com.bootest.dto.optimizer.OptimizationRequestDataDto;
import com.bootest.dto.optimizer.OptimizationTargetDataDto;
import com.bootest.dto.optimizer.RightSizeRecommendationDto;
import com.bootest.model.Account;
import com.bootest.model.AwsInstanceType;
import com.bootest.model.Optimizer;
import com.bootest.repository.AccountRepo;
import com.bootest.repository.AwsInstanceTypeRepo;
import com.bootest.repository.OptimizerRepo;
import com.bootest.searcher.SearchBuilder;
import com.bootest.searcher.SearchOperationType;
import com.bootest.type.OptimizationType;
import com.bootest.type.ServiceType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypeOfferingsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypeOfferingsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceLifecycleType;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.InstanceTypeInfo;
import software.amazon.awssdk.services.ec2.model.InstanceTypeOffering;
import software.amazon.awssdk.services.ec2.model.LocationType;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeState;

@Slf4j
@RequiredArgsConstructor
@Service
public class OptimizationService {

    private final Ec2ResDescribeService ec2Desc;
    private final Ec2ResStateChange state;
    private final AwsInstanceTypeRepo awsInstanceTypeRepo;
    private final Ec2ClientManager awscm;
    private final AccountRepo accountRepo;
    private final OptimizerRepo optimizerRepo;
    private final InstancesService instanceService;
    private final VolumeService volumeService;

    @Async("threadPoolTaskExecutor")
    public void optimize(OptimizationRequestDataDto temp)
            throws JsonParseException, JsonMappingException, IOException, InterruptedException {
        Optimizer optimizer = optimizerRepo.findByResourceIdAndOptimizationType(temp.getResourceId(), temp.getType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Resource ID " + temp.getResourceId()));

        Account account = accountRepo.findByAccountId(optimizer.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account ID " + optimizer.getAccountId()));

        Region region = Region.of(optimizer.getRegion());

        Ec2Client ec2 = awscm.getEc2WithAccount(region, account);

        if (optimizer.getServiceType().equals(ServiceType.INSTANCE)) {
            if (optimizer.getOptimizationType().equals(OptimizationType.UNUSED)) {
                if (instanceValidator(ec2, optimizer.getResourceId())) {
                    instanceService.terminateInstance(ec2, List.of(optimizer.getResourceId()));
                    optimizer.setOptimized(true);
                    optimizerRepo.save(optimizer);
                } else {
                    throw new IllegalArgumentException(
                            "Unable to terminate the instance: " + optimizer.getResourceId());
                }
            } else if (optimizer.getOptimizationType().equals(OptimizationType.RIGHT_SIZE)) {
                Boolean optimized = instanceRightSize(ec2, optimizer.getResourceId(), temp.getRecommendation(),
                        optimizer.getRegion());
                if (!optimized) {
                    throw new IllegalArgumentException(
                            "Instance Right-Size Request Failed: " + optimizer.getResourceId());
                } else {
                    optimizer.setOptimized(true);
                    optimizerRepo.save(optimizer);
                }
            } else if (optimizer.getOptimizationType().equals(OptimizationType.VERSION_UP)) {
                Boolean optimized = instanceRightSize(ec2, optimizer.getResourceId(), optimizer.getRecommendation(),
                        optimizer.getRegion());
                if (!optimized) {
                    throw new IllegalArgumentException(
                            "Instance Version-Up Request Failed: " + optimizer.getResourceId());
                } else {
                    optimizer.setOptimized(true);
                    optimizerRepo.save(optimizer);
                }
            }
        } else if (optimizer.getServiceType().equals(ServiceType.VOLUME)) {
            if (optimizer.getOptimizationType().equals(OptimizationType.UNUSED)) {
                if (volumeValidator(ec2, optimizer.getResourceId())) {
                    volumeService.deleteVolume(ec2, optimizer.getResourceId());
                    optimizer.setOptimized(true);
                    optimizerRepo.save(optimizer);
                } else {
                    throw new IllegalArgumentException("Delete Volume Request Failed: " + optimizer.getResourceId());
                }
            } else if (optimizer.getOptimizationType().equals(OptimizationType.VERSION_UP)) {
                Boolean optimized = volumeService.modifyVolumeType(ec2, optimizer.getResourceId(),
                        optimizer.getRecommendation());
                if (!optimized) {
                    throw new IllegalArgumentException(
                            "Volume Version-Up Request Failed: " + optimizer.getResourceId());
                } else {
                    optimizer.setOptimized(true);
                    optimizerRepo.save(optimizer);
                }
            }
        }
    }

    public Boolean volumeValidator(Ec2Client ec2, String volumeId) {
        Boolean optimize = false;
        List<Volume> volumes = ec2Desc.getVolumeDesc(ec2, volumeId, null);

        if (volumes != null && !volumes.isEmpty()) {
            for (Volume v : volumes) {
                if (v.state().equals(VolumeState.AVAILABLE)) {
                    optimize = true;
                }
            }
        }
        return optimize;
    }

    public Boolean instanceValidator(Ec2Client ec2, String instanceId) {
        Boolean optimize = false;
        List<Instance> instances = ec2Desc.getInstanceDesc(ec2, instanceId);

        if (instances != null && !instances.isEmpty()) {
            for (Instance i : instances) {
                if (!i.instanceLifecycle().equals(InstanceLifecycleType.SPOT)) {
                    if (i.state().name().equals(InstanceStateName.STOPPED)) {
                        optimize = true;
                    }
                }
            }
        }
        return optimize;
    }

    public Boolean instanceRightSize(Ec2Client ec2, String instanceId, String recommendation, String region)
            throws InterruptedException {
        Boolean optimized = false;

        List<Instance> instances = ec2Desc.getInstanceDesc(ec2, instanceId);

        if (instances != null && !instances.isEmpty()) {
            for (Instance i : instances) {
                String lifeCycle = i.instanceLifecycle() == InstanceLifecycleType.SPOT ? "spot" : "on-demand";
                if (!lifeCycle.equals("spot")) {
                    List<String> aZones = getInstanceTypeOffering(ec2, recommendation);

                    for (String zone : aZones) {
                        if (i.placement().availabilityZone().equals(zone)) {
                            state.stopInstance(ec2, instanceId);
                            try {
                                instanceService.modifyInstanceAttribute(ec2, instanceId, recommendation);
                            } catch (Exception e) {
                                log.error("Right-Size Failed");
                            }
                            state.startInstance(instanceId, ec2);
                            optimized = true;
                        }
                    }
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

    public OptimizationTargetDataDto getOptTargetData(Optimizer target) {
        OptimizationTargetDataDto data = new OptimizationTargetDataDto();
        data.setRegion(target.getRegion());
        data.setAccountId(target.getAccountId());
        data.setAccountName(target.getAccountName());
        data.setResourceId(target.getResourceId());
        data.setResourceName(target.getResourceName());
        data.setServiceType(target.getServiceType());
        data.setResourceType(target.getResourceType());
        data.setOptimizationType(target.getOptimizationType());
        data.setRecommendedAction(target.getRecommendedAction());
        data.setEstimatedMonthlySavings(target.getEstimatedMonthlySavings());

        if (target.getServiceType().equals(ServiceType.INSTANCE)
                && !target.getOptimizationType().equals(OptimizationType.UNUSED)) {

            Account account = accountRepo.findByAccountId(target.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Account ID"));
            Region region = Region.of(target.getRegion());
            Ec2Client ec2 = awscm.getEc2WithAccount(region, account);

            // Current Instance Info
            InstanceResourceDetailsDto instanceDetails = getInstanceResourceDetails(target.getRegion(),
                    target.getResourceType(), target.getInstanceOs(), ec2, null);
            data.setInstanceDetails(instanceDetails);

            if (target.getOptimizationType().equals(OptimizationType.RIGHT_SIZE)) {
                // Right Size Recommendations
                data.setRightSizeRecommendations(getInstanceRecommendation(target, instanceDetails, ec2));
            } else if (target.getOptimizationType().equals(OptimizationType.VERSION_UP)) {
                // Version Up Recommendation
                data.setVersionUpInstanceRecommendation(getInstanceResourceDetails(target.getRegion(),
                        target.getRecommendation(), target.getInstanceOs(), ec2, instanceDetails.getMonthlyPrice()));
            }
        }
        return data;
    }

    public InstanceResourceDetailsDto getInstanceResourceDetails(
            String regionStr,
            String resourceType,
            String os,
            Ec2Client ec2,
            Float currentMonthly) {
        AwsInstanceType instanceType = awsInstanceTypeRepo.findByRegionAndInstanceType(regionStr, resourceType)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Resource Info"));

        DescribeInstanceTypesRequest request = DescribeInstanceTypesRequest.builder()
                .instanceTypesWithStrings(resourceType)
                .build();

        try {
            DescribeInstanceTypesResponse response = ec2.describeInstanceTypes(request);

            InstanceResourceDetailsDto data = new InstanceResourceDetailsDto();

            for (InstanceTypeInfo info : response.instanceTypes()) {

                Float hourlyPrice = 0f;
                if (os.equals("Linux")) {
                    hourlyPrice = instanceType.getOdLinuxPricing();
                } else {
                    hourlyPrice = instanceType.getOdWindowsPricing();
                }

                Float montlyPrice = hourlyPrice * 24 * 30;

                data.setOs(os);
                data.setInstanceType(info.instanceTypeAsString());
                data.setVCpus(instanceType.getVcpus().intValue());
                data.setMemoryGiB(instanceType.getMemoryGib());
                data.setNetworkPerformance(info.networkInfo().networkPerformance());
                data.setHourlyPrice(hourlyPrice);
                data.setMonthlyPrice(montlyPrice);
                data.setHypervisor(info.hypervisorAsString());
                data.setArchitecture(info.processorInfo().supportedArchitectures().toString());
                data.setSustainedClockSpeedInGhz(info.processorInfo().sustainedClockSpeedInGhz());

                if (currentMonthly != null) {
                    data.setEstimatedMonthlySavings(currentMonthly - montlyPrice);
                }
            }
            return data;
        } catch (Exception e) {
            log.error("Describe Instance Type Request Failed (Message: {})", e.getMessage(), e);
            return null;
        }
    }

    public List<RightSizeRecommendationDto> getInstanceRecommendation(
            Optimizer optimizer,
            InstanceResourceDetailsDto details,
            Ec2Client ec2) {
        List<RightSizeRecommendationDto> results = new ArrayList<>();

        if (details != null) {

            SearchBuilder<AwsInstanceType> searchBuilder = SearchBuilder.builder();
            searchBuilder.with("region", SearchOperationType.EQUAL, optimizer.getRegion());
            searchBuilder.with("vCpus", SearchOperationType.EQUAL, details.getVCpus());
            searchBuilder.with("memoryGiB", SearchOperationType.EQUAL, details.getMemoryGiB());
            List<AwsInstanceType> instanceTypes = awsInstanceTypeRepo.findAll(searchBuilder.build());

            if (!instanceTypes.isEmpty()) {
                for (AwsInstanceType vit : instanceTypes) {
                    if (details.getOs().equals("Linux")) {
                        if (details.getHourlyPrice() >= vit.getOdLinuxPricing()) {
                            RightSizeRecommendationDto additional = new RightSizeRecommendationDto();
                            additional.setRecommendation(getInstanceResourceDetails(optimizer.getRegion(),
                                    vit.getInstanceType(), optimizer.getInstanceOs(), ec2, details.getMonthlyPrice()));
                            results.add(additional);
                        }
                    } else {
                        if (details.getHourlyPrice() >= vit.getOdWindowsPricing()) {
                            RightSizeRecommendationDto additional = new RightSizeRecommendationDto();
                            additional.setRecommendation(getInstanceResourceDetails(optimizer.getRegion(),
                                    vit.getInstanceType(), optimizer.getInstanceOs(), ec2, details.getMonthlyPrice()));
                            results.add(additional);
                        }
                    }
                }
            }
        }
        return results;
    }

}
