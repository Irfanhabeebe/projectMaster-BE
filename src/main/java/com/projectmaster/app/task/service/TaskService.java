package com.projectmaster.app.task.service;

import com.projectmaster.app.common.enums.ActivityType;
import com.projectmaster.app.common.enums.TaskPriority;
import com.projectmaster.app.common.enums.TaskStatus;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.task.dto.CreateTaskRequest;
import com.projectmaster.app.task.dto.TaskDto;
import com.projectmaster.app.task.dto.UpdateTaskRequest;
import com.projectmaster.app.task.entity.Task;
import com.projectmaster.app.task.entity.TaskActivityLog;
import com.projectmaster.app.task.entity.TaskDependency;
import com.projectmaster.app.task.repository.TaskActivityLogRepository;
import com.projectmaster.app.task.repository.TaskDependencyRepository;
import com.projectmaster.app.task.repository.TaskRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectStepRepository projectStepRepository;
    private final UserRepository userRepository;
    private final TaskActivityLogRepository activityLogRepository;
    private final TaskDependencyRepository dependencyRepository;

    /**
     * Create a new task
     */
    public TaskDto createTask(UUID userId, CreateTaskRequest request) {
        log.info("Creating new task for project step: {}", request.getProjectStepId());

        // Validate project step exists
        ProjectStep projectStep = projectStepRepository.findById(request.getProjectStepId())
                .orElseThrow(() -> new EntityNotFoundException("Project step not found with id: " + request.getProjectStepId()));

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Validate user belongs to the same company as the project step
        if (!user.getCompany().getId().equals(projectStep.getProjectTask().getProjectStage().getProject().getCompany().getId())) {
            throw new ProjectMasterException("User does not belong to the same company as the project step");
        }

        // Validate due date if provided
        if (request.getDueDate() != null && request.getDueDate().isBefore(LocalDate.now())) {
            throw new ProjectMasterException("Due date cannot be in the past");
        }

        // Validate assignee if provided
        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignee not found with id: " + request.getAssignedToId()));
            
            // Validate assignee belongs to the same company as the project step
            if (!assignee.getCompany().getId().equals(projectStep.getProjectTask().getProjectStage().getProject().getCompany().getId())) {
                throw new ProjectMasterException("Assignee does not belong to the same company as the project step");
            }
        }

        // Process tags
        String tags = null;
        if (request.getTagList() != null && !request.getTagList().isEmpty()) {
            tags = String.join(",", request.getTagList());
        } else if (request.getTags() != null) {
            tags = request.getTags();
        }

        // Create task entity
        Task task = Task.builder()
                .projectStep(projectStep)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(request.getStatus())
                .dueDate(request.getDueDate())
                .startDate(request.getStartDate())
                .estimatedHours(request.getEstimatedHours())
                .actualHours(request.getActualHours())
                .completionPercentage(request.getCompletionPercentage())
                .createdBy(user)
                .assignedTo(assignee)
                .tags(tags)
                .isMilestone(request.getIsMilestone())
                .storyPoints(request.getStoryPoints())
                .blockedReason(request.getBlockedReason())
                .lastActivityAt(Instant.now())
                .build();

        Task savedTask = taskRepository.save(task);

        // Log activity
        logActivity(savedTask, user, ActivityType.CREATED, null, null, "Task created");

        // Create dependencies if provided
        if (request.getDependsOnTaskIds() != null && !request.getDependsOnTaskIds().isEmpty()) {
            for (UUID dependsOnTaskId : request.getDependsOnTaskIds()) {
                Task dependsOnTask = taskRepository.findById(dependsOnTaskId)
                        .orElseThrow(() -> new EntityNotFoundException("Dependency task not found with id: " + dependsOnTaskId));
                
                // Validate no circular dependencies
                if (dependencyRepository.existsByTaskIdAndDependsOnTaskId(dependsOnTaskId, savedTask.getId())) {
                    throw new ProjectMasterException("Circular dependency detected");
                }

                TaskDependency dependency = TaskDependency.builder()
                        .task(savedTask)
                        .dependsOnTask(dependsOnTask)
                        .build();
                
                dependencyRepository.save(dependency);
            }
        }

        log.info("Task created successfully with id: {}", savedTask.getId());
        return convertToDto(savedTask);
    }

    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskDto getTaskById(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
        return convertToDto(task);
    }

    /**
     * Get all tasks for a project step with pagination
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByProjectStep(UUID projectStepId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByProjectStepId(projectStepId, pageable);
        return tasks.map(this::convertToDto);
    }

    /**
     * Get all tasks for a project with pagination
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByProject(UUID projectId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByProjectId(projectId, pageable);
        return tasks.map(this::convertToDto);
    }

    /**
     * Get tasks by status
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByStatus(TaskStatus status, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByStatus(status, pageable);
        return tasks.map(this::convertToDto);
    }

    /**
     * Get tasks by priority
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByPriority(TaskPriority priority, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByPriority(priority, pageable);
        return tasks.map(this::convertToDto);
    }

    /**
     * Get tasks assigned to a user
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByUser(UUID userId, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByCreatedById(userId, pageable);
        return tasks.map(this::convertToDto);
    }

    /**
     * Search tasks by project step with search term
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> searchTasks(UUID projectStepId, String searchTerm, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByProjectStepIdWithSearch(projectStepId, searchTerm, pageable);
        return tasks.map(this::convertToDto);
    }

    /**
     * Update task
     */
    public TaskDto updateTask(UUID taskId, UpdateTaskRequest request) {
        log.info("Updating task with id: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        // Update fields if provided
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            if (request.getDueDate().isBefore(LocalDate.now())) {
                throw new ProjectMasterException("Due date cannot be in the past");
            }
            task.setDueDate(request.getDueDate());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }
        if (request.getCompletionPercentage() != null) {
            task.setCompletionPercentage(request.getCompletionPercentage());
            
            // Auto-update status based on completion percentage
            if (request.getCompletionPercentage() == 100 && task.getStatus() != TaskStatus.COMPLETED) {
                task.setStatus(TaskStatus.COMPLETED);
            } else if (request.getCompletionPercentage() > 0 && task.getStatus() == TaskStatus.OPEN) {
                task.setStatus(TaskStatus.IN_PROGRESS);
            }
        }

        // Update project step if provided
        if (request.getProjectStepId() != null && !request.getProjectStepId().equals(task.getProjectStep().getId())) {
            ProjectStep newProjectStep = projectStepRepository.findById(request.getProjectStepId())
                    .orElseThrow(() -> new EntityNotFoundException("Project step not found with id: " + request.getProjectStepId()));
            
            // Validate the new project step belongs to the same company
            if (!newProjectStep.getProjectTask().getProjectStage().getProject().getCompany().getId()
                    .equals(task.getProjectStep().getProjectTask().getProjectStage().getProject().getCompany().getId())) {
                throw new ProjectMasterException("Cannot move task to a project step from a different company");
            }
            task.setProjectStep(newProjectStep);
        }

        Task updatedTask = taskRepository.save(task);
        log.info("Task updated successfully with id: {}", updatedTask.getId());

        return convertToDto(updatedTask);
    }

    /**
     * Delete task
     */
    public void deleteTask(UUID taskId) {
        log.info("Deleting task with id: {}", taskId);

        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException("Task not found with id: " + taskId);
        }

        taskRepository.deleteById(taskId);
        log.info("Task deleted successfully with id: {}", taskId);
    }

    /**
     * Get overdue tasks
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getOverdueTasks() {
        List<Task> overdueTasks = taskRepository.findOverdueTasks(LocalDate.now());
        return overdueTasks.stream().map(this::convertToDto).toList();
    }

    /**
     * Get high priority incomplete tasks
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getHighPriorityIncompleteTasks() {
        List<Task> highPriorityTasks = taskRepository.findHighPriorityIncompleteTasks();
        return highPriorityTasks.stream().map(this::convertToDto).toList();
    }

    /**
     * Get active tasks for a user
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getActiveTasksByUser(UUID userId) {
        List<Task> activeTasks = taskRepository.findActiveTasksByUserId(userId);
        return activeTasks.stream().map(this::convertToDto).toList();
    }

    /**
     * Get tasks requiring attention (overdue or high priority)
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksRequiringAttention() {
        List<Task> tasksRequiringAttention = taskRepository.findTasksRequiringAttention(LocalDate.now());
        return tasksRequiringAttention.stream().map(this::convertToDto).toList();
    }

    /**
     * Get task statistics for a project step
     */
    @Transactional(readOnly = true)
    public TaskStatistics getTaskStatistics(UUID projectStepId) {
        return TaskStatistics.builder()
                .totalTasks(taskRepository.countByProjectStepIdAndStatus(projectStepId, null))
                .openTasks(taskRepository.countByProjectStepIdAndStatus(projectStepId, TaskStatus.OPEN))
                .inProgressTasks(taskRepository.countByProjectStepIdAndStatus(projectStepId, TaskStatus.IN_PROGRESS))
                .completedTasks(taskRepository.countByProjectStepIdAndStatus(projectStepId, TaskStatus.COMPLETED))
                .cancelledTasks(taskRepository.countByProjectStepIdAndStatus(projectStepId, TaskStatus.CANCELLED))
                .totalEstimatedHours(taskRepository.sumEstimatedHoursByProjectStepId(projectStepId))
                .totalActualHours(taskRepository.sumActualHoursByProjectStepId(projectStepId))
                .build();
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
     * Task statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TaskStatistics {
        private Long totalTasks;
        private Long openTasks;
        private Long inProgressTasks;
        private Long completedTasks;
        private Long cancelledTasks;
        private Integer totalEstimatedHours;
        private Integer totalActualHours;
    }
}