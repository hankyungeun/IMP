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

//    public List<TagDescription> getTagDesc(Ec2Client ec2, String instanceId, Boolean getName) {
//
//        DescribeTagsRequest.Builder requestBuilder = DescribeTagsRequest.builder();
//
//        List<Filter> filters = new ArrayList<>();
//
//        if (instanceId != null) {
//            filters.add(Filter.builder().name("resource-id").values(instanceId).build());
//        }
//
//        if (getName == true) {
//            filters.add(Filter.builder().name("key").values("Name").build());
//        }
//
//        if (!filters.isEmpty()) {
//            requestBuilder.filters(filters);
//        }
//
//        try {
//
//            DescribeTagsResponse response = ec2.describeTags(requestBuilder.build());
//
//            return response.tags();
//
//        } catch (Exception e) {
//            log.error("Tags Not Found. Message: {}", e.getMessage());
//            return null;
//        }
//    }
//
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

//    public List<SecurityGroupRule> getSecurityGroupRuleDesc(Ec2Client ec2, String ruleId, String groupId) {
//
//        List<SecurityGroupRule> results = new ArrayList<>();
//
//        String nextToken = null;
//
//        DescribeSecurityGroupRulesRequest.Builder requestBuilder = DescribeSecurityGroupRulesRequest.builder()
//                .nextToken(nextToken);
//
//        if (ruleId != null) {
//            requestBuilder.securityGroupRuleIds(ruleId);
//        }
//
//        if (groupId != null) {
//            Map<String, String> filterMap = new HashMap<>();
//            filterMap.put("group-id", groupId);
//
//            requestBuilder.filters(getFilters(filterMap));
//        }
//
//        try {
//            do {
//                DescribeSecurityGroupRulesResponse response = ec2.describeSecurityGroupRules(requestBuilder.build());
//
//                for (SecurityGroupRule sgr : response.securityGroupRules()) {
//                    results.add(sgr);
//                }
//                nextToken = response.nextToken();
//            } while (nextToken != null);
//        } catch (Exception e) {
//            log.error("Security Group Rules Not Found. Message: {}", e.getMessage());
//            return null;
//        }
//        return results;
//    }

//    public Map<String, String> getVolIdNDevName(Instance instance) {
//
//        log.info("Begin Volume Mapping Request instanceId: {}", instance.instanceId());
//
//        Map<String, String> results = new HashMap<>();
//
//        for (InstanceBlockDeviceMapping ibdm : instance.blockDeviceMappings()) {
//            results.put(ibdm.ebs().volumeId(), ibdm.deviceName());
//        }
//        log.info("Volume Mapping Request Complete instanceId: {}", instance.instanceId());
//        return results;
//    }

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
