package com.bootest.dto.usage;

import lombok.Data;

import java.util.Map;

@Data
public class SummaryResponse {
    private Float avgCpu;
    private Float avgNetIn;
    private Float avgNetOut;
    private Map<String, Float> cpuDailyAvgs;
    private Map<String, Float> netInDailyAvgs;
    private Map<String, Float> netOutDailyAvgs;

    public SummaryResponse() {
        
    }

    public SummaryResponse(Float avgCpu, Float avgNetIn, Float avgNetOut, Map<String, Float> cpuDailyAvgs, Map<String, Float> netInDailyAvgs, Map<String, Float> netOutDailyAvgs) {
        this.avgCpu = avgCpu;
        this.avgNetIn = avgNetIn;
        this.avgNetOut = avgNetOut;
        this.cpuDailyAvgs = cpuDailyAvgs;
        this.netInDailyAvgs = netInDailyAvgs;
        this.netOutDailyAvgs = netOutDailyAvgs;
    }
}
