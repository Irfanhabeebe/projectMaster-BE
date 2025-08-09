package com.projectmaster.app.task.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.ActivityType;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Helper methods
    public String getFormattedDescription() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        
        // Generate description based on activity type and values
        return switch (activityType) {
            case STATUS_CHANGED -> String.format("Status changed from %s to %s", oldValue, newValue);
            case PRIORITY_CHANGED -> String.format("Priority changed from %s to %s", oldValue, newValue);
            case ASSIGNED -> String.format("Task assigned to %s", newValue);
            case UNASSIGNED -> "Task unassigned";
            case DUE_DATE_CHANGED -> String.format("Due date changed from %s to %s", oldValue, newValue);
            case PROGRESS_UPDATED -> String.format("Progress updated to %s%%", newValue);
            default -> activityType.getDisplayName();
        };
    }
}