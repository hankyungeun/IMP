package com.bootest.dto.optimizer;

import com.bootest.model.AwsInstanceType;
import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;

import lombok.Data;
import software.amazon.awssdk.services.ec2.model.Instance;

@Data
public class GetModernInstanceOptResponse {
    private String resourceId;
    private String region;
    private String resourceName;
    private String accountId;
    private ServiceType serviceType;
    private OptimizationType optType;
    private RecommendedAction recommendedAction;
    private AwsInstanceType original;
    private AwsInstanceType recommendation;
    private String resourceType;
    private Float estimatedMonthlySavings;
    private String os;
    private String optimizationReason;
    private String lifeCycle;

    public GetModernInstanceOptResponse() {

    }

    public GetModernInstanceOptResponse(Instance i, String lifeCycle, String reason, Float savings, String accountId, String resourceName, String os, AwsInstanceType original, AwsInstanceType recommendation, String region) {
        this.resourceId = i.instanceId();
        this.region = region;
        this.resourceName = resourceName;
        this.accountId = accountId;
        this.serviceType = ServiceType.INSTANCE;
        this.optType = OptimizationType.VERSION_UP;
        this.recommendedAction = RecommendedAction.MODIFY;
        this.original = original;
        this.recommendation = recommendation;
        this.resourceType = i.instanceTypeAsString();
        this.estimatedMonthlySavings = savings;
        this.os = os;
        this.optimizationReason = reason;
        this.lifeCycle = lifeCycle;
    }
}
