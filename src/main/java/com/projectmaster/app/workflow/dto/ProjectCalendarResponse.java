package com.projectmaster.app.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Calendar view response for project scheduling
 * Provides structured data for calendar UI components
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectCalendarResponse {
    
    private UUID projectId;
    private String projectName;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private int totalDuration;
    
    // Calendar data organized by time periods
    private List<CalendarStage> stages;
    private List<CalendarMilestone> milestones;
    private List<CalendarHoliday> holidays;
    
    // Summary information
    private CalendarSummary summary;
    
    /**
     * Calendar representation of a project stage
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarStage {
        private UUID stageId;
        private String stageName;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private int duration;
        private String status;
        private String color;
        private int order;
        
        // Nested tasks
        private List<CalendarTask> tasks;
    }
    
    /**
     * Calendar representation of a project task
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarTask {
        private UUID taskId;
        private String taskName;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private int duration;
        private String status;
        private String color;
        private int order;
        private UUID stageId;
        
        // Nested steps
        private List<CalendarStep> steps;
    }
    
    /**
     * Calendar representation of a project step
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarStep {
        private UUID stepId;
        private String stepName;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private int duration;
        private String status;
        private String color;
        private int order;
        private UUID taskId;
        private UUID stageId;
        
        // Assignment information
        private String assignedTo;
        private String assignedToType; // CREW, CONTRACTING_COMPANY
        private String specialty;
        
        // Dependencies
        private List<UUID> dependsOn;
        private List<UUID> dependents;
        private boolean dependenciesSatisfied;
        
        // Progress tracking
        private double progressPercentage;
        private boolean isCriticalPath;
        private int slackDays;
    }
    
    /**
     * Calendar milestone marker
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarMilestone {
        private UUID milestoneId;
        private String name;
        private String description;
        private LocalDate date;
        private String type; // STAGE_COMPLETE, TASK_COMPLETE, STEP_COMPLETE, PROJECT_COMPLETE
        private String status; // PENDING, COMPLETED, OVERDUE
        private String color;
        private UUID relatedEntityId;
        private String relatedEntityType;
    }
    
    /**
     * Calendar holiday marker
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarHoliday {
        private String name;
        private LocalDate date;
        private String type; // NATIONAL, STATE, REGIONAL
        private String stateCode;
        private String color;
        private boolean isWorkingDay;
    }
    
    /**
     * Calendar summary information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CalendarSummary {
        private int totalStages;
        private int totalTasks;
        private int totalSteps;
        private int completedItems;
        private int inProgressItems;
        private int pendingItems;
        private int overdueItems;
        
        // Timeline information
        private LocalDate earliestStart;
        private LocalDate latestEnd;
        private int totalBusinessDays;
        private int totalCalendarDays;
        
        // Critical path
        private int criticalPathDuration;
        private List<UUID> criticalPathSteps;
        
        // Resource utilization
        private int activeAssignments;
        private int availableCrew;
        private double utilizationRate;
        
        // Risk indicators
        private int atRiskItems;
        private int blockedItems;
        private double scheduleRisk;
    }
    
    /**
     * Get all calendar items (stages, tasks, steps) for a specific date
     */
    public List<CalendarItem> getItemsForDate(LocalDate date) {
        // This would be implemented to return all items that occur on the given date
        // For now, returning empty list as this is a DTO
        return List.of();
    }
    
    /**
     * Get all calendar items in a date range
     */
    public List<CalendarItem> getItemsInRange(LocalDate startDate, LocalDate endDate) {
        // This would be implemented to return all items in the date range
        // For now, returning empty list as this is a DTO
        return List.of();
    }
    
    /**
     * Get critical path items
     */
    public List<CalendarItem> getCriticalPathItems() {
        // This would be implemented to return all critical path items
        // For now, returning empty list as this is a DTO
        return List.of();
    }
    
    /**
     * Base interface for calendar items
     */
    public interface CalendarItem {
        UUID getId();
        String getName();
        LocalDate getStartDate();
        LocalDate getEndDate();
        String getStatus();
        String getType();
    }
}

