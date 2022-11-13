package com.bootest.dto.instance.state;

import java.util.List;

import lombok.Data;

@Data
public class InstanceStateChangeDto {
    private String regionId;
    private String accountId;
    private List<String> instanceIds;

}
