package com.projectmaster.app.project.service;

import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Context class that holds all the data and state needed for schedule calculation
 * Implements composite pattern for better organization of scheduling logic
 */
@Data
@Builder
@Slf4j
public class ScheduleCalculationContext {
    
    private final Project project;
    private final List<ProjectStage> stages;
    private final List<ProjectTask> tasks;
    private final List<ProjectStep> steps;
    private final List<ProjectDependency> dependencies;
    private final ProjectBusinessCalendarService businessCalendarService;
    
    // Calculated dates storage
    private final Map<UUID, CalculatedEntityDates> calculatedDates;
    
    // Lookup maps for efficient access
    private final Map<UUID, ProjectStage> stageMap;
    private final Map<UUID, ProjectTask> taskMap;
    private final Map<UUID, ProjectStep> stepMap;
    
    // Dependency maps for efficient lookup
    private final Map<UUID, List<ProjectDependency>> dependenciesByEntity;
    private final Map<UUID, List<ProjectDependency>> dependentsOfEntity;
    
    // Grouping maps
    private final Map<UUID, List<ProjectTask>> tasksByStage;
    private final Map<UUID, List<ProjectStep>> stepsByTask;
    
    /**
     * Data class to hold calculated dates for any entity
     */
    @Data
    @Builder
    public static class CalculatedEntityDates {
        private UUID entityId;
        private DependencyEntityType entityType;
        private String entityName;
        private LocalDate plannedStartDate;
        private LocalDate plannedEndDate;
        private Integer estimatedDays;
        private boolean dependenciesSatisfied;
        private List<UUID> dependsOn;
        private List<UUID> dependents;
    }
    
    /**
     * Create a context from project entities
     */
    public static ScheduleCalculationContext create(
            Project project,
            List<ProjectStage> stages,
            List<ProjectTask> tasks,
            List<ProjectStep> steps,
            List<ProjectDependency> dependencies,
            ProjectBusinessCalendarService businessCalendarService) {
        
        // Create lookup maps
        Map<UUID, ProjectStage> stageMap = stages.stream()
                .collect(Collectors.toMap(ProjectStage::getId, stage -> stage));
        
        Map<UUID, ProjectTask> taskMap = tasks.stream()
                .collect(Collectors.toMap(ProjectTask::getId, task -> task));
        
        Map<UUID, ProjectStep> stepMap = steps.stream()
                .collect(Collectors.toMap(ProjectStep::getId, step -> step));
        
        // Create dependency maps
        Map<UUID, List<ProjectDependency>> dependenciesByEntity = dependencies.stream()
                .collect(Collectors.groupingBy(ProjectDependency::getDependentEntityId));
        
        Map<UUID, List<ProjectDependency>> dependentsOfEntity = dependencies.stream()
                .collect(Collectors.groupingBy(ProjectDependency::getDependsOnEntityId));
        
        // Create grouping maps
        Map<UUID, List<ProjectTask>> tasksByStage = tasks.stream()
                .collect(Collectors.groupingBy(task -> task.getProjectStage().getId()));
        
        Map<UUID, List<ProjectStep>> stepsByTask = steps.stream()
                .collect(Collectors.groupingBy(step -> step.getProjectTask().getId()));
        
        return ScheduleCalculationContext.builder()
                .project(project)
                .stages(stages)
                .tasks(tasks)
                .steps(steps)
                .dependencies(dependencies)
                .businessCalendarService(businessCalendarService)
                .calculatedDates(new HashMap<>())
                .stageMap(stageMap)
                .taskMap(taskMap)
                .stepMap(stepMap)
                .dependenciesByEntity(dependenciesByEntity)
                .dependentsOfEntity(dependentsOfEntity)
                .tasksByStage(tasksByStage)
                .stepsByTask(stepsByTask)
                .build();
    }
    
    /**
     * Get entity by ID and type
     */
    public Object getEntity(UUID entityId, DependencyEntityType entityType) {
        switch (entityType) {
            case STAGE:
                return stageMap.get(entityId);
            case TASK:
                return taskMap.get(entityId);
            case STEP:
                return stepMap.get(entityId);
            default:
                return null;
        }
    }
    
    /**
     * Get entity name by ID and type
     */
    public String getEntityName(UUID entityId, DependencyEntityType entityType) {
        switch (entityType) {
            case STAGE:
                ProjectStage stage = stageMap.get(entityId);
                return stage != null ? stage.getName() : "Unknown Stage";
            case TASK:
                ProjectTask task = taskMap.get(entityId);
                return task != null ? task.getName() : "Unknown Task";
            case STEP:
                ProjectStep step = stepMap.get(entityId);
                return step != null ? step.getName() : "Unknown Step";
            default:
                return "Unknown Entity";
        }
    }
    
    /**
     * Get entity estimated days by ID and type
     */
    public Integer getEntityEstimatedDays(UUID entityId, DependencyEntityType entityType) {
        switch (entityType) {
            case STAGE:
                ProjectStage stage = stageMap.get(entityId);
                return stage != null ? stage.getEstimatedDurationDays() : null;
            case TASK:
                ProjectTask task = taskMap.get(entityId);
                return task != null ? task.getEstimatedDays() : null;
            case STEP:
                ProjectStep step = stepMap.get(entityId);
                return step != null ? step.getEstimatedDays() : null;
            default:
                return null;
        }
    }
    
    /**
     * Get dependencies for an entity
     */
    public List<ProjectDependency> getDependenciesFor(UUID entityId) {
        return dependenciesByEntity.getOrDefault(entityId, new ArrayList<>());
    }
    
    /**
     * Get dependents of an entity
     */
    public List<ProjectDependency> getDependentsOf(UUID entityId) {
        return dependentsOfEntity.getOrDefault(entityId, new ArrayList<>());
    }
    
    /**
     * Get tasks for a stage
     */
    public List<ProjectTask> getTasksForStage(UUID stageId) {
        return tasksByStage.getOrDefault(stageId, new ArrayList<>());
    }
    
    /**
     * Get steps for a task
     */
    public List<ProjectStep> getStepsForTask(UUID taskId) {
        return stepsByTask.getOrDefault(taskId, new ArrayList<>());
    }
    
    /**
     * Store calculated dates for an entity
     */
    public void setCalculatedDates(UUID entityId, CalculatedEntityDates dates) {
        calculatedDates.put(entityId, dates);
    }
    
    /**
     * Get calculated dates for an entity
     */
    public CalculatedEntityDates getCalculatedDates(UUID entityId) {
        return calculatedDates.get(entityId);
    }
    
    /**
     * Check if entity has calculated dates
     */
    public boolean hasCalculatedDates(UUID entityId) {
        return calculatedDates.containsKey(entityId);
    }
    
    /**
     * Get all entities of a specific type
     */
    public List<UUID> getEntitiesOfType(DependencyEntityType entityType) {
        switch (entityType) {
            case STAGE:
                return stages.stream().map(ProjectStage::getId).collect(Collectors.toList());
            case TASK:
                return tasks.stream().map(ProjectTask::getId).collect(Collectors.toList());
            case STEP:
                return steps.stream().map(ProjectStep::getId).collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }
    
    /**
     * Get entity type for a given entity ID
     */
    public DependencyEntityType getEntityType(UUID entityId) {
        if (stageMap.containsKey(entityId)) {
            return DependencyEntityType.STAGE;
        } else if (taskMap.containsKey(entityId)) {
            return DependencyEntityType.TASK;
        } else if (stepMap.containsKey(entityId)) {
            return DependencyEntityType.STEP;
        }
        return null;
    }
    
    /**
     * Get project start date
     */
    public LocalDate getProjectStartDate() {
        return project.getPlannedStartDate();
    }
    
    /**
     * Get step map for direct access to steps by ID
     */
    public Map<UUID, ProjectStep> getStepMap() {
        return stepMap;
    }
    
    /**
     * Get task map for direct access to tasks by ID
     */
    public Map<UUID, ProjectTask> getTaskMap() {
        return taskMap;
    }
    
    /**
     * Log context statistics
     */
    public void logStatistics() {
        log.info("Schedule calculation context for project {}: {} stages, {} tasks, {} steps, {} dependencies",
                project.getId(), stages.size(), tasks.size(), steps.size(), dependencies.size());
    }
}