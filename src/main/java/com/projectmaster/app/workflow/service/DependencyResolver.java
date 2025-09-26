package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.AdhocTask;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.AdhocTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DependencyResolver {
    
    private final ProjectDependencyRepository projectDependencyRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStepRepository projectStepRepository;
    private final AdhocTaskRepository adhocTaskRepository;
    
    /**
     * Check if an entity can start based on its dependencies
     */
    public boolean canStart(UUID entityId, DependencyEntityType entityType, UUID projectId) {
        List<ProjectDependency> dependencies = projectDependencyRepository
            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                entityType, entityId, projectId);
        
        if (dependencies.isEmpty()) {
            return true; // No dependencies, can start
        }
        
        return dependencies.stream()
            .allMatch(this::isDependencySatisfied);
    }
    
    /**
     * Get all entities that can start now (dependencies satisfied)
     */
    public List<UUID> getReadyToStartEntities(UUID projectId, DependencyEntityType entityType) {
        return projectDependencyRepository.findReadyToStartEntities(projectId, entityType);
    }
    
    /**
     * Update dependency status when an entity completes
     */
    @Transactional
    public void updateDependenciesOnCompletion(UUID completedEntityId, 
                                             DependencyEntityType entityType, 
                                             UUID projectId) {
        
        log.info("Updating dependencies for completed {} entity {} in project {}", 
                entityType, completedEntityId, projectId);
        
        // Find all dependencies where this entity is the dependency
        List<ProjectDependency> affectedDependencies = projectDependencyRepository
            .findByDependsOnEntityIdAndDependsOnEntityTypeAndProjectId(
                completedEntityId, entityType, projectId);
        
        for (ProjectDependency dependency : affectedDependencies) {
            // Mark dependency as satisfied
            dependency.setStatus(DependencyStatus.SATISFIED);
            dependency.setSatisfiedAt(Instant.now());
            projectDependencyRepository.save(dependency);
            
            log.debug("Marked dependency {} as satisfied", dependency.getId());
            
            // Check if the dependent entity can now start
            if (canDependentEntityStart(dependency)) {
                updateDependentEntityStatus(dependency);
                log.info("Entity {} is now ready to start", dependency.getDependentEntityId());
            }
        }
    }
    
    /**
     * Check if a dependent entity can start (all its dependencies are satisfied)
     */
    private boolean canDependentEntityStart(ProjectDependency dependency) {
        List<ProjectDependency> allDependencies = projectDependencyRepository
            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                dependency.getDependentEntityType(), 
                dependency.getDependentEntityId(), 
                dependency.getProjectId());
        
        return allDependencies.stream()
            .allMatch(dep -> dep.getStatus() == DependencyStatus.SATISFIED);
    }
    
    /**
     * Update the status of dependent entity to IN_PROGRESS
     */
    private void updateDependentEntityStatus(ProjectDependency dependency) {
        switch (dependency.getDependentEntityType()) {
            case TASK:
                updateProjectTaskStatus(dependency.getDependentEntityId());
                break;
            case STEP:
                updateProjectStepStatus(dependency.getDependentEntityId());
                break;
            case ADHOC_TASK:
                updateAdhocTaskStatus(dependency.getDependentEntityId());
                break;
        }
    }
    
    /**
     * Update project task status to IN_PROGRESS
     */
    private void updateProjectTaskStatus(UUID taskId) {
        ProjectTask task = projectTaskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Project task not found: " + taskId));
        
        task.setStatus(com.projectmaster.app.common.enums.StageStatus.IN_PROGRESS);
        if (task.getActualStartDate() == null) {
            task.setActualStartDate(java.time.LocalDate.now());
        }
        projectTaskRepository.save(task);
        
        log.info("Updated project task {} status to IN_PROGRESS", taskId);
    }
    
    /**
     * Update project step status to IN_PROGRESS
     */
    private void updateProjectStepStatus(UUID stepId) {
        ProjectStep step = projectStepRepository.findById(stepId)
            .orElseThrow(() -> new RuntimeException("Project step not found: " + stepId));
        
        step.setStatus(com.projectmaster.app.project.entity.ProjectStep.StepExecutionStatus.IN_PROGRESS);
        if (step.getActualStartDate() == null) {
            step.setActualStartDate(java.time.LocalDate.now());
        }
        projectStepRepository.save(step);
        
        log.info("Updated project step {} status to IN_PROGRESS", stepId);
    }
    
    /**
     * Update ad-hoc task status to IN_PROGRESS
     */
    private void updateAdhocTaskStatus(UUID taskId) {
        AdhocTask task = adhocTaskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Ad-hoc task not found: " + taskId));
        
        task.setStatus(com.projectmaster.app.common.enums.StageStatus.IN_PROGRESS);
        if (task.getActualStartDate() == null) {
            task.setActualStartDate(java.time.LocalDate.now());
        }
        adhocTaskRepository.save(task);
        
        log.info("Updated ad-hoc task {} status to IN_PROGRESS", taskId);
    }
    
    /**
     * Check if a dependency is satisfied
     */
    private boolean isDependencySatisfied(ProjectDependency dependency) {
        return dependency.getStatus() == DependencyStatus.SATISFIED;
    }
    
    /**
     * Get dependency status for an entity
     */
    public DependencyStatus getEntityDependencyStatus(UUID entityId, DependencyEntityType entityType, UUID projectId) {
        List<ProjectDependency> dependencies = projectDependencyRepository
            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                entityType, entityId, projectId);
        
        if (dependencies.isEmpty()) {
            return DependencyStatus.SATISFIED; // No dependencies
        }
        
        boolean allSatisfied = dependencies.stream()
            .allMatch(dep -> dep.getStatus() == DependencyStatus.SATISFIED);
        
        boolean anyBlocked = dependencies.stream()
            .anyMatch(dep -> dep.getStatus() == DependencyStatus.BLOCKED);
        
        if (anyBlocked) {
            return DependencyStatus.BLOCKED;
        } else if (allSatisfied) {
            return DependencyStatus.SATISFIED;
        } else {
            return DependencyStatus.PENDING;
        }
    }
    
    /**
     * Get all dependencies for an entity
     */
    public List<ProjectDependency> getEntityDependencies(UUID entityId, DependencyEntityType entityType, UUID projectId) {
        return projectDependencyRepository
            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                entityType, entityId, projectId);
    }
}
