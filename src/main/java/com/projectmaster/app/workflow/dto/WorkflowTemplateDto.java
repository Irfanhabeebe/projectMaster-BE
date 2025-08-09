package com.projectmaster.app.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTemplateDto {
    
    private UUID id;
    private UUID companyId;
    private String companyName;
    private String name;
    private String description;
    private String category;
    private Boolean active;
    private Boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;
}