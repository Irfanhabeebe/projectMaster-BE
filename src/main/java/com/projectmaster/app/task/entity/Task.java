package com.projectmaster.app.task.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.TaskPriority;
import com.projectmaster.app.common.enums.TaskStatus;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_step_id", nullable = false)
    private ProjectStep projectStep;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "priority", nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.OPEN;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Builder.Default
    @Column(name = "actual_hours", nullable = false)
    private Integer actualHours = 0;

    @Builder.Default
    @Column(name = "completion_percentage", nullable = false)
    private Integer completionPercentage = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "tags")
    private String tags;

    @Builder.Default
    @Column(name = "is_milestone", nullable = false)
    private Boolean isMilestone = false;

    @Column(name = "story_points")
    private Integer storyPoints;

    @Column(name = "blocked_reason", columnDefinition = "TEXT")
    private String blockedReason;

    @Builder.Default
    @Column(name = "last_activity_at", nullable = false)
    private Instant lastActivityAt = Instant.now();

    // Relationships for enhanced functionality
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskDependency> dependencies = new ArrayList<>();

    @OneToMany(mappedBy = "dependsOnTask", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskDependency> dependentTasks = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskTimeEntry> timeEntries = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskActivityLog> activityLogs = new ArrayList<>();

    // Helper methods
    public boolean isBlocked() {
        return blockedReason != null && !blockedReason.trim().isEmpty();
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) &&
               status != TaskStatus.COMPLETED && status != TaskStatus.CANCELLED;
    }

    public boolean isAssigned() {
        return assignedTo != null;
    }

    public int getTotalLoggedMinutes() {
        return timeEntries.stream()
                .filter(entry -> entry.getDurationMinutes() != null)
                .mapToInt(TaskTimeEntry::getDurationMinutes)
                .sum();
    }

    public double getTotalLoggedHours() {
        return getTotalLoggedMinutes() / 60.0;
    }
}