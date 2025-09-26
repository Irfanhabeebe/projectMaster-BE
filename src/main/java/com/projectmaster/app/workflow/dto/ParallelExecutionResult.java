package com.projectmaster.app.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParallelExecutionResult {
    private List<UUID> startedTasks;
    private List<UUID> startedSteps;
    private List<UUID> startedAdhocTasks;
    private Instant executionTime;
    private Integer totalEntitiesStarted;
    private String executionSummary; // "Started 3 tasks and 5 steps in parallel"
    private List<String> warnings; // Any issues during execution
    private Integer estimatedTimeSavingDays; // Time saved by parallel execution
}
