package com.projectmaster.app.project.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.project.dto.ProjectStepPhotoDto;
import com.projectmaster.app.project.service.ProjectStepPhotoService;
import com.projectmaster.app.security.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/project-steps")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Project Step Photos", description = "Photo management for project steps")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectStepPhotoController {

    private final ProjectStepPhotoService photoService;

    /**
     * Upload a photo for a project step
     */
    @Operation(
        summary = "Upload photo for project step",
        description = "Upload a photo for a project step with metadata"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Photo uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/{projectStepId}/photos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectStepPhotoDto>> uploadPhoto(
            @Parameter(description = "Project step ID", required = true) @PathVariable UUID projectStepId,
            @Parameter(description = "Photo file") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Photo description") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Photo type (BEFORE, DURING, AFTER, QUALITY_CHECK, ISSUE, PROGRESS, COMPLETION)") @RequestParam(value = "photoType", required = false) String photoType,
            Authentication authentication) {
        
        log.info("Uploading photo for project step: {}", projectStepId);
        
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        UUID uploadedByUserId = userPrincipal.getUser().getId();
        
        ProjectStepPhotoDto photo = photoService.uploadPhoto(projectStepId, file, description, photoType, uploadedByUserId);
        
        ApiResponse<ProjectStepPhotoDto> response = ApiResponse.<ProjectStepPhotoDto>builder()
                .success(true)
                .message("Photo uploaded successfully")
                .data(photo)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all photos for a project step
     */
    @Operation(
        summary = "Get photos for project step",
        description = "Retrieve all photos for a specific project step"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Photos retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{projectStepId}/photos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<List<ProjectStepPhotoDto>>> getPhotosByProjectStep(
            @Parameter(description = "Project step ID", required = true) @PathVariable UUID projectStepId) {
        
        log.info("Fetching photos for project step: {}", projectStepId);
        
        List<ProjectStepPhotoDto> photos = photoService.getPhotosByProjectStep(projectStepId);
        
        ApiResponse<List<ProjectStepPhotoDto>> response = ApiResponse.<List<ProjectStepPhotoDto>>builder()
                .success(true)
                .message("Photos retrieved successfully")
                .data(photos)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get photos by project step and photo type
     */
    @Operation(
        summary = "Get photos by type",
        description = "Retrieve photos for a project step filtered by photo type"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Photos retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Project step not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{projectStepId}/photos/type/{photoType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<List<ProjectStepPhotoDto>>> getPhotosByProjectStepAndType(
            @Parameter(description = "Project step ID", required = true) @PathVariable UUID projectStepId,
            @Parameter(description = "Photo type", required = true) @PathVariable String photoType) {
        
        log.info("Fetching photos for project step {} with type: {}", projectStepId, photoType);
        
        List<ProjectStepPhotoDto> photos = photoService.getPhotosByProjectStepAndType(projectStepId, photoType);
        
        ApiResponse<List<ProjectStepPhotoDto>> response = ApiResponse.<List<ProjectStepPhotoDto>>builder()
                .success(true)
                .message("Photos retrieved successfully")
                .data(photos)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific photo by ID
     */
    @Operation(
        summary = "Get photo by ID",
        description = "Retrieve a specific photo by its ID"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Photo retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Photo not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/photos/{photoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectStepPhotoDto>> getPhotoById(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID photoId) {
        
        log.info("Fetching photo with ID: {}", photoId);
        
        ProjectStepPhotoDto photo = photoService.getPhotoById(photoId);
        
        ApiResponse<ProjectStepPhotoDto> response = ApiResponse.<ProjectStepPhotoDto>builder()
                .success(true)
                .message("Photo retrieved successfully")
                .data(photo)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update photo metadata
     */
    @Operation(
        summary = "Update photo metadata",
        description = "Update metadata for a specific photo"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Photo metadata updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Photo not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/photos/{photoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectStepPhotoDto>> updatePhotoMetadata(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID photoId,
            @Parameter(description = "Photo description") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Photo type") @RequestParam(value = "photoType", required = false) String photoType,
            @Parameter(description = "Is public") @RequestParam(value = "isPublic", required = false) Boolean isPublic,
            @Parameter(description = "Tags") @RequestParam(value = "tags", required = false) String tags) {
        
        log.info("Updating photo metadata for photo: {}", photoId);
        
        ProjectStepPhotoDto photo = photoService.updatePhotoMetadata(photoId, description, photoType, isPublic, tags);
        
        ApiResponse<ProjectStepPhotoDto> response = ApiResponse.<ProjectStepPhotoDto>builder()
                .success(true)
                .message("Photo metadata updated successfully")
                .data(photo)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a photo
     */
    @Operation(
        summary = "Delete photo",
        description = "Delete a specific photo"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Photo deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Photo not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/photos/{photoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER') or hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @Parameter(description = "Photo ID", required = true) @PathVariable UUID photoId) {
        
        log.info("Deleting photo with ID: {}", photoId);
        
        photoService.deletePhoto(photoId);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Photo deleted successfully")
                .build();
        
        return ResponseEntity.ok(response);
    }
}
