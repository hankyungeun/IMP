package com.bootest.dto.volume;

import java.util.List;

import lombok.Data;

@Data
public class DescribeVolumeDto {
    private String accountId;
    private String accountName;
    private String regionId;
    private List<DescribeVolumeDataDto> data;

    public DescribeVolumeDto() {

    }

    public DescribeVolumeDto(String accountId, String accountName, String regionId, List<DescribeVolumeDataDto> data) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.regionId = regionId;
        this.data = data;
    }
}
