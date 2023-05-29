package com.bootest.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

@Slf4j
@Service
public class Ec2ResourceDescribeService {

    public List<Filter> getFilters(Map<String, String> filterMap) {

        List<Filter> filters = new ArrayList<>();

        Filter.Builder fBuilder = Filter.builder();

        if (!filterMap.isEmpty()) {

            for (Entry<String, String> entry : filterMap.entrySet()) {

                fBuilder.name(entry.getKey()).values(entry.getValue());
                filters.add(fBuilder.build());

            }
        }
        return filters;
    }

    /**
     * 태그 상세정보 획득
     * 
     * @param ec2
     * @param instanceId
     * @param getName
     * @return 1 개 이상의 Tag 상세정보 List
     */
    public List<TagDescription> getTagDesc(Ec2Client ec2, String instanceId, Boolean getName) {

        DescribeTagsRequest.Builder requestBuilder = DescribeTagsRequest.builder();

        List<Filter> filters = new ArrayList<>();

        if (instanceId != null) {
            filters.add(Filter.builder().name("resource-id").values(instanceId).build());
        }

        if (getName == true) {
            filters.add(Filter.builder().name("key").values("Name").build());
        }

        if (!filters.isEmpty()) {
            requestBuilder.filters(filters);
        }

        try {

            DescribeTagsResponse response = ec2.describeTags(requestBuilder.build());

            return response.tags();

        } catch (Exception e) {
            log.error("Tags Not Found. Message: {}", e.getMessage());
            return null;
        }
    }

    /**
     * AWS Describe Request for EIP Address specifications.
     * 
     * @author minsoo
     * @param ec2
     * @param allocationId
     * @param instanceId
     * @return 1 or more EIP Address Specifications List
     */
    public List<Address> getAddressDesc(Ec2Client ec2, String allocationId, String instanceId) {

        List<Address> results = null;

        DescribeAddressesRequest.Builder requestBuilder = DescribeAddressesRequest.builder();

        if (allocationId != null) {
            requestBuilder.allocationIds(allocationId);
        }

        List<Filter> filters = new ArrayList<>();

        if (instanceId != null) {
            filters.add(Filter.builder().name("instance-id").values(instanceId).build());
        }

        if (!filters.isEmpty()) {
            requestBuilder.filters(filters);
        }

        try {
            DescribeAddressesResponse response = ec2.describeAddresses(requestBuilder.build());
            results = response.addresses();
        } catch (Exception e) {
            log.error("Failed to get EIP Address. Reason: {}", e.getMessage());
            return null;
        }
        return results;
    }

    /**
     * 아마존 머신 이미지(AMI) 상세정보 획득
     * 
     * @param ec2
     * @param id
     * @param owner
     * @return 1 개 이상의 AMI 상세정보 List
     */
    public List<Image> getImageDesc(Ec2Client ec2, String id, String owner) {

        List<Image> results = null;

        DescribeImagesRequest.Builder requestBuilder = DescribeImagesRequest.builder();

        if (id != null) {
            requestBuilder.imageIds(id);
        }

        if (owner != null) {
            requestBuilder.owners(owner);
        }

        try {

            DescribeImagesResponse response = ec2.describeImages(requestBuilder.build());

            results = response.images();

        } catch (Exception e) {
            log.error("Image Not Found. Message: {}", e.getMessage());
            return null;
        }
        return results;
    }

    public List<LaunchPermission> getImageAttributeDesc(Ec2Client ec2, String id) {

        List<LaunchPermission> results = null;

        DescribeImageAttributeRequest.Builder requestBuilder = DescribeImageAttributeRequest.builder()
                .attribute(ImageAttributeName.LAUNCH_PERMISSION);

        if (id != null) {
            requestBuilder.imageId(id);
        }

        try {

            DescribeImageAttributeResponse response = ec2.describeImageAttribute(requestBuilder.build());

            results = response.launchPermissions();

        } catch (Exception e) {
            log.error("Image Permission Not Found. Message: {}", e.getMessage());
            return null;
        }
        return results;
    }

    public List<KeyPairInfo> getKeyPairsDesc(Ec2Client ec2, String id) {

        List<KeyPairInfo> results = null;

        DescribeKeyPairsRequest.Builder requestBuilder = DescribeKeyPairsRequest.builder();

        if (id != null) {
            requestBuilder.keyPairIds(id);
        }

        try {

            DescribeKeyPairsResponse response = ec2.describeKeyPairs(requestBuilder.build());

            results = response.keyPairs();

        } catch (Exception e) {
            log.error("Key Pair Not Found. Message: {}", e.getMessage());
            return null;
        }
        return results;
    }

    /**
     * 인스턴스 상세정보 요청 획득
     * 
     * @param ec2
     * @param id
     * @return 1개 이상의 Instance 상세정보 리스트
     */
    public List<Instance> getInstanceDesc(Ec2Client ec2, String id) {

        List<Instance> results = new ArrayList<>();

        String nextToken = null;

        try {
            do {

                DescribeInstancesRequest.Builder requestBuilder = DescribeInstancesRequest.builder()
                        .nextToken(nextToken);

                if (id != null) {
                    requestBuilder.instanceIds(id);
                }

                DescribeInstancesResponse response = ec2.describeInstances(requestBuilder.build());

                for (Reservation r : response.reservations()) {
                    for (Instance i : r.instances()) {
                        results.add(i);
                    }
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
        } catch (Exception e) {
            log.error("Instance Not Found. Message: {}", e.getMessage());
            return null;
        }
        return results;
    }

    /**
     * 볼륨 Id 및 인스턴스 상세정보에 따른 볼륨 상세 정보 획득
     * 
     * @author Minsoo
     * @param ec2
     * @param volId
     * @param instance
     * @return 1개 이상의 Volume 상세정보 리스트
     */
    public List<Volume> getVolumeDesc(Ec2Client ec2, String volId, Instance instance) {

        List<Volume> results = new ArrayList<>();

        String nextToken = null;

        DescribeVolumesRequest.Builder requestBuilder = DescribeVolumesRequest.builder()
                .nextToken(nextToken);

        if (volId != null) {
            requestBuilder.volumeIds(volId);
        }

        if (instance != null) {
            Map<String, String> filterMap = new HashMap<>();
            filterMap.put("attachment.instance-id", instance.instanceId());
            filterMap.put("attachment.device", instance.rootDeviceName());

            requestBuilder.filters(getFilters(filterMap));
        }

        try {
            do {
                DescribeVolumesResponse response = ec2.describeVolumes(requestBuilder.build());

                for (Volume v : response.volumes()) {
                    results.add(v);
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
        } catch (Exception e) {
            log.error("No Volume Found. Message: {}", e.getMessage());
            return null;
        }
        return results;
    }

    public List<SecurityGroup> getSecurityGroupDesc(Ec2Client ec2, String id) {

        List<SecurityGroup> results = new ArrayList<>();

        String nextToken = null;

        DescribeSecurityGroupsRequest.Builder requestBuilder = DescribeSecurityGroupsRequest.builder()
                .nextToken(nextToken);

        if (id != null) {
            requestBuilder.groupIds(id);
        }

        try {
            do {
                DescribeSecurityGroupsResponse response = ec2.describeSecurityGroups(requestBuilder.build());

                for (SecurityGroup sg : response.securityGroups()) {
                    results.add(sg);
                }
                nextToken = response.nextToken();
            } while (nextToken != null);

        } catch (Exception e) {
            log.error("Security Groups Not Found. Message: {}", e.getMessage());
            return null;
        }
        return results;
    }

    public List<SecurityGroupRule> getSecurityGroupRuleDesc(Ec2Client ec2, String ruleId, String groupId) {

        List<SecurityGroupRule> results = new ArrayList<>();

        String nextToken = null;

        DescribeSecurityGroupRulesRequest.Builder requestBuilder = DescribeSecurityGroupRulesRequest.builder()
                .nextToken(nextToken);

        if (ruleId != null) {
            requestBuilder.securityGroupRuleIds(ruleId);
        }

        if (groupId != null) {
            Map<String, String> filterMap = new HashMap<>();
            filterMap.put("group-id", groupId);

            requestBuilder.filters(getFilters(filterMap));
        }

        try {
            do {
                DescribeSecurityGroupRulesResponse response = ec2.describeSecurityGroupRules(requestBuilder.build());

                for (SecurityGroupRule sgr : response.securityGroupRules()) {
                    results.add(sgr);
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
        } catch (Exception e) {
            log.error("Security Group Rules Not Found. Message: {}", e.getMessage());
            return null;
        }
        return results;
    }

    public List<Snapshot> getSnapshotDesc(Ec2Client ec2, String accountId, String id) {

        String nextToken = null;

        List<Snapshot> results = new ArrayList<>();

        DescribeSnapshotsRequest.Builder requestBuilder = DescribeSnapshotsRequest.builder()
                .nextToken(nextToken);

        if (accountId != null) {
            requestBuilder.ownerIds(accountId);
        }

        if (id != null) {
            requestBuilder.snapshotIds(id);
        }

        try {
            DescribeSnapshotsResponse response = ec2.describeSnapshots(requestBuilder.build());

            for (Snapshot s : response.snapshots()) {
                results.add(s);
            }
        } catch (Exception e) {
            log.error("Snapshot Not Found. Message: {}", e.getMessage());
            return null;
        }
        return results;
    }

    /**
     * 스팟 인스턴스 요청 상세 정보 획득
     * 
     * @author minsoo
     * @param ec2
     * @param spotRequestId
     * @param instanceId
     * @return 1개 이상의 Spot Request 상세정보 리스트
     */
    public List<SpotInstanceRequest> getSpotInstanceRequestDesc(Ec2Client ec2, String spotRequestId,
            String instanceId) {

        List<SpotInstanceRequest> results = new ArrayList<>();

        String nextToken = null;

        DescribeSpotInstanceRequestsRequest.Builder requestBuilder = DescribeSpotInstanceRequestsRequest.builder()
                .nextToken(nextToken);

        if (spotRequestId != null) {
            requestBuilder.spotInstanceRequestIds(spotRequestId);
        }

        if (instanceId != null) {
            Map<String, String> filterMap = new HashMap<>();
            filterMap.put("instance-id", instanceId);

            requestBuilder.filters(getFilters(filterMap));
        }

        try {
            do {
                DescribeSpotInstanceRequestsResponse response = ec2
                        .describeSpotInstanceRequests(requestBuilder.build());

                for (SpotInstanceRequest sir : response.spotInstanceRequests()) {
                    results.add(sir);
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
        } catch (Exception e) {
            log.error("Spot Request Not Found. Message: {}", e.getMessage());
            return null;
        }
        return results;
    }

    public BlockDeviceMapping getRootBlockDeviceMapping(Volume v, String snapshotId) {

        EbsBlockDevice.Builder ebsBuilder = EbsBlockDevice.builder()
                .volumeType(v.volumeType())
                .volumeSize(v.size())
                .encrypted(v.encrypted())
                .kmsKeyId(v.kmsKeyId())
                .deleteOnTermination(v.attachments().get(0).deleteOnTermination());

        if (VolumeType.IO1.equals(v.volumeType())) {
            ebsBuilder.iops(v.iops());
        } else if (VolumeType.GP3.equals(v.volumeType()) || VolumeType.IO2.equals(v.volumeType())) {
            ebsBuilder.iops(v.iops()).throughput(v.throughput());
        }

        if (snapshotId != null) {
            ebsBuilder.snapshotId(snapshotId);
        }

        return BlockDeviceMapping.builder()
                .ebs(ebsBuilder.build())
                .deviceName(v.attachments().get(0).device())
                .build();
    }

    // Do rather than describe

    /**
     * 스팟 인스턴스 상세 정보 획득
     * 
     * @param sir
     * @return instanceMarketOptions
     */
    public InstanceMarketOptionsRequest getInstanceMarketOption(SpotInstanceRequest sir) {
        SpotMarketOptions smo = SpotMarketOptions.builder()
                .instanceInterruptionBehavior(sir.instanceInterruptionBehavior())
                .spotInstanceType(sir.type())
                .build();
        InstanceMarketOptionsRequest imoReq = InstanceMarketOptionsRequest.builder()
                .marketType(MarketType.SPOT)
                .spotOptions(smo)
                .build();
        return imoReq;
    }

    /**
     * Get a list of mapping that consists of Volume Id and Device name.
     * 
     * @author minsoo
     * @param instances
     * @return {volumeId, deviceName}, ...
     */
    public Map<String, String> getVolIdNDevName(Instance instance) {

        log.info("Begin Volume Mapping Request instanceId: {}", instance.instanceId());

        Map<String, String> results = new HashMap<>();

        for (InstanceBlockDeviceMapping ibdm : instance.blockDeviceMappings()) {
            results.put(ibdm.ebs().volumeId(), ibdm.deviceName());
        }
        log.info("Volume Mapping Request Complete instanceId: {}", instance.instanceId());
        return results;
    }

    /**
     * 
     * @param ec2
     * @param aZoneGroup
     * @param vpcId
     * @return 1개 이상의 Subnet 상세정보 List
     */
    public List<Subnet> getSubnetDesc(Ec2Client ec2, String aZoneGroup, String vpcId) {

        log.info("Begin Subnet Describe Request. aZoneGroup: {}, vpcId: {}", aZoneGroup, vpcId);

        List<Subnet> results = new ArrayList<>();

        String nextToken = null;

        DescribeSubnetsRequest.Builder requestBuilder = DescribeSubnetsRequest.builder()
                .nextToken(nextToken);

        List<Filter> filters = new ArrayList<>();

        if (aZoneGroup != null) {
            filters.add(Filter.builder().name("availability-zone").values(aZoneGroup).build());
        }
        if (vpcId != null) {
            filters.add(Filter.builder().name("vpc-id").values(vpcId).build());
        }

        if (!filters.isEmpty()) {
            requestBuilder.filters(filters);
        }

        try {
            do {
                DescribeSubnetsResponse response = ec2.describeSubnets(requestBuilder.build());

                for (Subnet s : response.subnets()) {
                    results.add(s);
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
        } catch (Exception e) {
            log.error("Subnet Not Found. Message: {}", e.getMessage());
            return null;
        }

        log.info("Successfully Requested Subnet(s)");
        return results;
    }

    public void associateEIp(Ec2Client ec2, String allocationId, String instanceId) throws InterruptedException {

        AssociateAddressRequest request = AssociateAddressRequest.builder()
                .allocationId(allocationId)
                .instanceId(instanceId)
                .build();

        try {
            ec2.associateAddress(request);
        } catch (Exception e) {
            log.error("Unable to associate address. Message: {}", e.getMessage());
        }
    }

    public void editSpotMigrationTag(Ec2Client ec2, String instanceId) {

        log.info("Begin Edit Tag Request");

        List<TagDescription> tds = getTagDesc(ec2, instanceId, true);

        Tag.Builder tagBuilder = Tag.builder().key("Name");

        if (!tds.isEmpty() && tds != null) {
            tagBuilder.value(tds.get(0).value() + " *Migrated");
        } else {
            tagBuilder.value("*Migrated");
        }

        try {
            ec2.createTags(
                    CreateTagsRequest.builder()
                            .resources(instanceId)
                            .tags(tagBuilder.build())
                            .build());

            log.info("Edit Tag Request Complete instanceId: {}", instanceId);
        } catch (Exception e) {
            log.error("Edit Tag Request Failed Message: {}", e.getMessage());
        }
    }

    public List<TagSpecification> getTagSpecification(List<Tag> tags, List<ResourceType> resourceTypes) {
        List<TagSpecification> results = new ArrayList<>();

        TagSpecification.Builder tsBuilder = TagSpecification.builder();

        for (ResourceType rt : resourceTypes) {
            tsBuilder.tags(tags).resourceType(rt);
            results.add(tsBuilder.build());
        }
        return results;
    }

    public List<SnapshotTierStatus> getSnapshotTierStatus(Ec2Client ec2, String snapshotId, String volumeId) {
        List<SnapshotTierStatus> results = new ArrayList<>();

        String nextToken = null;

        DescribeSnapshotTierStatusRequest.Builder requestBuilder = DescribeSnapshotTierStatusRequest.builder()
                .nextToken(nextToken);

        List<Filter> filters = new ArrayList<>();

        if (snapshotId != null) {
            filters.add(Filter.builder().name("snapshot-id").values(snapshotId).build());
        }

        if (volumeId != null) {
            filters.add(Filter.builder().name("volume-id").values(volumeId).build());
        }

        if (!filters.isEmpty()) {
            requestBuilder.filters(filters);
        }

        try {
            do {
                DescribeSnapshotTierStatusResponse response = ec2.describeSnapshotTierStatus(requestBuilder.build());
                for (SnapshotTierStatus sts : response.snapshotTierStatuses()) {
                    results.add(sts);
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
        } catch (Exception e) {
            log.error("Get Snapshot Tier Status Request Failed (Message: {})", e.getMessage(), e);
            return null;
        }
        return results;
    }

    public String getResourceName(List<Tag> tags) {
        String name = null;

        for (Tag t : tags) {
            if (t.key().equals("Name")) {
                name = t.value();
            }
        }
        return name;
    }

}
