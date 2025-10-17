package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.WorkflowStepRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowStepRequirementRepository extends JpaRepository<WorkflowStepRequirement, UUID> {

    /**
     * Find all requirements for a workflow step ordered by display order
     */
    List<WorkflowStepRequirement> findByWorkflowStepIdOrderByDisplayOrder(UUID workflowStepId);

    /**
     * Find by workflow step and category
     */
    @Query("SELECT wsr FROM WorkflowStepRequirement wsr " +
           "WHERE wsr.workflowStep.id = :workflowStepId AND wsr.category.id = :categoryId")
    List<WorkflowStepRequirement> findByWorkflowStepIdAndCategoryId(@Param("workflowStepId") UUID workflowStepId, 
                                                                  @Param("categoryId") UUID categoryId);

    /**
     * Find by supplier
     */
    List<WorkflowStepRequirement> findBySupplierId(UUID supplierId);

    /**
     * Find by category
     */
    List<WorkflowStepRequirement> findByCategoryId(UUID categoryId);

    /**
     * Find by procurement type
     */
    List<WorkflowStepRequirement> findByProcurementType(WorkflowStepRequirement.ProcurementType procurementType);

    /**
     * Find optional requirements for a workflow step
     */
    List<WorkflowStepRequirement> findByWorkflowStepIdAndIsOptionalTrueOrderByDisplayOrder(UUID workflowStepId);

    /**
     * Find required (non-optional) requirements for a workflow step
     */
    List<WorkflowStepRequirement> findByWorkflowStepIdAndIsOptionalFalseOrderByDisplayOrder(UUID workflowStepId);

    /**
     * Count requirements for a workflow step
     */
    long countByWorkflowStepId(UUID workflowStepId);

    /**
     * Delete all requirements for a workflow step
     */
    void deleteByWorkflowStepId(UUID workflowStepId);
    
    /**
     * Find requirement by workflow step, category, and item name for comparison
     */
    @Query("SELECT wsr FROM WorkflowStepRequirement wsr " +
           "WHERE wsr.workflowStep.id = :workflowStepId AND " +
           "wsr.category.id = :categoryId AND " +
           "wsr.itemName = :itemName")
    WorkflowStepRequirement findByWorkflowStepIdAndCategoryIdAndItemName(
        @Param("workflowStepId") UUID workflowStepId,
        @Param("categoryId") UUID categoryId,
        @Param("itemName") String itemName);
    
    /**
     * Find all requirements for a workflow step for bulk operations
     */
    List<WorkflowStepRequirement> findByWorkflowStepId(UUID workflowStepId);
}
