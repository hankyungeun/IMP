package com.bootest.dto.optimizer;

import java.util.List;

import lombok.Data;

@Data
public class OptimizationRequestFormDto {
    private List<OptimizationRequestDataDto> data;
}
