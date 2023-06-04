package com.bootest.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import com.bootest.dto.TagDto;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

@Slf4j
@Service
public class Ec2ResDescribeService {

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
     * 인스턴스 상세정보 요청 획득
     * 
     * @param ec2
     * @param id
     * @return 1개 이상의 Instance 상세정보 리스트
     */
    public List<Instance> getInstanceDesc(Ec2Client ec2, String id) {

        List<Instance> results = new ArrayList<>();

        String nextToken = null;

        DescribeInstancesRequest.Builder requestBuilder = DescribeInstancesRequest.builder()
                .nextToken(nextToken);

        if (id != null) {
            requestBuilder.instanceIds(id);
        }

        try {
            do {
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
