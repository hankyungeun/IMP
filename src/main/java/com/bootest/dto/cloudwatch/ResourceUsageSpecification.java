package com.bootest.dto.cloudwatch;

import java.time.Instant;
import java.util.Map;

import lombok.Data;

@Data
public class ResourceUsageSpecification {
    
    private String resourceId;

    private String usageId;

    private String label;

    private Map<Instant, Double> timeAndValues;

}
