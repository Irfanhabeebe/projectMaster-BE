package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.ProjectTaskRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.dto.WorkflowExecutionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkflowContextBuilder {
    
    private final ProjectRepository projectRepository;
    private final ProjectStageRepository projectStageRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final ProjectStepRepository projectStepRepository;
    private final UserRepository userRepository;
    
    public WorkflowExecutionContext buildContext(WorkflowExecutionRequest request) {
        log.debug("Building workflow execution context for project: {}", request.getProjectId());
        
        // Load project
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.getProjectId()));
        
        // Load user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.getUserId()));
        
        // Load stage if specified
        ProjectStage projectStage = null;
        if (request.getStageId() != null) {
            projectStage = projectStageRepository.findById(request.getStageId())
                    .orElseThrow(() -> new EntityNotFoundException("Project stage not found: " + request.getStageId()));
        }
        
        // Load task if specified
        ProjectTask projectTask = null;
        if (request.getTaskId() != null) {
            projectTask = projectTaskRepository.findById(request.getTaskId())
                    .orElseThrow(() -> new EntityNotFoundException("Project task not found: " + request.getTaskId()));
        }
        
        // Load step if specified
        ProjectStep projectStep = null;
        if (request.getStepId() != null) {
            projectStep = projectStepRepository.findById(request.getStepId())
                    .orElseThrow(() -> new EntityNotFoundException("Project step not found: " + request.getStepId()));
        }
        
        return WorkflowExecutionContext.builder()
                .project(project)
                .projectStage(projectStage)
                .projectTask(projectTask)
                .projectStep(projectStep)
                .workflowStage(projectStage != null ? projectStage.getWorkflowStage() : null)
                .workflowTask(projectTask != null ? projectTask.getWorkflowTask() : null)
                .workflowStep(projectStep != null ? projectStep.getWorkflowStep() : null)
                .action(request.getAction())
                .user(user)
                .metadata(request.getMetadata())
                .build();
    }
}