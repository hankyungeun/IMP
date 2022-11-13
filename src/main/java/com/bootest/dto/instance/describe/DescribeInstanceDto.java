package com.bootest.dto.instance.describe;

import java.util.List;

import lombok.Data;

@Data
public class DescribeInstanceDto {
    private String accountId;
    private String accountName;
    private List<InstanceSpecificationsDto> primaryDetails;
}
