package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.converter.JsonbConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "workflow_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_stage_id", nullable = false)
    private WorkflowStage workflowStage;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @Builder.Default
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @OneToMany(mappedBy = "workflowTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt")
    private List<WorkflowStep> steps;

    // Reference to the standard workflow task this was copied from
    @Column(name = "standard_workflow_task_id")
    private UUID standardWorkflowTaskId;
} 