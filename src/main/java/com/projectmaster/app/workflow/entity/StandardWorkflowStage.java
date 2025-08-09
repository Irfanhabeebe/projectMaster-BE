package com.projectmaster.app.workflow.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "standard_workflow_stages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardWorkflowStage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_workflow_template_id", nullable = false)
    private StandardWorkflowTemplate standardWorkflowTemplate;

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

    @OneToMany(mappedBy = "standardWorkflowStage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex")
    private List<StandardWorkflowTask> tasks;
}