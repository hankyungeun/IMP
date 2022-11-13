package com.bootest.dto.volume;

import java.time.Instant;
import java.util.List;

import com.bootest.dto.TagDto;

import lombok.Data;

@Data
public class DescribeVolumeDataDto {
    private String volumeId;
    private String volumeType;
    private Integer size;
    private String volumeState;
    private Integer iops;
    private Integer throughput;
    private Boolean encrypted;
    private String kmsKeyId;
    private String snapshotId;
    private String availabilityZone;
    private Instant created;
    private Boolean multiAttachEnabled;
    private List<VolumeAttachmentSpecification> attachmentSpecifications;
    private List<TagDto> tags;
}
