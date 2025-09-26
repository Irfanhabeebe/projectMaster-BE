package com.projectmaster.app.project.dto;

/**
 * Enum representing the types of entities that can have dependencies
 */
public enum DependencyEntityType {
    TASK("Task"),
    STEP("Step"),
    ADHOC_TASK("Ad-hoc Task");
    
    private final String displayName;
    
    DependencyEntityType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
