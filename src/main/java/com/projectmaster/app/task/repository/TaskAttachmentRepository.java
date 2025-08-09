package com.projectmaster.app.task.repository;

import com.projectmaster.app.task.entity.TaskAttachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, UUID> {

    /**
     * Find all attachments for a task
     */
    List<TaskAttachment> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    /**
     * Find attachments for a task with pagination
     */
    Page<TaskAttachment> findByTaskId(UUID taskId, Pageable pageable);

    /**
     * Find all attachments uploaded by a user
     */
    List<TaskAttachment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find attachments by user with pagination
     */
    Page<TaskAttachment> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find attachments for a project
     */
    @Query("SELECT ta FROM TaskAttachment ta WHERE ta.task.projectStep.projectTask.projectStage.project.id = :projectId " +
           "ORDER BY ta.createdAt DESC")
    List<TaskAttachment> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find attachments for a project with pagination
     */
    @Query("SELECT ta FROM TaskAttachment ta WHERE ta.task.projectStep.projectTask.projectStage.project.id = :projectId")
    Page<TaskAttachment> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Find attachments by content type
     */
    List<TaskAttachment> findByContentTypeContainingIgnoreCase(String contentType);

    /**
     * Find attachments by file extension
     */
    @Query("SELECT ta FROM TaskAttachment ta WHERE LOWER(ta.fileName) LIKE LOWER(CONCAT('%.', :extension))")
    List<TaskAttachment> findByFileExtension(@Param("extension") String extension);

    /**
     * Find attachments by file name pattern
     */
    @Query("SELECT ta FROM TaskAttachment ta WHERE LOWER(ta.fileName) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<TaskAttachment> findByFileNameContaining(@Param("pattern") String pattern);

    /**
     * Count attachments for a task
     */
    long countByTaskId(UUID taskId);

    /**
     * Calculate total file size for a task
     */
    @Query("SELECT COALESCE(SUM(ta.fileSize), 0) FROM TaskAttachment ta WHERE ta.task.id = :taskId")
    Long sumFileSizeByTaskId(@Param("taskId") UUID taskId);

    /**
     * Calculate total file size for a project
     */
    @Query("SELECT COALESCE(SUM(ta.fileSize), 0) FROM TaskAttachment ta " +
           "WHERE ta.task.projectStep.projectTask.projectStage.project.id = :projectId")
    Long sumFileSizeByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find large attachments (above specified size)
     */
    @Query("SELECT ta FROM TaskAttachment ta WHERE ta.fileSize > :sizeThreshold ORDER BY ta.fileSize DESC")
    List<TaskAttachment> findLargeAttachments(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * Delete all attachments for a task
     */
    void deleteByTaskId(UUID taskId);

    /**
     * Find attachments by file path (for cleanup operations)
     */
    List<TaskAttachment> findByFilePath(String filePath);
}