package com.projectmaster.app.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Result of a project schedule calculation
 * Contains calculated start/end dates for all project entities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCalculationResult {
    
    private UUID projectId;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private int totalBusinessDays;
    private int totalCalendarDays;
    
    // Calculated dates for each entity
    private Map<UUID, CalculatedDates> entityDates;
    
    // Summary information
    private ScheduleSummary summary;
    private List<ScheduleWarning> warnings;
    private List<ScheduleError> errors;
    
    // Metadata
    private LocalDateTime calculatedAt;
    private String calculatedBy;
    private String calculationMethod;
    private boolean success;
    
    /**
     * Calculated dates for a specific entity (stage, task, or step)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalculatedDates {
        private UUID entityId;
        private String entityType; // STAGE, TASK, STEP
        private String entityName;
        
        // Calculated dates
        private LocalDate plannedStartDate;
        private LocalDate plannedEndDate;
        private LocalDate actualStartDate;
        private LocalDate actualEndDate;
        
        // Duration information
        private int estimatedDays;
        private int actualDays;
        private int businessDays;
        
        // Status information
        private String status;
        private boolean isCriticalPath;
        private int slackDays;
        
        // Dependencies
        private List<UUID> dependsOn;
        private List<UUID> dependents;
        private boolean dependenciesSatisfied;
        
        // Progress tracking
        private double progressPercentage;
        private boolean isCompleted;
        private boolean isInProgress;
        private boolean isBlocked;
        
        // Notes and metadata
        private String notes;
        private LocalDateTime lastUpdated;
    }
    
    /**
     * Summary information about the calculated schedule
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleSummary {
        private int totalStages;
        private int totalTasks;
        private int totalSteps;
        private int completedStages;
        private int completedTasks;
        private int completedSteps;
        private int inProgressStages;
        private int inProgressTasks;
        private int inProgressSteps;
        private int blockedStages;
        private int blockedTasks;
        private int blockedSteps;
        
        // Critical path information
        private List<UUID> criticalPathEntities;
        private int criticalPathDuration;
        private int totalSlackDays;
        
        // Parallel execution opportunities
        private int parallelExecutionOpportunities;
        private int estimatedTimeSaving;
        
        // Risk indicators
        private double scheduleRisk;
        private int overdueItems;
        private int atRiskItems;
    }
    
    /**
     * Warning about potential schedule issues
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleWarning {
        private String code;
        private String message;
        private String severity; // LOW, MEDIUM, HIGH
        private UUID entityId;
        private String entityType;
        private String recommendation;
    }
    
    /**
     * Error that prevented schedule calculation
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleError {
        private String code;
        private String message;
        private UUID entityId;
        private String entityType;
        private String resolution;
    }
    
    /**
     * Get calculated dates for a specific entity
     */
    public CalculatedDates getEntityDates(UUID entityId) {
        return entityDates != null ? entityDates.get(entityId) : null;
    }
    
    /**
     * Check if the calculation was successful
     */
    public boolean isSuccessful() {
        return success && (errors == null || errors.isEmpty());
    }
    
    /**
     * Get the total project duration in business days
     */
    public int getProjectDuration() {
        return totalBusinessDays;
    }
    
    /**
     * Get all entities on the critical path
     */
    public List<CalculatedDates> getCriticalPathEntities() {
        if (entityDates == null || summary == null || summary.getCriticalPathEntities() == null) {
            return List.of();
        }
        
        return summary.getCriticalPathEntities().stream()
                .map(entityDates::get)
                .filter(dates -> dates != null)
                .toList();
    }
    
    /**
     * Get all entities that are currently blocked
     */
    public List<CalculatedDates> getBlockedEntities() {
        if (entityDates == null) {
            return List.of();
        }
        
        return entityDates.values().stream()
                .filter(CalculatedDates::isBlocked)
                .toList();
    }
    
    /**
     * Get all entities that are currently in progress
     */
    public List<CalculatedDates> getInProgressEntities() {
        if (entityDates == null) {
            return List.of();
        }
        
        return entityDates.values().stream()
                .filter(CalculatedDates::isInProgress)
                .toList();
    }
    
    /**
     * Get all entities that are completed
     */
    public List<CalculatedDates> getCompletedEntities() {
        if (entityDates == null) {
            return List.of();
        }
        
        return entityDates.values().stream()
                .filter(CalculatedDates::isCompleted)
                .toList();
    }
    
    /**
     * Get entities that can start on a specific date
     */
    public List<CalculatedDates> getEntitiesReadyToStart(LocalDate date) {
        if (entityDates == null) {
            return List.of();
        }
        
        return entityDates.values().stream()
                .filter(dates -> dates.getPlannedStartDate() != null && 
                               dates.getPlannedStartDate().equals(date) &&
                               !dates.isCompleted() &&
                               !dates.isInProgress() &&
                               dates.isDependenciesSatisfied())
                .toList();
    }
    
    /**
     * Get entities that are due to complete on a specific date
     */
    public List<CalculatedDates> getEntitiesDueToComplete(LocalDate date) {
        if (entityDates == null) {
            return List.of();
        }
        
        return entityDates.values().stream()
                .filter(dates -> dates.getPlannedEndDate() != null && 
                               dates.getPlannedEndDate().equals(date) &&
                               !dates.isCompleted())
                .toList();
    }
}

