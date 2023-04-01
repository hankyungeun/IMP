package com.bootest.dto.optimizer;

import java.util.List;

import lombok.Data;

@Data
public class GetRightSizeOptResponse {
    private String region;
    private List<GetInstanceRightSizeOptResponse> rightSizeInstances;

    public GetRightSizeOptResponse() {
        
    }

    public GetRightSizeOptResponse(String region, List<GetInstanceRightSizeOptResponse> rightSizeInstances) {
        this.region = region;
        this.rightSizeInstances = rightSizeInstances;
    }
}
