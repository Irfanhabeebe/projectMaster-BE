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
public class ParallelOpportunity {
    private String opportunityType; // "TASKS_CAN_START_PARALLEL", "STEPS_CAN_START_PARALLEL"
    private List<UUID> entityIds;
    private DependencyEntityType entityType;
    private String description; // "3 tasks can start in parallel after Foundation completes"
    private Integer potentialTimeSavingDays;
    private String currentStatus; // "READY", "WAITING_FOR_DEPENDENCY", "BLOCKED"
    private List<String> blockingReasons; // Why this opportunity can't be taken yet
}
