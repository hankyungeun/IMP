package com.bootest.dto.instance.describe;

import java.util.List;

import lombok.Data;

@Data
public class SecurityGroupDetailsDto {
    private List<SecurityRulesDto> inboundRules;
    private List<SecurityRulesDto> outBoundRules;
}
