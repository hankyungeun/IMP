package com.bootest.type;

public enum RecommendedAction {
    DELETE("delete"),
    ARCHIVE("Archive"),
    TERMINATE("terminate"),
    MODIFY("modify");

    private String value;

    private RecommendedAction(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }
}
