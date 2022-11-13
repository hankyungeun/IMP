package com.bootest.dto.instance.describe;

import lombok.Data;

@Data
public class SecuritySpecificationsDto {
    private String securityGroupId;
    private SecurityGroupDetailsDto groupDetails;
}
