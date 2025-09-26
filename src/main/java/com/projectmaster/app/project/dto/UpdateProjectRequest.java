package com.projectmaster.app.project.dto;

import com.projectmaster.app.common.enums.ProjectStatus;
import com.projectmaster.app.customer.dto.AddressRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing project")
public class UpdateProjectRequest {

    @Schema(description = "ID of the customer for this project", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID customerId;

    @Schema(description = "ID of the workflow template to use", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID workflowTemplateId;

    @Size(max = 50, message = "Project number must not exceed 50 characters")
    @Schema(description = "Unique project number", example = "PRJ-2024-001")
    private String projectNumber;

    @Size(max = 255, message = "Project name must not exceed 255 characters")
    @Schema(description = "Name of the project", example = "Residential Construction Project")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Detailed description of the project", example = "New residential construction project with modern amenities")
    private String description;

    @Valid
    @Schema(description = "Project address information")
    private AddressRequest address;

    @DecimalMin(value = "0.0", inclusive = false, message = "Budget must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Budget must have at most 13 integer digits and 2 decimal places")
    @Schema(description = "Project budget amount", example = "500000.00")
    private BigDecimal budget;

    @Schema(description = "Project planned start date", example = "2024-01-15")
    private LocalDate plannedStartDate;

    @Schema(description = "Expected project completion date", example = "2024-12-31")
    private LocalDate expectedEndDate;

    @Schema(description = "Actual project completion date", example = "2024-11-30")
    private LocalDate actualEndDate;

    @Schema(description = "Current project status", example = "IN_PROGRESS")
    private ProjectStatus status;

    @Min(value = 0, message = "Progress percentage must be between 0 and 100")
    @Max(value = 100, message = "Progress percentage must be between 0 and 100")
    @Schema(description = "Project completion percentage", example = "75")
    private Integer progressPercentage;

    @Schema(description = "Additional notes about the project", example = "Updated requirements and considerations")
    private String notes;
}