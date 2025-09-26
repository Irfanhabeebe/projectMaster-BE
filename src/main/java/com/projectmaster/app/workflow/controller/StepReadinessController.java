package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.workflow.service.StepReadinessChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/workflow/step-readiness")
@RequiredArgsConstructor
@Slf4j
public class StepReadinessController {

    private final StepReadinessChecker stepReadinessChecker;

    /**
     * Check step readiness for a specific step
     */
    @PostMapping("/check-step/{stepId}")
    public ResponseEntity<Map<String, Object>> checkStepReadiness(@PathVariable UUID stepId) {
        log.info("Manual step readiness check requested for step: {}", stepId);
        
        try {
            stepReadinessChecker.checkAndUpdateStepStatus(stepId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Step readiness check completed",
                "stepId", stepId
            ));
        } catch (Exception e) {
            log.error("Error checking step readiness for step {}: {}", stepId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error checking step readiness: " + e.getMessage(),
                "stepId", stepId
            ));
        }
    }

    /**
     * Check step readiness for all steps in a project
     */
    @PostMapping("/check-project/{projectId}")
    public ResponseEntity<Map<String, Object>> checkProjectStepReadiness(@PathVariable UUID projectId) {
        log.info("Manual project step readiness check requested for project: {}", projectId);
        
        try {
            stepReadinessChecker.checkAllStepsInProject(projectId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Project step readiness check completed",
                "projectId", projectId
            ));
        } catch (Exception e) {
            log.error("Error checking step readiness for project {}: {}", projectId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error checking step readiness: " + e.getMessage(),
                "projectId", projectId
            ));
        }
    }
}

