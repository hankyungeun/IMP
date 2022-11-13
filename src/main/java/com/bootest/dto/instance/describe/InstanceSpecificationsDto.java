package com.bootest.dto.instance.describe;

import java.util.List;

import com.bootest.dto.TagDto;

import lombok.Data;

@Data
public class InstanceSpecificationsDto {
    private String instanceId;
    private String instanceType;
    private String instanceState;
    private String hostType;
    private String ipV6Address;
    private String publicIpV4Address;
    private String publicIpV4DNS;
    private String privateIpV4address;
    private String privateIpV4DNS;
    private String vpcId;
    private String subnetId;
    private List<InstanceDetailsDto> instanceDetails;
    private List<SecuritySpecificationsDto> securityDetails;
    private BlockDeviceSpecificationsDto volumeDetails;
    private List<TagDto> tags;
}
