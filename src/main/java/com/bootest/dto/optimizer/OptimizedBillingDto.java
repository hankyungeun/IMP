package com.bootest.dto.optimizer;

import java.util.List;

import lombok.Data;

@Data
public class OptimizedBillingDto {
    private Float monthToDate;
    private Float forecast;
    private List<OptimizedBillingDataDto> data;
}
