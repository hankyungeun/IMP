package com.bootest.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootest.task.InstanceTypeTask;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.regions.Region;

@RestController
@RequiredArgsConstructor
@RequestMapping("/instance-type")
public class InstanceTypeTesterController {

    private final InstanceTypeTask task;

    @GetMapping
    public String refreshInstanceType() throws Exception {
        task.reloadInstanceTypeInfo(List.of(Region.AP_NORTHEAST_2));
        return "done";
    }

}
