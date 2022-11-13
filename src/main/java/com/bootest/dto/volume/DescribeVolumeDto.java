package com.bootest.dto.volume;

import java.util.List;

import lombok.Data;

@Data
public class DescribeVolumeDto {
    private String accountId;
    private String accountName;
    private String regionId;
    private List<DescribeVolumeDataDto> data;
}
