package com.projectmaster.app.document.dto;

import com.projectmaster.app.common.enums.DocumentCategory;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for document update requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUpdateRequest {
    
    @Size(max = 255, message = "Filename cannot exceed 255 characters")
    private String filename;
    
    private DocumentCategory documentCategory;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private List<String> tags;
    
    private Boolean isPublic;
    
    private Boolean isArchived;
    
    private String metadata; // JSON string for additional metadata
}