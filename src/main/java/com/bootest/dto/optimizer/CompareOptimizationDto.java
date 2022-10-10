package com.bootest.dto.optimizer;

import lombok.Data;

@Data
public class CompareOptimizationDto {
    private String resourceId;
    private String region;
    private String resourceType;
    private String recommendedSuggestion;
    private Integer daysToCompare;
}
