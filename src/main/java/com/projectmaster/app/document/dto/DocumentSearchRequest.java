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
 * DTO for document search requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSearchRequest {
    
    private UUID projectId;
    private UUID taskId;
    private UUID uploadedById;
    private DocumentType documentType;
    private DocumentCategory documentCategory;
    private String searchTerm; // Search in filename, original filename, description
    private List<String> tags;
    private Boolean isPublic;
    private Boolean isArchived;
    
    // Date range filters
    private Instant createdAfter;
    private Instant createdBefore;
    private Instant updatedAfter;
    private Instant updatedBefore;
    
    // Size filters
    private Long minFileSize;
    private Long maxFileSize;
    
    // Version filters
    private Integer minVersion;
    private Integer maxVersion;
    
    // Sorting options
    @Builder.Default
    private String sortBy = "createdAt"; // createdAt, updatedAt, filename, fileSize
    
    @Builder.Default
    private String sortDirection = "desc"; // asc, desc
    
    // Pagination
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 20;
}