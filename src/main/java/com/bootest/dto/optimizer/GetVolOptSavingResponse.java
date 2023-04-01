package com.bootest.dto.optimizer;

import lombok.Data;

@Data
public class GetVolOptSavingResponse {
    private Float totalEstimatedSavings;
    private Float sizeSavings;
    private Float iopsSavings;
    private Float throughputSavings;

    public GetVolOptSavingResponse() {
        
    }

    public GetVolOptSavingResponse(Float total, Float size, Float iops, Float throughput) {
        this.totalEstimatedSavings = total;
        this.sizeSavings = size;
        this.iopsSavings = iops;
        this.throughputSavings = throughput;
    }
}
