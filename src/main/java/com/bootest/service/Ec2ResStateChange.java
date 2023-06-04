package com.bootest.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttachVolumeRequest;
import software.amazon.awssdk.services.ec2.model.AttachVolumeResponse;
import software.amazon.awssdk.services.ec2.model.CancelSpotInstanceRequestsRequest;
import software.amazon.awssdk.services.ec2.model.DeleteVolumeRequest;
import software.amazon.awssdk.services.ec2.model.DetachVolumeRequest;
import software.amazon.awssdk.services.ec2.model.DetachVolumeResponse;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.SpotInstanceRequest;
import software.amazon.awssdk.services.ec2.model.StartInstancesRequest;
import software.amazon.awssdk.services.ec2.model.StopInstancesRequest;
import software.amazon.awssdk.services.ec2.model.TerminateInstancesRequest;

@Slf4j
@RequiredArgsConstructor
@Service
public class Ec2ResStateChange {
    private final Ec2StateDescribe ec2State;
    private final Ec2ResourceDescribeService descService;

    /**
     * Request to stop an instance of given instanceId.
     * 
     * @param ec2
     * @param instanceId
     * @return true or false regarding the state of the instance is set to stop
     * @throws InterruptedException
     */
    public Boolean stopInstance(Ec2Client ec2, String instanceId) throws InterruptedException {

        log.info("Begin Stop Instance Request instanceId: {}", instanceId);

        boolean stopped = false;

        try {
            for (int i = 0; i < 100; i++) {

                String state = ec2State.getInstanceState(instanceId, ec2);

                if ("running".equals(state)) {

                    log.info("Requesting for running instance to stop");

                    ec2.stopInstances(
                            StopInstancesRequest.builder()
                                    .instanceIds(instanceId)
                                    .build());

                } else if ("stopping".equals(state) || "stopped".equals(state)) {
                    log.info("Stop Instance Request Complete instanceId: {}", instanceId);
                    stopped = true;
                    break;
                } else if ("terminated".equals(state)) {
                    log.warn("Unable to stop terminated instance");
                    return false;
                } else {
                    Thread.sleep(2000);
                    continue;
                }
            }
        } catch (Exception e) {
            log.error("Exceeeded All Attempts. Message: {}", e.getMessage());
            return false;
        }
        return stopped;
    }

    /**
     * Request to start instance of given instanceId.
     * 
     * @param instanceId
     * @param ec2
     * @return true or false regarding the state of the instance is set to start
     * @throws InterruptedException
     */
    public Boolean startInstance(String instanceId, Ec2Client ec2) throws InterruptedException {

        log.info("Begin Start Instance Request of instanceId: {}", instanceId);

        boolean started = false;

        for (int i = 0; i < 100; i++) {

            String instanceState = ec2State.getInstanceState(instanceId, ec2);

            if ("pending".equals(instanceState)) {
                Thread.sleep(2000);
                continue;
            } else if ("running".equals(instanceState)) {
                started = true;
                log.info("Successfully started instanceId: {}, state: {}", instanceId, instanceState);
                break;
            } else if ("terminated".equals(instanceState) || "terminating".equals(instanceState)) {
                log.info("Unable to start a terminated instanceId: {}, state: {}", instanceId, instanceState);
                return started;
            } else {
                try {
                    ec2.startInstances(
                            StartInstancesRequest.builder()
                                    .instanceIds(instanceId)
                                    .build());

                } catch (Exception e) {
                    log.warn("The following instance cannot be started");
                }
                Thread.sleep(2000);
                continue;
            }
        }
        return started;
    }
}
