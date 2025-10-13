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
     * Find all steps by workflow task ID ordered by creation time
     */
    List<WorkflowStep> findByWorkflowTaskIdOrderByCreatedAt(UUID workflowTaskId);
    
    /**
     * Find steps by workflow task ID and estimated days range
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflowTask.id = :taskId AND " +
           "ws.estimatedDays BETWEEN :minDays AND :maxDays ORDER BY ws.createdAt")
    List<WorkflowStep> findByTaskIdAndEstimatedDaysRange(@Param("taskId") UUID taskId,
                                                         @Param("minDays") Integer minDays,
                                                         @Param("maxDays") Integer maxDays);
    
    /**
     * Count steps by workflow task
     */
    Long countByWorkflowTaskId(UUID workflowTaskId);
    
    /**
     * Find next step by creation time
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflowTask.id = :taskId AND " +
           "ws.createdAt > :currentCreatedAt ORDER BY ws.createdAt LIMIT 1")
    WorkflowStep findNextStep(@Param("taskId") UUID taskId, @Param("currentCreatedAt") java.time.Instant currentCreatedAt);
    
    /**
     * Find steps by workflow template (through stage and task)
     */
    @Query("SELECT ws FROM WorkflowStep ws WHERE ws.workflowTask.workflowStage.workflowTemplate.id = :templateId " +
           "ORDER BY ws.workflowTask.workflowStage.createdAt, ws.workflowTask.createdAt, ws.createdAt")
    List<WorkflowStep> findByWorkflowTemplateId(@Param("templateId") UUID templateId);
}