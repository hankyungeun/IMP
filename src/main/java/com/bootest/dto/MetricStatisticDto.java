package com.bootest.dto;

import lombok.Data;

@Data
public class MetricStatisticDto {
    private String label;
    private StatisticDataDto average;
    private StatisticDataDto max;
    private StatisticDataDto min;
}
