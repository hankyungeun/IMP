package com.bootest.controller;

import java.util.List;
import java.util.UUID;

import com.bootest.aws.Ec2ClientManager;
import com.bootest.model.*;
import com.bootest.repository.*;

import com.bootest.searcher.SearchBuilder;
import com.bootest.searcher.SearchOperationType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstancesResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceBlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.Reservation;
import software.amazon.awssdk.services.ec2.model.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/instance")
public class InstanceController {

    private final AccountRepo accountRepo;
    private final Ec2ClientManager ec2cm;
    private final InstanceRecoRepo instanceRecoRepo;
    private final StorageAssociationRepo storageAssociationRepo;

    @GetMapping
    public List<InstanceReco> findAll(
            @RequestParam(name = "instanceId", required = false) String instanceId) {
        SearchBuilder<InstanceReco> searchBuilder = SearchBuilder.builder();

        if (instanceId != null) {
            searchBuilder.with("volumeId", SearchOperationType.EQUAL, instanceId);
        }

        List<InstanceReco> result = instanceRecoRepo.findAll(searchBuilder.build());

        return result;
    }

    @GetMapping("/get")
    public void save() {
        String nextToken = null;

        List<Account> accounts = accountRepo.findAll();

        for (Account account : accounts) {

            // String[] regionArr = account.getRegions().split(", ");

            Region region = Region.of(account.getRegions());

            Ec2Client ec2 = ec2cm.getEc2WithAccount(region, account);

            DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                    .maxResults(100)
                    .nextToken(nextToken)
                    .build();

            do {

                DescribeInstancesResponse res = ec2.describeInstances(request);

                if (res.reservations().isEmpty()) {
                    continue;
                }

                for (Reservation r : res.reservations()) {
                    for (Instance i : r.instances()) {

                        InstanceReco instanceInfo = instanceRecoRepo.findByInstanceId(i.instanceId())
                                .orElseGet(() -> {
                                    InstanceReco ir = new InstanceReco();
                                    ir.setId(UUID.randomUUID().toString());
                                    ir.setInstanceId(i.instanceId());
                                    return ir;
                                });

                        String tagName = null;
                        for (Tag t : i.tags()) {
                            if (t.key().equals("Name")) {
                                tagName = t.value();
                            }
                        }

                        instanceInfo.setInstanceName(tagName);
                        instanceInfo.setInstanceType(i.instanceTypeAsString());
                        instanceInfo.setLaunchTime(i.launchTime().toString());
                        instanceInfo.setAvailabilityZone(i.placement().availabilityZone());
                        instanceInfo.setInstanceState(i.state().nameAsString());
                        instanceInfo.setInstanceLifeCycle(i.instanceLifecycleAsString());
                        instanceInfo.setOs(i.platformAsString() == null ? "Linux" : "Windows");

                        instanceRecoRepo.save(instanceInfo);

                        if (!i.blockDeviceMappings().isEmpty()) {
                            for (InstanceBlockDeviceMapping ibdm : i.blockDeviceMappings()) {
                                mapVolumeAndInstance(i, ibdm);
                            }
                        }

                    }
                }

                nextToken = res.nextToken();

            } while (nextToken != null);
        }
    }

    public void mapVolumeAndInstance(Instance i, InstanceBlockDeviceMapping bdMap) {

        StorageAssociation storageAss = storageAssociationRepo
                .findByInstanceIdAndVolumeId(i.instanceId(), bdMap.ebs().volumeId())
                .orElseGet(() -> {
                    StorageAssociation sa = new StorageAssociation();
                    sa.setId(UUID.randomUUID().toString());
                    sa.setInstanceId(i.instanceId());
                    sa.setVolumeId(bdMap.ebs().volumeId());
                    return sa;
                });

        storageAssociationRepo.save(storageAss);
    }

}
