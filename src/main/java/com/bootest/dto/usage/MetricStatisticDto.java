package com.bootest.dto.usage;

import com.bootest.dto.StatisticDataDto;

import lombok.Data;

@Data
public class MetricStatisticDto {
    private String label;
    private StatisticDataDto average;
    private StatisticDataDto max;
    private StatisticDataDto min;
}
