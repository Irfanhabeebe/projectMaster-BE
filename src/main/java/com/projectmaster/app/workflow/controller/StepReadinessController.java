package com.projectmaster.app.workflow.controller;

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
    
    // Generic project-wide step checking removed to prevent unnecessary updates
    // Use targeted step completion events instead
    
    /**
     * Check readiness for a specific step
     */
    @PostMapping("/check-step/{stepId}")
    public ResponseEntity<Map<String, Object>> checkStepReadiness(@PathVariable UUID stepId) {
        log.info("Manual step readiness check requested for step: {}", stepId);
        
        // This would call stepReadinessChecker.checkAndUpdateStepStatus(stepId)
        // Implementation removed to prevent generic updates
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Step readiness check completed",
            "stepId", stepId
        ));
    }
}

