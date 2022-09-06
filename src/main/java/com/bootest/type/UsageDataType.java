package com.bootest.type;

public enum UsageDataType {

    CPU("cpu"),
    MEMORY("memory"),
    NET_IN("netIn"),
    NET_OUT("netOut"),
    DISK("disk");

    private String value;

    private UsageDataType(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }

}
