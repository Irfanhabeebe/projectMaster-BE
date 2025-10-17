package com.projectmaster.app.project.service;

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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
                    .quantity(workflowReq.getDefaultQuantity() != null ? workflowReq.getDefaultQuantity() : BigDecimal.ZERO) // Use template default or 0
                    .unit(workflowReq.getUnit())
                    .estimatedCost(workflowReq.getEstimatedCost())
                    .procurementType(convertProcurementType(workflowReq.getProcurementType()))
                    .isOptional(workflowReq.getIsOptional())
                    .notes(workflowReq.getTemplateNotes()) // Copy template notes
                    .supplierItemCode(workflowReq.getSupplierItemCode())
                    .status(ProjectStepRequirement.RequirementStatus.PENDING)
                    .isTemplateCopied(true)                // Mark as copied from template
                    .displayOrder(workflowReq.getDisplayOrder())
                    .customerSelectable(workflowReq.getCustomerSelectable()) // Copy customer selectable flag
                    .build();
            
            projectStepRequirementRepository.save(projectReq);
            log.debug("Copied requirement: {} to project step: {}", 
                    workflowReq.getItemName(), projectStep.getId());
        }
        
        log.info("Copied {} requirements from workflow step {} to project step {}", 
                workflowRequirements.size(), workflowStep.getId(), projectStep.getId());
    }


    /**
     * Sync project step requirements - smart update/create/delete based on category and item name
     */
    public List<ProjectStepRequirement> syncProjectStepRequirements(
            UUID projectStepId, List<UpdateProjectStepRequirementRequest> requests) {
        
        log.info("Syncing requirements for project step: {}", projectStepId);
        
        ProjectStep projectStep = projectStepRepository.findById(projectStepId)
                .orElseThrow(() -> new EntityNotFoundException("Project step not found"));
        
        // Get all existing requirements for this step
        List<ProjectStepRequirement> existingRequirements = projectStepRequirementRepository
                .findByProjectStepIdOrderByDisplayOrder(projectStepId);
        
        // Create a map of existing requirements by category+itemName for quick lookup
        Map<String, ProjectStepRequirement> existingMap = existingRequirements.stream()
                .collect(Collectors.toMap(
                    req -> createRequirementKey(req.getCategory().getId(), req.getItemName()),
                    req -> req
                ));
        
        // Track which requirements were in the request
        Set<String> requestedKeys = new HashSet<>();
        List<ProjectStepRequirement> result = new ArrayList<>();
        
        // Process each request - update existing or create new
        for (UpdateProjectStepRequirementRequest request : requests) {
            if (request.getCategory() == null || request.getItemName() == null || request.getItemName().isBlank()) {
                log.warn("Skipping requirement with null category or empty item name");
                continue;
            }
            
            String key = createRequirementKey(request.getCategory().getId(), request.getItemName());
            requestedKeys.add(key);
            
            ProjectStepRequirement requirement = existingMap.get(key);
            
            if (requirement != null) {
                // Update existing requirement
                log.debug("Updating existing requirement: {} - {}", 
                        request.getCategory().getName(), request.getItemName());
                updateRequirementFromRequest(requirement, request);
            } else {
                // Create new requirement
                log.debug("Creating new requirement: {} - {}", 
                        request.getCategory().getName(), request.getItemName());
                requirement = ProjectStepRequirement.builder()
                        .projectStep(projectStep)
                        .workflowStepRequirement(null) // Project-specific
                        .category(request.getCategory())
                        .supplier(request.getSupplier())
                        .itemName(request.getItemName())
                        .brand(request.getBrand())
                        .model(request.getModel())
                        .quantity(request.getQuantity() != null ? request.getQuantity() : BigDecimal.ZERO)
                        .unit(request.getUnit())
                        .estimatedCost(request.getEstimatedCost())
                        .actualCost(request.getActualCost())
                        .procurementType(request.getProcurementType() != null ? 
                                request.getProcurementType() : ProjectStepRequirement.ProcurementType.BUY) // Default to BUY if null
                        .isOptional(request.getIsOptional() != null ? request.getIsOptional() : false)
                        .notes(request.getNotes())
                        .supplierItemCode(request.getSupplierItemCode())
                        .supplierQuoteNumber(request.getSupplierQuoteNumber())
                        .quoteExpiryDate(request.getQuoteExpiryDate())
                        .requiredDeliveryDate(request.getRequiredDeliveryDate())
                        .deliveryInstructions(request.getDeliveryInstructions())
                        .status(request.getStatus() != null ? 
                                request.getStatus() : ProjectStepRequirement.RequirementStatus.PENDING)
                        .isTemplateCopied(false)
                        .displayOrder(getNextDisplayOrder(projectStepId))
                        .customerSelectable(request.getCustomerSelectable() != null ? 
                                request.getCustomerSelectable() : true) // Default to true if not specified
                        .build();
            }
            
            requirement = projectStepRequirementRepository.save(requirement);
            result.add(requirement);
        }
        
        // Delete requirements that are in database but not in request
        List<ProjectStepRequirement> toDelete = existingRequirements.stream()
                .filter(req -> !requestedKeys.contains(
                        createRequirementKey(req.getCategory().getId(), req.getItemName())))
                .collect(Collectors.toList());
        
        if (!toDelete.isEmpty()) {
            log.info("Deleting {} requirements not present in request", toDelete.size());
            toDelete.forEach(req -> {
                log.debug("Deleting requirement: {} - {}", 
                        req.getCategory().getName(), req.getItemName());
                projectStepRequirementRepository.delete(req);
            });
        }
        
        log.info("Sync complete - {} requirements in result, {} deleted", 
                result.size(), toDelete.size());
        
        return result;
    }
    
    /**
     * Create a unique key for requirement matching based on category ID and item name
     */
    private String createRequirementKey(UUID categoryId, String itemName) {
        return categoryId.toString() + ":" + itemName.trim().toLowerCase();
    }
    
    /**
     * Update requirement fields from request
     */
    private void updateRequirementFromRequest(
            ProjectStepRequirement requirement, UpdateProjectStepRequirementRequest request) {
        
        if (request.getSupplier() != null) {
            requirement.setSupplier(request.getSupplier());
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
        // Default to BUY if not specified, otherwise use provided value
        if (request.getProcurementType() != null) {
            requirement.setProcurementType(request.getProcurementType());
        } else {
            requirement.setProcurementType(ProjectStepRequirement.ProcurementType.BUY);
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
        if (request.getCustomerSelectable() != null) {
            requirement.setCustomerSelectable(request.getCustomerSelectable());
        }
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
     * Convert ProjectStepRequirement entity to response DTO
     */
    public com.projectmaster.app.project.dto.ProjectStepRequirementResponse convertToResponse(ProjectStepRequirement requirement) {
        return com.projectmaster.app.project.dto.ProjectStepRequirementResponse.builder()
                .id(requirement.getId())
                .projectStepId(requirement.getProjectStep().getId())
                .workflowStepRequirementId(requirement.getWorkflowStepRequirement() != null ? 
                        requirement.getWorkflowStepRequirement().getId() : null)
                .categoryId(requirement.getCategory() != null ? requirement.getCategory().getId() : null)
                .categoryName(requirement.getCategory() != null ? requirement.getCategory().getName() : null)
                .categoryGroup(requirement.getCategory() != null ? requirement.getCategory().getCategoryGroup() : null)
                .supplierId(requirement.getSupplier() != null ? requirement.getSupplier().getId() : null)
                .supplierName(requirement.getSupplier() != null ? requirement.getSupplier().getName() : null)
                .itemName(requirement.getItemName())
                .brand(requirement.getBrand())
                .model(requirement.getModel())
                .quantity(requirement.getQuantity())
                .unit(requirement.getUnit())
                .estimatedCost(requirement.getEstimatedCost())
                .actualCost(requirement.getActualCost())
                .procurementType(requirement.getProcurementType())
                .status(requirement.getStatus())
                .isOptional(requirement.getIsOptional())
                .notes(requirement.getNotes())
                .supplierItemCode(requirement.getSupplierItemCode())
                .supplierQuoteNumber(requirement.getSupplierQuoteNumber())
                .quoteExpiryDate(requirement.getQuoteExpiryDate())
                .requiredDeliveryDate(requirement.getRequiredDeliveryDate())
                .deliveryInstructions(requirement.getDeliveryInstructions())
                .isTemplateCopied(requirement.getIsTemplateCopied())
                .displayOrder(requirement.getDisplayOrder())
                .createdAt(requirement.getCreatedAt())
                .updatedAt(requirement.getUpdatedAt())
                .customerSelectable(requirement.getCustomerSelectable())
                .build();
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
