package com.bootest.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {


    @GetMapping("/instanceUsage")
    public String instanceUsage() {
        return "instance";
    }

    @GetMapping("/recommendation")
    public String recommendation() {
        return "recommendation";
    }

}
