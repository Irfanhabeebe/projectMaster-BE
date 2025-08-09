package com.projectmaster.app.task.repository;

import com.projectmaster.app.task.entity.TaskTimeEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskTimeEntryRepository extends JpaRepository<TaskTimeEntry, UUID> {

    /**
     * Find all time entries for a task
     */
    List<TaskTimeEntry> findByTaskIdOrderByStartTimeDesc(UUID taskId);

    /**
     * Find time entries for a task with pagination
     */
    Page<TaskTimeEntry> findByTaskId(UUID taskId, Pageable pageable);

    /**
     * Find all time entries for a user
     */
    List<TaskTimeEntry> findByUserIdOrderByStartTimeDesc(UUID userId);

    /**
     * Find time entries for a user with pagination
     */
    Page<TaskTimeEntry> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find active time entries (no end time) for a user
     */
    List<TaskTimeEntry> findByUserIdAndEndTimeIsNull(UUID userId);

    /**
     * Find active time entry for a specific task and user
     */
    Optional<TaskTimeEntry> findByTaskIdAndUserIdAndEndTimeIsNull(UUID taskId, UUID userId);

    /**
     * Find time entries within a date range
     */
    @Query("SELECT te FROM TaskTimeEntry te WHERE te.startTime >= :startTime AND te.startTime <= :endTime")
    List<TaskTimeEntry> findByDateRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);

    /**
     * Find time entries for a user within a date range
     */
    @Query("SELECT te FROM TaskTimeEntry te WHERE te.user.id = :userId AND te.startTime >= :startTime AND te.startTime <= :endTime")
    List<TaskTimeEntry> findByUserIdAndDateRange(@Param("userId") UUID userId, 
                                                @Param("startTime") Instant startTime, 
                                                @Param("endTime") Instant endTime);

    /**
     * Find time entries for a project within a date range
     */
    @Query("SELECT te FROM TaskTimeEntry te WHERE te.task.projectStep.projectTask.projectStage.project.id = :projectId " +
           "AND te.startTime >= :startTime AND te.startTime <= :endTime")
    List<TaskTimeEntry> findByProjectIdAndDateRange(@Param("projectId") UUID projectId,
                                                   @Param("startTime") Instant startTime,
                                                   @Param("endTime") Instant endTime);

    /**
     * Calculate total logged minutes for a task
     */
    @Query("SELECT COALESCE(SUM(te.durationMinutes), 0) FROM TaskTimeEntry te WHERE te.task.id = :taskId")
    Integer sumDurationMinutesByTaskId(@Param("taskId") UUID taskId);

    /**
     * Calculate total logged minutes for a user
     */
    @Query("SELECT COALESCE(SUM(te.durationMinutes), 0) FROM TaskTimeEntry te WHERE te.user.id = :userId")
    Integer sumDurationMinutesByUserId(@Param("userId") UUID userId);

    /**
     * Calculate total logged minutes for a project
     */
    @Query("SELECT COALESCE(SUM(te.durationMinutes), 0) FROM TaskTimeEntry te " +
           "WHERE te.task.projectStep.projectTask.projectStage.project.id = :projectId")
    Integer sumDurationMinutesByProjectId(@Param("projectId") UUID projectId);

    /**
     * Calculate total billable minutes for a user within date range
     */
    @Query("SELECT COALESCE(SUM(te.durationMinutes), 0) FROM TaskTimeEntry te " +
           "WHERE te.user.id = :userId AND te.isBillable = true " +
           "AND te.startTime >= :startTime AND te.startTime <= :endTime")
    Integer sumBillableMinutesByUserIdAndDateRange(@Param("userId") UUID userId,
                                                  @Param("startTime") Instant startTime,
                                                  @Param("endTime") Instant endTime);

    /**
     * Find billable time entries for a project
     */
    @Query("SELECT te FROM TaskTimeEntry te WHERE te.task.projectStep.projectTask.projectStage.project.id = :projectId " +
           "AND te.isBillable = true")
    List<TaskTimeEntry> findBillableByProjectId(@Param("projectId") UUID projectId);

    /**
     * Delete all time entries for a task
     */
    void deleteByTaskId(UUID taskId);

    /**
     * Count active time entries for a user
     */
    long countByUserIdAndEndTimeIsNull(UUID userId);
}