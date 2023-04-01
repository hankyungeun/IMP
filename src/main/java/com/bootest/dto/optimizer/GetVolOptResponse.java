package com.bootest.dto.optimizer;

import java.util.List;

import lombok.Data;

@Data
public class GetVolOptResponse {
    private List<GetUnusedVolOptResponse> unusedVols;
    private List<GetModernVolOptResponse> outDatedVols;

    public GetVolOptResponse() {
        
    }

    public GetVolOptResponse(List<GetUnusedVolOptResponse> unusedVols, List<GetModernVolOptResponse> outDatedVols) {
        this.unusedVols = unusedVols;
        this.outDatedVols = outDatedVols;
    }
}
