package com.bootest.dto;

import lombok.Data;

@Data
public class OverviewResponse {
    private Integer instanceCnt;
    private Integer volCnt;
    private Float estimatedCost;

    public OverviewResponse() {

    }

    public OverviewResponse(Integer instanceCnt, Integer volCnt,Float estimatedCost) {
        this.instanceCnt = instanceCnt;
        this.volCnt = volCnt;
        this.estimatedCost = estimatedCost;
    }
}
