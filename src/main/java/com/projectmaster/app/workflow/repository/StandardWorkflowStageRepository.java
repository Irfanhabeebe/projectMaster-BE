package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.StandardWorkflowStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StandardWorkflowStageRepository extends JpaRepository<StandardWorkflowStage, UUID> {
    
    /**
     * Find all stages by standard workflow template ID ordered by order index
     */
    List<StandardWorkflowStage> findByStandardWorkflowTemplateIdOrderByOrderIndex(UUID standardWorkflowTemplateId);
    
    /**
     * Find stages by standard workflow template ID and parallel execution flag
     */
    List<StandardWorkflowStage> findByStandardWorkflowTemplateIdAndParallelExecution(UUID standardWorkflowTemplateId, Boolean parallelExecution);
    
    /**
     * Find stages requiring approvals
     */
    @Query("SELECT sws FROM StandardWorkflowStage sws WHERE sws.requiredApprovals > 0")
    List<StandardWorkflowStage> findStagesRequiringApprovals();
    
    /**
     * Find stages by standard workflow template and order range
     */
    @Query("SELECT sws FROM StandardWorkflowStage sws WHERE sws.standardWorkflowTemplate.id = :templateId AND " +
           "sws.orderIndex BETWEEN :startOrder AND :endOrder ORDER BY sws.orderIndex")
    List<StandardWorkflowStage> findByTemplateIdAndOrderRange(@Param("templateId") UUID templateId,
                                                             @Param("startOrder") Integer startOrder,
                                                             @Param("endOrder") Integer endOrder);
    
    /**
     * Count stages by standard workflow template
     */
    Long countByStandardWorkflowTemplateId(UUID standardWorkflowTemplateId);
    
    /**
     * Find stages with their tasks for a template
     */
    @Query("SELECT DISTINCT sws FROM StandardWorkflowStage sws " +
           "LEFT JOIN FETCH sws.tasks " +
           "WHERE sws.standardWorkflowTemplate.id = :templateId " +
           "ORDER BY sws.orderIndex")
    List<StandardWorkflowStage> findByTemplateIdWithSteps(@Param("templateId") UUID templateId);

    @Query("SELECT sws FROM StandardWorkflowStage sws WHERE sws.standardWorkflowTemplate.id = :templateId ORDER BY sws.orderIndex")
    List<StandardWorkflowStage> findByTemplateIdWithTasks(@Param("templateId") UUID templateId);

    @Query("SELECT sws FROM StandardWorkflowStage sws WHERE sws.standardWorkflowTemplate.active = true ORDER BY sws.standardWorkflowTemplate.name, sws.orderIndex")
    List<StandardWorkflowStage> findAllActiveWithTasks();
}