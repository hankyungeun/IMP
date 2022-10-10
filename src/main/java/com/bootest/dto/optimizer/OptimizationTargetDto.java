package com.bootest.dto.optimizer;

import java.util.List;

import lombok.Data;

@Data
public class OptimizationTargetDto {
    private Integer totalTargetCnt;
    private List<OptimizationTargetDataDto> data;

}
