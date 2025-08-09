package com.projectmaster.app.workflow.engine.rule;

import com.projectmaster.app.common.enums.StageStatus;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.repository.ProjectStageRepository;
import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.enums.RulePriority;
import com.projectmaster.app.workflow.enums.RuleType;
import com.projectmaster.app.workflow.enums.WorkflowActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SequentialStageRule implements WorkflowRule {
    
    private final ProjectStageRepository projectStageRepository;
    
    @Override
    public boolean appliesTo(WorkflowExecutionContext context) {
        // This rule applies to stage start actions
        return context.getAction().getType() == WorkflowActionType.START_STAGE &&
               context.getProjectStage() != null;
    }
    
    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        ProjectStage currentStage = context.getProjectStage();
        
        // Check if workflow stage allows parallel execution
        if (currentStage.getWorkflowStage().getParallelExecution()) {
            log.debug("Stage allows parallel execution, skipping sequential check");
            return true;
        }
        
        // Get all stages for the project
        List<ProjectStage> allStages = projectStageRepository
                .findByProjectIdOrderByWorkflowStageOrderIndex(currentStage.getProject().getId());
        
        // Check if all previous stages are completed
        Integer currentOrder = currentStage.getWorkflowStage().getOrderIndex();
        
        for (ProjectStage stage : allStages) {
            if (stage.getWorkflowStage().getOrderIndex() < currentOrder &&
                stage.getStatus() != StageStatus.COMPLETED) {
                log.warn("Previous stage {} is not completed (status: {})", 
                        stage.getId(), stage.getStatus());
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public String getFailureMessage() {
        return "Previous stages must be completed before starting this stage";
    }
    
    @Override
    public RulePriority getPriority() {
        return RulePriority.HIGH;
    }
    
    @Override
    public RuleType getRuleType() {
        return RuleType.PREREQUISITE;
    }
}