package com.projectmaster.app.common.enums;

public enum UserRole {
    SUPER_USER("Super User"),
    ADMIN("Admin"),
    PROJECT_MANAGER("Project Manager"),
    TRADIE("Tradie"),
    CUSTOMER("Customer");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}