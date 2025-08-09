package com.projectmaster.app.task.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "task_time_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTimeEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Builder.Default
    @Column(name = "is_billable", nullable = false)
    private Boolean isBillable = false;

    // Helper methods
    public boolean isActive() {
        return endTime == null;
    }

    public void stopTimer() {
        if (endTime == null && startTime != null) {
            endTime = Instant.now();
            calculateDuration();
        }
    }

    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            long durationMillis = endTime.toEpochMilli() - startTime.toEpochMilli();
            durationMinutes = (int) (durationMillis / (1000 * 60));
        }
    }

    public double getDurationHours() {
        return durationMinutes != null ? durationMinutes / 60.0 : 0.0;
    }
}