package com.projectmaster.app.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for project step photo data")
public class ProjectStepPhotoDto {

    @Schema(description = "Photo ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;

    @Schema(description = "Project step ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID projectStepId;

    @Schema(description = "Uploaded by user ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID uploadedByUserId;

    @Schema(description = "Uploaded by user name", example = "John Doe")
    private String uploadedByUserName;

    @Schema(description = "File name", example = "photo_20241203_143022.jpg")
    private String fileName;

    @Schema(description = "Original file name", example = "excavation_progress.jpg")
    private String originalFileName;

    @Schema(description = "File path", example = "/uploads/project-steps/step-123/photo_20241203_143022.jpg")
    private String filePath;

    @Schema(description = "File size in bytes", example = "2048576")
    private Long fileSize;

    @Schema(description = "MIME type", example = "image/jpeg")
    private String mimeType;

    @Schema(description = "Photo description", example = "Excavation progress after 2 hours of work")
    private String description;

    @Schema(description = "Photo type", example = "DURING")
    private String photoType;

    @Schema(description = "Photo type display name", example = "During Work")
    private String photoTypeDisplayName;

    @Schema(description = "Latitude coordinate", example = "-33.8688")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "151.2093")
    private Double longitude;

    @Schema(description = "Whether the photo is public", example = "false")
    private Boolean isPublic;

    @Schema(description = "Photo tags", example = "excavation,progress,foundation")
    private String tags;

    @Schema(description = "When the photo was uploaded")
    private Instant uploadedAt;

    @Schema(description = "When the photo was last updated")
    private Instant updatedAt;

    // Computed fields
    @Schema(description = "File size in human readable format", example = "2.0 MB")
    private String fileSizeFormatted;

    @Schema(description = "Photo URL for viewing", example = "/api/project-steps/photos/123/view")
    private String photoUrl;

    @Schema(description = "Thumbnail URL", example = "/api/project-steps/photos/123/thumbnail")
    private String thumbnailUrl;
}
