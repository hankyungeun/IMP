package com.bootest.dto;

import java.util.List;

import lombok.Data;

@Data
public class GetMonthlyUsageDto {
    private String accountId;
    private String accountName;
    private String region;
    private String resourceId;
    private String resourceName;
    private String os;
    private String instanceType;
    private List<StatisticDataValueDto> cpuAvg;
    private List<StatisticDataValueDto> memAvg;
    private List<StatisticDataValueDto> diskAvg;
    private List<StatisticDataValueDto> netInAvg;
    private List<StatisticDataValueDto> netOutAvg;

}
