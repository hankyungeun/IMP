package com.bootest.dto.optimizer;

import com.bootest.type.OptimizationType;

import lombok.Data;

@Data
public class OptimizationRequestDataDto {
    private String resourceId;
    private OptimizationType type;
    private String recommendation;
    private Float estimatedMonthlySavings;
}
