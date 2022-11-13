package com.bootest.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

@Slf4j
@Service
public class Ec2StateDescribe {
    /**
     * Get the state of the instance.
     * 
     * @param srcInstanceId
     * @return reponse.get(0).instances().get(0).state(); (e.g. Stopping, Running,
     *         etc.)
     * @throws InterruptedException
     */
    public String getInstanceState(String instanceId, Ec2Client ec2) throws InterruptedException {
        String result = null;
        for (int i = 0; i < 30; i++) {
            DescribeInstancesResponse dires = ec2.describeInstances(
                    DescribeInstancesRequest.builder()
                            .instanceIds(instanceId)
                            .build());

            for (Reservation r : dires.reservations()) {
                for (Instance instance : r.instances()) {
                    result = instance.state().nameAsString();
                    if (result == null || result.isBlank()) {
                        Thread.sleep(2000);
                        continue;
                    } else {
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the state of the Volume.
     * 
     * @param srcVolumeId
     * @return response.volumes().get(0).state(); (e.g. In use, Available and,etc)
     */
    public String getVolumeState(String volumeId, Ec2Client ec2) {
        DescribeVolumesResponse response = ec2
                .describeVolumes(DescribeVolumesRequest.builder().volumeIds(volumeId).build());

        return response.volumes().get(0).stateAsString();
    }

    /**
     * Get the state of the spot request with spot request ID.
     * 
     * @param spotReqId
     * @return spotInstanceState
     */
    public String getSpotReqStateBySpotReqId(String spotReqId, Ec2Client ec2) {
        int cnt = 0;
        String sis = null;

        while (cnt++ <= 6) {
            try {
                List<SpotInstanceRequest> response = ec2
                        .describeSpotInstanceRequests(
                                DescribeSpotInstanceRequestsRequest.builder().spotInstanceRequestIds(spotReqId).build())
                        .spotInstanceRequests();
                sis = response.get(0).stateAsString();
            } catch (Ec2Exception e) {
                log.warn("Describe Spot Instance Request Exception : {}", e.awsErrorDetails());
                if (cnt == 6) {
                    log.error("모든 시도 횟수를 초과했습니다. ERROR MESSAGE: {}", e.awsErrorDetails());
                    throw e;
                }
            }
        }
        return sis;
    }

    /**
     * Get the state of the spot request with instance ID.
     * 
     * @param instanceId
     * @param ec2
     * @return spotInstanceState
     * @throws InterruptedException
     */
    public String getSpotReqStateByInstanceId(String instanceId, Ec2Client ec2) throws InterruptedException {
        Filter f1 = Filter.builder().name("instance-id").values(instanceId).build();
        String result = null;
        for (int i = 0; i < 50; i++) {
            List<SpotInstanceRequest> spotReqSpecification = ec2
                    .describeSpotInstanceRequests(DescribeSpotInstanceRequestsRequest.builder().filters(f1).build())
                    .spotInstanceRequests();
            if (spotReqSpecification.size() != 0) {
                result = spotReqSpecification.get(0).stateAsString();
                break;
            } else {
                Thread.sleep(2000);
                continue;
            }
        }
        return result;
    }
}
