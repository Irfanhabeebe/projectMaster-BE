package com.projectmaster.app.workflow.repository;

import com.projectmaster.app.workflow.entity.StandardWorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StandardWorkflowStepRepository extends JpaRepository<StandardWorkflowStep, UUID> {

    @Query("SELECT sws FROM StandardWorkflowStep sws WHERE sws.standardWorkflowTask.id = :standardWorkflowTaskId ORDER BY sws.createdAt")
    List<StandardWorkflowStep> findByStandardWorkflowTaskIdOrderByOrderIndex(@Param("standardWorkflowTaskId") UUID standardWorkflowTaskId);

    @Query("SELECT sws FROM StandardWorkflowStep sws WHERE sws.standardWorkflowTask.standardWorkflowStage.id = :standardWorkflowStageId ORDER BY sws.standardWorkflowTask.createdAt, sws.createdAt")
    List<StandardWorkflowStep> findByStandardWorkflowStageIdOrderByTaskAndStepOrder(@Param("standardWorkflowStageId") UUID standardWorkflowStageId);

    @Query("SELECT sws FROM StandardWorkflowStep sws WHERE sws.standardWorkflowTask.standardWorkflowStage.standardWorkflowTemplate.id = :standardWorkflowTemplateId ORDER BY sws.standardWorkflowTask.standardWorkflowStage.createdAt, sws.standardWorkflowTask.createdAt, sws.createdAt")
    List<StandardWorkflowStep> findByStandardWorkflowTemplateIdOrderByStageAndTaskAndStepOrder(@Param("standardWorkflowTemplateId") UUID standardWorkflowTemplateId);
}