package com.projectmaster.app.project.service;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.project.dto.*;
import com.projectmaster.app.project.entity.*;
import com.projectmaster.app.project.repository.*;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StepUpdateService {

    private final StepUpdateRepository stepUpdateRepository;
    private final StepUpdateDocumentRepository stepUpdateDocumentRepository;
    private final ProjectStepRepository projectStepRepository;
    private final UserRepository userRepository;
    private final ProjectStepAssignmentRepository projectStepAssignmentRepository;

    private static final String UPLOAD_DIR = "uploads/step-documents/";

    /**
     * Create a new step update with optional document uploads
     */
    public StepUpdateResponse createStepUpdate(StepUpdateRequest request, UUID userId) {
        log.info("Creating step update for step: {} by user: {}", request.getStepId(), userId);

        // Validate user authorization
        validateUserAuthorization(UUID.fromString(request.getStepId()), userId);

        // Get entities
        ProjectStep step = projectStepRepository.findById(UUID.fromString(request.getStepId()))
                .orElseThrow(() -> new ProjectMasterException("Step not found: " + request.getStepId()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectMasterException("User not found: " + userId));

        // Create step update
        StepUpdate stepUpdate = StepUpdate.builder()
                .projectStep(step)
                .updatedBy(user)
                .updateType(request.getUpdateType())
                .title(request.getTitle())
                .comments(request.getComments())
                .progressPercentage(request.getProgressPercentage())
                .updateDate(LocalDateTime.now())
                .blockers(request.getBlockers())
                .build();

        stepUpdate = stepUpdateRepository.save(stepUpdate);

        // Handle document uploads
        if (request.getDocuments() != null && !request.getDocuments().isEmpty()) {
            List<StepUpdateDocument> documents = uploadDocumentsFromRequest(stepUpdate, request.getDocuments());
            stepUpdate.setDocuments(documents);
        }

        log.info("Step update created successfully: {}", stepUpdate.getId());
        return convertToResponse(stepUpdate);
    }

    /**
     * Update an existing step update
     */
    public StepUpdateResponse updateStepUpdate(UUID updateId, StepUpdateRequest request, UUID userId) {
        log.info("Updating step update: {} by user: {}", updateId, userId);

        StepUpdate stepUpdate = stepUpdateRepository.findById(updateId)
                .orElseThrow(() -> new ProjectMasterException("Step update not found: " + updateId));

        // Validate user authorization
        validateUserAuthorization(stepUpdate.getProjectStep().getId(), userId);

        // Update fields
        if (request.getUpdateType() != null) {
            stepUpdate.setUpdateType(request.getUpdateType());
        }
        if (request.getTitle() != null) {
            stepUpdate.setTitle(request.getTitle());
        }
        if (request.getComments() != null) {
            stepUpdate.setComments(request.getComments());
        }
        if (request.getProgressPercentage() != null) {
            stepUpdate.setProgressPercentage(request.getProgressPercentage());
        }
        if (request.getBlockers() != null) {
            stepUpdate.setBlockers(request.getBlockers());
        }

        stepUpdate = stepUpdateRepository.save(stepUpdate);

        log.info("Step update updated successfully: {}", stepUpdate.getId());
        return convertToResponse(stepUpdate);
    }

    /**
     * Retrieve updates based on level and filters
     */
    @Transactional(readOnly = true)
    public UpdatesRetrievalResponse retrieveUpdates(UpdatesRetrievalRequest request) {
        log.info("Retrieving updates for level: {} and entity: {}", request.getLevel(), request.getEntityId());

        Pageable pageable = createPageable(request);
        Page<StepUpdate> updatesPage;

        switch (request.getLevel()) {
            case STEP:
                updatesPage = stepUpdateRepository.findByProjectStepIdWithDetails(
                        UUID.fromString(request.getEntityId()), pageable);
                break;
            case TASK:
                updatesPage = stepUpdateRepository.findByProjectTaskIdWithDetails(
                        UUID.fromString(request.getEntityId()), pageable);
                break;
            case STAGE:
                updatesPage = stepUpdateRepository.findByProjectStageIdWithDetails(
                        UUID.fromString(request.getEntityId()), pageable);
                break;
            case PROJECT:
                updatesPage = stepUpdateRepository.findByProjectIdWithDetails(
                        UUID.fromString(request.getEntityId()), pageable);
                break;
            default:
                throw new ProjectMasterException("Invalid update level: " + request.getLevel());
        }

        List<StepUpdateResponse> updateResponses = updatesPage.getContent()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        UpdatesRetrievalResponse.UpdateSummary summary = calculateSummary(updatesPage.getContent());

        return UpdatesRetrievalResponse.builder()
                .updates(updateResponses)
                .totalElements(updatesPage.getTotalElements())
                .totalPages(updatesPage.getTotalPages())
                .currentPage(updatesPage.getNumber())
                .pageSize(updatesPage.getSize())
                .hasNext(updatesPage.hasNext())
                .hasPrevious(updatesPage.hasPrevious())
                .numberOfElements(updatesPage.getNumberOfElements())
                .summary(summary)
                .build();
    }

    /**
     * Get a specific step update by ID
     */
    @Transactional(readOnly = true)
    public StepUpdateResponse getStepUpdate(UUID updateId) {
        StepUpdate stepUpdate = stepUpdateRepository.findById(updateId)
                .orElseThrow(() -> new ProjectMasterException("Step update not found: " + updateId));

        return convertToResponse(stepUpdate);
    }

    /**
     * Delete a step update
     */
    public void deleteStepUpdate(UUID updateId, UUID userId) {
        log.info("Deleting step update: {} by user: {}", updateId, userId);

        StepUpdate stepUpdate = stepUpdateRepository.findById(updateId)
                .orElseThrow(() -> new ProjectMasterException("Step update not found: " + updateId));

        // Validate user authorization
        validateUserAuthorization(stepUpdate.getProjectStep().getId(), userId);

        // Delete associated documents
        for (StepUpdateDocument document : stepUpdate.getDocuments()) {
            deleteDocumentFile(document.getFileName());
        }

        stepUpdateRepository.delete(stepUpdate);
        log.info("Step update deleted successfully: {}", updateId);
    }

    /**
     * Upload documents from request
     */
    public List<StepUpdateDocument> uploadDocumentsFromRequest(StepUpdate stepUpdate, 
                                                              List<StepUpdateRequest.DocumentUploadRequest> documentRequests) {
        List<StepUpdateDocument> documents = new ArrayList<>();

        for (StepUpdateRequest.DocumentUploadRequest docRequest : documentRequests) {
            if (docRequest.getFile() != null && !docRequest.getFile().isEmpty()) {
                try {
                    StepUpdateDocument document = uploadDocument(stepUpdate, docRequest.getFile(), docRequest);
                    documents.add(document);
                } catch (IOException e) {
                    log.error("Failed to upload document: {}", docRequest.getFile().getOriginalFilename(), e);
                    throw new ProjectMasterException("Failed to upload document: " + docRequest.getFile().getOriginalFilename());
                }
            }
        }

        return documents;
    }

    /**
     * Upload documents for a step update (legacy method - keeping for backward compatibility)
     */
    public List<StepUpdateDocument> uploadDocuments(StepUpdate stepUpdate, List<MultipartFile> files, 
                                                   List<StepUpdateRequest.DocumentUploadRequest> documentRequests) {
        List<StepUpdateDocument> documents = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            StepUpdateRequest.DocumentUploadRequest docRequest = i < documentRequests.size() ? 
                    documentRequests.get(i) : null;

            try {
                StepUpdateDocument document = uploadDocument(stepUpdate, file, docRequest);
                documents.add(document);
            } catch (IOException e) {
                log.error("Failed to upload document: {}", file.getOriginalFilename(), e);
                throw new ProjectMasterException("Failed to upload document: " + file.getOriginalFilename());
            }
        }

        return stepUpdateDocumentRepository.saveAll(documents);
    }

    /**
     * Download a document
     */
    @Transactional(readOnly = true)
    public byte[] downloadDocument(UUID documentId) throws IOException {
        StepUpdateDocument document = stepUpdateDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ProjectMasterException("Document not found: " + documentId));

        Path filePath = Paths.get(UPLOAD_DIR + document.getFileName());
        return Files.readAllBytes(filePath);
    }

    /**
     * Delete a document
     */
    public void deleteDocument(UUID documentId, UUID userId) {
        StepUpdateDocument document = stepUpdateDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ProjectMasterException("Document not found: " + documentId));

        // Validate user authorization
        validateUserAuthorization(document.getStepUpdate().getProjectStep().getId(), userId);

        deleteDocumentFile(document.getFileName());
        stepUpdateDocumentRepository.delete(document);
    }

    /**
     * Get document entity by ID (for download purposes)
     */
    @Transactional(readOnly = true)
    public StepUpdateDocument getStepUpdateDocumentEntity(UUID documentId) {
        return stepUpdateDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ProjectMasterException("Document not found: " + documentId));
    }

    // Private helper methods

    private void validateUserAuthorization(UUID stepId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectMasterException("User not found: " + userId));

        ProjectStep step = projectStepRepository.findById(stepId)
                .orElseThrow(() -> new ProjectMasterException("Step not found: " + stepId));

        // Check if user is a project manager or admin
        if (user.getRole().name().equals("PROJECT_MANAGER") || user.getRole().name().equals("SUPER_USER")) {
            if (user.getCompany() != null && user.getCompany().getId().equals(step.getProjectTask().getProjectStage().getProject().getCompany().getId())) {
                return;
            }
        }

        // Check if user is assigned to this step
        List<ProjectStepAssignment> assignments = projectStepAssignmentRepository.findByProjectStepId(stepId);
        boolean isAssigned = assignments.stream()
                .anyMatch(assignment -> 
                    (assignment.getCrew() != null && assignment.getCrew().getUser().getId().equals(userId)) ||
                    (assignment.getContractingCompany() != null && assignment.getContractingCompany().getUsers().stream()
                        .anyMatch(companyUser -> companyUser.getId().equals(userId)))
                );

        if (!isAssigned) {
            throw new ProjectMasterException("User is not authorized to update this step. User must be assigned to the step or be a project manager.");
        }
    }

    private StepUpdateDocument uploadDocument(StepUpdate stepUpdate, MultipartFile file, 
                                            StepUpdateRequest.DocumentUploadRequest docRequest) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String displayFileName = docRequest != null && docRequest.getFileName() != null ? 
                docRequest.getFileName() : originalFilename;
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Save file
        Files.copy(file.getInputStream(), filePath);

        // Create document entity
        StepUpdateDocument document = StepUpdateDocument.builder()
                .stepUpdate(stepUpdate)
                .fileName(uniqueFilename) // Store unique filename for file system
                .originalFileName(displayFileName) // Store original/display filename
                .fileExtension(fileExtension)
                .mimeType(deriveMimeType(fileExtension))
                .documentType(docRequest != null ? docRequest.getDocumentType() : StepUpdateDocument.DocumentType.OTHER)
                .description(docRequest != null ? docRequest.getDescription() : null)
                .uploadDate(LocalDateTime.now())
                .isPublic(docRequest != null && docRequest.getIsPublic() != null ? docRequest.getIsPublic() : true)
                .build();

        return stepUpdateDocumentRepository.save(document);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String deriveMimeType(String fileExtension) {
        return switch (fileExtension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "wmv" -> "video/x-ms-wmv";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "7z" -> "application/x-7z-compressed";
            case "tar" -> "application/x-tar";
            case "gz" -> "application/gzip";
            default -> "application/octet-stream";
        };
    }

    private void deleteDocumentFile(String fileName) {
        try {
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileName, e);
        }
    }

    private Pageable createPageable(UpdatesRetrievalRequest request) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection()) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, request.getSortBy());
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private UpdatesRetrievalResponse.UpdateSummary calculateSummary(List<StepUpdate> updates) {
        long totalUpdates = updates.size();
        long milestoneUpdates = updates.stream().mapToLong(u -> u.getUpdateType() == StepUpdate.UpdateType.MILESTONE_REACHED ? 1 : 0).sum();
        long updatesWithDocuments = updates.stream().mapToLong(u -> u.getDocuments().isEmpty() ? 0 : 1).sum();
        
        Set<StepUpdate.UpdateType> updateTypes = updates.stream()
                .map(StepUpdate::getUpdateType)
                .collect(Collectors.toSet());
        
        Set<StepUpdateDocument.DocumentType> documentTypes = updates.stream()
                .flatMap(u -> u.getDocuments().stream())
                .map(StepUpdateDocument::getDocumentType)
                .collect(Collectors.toSet());
        
        long totalDocuments = updates.stream()
                .mapToLong(u -> u.getDocuments().size())
                .sum();
        
        // File size tracking removed in simplified version
        long totalFileSize = 0;

        String mostRecentUpdate = updates.stream()
                .map(StepUpdate::getUpdateDate)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .map(LocalDateTime::toString)
                .orElse(null);

        String oldestUpdate = updates.stream()
                .map(StepUpdate::getUpdateDate)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .map(LocalDateTime::toString)
                .orElse(null);

        return UpdatesRetrievalResponse.UpdateSummary.builder()
                .totalUpdates(totalUpdates)
                .milestoneUpdates(milestoneUpdates)
                .updatesWithDocuments(updatesWithDocuments)
                .updateTypeCount((long) updateTypes.size())
                .documentTypeCount((long) documentTypes.size())
                .totalDocuments(totalDocuments)
                .totalFileSize(totalFileSize)
                .mostRecentUpdate(mostRecentUpdate)
                .oldestUpdate(oldestUpdate)
                .build();
    }

    private StepUpdateResponse convertToResponse(StepUpdate stepUpdate) {
        return StepUpdateResponse.builder()
                .id(stepUpdate.getId())
                .stepId(stepUpdate.getProjectStep().getId())
                .stepName(stepUpdate.getProjectStep().getName())
                .taskName(stepUpdate.getProjectStep().getProjectTask().getName())
                .stageName(stepUpdate.getProjectStep().getProjectTask().getProjectStage().getName())
                .projectName(stepUpdate.getProjectStep().getProjectTask().getProjectStage().getProject().getName())
                .updatedBy(convertUserToResponse(stepUpdate.getUpdatedBy()))
                .updateType(stepUpdate.getUpdateType())
                .title(stepUpdate.getTitle())
                .comments(stepUpdate.getComments())
                .progressPercentage(stepUpdate.getProgressPercentage())
                .updateDate(stepUpdate.getUpdateDate())
                .blockers(stepUpdate.getBlockers())
                .documents(stepUpdate.getDocuments().stream()
                        .map(this::convertDocumentToResponse)
                        .collect(Collectors.toList()))
                .createdAt(stepUpdate.getCreatedAt() != null ? stepUpdate.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(stepUpdate.getUpdatedAt() != null ? stepUpdate.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    private StepUpdateResponse.DocumentResponse convertDocumentToResponse(StepUpdateDocument document) {
        return StepUpdateResponse.DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .originalFileName(document.getOriginalFileName())
                .fileExtension(document.getFileExtension())
                .mimeType(document.getMimeType())
                .description(document.getDescription())
                .documentType(document.getDocumentType())
                .uploadDate(document.getUploadDate())
                .isPublic(document.getIsPublic())
                .downloadUrl("/api/step-updates/documents/" + document.getId() + "/download")
                .createdAt(document.getCreatedAt() != null ? document.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(document.getUpdatedAt() != null ? document.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }

    private StepUpdateResponse.UserSummaryResponse convertUserToResponse(User user) {
        return StepUpdateResponse.UserSummaryResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .companyName(user.getCompany() != null ? user.getCompany().getName() : null)
                .build();
    }
}
