package com.bootest.service;

import org.springframework.stereotype.Service;

@Service
public class InstanceFamilyUpToDate {
    public String instanceFamilySwitch(String instanceFamily) {

        switch (instanceFamily) {
            case "t2":
                return "t3";
            case "c4":
                return "c5";
            case "d2":
                return "d3";
            case "i2":
                return "i3";
            case "m4":
                return "m5";
            case "r3":
                return "r5";
            case "r4":
                return "r5";
            default:
                return null;
        }
    }
}
