package com.bootest.dto.optimizer;

import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;

import lombok.Data;
import software.amazon.awssdk.services.ec2.model.Volume;

@Data
public class GetModernVolOptResponse {
    private String resourceId;
    private String region;
    private String resourceName;
    private String accountId;
    private ServiceType serviceType;
    private OptimizationType optType;
    private RecommendedAction recommendedAction;
    private Float originalCost;
    private Float recommendationCost;
    private String resourceType;
    private Float estimatedMonthlySavings;
    private String optimizationReason;
    private Integer size;
    private Integer iops;
    private Integer throughput;

    public GetModernVolOptResponse() {

    }

    public GetModernVolOptResponse(Volume v, String reason, Float savings, String accountId, String resourceName, Float original, Float recommendation, String region) {
        this.resourceId = v.volumeId();
        this.region = region;
        this.resourceName = resourceName;
        this.accountId = accountId;
        this.serviceType = ServiceType.VOLUME;
        this.optType = OptimizationType.VERSION_UP;
        this.recommendedAction = RecommendedAction.MODIFY;
        this.originalCost = original;
        this.recommendationCost = recommendation;
        this.resourceType = v.volumeTypeAsString();
        this.estimatedMonthlySavings = savings;
        this.optimizationReason = reason;
        this.size = v.size();
        this.iops = v.iops();
        this.throughput = v.throughput();
    }
}
