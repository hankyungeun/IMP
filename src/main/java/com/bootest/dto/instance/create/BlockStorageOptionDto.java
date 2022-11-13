package com.bootest.dto.instance.create;

import lombok.Data;

@Data
public class BlockStorageOptionDto {
    private String volumeType;
    private String snapshotId;
    private String deviceName;
    private Integer size;
    private Integer iops;
    private Integer throughput;
    private Boolean deleteOnTermination = true;
    private Boolean encrypted;
    private String kmsKeyId;
}
