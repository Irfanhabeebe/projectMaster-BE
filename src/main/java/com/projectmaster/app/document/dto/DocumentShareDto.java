package com.projectmaster.app.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for Document Share entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentShareDto {
    
    private UUID id;
    private UUID documentId;
    private String documentFilename;
    private UUID sharedById;
    private String sharedByName;
    private String shareToken;
    private String shareUrl; // Full URL for sharing
    private Instant expiresAt;
    private Boolean hasPassword;
    private Integer downloadLimit;
    private Integer downloadCount;
    private Boolean isActive;
    private Instant createdAt;
    
    // Computed fields
    private Boolean isExpired;
    private Boolean isDownloadLimitReached;
    private Boolean isAccessible;
    private Long remainingDownloads;
    private String status; // ACTIVE, EXPIRED, LIMIT_REACHED, INACTIVE
}