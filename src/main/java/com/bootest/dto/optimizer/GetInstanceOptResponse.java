package com.bootest.dto.optimizer;

import java.util.List;

import lombok.Data;

@Data
public class GetInstanceOptResponse {
    private List<GetUnusedInstanceOptResponse> unusedInstances;
    private List<GetModernInstanceOptResponse> outDatedInstances;

    public GetInstanceOptResponse() {
        
    }

    public GetInstanceOptResponse(List<GetUnusedInstanceOptResponse> unusedInstances, List<GetModernInstanceOptResponse> outDatedInstances) {
        this.unusedInstances = unusedInstances;
        this.outDatedInstances = outDatedInstances;
    }
}
