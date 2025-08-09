package com.projectmaster.app.document.dto;

import com.projectmaster.app.common.enums.DocumentCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * DTO for document upload requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentUploadRequest {
    
    @NotNull(message = "File is required")
    private MultipartFile file;
    
    private UUID projectId;
    
    private UUID taskId;
    
    @NotNull(message = "Document category is required")
    private DocumentCategory documentCategory;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private List<String> tags;
    
    @Builder.Default
    private Boolean isPublic = false;
    
    private String metadata; // JSON string for additional metadata
    
    // Validation method to ensure either projectId or taskId is provided
    public boolean hasValidReference() {
        return (projectId != null && taskId == null) || (projectId == null && taskId != null);
    }
}