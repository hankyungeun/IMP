package com.bootest.task;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.model.Account;
import com.bootest.model.ResourceUsage;
import com.bootest.repository.AccountRepo;
import com.bootest.repository.ResourceUsageRepo;
import com.bootest.service.RightSizeOpportunityService;
import com.bootest.service.UnusedAndVersionOpportunityService;
import com.bootest.type.UsageDataType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableScheduling
public class ResourceOptOpportunityTask {

    private final AccountRepo accountRepo;
    private final Ec2ClientManager ec2cm;
    private final ResourceUsageRepo resourceUsageRepo;
    private final UnusedAndVersionOpportunityService unusedNVersionOpService;
    private final RightSizeOpportunityService rightSizeOpportunityService;

    @Value("${task.optimizer.enabled}")
    private boolean isScheduledTaskEnabled;

    @Scheduled(cron = "0 0 * * * *")
    public void doOptimizationOpportunityTask() throws JsonParseException, JsonMappingException, IOException {

        if (isScheduledTaskEnabled) {
            optimizationOpportunityTask();
        } else {
            log.debug("doOptimizationOpportunityCache is disabled");
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void doRightSizeOpportunityTask() throws JsonMappingException,
            JsonProcessingException {
        if (isScheduledTaskEnabled) {
            rightSizeOpportunityTask();
        } else {
            log.debug("doOptimizationOpportunityCache is disabled");
        }
    }

    public void optimizationOpportunityTask() throws JsonParseException,
            JsonMappingException, IOException {
        List<Account> accounts = accountRepo.findAll();

        for (Account a : accounts) {
            String[] regionStrArr = a.getRegions().split(", ");

            for (String regionStr : regionStrArr) {

                Region region = Region.of(regionStr);

                Ec2Client ec2 = ec2cm.getEc2WithAccount(region, a);

                // Instance Optimizer
                unusedNVersionOpService.instanceOptimizationTask(a, ec2, regionStr);

                // Volume optimizer
                unusedNVersionOpService.volumeOptimizationTask(a, ec2, regionStr);

            }
        }
    }

    public void rightSizeOpportunityTask() throws JsonMappingException,
            JsonProcessingException {
        LocalDate now = LocalDate.now();

        Integer year = now.getYear();
        Integer month = now.getMonthValue();

        List<ResourceUsage> usages = resourceUsageRepo.findAllByResourceStateAndAnnuallyAndMonthlyAndDataType(
                "running",
                year.shortValue(), month.shortValue(), UsageDataType.CPU);

        LocalDate minusPeriod = now.minusDays(30);

        Instant to = now.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant from = minusPeriod.atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<LocalDate> dates = minusPeriod.datesUntil(now).collect(Collectors.toList());

        Set<String> dateSet = new HashSet<>();
        for (LocalDate ld : dates) {
            String[] ldArray = ld.toString().split("-");
            dateSet.add(ldArray[0] + "-" + ldArray[1]);
        }

        for (ResourceUsage usage : usages) {
            rightSizeOpportunityService.rightSizeOpportunityTask(dateSet, usage, to,
                    from);
        }
    }

}
