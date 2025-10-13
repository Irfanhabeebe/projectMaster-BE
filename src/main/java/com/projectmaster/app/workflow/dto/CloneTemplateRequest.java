package com.projectmaster.app.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloneTemplateRequest {
    
    @NotBlank(message = "Template name is required")
    @Size(min = 1, max = 255, message = "Template name must be between 1 and 255 characters")
    private String newTemplateName;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
