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
                projectStage.setPlannedStartDate(previousStageEndDate.plusDays(1));
                projectStage.setPlannedEndDate(previousStageEndDate);
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
                                        projectStep.setPlannedStartDate(stepStartDate);
                                        projectStep.setPlannedEndDate(businessCalendarService.addBusinessDays(stepStartDate, projectStep.getEstimatedDays()));
                                        stepRepository.save(projectStep);
                                        if(taskEndDate.isBefore(projectStep.getPlannedEndDate())){
                                            taskEndDate = projectStep.getPlannedEndDate();
                                        }
                                    }
                                }
                            }
                            projectTask.setPlannedStartDate(taskStartDate);
                            projectTask.setPlannedEndDate(taskEndDate);
                            taskRepository.save(projectTask);
                            taskIdsWithStartDateCalculated.add(projectTask.getId());
                            if(projectStage.getPlannedEndDate().isBefore(taskEndDate)){
                                projectStage.setPlannedEndDate(taskEndDate);
                                previousStageEndDate = taskEndDate;
                            }
                        }
                    }
                }
                projectStageRepository.save(projectStage);
            }
        }
        catch (Exception e) {
            log.error("Error calculating schedule for project {}: {}", project.getId(), e.getMessage(), e);
        // return createErrorResult(project.getId(), "Schedule calculation failed: " + e.getMessage(), List.of());
        }
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
