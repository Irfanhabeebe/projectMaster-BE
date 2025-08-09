package com.projectmaster.app.task.repository;

import com.projectmaster.app.common.enums.ActivityType;
import com.projectmaster.app.task.entity.TaskActivityLog;
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
public interface TaskActivityLogRepository extends JpaRepository<TaskActivityLog, UUID> {

    /**
     * Find all activity logs for a task ordered by creation time
     */
    List<TaskActivityLog> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    /**
     * Find activity logs for a task with pagination
     */
    Page<TaskActivityLog> findByTaskId(UUID taskId, Pageable pageable);

    /**
     * Find all activity logs by a user
     */
    List<TaskActivityLog> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find activity logs by user with pagination
     */
    Page<TaskActivityLog> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find activity logs by activity type
     */
    List<TaskActivityLog> findByActivityTypeOrderByCreatedAtDesc(ActivityType activityType);

    /**
     * Find activity logs for a task by activity type
     */
    List<TaskActivityLog> findByTaskIdAndActivityTypeOrderByCreatedAtDesc(UUID taskId, ActivityType activityType);

    /**
     * Find activity logs for a project
     */
    @Query("SELECT tal FROM TaskActivityLog tal WHERE tal.task.projectStep.projectTask.projectStage.project.id = :projectId " +
           "ORDER BY tal.createdAt DESC")
    List<TaskActivityLog> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find activity logs for a project with pagination
     */
    @Query("SELECT tal FROM TaskActivityLog tal WHERE tal.task.projectStep.projectTask.projectStage.project.id = :projectId")
    Page<TaskActivityLog> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Find recent activity logs for a task (within last N days)
     */
    @Query("SELECT tal FROM TaskActivityLog tal WHERE tal.task.id = :taskId AND tal.createdAt >= :since " +
           "ORDER BY tal.createdAt DESC")
    List<TaskActivityLog> findRecentByTaskId(@Param("taskId") UUID taskId, @Param("since") Instant since);

    /**
     * Find activity logs within date range
     */
    @Query("SELECT tal FROM TaskActivityLog tal WHERE tal.createdAt >= :startTime AND tal.createdAt <= :endTime " +
           "ORDER BY tal.createdAt DESC")
    List<TaskActivityLog> findByDateRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    /**
     * Find activity logs for a user within date range
     */
    @Query("SELECT tal FROM TaskActivityLog tal WHERE tal.user.id = :userId " +
           "AND tal.createdAt >= :startTime AND tal.createdAt <= :endTime " +
           "ORDER BY tal.createdAt DESC")
    List<TaskActivityLog> findByUserIdAndDateRange(@Param("userId") UUID userId,
                                                  @Param("startTime") Instant startTime,
                                                  @Param("endTime") Instant endTime);

    /**
     * Find activity logs for a project within date range
     */
    @Query("SELECT tal FROM TaskActivityLog tal WHERE tal.task.projectStep.projectTask.projectStage.project.id = :projectId " +
           "AND tal.createdAt >= :startTime AND tal.createdAt <= :endTime " +
           "ORDER BY tal.createdAt DESC")
    List<TaskActivityLog> findByProjectIdAndDateRange(@Param("projectId") UUID projectId,
                                                     @Param("startTime") Instant startTime,
                                                     @Param("endTime") Instant endTime);

    /**
     * Count activity logs for a task
     */
    long countByTaskId(UUID taskId);

    /**
     * Count activity logs by type for a task
     */
    long countByTaskIdAndActivityType(UUID taskId, ActivityType activityType);

    /**
     * Find most active users (by activity count)
     */
    @Query("SELECT tal.user.id, COUNT(tal) as activityCount FROM TaskActivityLog tal " +
           "GROUP BY tal.user.id ORDER BY activityCount DESC")
    List<Object[]> findMostActiveUsers();

    /**
     * Find most active tasks (by activity count)
     */
    @Query("SELECT tal.task.id, COUNT(tal) as activityCount FROM TaskActivityLog tal " +
           "GROUP BY tal.task.id ORDER BY activityCount DESC")
    List<Object[]> findMostActiveTasks();

    /**
     * Delete all activity logs for a task
     */
    void deleteByTaskId(UUID taskId);

    /**
     * Find activity logs containing specific text in description
     */
    @Query("SELECT tal FROM TaskActivityLog tal WHERE tal.task.id = :taskId AND " +
           "(LOWER(tal.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(tal.oldValue) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(tal.newValue) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<TaskActivityLog> findByTaskIdAndTextSearch(@Param("taskId") UUID taskId, 
                                                   @Param("searchText") String searchText);
}