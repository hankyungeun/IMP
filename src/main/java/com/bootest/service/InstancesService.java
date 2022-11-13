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

    /**
     * Request to create 1 or more on-demand or spot instances.
     * 
     * @param ec2
     * @param template
     * @return
     */
    public String createInstances(Ec2Client ec2, RunInstancesDto template) {

        log.info("Launch New Instance Request Start");

        RunInstancesRequest.Builder requestBuilder = RunInstancesRequest.builder()
                .imageId(template.getAmiId())
                .instanceType(template.getInstanceType())
                .minCount(template.getNumberOfInstances())
                .maxCount(template.getNumberOfInstances());

        if (template.getBlockStorageOptions() != null) {
            requestBuilder.blockDeviceMappings(getBlockDeviceMappings(template.getBlockStorageOptions()));
        }

        if (template.getSubnetId() != null) {
            requestBuilder.subnetId(template.getSubnetId());
        }

        if (template.getEnableMonitoring() != null) {
            requestBuilder.monitoring(
                    RunInstancesMonitoringEnabled.builder()
                            .enabled(template.getEnableMonitoring())
                            .build());
        }

        if (template.getInstanceMarketOptionsRequest() != null) {
            requestBuilder.instanceMarketOptions(getInstanceMarketOptions(template.getInstanceMarketOptionsRequest()));
        }

        if (template.getSecurityGroupIds() != null) {
            requestBuilder.securityGroupIds(template.getSecurityGroupIds());
        }

        if (template.getKeyName() != null) {
            requestBuilder.keyName(template.getKeyName());
        }

        if (template.getTags() != null) {
            requestBuilder.tagSpecifications(getTagSpecifications(template.getTags()));
        }

        try {
            RunInstancesResponse riRes = ec2.runInstances(requestBuilder.build());

            String instanceId = riRes.instances().get(0).instanceId();

            log.info("Launch New Instance Request Complete. instanceId: {}", instanceId);
            return instanceId;
        } catch (Ec2Exception e) {
            log.error("Launch New Instance Request Failed (Reason: {})", e.getMessage(), e);
            return "Error=" + e.getMessage();
        }
    }

    public TagSpecification getTagSpecifications(List<TagDto> vrmTags) {

        List<Tag> tags = new ArrayList<>();

        for (TagDto td : vrmTags) {
            Tag tag = Tag.builder()
                    .key(td.getKey())
                    .value(td.getValue())
                    .build();

            tags.add(tag);
        }
        return TagSpecification.builder().tags(tags).build();
    }

    public List<BlockDeviceMapping> getBlockDeviceMappings(List<BlockStorageOptionDto> vrmBSOs) {

        List<BlockDeviceMapping> bdms = new ArrayList<>();

        for (BlockStorageOptionDto bso : vrmBSOs) {
            EbsBlockDevice.Builder ebsBuilder = EbsBlockDevice.builder()
                    .volumeType(bso.getVolumeType())
                    .volumeSize(bso.getSize())
                    .snapshotId(bso.getSnapshotId())
                    .encrypted(bso.getEncrypted())
                    .kmsKeyId(bso.getKmsKeyId())
                    .deleteOnTermination(bso.getDeleteOnTermination());

            if ("io1".equals(bso.getVolumeType())) {
                ebsBuilder.iops(bso.getIops());
            } else if ("gp3".equals(bso.getVolumeType()) || "io2".equals(bso.getVolumeType())) {
                ebsBuilder.iops(bso.getIops()).throughput(bso.getThroughput());
            }

            BlockDeviceMapping bdm = BlockDeviceMapping.builder()
                    .ebs(ebsBuilder.build())
                    .deviceName(bso.getDeviceName())
                    .build();

            bdms.add(bdm);
        }
        return bdms;
    }

    public InstanceMarketOptionsRequest getInstanceMarketOptions(InstanceMarketOptionsRequestDto imor) {

        SpotMarketOptions.Builder smoBuilder = SpotMarketOptions.builder();

        if (imor.getInstanceInterruptionBehavior() != null) {
            smoBuilder.instanceInterruptionBehavior(imor.getInstanceInterruptionBehavior());
        }

        if (imor.getMaxPrice() != null) {
            smoBuilder.maxPrice(imor.getMaxPrice());
        }

        if (imor.getValidUntill() != null) {
            smoBuilder.validUntil(imor.getValidUntill());
        }

        if (imor.getSpotInstanceType() != null) {
            smoBuilder.spotInstanceType(imor.getSpotInstanceType());
        }

        InstanceMarketOptionsRequest imoreq = InstanceMarketOptionsRequest.builder()
                .marketType(imor.getMarketTypeAsString())
                .spotOptions(smoBuilder.build())
                .build();

        return imoreq;
    }

    public List<InstanceSpecificationsDto> getInstanceSpecification(Ec2Client ec2, String id) {

        List<InstanceSpecificationsDto> results = new ArrayList<>();

        List<Instance> instances = descService.getInstanceDesc(ec2, id);

        if (instances == null) {
            return null;
        }

        for (Instance i : instances) {
            InstanceSpecificationsDto isDto = new InstanceSpecificationsDto();

            isDto.setInstanceId(i.instanceId());
            isDto.setInstanceType(i.instanceTypeAsString());
            isDto.setInstanceState(i.state().nameAsString());
            if (i.privateDnsNameOptions() != null) {
                isDto.setHostType(i.privateDnsNameOptions().hostnameTypeAsString());
            }
            isDto.setIpV6Address(i.ipv6Address());
            isDto.setPublicIpV4Address(i.publicIpAddress());
            isDto.setPublicIpV4DNS(i.publicDnsName());
            isDto.setPrivateIpV4address(i.privateIpAddress());
            isDto.setPrivateIpV4DNS(i.privateDnsName());
            isDto.setVpcId(i.vpcId());
            isDto.setSubnetId(i.subnetId());
            isDto.setInstanceDetails(getInstanceDetails(ec2, i));
            isDto.setSecurityDetails(getSecuritySpecifics(ec2, i));
            isDto.setVolumeDetails(getVolumeSpecifications(ec2, i));
            isDto.setTags(VrmTags.getEc2Tags(i.tags()));

            results.add(isDto);
        }
        return results;
    }

    public List<InstanceDetailsDto> getInstanceDetails(Ec2Client ec2, Instance i) {

        List<InstanceDetailsDto> results = new ArrayList<>();

        String platform = i.platformAsString() == null ? "Linux" : i.platformAsString();
        String lifecycle = i.instanceLifecycleAsString() == null ? "on-demand" : i.instanceLifecycleAsString();

        InstanceDetailsDto idDto = new InstanceDetailsDto();

        idDto.setPlatform(platform);
        idDto.setPlatformDetails(i.platformDetails());
        idDto.setLaunchTime(i.launchTime());
        idDto.setHibernationOption(i.hibernationOptions().configured());
        idDto.setAmiId(i.imageId());
        idDto.setAmiLaunchIndex(i.amiLaunchIndex());
        idDto.setUsageOperation(i.usageOperation());
        idDto.setEnclavesSupport(i.enclaveOptions().enabled());
        idDto.setMonitoring(i.monitoring().stateAsString());
        DescribeInstanceAttributeResponse diaRes = ec2
                .describeInstanceAttribute(DescribeInstanceAttributeRequest.builder()
                        .instanceId(i.instanceId())
                        .attribute(InstanceAttributeName.DISABLE_API_TERMINATION)
                        .build());
        idDto.setTerminationProtection(diaRes.disableApiTermination().value());
        idDto.setLifecycle(lifecycle);
        idDto.setKeyPairName(i.keyName());
        idDto.setKernelId(i.kernelId());
        idDto.setRamDiskId(i.ramdiskId());
        idDto.setBootMode(i.bootModeAsString());
        idDto.setHostId(i.placement().hostId());
        idDto.setHostResourceGroupArn(i.placement().hostResourceGroupArn());
        idDto.setAffinity(i.placement().affinity());
        idDto.setTenancy(i.placement().tenancyAsString());
        idDto.setPartitionNumber(i.placement().partitionNumber());
        idDto.setPlacementGroup(i.placement().groupName());
        idDto.setVirtualizationType(i.virtualizationTypeAsString());
        idDto.setNumberOfVCpus(i.cpuOptions().coreCount());
        idDto.setCapacityReservationId(i.capacityReservationId());
        idDto.setCapacityReservationSetting(
                i.capacityReservationSpecification().capacityReservationPreferenceAsString());
        List<String> elasticInferenceAccelIds = new ArrayList<>();
        for (int j = 0; j < i.elasticGpuAssociations().size(); j++) {
            String elasticInferenceAccelId = i.elasticGpuAssociations().get(j).elasticGpuAssociationId();
            elasticInferenceAccelIds.add(elasticInferenceAccelId);
        }
        idDto.setElasticInferenceAcceleratorIds(elasticInferenceAccelIds);
        results.add(idDto);
        return results;
    }

    public List<SecuritySpecificationsDto> getSecuritySpecifics(Ec2Client ec2, Instance i) {
        List<SecuritySpecificationsDto> results = new ArrayList<>();
        for (int j = 0; j < i.securityGroups().size(); j++) {
            SecuritySpecificationsDto ssDto = new SecuritySpecificationsDto();
            String securityGroupId = i.securityGroups().get(j).groupId();
            ssDto.setSecurityGroupId(securityGroupId);
            ssDto.setGroupDetails(getSecurityGroupDetails(ec2, securityGroupId, i.securityGroups().get(j)));
            results.add(ssDto);
        }
        return results;
    }

    public SecurityGroupDetailsDto getSecurityGroupDetails(Ec2Client ec2, String securityGroupId, GroupIdentifier sg) {

        List<SecurityGroupRule> sgrs = descService.getSecurityGroupRuleDesc(ec2, null, securityGroupId);

        if (sgrs == null) {
            return null;
        }

        SecurityGroupDetailsDto srdDto = new SecurityGroupDetailsDto();
        List<SecurityRulesDto> inboundRules = new ArrayList<>();
        List<SecurityRulesDto> outboundRules = new ArrayList<>();

        for (SecurityGroupRule sgr : sgrs) {
            if (sgr.isEgress() == false) {
                inboundRules.add(getSecurityRuleDetails(sgr, sg));
            } else {
                outboundRules.add(getSecurityRuleDetails(sgr, sg));
            }
        }

        srdDto.setInboundRules(inboundRules);
        srdDto.setOutBoundRules(outboundRules);
        return srdDto;
    }

    public SecurityRulesDto getSecurityRuleDetails(SecurityGroupRule sgr, GroupIdentifier sg) {
        SecurityRulesDto srDto = new SecurityRulesDto();
        srDto.setSecurityGroupRuleId(sgr.securityGroupRuleId());
        srDto.setSecurityGroupName(sg.groupName());
        srDto.setPort(sgr.fromPort());
        srDto.setProtocol(sgr.ipProtocol());
        srDto.setSource(sgr.cidrIpv4());
        return srDto;
    }

    public BlockDeviceSpecificationsDto getVolumeSpecifications(Ec2Client ec2, Instance i) {
        BlockDeviceSpecificationsDto vsDto = new BlockDeviceSpecificationsDto();
        vsDto.setRootDeviceName(i.rootDeviceName());
        vsDto.setRootDeviceType(i.rootDeviceTypeAsString());
        vsDto.setEbsOptimized(i.ebsOptimized());
        vsDto.setEbsSpecifications(getVolumeDetails(ec2, i));
        return vsDto;
    }

    public List<EbsSpecificationsDto> getVolumeDetails(Ec2Client ec2, Instance i) {

        List<EbsSpecificationsDto> results = new ArrayList<>();

        for (InstanceBlockDeviceMapping ibdm : i.blockDeviceMappings()) {

            List<Volume> volumesList = descService.getVolumeDesc(ec2, ibdm.ebs().volumeId(), null);
            volumesList.get(0).size();

            EbsSpecificationsDto esDto = new EbsSpecificationsDto();

            esDto.setVolId(ibdm.ebs().volumeId());
            esDto.setDeviceName(ibdm.deviceName());
            esDto.setVolSize(volumesList.get(0).size());
            esDto.setAttachmentStatus(ibdm.ebs().statusAsString());
            esDto.setAttachmentTime(ibdm.ebs().attachTime());
            esDto.setEncrypted(volumesList.get(0).encrypted());
            esDto.setKmsKeyId(volumesList.get(0).kmsKeyId());
            esDto.setDeleteOnTermination(ibdm.ebs().deleteOnTermination());

            results.add(esDto);
        }
        return results;
    }

    /**
     * Start 1 or more instances.
     * 
     * @param instanceId
     * @param ec2
     * @return true or false regarding the state of the instance is set to start
     * @throws InterruptedException
     */
    public String startInstance(Ec2Client ec2, List<String> instanceIds) {

        log.info("Start Instance Request Start (instanceId: {})", instanceIds);

        try {
            ec2.startInstances(
                    StartInstancesRequest.builder()
                            .instanceIds(instanceIds)
                            .build());

            log.info("Start Instance Request Complete (instanceId: {})", instanceIds);
            return "starting";
        } catch (Ec2Exception e) {
            log.warn("Start Instance Request Failed (Reason: {})", e.getMessage(), e);
            return "Error=" + e.getMessage();
        }
    }

    /**
     * Stop 1 or more instances.
     * 
     * @param ec2
     * @param instanceId
     * @return true or false regarding the state of the instance is set to stop
     * @throws InterruptedException
     */
    public String stopInstance(Ec2Client ec2, List<String> instanceIds) {

        log.info("Stop Instance Request Start (instanceIds: {})", instanceIds);

        try {
            ec2.stopInstances(
                    StopInstancesRequest.builder()
                            .instanceIds(instanceIds)
                            .build());

            log.info("Stop Instance Request Complete (instanceIds: {})", instanceIds);
            return "stopping";
        } catch (Ec2Exception e) {
            log.error("Stop Instance Request Failed (Reason: {})", e.getMessage(), e);
            return "Error=" + e.getMessage();
        }
    }

    /**
     * Reboot 1 or more instances without stopping or shutting down an instance.
     * 
     * @param ec2
     * @param instanceId
     * @return
     */
    public ResultObject rebootInstance(Ec2Client ec2, List<String> instanceIds) {
        ResultObject result = new ResultObject();
        log.info("Reboot Instance Request Start (instanceId: {})", instanceIds);

        try {
            ec2.rebootInstances(
                    RebootInstancesRequest.builder()
                            .instanceIds(instanceIds)
                            .build());

            log.info("Reboot Instance Request Complete (instanceId: {})", instanceIds);
            result.setResult(true);
            result.setData(true);
            result.setMessage("Reboot Successfully Requested");
            return result;
        } catch (Ec2Exception e) {
            log.error("Reboot Instance Request Failed (Reason: {})", e.getMessage(), e);
            result.setResult(false);
            result.setData(e.getMessage());
            result.setMessage("Reboot Instance Request Failed");
            return result;
        }
    }

    /**
     * Terminate 1 or more instances.
     * 
     * @param ec2
     * @param instanceIds
     * @return
     */
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
