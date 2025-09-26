package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {
    
    /**
     * Find all steps by workflow stage ID ordered by order index
     */
    List<WorkflowStep> findByWorkflowTaskIdOrderByOrderIndex(UUID workflowTaskId);
    
    /**
     * Find steps by workflow stage ID and estimated days range
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflowTask.id = :taskId AND " +
           "ws.estimatedDays BETWEEN :minDays AND :maxDays ORDER BY ws.orderIndex")
    List<WorkflowStep> findByTaskIdAndEstimatedDaysRange(@Param("taskId") UUID taskId,
                                                         @Param("minDays") Integer minDays,
                                                         @Param("maxDays") Integer maxDays);
    
    /**
     * Find steps with required skills
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.requiredSkills IS NOT NULL AND ws.requiredSkills != ''")
    List<WorkflowStep> findStepsWithRequiredSkills();
    
    /**
     * Find steps by workflow stage and order range
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflowTask.id = :taskId AND " +
           "ws.orderIndex BETWEEN :startOrder AND :endOrder ORDER BY ws.orderIndex")
    List<WorkflowStep> findByTaskIdAndOrderRange(@Param("taskId") UUID taskId,
                                                 @Param("startOrder") Integer startOrder,
                                                 @Param("endOrder") Integer endOrder);
    
    /**
     * Count steps by workflow stage
     */
    Long countByWorkflowTaskId(UUID workflowTaskId);
    
    /**
     * Find next step by order index
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflowTask.id = :taskId AND " +
           "ws.orderIndex > :currentOrder ORDER BY ws.orderIndex LIMIT 1")
    WorkflowStep findNextStep(@Param("taskId") UUID taskId, @Param("currentOrder") Integer currentOrder);
    
    /**
     * Find steps by workflow template (through stage)
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflowTask.workflowStage.workflowTemplate.id = :templateId " +
           "ORDER BY ws.workflowTask.workflowStage.orderIndex, ws.workflowTask.orderIndex, ws.orderIndex")
    List<WorkflowStep> findByWorkflowTemplateId(@Param("templateId") UUID templateId);
}