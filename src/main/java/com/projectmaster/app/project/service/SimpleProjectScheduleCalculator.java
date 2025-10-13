package com.projectmaster.app.project.service;

import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyType;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simplified Project Schedule Calculator
 * 
 * Implements a clean, three-layer scheduling algorithm:
 * 1. Steps → Tasks → Stages (bottom-up calculation)
 * 2. Finish-to-Start dependencies only
 * 3. Sequential stage execution based on order index
 * 4. Business day calculation for dependencies
 * 5. Circular dependency detection and handling
 */
@Service("simpleProjectScheduleCalculator")
@RequiredArgsConstructor
@Slf4j
public class SimpleProjectScheduleCalculator {

    private final ProjectStageRepository projectStageRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStepRepository projectStepRepository;
    private final ProjectDependencyRepository projectDependencyRepository;
    private final ProjectBusinessCalendarService businessCalendarService;

    /**
     * Calculate project schedule using simplified algorithm
     */
    @Transactional
    public void calculateProjectSchedule(Project project) {
        log.info("Starting simplified schedule calculation for project: {}", project.getId());
        
        try {
            // Initialize holiday cache for this company to optimize business day calculations
            businessCalendarService.initializeHolidayCache(project.getCompany().getId());
            log.info("Holiday cache initialized for company: {}", project.getCompany().getId());
            
            // Load all project entities
            List<ProjectStage> stages = projectStageRepository.findByProjectIdOrderByOrderIndex(project.getId());
            List<ProjectTask> tasks = projectTaskRepository.findByProjectStagesProjectId(project.getId());
            List<ProjectStep> steps = projectStepRepository.findByProjectTasksProjectStagesProjectId(project.getId());
            List<ProjectDependency> dependencies = projectDependencyRepository.findByProjectId(project.getId());
            
            log.info("Loaded entities for project {}: {} stages, {} tasks, {} steps, {} dependencies", 
                    project.getId(), stages.size(), tasks.size(), steps.size(), dependencies.size());
            
            // Log step durations for debugging
            for (ProjectStep step : steps) {
                if (step.getEstimatedDays() != null && step.getEstimatedDays() > 100) {
                    log.warn("Step {} has large estimated duration: {} days", step.getId(), step.getEstimatedDays());
                }
            }
            
            // Detect and handle circular dependencies
            Set<UUID> circularEntities = detectCircularDependencies(dependencies);
            log.info("Detected {} circular dependencies", circularEntities.size());
            
            // Calculate schedule starting from project start date
            LocalDate projectStartDate = project.getPlannedStartDate();
            if (projectStartDate == null) {
                projectStartDate = LocalDate.now();
                log.warn("Project start date is null, using current date: {}", projectStartDate);
            }
            
            // Step 1: Calculate step schedules
            calculateStepSchedules(steps, tasks, dependencies, circularEntities, projectStartDate);
            
            // Step 2: Calculate task schedules based on their steps
            calculateTaskSchedules(tasks, steps, dependencies, circularEntities, projectStartDate);
            
            // Step 3: Calculate stage schedules based on their tasks
            calculateStageSchedules(stages, tasks, projectStartDate);
            
            // Step 4: Calculate estimated days from actual dates
            calculateEstimatedDays(stages, tasks, steps);
            
            // Save all entities
            projectStageRepository.saveAll(stages);
            projectTaskRepository.saveAll(tasks);
            projectStepRepository.saveAll(steps);
            
            log.info("Successfully calculated schedule for project: {}", project.getId());
            
        } catch (Exception e) {
            log.error("Error calculating project schedule for project: {}", project.getId(), e);
            throw new RuntimeException("Failed to calculate project schedule", e);
        } finally {
            // Clear the holiday cache after calculation to free memory
            businessCalendarService.clearHolidayCache();
        }
    }
    
    /**
     * Detect circular dependencies in the project
     */
    private Set<UUID> detectCircularDependencies(List<ProjectDependency> dependencies) {
        Set<UUID> circularEntities = new HashSet<>();
        
        // Simple circular dependency detection: if A depends on B and B depends on A
        for (ProjectDependency dep1 : dependencies) {
            for (ProjectDependency dep2 : dependencies) {
                if (dep1.getDependentEntityId().equals(dep2.getDependsOnEntityId()) &&
                    dep1.getDependsOnEntityId().equals(dep2.getDependentEntityId()) &&
                    dep1.getDependentEntityType().equals(dep2.getDependsOnEntityType()) &&
                    dep1.getDependsOnEntityType().equals(dep2.getDependentEntityType())) {
                    
                    circularEntities.add(dep1.getDependentEntityId());
                    circularEntities.add(dep1.getDependsOnEntityId());
                    log.warn("Circular dependency detected between {} and {}", 
                            dep1.getDependentEntityId(), dep1.getDependsOnEntityId());
                }
            }
        }
        
        return circularEntities;
    }
    
    /**
     * Calculate schedules for all steps
     */
    private void calculateStepSchedules(List<ProjectStep> steps, List<ProjectTask> tasks, 
                                      List<ProjectDependency> dependencies, Set<UUID> circularEntities,
                                      LocalDate projectStartDate) {
        
        // Group steps by task for easier processing
        Map<UUID, List<ProjectStep>> stepsByTask = steps.stream()
                .collect(Collectors.groupingBy(step -> step.getProjectTask().getId()));
        
        // Process each task's steps
        for (ProjectTask task : tasks) {
            List<ProjectStep> taskSteps = stepsByTask.get(task.getId());
            if (taskSteps == null || taskSteps.isEmpty()) {
                continue;
            }
            
            // Sort steps by creation time
            taskSteps.sort(Comparator.comparing(ProjectStep::getCreatedAt));
            
            // Get step dependencies for this task
            List<ProjectDependency> stepDependencies = dependencies.stream()
                    .filter(dep -> dep.getDependentEntityType() == DependencyEntityType.STEP &&
                                  dep.getDependsOnEntityType() == DependencyEntityType.STEP)
                    .collect(Collectors.toList());
            
            // Calculate step schedules
            calculateStepSchedulesForTask(taskSteps, stepDependencies, circularEntities, projectStartDate);
        }
    }
    
    /**
     * Calculate schedules for steps within a single task
     */
    private void calculateStepSchedulesForTask(List<ProjectStep> steps, 
                                             List<ProjectDependency> stepDependencies,
                                             Set<UUID> circularEntities, LocalDate projectStartDate) {
        
        // Create a map of step dependencies
        Map<UUID, List<ProjectDependency>> stepDepsMap = stepDependencies.stream()
                .collect(Collectors.groupingBy(ProjectDependency::getDependentEntityId));
        
        // Process steps in order
        for (ProjectStep step : steps) {
            if (circularEntities.contains(step.getId())) {
                // Set circular dependency steps to project start date
                step.setPlannedStartDate(projectStartDate);
                step.setPlannedEndDate(projectStartDate);
                log.warn("Step {} has circular dependency, setting dates to project start date", step.getId());
                continue;
            }
            
            // Check if step has dependencies
            List<ProjectDependency> stepDeps = stepDepsMap.get(step.getId());
            if (stepDeps == null || stepDeps.isEmpty()) {
                // No dependencies - can start immediately
                step.setPlannedStartDate(projectStartDate);
            } else {
                // Has dependencies - find the latest end date of dependencies
                LocalDate latestDependencyEndDate = findLatestDependencyEndDate(stepDeps, steps);
                step.setPlannedStartDate(businessCalendarService.getNextBusinessDay(latestDependencyEndDate));
            }
            
            // Calculate end date based on duration
            int duration = step.getEstimatedDays() != null ? step.getEstimatedDays() : 0;
            
            log.debug("Step {}: duration={}, startDate={}", step.getId(), duration, step.getPlannedStartDate());
            
            // Safety check for unreasonable duration values
            if (duration > 365) {
                log.warn("Step {} has unusually large estimated duration: {} days. Limiting to 30 days.", 
                        step.getId(), duration);
                duration = 30;
            }
            
            if (duration == 0) {
                step.setPlannedEndDate(step.getPlannedStartDate());
                log.debug("Step {}: 0 duration, endDate={}", step.getId(), step.getPlannedEndDate());
            } else {
                // For a step that takes N days, we need to add N-1 business days to get the end date
                // Example: If step starts on Day 1 and takes 5 days, it ends on Day 5
                // So we add 4 business days to Day 1 to get Day 5
                int businessDaysToAdd = duration - 1;
                log.debug("Step {}: adding {} business days to {}", step.getId(), businessDaysToAdd, step.getPlannedStartDate());
                
                try {
                    step.setPlannedEndDate(businessCalendarService.addBusinessDays(step.getPlannedStartDate(), businessDaysToAdd));
                    log.debug("Step {}: calculated endDate={}", step.getId(), step.getPlannedEndDate());
                } catch (Exception e) {
                    log.error("Error calculating end date for step {}: duration={}, startDate={}, businessDaysToAdd={}", 
                             step.getId(), duration, step.getPlannedStartDate(), businessDaysToAdd, e);
                    step.setPlannedEndDate(step.getPlannedStartDate());
                }
            }
        }
    }
    
    /**
     * Find the latest end date among step dependencies
     */
    private LocalDate findLatestDependencyEndDate(List<ProjectDependency> dependencies, List<ProjectStep> allSteps) {
        LocalDate latestDate = LocalDate.MIN;
        
        for (ProjectDependency dep : dependencies) {
            // Find the step that this step depends on
            ProjectStep dependsOnStep = allSteps.stream()
                    .filter(step -> step.getId().equals(dep.getDependsOnEntityId()))
                    .findFirst()
                    .orElse(null);
            
            if (dependsOnStep != null && dependsOnStep.getPlannedEndDate() != null) {
                if (dependsOnStep.getPlannedEndDate().isAfter(latestDate)) {
                    latestDate = dependsOnStep.getPlannedEndDate();
                }
            }
        }
        
        return latestDate;
    }
    
    /**
     * Calculate schedules for all tasks based on their steps
     */
    private void calculateTaskSchedules(List<ProjectTask> tasks, List<ProjectStep> steps,
                                      List<ProjectDependency> dependencies, Set<UUID> circularEntities,
                                      LocalDate projectStartDate) {
        
        // Group steps by task
        Map<UUID, List<ProjectStep>> stepsByTask = steps.stream()
                .collect(Collectors.groupingBy(step -> step.getProjectTask().getId()));
        
        // Get task dependencies
        List<ProjectDependency> taskDependencies = dependencies.stream()
                .filter(dep -> dep.getDependentEntityType() == DependencyEntityType.TASK &&
                              dep.getDependsOnEntityType() == DependencyEntityType.TASK)
                .collect(Collectors.toList());
        
        // Create a map of task dependencies
        Map<UUID, List<ProjectDependency>> taskDepsMap = taskDependencies.stream()
                .collect(Collectors.groupingBy(ProjectDependency::getDependentEntityId));
        
        // Process tasks
        for (ProjectTask task : tasks) {
            if (circularEntities.contains(task.getId())) {
                // Set circular dependency tasks to project start date
                task.setPlannedStartDate(projectStartDate);
                task.setPlannedEndDate(projectStartDate);
                log.warn("Task {} has circular dependency, setting dates to project start date", task.getId());
                continue;
            }
            
            List<ProjectStep> taskSteps = stepsByTask.get(task.getId());
            if (taskSteps == null || taskSteps.isEmpty()) {
                // Task has no steps - set dates to project start date
                task.setPlannedStartDate(projectStartDate);
                task.setPlannedEndDate(projectStartDate);
                continue;
            }
            
            // Check if task has dependencies
            List<ProjectDependency> taskDeps = taskDepsMap.get(task.getId());
            if (taskDeps == null || taskDeps.isEmpty()) {
                // No dependencies - start date is earliest step start date
                LocalDate earliestStepStart = taskSteps.stream()
                        .map(ProjectStep::getPlannedStartDate)
                        .filter(Objects::nonNull)
                        .min(LocalDate::compareTo)
                        .orElse(projectStartDate);
                task.setPlannedStartDate(earliestStepStart);
            } else {
                // Has dependencies - find the latest end date of dependencies
                LocalDate latestDependencyEndDate = findLatestTaskDependencyEndDate(taskDeps, tasks);
                task.setPlannedStartDate(businessCalendarService.getNextBusinessDay(latestDependencyEndDate));
            }
            
            // End date is latest step end date
            LocalDate latestStepEnd = taskSteps.stream()
                    .map(ProjectStep::getPlannedEndDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(task.getPlannedStartDate());
            task.setPlannedEndDate(latestStepEnd);
        }
    }
    
    /**
     * Find the latest end date among task dependencies
     */
    private LocalDate findLatestTaskDependencyEndDate(List<ProjectDependency> dependencies, List<ProjectTask> allTasks) {
        LocalDate latestDate = LocalDate.MIN;
        
        for (ProjectDependency dep : dependencies) {
            // Find the task that this task depends on
            ProjectTask dependsOnTask = allTasks.stream()
                    .filter(task -> task.getId().equals(dep.getDependsOnEntityId()))
                    .findFirst()
                    .orElse(null);
            
            if (dependsOnTask != null && dependsOnTask.getPlannedEndDate() != null) {
                if (dependsOnTask.getPlannedEndDate().isAfter(latestDate)) {
                    latestDate = dependsOnTask.getPlannedEndDate();
                }
            }
        }
        
        return latestDate;
    }
    
    /**
     * Calculate schedules for all stages based on their tasks (sequential by order index)
     */
    private void calculateStageSchedules(List<ProjectStage> stages, List<ProjectTask> tasks, LocalDate projectStartDate) {
        
        // Group tasks by stage
        Map<UUID, List<ProjectTask>> tasksByStage = tasks.stream()
                .collect(Collectors.groupingBy(task -> task.getProjectStage().getId()));
        
        // Sort stages by order index
        stages.sort(Comparator.comparing(ProjectStage::getOrderIndex));
        
        LocalDate currentStageStartDate = projectStartDate;
        
        for (ProjectStage stage : stages) {
            List<ProjectTask> stageTasks = tasksByStage.get(stage.getId());
            if (stageTasks == null || stageTasks.isEmpty()) {
                // Stage has no tasks - set dates to current stage start date
                stage.setPlannedStartDate(currentStageStartDate);
                stage.setPlannedEndDate(currentStageStartDate);
                currentStageStartDate = businessCalendarService.getNextBusinessDay(currentStageStartDate);
                continue;
            }
            
            // Stage start date is current stage start date
            stage.setPlannedStartDate(currentStageStartDate);
            
            // Stage end date is latest task end date
            LocalDate latestTaskEnd = stageTasks.stream()
                    .map(ProjectTask::getPlannedEndDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(currentStageStartDate);
            stage.setPlannedEndDate(latestTaskEnd);
            
            // Next stage starts the day after this stage ends
            currentStageStartDate = businessCalendarService.getNextBusinessDay(stage.getPlannedEndDate());
        }
    }
    
    /**
     * Calculate estimated days from actual start/end dates
     */
    private void calculateEstimatedDays(List<ProjectStage> stages, List<ProjectTask> tasks, List<ProjectStep> steps) {
        
        // Calculate estimated days for steps
        for (ProjectStep step : steps) {
            if (step.getPlannedStartDate() != null && step.getPlannedEndDate() != null) {
                int businessDays = businessCalendarService.calculateBusinessDaysBetween(
                        step.getPlannedStartDate(), step.getPlannedEndDate());
                step.setEstimatedDays(businessDays + 1); // +1 to include both start and end days
            }
        }
        
        // Calculate estimated days for tasks
        for (ProjectTask task : tasks) {
            if (task.getPlannedStartDate() != null && task.getPlannedEndDate() != null) {
                int businessDays = businessCalendarService.calculateBusinessDaysBetween(
                        task.getPlannedStartDate(), task.getPlannedEndDate());
                task.setEstimatedDays(businessDays + 1); // +1 to include both start and end days
            }
        }
        
        // Calculate estimated days for stages
        for (ProjectStage stage : stages) {
            if (stage.getPlannedStartDate() != null && stage.getPlannedEndDate() != null) {
                int businessDays = businessCalendarService.calculateBusinessDaysBetween(
                        stage.getPlannedStartDate(), stage.getPlannedEndDate());
                stage.setEstimatedDurationDays(businessDays + 1); // +1 to include both start and end days
            }
        }
    }
}
