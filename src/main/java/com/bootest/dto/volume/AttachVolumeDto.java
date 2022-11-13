package com.bootest.dto.volume;

import lombok.Data;

@Data
public class AttachVolumeDto {
    private String regionId;
    private String accountId;
    private String instanceId;
    private String volumeId;
    private String device;
}
