package com.projectmaster.app.task.service;

import com.projectmaster.app.common.enums.TaskPriority;
import com.projectmaster.app.common.enums.TaskStatus;
import com.projectmaster.app.task.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TaskReportingService {

    private final TaskRepository taskRepository;
    private final TaskTimeEntryRepository timeEntryRepository;
    private final TaskCommentRepository commentRepository;
    private final TaskActivityLogRepository activityLogRepository;
    private final TaskNotificationRepository notificationRepository;

    /**
     * Get comprehensive task statistics for a project
     */
    public ProjectTaskStatistics getProjectTaskStatistics(UUID projectId) {
        log.info("Generating task statistics for project {}", projectId);

        return ProjectTaskStatistics.builder()
                .projectId(projectId)
                .totalTasks(taskRepository.countByProjectId(projectId))
                .openTasks(taskRepository.findByProjectIdAndStatus(projectId, TaskStatus.OPEN).size())
                .inProgressTasks(taskRepository.findByProjectIdAndStatus(projectId, TaskStatus.IN_PROGRESS).size())
                .completedTasks(taskRepository.findByProjectIdAndStatus(projectId, TaskStatus.COMPLETED).size())
                .cancelledTasks(taskRepository.findByProjectIdAndStatus(projectId, TaskStatus.CANCELLED).size())
                .overdueTasks(taskRepository.findOverdueTasks(LocalDate.now()).stream()
                        .filter(task -> task.getProjectStep().getProjectTask().getProjectStage().getProject().getId().equals(projectId))
                        .collect(Collectors.toList()).size())
                .blockedTasks((int) taskRepository.countBlockedTasks())
                .milestones(taskRepository.findMilestonesByProjectId(projectId).size())
                .totalEstimatedHours(taskRepository.findByProjectId(projectId).stream()
                        .mapToInt(task -> task.getEstimatedHours() != null ? task.getEstimatedHours() : 0)
                        .sum())
                .totalActualHours(taskRepository.findByProjectId(projectId).stream()
                        .mapToInt(task -> task.getActualHours() != null ? task.getActualHours() : 0)
                        .sum())
                .totalLoggedMinutes(timeEntryRepository.sumDurationMinutesByProjectId(projectId))
                .averageCompletionPercentage(taskRepository.findByProjectId(projectId).stream()
                        .mapToInt(task -> task.getCompletionPercentage() != null ? task.getCompletionPercentage() : 0)
                        .average().orElse(0.0))
                .tasksByPriority(getTaskCountByPriority(projectId))
                .tasksByStatus(getTaskCountByStatus(projectId))
                .build();
    }

    /**
     * Get user productivity statistics
     */
    public UserProductivityStatistics getUserProductivityStatistics(UUID userId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating productivity statistics for user {} from {} to {}", userId, startDate, endDate);

        Instant startInstant = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);

        List<Object[]> timeEntries = timeEntryRepository.findByUserIdAndDateRange(userId, startInstant, endInstant)
                .stream()
                .map(entry -> new Object[]{entry.getTask().getId(), entry.getDurationMinutes()})
                .collect(Collectors.toList());

        return UserProductivityStatistics.builder()
                .userId(userId)
                .startDate(startDate)
                .endDate(endDate)
                .assignedTasks(taskRepository.countByAssignedToId(userId))
                .completedTasks(taskRepository.findByAssignedToIdAndStatus(userId, TaskStatus.COMPLETED).size())
                .totalLoggedMinutes(timeEntryRepository.sumDurationMinutesByUserId(userId))
                .totalLoggedHours(timeEntryRepository.sumDurationMinutesByUserId(userId) / 60.0)
                .billableMinutes(timeEntryRepository.sumBillableMinutesByUserIdAndDateRange(userId, startInstant, endInstant))
                .billableHours(timeEntryRepository.sumBillableMinutesByUserIdAndDateRange(userId, startInstant, endInstant) / 60.0)
                .averageTaskCompletionTime(calculateAverageTaskCompletionTime(userId))
                .tasksCompletedOnTime(calculateTasksCompletedOnTime(userId))
                .tasksCompletedLate(calculateTasksCompletedLate(userId))
                .commentsAdded(commentRepository.countByUserId(userId))
                .activeDays(calculateActiveDays(userId, startDate, endDate))
                .productivityScore(calculateProductivityScore(userId, startDate, endDate))
                .build();
    }

    /**
     * Get time tracking report
     */
    public TimeTrackingReport getTimeTrackingReport(UUID projectId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating time tracking report for project {} from {} to {}", projectId, startDate, endDate);

        Instant startInstant = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);

        return TimeTrackingReport.builder()
                .projectId(projectId)
                .startDate(startDate)
                .endDate(endDate)
                .totalLoggedMinutes(timeEntryRepository.sumDurationMinutesByProjectId(projectId))
                .totalLoggedHours(timeEntryRepository.sumDurationMinutesByProjectId(projectId) / 60.0)
                .billableMinutes(timeEntryRepository.findBillableByProjectId(projectId).stream()
                        .mapToInt(entry -> entry.getDurationMinutes() != null ? entry.getDurationMinutes() : 0)
                        .sum())
                .nonBillableMinutes(timeEntryRepository.findByProjectIdAndDateRange(projectId, startInstant, endInstant).stream()
                        .filter(entry -> !entry.getIsBillable())
                        .mapToInt(entry -> entry.getDurationMinutes() != null ? entry.getDurationMinutes() : 0)
                        .sum())
                .timeEntriesCount(timeEntryRepository.findByProjectIdAndDateRange(projectId, startInstant, endInstant).size())
                .uniqueUsers(timeEntryRepository.findByProjectIdAndDateRange(projectId, startInstant, endInstant).stream()
                        .map(entry -> entry.getUser().getId())
                        .collect(Collectors.toSet()).size())
                .averageSessionDuration(calculateAverageSessionDuration(projectId, startDate, endDate))
                .timeByUser(getTimeByUser(projectId, startDate, endDate))
                .timeByTask(getTimeByTask(projectId, startDate, endDate))
                .dailyTimeBreakdown(getDailyTimeBreakdown(projectId, startDate, endDate))
                .build();
    }

    /**
     * Get task completion trends
     */
    public TaskCompletionTrends getTaskCompletionTrends(UUID projectId, int days) {
        log.info("Generating task completion trends for project {} over {} days", projectId, days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        Map<LocalDate, Integer> completionsByDate = new LinkedHashMap<>();
        Map<LocalDate, Integer> creationsByDate = new LinkedHashMap<>();

        // Initialize maps with zero values
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            completionsByDate.put(date, 0);
            creationsByDate.put(date, 0);
        }

        // Get completed tasks and group by completion date
        taskRepository.findByProjectIdAndStatus(projectId, TaskStatus.COMPLETED).forEach(task -> {
            LocalDate completionDate = task.getUpdatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
            if (!completionDate.isBefore(startDate) && !completionDate.isAfter(endDate)) {
                completionsByDate.merge(completionDate, 1, Integer::sum);
            }
        });

        // Get created tasks and group by creation date
        taskRepository.findByProjectId(projectId).forEach(task -> {
            LocalDate creationDate = task.getCreatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
            if (!creationDate.isBefore(startDate) && !creationDate.isAfter(endDate)) {
                creationsByDate.merge(creationDate, 1, Integer::sum);
            }
        });

        return TaskCompletionTrends.builder()
                .projectId(projectId)
                .startDate(startDate)
                .endDate(endDate)
                .completionsByDate(completionsByDate)
                .creationsByDate(creationsByDate)
                .averageCompletionsPerDay(completionsByDate.values().stream().mapToInt(Integer::intValue).average().orElse(0.0))
                .averageCreationsPerDay(creationsByDate.values().stream().mapToInt(Integer::intValue).average().orElse(0.0))
                .totalCompletions(completionsByDate.values().stream().mapToInt(Integer::intValue).sum())
                .totalCreations(creationsByDate.values().stream().mapToInt(Integer::intValue).sum())
                .build();
    }

    /**
     * Get team performance metrics
     */
    public TeamPerformanceMetrics getTeamPerformanceMetrics(UUID projectId) {
        log.info("Generating team performance metrics for project {}", projectId);

        List<Object[]> userStats = taskRepository.findByProjectId(projectId).stream()
                .filter(task -> task.getAssignedTo() != null)
                .collect(Collectors.groupingBy(task -> task.getAssignedTo().getId()))
                .entrySet().stream()
                .map(entry -> {
                    UUID userId = entry.getKey();
                    List<com.projectmaster.app.task.entity.Task> userTasks = entry.getValue();
                    
                    long completedTasks = userTasks.stream().filter(task -> task.getStatus() == TaskStatus.COMPLETED).count();
                    long totalTasks = userTasks.size();
                    double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
                    
                    return new Object[]{userId, totalTasks, completedTasks, completionRate};
                })
                .collect(Collectors.toList());

        return TeamPerformanceMetrics.builder()
                .projectId(projectId)
                .totalTeamMembers(userStats.size())
                .averageTasksPerMember(userStats.stream().mapToLong(stat -> (Long) stat[1]).average().orElse(0.0))
                .averageCompletionRate(userStats.stream().mapToDouble(stat -> (Double) stat[3]).average().orElse(0.0))
                .topPerformers(getTopPerformers(userStats))
                .teamProductivityScore(calculateTeamProductivityScore(projectId))
                .collaborationScore(calculateCollaborationScore(projectId))
                .userPerformanceBreakdown(getUserPerformanceBreakdown(userStats))
                .build();
    }

    // Helper methods

    private Map<TaskPriority, Integer> getTaskCountByPriority(UUID projectId) {
        Map<TaskPriority, Integer> counts = new EnumMap<>(TaskPriority.class);
        for (TaskPriority priority : TaskPriority.values()) {
            counts.put(priority, taskRepository.findByProjectId(projectId).stream()
                    .filter(task -> task.getPriority() == priority)
                    .collect(Collectors.toList()).size());
        }
        return counts;
    }

    private Map<TaskStatus, Integer> getTaskCountByStatus(UUID projectId) {
        Map<TaskStatus, Integer> counts = new EnumMap<>(TaskStatus.class);
        for (TaskStatus status : TaskStatus.values()) {
            counts.put(status, taskRepository.findByProjectIdAndStatus(projectId, status).size());
        }
        return counts;
    }

    private double calculateAverageTaskCompletionTime(UUID userId) {
        // This would require tracking task start and completion times
        // For now, return a placeholder
        return 0.0;
    }

    private int calculateTasksCompletedOnTime(UUID userId) {
        return (int) taskRepository.findByAssignedToIdAndStatus(userId, TaskStatus.COMPLETED).stream()
                .filter(task -> task.getDueDate() != null && 
                        task.getUpdatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().isBefore(task.getDueDate().plusDays(1)))
                .count();
    }

    private int calculateTasksCompletedLate(UUID userId) {
        return (int) taskRepository.findByAssignedToIdAndStatus(userId, TaskStatus.COMPLETED).stream()
                .filter(task -> task.getDueDate() != null && 
                        task.getUpdatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().isAfter(task.getDueDate()))
                .count();
    }

    private int calculateActiveDays(UUID userId, LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        
        return (int) activityLogRepository.findByUserIdAndDateRange(userId, startInstant, endInstant).stream()
                .map(log -> log.getCreatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate())
                .distinct()
                .count();
    }

    private double calculateProductivityScore(UUID userId, LocalDate startDate, LocalDate endDate) {
        // Simple productivity score based on tasks completed, time logged, and activity
        long completedTasks = taskRepository.findByAssignedToIdAndStatus(userId, TaskStatus.COMPLETED).size();
        int loggedMinutes = timeEntryRepository.sumDurationMinutesByUserId(userId);
        int activeDays = calculateActiveDays(userId, startDate, endDate);
        
        return (completedTasks * 10) + (loggedMinutes / 60.0) + (activeDays * 5);
    }

    private double calculateAverageSessionDuration(UUID projectId, LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        
        return timeEntryRepository.findByProjectIdAndDateRange(projectId, startInstant, endInstant).stream()
                .filter(entry -> entry.getDurationMinutes() != null)
                .mapToInt(entry -> entry.getDurationMinutes())
                .average()
                .orElse(0.0);
    }

    private Map<UUID, Integer> getTimeByUser(UUID projectId, LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        
        return timeEntryRepository.findByProjectIdAndDateRange(projectId, startInstant, endInstant).stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getUser().getId(),
                        Collectors.summingInt(entry -> entry.getDurationMinutes() != null ? entry.getDurationMinutes() : 0)
                ));
    }

    private Map<UUID, Integer> getTimeByTask(UUID projectId, LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        
        return timeEntryRepository.findByProjectIdAndDateRange(projectId, startInstant, endInstant).stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getTask().getId(),
                        Collectors.summingInt(entry -> entry.getDurationMinutes() != null ? entry.getDurationMinutes() : 0)
                ));
    }

    private Map<LocalDate, Integer> getDailyTimeBreakdown(UUID projectId, LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        
        return timeEntryRepository.findByProjectIdAndDateRange(projectId, startInstant, endInstant).stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getStartTime().atZone(java.time.ZoneOffset.UTC).toLocalDate(),
                        Collectors.summingInt(entry -> entry.getDurationMinutes() != null ? entry.getDurationMinutes() : 0)
                ));
    }

    private List<UUID> getTopPerformers(List<Object[]> userStats) {
        return userStats.stream()
                .sorted((a, b) -> Double.compare((Double) b[3], (Double) a[3])) // Sort by completion rate desc
                .limit(5)
                .map(stat -> (UUID) stat[0])
                .collect(Collectors.toList());
    }

    private double calculateTeamProductivityScore(UUID projectId) {
        // Calculate based on overall project metrics
        ProjectTaskStatistics stats = getProjectTaskStatistics(projectId);
        return (stats.getCompletedTasks() * 10.0) + (stats.getTotalLoggedMinutes() / 60.0) - (stats.getOverdueTasks() * 5.0);
    }

    private double calculateCollaborationScore(UUID projectId) {
        // Calculate based on comments, activity, and task interactions
        long totalComments = commentRepository.findByProjectId(projectId).size();
        long totalActivities = activityLogRepository.findByProjectId(projectId).size();
        return (totalComments * 2.0) + (totalActivities * 1.0);
    }

    private Map<UUID, Object> getUserPerformanceBreakdown(List<Object[]> userStats) {
        return userStats.stream()
                .collect(Collectors.toMap(
                        stat -> (UUID) stat[0],
                        stat -> Map.of(
                                "totalTasks", stat[1],
                                "completedTasks", stat[2],
                                "completionRate", stat[3]
                        )
                ));
    }

    // DTO Classes for reporting

    @lombok.Data
    @lombok.Builder
    public static class ProjectTaskStatistics {
        private UUID projectId;
        private long totalTasks;
        private int openTasks;
        private int inProgressTasks;
        private int completedTasks;
        private int cancelledTasks;
        private int overdueTasks;
        private int blockedTasks;
        private int milestones;
        private int totalEstimatedHours;
        private int totalActualHours;
        private int totalLoggedMinutes;
        private double averageCompletionPercentage;
        private Map<TaskPriority, Integer> tasksByPriority;
        private Map<TaskStatus, Integer> tasksByStatus;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserProductivityStatistics {
        private UUID userId;
        private LocalDate startDate;
        private LocalDate endDate;
        private long assignedTasks;
        private int completedTasks;
        private int totalLoggedMinutes;
        private double totalLoggedHours;
        private int billableMinutes;
        private double billableHours;
        private double averageTaskCompletionTime;
        private int tasksCompletedOnTime;
        private int tasksCompletedLate;
        private long commentsAdded;
        private int activeDays;
        private double productivityScore;
    }

    @lombok.Data
    @lombok.Builder
    public static class TimeTrackingReport {
        private UUID projectId;
        private LocalDate startDate;
        private LocalDate endDate;
        private int totalLoggedMinutes;
        private double totalLoggedHours;
        private int billableMinutes;
        private int nonBillableMinutes;
        private int timeEntriesCount;
        private int uniqueUsers;
        private double averageSessionDuration;
        private Map<UUID, Integer> timeByUser;
        private Map<UUID, Integer> timeByTask;
        private Map<LocalDate, Integer> dailyTimeBreakdown;
    }

    @lombok.Data
    @lombok.Builder
    public static class TaskCompletionTrends {
        private UUID projectId;
        private LocalDate startDate;
        private LocalDate endDate;
        private Map<LocalDate, Integer> completionsByDate;
        private Map<LocalDate, Integer> creationsByDate;
        private double averageCompletionsPerDay;
        private double averageCreationsPerDay;
        private int totalCompletions;
        private int totalCreations;
    }

    @lombok.Data
    @lombok.Builder
    public static class TeamPerformanceMetrics {
        private UUID projectId;
        private int totalTeamMembers;
        private double averageTasksPerMember;
        private double averageCompletionRate;
        private List<UUID> topPerformers;
        private double teamProductivityScore;
        private double collaborationScore;
        private Map<UUID, Object> userPerformanceBreakdown;
    }
}