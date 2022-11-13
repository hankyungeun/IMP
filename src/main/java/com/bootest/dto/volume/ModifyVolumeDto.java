package com.bootest.dto.volume;

import lombok.Data;

@Data
public class ModifyVolumeDto {
    private String regionId;
    private String accountId;
    private String volumeId;
    private Integer iops;
    private String volumeType;
    private Boolean multiAttachEnabled;
    private Integer size;
    private Integer throughput;
}
