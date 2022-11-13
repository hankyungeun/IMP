package com.bootest.dto.volume;

import lombok.Data;

@Data
public class DetachVolumeDto {
    private String regionId;
    private String accountId;
    private String instanceId;
    private String volumeId;
    private Boolean force;
}
