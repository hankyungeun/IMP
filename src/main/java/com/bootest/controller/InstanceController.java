package com.bootest.controller;

import java.util.ArrayList;
import java.util.List;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.dto.instance.InstanceDto;
import com.bootest.model.*;
import com.bootest.repository.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.Reservation;

@RestController
@RequiredArgsConstructor
@RequestMapping("/instance")
public class InstanceController {

    private final AccountRepo accountRepo;
    private final Ec2ClientManager ec2cm;
    private final ResourceUsageRepo resourceUsageRepo;

    @GetMapping
    public List<InstanceDto> findAll() {

        List<InstanceDto> results = new ArrayList<>();

        String nextToken = null;

        List<Account> accounts = accountRepo.findAll();

        for (Account account : accounts) {

            //String[] regionArr = account.getRegions().split(", ");

            Region region = Region.of(account.getRegions());

            Ec2Client ec2 = ec2cm.getEc2WithAccount(region, account);

            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .maxResults(100)
                    .nextToken(nextToken)
                    .build();

            do {

                DescribeInstancesResponse res = ec2.describeInstances(request);

                if (res.reservations().isEmpty()) {
                    // 어떤 행동
                    return null;
                }

                for (Reservation r : res.reservations()) {
                    for (Instance i : r.instances()) {
                        InstanceDto data = new InstanceDto();
                        data.setInstanceId(i.instanceId());
                        data.setOs(i.platformAsString() == null ? "Linux" : "Windows");
                        data.setInstanceType(i.instanceTypeAsString());
                        data.setVolumeId(i.blockDeviceMappings().get(0).ebs().volumeId());
                        results.add(data);
                    }
                }

                nextToken = res.nextToken();

            } while (nextToken != null);

        }

        return results;

    }
}
