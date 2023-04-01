package com.bootest.dto.optimizer;

import lombok.Data;

@Data
public class RightSizeThresholdRequest {
    private Double avgCpu = 2.0;
    private Double maxCpu = 30.0;
    private Double avgMem = 10.0;
    private Double maxMem = 99.0;
    private Double avgNetIn = 5.0;
    private Double avgNetOut = 5.0;
}
