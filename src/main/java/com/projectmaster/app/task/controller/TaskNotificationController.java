package com.projectmaster.app.task.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import com.projectmaster.app.task.entity.TaskNotification;
import com.projectmaster.app.task.service.TaskNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class TaskNotificationController {

    private final TaskNotificationService notificationService;

    /**
     * Get notifications for current user
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TaskNotification>>> getUserNotifications(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        log.info("Fetching notifications for user: {}", userId);
        
        Page<TaskNotification> notifications = notificationService.getUserNotifications(userId, pageable);
        
        ApiResponse<Page<TaskNotification>> response = ApiResponse.<Page<TaskNotification>>builder()
                .success(true)
                .message("Notifications retrieved successfully")
                .data(notifications)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get unread notifications for current user
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Page<TaskNotification>>> getUnreadNotifications(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        log.info("Fetching unread notifications for user: {}", userId);
        
        Page<TaskNotification> notifications = notificationService.getUnreadNotifications(userId, pageable);
        
        ApiResponse<Page<TaskNotification>> response = ApiResponse.<Page<TaskNotification>>builder()
                .success(true)
                .message("Unread notifications retrieved successfully")
                .data(notifications)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get unread notification count for current user
     */
    @GetMapping("/unread/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        log.info("Fetching unread notification count for user: {}", userId);
        
        long count = notificationService.getUnreadNotificationCount(userId);
        
        ApiResponse<Long> response = ApiResponse.<Long>builder()
                .success(true)
                .message("Unread notification count retrieved successfully")
                .data(count)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Mark notification as read
     */
    @PostMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID notificationId,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        
        notificationService.markAsRead(notificationId, userId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Notification marked as read successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Mark all notifications as read for current user
     */
    @PostMapping("/read-all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        log.info("Marking all notifications as read for user: {}", userId);
        
        notificationService.markAllAsRead(userId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("All notifications marked as read successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable UUID notificationId,
            Authentication authentication) {
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID userId = userPrincipal.getUser().getId();
        
        log.info("Deleting notification {} for user: {}", notificationId, userId);
        
        notificationService.deleteNotification(notificationId, userId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Notification deleted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
}