package com.bootest.dto.instance.describe;

import java.time.Instant;

import lombok.Data;

@Data
public class EbsSpecificationsDto {
    private String volId;
    private String deviceName;
    private Integer volSize;
    private String attachmentStatus;
    private Instant attachmentTime;
    private Boolean encrypted;
    private String kmsKeyId;
    private Boolean deleteOnTermination;
}
