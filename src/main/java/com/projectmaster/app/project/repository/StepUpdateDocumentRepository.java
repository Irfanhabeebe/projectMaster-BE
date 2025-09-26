package com.projectmaster.app.project.repository;

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
public interface StepUpdateDocumentRepository extends JpaRepository<StepUpdateDocument, UUID> {

    // Find documents for a specific step update
    List<StepUpdateDocument> findByStepUpdateIdOrderByUploadDateDesc(UUID stepUpdateId);

    // Find documents by step
    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.id = :stepId " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByStepId(@Param("stepId") UUID stepId);

    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.id = :stepId " +
           "ORDER BY sd.uploadDate DESC")
    Page<StepUpdateDocument> findByStepId(@Param("stepId") UUID stepId, Pageable pageable);

    // Find documents by task
    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.id = :taskId " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByTaskId(@Param("taskId") UUID taskId);

    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.id = :taskId " +
           "ORDER BY sd.uploadDate DESC")
    Page<StepUpdateDocument> findByTaskId(@Param("taskId") UUID taskId, Pageable pageable);

    // Find documents by stage
    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.projectStage.id = :stageId " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByStageId(@Param("stageId") UUID stageId);

    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.projectStage.id = :stageId " +
           "ORDER BY sd.uploadDate DESC")
    Page<StepUpdateDocument> findByStageId(@Param("stageId") UUID stageId, Pageable pageable);

    // Find documents by project
    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.projectStage.project.id = :projectId " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByProjectId(@Param("projectId") UUID projectId);

    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.projectStage.project.id = :projectId " +
           "ORDER BY sd.uploadDate DESC")
    Page<StepUpdateDocument> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    // Find documents by type
    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.id = :stepId " +
           "AND sd.documentType = :documentType " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByStepIdAndDocumentType(@Param("stepId") UUID stepId, 
                                                        @Param("documentType") StepUpdateDocument.DocumentType documentType);

    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.id = :taskId " +
           "AND sd.documentType = :documentType " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByTaskIdAndDocumentType(@Param("taskId") UUID taskId, 
                                                        @Param("documentType") StepUpdateDocument.DocumentType documentType);

    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.projectStage.project.id = :projectId " +
           "AND sd.documentType = :documentType " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByProjectIdAndDocumentType(@Param("projectId") UUID projectId, 
                                                           @Param("documentType") StepUpdateDocument.DocumentType documentType);

    // Find documents by multiple types
    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.id = :stepId " +
           "AND sd.documentType IN :documentTypes " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByStepIdAndDocumentTypes(@Param("stepId") UUID stepId, 
                                                         @Param("documentTypes") List<StepUpdateDocument.DocumentType> documentTypes);

    // Find documents by date range
    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.id = :stepId " +
           "AND sd.uploadDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByStepIdAndDateRange(@Param("stepId") UUID stepId, 
                                                     @Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);

    // Find photos only
    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.id = :stepId " +
           "AND sd.documentType = 'PHOTO' " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findPhotosByStepId(@Param("stepId") UUID stepId);

    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.id = :taskId " +
           "AND sd.documentType = 'PHOTO' " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findPhotosByTaskId(@Param("taskId") UUID taskId);

    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.projectTask.projectStage.project.id = :projectId " +
           "AND sd.documentType = 'PHOTO' " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findPhotosByProjectId(@Param("projectId") UUID projectId);

    // Count documents by step
    @Query("SELECT COUNT(sd) FROM StepUpdateDocument sd WHERE sd.stepUpdate.projectStep.id = :stepId")
    Long countByStepId(@Param("stepId") UUID stepId);

    // Count documents by task
    @Query("SELECT COUNT(sd) FROM StepUpdateDocument sd WHERE sd.stepUpdate.projectStep.projectTask.id = :taskId")
    Long countByTaskId(@Param("taskId") UUID taskId);

    // Count documents by stage
    @Query("SELECT COUNT(sd) FROM StepUpdateDocument sd WHERE sd.stepUpdate.projectStep.projectTask.projectStage.id = :stageId")
    Long countByStageId(@Param("stageId") UUID stageId);

    // Count documents by project
    @Query("SELECT COUNT(sd) FROM StepUpdateDocument sd WHERE sd.stepUpdate.projectStep.projectTask.projectStage.project.id = :projectId")
    Long countByProjectId(@Param("projectId") UUID projectId);

    // Find documents by file extension
    @Query("SELECT sd FROM StepUpdateDocument sd " +
           "JOIN FETCH sd.stepUpdate su " +
           "WHERE su.projectStep.id = :stepId " +
           "AND LOWER(sd.fileExtension) = LOWER(:extension) " +
           "ORDER BY sd.uploadDate DESC")
    List<StepUpdateDocument> findByStepIdAndFileExtension(@Param("stepId") UUID stepId, 
                                                         @Param("extension") String extension);

}
