package com.projectmaster.app.document.dto;

import com.projectmaster.app.common.enums.DocumentCategory;
import com.projectmaster.app.common.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for Document entity (without file content for listing)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDto {
    
    private UUID id;
    private UUID projectId;
    private String projectName;
    private UUID taskId;
    private String taskTitle;
    private UUID uploadedById;
    private String uploadedByName;
    private String filename;
    private String originalFilename;
    private Long fileSize;
    private String formattedFileSize;
    private String mimeType;
    private DocumentType documentType;
    private DocumentCategory documentCategory;
    private String description;
    private List<String> tags;
    private Integer version;
    private Boolean isPublic;
    private Boolean isArchived;
    private String checksum;
    private String metadata;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Additional computed fields
    private Long downloadCount;
    private Boolean hasVersions;
    private Integer totalVersions;
    private Boolean isShared;
    private Integer activeSharesCount;
}