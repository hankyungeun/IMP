package com.bootest.dto.instance.describe;

import java.util.List;

import lombok.Data;

@Data
public class BlockDeviceSpecificationsDto {
    private String rootDeviceName;
    private String rootDeviceType;
    private Boolean ebsOptimized;
    private List<EbsSpecificationsDto> ebsSpecifications;
}
