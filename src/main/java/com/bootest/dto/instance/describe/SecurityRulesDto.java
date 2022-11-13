package com.bootest.dto.instance.describe;

import lombok.Data;

@Data
public class SecurityRulesDto {
    private String securityGroupRuleId;
    private String securityGroupName;
    private Integer port;
    private String protocol;
    private String source;
}
