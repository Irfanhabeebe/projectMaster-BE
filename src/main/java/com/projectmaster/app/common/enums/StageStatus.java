package com.projectmaster.app.common.enums;

public enum StageStatus {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    BLOCKED("Blocked"),
    SKIPPED("Skipped");

    private final String displayName;

    StageStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}