package com.bootest.dto.optimizer;

import lombok.Data;

@Data
public class GetResourceOptResponse {
    private String region;
    private GetInstanceOptResponse instanceOptResponse;

    public GetResourceOptResponse() {

    }

    public GetResourceOptResponse(String region, GetInstanceOptResponse instanceOptResponse) {
        this.region = region;
        this.instanceOptResponse = instanceOptResponse;
    }

}
