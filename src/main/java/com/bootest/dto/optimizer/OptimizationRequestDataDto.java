package com.bootest.dto.optimizer;

import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;

import lombok.Data;

@Data
public class OptimizationRequestDataDto {
    private String region;
    private String resourceId;
    private ServiceType serviceType;
    private OptimizationType optimizationType;
    private RecommendedAction recommendedAction;
    private String recommendation;
}
