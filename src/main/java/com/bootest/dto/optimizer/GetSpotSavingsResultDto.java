package com.bootest.dto.optimizer;

import java.util.List;

import lombok.Data;

@Data
public class GetSpotSavingsResultDto {
    private Double linuxSpotSavings;
    private Double windowsSpotSavings;
    private List<CostDataDto> dailyLinuxSpotSavings;
    private List<CostDataDto> dailyWindowsSpotSavings;
}
