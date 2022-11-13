package com.bootest.dto.volume;

import java.util.List;

import com.bootest.dto.TagDto;

import lombok.Data;

@Data
public class CreateVolumeDto {
    private String regionId;
    private String accountId;
    private String volumeType;
    private Integer size;
    private Integer iops;
    private Integer throughput;
    private String availabilityZone;
    private String snapshotId;
    private Boolean encryption;
    private String kmsKey;
    private List<TagDto> tags;
}
