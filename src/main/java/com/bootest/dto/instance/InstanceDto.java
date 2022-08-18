package com.bootest.dto.instance;

import java.util.List;

import lombok.Data;
import software.amazon.awssdk.services.ec2.model.Instance;

@Data
public class InstanceDto {
    private String instanceId;
    private String instanceType;
    private String os;
    private String volumeId;
}
