package com.bootest.task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.pricing.InstanceTypeSpecDto;
import com.bootest.model.Account;
import com.bootest.model.AwsInstanceType;
import com.bootest.repository.AccountRepo;
import com.bootest.repository.AwsInstanceTypeRepo;
import com.bootest.service.FindOnDemandPrice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceTypesResponse;
import software.amazon.awssdk.services.ec2.model.InstanceTypeInfo;

@Slf4j
@RequiredArgsConstructor
@EnableScheduling
@Component
public class InstanceTypeTask {
    private final FindOnDemandPrice findOd;
    private final AwsInstanceTypeRepo instanceTypesRepo;
    private final AccountRepo accountRepo;
    private final Ec2ClientManager ec2cm;

    @Value("${task.master-account}")
    private String masterAccount;

    @Value("${task.enable-od}")
    private boolean enabledJob;

    @Scheduled(cron = "0 0 0 * * MON")
    public void doReloadInstanceTypes() throws Exception {
        if (enabledJob) {
            reloadInstanceTypeInfo(List.of(Region.AP_NORTHEAST_2));
        } else {
            log.debug("reloadInstanceTypeInfo is disabled");
        }
    }

    public void reloadInstanceTypeInfo(List<Region> regions) throws Exception {

        List<AwsInstanceType> results = new ArrayList<>();

        Account account = accountRepo.findByAccountId(masterAccount)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account ID: " + masterAccount));

        if (regions == null || regions.isEmpty()) {
            regions = Region.regions();
        }

        for (Region region : regions) {

            Ec2Client ec2 = ec2cm.getEc2WithAccount(region, account);
            List<InstanceTypeInfo> instanceTypesInfo = null;

            try {
                instanceTypesInfo = getInstanceTypesInfo(ec2);
            } catch (Exception e) {
                log.warn(
                        "Unable to get instance type info from the region with the current account. region: {}, account: {}",
                        region.toString(), account.getAccountId());
            }

            if (instanceTypesInfo != null) {

                List<InstanceTypeSpecDto> linuxOdPrices = findOd.getAllOdPrice(region.toString(), account, "Linux");
                List<InstanceTypeSpecDto> windowsOdPrices = findOd.getAllOdPrice(region.toString(), account, "Windows");

                for (InstanceTypeInfo iti : instanceTypesInfo) {
                    AwsInstanceType vrmInstanceType = instanceTypesRepo
                            .findByRegionAndInstanceType(region.toString(), iti.instanceTypeAsString())
                            .orElseGet(() -> {

                                Float memMiB = iti.memoryInfo().sizeInMiB().floatValue();

                                AwsInstanceType vrmIt = new AwsInstanceType();
                                vrmIt.setId(UUID.randomUUID().toString());
                                vrmIt.setRegion(region.toString());
                                vrmIt.setInstanceType(iti.instanceTypeAsString());
                                vrmIt.setVcpus(iti.vCpuInfo().defaultVCpus().shortValue());
                                vrmIt.setMemoryGib(memMiB / 1024);
                                vrmIt.setNetworkPerformance(iti.networkInfo().networkPerformance());

                                return vrmIt;
                            });

                    for (InstanceTypeSpecDto its : linuxOdPrices) {
                        if (iti.instanceTypeAsString().equals(its.getInstanceType())) {
                            vrmInstanceType.setOdLinuxPricing(its.getCost());
                        }
                    }
                    for (InstanceTypeSpecDto its : windowsOdPrices) {
                        if (iti.instanceTypeAsString().equals(its.getInstanceType())) {
                            vrmInstanceType.setOdWindowsPricing(its.getCost());
                        }
                    }
                    results.add(vrmInstanceType);
                }
            }
        }
        instanceTypesRepo.saveAll(results);
    }

    /**
     * Get all instance types offered from the selected region.
     * Regions are returned as list by groups of 100.
     * 
     * @param ec2
     * @return Instance Type Info
     */
    public List<InstanceTypeInfo> getInstanceTypesInfo(Ec2Client ec2) {

        List<InstanceTypeInfo> instanceTypeInfoList = new ArrayList<>();

        String nextToken = null;

        do {
            DescribeInstanceTypesResponse ditres = ec2.describeInstanceTypes(
                    DescribeInstanceTypesRequest.builder()
                            .maxResults(100)
                            .nextToken(nextToken)
                            .build());

            for (InstanceTypeInfo iti : ditres.instanceTypes()) {
                instanceTypeInfoList.add(iti);
            }

            nextToken = ditres.nextToken();

        } while (nextToken != null);
        return instanceTypeInfoList;
    }

}
