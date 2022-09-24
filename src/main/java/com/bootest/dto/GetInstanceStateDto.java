package com.bootest.dto;

import lombok.Data;

@Data
public class GetInstanceStateDto {
    private int running;
    private int stopped;
    private int terminated;
    private int spot;
    private int onDemand;
}
