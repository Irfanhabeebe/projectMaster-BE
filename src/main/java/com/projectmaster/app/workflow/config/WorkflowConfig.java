package com.projectmaster.app.workflow.config;

import com.projectmaster.app.workflow.engine.handler.WorkflowActionHandler;
import com.projectmaster.app.workflow.enums.WorkflowActionType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class WorkflowConfig {
    
    @Bean
    public Map<WorkflowActionType, WorkflowActionHandler> actionHandlers(
            List<WorkflowActionHandler> handlers) {
        return handlers.stream()
                .collect(Collectors.toMap(
                        WorkflowActionHandler::getSupportedActionType,
                        Function.identity()
                ));
    }
}