package com.bootest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/instance")
public class InstanceController {

    private final AccountRepo accountRepo;
    private final Ec2ClientManager ec2cm;
    private final InstanceRecoRepo instanceRecoRepo;
    private final StorageAssociationRepo storageAssociationRepo;

    @GetMapping
    //디비에 저장된 인스턴스 목록을 가져온다
    //인스턴스 아이디로 필터링 가능
    public List<InstanceReco> findAll(
            HttpServletRequest request,
            @RequestParam(name = "instanceId", required = false) String instanceId) {
        SearchBuilder<InstanceReco> searchBuilder = SearchBuilder.builder();

        User user = (User) request.getSession().getAttribute("loginUser");

        List<Account> accounts = new ArrayList<>();

        if (user != null) {
            accounts.addAll(user.getAccounts());
        }

        if (!accounts.isEmpty()) {
            searchBuilder.with("accountId", SearchOperationType.EQUAL, accounts.stream().map(Account::getAccountId).collect(Collectors.toSet()));
        }

        if (instanceId != null) {
            searchBuilder.with("volumeId", SearchOperationType.EQUAL, instanceId);
        }

        List<InstanceReco> result = instanceRecoRepo.findAll(searchBuilder.build());

        return result;
    }

    //아마존으로부터 DescribeInstancesRequest api를 사용해 인스턴스 목록을 디비에 저장한다
    @GetMapping("/get")
    public void save() {
        String nextToken = null;

        List<Account> accounts = accountRepo.findAll();

        for (Account account : accounts) {

            // String[] regionArr = account.getRegions().split(", ");

            Region region = Region.of(account.getRegions());

            Ec2Client ec2 = ec2cm.getEc2WithAccount(region, account);

            do {
                DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                        .maxResults(100)
                        .nextToken(nextToken)
                        .build();

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
