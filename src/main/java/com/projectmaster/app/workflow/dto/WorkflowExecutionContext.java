package com.projectmaster.app.workflow.dto;

import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.entity.ProjectStage;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectTask;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.workflow.entity.WorkflowStage;
import com.projectmaster.app.workflow.entity.WorkflowStep;
import com.projectmaster.app.workflow.entity.WorkflowTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecutionContext {
    private Project project;
    private ProjectStage projectStage;
    private ProjectTask projectTask;
    private ProjectStep projectStep;
    private WorkflowStage workflowStage;
    private WorkflowTask workflowTask;
    private WorkflowStep workflowStep;
    private WorkflowAction action;
    private User user;
    private Map<String, Object> metadata;
}