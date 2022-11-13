package com.bootest.dto.usage;

import java.util.List;

import com.bootest.dto.optimizer.UsageMetricDto;

import lombok.Data;

@Data
public class AverageResourceUsage {

    private String accountId;
    private String accountName;
    private String region;
    private String resourceId;
    private String resourceName;
    private String resourceState;
    private String lifeCycle;
    private String os;
    private String instanceType;
    private String date;
    private List<UsageMetricDto> cpuUsageMetrics;
    private List<UsageMetricDto> netInUsageMetrics;
    private List<UsageMetricDto> netOutUsageMetrics;
    private Double avgCpuUsage;
    private Double avgNetIn;
    private Double avgNetOut;

}
