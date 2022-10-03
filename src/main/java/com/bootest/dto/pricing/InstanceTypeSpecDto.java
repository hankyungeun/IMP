package com.bootest.dto.pricing;

import lombok.Data;

@Data
public class InstanceTypeSpecDto {
    private String region;
    private String os;
    private String instanceType;
    private Float cost;
}
