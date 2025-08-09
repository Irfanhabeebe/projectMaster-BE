package com.projectmaster.app.common.enums;

public enum NotificationType {
    TASK_ASSIGNED("Task Assigned"),
    TASK_DUE_SOON("Task Due Soon"),
    TASK_OVERDUE("Task Overdue"),
    TASK_COMPLETED("Task Completed"),
    TASK_UPDATED("Task Updated"),
    TASK_COMMENTED("New Comment"),
    DEPENDENCY_COMPLETED("Dependency Completed"),
    MILESTONE_APPROACHING("Milestone Approaching"),
    PROJECT_UPDATE("Project Update");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}