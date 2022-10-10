package com.bootest.dto.optimizer;

import lombok.Data;

@Data
public class InstanceResourceDetailsDto {
    private String os;
    private String instanceType;
    private Integer vCpus;
    private Float memoryGiB;
    private String networkPerformance;
    private Float hourlyPrice;
    private Float monthlyPrice;
    private Float estimatedMonthlySavings;
    private String hypervisor;
    private String architecture;
    private Double sustainedClockSpeedInGhz;

}
