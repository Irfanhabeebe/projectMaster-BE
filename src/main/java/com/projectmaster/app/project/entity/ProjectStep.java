package com.projectmaster.app.project.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.converter.JsonbConverter;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.entity.Specialty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "project_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_task_id", nullable = false)
    private ProjectTask projectTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id")
    private WorkflowStep workflowStep;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private StepExecutionStatus status = StepExecutionStatus.NOT_STARTED;

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

    // Copied properties from WorkflowStep
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    // Version tracking
    @Column(name = "workflow_step_version")
    private Integer workflowStepVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;
    
    @Builder.Default
    @Column(name = "adhoc_step_flag", nullable = false)
    private Boolean adhocStepFlag = false;
    
    /**
     * Step execution status - tracks the actual work progress
     */
    public enum StepExecutionStatus {
        NOT_STARTED,    // Step hasn't begun
        READY_TO_START, // Step is ready to start
        IN_PROGRESS,    // Work actively happening
        BLOCKED,        // Step paused temporarily
        COMPLETED,      // Step finished successfully
        CANCELLED       // Step cancelled
    }
}