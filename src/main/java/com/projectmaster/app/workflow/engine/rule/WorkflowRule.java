package com.projectmaster.app.workflow.engine.rule;

import com.projectmaster.app.workflow.dto.WorkflowExecutionContext;
import com.projectmaster.app.workflow.enums.RulePriority;
import com.projectmaster.app.workflow.enums.RuleType;

public interface WorkflowRule {
    boolean appliesTo(WorkflowExecutionContext context);
    boolean evaluate(WorkflowExecutionContext context);
    String getFailureMessage();
    RulePriority getPriority();
    RuleType getRuleType();
}