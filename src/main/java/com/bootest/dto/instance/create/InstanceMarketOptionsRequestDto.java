package com.bootest.dto.instance.create;

import java.time.Instant;

import lombok.Data;

@Data
public class InstanceMarketOptionsRequestDto {
    /**
     * "spot" to request spot instance, and leave it as null to request "OnDemand"
     */
    private String marketTypeAsString;
    private String instanceInterruptionBehavior = null;
    private String maxPrice = null;
    private String spotInstanceType = "one-time";
    private Instant validUntill = null;

}
