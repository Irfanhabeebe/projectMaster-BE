package com.projectmaster.app.workflow.dto;

import com.projectmaster.app.workflow.entity.DependencyEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BottleneckInfo {
    private UUID entityId;
    private DependencyEntityType entityType;
    private String entityName;
    private Integer impactedEntityCount; // How many entities are waiting for this
    private List<UUID> dependentEntityIds; // What is waiting for this
    private String severity; // "HIGH", "MEDIUM", "LOW"
    private String recommendation; // Suggested action to resolve bottleneck
    private Integer potentialDelayDays; // How much this could delay the project
}
