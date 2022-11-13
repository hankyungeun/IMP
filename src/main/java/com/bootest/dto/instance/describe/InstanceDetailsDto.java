package com.bootest.dto.instance.describe;

import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data
public class InstanceDetailsDto {
    private String platform;
    private String platformDetails;
    private Instant launchTime;
    private Boolean hibernationOption;
    private String amiId;
    private Integer amiLaunchIndex;
    private String usageOperation;
    private Boolean enclavesSupport;
    private String monitoring;
    private Boolean terminationProtection;
    private String lifecycle;
    private String keyPairName;
    private String kernelId;
    private String ramDiskId;
    private String bootMode;
    private String hostId;
    private String hostResourceGroupArn;
    private String affinity;
    private String tenancy;
    private Integer partitionNumber;
    private String placementGroup;
    private String virtualizationType;
    private Integer numberOfVCpus;
    private String capacityReservationId;
    private String capacityReservationSetting;
    private List<String> elasticInferenceAcceleratorIds;

}
