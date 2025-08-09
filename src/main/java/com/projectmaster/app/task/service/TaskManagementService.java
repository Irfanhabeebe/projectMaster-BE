package com.projectmaster.app.task.service;

import com.projectmaster.app.common.enums.ActivityType;
import com.projectmaster.app.common.enums.NotificationType;
import com.projectmaster.app.common.enums.TaskStatus;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.task.dto.*;
import com.projectmaster.app.task.entity.*;
import com.projectmaster.app.task.repository.*;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskManagementService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskTimeEntryRepository timeEntryRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskActivityLogRepository activityLogRepository;
    private final TaskNotificationRepository notificationRepository;
    private final TaskDependencyRepository dependencyRepository;

    /**
     * Assign a task to a user
     */
    public TaskDto assignTask(UUID taskId, UUID assigneeId, UUID assignedById) {
        log.info("Assigning task {} to user {}", taskId, assigneeId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + assigneeId));

        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + assignedById));

        // Validate assignee belongs to the same company as the task
        if (!assignee.getCompany().getId().equals(task.getProjectStep().getProjectTask().getProjectStage().getProject().getCompany().getId())) {
            throw new ProjectMasterException("Cannot assign task to user from different company");
        }

        String oldAssignee = task.getAssignedTo() != null ? task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName() : null;
        String newAssignee = assignee.getFirstName() + " " + assignee.getLastName();

        task.setAssignedTo(assignee);
        task.setLastActivityAt(Instant.now());

        Task savedTask = taskRepository.save(task);

        // Log activity
        logActivity(task, assignedBy, ActivityType.ASSIGNED, oldAssignee, newAssignee, 
                   "Task assigned to " + newAssignee);

        // Create notification for assignee
        createNotification(task, assignee, NotificationType.TASK_ASSIGNED,
                          "Task Assigned: " + task.getTitle(),
                          "You have been assigned to task: " + task.getTitle());

        log.info("Task {} assigned successfully to user {}", taskId, assigneeId);
        return convertToDto(savedTask);
    }

    /**
     * Unassign a task
     */
    public TaskDto unassignTask(UUID taskId, UUID unassignedById) {
        log.info("Unassigning task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        User unassignedBy = userRepository.findById(unassignedById)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + unassignedById));

        if (task.getAssignedTo() == null) {
            throw new ProjectMasterException("Task is not assigned to anyone");
        }

        String oldAssignee = task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName();
        task.setAssignedTo(null);
        task.setLastActivityAt(Instant.now());

        Task savedTask = taskRepository.save(task);

        // Log activity
        logActivity(task, unassignedBy, ActivityType.UNASSIGNED, oldAssignee, null, 
                   "Task unassigned from " + oldAssignee);

        log.info("Task {} unassigned successfully", taskId);
        return convertToDto(savedTask);
    }

    /**
     * Start time tracking for a task
     */
    public TaskTimeEntryDto startTimeTracking(StartTimeEntryRequest request, UUID userId) {
        log.info("Starting time tracking for task {} by user {}", request.getTaskId(), userId);

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + request.getTaskId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Check if user already has an active time entry for this task
        if (timeEntryRepository.findByTaskIdAndUserIdAndEndTimeIsNull(request.getTaskId(), userId).isPresent()) {
            throw new ProjectMasterException("User already has an active time entry for this task");
        }

        TaskTimeEntry timeEntry = TaskTimeEntry.builder()
                .task(task)
                .user(user)
                .description(request.getDescription())
                .startTime(Instant.now())
                .isBillable(request.getIsBillable())
                .build();

        TaskTimeEntry savedEntry = timeEntryRepository.save(timeEntry);

        // Log activity
        logActivity(task, user, ActivityType.TIME_LOGGED, null, null, 
                   "Started time tracking");

        task.setLastActivityAt(Instant.now());
        taskRepository.save(task);

        log.info("Time tracking started successfully for task {}", request.getTaskId());
        return convertToTimeEntryDto(savedEntry);
    }

    /**
     * Stop time tracking for a task
     */
    public TaskTimeEntryDto stopTimeTracking(UUID taskId, UUID userId) {
        log.info("Stopping time tracking for task {} by user {}", taskId, userId);

        TaskTimeEntry timeEntry = timeEntryRepository.findByTaskIdAndUserIdAndEndTimeIsNull(taskId, userId)
                .orElseThrow(() -> new EntityNotFoundException("No active time entry found for this task and user"));

        timeEntry.stopTimer();
        TaskTimeEntry savedEntry = timeEntryRepository.save(timeEntry);

        // Update task's actual hours
        Task task = timeEntry.getTask();
        Integer totalMinutes = timeEntryRepository.sumDurationMinutesByTaskId(taskId);
        task.setActualHours((int) Math.ceil(totalMinutes / 60.0));
        task.setLastActivityAt(Instant.now());
        taskRepository.save(task);

        // Log activity
        logActivity(task, timeEntry.getUser(), ActivityType.TIME_LOGGED, null, 
                   savedEntry.getDurationHours() + " hours", 
                   "Logged " + savedEntry.getDurationHours() + " hours");

        log.info("Time tracking stopped successfully for task {}", taskId);
        return convertToTimeEntryDto(savedEntry);
    }

    /**
     * Add a comment to a task
     */
    public TaskCommentDto addComment(AddCommentRequest request, UUID userId) {
        log.info("Adding comment to task {} by user {}", request.getTaskId(), userId);

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + request.getTaskId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(user)
                .comment(request.getComment())
                .isInternal(request.getIsInternal())
                .build();

        TaskComment savedComment = commentRepository.save(comment);

        // Log activity
        logActivity(task, user, ActivityType.COMMENT_ADDED, null, null, 
                   "Added comment: " + (request.getComment().length() > 50 ? 
                   request.getComment().substring(0, 50) + "..." : request.getComment()));

        task.setLastActivityAt(Instant.now());
        taskRepository.save(task);

        // Notify assigned user if different from commenter
        if (task.getAssignedTo() != null && !task.getAssignedTo().getId().equals(userId)) {
            createNotification(task, task.getAssignedTo(), NotificationType.TASK_COMMENTED,
                              "New Comment: " + task.getTitle(),
                              user.getFirstName() + " " + user.getLastName() + " commented on your task");
        }

        log.info("Comment added successfully to task {}", request.getTaskId());
        return convertToCommentDto(savedComment);
    }

    /**
     * Block a task with reason
     */
    public TaskDto blockTask(UUID taskId, String reason, UUID blockedById) {
        log.info("Blocking task {} with reason: {}", taskId, reason);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        User blockedBy = userRepository.findById(blockedById)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + blockedById));

        task.setBlockedReason(reason);
        task.setLastActivityAt(Instant.now());

        Task savedTask = taskRepository.save(task);

        // Log activity
        logActivity(task, blockedBy, ActivityType.BLOCKED, null, reason, 
                   "Task blocked: " + reason);

        // Notify assigned user
        if (task.getAssignedTo() != null) {
            createNotification(task, task.getAssignedTo(), NotificationType.TASK_UPDATED,
                              "Task Blocked: " + task.getTitle(),
                              "Your task has been blocked: " + reason);
        }

        log.info("Task {} blocked successfully", taskId);
        return convertToDto(savedTask);
    }

    /**
     * Unblock a task
     */
    public TaskDto unblockTask(UUID taskId, UUID unblockedById) {
        log.info("Unblocking task {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        User unblockedBy = userRepository.findById(unblockedById)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + unblockedById));

        if (!task.isBlocked()) {
            throw new ProjectMasterException("Task is not blocked");
        }

        String oldReason = task.getBlockedReason();
        task.setBlockedReason(null);
        task.setLastActivityAt(Instant.now());

        Task savedTask = taskRepository.save(task);

        // Log activity
        logActivity(task, unblockedBy, ActivityType.UNBLOCKED, oldReason, null, 
                   "Task unblocked");

        // Notify assigned user
        if (task.getAssignedTo() != null) {
            createNotification(task, task.getAssignedTo(), NotificationType.TASK_UPDATED,
                              "Task Unblocked: " + task.getTitle(),
                              "Your task has been unblocked and is ready to continue");
        }

        log.info("Task {} unblocked successfully", taskId);
        return convertToDto(savedTask);
    }

    /**
     * Update task progress
     */
    public TaskDto updateProgress(UUID taskId, Integer completionPercentage, UUID updatedById) {
        log.info("Updating progress for task {} to {}%", taskId, completionPercentage);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        User updatedBy = userRepository.findById(updatedById)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + updatedById));

        Integer oldPercentage = task.getCompletionPercentage();
        task.setCompletionPercentage(completionPercentage);

        // Auto-update status based on completion percentage
        TaskStatus oldStatus = task.getStatus();
        if (completionPercentage == 100 && task.getStatus() != TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.COMPLETED);
        } else if (completionPercentage > 0 && task.getStatus() == TaskStatus.OPEN) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        task.setLastActivityAt(Instant.now());
        Task savedTask = taskRepository.save(task);

        // Log activity
        logActivity(task, updatedBy, ActivityType.PROGRESS_UPDATED, 
                   oldPercentage + "%", completionPercentage + "%", 
                   "Progress updated to " + completionPercentage + "%");

        // Log status change if it occurred
        if (!oldStatus.equals(task.getStatus())) {
            logActivity(task, updatedBy, ActivityType.STATUS_CHANGED, 
                       oldStatus.getDisplayName(), task.getStatus().getDisplayName(), 
                       "Status changed to " + task.getStatus().getDisplayName());
        }

        // Notify on completion
        if (completionPercentage == 100 && task.getAssignedTo() != null) {
            createNotification(task, task.getCreatedBy(), NotificationType.TASK_COMPLETED,
                              "Task Completed: " + task.getTitle(),
                              "Task has been completed by " + task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName());
        }

        log.info("Progress updated successfully for task {}", taskId);
        return convertToDto(savedTask);
    }

    /**
     * Log activity for a task
     */
    private void logActivity(Task task, User user, ActivityType activityType, 
                           String oldValue, String newValue, String description) {
        TaskActivityLog activityLog = TaskActivityLog.builder()
                .task(task)
                .user(user)
                .activityType(activityType)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .build();

        activityLogRepository.save(activityLog);
    }

    /**
     * Create a notification
     */
    private void createNotification(Task task, User user, NotificationType type, 
                                  String title, String message) {
        TaskNotification notification = TaskNotification.builder()
                .task(task)
                .user(user)
                .notificationType(type)
                .title(title)
                .message(message)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Convert Task entity to DTO
     */
    private TaskDto convertToDto(Task task) {
        List<String> tagList = task.getTags() != null ? 
                Arrays.asList(task.getTags().split(",")).stream()
                        .map(String::trim)
                        .filter(tag -> !tag.isEmpty())
                        .collect(Collectors.toList()) : List.of();

        return TaskDto.builder()
                .id(task.getId())
                .projectStepId(task.getProjectStep().getId())
                .projectStepName(task.getProjectStep().getName())
                .projectName(task.getProjectStep().getProjectTask().getProjectStage().getProject().getName())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .startDate(task.getStartDate())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .completionPercentage(task.getCompletionPercentage())
                .createdById(task.getCreatedBy().getId())
                .createdByName(task.getCreatedBy().getFirstName() + " " + task.getCreatedBy().getLastName())
                .assignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .assignedToName(task.getAssignedTo() != null ? 
                        task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName() : null)
                .tags(task.getTags())
                .tagList(tagList)
                .isMilestone(task.getIsMilestone())
                .storyPoints(task.getStoryPoints())
                .blockedReason(task.getBlockedReason())
                .lastActivityAt(task.getLastActivityAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .isOverdue(task.isOverdue())
                .isBlocked(task.isBlocked())
                .isAssigned(task.isAssigned())
                .totalLoggedMinutes(task.getTotalLoggedMinutes())
                .totalLoggedHours(task.getTotalLoggedHours())
                .dependencyCount(task.getDependencies().size())
                .commentCount(task.getComments().size())
                .attachmentCount(task.getAttachments().size())
                .build();
    }

    /**
     * Convert TaskTimeEntry entity to DTO
     */
    private TaskTimeEntryDto convertToTimeEntryDto(TaskTimeEntry timeEntry) {
        return TaskTimeEntryDto.builder()
                .id(timeEntry.getId())
                .taskId(timeEntry.getTask().getId())
                .taskTitle(timeEntry.getTask().getTitle())
                .userId(timeEntry.getUser().getId())
                .userName(timeEntry.getUser().getFirstName() + " " + timeEntry.getUser().getLastName())
                .description(timeEntry.getDescription())
                .startTime(timeEntry.getStartTime())
                .endTime(timeEntry.getEndTime())
                .durationMinutes(timeEntry.getDurationMinutes())
                .durationHours(timeEntry.getDurationHours())
                .isBillable(timeEntry.getIsBillable())
                .isActive(timeEntry.isActive())
                .createdAt(timeEntry.getCreatedAt())
                .updatedAt(timeEntry.getUpdatedAt())
                .build();
    }

    /**
     * Convert TaskComment entity to DTO
     */
    private TaskCommentDto convertToCommentDto(TaskComment comment) {
        return TaskCommentDto.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .taskTitle(comment.getTask().getTitle())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName())
                .comment(comment.getComment())
                .isInternal(comment.getIsInternal())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}