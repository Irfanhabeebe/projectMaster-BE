package com.projectmaster.app.common.enums;

public enum ActivityType {
    CREATED("Task Created"),
    UPDATED("Task Updated"),
    STATUS_CHANGED("Status Changed"),
    ASSIGNED("Task Assigned"),
    UNASSIGNED("Task Unassigned"),
    PRIORITY_CHANGED("Priority Changed"),
    DUE_DATE_CHANGED("Due Date Changed"),
    PROGRESS_UPDATED("Progress Updated"),
    COMMENT_ADDED("Comment Added"),
    ATTACHMENT_ADDED("Attachment Added"),
    ATTACHMENT_REMOVED("Attachment Removed"),
    TIME_LOGGED("Time Logged"),
    DEPENDENCY_ADDED("Dependency Added"),
    DEPENDENCY_REMOVED("Dependency Removed"),
    BLOCKED("Task Blocked"),
    UNBLOCKED("Task Unblocked");

    private final String displayName;

    ActivityType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}