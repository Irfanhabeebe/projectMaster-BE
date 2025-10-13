package com.projectmaster.app.workflow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepReadinessScheduler {
    
    // Generic project-wide step checking removed to prevent unnecessary updates
    // Step readiness is now handled only when specific events occur (step completion, task completion, etc.)
    
    // If you need periodic checks, implement targeted checks for specific projects/steps
    // rather than checking all steps in all projects
}

