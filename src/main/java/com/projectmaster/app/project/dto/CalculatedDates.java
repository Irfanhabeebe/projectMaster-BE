package com.projectmaster.app.project.dto;

import com.projectmaster.app.workflow.entity.DependencyEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Calculated dates for a specific entity (stage, task, or step)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatedDates {
    
    private UUID entityId;
    private DependencyEntityType entityType;
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
    
    /**
     * Check if this entity can start on the given date
     */
    public boolean canStartOn(LocalDate date) {
        return plannedStartDate != null && 
               plannedStartDate.equals(date) && 
               !isCompleted && 
               !isInProgress && 
               dependenciesSatisfied;
    }
    
    /**
     * Check if this entity is due to complete on the given date
     */
    public boolean isDueToCompleteOn(LocalDate date) {
        return plannedEndDate != null && 
               plannedEndDate.equals(date) && 
               !isCompleted;
    }
    
    /**
     * Check if this entity is overdue
     */
    public boolean isOverdue() {
        return plannedEndDate != null && 
               plannedEndDate.isBefore(LocalDate.now()) && 
               !isCompleted;
    }
}
