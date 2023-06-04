package com.bootest.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bootest.dto.TagDto;
import com.bootest.dto.instance.create.BlockStorageOptionDto;
import com.bootest.dto.instance.create.InstanceMarketOptionsRequestDto;
import com.bootest.dto.instance.create.RunInstancesDto;
import com.bootest.dto.instance.describe.BlockDeviceSpecificationsDto;
import com.bootest.dto.instance.describe.EbsSpecificationsDto;
import com.bootest.dto.instance.describe.InstanceDetailsDto;
import com.bootest.dto.instance.describe.InstanceSpecificationsDto;
import com.bootest.dto.instance.describe.SecurityGroupDetailsDto;
import com.bootest.dto.instance.describe.SecurityRulesDto;
import com.bootest.dto.instance.describe.SecuritySpecificationsDto;
import com.bootest.model.ResultObject;
import com.bootest.util.VrmTags;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttributeValue;
import software.amazon.awssdk.services.ec2.model.BlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceAttributeRequest;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceAttributeResponse;
import software.amazon.awssdk.services.ec2.model.EbsBlockDevice;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.GroupIdentifier;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceAttributeName;
import software.amazon.awssdk.services.ec2.model.InstanceBlockDeviceMapping;
import software.amazon.awssdk.services.ec2.model.InstanceMarketOptionsRequest;
import software.amazon.awssdk.services.ec2.model.ModifyInstanceAttributeRequest;
import software.amazon.awssdk.services.ec2.model.RebootInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesMonitoringEnabled;
import software.amazon.awssdk.services.ec2.model.RunInstancesRequest;
import software.amazon.awssdk.services.ec2.model.RunInstancesResponse;
import software.amazon.awssdk.services.ec2.model.SecurityGroupRule;
import software.amazon.awssdk.services.ec2.model.SpotMarketOptions;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagSpecification;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;
import software.amazon.awssdk.services.ec2.model.Volume;

@Slf4j
@RequiredArgsConstructor
@Service
public class InstancesService {

    private final Ec2ResourceDescribeService descService;
    private final Ec2StateDescribe ec2State;

    public String terminateInstance(Ec2Client ec2, List<String> instanceIds) {

        log.info("Terminate Instance Request Start (instanceIds={})", instanceIds);

        try {
            ec2.terminateInstances(
                    TerminateInstancesRequest.builder()
                            .instanceIds(instanceIds)
                            .build());

            log.info("Terminate Instance Request Complete (instanceIds={})", instanceIds);
            return "terminated";
        } catch (Ec2Exception e) {
            log.error("Terminate Instance Request Failed (Reason: {})", e.getMessage(), e);
            return "Error=" + e.getMessage();
        }
    }

    public void modifyInstanceAttribute(Ec2Client ec2, String instanceId, String type) throws InterruptedException {
        boolean stopped = false;

        for (int i = 0; i < 100; i++) {
            String instanceState = ec2State.getInstanceState(instanceId, ec2);

            if ("stopped".equals(instanceState)) {
                stopped = true;
                break;
            } else if ("running".equals(instanceState) || "pending".equals(instanceState)) {
                ec2.stopInstances(
                        StopInstancesRequest.builder()
                                .instanceIds(instanceId)
                                .build());
                Thread.sleep(3000);
            } else if ("terminated".equals(instanceState) || "shutting-down".equals(instanceState)) {
                break;
            } else {
                Thread.sleep(2000);
            }
        }

        if (stopped == true) {
            AttributeValue attribute = AttributeValue.builder()
                    .value(type)
                    .build();

            ModifyInstanceAttributeRequest request = ModifyInstanceAttributeRequest.builder()
                    .instanceId(instanceId)
                    .instanceType(attribute)
                    .build();

            try {
                ec2.modifyInstanceAttribute(request);
            } catch (Exception e) {
                log.error("Modify Instance Type Request Failed (Message: {})", e.getMessage(), e);
            }
        }
    }
}
