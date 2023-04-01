package com.bootest.dto.optimizer;

import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;

import lombok.Data;
import software.amazon.awssdk.services.ec2.model.Instance;

@Data
public class GetUnusedInstanceOptResponse {
    private String resourceId;
    private String region;
    private String resourceName;
    private String accountId;
    private ServiceType serviceType;
    private OptimizationType optType;
    private RecommendedAction recommendedAction;
    private String resourceType;
    private Float estimatedMonthlySavings;
    private String os;
    private String optimizationReason;
    private String lifeCycle;

    public GetUnusedInstanceOptResponse() {
        
    }

    public GetUnusedInstanceOptResponse(Instance i, String os, String region, String accountId, String resourceName, String reason, Float estimatedMonthlySavings, String lifeCycle) {
        this.resourceId = i.instanceId();
        this.region = region;
        this.resourceName = resourceName;
        this.accountId = accountId;
        this.serviceType = ServiceType.INSTANCE;
        this.optType = OptimizationType.UNUSED;
        this.recommendedAction = RecommendedAction.DELETE;
        this.resourceType = i.instanceTypeAsString();
        this.estimatedMonthlySavings = estimatedMonthlySavings;
        this.os = os;
        this.optimizationReason = reason;
        this.lifeCycle = lifeCycle;
    }
}
