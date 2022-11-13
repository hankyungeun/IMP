package com.bootest.dto.usage;

import java.util.List;

import com.bootest.dto.optimizer.UsageMetricDto;
import com.bootest.type.UsageDataType;

import lombok.Data;

@Data
public class AverageUsageDto {
    private String accountId;
    private String accountName;
    private String region;
    private String resourceId;
    private String resourceName;
    private UsageDataType usageDataType;
    private String resourceState;
    private String lifeCycle;
    private String os;
    private String instanceType;
    private String date;
    private List<UsageMetricDto> usageMetrics;
    private Double avgUsage;
}
