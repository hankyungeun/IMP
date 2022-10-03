// package com.gytni.vrm.service;

// import java.io.IOException;
// import java.math.BigDecimal;
// import java.math.RoundingMode;
// import java.time.Instant;
// import java.time.temporal.ChronoUnit;
// import java.util.List;
// import java.util.UUID;

// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;

// import com.fasterxml.jackson.core.JsonParseException;
// import com.fasterxml.jackson.databind.JsonMappingException;
// import com.gytni.vrm.dto.optimizer.RegisterOptimizerDto;
// import com.gytni.vrm.model.Account;
// import com.gytni.vrm.model.VrmInstanceType;
// import com.gytni.vrm.model.VrmOptimizer;
// import com.gytni.vrm.model.VrmSpotInstanceType;
// import com.gytni.vrm.repo.VrmInstanceTypeRepo;
// import com.gytni.vrm.repo.VrmOptimizerRepo;
// import com.gytni.vrm.repo.VrmSpotInstanceTypeRepo;
// import com.gytni.vrm.service.resourceDesc.Ec2ResourceDescribeService;
// import com.gytni.vrm.service.resourceDesc.ElbResourceDescribeService;
// import com.gytni.vrm.task.LoadConfig;
// import com.gytni.vrm.type.CSP;
// import com.gytni.vrm.type.OptimizationType;
// import com.gytni.vrm.type.RecommendedAction;
// import com.gytni.vrm.type.ServiceType;

// import lombok.RequiredArgsConstructor;
// import software.amazon.awssdk.services.ec2.Ec2Client;
// import software.amazon.awssdk.services.ec2.model.Address;
// import
// software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
// import
// software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesResponse;
// import software.amazon.awssdk.services.ec2.model.DescribeVolumesRequest;
// import software.amazon.awssdk.services.ec2.model.DescribeVolumesResponse;
// import software.amazon.awssdk.services.ec2.model.Filter;
// import software.amazon.awssdk.services.ec2.model.Instance;
// import software.amazon.awssdk.services.ec2.model.InstanceLifecycleType;
// import software.amazon.awssdk.services.ec2.model.InstanceStateName;
// import software.amazon.awssdk.services.ec2.model.Snapshot;
// import software.amazon.awssdk.services.ec2.model.SnapshotTierStatus;
// import software.amazon.awssdk.services.ec2.model.StorageTier;
// import software.amazon.awssdk.services.ec2.model.Volume;
// import software.amazon.awssdk.services.ec2.model.VolumeState;
// import software.amazon.awssdk.services.ec2.model.VolumeType;
// import
// software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
// import
// software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
// import
// software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
// import
// software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;

// @Service
// @RequiredArgsConstructor
// public class UnusedAndVersionOpportunityService {

// private final Ec2ResourceDescribeService ec2Desc;
// private final ElbResourceDescribeService elbDesc;
// private final InstanceFamilyUpToDate upToDate;
// private final VrmOptimizerRepo vrmOptimizerRepo;
// private final AwsInstanceTypeRepo vrmInstanceTypeRepo;
// private final AwsSpotInstanceTypeRepo vrmSpotInstanceTypeRepo;
// private final FindOnDemandPrice findOnDemandPrice;
// private final LoadConfig loadConfig;

// @Async("threadPoolTaskExecutor")
// public void instanceOptimizationTask(Account a, Ec2Client ec2, String
// regionStr) {

// List<Instance> instances = ec2Desc.getInstanceDesc(ec2, null);

// if (instances != null && !instances.isEmpty()) {
// for (Instance i : instances) {

// RegisterOptimizerDto registerTemp = new RegisterOptimizerDto();

// String os = i.platformAsString() == null ? "Linux" : "Windows";

// String lifeCycle = i.instanceLifecycle() == InstanceLifecycleType.SPOT ?
// "spot" : "on-demand";

// if (i.state().name().equals(InstanceStateName.STOPPED)) {

// String resourceName = ec2Desc.getResourceName(i.tags());

// Float cost = getMonthlySavings(os, lifeCycle, i.instanceTypeAsString(),
// regionStr, null);

// registerTemp.setResourceId(i.instanceId());
// registerTemp.setResourceName(resourceName);
// registerTemp.setServiceType(ServiceType.INSTANCE);
// registerTemp.setRegion(regionStr);
// registerTemp.setAccountId(a.getAccountId());
// registerTemp.setAccountName(a.getAccountName());
// registerTemp.setOptType(OptimizationType.UNUSED);
// registerTemp.setAction(RecommendedAction.TERMINATE);
// registerTemp.setResourceType(i.instanceTypeAsString());
// registerTemp.setRecommendation(null);
// registerTemp.setEstimatedMonthlySavings(cost);
// registerTemp.setInstanceOs(os);
// registerTemp.setOptimizationReason("Unused Resource");

// registerOptimizer(registerTemp);
// }

// String[] instanceFamily = i.instanceTypeAsString().split("\\.");

// String updatedFamily = upToDate.instanceFamilySwitch(instanceFamily[0]);

// if (updatedFamily != null) {

// String recommendedInstanceType = updatedFamily + "." + instanceFamily[1];

// DescribeInstanceTypesResponse descInstanceType = ec2.describeInstanceTypes(
// DescribeInstanceTypesRequest.builder()
// .instanceTypesWithStrings(recommendedInstanceType)
// .build());

// if (!descInstanceType.instanceTypes().isEmpty()) {

// String resourceName = ec2Desc.getResourceName(i.tags());

// Float cost = getMonthlySavings(os, lifeCycle, i.instanceTypeAsString(),
// regionStr,
// recommendedInstanceType);

// registerTemp.setResourceId(i.instanceId());
// registerTemp.setResourceName(resourceName);
// registerTemp.setServiceType(ServiceType.INSTANCE);
// registerTemp.setRegion(regionStr);
// registerTemp.setAccountId(a.getAccountId());
// registerTemp.setAccountName(a.getAccountName());
// registerTemp.setOptType(OptimizationType.VERSION_UP);
// registerTemp.setAction(RecommendedAction.MODIFY);
// registerTemp.setResourceType(i.instanceTypeAsString());
// registerTemp.setRecommendation(recommendedInstanceType);
// registerTemp.setEstimatedMonthlySavings(cost);
// registerTemp.setInstanceOs(os);
// registerTemp.setOptimizationReason("Outdated Resource Type");

// registerOptimizer(registerTemp);
// }
// }
// }
// }
// }

// @Async("threadPoolTaskExecutor")
// public void volumeOptimizationTask(Account a, Ec2Client ec2, String
// regionStr)
// throws JsonParseException, JsonMappingException, IOException {

// List<Volume> volumes = ec2Desc.getVolumeDesc(ec2, null, null);

// if (volumes != null && !volumes.isEmpty()) {
// for (Volume v : volumes) {

// RegisterOptimizerDto registerTemp = new RegisterOptimizerDto();

// if (v.state().equals(VolumeState.AVAILABLE)) {

// String resourceName = ec2Desc.getResourceName(v.tags());

// Float cost = findOnDemandPrice.getStorageCost(regionStr,
// v.volumeTypeAsString()) * v.size();

// registerTemp.setResourceId(v.volumeId());
// registerTemp.setResourceName(resourceName);
// registerTemp.setServiceType(ServiceType.VOLUME);
// registerTemp.setRegion(regionStr);
// registerTemp.setAccountId(a.getAccountId());
// registerTemp.setAccountName(a.getAccountName());
// registerTemp.setOptType(OptimizationType.UNUSED);
// registerTemp.setAction(RecommendedAction.DELETE);
// registerTemp.setResourceType(v.volumeTypeAsString());
// registerTemp.setRecommendation(null);
// registerTemp.setEstimatedMonthlySavings(cost);
// registerTemp.setInstanceOs(null);
// registerTemp.setOptimizationReason("Unused Resource");

// registerOptimizer(registerTemp);

// } else if (v.state().equals(VolumeState.IN_USE)) {
// if (v.volumeType().equals(VolumeType.GP2)) {
// if (v.size() < 1000) {

// String resourceName = ec2Desc.getResourceName(v.tags());

// Float originalCost = findOnDemandPrice.getStorageCost(regionStr,
// v.volumeTypeAsString())
// * v.size();

// Float changedCost = findOnDemandPrice.getStorageCost(regionStr, "gp3") *
// v.size();

// registerTemp.setResourceId(v.volumeId());
// registerTemp.setResourceName(resourceName);
// registerTemp.setServiceType(ServiceType.VOLUME);
// registerTemp.setRegion(regionStr);
// registerTemp.setAccountId(a.getAccountId());
// registerTemp.setAccountName(a.getAccountName());
// registerTemp.setOptType(OptimizationType.VERSION_UP);
// registerTemp.setAction(RecommendedAction.MODIFY);
// registerTemp.setResourceType(v.volumeTypeAsString());
// registerTemp.setRecommendation("gp3");
// registerTemp.setEstimatedMonthlySavings(originalCost - changedCost);
// registerTemp.setInstanceOs(null);
// registerTemp.setOptimizationReason("Outdated Resource Type");

// registerOptimizer(registerTemp);
// }
// }
// }
// }
// }
// }

// @Async("threadPoolTaskExecutor")
// public void eipOptimizationTask(Account a, Ec2Client ec2, String regionStr)
// throws JsonParseException, JsonMappingException, IOException {

// List<Address> addresses = ec2Desc.getAddressDesc(ec2, null, null);

// if (addresses != null && !addresses.isEmpty()) {

// RegisterOptimizerDto registerTemp = new RegisterOptimizerDto();

// for (Address address : addresses) {
// if (address.associationId() == null) {

// String resourceName = ec2Desc.getResourceName(address.tags());

// registerTemp.setResourceId(address.allocationId());
// registerTemp.setResourceName(resourceName);
// registerTemp.setServiceType(ServiceType.EIP);
// registerTemp.setRegion(regionStr);
// registerTemp.setAccountId(a.getAccountId());
// registerTemp.setAccountName(a.getAccountName());
// registerTemp.setOptType(OptimizationType.UNUSED);
// registerTemp.setAction(RecommendedAction.DELETE);
// registerTemp.setResourceType("standard");
// registerTemp.setRecommendation(null);
// registerTemp.setEstimatedMonthlySavings(loadConfig.getOptimizationConfig().getEipCost());
// registerTemp.setInstanceOs(null);
// registerTemp.setOptimizationReason("EIP Not Attached");

// registerOptimizer(registerTemp);
// }
// }
// }
// }

// @Async("threadPoolTaskExecutor")
// public void snapshotOptimizationTask(Account a, Ec2Client ec2, String
// regionStr)
// throws JsonParseException, JsonMappingException, IOException {

// List<Snapshot> snapshots = ec2Desc.getSnapshotDesc(ec2, a.getAccountId(),
// null);

// if (snapshots != null && !snapshots.isEmpty()) {
// for (Snapshot s : snapshots) {

// RegisterOptimizerDto registerTemp = new RegisterOptimizerDto();

// if (s.storageTier().equals(StorageTier.ARCHIVE)) {

// List<SnapshotTierStatus> tierStatus = ec2Desc.getSnapshotTierStatus(ec2,
// s.snapshotId(), null);

// if (tierStatus != null && !tierStatus.isEmpty()) {
// for (SnapshotTierStatus sts : tierStatus) {
// if (Instant.now().isAfter(sts.archivalCompleteTime().plus(30,
// ChronoUnit.DAYS))) {

// String resourceName = ec2Desc.getResourceName(s.tags());

// Float cost = findOnDemandPrice.getSnapshotCost(regionStr, "archive");

// registerTemp.setResourceId(s.snapshotId());
// registerTemp.setResourceName(resourceName);
// registerTemp.setServiceType(ServiceType.SNAPSHOT);
// registerTemp.setRegion(regionStr);
// registerTemp.setAccountId(a.getAccountId());
// registerTemp.setAccountName(a.getAccountName());
// registerTemp.setOptType(OptimizationType.UNUSED);
// registerTemp.setAction(RecommendedAction.DELETE);
// registerTemp.setResourceType("archive");
// registerTemp.setRecommendation(null);
// registerTemp.setEstimatedMonthlySavings(cost);
// registerTemp.setInstanceOs(null);
// registerTemp.setOptimizationReason("Archived and Not Used for More Than 30
// Days");

// registerOptimizer(registerTemp);
// }
// }
// }
// } else {
// if (!validateSnapshotUsage(s, ec2)) {
// if (Instant.now().isAfter(s.startTime().plus(30, ChronoUnit.DAYS))) {

// String resourceName = ec2Desc.getResourceName(s.tags());

// Float cost = findOnDemandPrice.getSnapshotCost(regionStr, "standard");

// registerTemp.setResourceId(s.snapshotId());
// registerTemp.setResourceName(resourceName);
// registerTemp.setServiceType(ServiceType.SNAPSHOT);
// registerTemp.setRegion(regionStr);
// registerTemp.setAccountId(a.getAccountId());
// registerTemp.setAccountName(a.getAccountName());
// registerTemp.setOptType(OptimizationType.UNUSED);
// registerTemp.setAction(RecommendedAction.ARCHIVE);
// registerTemp.setResourceType("standard");
// registerTemp.setRecommendation(null);
// registerTemp.setEstimatedMonthlySavings(cost);
// registerTemp.setInstanceOs(null);
// registerTemp.setOptimizationReason("Unused Resource");

// registerOptimizer(registerTemp);
// }
// }
// }
// }
// }
// }

// @Async("threadPoolTaskExecutor")
// public void elbOptimizationTask(Account a, ElasticLoadBalancingV2Client elb,
// String regionStr)
// throws JsonParseException, JsonMappingException, IOException {

// List<LoadBalancer> loadBalancers = elbDesc.getLoadBalancerDesc(elb, null,
// null);

// if (loadBalancers != null && !loadBalancers.isEmpty()) {
// Float cost = findOnDemandPrice.getStandardElbCost(regionStr) * 24 * 30;
// for (LoadBalancer lb : loadBalancers) {

// RegisterOptimizerDto registerTemp = new RegisterOptimizerDto();

// List<TargetGroup> targetGroups = elbDesc.getTargetGroupDesc(elb,
// lb.loadBalancerName());

// if (targetGroups.isEmpty() || targetGroups == null) {

// registerTemp.setResourceId(lb.loadBalancerArn());
// registerTemp.setResourceName(lb.loadBalancerName());
// registerTemp.setServiceType(ServiceType.ELB);
// registerTemp.setRegion(regionStr);
// registerTemp.setAccountId(a.getAccountId());
// registerTemp.setAccountName(a.getAccountName());
// registerTemp.setOptType(OptimizationType.UNUSED);
// registerTemp.setAction(RecommendedAction.DELETE);
// registerTemp.setResourceType(lb.typeAsString());
// registerTemp.setRecommendation(null);
// registerTemp.setEstimatedMonthlySavings(cost);
// registerTemp.setInstanceOs(null);
// registerTemp.setOptimizationReason("ELB Target Not Attached");

// registerOptimizer(registerTemp);

// } else {
// for (TargetGroup tg : targetGroups) {

// List<TargetHealthDescription> healths = elbDesc.getTargetHealthStateDesc(elb,
// tg.targetGroupArn());

// if (healths.isEmpty() || healths == null) {

// registerTemp.setResourceId(lb.loadBalancerArn());
// registerTemp.setResourceName(lb.loadBalancerName());
// registerTemp.setServiceType(ServiceType.ELB);
// registerTemp.setRegion(regionStr);
// registerTemp.setAccountId(a.getAccountId());
// registerTemp.setAccountName(a.getAccountName());
// registerTemp.setOptType(OptimizationType.UNUSED);
// registerTemp.setAction(RecommendedAction.DELETE);
// registerTemp.setResourceType(lb.typeAsString());
// registerTemp.setRecommendation(null);
// registerTemp.setEstimatedMonthlySavings(cost);
// registerTemp.setInstanceOs(null);
// registerTemp.setOptimizationReason("No Instance Attached to Any Target(s)");

// registerOptimizer(registerTemp);
// }
// }
// }
// }
// }
// }

// public Float getMonthlySavings(
// String os,
// String lifeCycle,
// String instanceType,
// String region,
// String recommendation) {
// Float result = 0f;

// if (lifeCycle.equals("spot")) {

// AwsSpotInstanceType sit =
// vrmSpotInstanceTypeRepo.findByRegionAndOsAndInstanceType(region, os,
// instanceType)
// .orElse(null);

// if (sit != null) {

// Float monthlyCost = sit.getSpotPricing() * 24 * 30;

// if (recommendation != null) {

// AwsSpotInstanceType sitRecommendation = vrmSpotInstanceTypeRepo
// .findByRegionAndOsAndInstanceType(region, os, recommendation)
// .orElse(null);

// if (sitRecommendation != null) {

// Float changedMonthlyCost = sitRecommendation.getSpotPricing() * 24 * 30;

// result = monthlyCost - changedMonthlyCost;

// } else {
// return null;
// }
// } else {
// result = monthlyCost;
// }
// }
// } else {

// AwsInstanceType od = vrmInstanceTypeRepo.findByRegionAndInstanceType(region,
// instanceType)
// .orElse(null);

// if (od != null) {

// Float monthlyCost = 0f;

// if (os.equals("Linux")) {
// monthlyCost = od.getOdLinuxPricing() * 24 * 30;
// } else {
// monthlyCost = od.getOdWindowsPricing() * 24 * 30;
// }

// if (recommendation != null) {

// Float changedMonthlyCost = 0f;

// AwsInstanceType odRecommendation = vrmInstanceTypeRepo
// .findByRegionAndInstanceType(region, recommendation)
// .orElse(null);

// if (odRecommendation != null) {

// if (os.equals("Linux")) {
// changedMonthlyCost = odRecommendation.getOdLinuxPricing() * 24 * 30;
// } else {
// changedMonthlyCost = odRecommendation.getOdWindowsPricing() * 24 * 30;
// }

// result = monthlyCost - changedMonthlyCost;

// } else {
// return null;
// }
// } else {
// result = monthlyCost;
// }
// }
// }

// BigDecimal bd = BigDecimal.valueOf(result);
// bd = bd.setScale(2, RoundingMode.HALF_UP);

// return bd.floatValue();
// }

// public Boolean validateSnapshotUsage(Snapshot s, Ec2Client ec2) {

// List<Volume> snapshotVolumes = ec2Desc.getVolumeDesc(ec2, s.volumeId(),
// null);

// if (snapshotVolumes != null && !snapshotVolumes.isEmpty()) {
// return true;
// }

// DescribeVolumesResponse response = ec2.describeVolumes(
// DescribeVolumesRequest.builder()
// .filters(
// Filter.builder()
// .name("snapshot-id")
// .values(s.snapshotId())
// .build())
// .build());

// if (!response.volumes().isEmpty()) {
// return true;
// }
// return false;
// }

// public VrmOptimizer registerOptimizer(RegisterOptimizerDto registerTemp) {
// VrmOptimizer optimizer = vrmOptimizerRepo
// .findByAccountIdAndResourceIdAndOptimizationType(registerTemp.getAccountId(),
// registerTemp.getResourceId(), registerTemp.getOptType())
// .orElseGet(() -> {
// VrmOptimizer vo = new VrmOptimizer();
// vo.setId(UUID.randomUUID().toString());
// vo.setResourceId(registerTemp.getResourceId());
// vo.setCsp(CSP.AWS);
// vo.setAccountId(registerTemp.getAccountId());
// vo.setAccountName(registerTemp.getAccountName());
// vo.setOptimizationType(registerTemp.getOptType());
// return vo;
// });

// optimizer.setRegion(registerTemp.getRegion());
// optimizer.setResourceName(registerTemp.getResourceName());
// optimizer.setServiceType(registerTemp.getServiceType());
// optimizer.setOptimized(false);
// optimizer.setResourceType(registerTemp.getResourceType());
// optimizer.setInstanceOs(registerTemp.getInstanceOs());
// optimizer.setRecommendedAction(registerTemp.getAction());
// optimizer.setRecommendation(registerTemp.getRecommendation());
// optimizer.setOptimizationReason(registerTemp.getOptimizationReason());
// optimizer.setEstimatedMonthlySavings(registerTemp.getEstimatedMonthlySavings());

// return vrmOptimizerRepo.save(optimizer);
// }

// }
