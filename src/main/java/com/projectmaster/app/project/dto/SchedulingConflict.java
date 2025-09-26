package com.projectmaster.app.project.dto;

import com.projectmaster.app.workflow.entity.DependencyEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Represents a scheduling conflict that prevents proper schedule calculation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulingConflict {
    
    private UUID entityId;
    private DependencyEntityType entityType;
    private String conflictType;
    private String description;
    private LocalDate conflictDate;
    private List<String> suggestions;
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    
    /**
     * Common conflict types
     */
    public static class ConflictTypes {
        public static final String CIRCULAR_DEPENDENCY = "CIRCULAR_DEPENDENCY";
        public static final String MISSING_DEPENDENCY = "MISSING_DEPENDENCY";
        public static final String INVALID_DURATION = "INVALID_DURATION";
        public static final String RESOURCE_CONFLICT = "RESOURCE_CONFLICT";
        public static final String DATE_CONFLICT = "DATE_CONFLICT";
        public static final String DEPENDENCY_LOOP = "DEPENDENCY_LOOP";
        public static final String INSUFFICIENT_TIME = "INSUFFICIENT_TIME";
    }
    
    /**
     * Common severities
     */
    public static class Severities {
        public static final String LOW = "LOW";
        public static final String MEDIUM = "MEDIUM";
        public static final String HIGH = "HIGH";
        public static final String CRITICAL = "CRITICAL";
    }
}
