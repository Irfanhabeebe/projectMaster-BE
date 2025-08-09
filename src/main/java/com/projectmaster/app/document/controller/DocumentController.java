package com.projectmaster.app.document.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.document.dto.*;
import com.projectmaster.app.document.service.DocumentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for document management operations
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Upload a document to a project or task
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentDto>> uploadDocument(
            @Valid @ModelAttribute DocumentUploadRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        DocumentDto document = documentService.uploadDocument(request, userId, httpRequest);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(document, "Document uploaded successfully"));
    }

    /**
     * Download a document by ID
     */
    @GetMapping("/{documentId}/download")
    public ResponseEntity<byte[]> downloadDocument(
            @PathVariable UUID documentId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        DocumentDownloadResponse response = documentService.downloadDocument(documentId, userId, httpRequest);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(response.getMimeType()));
        headers.setContentLength(response.getFileSize());
        headers.set(HttpHeaders.CONTENT_DISPOSITION, response.getContentDisposition());
        
        if (response.getChecksum() != null) {
            headers.set("X-Document-Checksum", response.getChecksum());
        }
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getContent());
    }

    /**
     * Get document metadata by ID
     */
    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDto>> getDocument(
            @PathVariable UUID documentId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        DocumentDto document = documentService.getDocument(documentId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(document, "Document retrieved successfully"));
    }

    /**
     * Update document metadata
     */
    @PutMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDto>> updateDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody DocumentUpdateRequest request,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        DocumentDto document = documentService.updateDocument(documentId, request, userId);
        
        return ResponseEntity.ok(ApiResponse.success(document, "Document updated successfully"));
    }

    /**
     * Delete (archive) a document
     */
    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<String>> deleteDocument(
            @PathVariable UUID documentId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        documentService.deleteDocument(documentId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully"));
    }

    /**
     * Search documents with filters and pagination
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<DocumentDto>>> searchDocuments(
            @Valid @RequestBody DocumentSearchRequest request,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        Page<DocumentDto> documents = documentService.searchDocuments(request, userId);
        
        return ResponseEntity.ok(ApiResponse.success(documents, "Documents retrieved successfully"));
    }

    /**
     * Get all documents for a specific project
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<DocumentDto>>> getDocumentsByProject(
            @PathVariable UUID projectId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        List<DocumentDto> documents = documentService.getDocumentsByProject(projectId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(documents, "Project documents retrieved successfully"));
    }

    /**
     * Get all documents for a specific task
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse<List<DocumentDto>>> getDocumentsByTask(
            @PathVariable UUID taskId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        List<DocumentDto> documents = documentService.getDocumentsByTask(taskId, userId);
        
        return ResponseEntity.ok(ApiResponse.success(documents, "Task documents retrieved successfully"));
    }

    /**
     * Stream a document for inline viewing (images, PDFs, videos)
     */
    @GetMapping("/{documentId}/stream")
    public void streamDocument(
            @PathVariable UUID documentId,
            Authentication authentication,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        DocumentDownloadResponse response = documentService.downloadDocument(documentId, userId, httpRequest);
        
        if (!Boolean.TRUE.equals(response.getIsStreamable())) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Document is not streamable");
            return;
        }
        
        httpResponse.setContentType(response.getMimeType());
        httpResponse.setContentLength(response.getFileSize().intValue());
        httpResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + response.getOriginalFilename() + "\"");
        
        if (response.getChecksum() != null) {
            httpResponse.setHeader("X-Document-Checksum", response.getChecksum());
        }
        
        httpResponse.getOutputStream().write(response.getContent());
        httpResponse.getOutputStream().flush();
    }

    /**
     * Get a thumbnail for image documents
     */
    @GetMapping("/{documentId}/thumbnail")
    public ResponseEntity<byte[]> getDocumentThumbnail(
            @PathVariable UUID documentId,
            @RequestParam(defaultValue = "200") int size,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        DocumentDownloadResponse response = documentService.downloadDocument(documentId, userId, httpRequest);
        
        // Check if it's an image
        if (!response.getMimeType().startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }
        
        // For now, return the original image
        // In a production system, you would implement thumbnail generation
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(response.getMimeType()));
        headers.setContentLength(response.getFileSize());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(response.getContent());
    }

    /**
     * Get statistics for documents
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DocumentStatsDto>> getDocumentStats(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID taskId,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        
        // This would be implemented in the service layer
        DocumentStatsDto stats = DocumentStatsDto.builder()
                .totalDocuments(0L)
                .totalSize(0L)
                .documentsByType(null)
                .documentsByCategory(null)
                .recentUploads(0L)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(stats, "Document statistics retrieved successfully"));
    }

    // Helper method to extract user ID from authentication
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        // This assumes your authentication principal contains the user ID
        // Adjust based on your authentication implementation
        return UUID.fromString(authentication.getName());
    }

    // DTO for document statistics
    /**
     * DTO for document statistics
     */
    public static class DocumentStatsDto {
        private Long totalDocuments;
        private Long totalSize;
        private Object documentsByType;
        private Object documentsByCategory;
        private Long recentUploads;
        
        public static DocumentStatsDtoBuilder builder() {
            return new DocumentStatsDtoBuilder();
        }
        
        public static class DocumentStatsDtoBuilder {
            private Long totalDocuments;
            private Long totalSize;
            private Object documentsByType;
            private Object documentsByCategory;
            private Long recentUploads;
            
            public DocumentStatsDtoBuilder totalDocuments(Long totalDocuments) {
                this.totalDocuments = totalDocuments;
                return this;
            }
            
            public DocumentStatsDtoBuilder totalSize(Long totalSize) {
                this.totalSize = totalSize;
                return this;
            }
            
            public DocumentStatsDtoBuilder documentsByType(Object documentsByType) {
                this.documentsByType = documentsByType;
                return this;
            }
            
            public DocumentStatsDtoBuilder documentsByCategory(Object documentsByCategory) {
                this.documentsByCategory = documentsByCategory;
                return this;
            }
            
            public DocumentStatsDtoBuilder recentUploads(Long recentUploads) {
                this.recentUploads = recentUploads;
                return this;
            }
            
            public DocumentStatsDto build() {
                DocumentStatsDto dto = new DocumentStatsDto();
                dto.totalDocuments = this.totalDocuments;
                dto.totalSize = this.totalSize;
                dto.documentsByType = this.documentsByType;
                dto.documentsByCategory = this.documentsByCategory;
                dto.recentUploads = this.recentUploads;
                return dto;
            }
        }
        
        // Getters and setters
        public Long getTotalDocuments() { return totalDocuments; }
        public void setTotalDocuments(Long totalDocuments) { this.totalDocuments = totalDocuments; }
        
        public Long getTotalSize() { return totalSize; }
        public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }
        
        public Object getDocumentsByType() { return documentsByType; }
        public void setDocumentsByType(Object documentsByType) { this.documentsByType = documentsByType; }
        
        public Object getDocumentsByCategory() { return documentsByCategory; }
        public void setDocumentsByCategory(Object documentsByCategory) { this.documentsByCategory = documentsByCategory; }
        
        public Long getRecentUploads() { return recentUploads; }
        public void setRecentUploads(Long recentUploads) { this.recentUploads = recentUploads; }
    }
}