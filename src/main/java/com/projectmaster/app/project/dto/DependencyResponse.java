package com.projectmaster.app.project.dto;

import com.projectmaster.app.workflow.entity.DependencyEntityType;
import com.projectmaster.app.workflow.entity.DependencyStatus;
import com.projectmaster.app.workflow.entity.DependencyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Project dependency information")
public class DependencyResponse {

    @Schema(description = "Dependency ID")
    private UUID id;

    @Schema(description = "Entity type that depends on another entity")
    private DependencyEntityType dependentEntityType;

    @Schema(description = "ID of the entity that depends on another entity")
    private UUID dependentEntityId;

    @Schema(description = "Name of the dependent entity")
    private String dependentEntityName;

    @Schema(description = "Entity type that is being depended upon")
    private DependencyEntityType dependsOnEntityType;

    @Schema(description = "ID of the entity that is being depended upon")
    private UUID dependsOnEntityId;

    @Schema(description = "Name of the entity that is being depended upon")
    private String dependsOnEntityName;

    @Schema(description = "Type of dependency (e.g., FINISH_TO_START)")
    private DependencyType dependencyType;

    @Schema(description = "Lag days for the dependency")
    private Integer lagDays;

    @Schema(description = "Dependency status")
    private DependencyStatus status;

    @Schema(description = "When the dependency was satisfied")
    private Instant satisfiedAt;

    @Schema(description = "Whether this dependency is on the critical path")
    private Boolean isCriticalPath;

    @Schema(description = "Slack days for this dependency")
    private Integer slackDays;

    @Schema(description = "Expected duration in days")
    private Integer expectedDurationDays;

    @Schema(description = "Dependency notes")
    private String notes;

    @Schema(description = "Dependency creation date")
    private Instant createdAt;

    @Schema(description = "Dependency last update date")
    private Instant updatedAt;
}
