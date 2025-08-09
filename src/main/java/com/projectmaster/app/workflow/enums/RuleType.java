package com.projectmaster.app.workflow.enums;

public enum RuleType {
    PREREQUISITE,      // Must be satisfied before action
    VALIDATION,        // Validates current state
    BUSINESS_LOGIC,    // Custom business rules
    APPROVAL,          // Approval requirements
    RESOURCE,          // Resource availability
    TIMING             // Time-based constraints
}