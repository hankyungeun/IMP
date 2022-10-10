package com.bootest.type;

public enum OptimizationType {
    UNUSED("unused"),
    VERSION_UP("versionUp"),
    RIGHT_SIZE("rightSize");

    private String value;

    private OptimizationType(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }

}
