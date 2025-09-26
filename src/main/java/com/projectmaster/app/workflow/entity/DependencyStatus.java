package com.projectmaster.app.workflow.entity;

/**
 * Enum representing the status of a dependency
 */
public enum DependencyStatus {
    PENDING,    // Waiting for dependency to be satisfied
    SATISFIED,  // Dependency is satisfied, dependent entity can proceed
    BLOCKED     // Dependency cannot be satisfied (e.g., blocked task)
}
