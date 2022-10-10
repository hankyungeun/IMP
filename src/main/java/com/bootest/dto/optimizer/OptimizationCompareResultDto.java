package com.bootest.dto.optimizer;

import java.util.List;

import lombok.Data;

@Data
public class OptimizationCompareResultDto {
    private String resourceId;
    private String region;
    private String resourceType;
    private String recommendedSuggestion;
    private String comparedDate;
    private Double cpuUsage;
    private Double maxCpuUsage;
    private Double memUsage;
    private Double maxMemUsage;
    private Double netInUsage;
    private Double netOutUsage;
    private List<UsageMetricDto> cpuUsageMetrics;
    private List<UsageMetricDto> maxCpuUsageMetrics;
    private List<UsageMetricDto> memUsageMetrics;
    private List<UsageMetricDto> maxMemUsageMetrics;
    private List<UsageMetricDto> netInUsageMetrics;
    private List<UsageMetricDto> netOutUsageMetrics;
    private List<UsageMetricDto> cpuUsageChangeMetrics;
    private List<UsageMetricDto> maxCpuUsageChangeMetrics;
    private List<UsageMetricDto> memUsageChangeMetrics;
    private List<UsageMetricDto> maxMemUsageChangeMetrics;

}
