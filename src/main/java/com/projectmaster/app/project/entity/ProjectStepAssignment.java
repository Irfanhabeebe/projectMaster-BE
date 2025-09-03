package com.projectmaster.app.project.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.contractor.entity.ContractingCompany;
import com.projectmaster.app.crew.entity.Crew;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_to_type", nullable = false)
    private AssignmentType assignedToType;
    
    // Only one of these will be populated based on assignedToType
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id")
    private Crew crew;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contracting_company_id")
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

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "total_hours")
    private Integer totalHours;

    @Column(name = "total_cost")
    private java.math.BigDecimal totalCost;

    /**
     * Assignment type - defines who is assigned to the step
     */
    public enum AssignmentType {
        CREW,               // Assigned to internal crew member
        CONTRACTING_COMPANY // Assigned to external contractor
    }
    
    /**
     * Assignment status - tracks the assignment acceptance and management
     */
    public enum AssignmentStatus {
        PENDING,        // Step assigned, waiting for response
        ACCEPTED,       // Assignment accepted
        DECLINED,       // Assignment declined
        CANCELLED       // Assignment cancelled
    }
}
