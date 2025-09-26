package com.projectmaster.app.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriticalPathAnalysis {
    private List<UUID> criticalPathEntityIds;
    private Integer totalProjectDurationDays;
    private Map<UUID, Integer> slackDays;
    private List<BottleneckInfo> bottlenecks;
    private Double projectCompletionRisk; // 0.0 to 1.0
    private List<ParallelOpportunity> parallelOpportunities;
}
