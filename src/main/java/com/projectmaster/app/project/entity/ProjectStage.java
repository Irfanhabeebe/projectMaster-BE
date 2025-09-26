package com.projectmaster.app.project.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "project_stages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_stage_id", nullable = false)
    private WorkflowStage workflowStage;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private StageStatus status = StageStatus.NOT_STARTED;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "approvals_received", nullable = false)
    private Integer approvalsReceived = 0;

    // Copied properties from WorkflowStage
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

    // Version tracking
    @Column(name = "workflow_template_version")
    private Integer workflowTemplateVersion;

    @Column(name = "workflow_stage_version")
    private Integer workflowStageVersion;

    @OneToMany(mappedBy = "projectStage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex")
    @Builder.Default
    private List<ProjectTask> tasks = new ArrayList<>();
}