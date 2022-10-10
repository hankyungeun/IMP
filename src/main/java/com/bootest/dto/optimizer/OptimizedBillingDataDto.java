package com.bootest.dto.optimizer;

import lombok.Data;

@Data
public class OptimizedBillingDataDto {
    private String resourceId;
    private String resourceType;
    private Float estimatedSavings;
}
