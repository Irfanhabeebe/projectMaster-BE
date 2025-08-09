package com.projectmaster.app.task.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.NotificationType;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "task_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskNotification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "scheduled_for")
    private Instant scheduledFor;

    @Column(name = "sent_at")
    private Instant sentAt;

    // Helper methods
    public boolean isPending() {
        return sentAt == null && (scheduledFor == null || scheduledFor.isAfter(Instant.now()));
    }

    public boolean isScheduled() {
        return scheduledFor != null && sentAt == null;
    }

    public boolean isSent() {
        return sentAt != null;
    }

    public void markAsSent() {
        this.sentAt = Instant.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}