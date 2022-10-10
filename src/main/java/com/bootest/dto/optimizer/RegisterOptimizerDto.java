package com.bootest.dto.optimizer;

import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;

import lombok.Data;

@Data
public class RegisterOptimizerDto {
    private String resourceId;
    private String resourceName;
    private ServiceType serviceType;
    private String region;
    private String accountId;
    private String accountName;
    private OptimizationType optType;
    private RecommendedAction action;
    private String resourceType;
    private String recommendation;
    private Float estimatedMonthlySavings;
    private String instanceOs;
    private String optimizationReason;
}
