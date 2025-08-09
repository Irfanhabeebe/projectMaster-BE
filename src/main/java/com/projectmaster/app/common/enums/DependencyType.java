package com.projectmaster.app.common.enums;

public enum DependencyType {
    FINISH_TO_START("Finish to Start"),
    START_TO_START("Start to Start"),
    FINISH_TO_FINISH("Finish to Finish"),
    START_TO_FINISH("Start to Finish");

    private final String displayName;

    DependencyType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}