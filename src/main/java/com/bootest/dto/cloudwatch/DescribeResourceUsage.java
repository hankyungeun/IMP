package com.bootest.dto.cloudwatch;

import java.util.List;

import lombok.Data;

@Data
public class DescribeResourceUsage {
    
    private String accountId;

    private List<ResourceUsageSpecification> specifications;

}
