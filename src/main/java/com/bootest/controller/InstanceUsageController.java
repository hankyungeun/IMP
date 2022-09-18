package com.bootest.controller;


import com.bootest.repository.ResourceUsageRepo;
import org.springframework.ui.Model;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class InstanceUsageController {


    @GetMapping("/instanceUsage")
    public String instanceUsage() {
        return "instance";
    }
}
