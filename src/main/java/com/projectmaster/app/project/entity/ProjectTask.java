package com.projectmaster.app.project.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.common.converter.JsonbConverter;
import com.projectmaster.app.workflow.entity.WorkflowTask;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.List;

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
    @JoinColumn(name = "workflow_task_id", nullable = false)
    private WorkflowTask workflowTask;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private StageStatus status = StageStatus.NOT_STARTED;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

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

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "required_skills")
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = JsonbConverter.class)
    private String requiredSkills;

    @Column(name = "requirements")
    @JdbcTypeCode(SqlTypes.JSON)
    @Convert(converter = JsonbConverter.class)
    private String requirements;

    // Version tracking
    @Column(name = "workflow_task_version")
    private Integer workflowTaskVersion;

    @OneToMany(mappedBy = "projectTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("orderIndex")
    private List<ProjectStep> steps;
} 