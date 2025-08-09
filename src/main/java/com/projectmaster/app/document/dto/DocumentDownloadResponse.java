package com.projectmaster.app.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for document download response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDownloadResponse {
    
    private String filename;
    private String originalFilename;
    private String mimeType;
    private Long fileSize;
    private byte[] content;
    private String checksum;
    
    // For streaming downloads
    private Boolean isStreamable;
    private String contentDisposition;
    
    public String getContentDisposition() {
        if (contentDisposition != null) {
            return contentDisposition;
        }
        return "attachment; filename=\"" + (originalFilename != null ? originalFilename : filename) + "\"";
    }
}