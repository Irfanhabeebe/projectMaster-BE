package com.projectmaster.app.workflow.service;

import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.project.entity.ProjectStepAssignment;
import com.projectmaster.app.project.repository.ProjectStepAssignmentRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import com.projectmaster.app.workflow.dto.CompleteStepRequest;
import com.projectmaster.app.workflow.dto.WorkflowExecutionRequest;
import com.projectmaster.app.workflow.dto.WorkflowExecutionResult;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDto;
import com.projectmaster.app.workflow.dto.WorkflowTemplateDetailResponse;
import com.projectmaster.app.workflow.entity.WorkflowTemplate;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import com.projectmaster.app.workflow.entity.WorkflowTask;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.entity.WorkflowDependency;
import com.projectmaster.app.workflow.entity.WorkflowStepRequirement;
import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.repository.WorkflowTemplateRepository;
import com.projectmaster.app.workflow.repository.WorkflowStageRepository;
import com.projectmaster.app.workflow.repository.WorkflowTaskRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRepository;
import com.projectmaster.app.workflow.repository.WorkflowDependencyRepository;
import com.projectmaster.app.workflow.repository.WorkflowStepRequirementRepository;
import com.projectmaster.app.workflow.engine.WorkflowEngine;
import com.projectmaster.app.common.enums.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class WorkflowService {
    
    private final WorkflowEngine workflowEngine;
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final WorkflowStageRepository workflowStageRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowDependencyRepository workflowDependencyRepository;
    private final WorkflowStepRequirementRepository workflowStepRequirementRepository;
    private final ProjectStepRepository projectStepRepository;
    private final ProjectStepAssignmentRepository projectStepAssignmentRepository;
    private final UserRepository userRepository;
    
    /**
     * Execute a workflow action
     */
    public WorkflowExecutionResult executeWorkflow(WorkflowExecutionRequest request) {
        log.info("Executing workflow action: {} for project: {}", 
                request.getAction().getType(), request.getProjectId());
        
        return workflowEngine.executeWorkflow(request);
    }
    
    /**
     * Check if a workflow transition can be executed
     */
    public boolean canExecuteTransition(WorkflowExecutionRequest request) {
        return workflowEngine.canExecuteTransition(request);
    }
    
    /**
     * Get available transitions for a project
     */
    public List<AvailableTransition> getAvailableTransitions(UUID projectId) {
        // TODO: Implement logic to determine available transitions based on current project state
        log.debug("Getting available transitions for project: {}", projectId);
        return List.of(); // Placeholder
    }
    
    /**
     * Start a project stage
     */
    public WorkflowExecutionResult startStage(UUID projectId, UUID stageId, UUID userId) {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stageId(stageId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.START_STAGE)
                        .targetId(stageId)
                        .build())
                .build();
        
        return executeWorkflow(request);
    }
    
    /**
     * Complete a project stage
     */
    public WorkflowExecutionResult completeStage(UUID projectId, UUID stageId, UUID userId) {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stageId(stageId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.COMPLETE_STAGE)
                        .targetId(stageId)
                        .build())
                .build();
        
        return executeWorkflow(request);
    }
    
    /**
     * Start a project step
     */
    public WorkflowExecutionResult startStep(UUID stepId, UUID userId) {
        // Validate authorization - user must be assigned to the step or be a project manager
        validateStepStartAuthorization(stepId, userId);
        
        // Get the project ID from the step
        var step = projectStepRepository.findById(stepId)
                .orElseThrow(() -> new ProjectMasterException("Step not found: " + stepId));
        UUID projectId = step.getProjectTask().getProjectStage().getProject().getId();
        
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stepId(stepId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.START_STEP)
                        .targetId(stepId)
                        .build())
                .build();
        
        return executeWorkflow(request);
    }
    
    /**
     * Validate that the user is authorized to start the step
     * - User must be assigned to the step, OR
     * - User must be a PROJECT_MANAGER or ADMIN in the same company as the project
     */
    private void validateStepStartAuthorization(UUID stepId, UUID userId) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectMasterException("User not found: " + userId));
        
        // Get step and project
        var step = projectStepRepository.findById(stepId)
                .orElseThrow(() -> new ProjectMasterException("Step not found: " + stepId));
        
        var project = step.getProjectTask().getProjectStage().getProject();
        
        // Check if user is a project manager or admin
        if (user.getRole() == UserRole.PROJECT_MANAGER || user.getRole() == UserRole.SUPER_USER) {
            // Project managers and super users can start any step in their company's projects
            if (user.getCompany() != null && user.getCompany().getId().equals(project.getCompany().getId())) {
                log.debug("User {} is authorized to start step {} as project manager", userId, stepId);
                return;
            }
        }
        
        // Check if user is assigned to this step
        List<ProjectStepAssignment> assignments = projectStepAssignmentRepository.findByProjectStepId(stepId);
        boolean isAssigned = assignments.stream()
                .anyMatch(assignment -> 
                    (assignment.getCrew() != null && assignment.getCrew().getUser().getId().equals(userId)) ||
                    (assignment.getContractingCompany() != null && assignment.getContractingCompany().getUsers().stream()
                        .anyMatch(companyUser -> companyUser.getId().equals(userId)))
                );
        
        if (isAssigned) {
            log.debug("User {} is authorized to start step {} as assigned user", userId, stepId);
            return;
        }
        
        throw new ProjectMasterException("User is not authorized to start this step. User must be assigned to the step or be a project manager.");
    }
    
    /**
     * Complete a project step
     */
    public WorkflowExecutionResult completeStep(UUID projectId, UUID stepId, UUID userId, CompleteStepRequest request) {
        WorkflowExecutionRequest workflowRequest = WorkflowExecutionRequest.builder()
                .projectId(projectId)
                .stepId(stepId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.COMPLETE_STEP)
                        .targetId(stepId)
                        .build())
                .completionData(Map.of(
                    "completionDate", request.getCompletionDate(),
                    "completionNotes", request.getCompletionNotes() != null ? request.getCompletionNotes() : "",
                    "qualityCheckPassed", request.getQualityCheckPassed() != null ? request.getQualityCheckPassed() : true,
                    "completionPercentage", request.getCompletionPercentage() != null ? request.getCompletionPercentage() : 100
                ))
                .build();
        
        return executeWorkflow(workflowRequest);
    }
    
    /**
     * Accept a project step assignment
     */
    public WorkflowExecutionResult acceptAssignment(UUID assignmentId, UUID userId) {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .assignmentId(assignmentId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.ACCEPT_ASSIGNMENT)
                        .targetId(assignmentId)
                        .build())
                .build();
        
        return executeWorkflow(request);
    }
    
    /**
     * Decline a project step assignment
     */
    public WorkflowExecutionResult declineAssignment(UUID assignmentId, UUID userId) {
        WorkflowExecutionRequest request = WorkflowExecutionRequest.builder()
                .assignmentId(assignmentId)
                .userId(userId)
                .action(com.projectmaster.app.workflow.dto.WorkflowAction.builder()
                        .type(com.projectmaster.app.workflow.enums.WorkflowActionType.DECLINE_ASSIGNMENT)
                        .targetId(assignmentId)
                        .build())
                .build();
        
        return executeWorkflow(request);
    }
    

    
    /**
     * Get workflow templates by company ID
     */
    @Transactional(readOnly = true)
    public List<WorkflowTemplateDto> getWorkflowTemplatesByCompany(UUID companyId) {
        log.debug("Getting workflow templates for company: {}", companyId);
        
        List<WorkflowTemplate> templates = workflowTemplateRepository.findByCompanyIdAndActiveTrue(companyId);
        
        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get detailed workflow template with complete structure
     */
    @Transactional(readOnly = true)
    public WorkflowTemplateDetailResponse getWorkflowTemplateDetail(UUID templateId) {
        log.debug("Getting detailed workflow template: {}", templateId);
        
        WorkflowTemplate template = workflowTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Workflow template not found: " + templateId));
        
        return convertToDetailResponse(template);
    }
    
    /**
     * Convert WorkflowTemplate entity to detailed DTO
     */
    private WorkflowTemplateDetailResponse convertToDetailResponse(WorkflowTemplate template) {
        List<WorkflowTemplateDetailResponse.WorkflowStageDetailResponse> stageResponses = 
                template.getStages().stream()
                        .map(this::convertToStageDetailResponse)
                        .toList();
        
        return WorkflowTemplateDetailResponse.builder()
                .id(template.getId())
                .companyId(template.getCompany().getId())
                .companyName(template.getCompany().getName())
                .name(template.getName())
                .description(template.getDescription())
                .category(template.getCategory())
                .active(template.getActive())
                .isDefault(template.getIsDefault())
                .version(template.getVersion())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .stages(stageResponses)
                .build();
    }
    
    /**
     * Convert WorkflowStage entity to detailed DTO
     */
    private WorkflowTemplateDetailResponse.WorkflowStageDetailResponse convertToStageDetailResponse(WorkflowStage stage) {
        List<WorkflowTemplateDetailResponse.WorkflowTaskDetailResponse> taskResponses = 
                stage.getTasks().stream()
                        .map(this::convertToTaskDetailResponse)
                        .toList();
        
        return WorkflowTemplateDetailResponse.WorkflowStageDetailResponse.builder()
                .id(stage.getId())
                .name(stage.getName())
                .description(stage.getDescription())
                .orderIndex(stage.getOrderIndex())
                .parallelExecution(stage.getParallelExecution())
                .requiredApprovals(stage.getRequiredApprovals())
                .estimatedDurationDays(stage.getEstimatedDurationDays())
                .version(stage.getVersion())
                .createdAt(stage.getCreatedAt())
                .updatedAt(stage.getUpdatedAt())
                .tasks(taskResponses)
                .build();
    }
    
    /**
     * Convert WorkflowTask entity to detailed DTO
     */
    private WorkflowTemplateDetailResponse.WorkflowTaskDetailResponse convertToTaskDetailResponse(WorkflowTask task) {
        List<WorkflowTemplateDetailResponse.WorkflowStepDetailResponse> stepResponses = 
                task.getSteps().stream()
                        .map(this::convertToStepDetailResponse)
                        .toList();
        
        // Get task dependencies
        List<WorkflowDependency> taskDependencies = workflowDependencyRepository
                .findByDependentEntityTypeAndDependentEntityId(DependencyEntityType.TASK, task.getId());
        
        List<WorkflowTemplateDetailResponse.WorkflowDependencyDetailResponse> dependencyResponses = 
                taskDependencies.stream()
                        .map(this::convertToDependencyDetailResponse)
                        .toList();
        
        return WorkflowTemplateDetailResponse.WorkflowTaskDetailResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .estimatedDays(task.getEstimatedDays())
                .version(task.getVersion())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .steps(stepResponses)
                .dependencies(dependencyResponses)
                .build();
    }
    
    /**
     * Convert WorkflowStep entity to detailed DTO
     */
    private WorkflowTemplateDetailResponse.WorkflowStepDetailResponse convertToStepDetailResponse(WorkflowStep step) {
        // Get step requirements
        List<WorkflowStepRequirement> stepRequirements = workflowStepRequirementRepository
                .findByWorkflowStepIdOrderByDisplayOrder(step.getId());
        
        List<WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse> requirementResponses = 
                stepRequirements.stream()
                        .map(this::convertToStepRequirementDetailResponse)
                        .toList();
        
        // Get step dependencies
        List<WorkflowDependency> dependencies = workflowDependencyRepository
                .findByDependentEntityTypeAndDependentEntityId(DependencyEntityType.STEP, step.getId());
        
        List<WorkflowTemplateDetailResponse.WorkflowDependencyDetailResponse> dependencyResponses = 
                dependencies.stream()
                        .map(this::convertToDependencyDetailResponse)
                        .toList();
        
        return WorkflowTemplateDetailResponse.WorkflowStepDetailResponse.builder()
                .id(step.getId())
                .name(step.getName())
                .description(step.getDescription())
                .estimatedDays(step.getEstimatedDays())
                .specialtyId(step.getSpecialty() != null ? step.getSpecialty().getId() : null)
                .specialtyName(step.getSpecialty() != null ? step.getSpecialty().getSpecialtyName() : null)
                .specialtyType(step.getSpecialty() != null ? step.getSpecialty().getSpecialtyType() : null)
                .version(step.getVersion())
                .createdAt(step.getCreatedAt())
                .updatedAt(step.getUpdatedAt())
                .stepRequirements(requirementResponses)
                .dependencies(dependencyResponses)
                .build();
    }
    
    /**
     * Convert WorkflowStepRequirement entity to detailed DTO
     */
    private WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse convertToStepRequirementDetailResponse(WorkflowStepRequirement requirement) {
        return WorkflowTemplateDetailResponse.WorkflowStepRequirementDetailResponse.builder()
                .id(requirement.getId())
                .itemName(requirement.getItemName())
                .itemDescription(requirement.getItemDescription())
                .displayOrder(requirement.getDisplayOrder())
                .categoryId(requirement.getCategory() != null ? requirement.getCategory().getId() : null)
                .categoryName(requirement.getCategory() != null ? requirement.getCategory().getName() : null)
                .categoryGroup(requirement.getCategory() != null ? requirement.getCategory().getCategoryGroup() : null)
                .supplierId(requirement.getSupplier() != null ? requirement.getSupplier().getId() : null)
                .supplierName(requirement.getSupplier() != null ? requirement.getSupplier().getName() : null)
                .brand(requirement.getBrand())
                .model(requirement.getModel())
                .defaultQuantity(requirement.getDefaultQuantity() != null ? requirement.getDefaultQuantity().toString() : null)
                .unit(requirement.getUnit())
                .estimatedCost(requirement.getEstimatedCost() != null ? requirement.getEstimatedCost().toString() : null)
                .procurementType(requirement.getProcurementType() != null ? requirement.getProcurementType().name() : null)
                .isOptional(requirement.getIsOptional())
                .notes(requirement.getNotes())
                .supplierItemCode(requirement.getSupplierItemCode())
                .templateNotes(requirement.getTemplateNotes())
                .createdAt(requirement.getCreatedAt())
                .updatedAt(requirement.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert WorkflowDependency entity to detailed DTO
     */
    private WorkflowTemplateDetailResponse.WorkflowDependencyDetailResponse convertToDependencyDetailResponse(WorkflowDependency dependency) {
        return WorkflowTemplateDetailResponse.WorkflowDependencyDetailResponse.builder()
                .id(dependency.getId())
                .dependentEntityType(dependency.getDependentEntityType().name())
                .dependentEntityId(dependency.getDependentEntityId())
                .dependentEntityName("") // Would need to fetch entity name based on type and ID
                .dependsOnEntityType(dependency.getDependsOnEntityType().name())
                .dependsOnEntityId(dependency.getDependsOnEntityId())
                .dependsOnEntityName("") // Would need to fetch entity name based on type and ID
                .dependencyType(dependency.getDependencyType().name())
                .lagDays(dependency.getLagDays())
                .createdAt(dependency.getCreatedAt())
                .updatedAt(dependency.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert WorkflowTemplate entity to DTO
     */
    private WorkflowTemplateDto convertToDto(WorkflowTemplate template) {
        return WorkflowTemplateDto.builder()
                .id(template.getId())
                .companyId(template.getCompany().getId())
                .companyName(template.getCompany().getName())
                .name(template.getName())
                .description(template.getDescription())
                .category(template.getCategory())
                .active(template.getActive())
                .isDefault(template.getIsDefault())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
    
    // Inner class for available transitions
    public static class AvailableTransition {
        private String actionType;
        private String targetId;
        private String description;
        private boolean enabled;
        
        // Constructors, getters, setters
        public AvailableTransition(String actionType, String targetId, String description, boolean enabled) {
            this.actionType = actionType;
            this.targetId = targetId;
            this.description = description;
            this.enabled = enabled;
        }
        
        // Getters and setters
        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }
        
        public String getTargetId() { return targetId; }
        public void setTargetId(String targetId) { this.targetId = targetId; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}