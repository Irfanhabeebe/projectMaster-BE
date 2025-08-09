package com.projectmaster.app.task.repository;

import com.projectmaster.app.task.entity.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    /**
     * Find all comments for a task ordered by creation time
     */
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);

    /**
     * Find comments for a task with pagination
     */
    Page<TaskComment> findByTaskId(UUID taskId, Pageable pageable);

    /**
     * Find all comments by a user
     */
    List<TaskComment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find comments by a user with pagination
     */
    Page<TaskComment> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find internal comments for a task
     */
    List<TaskComment> findByTaskIdAndIsInternalTrueOrderByCreatedAtAsc(UUID taskId);

    /**
     * Find public comments for a task
     */
    List<TaskComment> findByTaskIdAndIsInternalFalseOrderByCreatedAtAsc(UUID taskId);

    /**
     * Find comments for a project
     */
    @Query("SELECT tc FROM TaskComment tc WHERE tc.task.projectStep.projectTask.projectStage.project.id = :projectId " +
           "ORDER BY tc.createdAt DESC")
    List<TaskComment> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find comments for a project with pagination
     */
    @Query("SELECT tc FROM TaskComment tc WHERE tc.task.projectStep.projectTask.projectStage.project.id = :projectId")
    Page<TaskComment> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Find recent comments for a task (within last N days)
     */
    @Query("SELECT tc FROM TaskComment tc WHERE tc.task.id = :taskId AND tc.createdAt >= :since " +
           "ORDER BY tc.createdAt DESC")
    List<TaskComment> findRecentByTaskId(@Param("taskId") UUID taskId, @Param("since") Instant since);

    /**
     * Count comments for a task
     */
    long countByTaskId(UUID taskId);

    /**
     * Count internal comments for a task
     */
    long countByTaskIdAndIsInternalTrue(UUID taskId);

    /**
     * Count public comments for a task
     */
    long countByTaskIdAndIsInternalFalse(UUID taskId);

    /**
     * Find comments containing specific text
     */
    @Query("SELECT tc FROM TaskComment tc WHERE tc.task.id = :taskId AND " +
           "LOWER(tc.comment) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<TaskComment> findByTaskIdAndCommentContaining(@Param("taskId") UUID taskId, 
                                                      @Param("searchText") String searchText);

    /**
     * Delete all comments for a task
     */
    void deleteByTaskId(UUID taskId);

    /**
     * Find comments by user within date range
     */
    @Query("SELECT tc FROM TaskComment tc WHERE tc.user.id = :userId " +
           "AND tc.createdAt >= :startTime AND tc.createdAt <= :endTime " +
           "ORDER BY tc.createdAt DESC")
    List<TaskComment> findByUserIdAndDateRange(@Param("userId") UUID userId,
                                              @Param("startTime") Instant startTime,
                                              @Param("endTime") Instant endTime);

    /**
     * Count comments by user ID
     */
    long countByUserId(UUID userId);
}