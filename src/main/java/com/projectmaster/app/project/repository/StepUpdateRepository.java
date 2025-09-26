package com.projectmaster.app.project.repository;

import com.projectmaster.app.project.entity.StepUpdate;
import com.projectmaster.app.project.entity.StepUpdateDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StepUpdateRepository extends JpaRepository<StepUpdate, UUID> {

    // Find updates for a specific step
    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.id = :stepId " +
           "ORDER BY su.updateDate DESC")
    List<StepUpdate> findByProjectStepIdWithDetails(@Param("stepId") UUID stepId);

    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.id = :stepId " +
           "ORDER BY su.updateDate DESC")
    Page<StepUpdate> findByProjectStepIdWithDetails(@Param("stepId") UUID stepId, Pageable pageable);

    // Find updates for all steps in a task
    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.projectTask.id = :taskId " +
           "ORDER BY su.updateDate DESC")
    List<StepUpdate> findByProjectTaskIdWithDetails(@Param("taskId") UUID taskId);

    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.projectTask.id = :taskId " +
           "ORDER BY su.updateDate DESC")
    Page<StepUpdate> findByProjectTaskIdWithDetails(@Param("taskId") UUID taskId, Pageable pageable);

    // Find updates for all steps in a stage
    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.projectTask.projectStage.id = :stageId " +
           "ORDER BY su.updateDate DESC")
    List<StepUpdate> findByProjectStageIdWithDetails(@Param("stageId") UUID stageId);

    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.projectTask.projectStage.id = :stageId " +
           "ORDER BY su.updateDate DESC")
    Page<StepUpdate> findByProjectStageIdWithDetails(@Param("stageId") UUID stageId, Pageable pageable);

    // Find updates for all steps in a project
    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.projectTask.projectStage.project.id = :projectId " +
           "ORDER BY su.updateDate DESC")
    List<StepUpdate> findByProjectIdWithDetails(@Param("projectId") UUID projectId);

    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.projectTask.projectStage.project.id = :projectId " +
           "ORDER BY su.updateDate DESC")
    Page<StepUpdate> findByProjectIdWithDetails(@Param("projectId") UUID projectId, Pageable pageable);

    // Find updates by date range
    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.id = :stepId " +
           "AND su.updateDate BETWEEN :startDate AND :endDate " +
           "ORDER BY su.updateDate DESC")
    List<StepUpdate> findByStepIdAndDateRange(@Param("stepId") UUID stepId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    // Find updates by type
    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.id = :stepId " +
           "AND su.updateType = :updateType " +
           "ORDER BY su.updateDate DESC")
    List<StepUpdate> findByStepIdAndUpdateType(@Param("stepId") UUID stepId, 
                                              @Param("updateType") StepUpdate.UpdateType updateType);

    // Find milestone updates for a project
    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents " +
           "WHERE su.projectStep.projectTask.projectStage.project.id = :projectId " +
           "AND su.updateType = 'MILESTONE_REACHED' " +
           "ORDER BY su.updateDate DESC")
    List<StepUpdate> findMilestoneUpdatesByProjectId(@Param("projectId") UUID projectId);

    // Count updates by step
    @Query("SELECT COUNT(su) FROM StepUpdate su WHERE su.projectStep.id = :stepId")
    Long countByStepId(@Param("stepId") UUID stepId);

    // Count updates by task
    @Query("SELECT COUNT(su) FROM StepUpdate su WHERE su.projectStep.projectTask.id = :taskId")
    Long countByTaskId(@Param("taskId") UUID taskId);

    // Count updates by stage
    @Query("SELECT COUNT(su) FROM StepUpdate su WHERE su.projectStep.projectTask.projectStage.id = :stageId")
    Long countByStageId(@Param("stageId") UUID stageId);

    // Count updates by project
    @Query("SELECT COUNT(su) FROM StepUpdate su WHERE su.projectStep.projectTask.projectStage.project.id = :projectId")
    Long countByProjectId(@Param("projectId") UUID projectId);

    // Find latest update for each step in a task
    @Query("SELECT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "WHERE su.projectStep.projectTask.id = :taskId " +
           "AND su.updateDate = (" +
           "  SELECT MAX(su2.updateDate) FROM StepUpdate su2 " +
           "  WHERE su2.projectStep.id = su.projectStep.id" +
           ")")
    List<StepUpdate> findLatestUpdatesByTaskId(@Param("taskId") UUID taskId);

    // Find updates with specific document types
    @Query("SELECT DISTINCT su FROM StepUpdate su " +
           "JOIN FETCH su.updatedBy " +
           "LEFT JOIN FETCH su.documents sd " +
           "WHERE su.projectStep.id = :stepId " +
           "AND sd.documentType IN :documentTypes " +
           "ORDER BY su.updateDate DESC")
    List<StepUpdate> findByStepIdAndDocumentTypes(@Param("stepId") UUID stepId, 
                                                 @Param("documentTypes") List<StepUpdateDocument.DocumentType> documentTypes);
}
