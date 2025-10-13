package com.projectmaster.app.workflow.entity;

/**
 * Enum representing the types of entities that can be involved in dependencies
 */
public enum DependencyEntityType {
    STAGE("Stage"),
    TASK("Task"),
    STEP("Step");

    private final String displayName;

    DependencyEntityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
