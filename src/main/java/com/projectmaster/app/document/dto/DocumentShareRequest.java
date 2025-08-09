package com.projectmaster.app.document.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for document share requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentShareRequest {
    
    @NotNull(message = "Document ID is required")
    private UUID documentId;
    
    @Future(message = "Expiration date must be in the future")
    private Instant expiresAt;
    
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password; // Optional password protection
    
    @Min(value = 1, message = "Download limit must be at least 1")
    private Integer downloadLimit; // Optional download limit
    
    @Builder.Default
    private Boolean isActive = true;
}