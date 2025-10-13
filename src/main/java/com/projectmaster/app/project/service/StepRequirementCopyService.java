package com.projectmaster.app.project.service;

import com.projectmaster.app.project.dto.CreateProjectStepRequirementRequest;
import com.projectmaster.app.project.dto.UpdateProjectStepRequirementRequest;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectStepRequirement;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.project.repository.ProjectStepRequirementRepository;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.entity.WorkflowStepRequirement;
import com.projectmaster.app.workflow.repository.WorkflowStepRequirementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StepRequirementCopyService {

    private final WorkflowStepRequirementRepository workflowStepRequirementRepository;
    private final ProjectStepRequirementRepository projectStepRequirementRepository;
    private final ProjectStepRepository projectStepRepository;

    /**
     * Copy workflow step requirements to project step during project creation
     */
    public void copyWorkflowRequirementsToProjectStep(ProjectStep projectStep, WorkflowStep workflowStep) {
        log.debug("Copying requirements from workflow step {} to project step {}", 
                workflowStep.getId(), projectStep.getId());
        
        // Get all requirements from the workflow step
        List<WorkflowStepRequirement> workflowRequirements = workflowStepRequirementRepository
                .findByWorkflowStepIdOrderByDisplayOrder(workflowStep.getId());
        
        if (workflowRequirements.isEmpty()) {
            log.debug("No requirements found for workflow step: {}", workflowStep.getId());
            return;
        }
        
        for (WorkflowStepRequirement workflowReq : workflowRequirements) {
            ProjectStepRequirement projectReq = ProjectStepRequirement.builder()
                    .projectStep(projectStep)
                    .workflowStepRequirement(workflowReq)  // Link back to template
                    .category(workflowReq.getCategory())
                    .supplier(workflowReq.getSupplier())   // Copy default supplier
                    .itemName(workflowReq.getItemName())
                    .brand(workflowReq.getBrand())
                    .model(workflowReq.getModel())
                    .quantity(0.0) // Default to 0 when created from template
                    .unit(workflowReq.getUnit())
                    .estimatedCost(workflowReq.getEstimatedCost())
                    .procurementType(convertProcurementType(workflowReq.getProcurementType()))
                    .isOptional(workflowReq.getIsOptional())
                    .notes(workflowReq.getTemplateNotes()) // Copy template notes
                    .supplierItemCode(workflowReq.getSupplierItemCode())
                    .status(ProjectStepRequirement.RequirementStatus.PENDING)
                    .isTemplateCopied(true)                // Mark as copied from template
                    .displayOrder(workflowReq.getDisplayOrder())
                    .build();
            
            projectStepRequirementRepository.save(projectReq);
            log.debug("Copied requirement: {} to project step: {}", 
                    workflowReq.getItemName(), projectStep.getId());
        }
        
        log.info("Copied {} requirements from workflow step {} to project step {}", 
                workflowRequirements.size(), workflowStep.getId(), projectStep.getId());
    }

    /**
     * Add new project-specific requirements (not from template)
     */
    public ProjectStepRequirement addProjectSpecificRequirement(
            UUID projectStepId, CreateProjectStepRequirementRequest request) {
        
        ProjectStep projectStep = projectStepRepository.findById(projectStepId)
                .orElseThrow(() -> new EntityNotFoundException("Project step not found"));
        
        ProjectStepRequirement requirement = ProjectStepRequirement.builder()
                .projectStep(projectStep)
                .workflowStepRequirement(null) // Not from template
                .category(request.getCategory())
                .supplier(request.getSupplier())
                .itemName(request.getItemName())
                .brand(request.getBrand())
                .model(request.getModel())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .estimatedCost(request.getEstimatedCost())
                .procurementType(request.getProcurementType())
                .isOptional(request.getIsOptional() != null ? request.getIsOptional() : false)
                .notes(request.getNotes())
                .supplierItemCode(request.getSupplierItemCode())
                .status(ProjectStepRequirement.RequirementStatus.PENDING)
                .isTemplateCopied(false) // Project-specific addition
                .displayOrder(getNextDisplayOrder(projectStepId))
                .build();
        
        return projectStepRequirementRepository.save(requirement);
    }

    /**
     * Update an existing project step requirement
     */
    public ProjectStepRequirement updateProjectStepRequirement(
            UUID requirementId, UpdateProjectStepRequirementRequest request) {
        
        ProjectStepRequirement requirement = projectStepRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new EntityNotFoundException("Project step requirement not found"));
        
        // Update fields
        if (request.getCategory() != null) {
            requirement.setCategory(request.getCategory());
        }
        if (request.getSupplier() != null) {
            requirement.setSupplier(request.getSupplier());
        }
        if (request.getItemName() != null) {
            requirement.setItemName(request.getItemName());
        }
        if (request.getBrand() != null) {
            requirement.setBrand(request.getBrand());
        }
        if (request.getModel() != null) {
            requirement.setModel(request.getModel());
        }
        if (request.getQuantity() != null) {
            requirement.setQuantity(request.getQuantity());
        }
        if (request.getUnit() != null) {
            requirement.setUnit(request.getUnit());
        }
        if (request.getEstimatedCost() != null) {
            requirement.setEstimatedCost(request.getEstimatedCost());
        }
        if (request.getActualCost() != null) {
            requirement.setActualCost(request.getActualCost());
        }
        if (request.getProcurementType() != null) {
            requirement.setProcurementType(request.getProcurementType());
        }
        if (request.getStatus() != null) {
            requirement.setStatus(request.getStatus());
        }
        if (request.getIsOptional() != null) {
            requirement.setIsOptional(request.getIsOptional());
        }
        if (request.getNotes() != null) {
            requirement.setNotes(request.getNotes());
        }
        if (request.getSupplierItemCode() != null) {
            requirement.setSupplierItemCode(request.getSupplierItemCode());
        }
        if (request.getSupplierQuoteNumber() != null) {
            requirement.setSupplierQuoteNumber(request.getSupplierQuoteNumber());
        }
        if (request.getQuoteExpiryDate() != null) {
            requirement.setQuoteExpiryDate(request.getQuoteExpiryDate());
        }
        if (request.getRequiredDeliveryDate() != null) {
            requirement.setRequiredDeliveryDate(request.getRequiredDeliveryDate());
        }
        if (request.getDeliveryInstructions() != null) {
            requirement.setDeliveryInstructions(request.getDeliveryInstructions());
        }
        
        return projectStepRequirementRepository.save(requirement);
    }

    /**
     * Delete a project step requirement
     */
    public void deleteProjectStepRequirement(UUID requirementId) {
        ProjectStepRequirement requirement = projectStepRequirementRepository.findById(requirementId)
                .orElseThrow(() -> new EntityNotFoundException("Project step requirement not found"));
        
        projectStepRequirementRepository.delete(requirement);
        log.info("Deleted project step requirement: {}", requirementId);
    }

    /**
     * Get the next display order for a project step
     */
    private Integer getNextDisplayOrder(UUID projectStepId) {
        List<ProjectStepRequirement> requirements = projectStepRequirementRepository
                .findByProjectStepIdOrderByDisplayOrder(projectStepId);
        
        if (requirements.isEmpty()) {
            return 1;
        }
        
        return requirements.get(requirements.size() - 1).getDisplayOrder() + 1;
    }

    /**
     * Get all requirements for a project step
     */
    public List<ProjectStepRequirement> getProjectStepRequirements(UUID projectStepId) {
        return projectStepRequirementRepository.findByProjectStepIdOrderByDisplayOrder(projectStepId);
    }

    /**
     * Get template-copied requirements for a project step
     */
    public List<ProjectStepRequirement> getTemplateRequirements(UUID projectStepId) {
        return projectStepRequirementRepository.findByProjectStepIdAndIsTemplateCopiedTrueOrderByDisplayOrder(projectStepId);
    }

    /**
     * Get project-specific requirements for a project step
     */
    public List<ProjectStepRequirement> getProjectSpecificRequirements(UUID projectStepId) {
        return projectStepRequirementRepository.findByProjectStepIdAndIsTemplateCopiedFalseOrderByDisplayOrder(projectStepId);
    }

    /**
     * Convert WorkflowStepRequirement.ProcurementType to ProjectStepRequirement.ProcurementType
     */
    private ProjectStepRequirement.ProcurementType convertProcurementType(WorkflowStepRequirement.ProcurementType workflowType) {
        if (workflowType == null) {
            return ProjectStepRequirement.ProcurementType.BUY;
        }
        
        switch (workflowType) {
            case BUY:
                return ProjectStepRequirement.ProcurementType.BUY;
            case PROVIDED_BY_CONTRACTOR:
                return ProjectStepRequirement.ProcurementType.PROVIDED_BY_CONTRACTOR;
            case ALREADY_OWNED:
                return ProjectStepRequirement.ProcurementType.ALREADY_OWNED;
            case CLIENT_PROVIDES:
                return ProjectStepRequirement.ProcurementType.CLIENT_PROVIDES;
            default:
                return ProjectStepRequirement.ProcurementType.BUY;
        }
    }
}
