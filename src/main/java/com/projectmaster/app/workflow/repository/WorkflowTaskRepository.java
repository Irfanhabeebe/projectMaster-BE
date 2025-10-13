package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, UUID> {

    @Query("SELECT wt FROM WorkflowTask wt WHERE wt.workflowStage.id = :workflowStageId ORDER BY wt.createdAt")
    List<WorkflowTask> findByWorkflowStageIdOrderByCreatedAt(@Param("workflowStageId") UUID workflowStageId);

    @Query("SELECT wt FROM WorkflowTask wt WHERE wt.workflowStage.workflowTemplate.id = :workflowTemplateId ORDER BY wt.workflowStage.orderIndex, wt.createdAt")
    List<WorkflowTask> findByWorkflowTemplateIdOrderByStageAndTaskOrder(@Param("workflowTemplateId") UUID workflowTemplateId);
} 