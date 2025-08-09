package com.projectmaster.app.workflow.engine;

import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.engine.rule.WorkflowRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RuleEngine {
    
    private final List<WorkflowRule> workflowRules;
    
    public boolean canExecuteTransition(WorkflowExecutionContext context) {
        List<WorkflowRule> applicableRules = workflowRules.stream()
                .filter(rule -> rule.appliesTo(context))
                .collect(Collectors.toList());
        
        log.debug("Evaluating {} applicable rules for transition", applicableRules.size());
        
        boolean canExecute = applicableRules.stream()
                .allMatch(rule -> {
                    boolean result = rule.evaluate(context);
                    if (!result) {
                        log.warn("Rule failed: {} - {}", rule.getClass().getSimpleName(), rule.getFailureMessage());
                    }
                    return result;
                });
        
        log.debug("Transition evaluation result: {}", canExecute ? "ALLOWED" : "BLOCKED");
        return canExecute;
    }
    
    public List<WorkflowRule> getBlockingRules(WorkflowExecutionContext context) {
        return workflowRules.stream()
                .filter(rule -> rule.appliesTo(context))
                .filter(rule -> !rule.evaluate(context))
                .collect(Collectors.toList());
    }
    
    public List<String> getBlockingReasons(WorkflowExecutionContext context) {
        return getBlockingRules(context).stream()
                .map(WorkflowRule::getFailureMessage)
                .collect(Collectors.toList());
    }
}