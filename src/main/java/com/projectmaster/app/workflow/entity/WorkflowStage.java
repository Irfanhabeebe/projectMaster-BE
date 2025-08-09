package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "workflow_stages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowStage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_template_id", nullable = false)
    private WorkflowTemplate workflowTemplate;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Builder.Default
    @Column(name = "parallel_execution", nullable = false)
    private Boolean parallelExecution = false;

    @Builder.Default
    @Column(name = "required_approvals", nullable = false)
    private Integer requiredApprovals = 0;

    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    @Builder.Default
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @OneToMany(mappedBy = "workflowStage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex")
    private List<WorkflowTask> tasks;
}