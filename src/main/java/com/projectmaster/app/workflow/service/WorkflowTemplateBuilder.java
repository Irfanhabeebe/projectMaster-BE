package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import com.projectmaster.app.workflow.entity.WorkflowDependency;
import com.projectmaster.app.workflow.entity.ProjectDependency;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import com.projectmaster.app.workflow.repository.WorkflowDependencyRepository;
import com.projectmaster.app.workflow.repository.ProjectDependencyRepository;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.AdhocTask;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.AdhocTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowTemplateBuilder {
    
    private final WorkflowDependencyRepository workflowDependencyRepository;
    private final ProjectDependencyRepository projectDependencyRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStepRepository projectStepRepository;
    private final AdhocTaskRepository adhocTaskRepository;
    
    /**
     * Add a dependency to a workflow template
     */
    @Transactional
    public void addWorkflowDependency(UUID workflowTemplateId, WorkflowDependencyRequest request) {
        WorkflowDependency dependency = WorkflowDependency.builder()
            .workflowTemplateId(workflowTemplateId)
            .dependentEntityType(request.getDependentEntityType())
            .dependentEntityId(request.getDependentEntityId())
            .dependsOnEntityType(request.getDependsOnEntityType())
            .dependsOnEntityId(request.getDependsOnEntityId())
            .dependencyType(request.getDependencyType())
            .lagDays(request.getLagDays())
            .build();
        
        workflowDependencyRepository.save(dependency);
        
        log.info("Added workflow dependency: {} {} depends on {} {}", 
                request.getDependentEntityType(), request.getDependentEntityId(),
                request.getDependsOnEntityType(), request.getDependsOnEntityId());
    }
    
    /**
     * Copy workflow dependencies to project dependencies when creating a project
     */
    @Transactional
    public void copyDependenciesToProject(UUID projectId, UUID workflowTemplateId) {
        log.info("Copying workflow dependencies from template {} to project {}", 
                workflowTemplateId, projectId);
        
        List<WorkflowDependency> workflowDeps = workflowDependencyRepository
            .findByWorkflowTemplateId(workflowTemplateId);
        
        for (WorkflowDependency wfDep : workflowDeps) {
            try {
                ProjectDependency projectDep = createProjectDependency(projectId, wfDep);
                if (projectDep != null) {
                    projectDependencyRepository.save(projectDep);
                }
            } catch (Exception e) {
                log.warn("Failed to copy dependency {}: {}", wfDep.getId(), e.getMessage());
            }
        }
        
        log.info("Copied {} dependencies to project {}", workflowDeps.size(), projectId);
    }
    
    /**
     * Create a project dependency from a workflow dependency
     */
    private ProjectDependency createProjectDependency(UUID projectId, WorkflowDependency wfDep) {
        // Map workflow entity IDs to project entity IDs
        UUID dependentProjectEntityId = mapToProjectEntity(
            wfDep.getDependentEntityId(), wfDep.getDependentEntityType(), projectId);
        UUID dependsOnProjectEntityId = mapToProjectEntity(
            wfDep.getDependsOnEntityId(), wfDep.getDependsOnEntityType(), projectId);
        
        if (dependentProjectEntityId == null || dependsOnProjectEntityId == null) {
            log.warn("Could not map workflow entities to project entities for dependency {}", wfDep.getId());
            return null;
        }
        
        return ProjectDependency.builder()
            .projectId(projectId)
            .dependentEntityType(wfDep.getDependentEntityType())
            .dependentEntityId(dependentProjectEntityId)
            .dependsOnEntityType(wfDep.getDependsOnEntityType())
            .dependsOnEntityId(dependsOnProjectEntityId)
            .dependencyType(wfDep.getDependencyType())
            .lagDays(wfDep.getLagDays())
            .status(DependencyStatus.PENDING)
            .build();
    }
    
    /**
     * Map workflow entity ID to project entity ID
     */
    private UUID mapToProjectEntity(UUID workflowEntityId, DependencyEntityType entityType, UUID projectId) {
        switch (entityType) {
            case TASK:
                // Find project task by workflow task ID
                List<ProjectTask> projectTasks = projectTaskRepository
                    .findByProjectIdOrderByStageAndTaskOrder(projectId);
                return projectTasks.stream()
                    .filter(pt -> pt.getWorkflowTask().getId().equals(workflowEntityId))
                    .map(ProjectTask::getId)
                    .findFirst()
                    .orElse(null);
                    
            case STEP:
                // Find project step by workflow step ID
                List<ProjectStep> projectSteps = projectStepRepository
                    .findByProjectIdOrderByStageAndTaskAndStepOrder(projectId);
                return projectSteps.stream()
                    .filter(ps -> ps.getWorkflowStep().getId().equals(workflowEntityId))
                    .map(ProjectStep::getId)
                    .findFirst()
                    .orElse(null);
                    
            case ADHOC_TASK:
                // Ad-hoc tasks don't have workflow templates, so this shouldn't happen
                log.warn("Attempted to map ad-hoc task from workflow template");
                return null;
                
            default:
                return null;
        }
    }
    
    /**
     * Add a dependency for an ad-hoc task
     */
    @Transactional
    public void addAdhocTaskDependency(UUID adhocTaskId, UUID projectId, 
                                     DependencyRequest request) {
        
        ProjectDependency dependency = ProjectDependency.builder()
            .projectId(projectId)
            .dependentEntityType(DependencyEntityType.ADHOC_TASK)
            .dependentEntityId(adhocTaskId)
            .dependsOnEntityType(request.getDependsOnEntityType())
            .dependsOnEntityId(request.getDependsOnEntityId())
            .dependencyType(request.getDependencyType())
            .lagDays(request.getLagDays())
            .status(DependencyStatus.PENDING)
            .build();
        
        projectDependencyRepository.save(dependency);
        
        log.info("Added ad-hoc task dependency: task {} depends on {} {}", 
                adhocTaskId, request.getDependsOnEntityType(), request.getDependsOnEntityId());
    }
    
    /**
     * Get all dependencies for a project
     */
    public List<ProjectDependency> getProjectDependencies(UUID projectId) {
        return projectDependencyRepository.findByProjectId(projectId);
    }
    
    /**
     * Get dependencies for a specific entity
     */
    public List<ProjectDependency> getEntityDependencies(UUID entityId, 
                                                       DependencyEntityType entityType, 
                                                       UUID projectId) {
        return projectDependencyRepository
            .findByDependentEntityTypeAndDependentEntityIdAndProjectId(
                entityType, entityId, projectId);
    }
    
    // DTOs
    public static class WorkflowDependencyRequest {
        private DependencyEntityType dependentEntityType;
        private UUID dependentEntityId;
        private DependencyEntityType dependsOnEntityType;
        private UUID dependsOnEntityId;
        private DependencyType dependencyType;
        private Integer lagDays;
        
        // Getters and setters
        public DependencyEntityType getDependentEntityType() { return dependentEntityType; }
        public void setDependentEntityType(DependencyEntityType dependentEntityType) { this.dependentEntityType = dependentEntityType; }
        
        public UUID getDependentEntityId() { return dependentEntityId; }
        public void setDependentEntityId(UUID dependentEntityId) { this.dependentEntityId = dependentEntityId; }
        
        public DependencyEntityType getDependsOnEntityType() { return dependsOnEntityType; }
        public void setDependsOnEntityType(DependencyEntityType dependsOnEntityType) { this.dependsOnEntityType = dependsOnEntityType; }
        
        public UUID getDependsOnEntityId() { return dependsOnEntityId; }
        public void setDependsOnEntityId(UUID dependsOnEntityId) { this.dependsOnEntityId = dependsOnEntityId; }
        
        public DependencyType getDependencyType() { return dependencyType; }
        public void setDependencyType(DependencyType dependencyType) { this.dependencyType = dependencyType; }
        
        public Integer getLagDays() { return lagDays; }
        public void setLagDays(Integer lagDays) { this.lagDays = lagDays; }
    }
    
    public static class DependencyRequest {
        private DependencyEntityType dependsOnEntityType;
        private UUID dependsOnEntityId;
        private DependencyType dependencyType;
        private Integer lagDays;
        
        // Getters and setters
        public DependencyEntityType getDependsOnEntityType() { return dependsOnEntityType; }
        public void setDependsOnEntityType(DependencyEntityType dependsOnEntityType) { this.dependsOnEntityType = dependsOnEntityType; }
        
        public UUID getDependsOnEntityId() { return dependsOnEntityId; }
        public void setDependsOnEntityId(UUID dependsOnEntityId) { this.dependsOnEntityId = dependsOnEntityId; }
        
        public DependencyType getDependencyType() { return dependencyType; }
        public void setDependencyType(DependencyType dependencyType) { this.dependencyType = dependencyType; }
        
        public Integer getLagDays() { return lagDays; }
        public void setLagDays(Integer lagDays) { this.lagDays = lagDays; }
    }
}
