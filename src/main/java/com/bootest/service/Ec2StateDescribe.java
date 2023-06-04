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
}
