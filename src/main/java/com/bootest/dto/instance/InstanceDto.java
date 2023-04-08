package com.bootest.dto.instance;

import lombok.Data;

@Data
public class InstanceDto {
    private String instanceId;
    private String instanceType;
    private String os;
    private String volumeId;
}
