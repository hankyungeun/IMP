package com.bootest.type;

public enum ServiceType {
    INSTANCE("instance"),
    VOLUME("volume"),
    SNAPSHOT("snapshot"),
    ELB("elb"),
    EIP("eip");

    private String value;

    private ServiceType(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }
}
