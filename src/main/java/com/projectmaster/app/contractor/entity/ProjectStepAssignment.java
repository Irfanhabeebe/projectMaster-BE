package com.projectmaster.app.contractor.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_step_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectStepAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_step_id", nullable = false)
    private ProjectStep projectStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contracting_company_id", nullable = false)
    private ContractingCompany contractingCompany;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id", nullable = false)
    private User assignedByUser;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(name = "assigned_date", nullable = false)
    private LocalDateTime assignedDate;

    @Column(name = "accepted_date")
    private LocalDateTime acceptedDate;

    @Column(name = "declined_date")
    private LocalDateTime declinedDate;

    @Column(name = "decline_reason")
    private String declineReason;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "estimated_completion_date")
    private LocalDateTime estimatedCompletionDate;

    @Column(name = "actual_completion_date")
    private LocalDateTime actualCompletionDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "hourly_rate")
    private java.math.BigDecimal hourlyRate;

    @Column(name = "total_hours")
    private Integer totalHours;

    @Column(name = "total_cost")
    private java.math.BigDecimal totalCost;

    public enum AssignmentStatus {
        PENDING,        // Step assigned, waiting for contractor response
        ACCEPTED,       // Contractor accepted the assignment
        DECLINED,       // Contractor declined the assignment
        IN_PROGRESS,    // Work has started
        COMPLETED,      // Work completed
        CANCELLED       // Assignment cancelled
    }
}
