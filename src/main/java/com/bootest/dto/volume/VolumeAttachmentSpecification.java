package com.bootest.dto.volume;

import java.time.Instant;

import lombok.Data;

@Data
public class VolumeAttachmentSpecification {
    private String instanceId;
    private String deviceName;
    private Boolean deleteOnTermination;
    private Instant attachTime;
    private String volumeAttachmentStatus;
}
