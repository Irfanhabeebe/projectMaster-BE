package com.projectmaster.app.project.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.project.dto.*;
import com.projectmaster.app.project.entity.StepUpdate;
import com.projectmaster.app.project.entity.StepUpdateDocument;
import com.projectmaster.app.project.service.StepUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/step-updates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Step Updates", description = "APIs for managing step updates and document uploads")
public class StepUpdateController {

    private final StepUpdateService stepUpdateService;

    /**
     * Create a new step update with optional document uploads
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Create step update", description = "Create a new step update with optional document uploads")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Step update created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<StepUpdateResponse>> createStepUpdate(
            @RequestPart("request") StepUpdateRequest request,
            Authentication authentication) {

        log.info("Creating step update for step: {}", request.getStepId());

        UUID userId = getCurrentUserId(authentication);

        StepUpdateResponse response = stepUpdateService.createStepUpdate(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<StepUpdateResponse>builder()
                        .success(true)
                        .message("Step update created successfully")
                        .data(response)
                        .build());
    }

    /**
     * Update an existing step update
     */
    @PutMapping("/{updateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Update step update", description = "Update an existing step update")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Step update updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Step update not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<StepUpdateResponse>> updateStepUpdate(
            @PathVariable UUID updateId,
            @Valid @RequestBody StepUpdateRequest request,
            Authentication authentication) {

        log.info("Updating step update: {}", updateId);

        UUID userId = getCurrentUserId(authentication);
        StepUpdateResponse response = stepUpdateService.updateStepUpdate(updateId, request, userId);

        return ResponseEntity.ok(ApiResponse.<StepUpdateResponse>builder()
                .success(true)
                .message("Step update updated successfully")
                .data(response)
                .build());
    }

    /**
     * Get a specific step update by ID
     */
    @GetMapping("/{updateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get step update", description = "Get a specific step update by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Step update retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Step update not found")
    })
    public ResponseEntity<ApiResponse<StepUpdateResponse>> getStepUpdate(
            @PathVariable UUID updateId) {

        log.info("Getting step update: {}", updateId);

        StepUpdateResponse response = stepUpdateService.getStepUpdate(updateId);

        return ResponseEntity.ok(ApiResponse.<StepUpdateResponse>builder()
                .success(true)
                .message("Step update retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Delete a step update
     */
    @DeleteMapping("/{updateId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Delete step update", description = "Delete a step update and its associated documents")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Step update deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Step update not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> deleteStepUpdate(
            @PathVariable UUID updateId,
            Authentication authentication) {

        log.info("Deleting step update: {}", updateId);

        UUID userId = getCurrentUserId(authentication);
        stepUpdateService.deleteStepUpdate(updateId, userId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Step update deleted successfully")
                .build());
    }

    /**
     * Retrieve updates based on level and filters
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Search updates", description = "Retrieve updates at step, task, stage, or project level with filters")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Updates retrieved successfully")
    })
    public ResponseEntity<ApiResponse<UpdatesRetrievalResponse>> searchUpdates(
            @Valid @RequestBody UpdatesRetrievalRequest request) {

        log.info("Searching updates for level: {} and entity: {}", request.getLevel(), request.getEntityId());

        UpdatesRetrievalResponse response = stepUpdateService.retrieveUpdates(request);

        return ResponseEntity.ok(ApiResponse.<UpdatesRetrievalResponse>builder()
                .success(true)
                .message("Updates retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Get updates for a specific step
     */
    @GetMapping("/step/{stepId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get step updates", description = "Get all updates for a specific step")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Step updates retrieved successfully")
    })
    public ResponseEntity<ApiResponse<UpdatesRetrievalResponse>> getStepUpdates(
            @PathVariable UUID stepId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updateDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Getting updates for step: {}", stepId);

        UpdatesRetrievalRequest request = UpdatesRetrievalRequest.builder()
                .level(UpdatesRetrievalRequest.UpdateLevel.STEP)
                .entityId(stepId.toString())
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        UpdatesRetrievalResponse response = stepUpdateService.retrieveUpdates(request);

        return ResponseEntity.ok(ApiResponse.<UpdatesRetrievalResponse>builder()
                .success(true)
                .message("Step updates retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Get updates for a specific task
     */
    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get task updates", description = "Get all updates for a specific task")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task updates retrieved successfully")
    })
    public ResponseEntity<ApiResponse<UpdatesRetrievalResponse>> getTaskUpdates(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updateDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Getting updates for task: {}", taskId);

        UpdatesRetrievalRequest request = UpdatesRetrievalRequest.builder()
                .level(UpdatesRetrievalRequest.UpdateLevel.TASK)
                .entityId(taskId.toString())
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        UpdatesRetrievalResponse response = stepUpdateService.retrieveUpdates(request);

        return ResponseEntity.ok(ApiResponse.<UpdatesRetrievalResponse>builder()
                .success(true)
                .message("Task updates retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Get updates for a specific stage
     */
    @GetMapping("/stage/{stageId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get stage updates", description = "Get all updates for a specific stage")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stage updates retrieved successfully")
    })
    public ResponseEntity<ApiResponse<UpdatesRetrievalResponse>> getStageUpdates(
            @PathVariable UUID stageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updateDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Getting updates for stage: {}", stageId);

        UpdatesRetrievalRequest request = UpdatesRetrievalRequest.builder()
                .level(UpdatesRetrievalRequest.UpdateLevel.STAGE)
                .entityId(stageId.toString())
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        UpdatesRetrievalResponse response = stepUpdateService.retrieveUpdates(request);

        return ResponseEntity.ok(ApiResponse.<UpdatesRetrievalResponse>builder()
                .success(true)
                .message("Stage updates retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Get updates for a specific project
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Get project updates", description = "Get all updates for a specific project")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Project updates retrieved successfully")
    })
    public ResponseEntity<ApiResponse<UpdatesRetrievalResponse>> getProjectUpdates(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updateDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Getting updates for project: {}", projectId);

        UpdatesRetrievalRequest request = UpdatesRetrievalRequest.builder()
                .level(UpdatesRetrievalRequest.UpdateLevel.PROJECT)
                .entityId(projectId.toString())
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        UpdatesRetrievalResponse response = stepUpdateService.retrieveUpdates(request);

        return ResponseEntity.ok(ApiResponse.<UpdatesRetrievalResponse>builder()
                .success(true)
                .message("Project updates retrieved successfully")
                .data(response)
                .build());
    }

    /**
     * Download a document
     */
    @GetMapping("/documents/{documentId}/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Download document", description = "Download a document associated with a step update")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document downloaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable UUID documentId) throws IOException {

        log.info("Downloading document: {}", documentId);

        byte[] fileContent = stepUpdateService.downloadDocument(documentId);
        StepUpdateDocument document = stepUpdateService.getStepUpdateDocumentEntity(documentId);
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + 
                        (document.getOriginalFileName() != null ? document.getOriginalFileName() : document.getFileName()) + "\"")
                .body(resource);
    }

    /**
     * Delete a document
     */
    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('TRADIE')")
    @Operation(summary = "Delete document", description = "Delete a document associated with a step update")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable UUID documentId,
            Authentication authentication) {

        log.info("Deleting document: {}", documentId);

        UUID userId = getCurrentUserId(authentication);
        stepUpdateService.deleteDocument(documentId, userId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Document deleted successfully")
                .build());
    }

    private UUID getCurrentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
