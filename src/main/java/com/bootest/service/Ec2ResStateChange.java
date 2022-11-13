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

    public void terminateInstance(String instanceId, Ec2Client ec2) throws InterruptedException {

        log.info("Begin Terminate Instance Request instanceId: {}", instanceId);

        boolean terminated = false;

        for (int i = 0; i < 100; i++) {

            String instanceState = ec2State.getInstanceState(instanceId, ec2);

            if ("terminated".equals(instanceState) || "shutting-down".equals(instanceState)) {

                terminated = true;
                log.info("Terminate Instance Request Complete instanceId: {}", instanceId);
                break;

            } else {

                log.info("Terminating");

                ec2.terminateInstances(
                        TerminateInstancesRequest.builder()
                                .instanceIds(instanceId)
                                .build());

                Thread.sleep(2000);
                continue;

            }
        }

        if (!terminated) {
            throw new IllegalStateException("Terminate Instance Request Failed");
        }
    }

    public void startInstanceFromSpotReq(Ec2Client ec2, String instanceId) throws InterruptedException {
        log.info("Start Spot Instance Request Start (instanceId: {})", instanceId);
        boolean results = false;

        for (int i = 0; i < 100; i++) {

            String spotState = ec2State.getSpotReqStateByInstanceId(instanceId, ec2);

            if ("disabled".equals(spotState)) {
                ec2.startInstances(
                        StartInstancesRequest.builder()
                                .instanceIds(instanceId)
                                .build());

                results = true;
                break;

            } else {
                Thread.sleep(4000);
                continue;
            }
        }
        if (results == false) {
            log.warn("Start Instance Request Failed instanceId: {}", instanceId);
        }
        log.info("Start Spot Instance Request Complete (instanceId: {})", instanceId);
    }

    public void cancelSpotRequest(SpotInstanceRequest sir, Ec2Client ec2) throws InterruptedException {

        boolean result = false;

        ec2.cancelSpotInstanceRequests(
                CancelSpotInstanceRequestsRequest.builder()
                        .spotInstanceRequestIds(sir.spotInstanceRequestId())
                        .build());

        for (int i = 0; i < 100; i++) {

            String sis = ec2State.getSpotReqStateBySpotReqId(sir.spotInstanceRequestId(), ec2);

            if ("cancelled".equals(sis)) {
                result = true;
                break;
            } else {
                // log.warn("스팟 요청 취소 중 tries: {}, state: {}", i, sis);
                Thread.sleep(2000);
                continue;
            }
        }
        log.info("스팟 요청 취소 완료 spotReqId: {}, results: {}", sir.spotInstanceRequestId(), result);
    }

    public void deleteVolumeAttachedToInstance(Ec2Client ec2, String instanceId) throws InterruptedException {

        log.info("Begin Detach And Delete Storage Request instanceId: {}", instanceId);

        List<Instance> instances = descService.getInstanceDesc(ec2, instanceId);

        Instance instance = null;

        if (instances != null && !instances.isEmpty()) {
            instance = instances.get(0);
        }

        Map<String, String> volumeIdNDevName = descService.getVolIdNDevName(instance);

        for (Entry<String, String> entry : volumeIdNDevName.entrySet()) {

            for (int i = 0; i < 50; i++) {

                String state = ec2State.getInstanceState(instanceId, ec2);

                if ("stopped".equals(state)) {

                    ec2.detachVolume(
                            DetachVolumeRequest.builder()
                                    .volumeId(entry.getKey())
                                    .build());
                    break;

                } else {
                    Thread.sleep(2000);
                    continue;
                }
            }

            for (int i = 0; i < 50; i++) {

                if ("available".equals(ec2State.getVolumeState(entry.getKey(), ec2))) {

                    ec2.deleteVolume(
                            DeleteVolumeRequest.builder()
                                    .volumeId(entry.getKey())
                                    .build());
                    break;

                } else {
                    Thread.sleep(2000);
                    continue;
                }
            }
        }
        log.info("Detach And Delete Storage Request Complete instanceId: {}", instanceId);
    }

    public void attachVolumes(Ec2Client ec2, Map<String, String> volIdNDevNames, String instanceId) {

        log.info("Begin Attach Volume Request instanceId: {}", instanceId);

        volIdNDevNames.forEach((key, value) -> {
            for (int i = 0; i < 100; i++) {
                try {
                    if ("stopped".equals(ec2State.getInstanceState(instanceId, ec2))) {

                        String volumeState = ec2State.getVolumeState(key, ec2);

                        if ("available".equals(volumeState)) {

                            AttachVolumeResponse response = ec2.attachVolume(
                                    AttachVolumeRequest.builder()
                                            .instanceId(instanceId)
                                            .volumeId(key)
                                            .device(value)
                                            .build());

                            log.info("Attach Volume Request Complete instanceId: {}, volumeId: {}", instanceId,
                                    response.volumeId());
                            break;

                        } else {
                            Thread.sleep(2000);
                            continue;
                        }
                    } else {
                        Thread.sleep(2000);
                        continue;
                    }
                } catch (InterruptedException e) {
                    if (i == 100)
                        log.error("Exceeded All Tries");
                    e.printStackTrace();
                }
            }
        });
    }

    public void detachVolume(Ec2Client ec2, String instanceId, Map<String, String> volIdNDevNames) {

        log.info("Begin Detach Volume Request instanceId: {}", instanceId);

        volIdNDevNames.forEach((key, value) -> {

            for (int i = 0; i < 100; i++) {
                try {

                    String state = ec2State.getInstanceState(instanceId, ec2);

                    if ("stopped".equals(state)) {

                        String volumeState = ec2State.getVolumeState(key, ec2);

                        if ("in-use".equals(volumeState)) {

                            DetachVolumeResponse response = ec2.detachVolume(
                                    DetachVolumeRequest.builder()
                                            .instanceId(instanceId)
                                            .volumeId(key)
                                            .device(value)
                                            .build());

                            log.info("Detach Volume Request Complete instanceId: {}, volumeId: {}", instanceId,
                                    response.volumeId());
                            break;

                        } else {
                            Thread.sleep(2000);
                            continue;
                        }
                    } else if ("terminated".equals(state) || "shutting-down".equals(state)) {
                        log.debug("Cannot Detach Volume From Terminated Instance");
                        break;
                    } else {
                        Thread.sleep(2000);
                        continue;
                    }
                } catch (InterruptedException e) {
                    if (i == 100)
                        log.error("Exceeded All Tries");
                    e.printStackTrace();
                }
            }
        });
    }

    public void checkStoppedSpot(Ec2Client ec2, String instanceId) throws InterruptedException {
        log.info("Spot Instance State Request Start (instanceId: {})", instanceId);
        boolean results = false;

        for (int i = 0; i < 100; i++) {

            String spotState = ec2State.getSpotReqStateByInstanceId(instanceId, ec2);

            if ("disabled".equals(spotState)) {

                results = true;
                break;

            } else {
                Thread.sleep(4000);
                continue;
            }
        }
        if (results == false) {
            log.warn("Spot Instance State Request Failed (Exceeded all tries)");
        }
        log.info("Spot Instance State Request Complete (instanceId: {})", instanceId);
    }
}
