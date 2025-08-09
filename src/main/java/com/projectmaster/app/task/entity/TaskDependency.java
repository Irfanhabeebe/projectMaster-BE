package com.projectmaster.app.task.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.DependencyType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDependency extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depends_on_task_id", nullable = false)
    private Task dependsOnTask;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "dependency_type", nullable = false)
    private DependencyType dependencyType = DependencyType.FINISH_TO_START;
}