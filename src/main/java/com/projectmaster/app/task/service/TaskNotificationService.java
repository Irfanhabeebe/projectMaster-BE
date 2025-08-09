package com.projectmaster.app.task.service;

import com.projectmaster.app.common.enums.NotificationType;
import com.projectmaster.app.common.enums.TaskStatus;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.task.entity.Task;
import com.projectmaster.app.task.entity.TaskNotification;
import com.projectmaster.app.task.repository.TaskNotificationRepository;
import com.projectmaster.app.task.repository.TaskRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskNotificationService {

    private final TaskNotificationRepository notificationRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Get notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<TaskNotification> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<TaskNotification> getUnreadNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId, pageable);
    }

    /**
     * Get unread notification count for a user
     */
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(UUID notificationId, UUID userId) {
        TaskNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityException("User can only mark their own notifications as read");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
        log.info("Notification {} marked as read by user {}", notificationId, userId);
    }

    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadForUser(userId);
        log.info("All notifications marked as read for user {}", userId);
    }

    /**
     * Create a notification
     */
    @Async
    public void createNotification(UUID taskId, UUID userId, NotificationType type, 
                                 String title, String message) {
        createNotification(taskId, userId, type, title, message, null);
    }

    /**
     * Create a scheduled notification
     */
    @Async
    public void createNotification(UUID taskId, UUID userId, NotificationType type, 
                                 String title, String message, Instant scheduledFor) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            TaskNotification notification = TaskNotification.builder()
                    .task(task)
                    .user(user)
                    .notificationType(type)
                    .title(title)
                    .message(message)
                    .scheduledFor(scheduledFor)
                    .build();

            notificationRepository.save(notification);
            log.info("Notification created for user {} about task {}", userId, taskId);
        } catch (Exception e) {
            log.error("Failed to create notification for user {} about task {}: {}", userId, taskId, e.getMessage());
        }
    }

    /**
     * Send pending notifications (scheduled job)
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void sendPendingNotifications() {
        List<TaskNotification> pendingNotifications = notificationRepository.findPendingNotifications(Instant.now());
        
        for (TaskNotification notification : pendingNotifications) {
            try {
                // Mark as sent (in a real implementation, you would send email/push notification here)
                notification.markAsSent();
                notificationRepository.save(notification);
                log.info("Notification {} sent to user {}", notification.getId(), notification.getUser().getId());
            } catch (Exception e) {
                log.error("Failed to send notification {}: {}", notification.getId(), e.getMessage());
            }
        }
    }

    /**
     * Check for overdue tasks and create notifications (scheduled job)
     */
    @Scheduled(cron = "0 0 9 * * ?") // Run daily at 9 AM
    public void checkOverdueTasks() {
        log.info("Checking for overdue tasks...");
        
        List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDate.now());
        
        for (Task task : overdueTasks) {
            if (task.getAssignedTo() != null) {
                // Check if we already sent an overdue notification today
                List<TaskNotification> existingNotifications = notificationRepository
                        .findByUserIdAndDateRange(
                                task.getAssignedTo().getId(),
                                Instant.now().truncatedTo(ChronoUnit.DAYS),
                                Instant.now()
                        );
                
                boolean alreadyNotified = existingNotifications.stream()
                        .anyMatch(n -> n.getNotificationType() == NotificationType.TASK_OVERDUE 
                                && n.getTask().getId().equals(task.getId()));
                
                if (!alreadyNotified) {
                    createNotification(
                            task.getId(),
                            task.getAssignedTo().getId(),
                            NotificationType.TASK_OVERDUE,
                            "Task Overdue: " + task.getTitle(),
                            "Your task '" + task.getTitle() + "' is overdue. Due date was: " + task.getDueDate()
                    );
                }
            }
        }
        
        log.info("Overdue task check completed. Found {} overdue tasks", overdueTasks.size());
    }

    /**
     * Check for tasks due soon and create notifications (scheduled job)
     */
    @Scheduled(cron = "0 0 8 * * ?") // Run daily at 8 AM
    public void checkTasksDueSoon() {
        log.info("Checking for tasks due soon...");
        
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterTomorrow = LocalDate.now().plusDays(2);
        
        List<Task> tasksDueSoon = taskRepository.findTasksDueWithinDays(tomorrow, dayAfterTomorrow);
        
        for (Task task : tasksDueSoon) {
            if (task.getAssignedTo() != null && task.getStatus() != TaskStatus.COMPLETED) {
                // Check if we already sent a due soon notification for this task
                List<TaskNotification> existingNotifications = notificationRepository
                        .findByUserIdAndDateRange(
                                task.getAssignedTo().getId(),
                                Instant.now().minus(7, ChronoUnit.DAYS),
                                Instant.now()
                        );
                
                boolean alreadyNotified = existingNotifications.stream()
                        .anyMatch(n -> n.getNotificationType() == NotificationType.TASK_DUE_SOON 
                                && n.getTask().getId().equals(task.getId()));
                
                if (!alreadyNotified) {
                    String daysUntilDue = task.getDueDate().equals(tomorrow) ? "tomorrow" : "in 2 days";
                    createNotification(
                            task.getId(),
                            task.getAssignedTo().getId(),
                            NotificationType.TASK_DUE_SOON,
                            "Task Due Soon: " + task.getTitle(),
                            "Your task '" + task.getTitle() + "' is due " + daysUntilDue + " (" + task.getDueDate() + ")"
                    );
                }
            }
        }
        
        log.info("Due soon task check completed. Found {} tasks due soon", tasksDueSoon.size());
    }

    /**
     * Check for milestone tasks approaching and create notifications
     */
    @Scheduled(cron = "0 0 10 * * ?") // Run daily at 10 AM
    public void checkMilestonesApproaching() {
        log.info("Checking for approaching milestones...");
        
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        List<Task> milestones = taskRepository.findMilestonesByProjectId(null); // Get all milestones
        
        for (Task milestone : milestones) {
            if (milestone.getDueDate() != null && 
                milestone.getDueDate().isBefore(nextWeek) && 
                milestone.getDueDate().isAfter(LocalDate.now()) &&
                milestone.getStatus() != TaskStatus.COMPLETED) {
                
                // Notify project team members (for now, just the assigned user and creator)
                if (milestone.getAssignedTo() != null) {
                    createNotification(
                            milestone.getId(),
                            milestone.getAssignedTo().getId(),
                            NotificationType.MILESTONE_APPROACHING,
                            "Milestone Approaching: " + milestone.getTitle(),
                            "Milestone '" + milestone.getTitle() + "' is due on " + milestone.getDueDate()
                    );
                }
                
                if (!milestone.getCreatedBy().getId().equals(milestone.getAssignedTo() != null ? milestone.getAssignedTo().getId() : null)) {
                    createNotification(
                            milestone.getId(),
                            milestone.getCreatedBy().getId(),
                            NotificationType.MILESTONE_APPROACHING,
                            "Milestone Approaching: " + milestone.getTitle(),
                            "Milestone '" + milestone.getTitle() + "' is due on " + milestone.getDueDate()
                    );
                }
            }
        }
        
        log.info("Milestone check completed");
    }

    /**
     * Clean up old notifications (scheduled job)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOldNotifications() {
        log.info("Cleaning up old notifications...");
        
        // Delete notifications older than 30 days
        Instant cutoffDate = Instant.now().minus(30, ChronoUnit.DAYS);
        notificationRepository.deleteOldNotifications(cutoffDate);
        
        log.info("Old notifications cleanup completed");
    }

    /**
     * Delete notification
     */
    public void deleteNotification(UUID notificationId, UUID userId) {
        TaskNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityException("User can only delete their own notifications");
        }

        notificationRepository.delete(notification);
        log.info("Notification {} deleted by user {}", notificationId, userId);
    }
}