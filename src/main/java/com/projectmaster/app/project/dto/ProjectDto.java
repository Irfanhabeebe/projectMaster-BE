package com.projectmaster.app.project.dto;

import com.projectmaster.app.common.enums.ProjectStatus;
import com.projectmaster.app.customer.dto.AddressResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for project data")
public class ProjectDto {
    
    @Schema(description = "Unique identifier for the project", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    
    @Schema(description = "ID of the company that owns the project", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID companyId;
    
    @Schema(description = "Name of the company that owns the project", example = "ABC Construction Co.")
    private String companyName;
    
    @Schema(description = "ID of the customer for this project", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID customerId;
    
    @Schema(description = "Name of the customer for this project", example = "John Doe")
    private String customerName;
    
    @Schema(description = "ID of the workflow template used", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID workflowTemplateId;
    
    @Schema(description = "Name of the workflow template used", example = "Residential Construction Workflow")
    private String workflowTemplateName;
    
    @Schema(description = "Unique project number", example = "PRJ-2024-001")
    private String projectNumber;
    
    @Schema(description = "Name of the project", example = "Residential Construction Project")
    private String name;
    
    @Schema(description = "Detailed description of the project", example = "New residential construction project with modern amenities")
    private String description;
    
    @Schema(description = "Project address information")
    private AddressResponse address;
    @Schema(description = "Project budget amount", example = "500000.00")
    private BigDecimal budget;
    
    @Schema(description = "Project start date", example = "2024-01-15")
    private LocalDate startDate;
    
    @Schema(description = "Expected project completion date", example = "2024-12-31")
    private LocalDate expectedEndDate;
    
    @Schema(description = "Actual project completion date", example = "2024-11-30")
    private LocalDate actualEndDate;
    
    @Schema(description = "Current project status", example = "IN_PROGRESS")
    private ProjectStatus status;
    
    @Schema(description = "Project completion percentage", example = "75")
    private Integer progressPercentage;
    
    @Schema(description = "Additional notes about the project", example = "Special requirements and considerations")
    private String notes;
    
    @Schema(description = "When the project was created")
    private Instant createdAt;
    
    @Schema(description = "When the project was last updated")
    private Instant updatedAt;
}