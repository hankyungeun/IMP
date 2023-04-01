package com.bootest.dto.optimizer;

import java.util.List;

import com.bootest.model.AwsInstanceType;
import com.bootest.model.ResourceUsage;
import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;

import lombok.Data;

@Data
public class GetInstanceRightSizeOptResponse {
    private String resourceId;
    private String region;
    private String resourceName;
    private String accountId;
    private ServiceType serviceType;
    private OptimizationType optType;
    private RecommendedAction recommendedAction;
    private AwsInstanceType original;
    private List<AwsInstanceType> recommendations;
    private String resourceType;
    private Float estimatedMonthlySavings;
    private String os;
    private String optimizationReason;
    private String lifeCycle;
    private String defaultRecommendation;
    private Float originalMntlyPrice;
    private Float recommendationMntlyPrice;

    public GetInstanceRightSizeOptResponse() {

    }

    public GetInstanceRightSizeOptResponse(ResourceUsage usage, String resourceId, Float originalMntlyPrice, Float recommendationMntlyPrice, String defaultRecommendation, String reason, Float savings, AwsInstanceType original, List<AwsInstanceType> recommendations) {
        this.resourceId = resourceId;
        this.region = usage.getRegion();
        this.resourceName = usage.getResourceName();
        this.accountId = usage.getAccountId();
        this.serviceType = ServiceType.INSTANCE;
        this.optType = OptimizationType.RIGHT_SIZE;
        this.defaultRecommendation = defaultRecommendation;
        this.recommendedAction = RecommendedAction.MODIFY;
        this.original = original;
        this.recommendations = recommendations;
        this.resourceType = usage.getInstanceType();
        this.estimatedMonthlySavings = savings;
        this.os = usage.getOs();
        this.optimizationReason = reason;
        this.lifeCycle = usage.getLifeCycle();
        this.originalMntlyPrice = originalMntlyPrice;
        this.recommendationMntlyPrice = recommendationMntlyPrice;
    }
}
