package com.projectmaster.app.document.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmaster.app.common.enums.DocumentType;
import com.projectmaster.app.document.dto.*;
import com.projectmaster.app.document.entity.Document;
import com.projectmaster.app.document.entity.DocumentAccessLog;
import com.projectmaster.app.document.exception.DocumentAccessException;
import com.projectmaster.app.document.exception.DocumentNotFoundException;
import com.projectmaster.app.document.exception.DocumentUploadException;
import com.projectmaster.app.document.repository.DocumentAccessLogRepository;
import com.projectmaster.app.document.repository.DocumentRepository;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.project.repository.ProjectRepository;
import com.projectmaster.app.task.entity.Task;
import com.projectmaster.app.task.repository.TaskRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for document management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentAccessLogRepository accessLogRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.document.max-file-size:52428800}") // 50MB default
    private long maxFileSize;

    @Value("${app.document.allowed-mime-types:}")
    private List<String> allowedMimeTypes;

    @Value("${app.document.blocked-extensions:exe,bat,cmd,scr,pif,com}")
    private List<String> blockedExtensions;

    /**
     * Upload a new document
     */
    public DocumentDto uploadDocument(DocumentUploadRequest request, UUID uploadedById, HttpServletRequest httpRequest) {
        log.info("Uploading document for user: {}", uploadedById);

        // Validate request
        validateUploadRequest(request);

        // Get uploader
        User uploader = userRepository.findById(uploadedById)
                .orElseThrow(() -> new DocumentNotFoundException("User not found: " + uploadedById));

        // Validate file
        MultipartFile file = request.getFile();
        validateFile(file);

        try {
            // Read file content
            byte[] fileContent = file.getBytes();
            String checksum = calculateChecksum(fileContent);

            // Check for duplicates if needed
            Optional<Document> existingDoc = documentRepository.findByChecksumAndIsArchivedFalse(checksum);
            if (existingDoc.isPresent()) {
                log.warn("Duplicate document detected with checksum: {}", checksum);
                // You might want to handle duplicates differently
            }

            // Create document entity
            Document document = createDocumentEntity(request, file, fileContent, checksum, uploader);

            // Save document
            document = documentRepository.save(document);

            // Log access
            logDocumentAccess(document, uploader, "UPLOAD", httpRequest);

            log.info("Document uploaded successfully with ID: {}", document.getId());
            return convertToDto(document);

        } catch (IOException e) {
            throw new DocumentUploadException("Failed to read file content", e);
        }
    }

    /**
     * Download a document
     */
    @Transactional(readOnly = true)
    public DocumentDownloadResponse downloadDocument(UUID documentId, UUID userId, HttpServletRequest httpRequest) {
        log.info("Downloading document: {} for user: {}", documentId, userId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DocumentNotFoundException("User not found: " + userId));

        // Check access permissions
        validateDocumentAccess(document, user);

        // Check if document is archived
        if (Boolean.TRUE.equals(document.getIsArchived())) {
            throw DocumentAccessException.documentArchived();
        }

        // Log access
        logDocumentAccess(document, user, "DOWNLOAD", httpRequest);

        return DocumentDownloadResponse.builder()
                .filename(document.getFilename())
                .originalFilename(document.getOriginalFilename())
                .mimeType(document.getMimeType())
                .fileSize(document.getFileSize())
                .content(document.getFileContent())
                .checksum(document.getChecksum())
                .isStreamable(isStreamableContent(document.getMimeType()))
                .build();
    }

    /**
     * Get document by ID
     */
    @Transactional(readOnly = true)
    public DocumentDto getDocument(UUID documentId, UUID userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DocumentNotFoundException("User not found: " + userId));

        // Check access permissions
        validateDocumentAccess(document, user);

        return convertToDto(document);
    }

    /**
     * Update document metadata
     */
    public DocumentDto updateDocument(UUID documentId, DocumentUpdateRequest request, UUID userId) {
        log.info("Updating document: {} by user: {}", documentId, userId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DocumentNotFoundException("User not found: " + userId));

        // Check access permissions (only uploader or admin can update)
        if (!document.getUploadedBy().getId().equals(userId) && !isAdmin(user)) {
            throw DocumentAccessException.accessDenied();
        }

        // Update fields
        updateDocumentFields(document, request);

        document = documentRepository.save(document);

        log.info("Document updated successfully: {}", documentId);
        return convertToDto(document);
    }

    /**
     * Delete document (soft delete - archive)
     */
    public void deleteDocument(UUID documentId, UUID userId) {
        log.info("Deleting document: {} by user: {}", documentId, userId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DocumentNotFoundException("User not found: " + userId));

        // Check access permissions (only uploader or admin can delete)
        if (!document.getUploadedBy().getId().equals(userId) && !isAdmin(user)) {
            throw DocumentAccessException.accessDenied();
        }

        // Soft delete
        document.setIsArchived(true);
        documentRepository.save(document);

        log.info("Document archived successfully: {}", documentId);
    }

    /**
     * Search documents
     */
    @Transactional(readOnly = true)
    public Page<DocumentDto> searchDocuments(DocumentSearchRequest request, UUID userId) {
        log.debug("Searching documents for user: {}", userId);

        // Create pageable
        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Search documents
        Page<Document> documents = documentRepository.searchDocuments(
                request.getProjectId(),
                request.getTaskId(),
                request.getDocumentType(),
                request.getDocumentCategory(),
                request.getUploadedById(),
                request.getSearchTerm(),
                pageable
        );

        return documents.map(this::convertToDto);
    }

    /**
     * Get documents by project
     */
    @Transactional(readOnly = true)
    public List<DocumentDto> getDocumentsByProject(UUID projectId, UUID userId) {
        log.debug("Getting documents for project: {} by user: {}", projectId, userId);

        // Verify project access
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new DocumentNotFoundException("Project not found: " + projectId));

        List<Document> documents = documentRepository.findByProjectIdAndIsArchivedFalse(projectId);
        return documents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get documents by task
     */
    @Transactional(readOnly = true)
    public List<DocumentDto> getDocumentsByTask(UUID taskId, UUID userId) {
        log.debug("Getting documents for task: {} by user: {}", taskId, userId);

        // Verify task access
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DocumentNotFoundException("Task not found: " + taskId));

        List<Document> documents = documentRepository.findByTaskIdAndIsArchivedFalse(taskId);
        return documents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Private helper methods

    private void validateUploadRequest(DocumentUploadRequest request) {
        if (!request.hasValidReference()) {
            throw new DocumentUploadException("Either projectId or taskId must be provided, but not both");
        }

        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw DocumentUploadException.emptyFile();
        }
    }

    private void validateFile(MultipartFile file) {
        // Check file size
        if (file.getSize() > maxFileSize) {
            throw DocumentUploadException.fileTooLarge(file.getSize(), maxFileSize);
        }

        // Check MIME type if restrictions are configured
        if (!allowedMimeTypes.isEmpty() && !allowedMimeTypes.contains(file.getContentType())) {
            throw DocumentUploadException.unsupportedFileType(file.getContentType());
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            if (blockedExtensions.contains(extension)) {
                throw DocumentUploadException.unsupportedFileType("Blocked file extension: " + extension);
            }
        }

        // Validate filename
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw DocumentUploadException.invalidFileName(filename);
        }
    }

    private Document createDocumentEntity(DocumentUploadRequest request, MultipartFile file, 
                                        byte[] fileContent, String checksum, User uploader) {
        String originalFilename = file.getOriginalFilename();
        String filename = generateUniqueFilename(originalFilename);
        DocumentType documentType = DocumentType.fromMimeType(file.getContentType());

        Document.DocumentBuilder builder = Document.builder()
                .filename(filename)
                .originalFilename(originalFilename)
                .fileContent(fileContent)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .documentType(documentType)
                .documentCategory(request.getDocumentCategory())
                .description(request.getDescription())
                .isPublic(request.getIsPublic())
                .checksum(checksum)
                .uploadedBy(uploader);

        // Set project or task reference
        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new DocumentNotFoundException("Project not found: " + request.getProjectId()));
            builder.project(project);
        } else if (request.getTaskId() != null) {
            Task task = taskRepository.findById(request.getTaskId())
                    .orElseThrow(() -> new DocumentNotFoundException("Task not found: " + request.getTaskId()));
            builder.task(task);
        }

        // Set tags and metadata
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            try {
                builder.tags(objectMapper.writeValueAsString(request.getTags()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize tags", e);
            }
        }

        if (StringUtils.hasText(request.getMetadata())) {
            builder.metadata(request.getMetadata());
        }

        return builder.build();
    }

    private String calculateChecksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new DocumentUploadException("Failed to calculate checksum", e);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String baseName = getBaseName(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return String.format("%s_%s_%s.%s", baseName, timestamp, uuid, extension);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String getBaseName(String filename) {
        if (filename == null) {
            return "document";
        }
        int dotIndex = filename.lastIndexOf(".");
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    private void validateDocumentAccess(Document document, User user) {
        // Public documents are accessible to all
        if (Boolean.TRUE.equals(document.getIsPublic())) {
            return;
        }

        // Uploader always has access
        if (document.getUploadedBy().getId().equals(user.getId())) {
            return;
        }

        // Admin has access to all
        if (isAdmin(user)) {
            return;
        }

        // Check project/task access based on user's company and assignments
        // This is a simplified check - you might want to implement more sophisticated access control
        UUID userCompanyId = user.getCompany().getId();
        
        if (document.getProject() != null) {
            if (!document.getProject().getCompany().getId().equals(userCompanyId)) {
                throw DocumentAccessException.accessDenied();
            }
        } else if (document.getTask() != null) {
            UUID taskCompanyId = document.getTask().getProjectStep().getProjectTask().getProjectStage().getProject().getCompany().getId();
            if (!taskCompanyId.equals(userCompanyId)) {
                throw DocumentAccessException.accessDenied();
            }
        }
    }

    private boolean isAdmin(User user) {
        return user.getRole().name().equals("ADMIN");
    }

    private boolean isStreamableContent(String mimeType) {
        return mimeType != null && (
                mimeType.startsWith("image/") ||
                mimeType.startsWith("video/") ||
                mimeType.startsWith("audio/") ||
                mimeType.equals("application/pdf")
        );
    }

    private void logDocumentAccess(Document document, User user, String accessType, HttpServletRequest request) {
        try {
            DocumentAccessLog accessLog = DocumentAccessLog.builder()
                    .document(document)
                    .user(user)
                    .accessType(accessType)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .build();

            accessLogRepository.save(accessLog);
        } catch (Exception e) {
            log.warn("Failed to log document access", e);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void updateDocumentFields(Document document, DocumentUpdateRequest request) {
        if (StringUtils.hasText(request.getFilename())) {
            document.setFilename(request.getFilename());
        }
        if (request.getDocumentCategory() != null) {
            document.setDocumentCategory(request.getDocumentCategory());
        }
        if (request.getDescription() != null) {
            document.setDescription(request.getDescription());
        }
        if (request.getIsPublic() != null) {
            document.setIsPublic(request.getIsPublic());
        }
        if (request.getIsArchived() != null) {
            document.setIsArchived(request.getIsArchived());
        }
        if (request.getTags() != null) {
            try {
                document.setTags(objectMapper.writeValueAsString(request.getTags()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize tags", e);
            }
        }
        if (StringUtils.hasText(request.getMetadata())) {
            document.setMetadata(request.getMetadata());
        }
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        return Sort.by(direction, sortBy);
    }

    private DocumentDto convertToDto(Document document) {
        DocumentDto.DocumentDtoBuilder builder = DocumentDto.builder()
                .id(document.getId())
                .filename(document.getFilename())
                .originalFilename(document.getOriginalFilename())
                .fileSize(document.getFileSize())
                .formattedFileSize(document.getFormattedFileSize())
                .mimeType(document.getMimeType())
                .documentType(document.getDocumentType())
                .documentCategory(document.getDocumentCategory())
                .description(document.getDescription())
                .version(document.getVersion())
                .isPublic(document.getIsPublic())
                .isArchived(document.getIsArchived())
                .checksum(document.getChecksum())
                .metadata(document.getMetadata())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .uploadedById(document.getUploadedBy().getId())
                .uploadedByName(document.getUploadedBy().getFullName());

        // Set project info
        if (document.getProject() != null) {
            builder.projectId(document.getProject().getId())
                   .projectName(document.getProject().getName());
        }

        // Set task info
        if (document.getTask() != null) {
            builder.taskId(document.getTask().getId())
                   .taskTitle(document.getTask().getTitle());
        }

        // Parse tags
        if (StringUtils.hasText(document.getTags())) {
            try {
                List<String> tags = objectMapper.readValue(document.getTags(), new TypeReference<List<String>>() {});
                builder.tags(tags);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse tags for document: {}", document.getId());
            }
        }

        return builder.build();
    }
}