package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
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
    private final ProjectStepAssignmentRepository projectStepAssignmentRepository;
    private final UserRepository userRepository;
    
    public WorkflowExecutionContext buildContext(WorkflowExecutionRequest request) {
        log.debug("Building workflow execution context for project: {}", request.getProjectId());
        
        // Load project - either directly or derive from assignment
        Project project;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.getProjectId()));
        } else if (request.getAssignmentId() != null) {
            // Load assignment first to get project context
            ProjectStepAssignment assignment = projectStepAssignmentRepository.findById(request.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Project step assignment not found: " + request.getAssignmentId()));
            project = assignment.getProjectStep().getProjectTask().getProjectStage().getProject();
        } else {
            throw new EntityNotFoundException("Either projectId or assignmentId must be provided");
        }
        
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
        
        // Load assignment if specified
        ProjectStepAssignment projectStepAssignment = null;
        if (request.getAssignmentId() != null) {
            projectStepAssignment = projectStepAssignmentRepository.findById(request.getAssignmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Project step assignment not found: " + request.getAssignmentId()));
        }
        
        // Load step if specified or derive from assignment
        ProjectStep projectStep = null;
        if (request.getStepId() != null) {
            projectStep = projectStepRepository.findById(request.getStepId())
                    .orElseThrow(() -> new EntityNotFoundException("Project step not found: " + request.getStepId()));
        } else if (projectStepAssignment != null) {
            projectStep = projectStepAssignment.getProjectStep();
        }
        
        return WorkflowExecutionContext.builder()
                .project(project)
                .projectStage(projectStage)
                .projectTask(projectTask)
                .projectStep(projectStep)
                .projectStepAssignment(projectStepAssignment)
                .workflowStage(projectStage != null ? projectStage.getWorkflowStage() : null)
                .workflowTask(projectTask != null ? projectTask.getWorkflowTask() : null)
                .workflowStep(projectStep != null ? projectStep.getWorkflowStep() : null)
                .action(request.getAction())
                .user(user)
                .metadata(request.getMetadata())
                .build();
    }
}