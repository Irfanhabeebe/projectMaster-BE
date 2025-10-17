package com.projectmaster.app.project.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.workflow.entity.WorkflowTask;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "project_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ProjectTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_stage_id", nullable = false)
    private ProjectStage projectStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_task_id")
    private WorkflowTask workflowTask;

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

    @Column(name = "quality_check_passed")
    private Boolean qualityCheckPassed;

    // Copied properties from WorkflowTask
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    // Version tracking
    @Column(name = "workflow_task_version")
    private Integer workflowTaskVersion;
    
    @Builder.Default
    @Column(name = "adhoc_task_flag", nullable = false)
    private Boolean adhocTaskFlag = false;

    @OneToMany(mappedBy = "projectTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt")
    @Builder.Default
    private List<ProjectStep> steps = new ArrayList<>();
} 