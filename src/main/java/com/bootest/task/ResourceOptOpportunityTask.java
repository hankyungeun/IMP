// package com.gytni.vrm.task;

// import java.io.IOException;
// import java.time.Instant;
// import java.time.LocalDate;
// import java.time.ZoneId;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;
// import java.util.stream.Collectors;

// import org.springframework.scheduling.annotation.EnableScheduling;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// import com.fasterxml.jackson.core.JsonParseException;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.JsonMappingException;
// import com.gytni.vrm.aspect.Timer;
// import com.gytni.vrm.aws.AwsClientManager;
// import com.gytni.vrm.model.Account;
// import com.gytni.vrm.model.ResultObject;
// import com.gytni.vrm.model.VrmResourceUsage;
// import com.gytni.vrm.repo.AccountRepo;
// import com.gytni.vrm.repo.VrmResourceUsageRepo;
// import com.gytni.vrm.service.RightSizeOpportunityService;
// import com.gytni.vrm.service.UnusedAndVersionOpportunityService;
// import com.gytni.vrm.type.UsageDataType;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import software.amazon.awssdk.regions.Region;
// import software.amazon.awssdk.services.ec2.Ec2Client;
// import
// software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;

// @Slf4j
// @Component
// @RequiredArgsConstructor
// @EnableScheduling
// public class ResourceOptOpportunityTask {

// private final AccountRepo accountRepo;
// private final AwsClientManager awscm;
// private final VrmResourceUsageRepo vrmResourceUsageRepo;
// private final UnusedAndVersionOpportunityService unusedNVersionOpService;
// private final RightSizeOpportunityService rightSizeOpportunityService;
// private final LoadConfig loadConfig;

// @Timer
// @Scheduled(cron = "0 0 * * * *")
// public ResultObject doOptimizationOpportunityTask() throws
// JsonParseException, JsonMappingException, IOException {
// ResultObject result = new ResultObject();

// if (loadConfig.isScheduledTaskEnabled("optimizeOpportunity")) {
// optimizationOpportunityTask();
// result.setMessage("Do Optimization Opportunity Check Task Complete");
// result.setResult(true);
// } else {
// log.debug("doOptimizationOpportunityCache is disabled");
// result.setMessage("Do Optimization Opportunity Check Task Disabled");
// result.setResult(true);
// }
// return result;
// }

// @Timer
// @Scheduled(cron = "0 0 * * * *")
// public ResultObject doRightSizeOpportunityTask() throws JsonMappingException,
// JsonProcessingException {
// ResultObject result = new ResultObject();

// if (loadConfig.isScheduledTaskEnabled("optimizeOpportunity")) {
// rightSizeOpportunityTask();
// result.setMessage("Do Optimization Opportunity Check Task Complete");
// result.setResult(true);
// } else {
// log.debug("doOptimizationOpportunityCache is disabled");
// result.setMessage("Do Optimization Opportunity Check Task Disabled");
// result.setResult(true);
// }
// return result;
// }

// public void optimizationOpportunityTask() throws JsonParseException,
// JsonMappingException, IOException {
// List<Account> accounts = accountRepo.findAll();

// for (Account a : accounts) {
// String[] regionStrArr = a.getRegions().split(", ");

// for (String regionStr : regionStrArr) {

// Region region = Region.of(regionStr);

// Ec2Client ec2 = awscm.getEc2(region, a);
// ElasticLoadBalancingV2Client elb = awscm.getElb(region, a);

// // Instance Optimizer
// unusedNVersionOpService.instanceOptimizationTask(a, ec2, regionStr);

// // Volume optimizer
// unusedNVersionOpService.volumeOptimizationTask(a, ec2, regionStr);

// // EIP optimizer
// unusedNVersionOpService.eipOptimizationTask(a, ec2, regionStr);

// // Snapshot optimizer
// unusedNVersionOpService.snapshotOptimizationTask(a, ec2, regionStr);

// // Load Balancer optimizer
// unusedNVersionOpService.elbOptimizationTask(a, elb, regionStr);
// }
// }
// }

// public void rightSizeOpportunityTask() throws JsonMappingException,
// JsonProcessingException {
// LocalDate now = LocalDate.now();

// Integer year = now.getYear();
// Integer month = now.getMonthValue();

// List<VrmResourceUsage> usages =
// vrmResourceUsageRepo.findAllByResourceStateAndAnnuallyAndMonthlyAndDataType(
// "running",
// year.shortValue(), month.shortValue(), UsageDataType.CPU);

// LocalDate minusPeriod =
// now.minusDays(loadConfig.getOptimizationConfig().getDays());

// Instant to = now.atStartOfDay(ZoneId.systemDefault()).toInstant();
// Instant from = minusPeriod.atStartOfDay(ZoneId.systemDefault()).toInstant();

// List<LocalDate> dates =
// minusPeriod.datesUntil(now).collect(Collectors.toList());

// Set<String> dateSet = new HashSet<>();
// for (LocalDate ld : dates) {
// String[] ldArray = ld.toString().split("-");
// dateSet.add(ldArray[0] + "-" + ldArray[1]);
// }

// for (VrmResourceUsage usage : usages) {
// rightSizeOpportunityService.rightSizeOpportunityTask(dateSet, usage, to,
// from);
// }
// }

// }
