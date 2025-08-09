package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.StandardWorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StandardWorkflowTaskRepository extends JpaRepository<StandardWorkflowTask, UUID> {

    @Query("SELECT swt FROM StandardWorkflowTask swt WHERE swt.standardWorkflowStage.id = :standardWorkflowStageId ORDER BY swt.orderIndex")
    List<StandardWorkflowTask> findByStandardWorkflowStageIdOrderByOrderIndex(@Param("standardWorkflowStageId") UUID standardWorkflowStageId);

    @Query("SELECT swt FROM StandardWorkflowTask swt WHERE swt.standardWorkflowStage.standardWorkflowTemplate.id = :standardWorkflowTemplateId ORDER BY swt.standardWorkflowStage.orderIndex, swt.orderIndex")
    List<StandardWorkflowTask> findByStandardWorkflowTemplateIdOrderByStageAndTaskOrder(@Param("standardWorkflowTemplateId") UUID standardWorkflowTemplateId);
} 