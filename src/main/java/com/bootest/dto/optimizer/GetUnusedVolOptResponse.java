package com.bootest.dto.optimizer;

import com.bootest.type.OptimizationType;
import com.bootest.type.RecommendedAction;
import com.bootest.type.ServiceType;

import lombok.Data;
import software.amazon.awssdk.services.ec2.model.Volume;

@Data
public class GetUnusedVolOptResponse {
    private String resourceId;
    private String region;
    private String resourceName;
    private String accountId;
    private ServiceType serviceType;
    private Integer size;
    private Integer throughput;
    private Integer iops;
    private OptimizationType optType;
    private RecommendedAction recommendedAction;
    private String resourceType;
    private Float estimatedMonthlySavings;
    private String optimizationReason;
    private GetVolOptSavingResponse savingsResponses;

    public GetUnusedVolOptResponse() {
        
    }

    public GetUnusedVolOptResponse(Volume v, String region, String accountId, String resourceName, String reason, Float estimatedMonthlySavings, GetVolOptSavingResponse savingsResponses) {
        this.resourceId = v.volumeId();
        this.region = region;
        this.resourceName = resourceName;
        this.accountId = accountId;
        this.serviceType = ServiceType.VOLUME;
        this.optType = OptimizationType.UNUSED;
        this.recommendedAction = RecommendedAction.DELETE;
        this.resourceType = v.volumeTypeAsString();
        this.estimatedMonthlySavings = estimatedMonthlySavings;
        this.optimizationReason = reason;
        this.savingsResponses = savingsResponses;
        this.size = v.size();
        this.throughput = v.throughput();
        this.iops = v.iops();
    }
}
