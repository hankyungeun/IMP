package com.bootest.dto.instance.create;

import java.util.List;

import com.bootest.dto.TagDto;

import lombok.Data;

@Data
public class RunInstancesDto {
    private String regionId;
    private String accountId;
    private String amiId;
    private String instanceType;
    private Integer numberOfInstances = 1;
    private InstanceMarketOptionsRequestDto instanceMarketOptionsRequest;
    private List<BlockStorageOptionDto> blockStorageOptions;
    private List<TagDto> tags;
    private List<String> securityGroupIds;
    private String subnetId = null;
    private Boolean enableMonitoring = false;
    private Boolean enableTerminationProtection = false;
    private String keyName;
}
