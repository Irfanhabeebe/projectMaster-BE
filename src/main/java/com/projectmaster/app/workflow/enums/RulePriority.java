package com.projectmaster.app.workflow.enums;

public enum RulePriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int value;

    RulePriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}