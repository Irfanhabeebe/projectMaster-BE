package com.projectmaster.app.project.service;

import com.projectmaster.app.project.entity.AdhocTask;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.repository.AdhocTaskRepository;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.workflow.service.WorkflowTemplateBuilder;
import com.projectmaster.app.workflow.service.DependencyResolver;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdhocTaskService {
    
    private final AdhocTaskRepository adhocTaskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectStageRepository projectStageRepository;
    private final WorkflowTemplateBuilder workflowTemplateBuilder;
    private final DependencyResolver dependencyResolver;
    
    /**
     * Create a new ad-hoc task
     */
    @Transactional
    public AdhocTask createAdhocTask(CreateAdhocTaskRequest request, User createdBy) {
        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new RuntimeException("Project not found"));
        
        ProjectStage projectStage = null;
        if (request.getProjectStageId() != null) {
            projectStage = projectStageRepository.findById(request.getProjectStageId())
                .orElseThrow(() -> new RuntimeException("Project stage not found"));
        }
        
        AdhocTask adhocTask = AdhocTask.builder()
            .project(project)
            .projectStage(projectStage)
            .createdBy(createdBy)
            .title(request.getTitle())
            .description(request.getDescription())
            .priority(request.getPriority())
            .dueDate(request.getDueDate())
            .estimatedDays(request.getEstimatedDays())
            .notes(request.getNotes())
            .build();
        
        AdhocTask savedTask = adhocTaskRepository.save(adhocTask);
        
        // Create dependencies if specified
        if (request.getDependencies() != null && !request.getDependencies().isEmpty()) {
            createDependencies(savedTask, request.getDependencies());
        }
        
        log.info("Created ad-hoc task {} for project {}", savedTask.getId(), project.getId());
        return savedTask;
    }
    
    /**
     * Create dependencies for an ad-hoc task using the unified system
     */
    @Transactional
    public void createDependencies(AdhocTask adhocTask, List<DependencyRequest> dependencies) {
        for (DependencyRequest depRequest : dependencies) {
            // Convert to the correct DTO type
            WorkflowTemplateBuilder.DependencyRequest wfDepRequest = new WorkflowTemplateBuilder.DependencyRequest();
            wfDepRequest.setDependsOnEntityType(depRequest.getDependsOnEntityType());
            wfDepRequest.setDependsOnEntityId(depRequest.getDependsOnEntityId());
            wfDepRequest.setDependencyType(depRequest.getDependencyType());
            wfDepRequest.setLagDays(depRequest.getLagDays());
            
            workflowTemplateBuilder.addAdhocTaskDependency(
                adhocTask.getId(), 
                adhocTask.getProject().getId(), 
                wfDepRequest
            );
        }
        
        log.info("Created {} dependencies for ad-hoc task {}", dependencies.size(), adhocTask.getId());
    }
    
    /**
     * Complete an ad-hoc task
     */
    @Transactional
    public void completeAdhocTask(UUID taskId, User completedBy, String notes) {
        AdhocTask task = adhocTaskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Ad-hoc task not found"));
        
        task.setStatus(com.projectmaster.app.common.enums.StageStatus.COMPLETED);
        task.setActualEndDate(LocalDate.now());
        if (task.getActualStartDate() == null) {
            task.setActualStartDate(LocalDate.now());
        }
        if (notes != null) {
            task.setNotes(notes);
        }
        
        adhocTaskRepository.save(task);
        
        // Trigger dependency notifications through the unified system
        dependencyResolver.updateDependenciesOnCompletion(
            taskId, 
            DependencyEntityType.ADHOC_TASK, 
            task.getProject().getId()
        );
        
        log.info("Completed ad-hoc task {} by user {}", taskId, completedBy.getId());
    }
    
    /**
     * Get all ad-hoc tasks for a project
     */
    public List<AdhocTask> getAdhocTasksByProject(UUID projectId) {
        return adhocTaskRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }
    
    /**
     * Get ad-hoc tasks ready to start (dependencies satisfied)
     */
    public List<AdhocTask> getReadyToStartAdhocTasks(UUID projectId) {
        List<AdhocTask> allTasks = adhocTaskRepository.findByProjectIdAndStatus(
            projectId, com.projectmaster.app.common.enums.StageStatus.NOT_STARTED);
        
        return allTasks.stream()
            .filter(task -> dependencyResolver.canStart(task.getId(), DependencyEntityType.ADHOC_TASK, projectId))
            .toList();
    }
    
    /**
     * Check if an ad-hoc task can start
     */
    public boolean canTaskStart(UUID taskId, UUID projectId) {
        return dependencyResolver.canStart(taskId, DependencyEntityType.ADHOC_TASK, projectId);
    }
    
    /**
     * Get dependency status for an ad-hoc task
     */
    public com.projectmaster.app.workflow.entity.DependencyStatus getTaskDependencyStatus(UUID taskId, UUID projectId) {
        return dependencyResolver.getEntityDependencyStatus(taskId, DependencyEntityType.ADHOC_TASK, projectId);
    }
    
    // DTOs
    public static class CreateAdhocTaskRequest {
        private UUID projectId;
        private UUID projectStageId; // Optional
        private String title;
        private String description;
        private AdhocTask.TaskPriority priority;
        private LocalDate dueDate;
        private Integer estimatedDays;
        private String notes;
        private List<DependencyRequest> dependencies;
        
        // Getters and setters
        public UUID getProjectId() { return projectId; }
        public void setProjectId(UUID projectId) { this.projectId = projectId; }
        
        public UUID getProjectStageId() { return projectStageId; }
        public void setProjectStageId(UUID projectStageId) { this.projectStageId = projectStageId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public AdhocTask.TaskPriority getPriority() { return priority; }
        public void setPriority(AdhocTask.TaskPriority priority) { this.priority = priority; }
        
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        
        public Integer getEstimatedDays() { return estimatedDays; }
        public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        
        public List<DependencyRequest> getDependencies() { return dependencies; }
        public void setDependencies(List<DependencyRequest> dependencies) { this.dependencies = dependencies; }
    }
    
    public static class DependencyRequest {
        private DependencyEntityType dependsOnEntityType;
        private UUID dependsOnEntityId;
        private com.projectmaster.app.workflow.entity.DependencyType dependencyType;
        private Integer lagDays;
        
        // Getters and setters
        public DependencyEntityType getDependsOnEntityType() { return dependsOnEntityType; }
        public void setDependsOnEntityType(DependencyEntityType dependsOnEntityType) { this.dependsOnEntityType = dependsOnEntityType; }
        
        public UUID getDependsOnEntityId() { return dependsOnEntityId; }
        public void setDependsOnEntityId(UUID dependsOnEntityId) { this.dependsOnEntityId = dependsOnEntityId; }
        
        public com.projectmaster.app.workflow.entity.DependencyType getDependencyType() { return dependencyType; }
        public void setDependencyType(com.projectmaster.app.workflow.entity.DependencyType dependencyType) { this.dependencyType = dependencyType; }
        
        public Integer getLagDays() { return lagDays; }
        public void setLagDays(Integer lagDays) { this.lagDays = lagDays; }
    }
}