package com.bootest.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.bootest.dto.optimizer.RegisterOptimizerDto;
import com.bootest.model.Account;
import com.bootest.model.AwsInstanceType;
import com.bootest.model.Optimizer;
import com.bootest.repository.AwsInstanceTypeRepo;
import com.bootest.repository.OptimizerRepo;
import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeState;
import software.amazon.awssdk.services.ec2.model.VolumeType;

@Service
@RequiredArgsConstructor
public class UnusedAndVersionOpportunityService {

    private final Ec2ResDescribeService ec2Desc;
    private final InstanceFamilyUpToDate upToDate;
    private final OptimizerRepo optimizerRepo;
    private final AwsInstanceTypeRepo awsInstanceTypeRepo;
    private final FindOnDemandPrice findOnDemandPrice;

    @Async("threadPoolTaskExecutor")
    public void instanceOptimizationTask(Account a, Ec2Client ec2, String regionStr) {

        List<Instance> instances = ec2Desc.getInstanceDesc(ec2, null);

        if (instances != null && !instances.isEmpty()) {
            for (Instance i : instances) {

                RegisterOptimizerDto registerTemp = new RegisterOptimizerDto();

                String os = i.platformAsString() == null ? "Linux" : "Windows";

                if (i.state().name().equals(InstanceStateName.STOPPED)) {

                    String resourceName = ec2Desc.getResourceName(i.tags());

                    Float cost = getMonthlySavings(os, i.instanceTypeAsString(),
                            regionStr, null);

                    registerTemp.setResourceId(i.instanceId());
                    registerTemp.setResourceName(resourceName);
                    registerTemp.setServiceType(ServiceType.INSTANCE);
                    registerTemp.setRegion(regionStr);
                    registerTemp.setAccountId(a.getAccountId());
                    registerTemp.setAccountName(a.getName());
                    registerTemp.setOptType(OptimizationType.UNUSED);
                    registerTemp.setAction(RecommendedAction.TERMINATE);
                    registerTemp.setResourceType(i.instanceTypeAsString());
                    registerTemp.setRecommendation(null);
                    registerTemp.setEstimatedMonthlySavings(cost);
                    registerTemp.setInstanceOs(os);
                    registerTemp.setOptimizationReason("Unused Resource");

                    registerOptimizer(registerTemp);
                }

                String[] instanceFamily = i.instanceTypeAsString().split("\\.");

                String updatedFamily = upToDate.instanceFamilySwitch(instanceFamily[0]);

                if (updatedFamily != null) {

                    String recommendedInstanceType = updatedFamily + "." + instanceFamily[1];

                    DescribeInstanceTypesResponse descInstanceType = ec2.describeInstanceTypes(
                            DescribeInstanceTypesRequest.builder()
                                    .instanceTypesWithStrings(recommendedInstanceType)
                                    .build());

                    if (!descInstanceType.instanceTypes().isEmpty()) {

                        String resourceName = ec2Desc.getResourceName(i.tags());

                        Float cost = getMonthlySavings(os, i.instanceTypeAsString(),
                                regionStr,
                                recommendedInstanceType);

                        registerTemp.setResourceId(i.instanceId());
                        registerTemp.setResourceName(resourceName);
                        registerTemp.setServiceType(ServiceType.INSTANCE);
                        registerTemp.setRegion(regionStr);
                        registerTemp.setAccountId(a.getAccountId());
                        registerTemp.setAccountName(a.getName());
                        registerTemp.setOptType(OptimizationType.VERSION_UP);
                        registerTemp.setAction(RecommendedAction.MODIFY);
                        registerTemp.setResourceType(i.instanceTypeAsString());
                        registerTemp.setRecommendation(recommendedInstanceType);
                        registerTemp.setEstimatedMonthlySavings(cost);
                        registerTemp.setInstanceOs(os);
                        registerTemp.setOptimizationReason("Outdated Resource Type");

                        registerOptimizer(registerTemp);
                    }
                }
            }
        }
    }

    @Async("threadPoolTaskExecutor")
    public void volumeOptimizationTask(Account a, Ec2Client ec2, String regionStr)
            throws JsonParseException, JsonMappingException, IOException {

        List<Volume> volumes = ec2Desc.getVolumeDesc(ec2, null, null);

        if (volumes != null && !volumes.isEmpty()) {
            for (Volume v : volumes) {

                RegisterOptimizerDto registerTemp = new RegisterOptimizerDto();

                if (v.state().equals(VolumeState.AVAILABLE)) {

                    String resourceName = ec2Desc.getResourceName(v.tags());

                    Float cost = findOnDemandPrice.getStorageCost(regionStr,
                            v.volumeTypeAsString()) * v.size();

                    registerTemp.setResourceId(v.volumeId());
                    registerTemp.setResourceName(resourceName);
                    registerTemp.setServiceType(ServiceType.VOLUME);
                    registerTemp.setRegion(regionStr);
                    registerTemp.setAccountId(a.getAccountId());
                    registerTemp.setAccountName(a.getName());
                    registerTemp.setOptType(OptimizationType.UNUSED);
                    registerTemp.setAction(RecommendedAction.DELETE);
                    registerTemp.setResourceType(v.volumeTypeAsString());
                    registerTemp.setRecommendation(null);
                    registerTemp.setEstimatedMonthlySavings(cost);
                    registerTemp.setInstanceOs(null);
                    registerTemp.setOptimizationReason("Unused Resource");

                    registerOptimizer(registerTemp);

                } else if (v.state().equals(VolumeState.IN_USE)) {
                    if (v.volumeType().equals(VolumeType.GP2)) {
                        if (v.size() < 1000) {

                            String resourceName = ec2Desc.getResourceName(v.tags());

                            Float originalCost = findOnDemandPrice.getStorageCost(regionStr,
                                    v.volumeTypeAsString())
                                    * v.size();

                            Float changedCost = findOnDemandPrice.getStorageCost(regionStr, "gp3") *
                                    v.size();

                            registerTemp.setResourceId(v.volumeId());
                            registerTemp.setResourceName(resourceName);
                            registerTemp.setServiceType(ServiceType.VOLUME);
                            registerTemp.setRegion(regionStr);
                            registerTemp.setAccountId(a.getAccountId());
                            registerTemp.setAccountName(a.getName());
                            registerTemp.setOptType(OptimizationType.VERSION_UP);
                            registerTemp.setAction(RecommendedAction.MODIFY);
                            registerTemp.setResourceType(v.volumeTypeAsString());
                            registerTemp.setRecommendation("gp3");
                            registerTemp.setEstimatedMonthlySavings(originalCost - changedCost);
                            registerTemp.setInstanceOs(null);
                            registerTemp.setOptimizationReason("Outdated Resource Type");

                            registerOptimizer(registerTemp);
                        }
                    }
                }
            }
        }
    }

    public Float getMonthlySavings(
            String os,
            String instanceType,
            String region,
            String recommendation) {
        Float result = 0f;

        AwsInstanceType od = awsInstanceTypeRepo.findByRegionAndInstanceType(region,
                instanceType)
                .orElse(null);

        if (od != null) {

            Float monthlyCost = 0f;

            if (os.equals("Linux")) {
                monthlyCost = od.getOdLinuxPricing() * 24 * 30;
            } else {
                monthlyCost = od.getOdWindowsPricing() * 24 * 30;
            }

            if (recommendation != null) {

                Float changedMonthlyCost = 0f;

                AwsInstanceType odRecommendation = awsInstanceTypeRepo
                        .findByRegionAndInstanceType(region, recommendation)
                        .orElse(null);

                if (odRecommendation != null) {

                    if (os.equals("Linux")) {
                        changedMonthlyCost = odRecommendation.getOdLinuxPricing() * 24 * 30;
                    } else {
                        changedMonthlyCost = odRecommendation.getOdWindowsPricing() * 24 * 30;
                    }

                    result = monthlyCost - changedMonthlyCost;

                } else {
                    return null;
                }
            } else {
                result = monthlyCost;
            }
        }

        BigDecimal bd = BigDecimal.valueOf(result);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.floatValue();
    }

    public Optimizer registerOptimizer(RegisterOptimizerDto registerTemp) {
        Optimizer optimizer = optimizerRepo
                .findByAccountIdAndResourceIdAndOptimizationType(registerTemp.getAccountId(),
                        registerTemp.getResourceId(), registerTemp.getOptType())
                .orElseGet(() -> {
                    Optimizer vo = new Optimizer();
                    vo.setId(UUID.randomUUID().toString());
                    vo.setResourceId(registerTemp.getResourceId());
                    vo.setAccountId(registerTemp.getAccountId());
                    vo.setAccountName(registerTemp.getAccountName());
                    vo.setOptimizationType(registerTemp.getOptType());
                    return vo;
                });

        optimizer.setRegion(registerTemp.getRegion());
        optimizer.setResourceName(registerTemp.getResourceName());
        optimizer.setServiceType(registerTemp.getServiceType());
        optimizer.setOptimized(false);
        optimizer.setResourceType(registerTemp.getResourceType());
        optimizer.setInstanceOs(registerTemp.getInstanceOs());
        optimizer.setRecommendedAction(registerTemp.getAction());
        optimizer.setRecommendation(registerTemp.getRecommendation());
        optimizer.setOptimizationReason(registerTemp.getOptimizationReason());
        optimizer.setEstimatedMonthlySavings(registerTemp.getEstimatedMonthlySavings());

        return optimizerRepo.save(optimizer);
    }

}
