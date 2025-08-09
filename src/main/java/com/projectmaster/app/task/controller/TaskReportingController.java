package com.projectmaster.app.task.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.task.service.TaskReportingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/task-reporting")
@RequiredArgsConstructor
@Slf4j
public class TaskReportingController {

    private final TaskReportingService reportingService;

    /**
     * Get comprehensive task statistics for a project
     */
    @GetMapping("/project/{projectId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TaskReportingService.ProjectTaskStatistics>> getProjectTaskStatistics(
            @PathVariable UUID projectId) {
        
        log.info("Fetching task statistics for project: {}", projectId);
        
        TaskReportingService.ProjectTaskStatistics statistics = reportingService.getProjectTaskStatistics(projectId);
        
        ApiResponse<TaskReportingService.ProjectTaskStatistics> response = 
                ApiResponse.<TaskReportingService.ProjectTaskStatistics>builder()
                .success(true)
                .message("Project task statistics retrieved successfully")
                .data(statistics)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get user productivity statistics
     */
    @GetMapping("/user/{userId}/productivity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or (hasRole('USER') and #userId == authentication.principal.user.id)")
    public ResponseEntity<ApiResponse<TaskReportingService.UserProductivityStatistics>> getUserProductivityStatistics(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching productivity statistics for user: {} from {} to {}", userId, startDate, endDate);
        
        TaskReportingService.UserProductivityStatistics statistics = 
                reportingService.getUserProductivityStatistics(userId, startDate, endDate);
        
        ApiResponse<TaskReportingService.UserProductivityStatistics> response = 
                ApiResponse.<TaskReportingService.UserProductivityStatistics>builder()
                .success(true)
                .message("User productivity statistics retrieved successfully")
                .data(statistics)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get time tracking report for a project
     */
    @GetMapping("/project/{projectId}/time-tracking")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TaskReportingService.TimeTrackingReport>> getTimeTrackingReport(
            @PathVariable UUID projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching time tracking report for project: {} from {} to {}", projectId, startDate, endDate);
        
        TaskReportingService.TimeTrackingReport report = 
                reportingService.getTimeTrackingReport(projectId, startDate, endDate);
        
        ApiResponse<TaskReportingService.TimeTrackingReport> response = 
                ApiResponse.<TaskReportingService.TimeTrackingReport>builder()
                .success(true)
                .message("Time tracking report retrieved successfully")
                .data(report)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get task completion trends for a project
     */
    @GetMapping("/project/{projectId}/completion-trends")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TaskReportingService.TaskCompletionTrends>> getTaskCompletionTrends(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("Fetching task completion trends for project: {} over {} days", projectId, days);
        
        TaskReportingService.TaskCompletionTrends trends = 
                reportingService.getTaskCompletionTrends(projectId, days);
        
        ApiResponse<TaskReportingService.TaskCompletionTrends> response = 
                ApiResponse.<TaskReportingService.TaskCompletionTrends>builder()
                .success(true)
                .message("Task completion trends retrieved successfully")
                .data(trends)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get team performance metrics for a project
     */
    @GetMapping("/project/{projectId}/team-performance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<TaskReportingService.TeamPerformanceMetrics>> getTeamPerformanceMetrics(
            @PathVariable UUID projectId) {
        
        log.info("Fetching team performance metrics for project: {}", projectId);
        
        TaskReportingService.TeamPerformanceMetrics metrics = 
                reportingService.getTeamPerformanceMetrics(projectId);
        
        ApiResponse<TaskReportingService.TeamPerformanceMetrics> response = 
                ApiResponse.<TaskReportingService.TeamPerformanceMetrics>builder()
                .success(true)
                .message("Team performance metrics retrieved successfully")
                .data(metrics)
                .build();
        
        return ResponseEntity.ok(response);
    }
}