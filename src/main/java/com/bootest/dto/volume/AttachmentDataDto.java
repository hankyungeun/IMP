package com.bootest.dto.volume;

import lombok.Data;

@Data
public class AttachmentDataDto {
    private String instanceId;
    private String deviceName;

    public AttachmentDataDto() {

    }

    public AttachmentDataDto(String instanceId, String deviceName) {
        this.instanceId = instanceId;
        this.deviceName = deviceName;
    }
}
