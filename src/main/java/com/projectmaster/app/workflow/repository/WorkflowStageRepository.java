package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.WorkflowStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowStageRepository extends JpaRepository<WorkflowStage, UUID> {
    
    /**
     * Find all stages by workflow template ID ordered by order index
     */
    List<WorkflowStage> findByWorkflowTemplateIdOrderByOrderIndex(UUID workflowTemplateId);
    
    /**
     * Find stages by workflow template ID and parallel execution flag
     */
    List<WorkflowStage> findByWorkflowTemplateIdAndParallelExecution(UUID workflowTemplateId, Boolean parallelExecution);
    
    /**
     * Find stages requiring approvals
     */
    @Query("SELECT ws FROM WorkflowStage ws WHERE ws.requiredApprovals > 0")
    List<WorkflowStage> findStagesRequiringApprovals();
    
    /**
     * Find stages by workflow template and order range
     */
    @Query("SELECT ws FROM WorkflowStage ws WHERE ws.workflowTemplate.id = :templateId AND " +
           "ws.orderIndex BETWEEN :startOrder AND :endOrder ORDER BY ws.orderIndex")
    List<WorkflowStage> findByTemplateIdAndOrderRange(@Param("templateId") UUID templateId,
                                                     @Param("startOrder") Integer startOrder,
                                                     @Param("endOrder") Integer endOrder);
    
    /**
     * Count stages by workflow template
     */
    Long countByWorkflowTemplateId(UUID workflowTemplateId);
    
    /**
     * Find next stage by order index
     */
    @Query("SELECT ws FROM WorkflowStage ws WHERE ws.workflowTemplate.id = :templateId AND " +
           "ws.orderIndex > :currentOrder ORDER BY ws.orderIndex LIMIT 1")
    WorkflowStage findNextStage(@Param("templateId") UUID templateId, @Param("currentOrder") Integer currentOrder);
}