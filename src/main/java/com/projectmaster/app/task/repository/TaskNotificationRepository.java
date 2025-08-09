package com.projectmaster.app.task.repository;

import com.projectmaster.app.common.enums.NotificationType;
import com.projectmaster.app.task.entity.TaskNotification;
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
public interface TaskNotificationRepository extends JpaRepository<TaskNotification, UUID> {

    /**
     * Find all notifications for a user
     */
    List<TaskNotification> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find notifications for a user with pagination
     */
    Page<TaskNotification> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find unread notifications for a user
     */
    List<TaskNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    /**
     * Find unread notifications for a user with pagination
     */
    Page<TaskNotification> findByUserIdAndIsReadFalse(UUID userId, Pageable pageable);

    /**
     * Find notifications for a task
     */
    List<TaskNotification> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    /**
     * Find notifications by type
     */
    List<TaskNotification> findByNotificationTypeOrderByCreatedAtDesc(NotificationType notificationType);

    /**
     * Find notifications for a user by type
     */
    List<TaskNotification> findByUserIdAndNotificationTypeOrderByCreatedAtDesc(UUID userId, NotificationType notificationType);

    /**
     * Find pending notifications (scheduled but not sent)
     */
    @Query("SELECT tn FROM TaskNotification tn WHERE tn.sentAt IS NULL AND " +
           "(tn.scheduledFor IS NULL OR tn.scheduledFor <= :currentTime)")
    List<TaskNotification> findPendingNotifications(@Param("currentTime") Instant currentTime);

    /**
     * Find scheduled notifications (not yet due)
     */
    @Query("SELECT tn FROM TaskNotification tn WHERE tn.sentAt IS NULL AND tn.scheduledFor > :currentTime")
    List<TaskNotification> findScheduledNotifications(@Param("currentTime") Instant currentTime);

    /**
     * Find sent notifications
     */
    @Query("SELECT tn FROM TaskNotification tn WHERE tn.sentAt IS NOT NULL ORDER BY tn.sentAt DESC")
    List<TaskNotification> findSentNotifications();

    /**
     * Find notifications for a project
     */
    @Query("SELECT tn FROM TaskNotification tn WHERE tn.task.projectStep.projectTask.projectStage.project.id = :projectId " +
           "ORDER BY tn.createdAt DESC")
    List<TaskNotification> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find notifications within date range
     */
    @Query("SELECT tn FROM TaskNotification tn WHERE tn.createdAt >= :startTime AND tn.createdAt <= :endTime " +
           "ORDER BY tn.createdAt DESC")
    List<TaskNotification> findByDateRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    /**
     * Find notifications for a user within date range
     */
    @Query("SELECT tn FROM TaskNotification tn WHERE tn.user.id = :userId " +
           "AND tn.createdAt >= :startTime AND tn.createdAt <= :endTime " +
           "ORDER BY tn.createdAt DESC")
    List<TaskNotification> findByUserIdAndDateRange(@Param("userId") UUID userId,
                                                   @Param("startTime") Instant startTime,
                                                   @Param("endTime") Instant endTime);

    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndIsReadFalse(UUID userId);

    /**
     * Count notifications for a task
     */
    long countByTaskId(UUID taskId);

    /**
     * Count notifications by type for a user
     */
    long countByUserIdAndNotificationType(UUID userId, NotificationType notificationType);

    /**
     * Find overdue scheduled notifications
     */
    @Query("SELECT tn FROM TaskNotification tn WHERE tn.sentAt IS NULL AND tn.scheduledFor < :currentTime")
    List<TaskNotification> findOverdueScheduledNotifications(@Param("currentTime") Instant currentTime);

    /**
     * Mark notifications as read for a user
     */
    @Query("UPDATE TaskNotification tn SET tn.isRead = true WHERE tn.user.id = :userId AND tn.isRead = false")
    void markAllAsReadForUser(@Param("userId") UUID userId);

    /**
     * Mark notifications as read for a task
     */
    @Query("UPDATE TaskNotification tn SET tn.isRead = true WHERE tn.task.id = :taskId AND tn.isRead = false")
    void markAllAsReadForTask(@Param("taskId") UUID taskId);

    /**
     * Delete old notifications (older than specified date)
     */
    @Query("DELETE FROM TaskNotification tn WHERE tn.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Delete all notifications for a task
     */
    void deleteByTaskId(UUID taskId);

    /**
     * Find notifications containing specific text
     */
    @Query("SELECT tn FROM TaskNotification tn WHERE tn.user.id = :userId AND " +
           "(LOWER(tn.title) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(tn.message) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    List<TaskNotification> findByUserIdAndTextSearch(@Param("userId") UUID userId, 
                                                    @Param("searchText") String searchText);
}