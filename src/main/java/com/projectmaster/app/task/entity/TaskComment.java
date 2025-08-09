package com.projectmaster.app.task.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Builder.Default
    @Column(name = "is_internal", nullable = false)
    private Boolean isInternal = false;
}