package com.bootest.dto.optimizer;

import java.util.List;

import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;

import lombok.Data;

@Data
public class OptimizationTargetDataDto {
    private String region;
    private String accountId;
    private String accountName;
    private String resourceId;
    private String resourceName;
    private ServiceType serviceType;
    private String resourceType;
    private OptimizationType optimizationType;
    private RecommendedAction recommendedAction;
    private InstanceResourceDetailsDto instanceDetails;
    private List<RightSizeRecommendationDto> rightSizeRecommendations;
    private InstanceResourceDetailsDto versionUpInstanceRecommendation;
    private Float estimatedMonthlySavings;
}
