package com.projectmaster.app.project.service;

import com.projectmaster.app.project.dto.*;
import com.projectmaster.app.project.entity.*;
import com.projectmaster.app.project.repository.*;
import com.projectmaster.app.workflow.dto.ScheduleCalculationResult;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import com.projectmaster.app.workflow.repository.WorkflowStageRepository;
import com.projectmaster.app.workflow.repository.WorkflowTaskRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core schedule calculator service for project scheduling
 * Implements Phase 2 of the Project Scheduling Implementation Strategy
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectScheduleCalculator {

    private final ProjectBusinessCalendarService businessCalendarService;
    private final ProjectDependencyRepository dependencyRepository;
    private final ProjectStageRepository stageRepository;
    private final ProjectTaskRepository taskRepository;
    private final ProjectStepRepository stepRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAdvancedDependencyResolver dependencyResolver;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final ProjectStageRepository projectStageRepository;
    /**
     * Calculate the complete project schedule
     */
    public void calculateProjectSchedule(Project project, List<ProjectStage> projectStages, List<ProjectDependency> projectDependencies) {
        log.info("Calculating schedule for project {}", project.getId());

        try {
            // Initialize holiday cache for this company to optimize business day calculations
            businessCalendarService.initializeHolidayCache(project.getCompany().getId());
            log.info("Holiday cache initialized for company: {}", project.getCompany().getId());
            
            LocalDate projectStartDate = project.getPlannedStartDate();
            if (projectStartDate == null) {
                projectStartDate = LocalDate.now();
                project.setPlannedStartDate(projectStartDate);
                log.warn("Project start date was null, set to current date: {}", projectStartDate);  
            // Phase 1: Assign order indices based on dependencies
           // assignOrderIndices(project, projectStages, projectDependencies);
            }
            Map<UUID,Set<UUID>> dependenciesByEntityType = getDependenciesByEntityType(projectDependencies, projectStages);
            boolean firstStageFlag = true;
            LocalDate previousStageEndDate = projectStartDate.plusDays(-1);
            //Create new ProjectStageObject in order of order Index
            List<ProjectStage> projectStagesInOrder = projectStages.stream().sorted(Comparator.comparingInt(ProjectStage::getOrderIndex)).toList();
            for (ProjectStage projectStage : projectStagesInOrder) {
                // Only update stage dates if not completed
                boolean stageIsCompleted = projectStage.getStatus() == com.projectmaster.app.common.enums.StageStatus.COMPLETED;
                
                if (!stageIsCompleted) {
                    projectStage.setPlannedStartDate(previousStageEndDate.plusDays(1));
                    projectStage.setPlannedEndDate(previousStageEndDate);
                } else {
                    log.debug("Skipped updating dates for completed stage: {}", projectStage.getName());
                }
                List<ProjectTask> nextExecutionTasks = new ArrayList<>();
                Set<UUID> taskIdsWithStartDateCalculated = new HashSet<>();
                while (taskIdsWithStartDateCalculated.size() < projectStage.getTasks().size()){
                    for (ProjectTask projectTask : projectStage.getTasks()) {
                        LocalDate taskStartDate = projectStage.getPlannedStartDate();
                        LocalDate taskEndDate = taskStartDate;
                        boolean taskReadyToStart = true;
                        if(taskIdsWithStartDateCalculated.contains(projectTask.getId())){
                            continue;
                        }
                        Set<UUID> taskDependencies = dependenciesByEntityType.get(projectTask.getId());
                        if (taskDependencies != null){
                            for (UUID dependsOnEntityId : dependenciesByEntityType.get(projectTask.getId())) {
                                if(taskIdsWithStartDateCalculated == null){
                                    taskReadyToStart = false;
                                    break;
                                }
                                if(taskIdsWithStartDateCalculated.contains(dependsOnEntityId)){
                                    for (ProjectTask projectTask1 : projectStage.getTasks()){
                                        if(projectTask1.getId().equals(dependsOnEntityId)){
                                            if (projectTask1.getPlannedEndDate() != null && projectTask1.getPlannedEndDate().isAfter(taskStartDate)){
                                                taskStartDate = businessCalendarService.addBusinessDays(projectTask1.getPlannedEndDate(), 1);
                                            }
                                            break;
                                        }
                                    }
                                }
                                else{
                                    taskReadyToStart = false;
                                    break;
                                }
                            }
                        }
                        if(taskReadyToStart){
                            // Only update task dates if not completed
                            boolean taskIsCompleted = projectTask.getStatus() == com.projectmaster.app.common.enums.StageStatus.COMPLETED;
                            
                            Set<UUID> stepIdsWithStartDateCalculated = new HashSet<>();
                            while (stepIdsWithStartDateCalculated.size() < projectTask.getSteps().size()){
                                for (ProjectStep projectStep : projectTask.getSteps()) {
                                    if(stepIdsWithStartDateCalculated.contains(projectStep.getId())){
                                        continue;
                                    }
                                    boolean stepReadyToStart = true;
                                    LocalDate stepStartDate = taskStartDate;
                                    Set<UUID> stepDependencies = dependenciesByEntityType.get(projectStep.getId());
                                                    //Set<UUID> stepDependencies = dependenciesByEntityType.get(projectStep.getId());
                                    if(stepDependencies != null){
                                        for (UUID dependsOnStepEntityId : stepDependencies) {
                                            if (stepIdsWithStartDateCalculated == null){
                                                stepReadyToStart = false;
                                                break;
                                            }
                                            if(stepIdsWithStartDateCalculated.contains(dependsOnStepEntityId)){
                                                for (ProjectStep projectStep1 : projectTask.getSteps()){
                                                    if(projectStep1.getId().equals(dependsOnStepEntityId)){
                                                        if(projectStep1.getPlannedEndDate() != null && projectStep1.getPlannedEndDate().isAfter(stepStartDate)){
                                                            stepStartDate = businessCalendarService.addBusinessDays(projectStep1.getPlannedEndDate(), 1);
                                                        }
                                                        break;
                                                    }
                                                }
                                            }else{
                                                stepReadyToStart = false;
                                                break;
                                            }
                                        }
                                    }
                                    if (stepReadyToStart){
                                        stepIdsWithStartDateCalculated.add(projectStep.getId());
                                        
                                        // Only update dates if step is not completed
                                        if (projectStep.getStatus() != ProjectStep.StepExecutionStatus.COMPLETED) {
                                            projectStep.setPlannedStartDate(stepStartDate);
                                            projectStep.setPlannedEndDate(businessCalendarService.addBusinessDays(stepStartDate, projectStep.getEstimatedDays()));
                                            stepRepository.save(projectStep);
                                            log.debug("Updated dates for step: {} (status: {})", projectStep.getName(), projectStep.getStatus());
                                        } else {
                                            log.debug("Skipped updating dates for completed step: {}", projectStep.getName());
                                        }
                                        
                                        // Use actual end date for task aggregation (whether updated or existing)
                                        LocalDate stepEndDate = projectStep.getPlannedEndDate();
                                        if(stepEndDate != null && taskEndDate.isBefore(stepEndDate)){
                                            taskEndDate = stepEndDate;
                                        }
                                    }
                                }
                            }
                            
                            // Only update task dates if not completed
                            if (!taskIsCompleted) {
                                projectTask.setPlannedStartDate(taskStartDate);
                                projectTask.setPlannedEndDate(taskEndDate);
                                taskRepository.save(projectTask);
                                log.debug("Updated dates for task: {} (status: {})", projectTask.getName(), projectTask.getStatus());
                            } else {
                                log.debug("Skipped updating dates for completed task: {}", projectTask.getName());
                            }
                            
                            taskIdsWithStartDateCalculated.add(projectTask.getId());
                            
                            // Use actual end date for stage aggregation (whether updated or existing)
                            LocalDate actualTaskEndDate = projectTask.getPlannedEndDate();
                            if(actualTaskEndDate != null && projectStage.getPlannedEndDate().isBefore(actualTaskEndDate)){
                                projectStage.setPlannedEndDate(actualTaskEndDate);
                                previousStageEndDate = actualTaskEndDate;
                            }
                        }
                    }
                }
                
                // Only save if stage was modified (not completed)
                if (!stageIsCompleted) {
                    projectStageRepository.save(projectStage);
                    log.debug("Saved updated stage: {} (status: {})", projectStage.getName(), projectStage.getStatus());
                } else {
                    // For completed stages, update previousStageEndDate so next stage knows where to start
                    if (projectStage.getPlannedEndDate() != null) {
                        previousStageEndDate = projectStage.getPlannedEndDate();
                    }
                }
            }
        }
        catch (Exception e) {
            log.error("Error calculating schedule for project {}: {}", project.getId(), e.getMessage(), e);
        // return createErrorResult(project.getId(), "Schedule calculation failed: " + e.getMessage(), List.of());
        } finally {
            // Clear the holiday cache after calculation to free memory
            businessCalendarService.clearHolidayCache();
        }
    }
    /**
     * Recalculate project schedule - only recalculates non-completed items
     * This method is used when workflow rebuild is required
     */
    @Transactional
    public void recalculateProjectSchedule(UUID projectId) {
        log.info("Recalculating schedule for project: {}", projectId);
        
        // Load project
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));
        
        // Load all project entities (including completed ones for dependency chain)
        List<ProjectStage> projectStages = stageRepository.findByProjectIdOrderByOrderIndex(projectId);
        List<ProjectDependency> projectDependencies = dependencyRepository.findByProjectId(projectId);
        
        // Recalculate using the main calculation logic
        // The calculation will check status internally and skip updating completed items
        calculateProjectSchedule(project, projectStages, projectDependencies);
        
        // Clear the rebuild flag
        project.setWorkflowRebuildRequired(false);
        projectRepository.save(project);
        
        log.info("Schedule recalculated successfully for project: {}", projectId);
    }
    
    public Map<UUID,Set<UUID>> getDependenciesByEntityType(List<ProjectDependency> projectDependencies, List<ProjectStage> projectStages) {
        Map<UUID,Set<UUID>> dependenciesMapping = new HashMap<>();
        for (ProjectStage projectStage : projectStages) {
            for (ProjectTask projectTask : projectStage.getTasks()) {
                Set<UUID> dependsOnEntities = new HashSet<>();
                for (ProjectDependency projectDependency : projectDependencies) {
                    if(projectTask.getId().equals(projectDependency.getDependentEntityId())){
                        dependsOnEntities.add(projectDependency.getDependsOnEntityId());
                    }
                }
                if(!dependsOnEntities.isEmpty()){
                    dependenciesMapping.put(projectTask.getId(), dependsOnEntities);
                }
                for (ProjectStep projectStep : projectTask.getSteps()) {
                    Set<UUID> dependsOnStepEntities = new HashSet<>();
                    for (ProjectDependency projectDependency1 : projectDependencies) {
                        if(projectStep.getId().equals(projectDependency1.getDependentEntityId())){
                            dependsOnStepEntities.add(projectDependency1.getDependsOnEntityId());
                        }
                    }
                    if(!dependsOnStepEntities.isEmpty()){
                        dependenciesMapping.put(projectStep.getId(), dependsOnStepEntities);
                    }
                }
            }
        }
        return dependenciesMapping;
    }
}
