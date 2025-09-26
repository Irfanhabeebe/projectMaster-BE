package com.projectmaster.app.workflow.service;

import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepReadinessScheduler {

    private final ProjectRepository projectRepository;
    private final StepReadinessChecker stepReadinessChecker;

    /**
     * Check all active projects for step readiness every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional(readOnly = true)
    public void checkAllProjectsForReadySteps() {
        log.debug("Running scheduled step readiness check for all active projects");
        
        try {
            // Get all active projects
            List<Project> activeProjects = projectRepository.findByStatusIn(
                    List.of(com.projectmaster.app.common.enums.ProjectStatus.IN_PROGRESS, 
                           com.projectmaster.app.common.enums.ProjectStatus.PLANNING));
            
            log.debug("Found {} active projects to check", activeProjects.size());
            
            for (Project project : activeProjects) {
                try {
                    stepReadinessChecker.checkAllStepsInProject(project.getId());
                } catch (Exception e) {
                    log.error("Error checking step readiness for project {}: {}", 
                            project.getId(), e.getMessage(), e);
                }
            }
            
            log.debug("Completed scheduled step readiness check");
        } catch (Exception e) {
            log.error("Error during scheduled step readiness check: {}", e.getMessage(), e);
        }
    }
}

