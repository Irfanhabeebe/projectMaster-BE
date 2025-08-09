package com.projectmaster.app.task.repository;

import com.projectmaster.app.common.enums.TaskPriority;
import com.projectmaster.app.common.enums.TaskStatus;
import com.projectmaster.app.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Find all tasks by project step ID
     */
    List<Task> findByProjectStepIdOrderByCreatedAtAsc(UUID projectStepId);

    /**
     * Find tasks by project step ID with pagination
     */
    Page<Task> findByProjectStepId(UUID projectStepId, Pageable pageable);

    /**
     * Find tasks by project step ID and status
     */
    List<Task> findByProjectStepIdAndStatus(UUID projectStepId, TaskStatus status);

    /**
     * Find tasks by status
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Find tasks by priority
     */
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);

    /**
     * Find tasks by created by user
     */
    Page<Task> findByCreatedById(UUID createdById, Pageable pageable);

    /**
     * Find tasks with due date between given dates
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate")
    List<Task> findTasksDueBetween(@Param("startDate") LocalDate startDate, 
                                 @Param("endDate") LocalDate endDate);

    /**
     * Find overdue tasks (due date passed but not completed)
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDate currentDate);

    /**
     * Find tasks by project step with search term
     */
    @Query("SELECT t FROM Task t WHERE t.projectStep.id = :projectStepId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Task> findByProjectStepIdWithSearch(@Param("projectStepId") UUID projectStepId, 
                                           @Param("searchTerm") String searchTerm, 
                                           Pageable pageable);

    /**
     * Count tasks by status for a project step
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectStep.id = :projectStepId AND (:status IS NULL OR t.status = :status)")
    Long countByProjectStepIdAndStatus(@Param("projectStepId") UUID projectStepId, @Param("status") TaskStatus status);

    /**
     * Find tasks by project ID (through project step and stage)
     */
    @Query("SELECT t FROM Task t WHERE t.projectStep.projectTask.projectStage.project.id = :projectId")
    List<Task> findByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find tasks by project ID with pagination
     */
    @Query("SELECT t FROM Task t WHERE t.projectStep.projectTask.projectStage.project.id = :projectId")
    Page<Task> findByProjectId(@Param("projectId") UUID projectId, Pageable pageable);

    /**
     * Find tasks by project ID and status
     */
    @Query("SELECT t FROM Task t WHERE t.projectStep.projectTask.projectStage.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatus(@Param("projectId") UUID projectId, @Param("status") TaskStatus status);

    /**
     * Find high priority tasks that are not completed
     */
    @Query("SELECT t FROM Task t WHERE t.priority = 'HIGH' AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findHighPriorityIncompleteTasks();

    /**
     * Find tasks assigned to user (created by)
     */
    @Query("SELECT t FROM Task t WHERE t.createdBy.id = :userId AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findActiveTasksByUserId(@Param("userId") UUID userId);

    /**
     * Find tasks by company (through project)
     */
    @Query("SELECT t FROM Task t WHERE t.projectStep.projectTask.projectStage.project.company.id = :companyId")
    Page<Task> findByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

    /**
     * Find tasks requiring attention (overdue or high priority)
     */
    @Query("SELECT t FROM Task t WHERE (t.dueDate < :currentDate OR t.priority = 'HIGH') " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findTasksRequiringAttention(@Param("currentDate") LocalDate currentDate);

    /**
     * Calculate total estimated hours for a project step
     */
    @Query("SELECT COALESCE(SUM(t.estimatedHours), 0) FROM Task t WHERE t.projectStep.id = :projectStepId")
    Integer sumEstimatedHoursByProjectStepId(@Param("projectStepId") UUID projectStepId);

    /**
     * Calculate total actual hours for a project step
     */
    @Query("SELECT COALESCE(SUM(t.actualHours), 0) FROM Task t WHERE t.projectStep.id = :projectStepId")
    Integer sumActualHoursByProjectStepId(@Param("projectStepId") UUID projectStepId);

    /**
     * Find tasks assigned to a specific user
     */
    List<Task> findByAssignedToIdOrderByCreatedAtDesc(UUID assignedToId);

    /**
     * Find tasks assigned to a specific user with pagination
     */
    Page<Task> findByAssignedToId(UUID assignedToId, Pageable pageable);

    /**
     * Find unassigned tasks
     */
    List<Task> findByAssignedToIsNullOrderByCreatedAtDesc();

    /**
     * Find unassigned tasks with pagination
     */
    Page<Task> findByAssignedToIsNull(Pageable pageable);

    /**
     * Find tasks assigned to a user with specific status
     */
    List<Task> findByAssignedToIdAndStatus(UUID assignedToId, TaskStatus status);

    /**
     * Find blocked tasks
     */
    @Query("SELECT t FROM Task t WHERE t.blockedReason IS NOT NULL AND t.blockedReason != ''")
    List<Task> findBlockedTasks();

    /**
     * Find milestone tasks
     */
    List<Task> findByIsMilestoneTrue();

    /**
     * Find milestone tasks for a project
     */
    @Query("SELECT t FROM Task t WHERE t.projectStep.projectTask.projectStage.project.id = :projectId AND t.isMilestone = true")
    List<Task> findMilestonesByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find tasks with tags containing specific text
     */
    @Query("SELECT t FROM Task t WHERE t.tags IS NOT NULL AND LOWER(t.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Task> findByTagsContaining(@Param("tag") String tag);

    /**
     * Find tasks by story points
     */
    List<Task> findByStoryPoints(Integer storyPoints);

    /**
     * Find tasks with story points greater than specified value
     */
    List<Task> findByStoryPointsGreaterThan(Integer storyPoints);

    /**
     * Find tasks due within specified days
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findTasksDueWithinDays(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find tasks assigned to user that are due soon
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.dueDate BETWEEN :startDate AND :endDate " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findTasksDueSoonForUser(@Param("userId") UUID userId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /**
     * Find tasks with low completion percentage but approaching due date
     */
    @Query("SELECT t FROM Task t WHERE t.dueDate <= :dueDate AND t.completionPercentage < :completionThreshold " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findTasksAtRisk(@Param("dueDate") LocalDate dueDate, @Param("completionThreshold") Integer completionThreshold);

    /**
     * Find most active tasks (by activity log count)
     */
    @Query("SELECT t, COUNT(al) as activityCount FROM Task t LEFT JOIN t.activityLogs al " +
           "GROUP BY t ORDER BY activityCount DESC")
    List<Object[]> findMostActiveTasks();

    /**
     * Find tasks with most time logged
     */
    @Query("SELECT t, COALESCE(SUM(te.durationMinutes), 0) as totalMinutes FROM Task t LEFT JOIN t.timeEntries te " +
           "GROUP BY t ORDER BY totalMinutes DESC")
    List<Object[]> findTasksWithMostTimeLogged();

    /**
     * Find tasks by assignee and date range
     */
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :assigneeId " +
           "AND t.createdAt >= :startDate AND t.createdAt <= :endDate")
    List<Task> findByAssigneeAndDateRange(@Param("assigneeId") UUID assigneeId,
                                         @Param("startDate") Instant startDate,
                                         @Param("endDate") Instant endDate);

    /**
     * Count tasks by assignee
     */
    long countByAssignedToId(UUID assignedToId);

    /**
     * Count unassigned tasks
     */
    long countByAssignedToIsNull();

    /**
     * Count blocked tasks
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.blockedReason IS NOT NULL AND t.blockedReason != ''")
    long countBlockedTasks();

    /**
     * Count milestone tasks
     */
    long countByIsMilestoneTrue();

    /**
     * Find tasks that have dependencies
     */
    @Query("SELECT DISTINCT t FROM Task t WHERE EXISTS (SELECT 1 FROM TaskDependency td WHERE td.task = t)")
    List<Task> findTasksWithDependencies();

    /**
     * Find tasks that are dependencies for other tasks
     */
    @Query("SELECT DISTINCT t FROM Task t WHERE EXISTS (SELECT 1 FROM TaskDependency td WHERE td.dependsOnTask = t)")
    List<Task> findTasksThatAreDependencies();

    /**
     * Count tasks by project ID
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.projectStep.projectTask.projectStage.project.id = :projectId")
    long countByProjectId(@Param("projectId") UUID projectId);
}