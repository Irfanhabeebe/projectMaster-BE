package com.projectmaster.app.project.dto;

import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentStatus;
import com.projectmaster.app.project.entity.ProjectStepAssignment.AssignmentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ProjectStepAssignmentResponse {

    private UUID id;
    private UUID projectStepId;
    private AssignmentType assignedToType;
    private AssignmentStatus status;
    
    // Assignment details
    private UUID crewId;
    private String crewName;
    private String crewEmail;
    private String crewPosition;
    
    private UUID contractingCompanyId;
    private String contractingCompanyName;
    private String contractingCompanyContactEmail;
    
    // Assignment metadata
    private UUID assignedByUserId;
    private String assignedByUserName;
    private LocalDateTime assignedDate;
    private LocalDateTime acceptedDate;
    private LocalDateTime declinedDate;
    private String declineReason;
    
    // Work details
    private LocalDateTime startDate;
    private LocalDateTime estimatedCompletionDate;
    private LocalDateTime actualCompletionDate;
    private String notes;
    private BigDecimal hourlyRate;
    private Integer totalHours;
    private BigDecimal totalCost;
}
