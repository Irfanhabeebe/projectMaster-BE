package com.projectmaster.app.project.dto;

import com.projectmaster.app.project.entity.StepUpdate;
import com.projectmaster.app.project.entity.StepUpdateDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating a step update")
public class StepUpdateRequest {

    @NotNull(message = "Step ID is required")
    @Schema(description = "ID of the project step", example = "550e8400-e29b-41d4-a716-446655440000")
    private String stepId;

    @NotNull(message = "Update type is required")
    @Schema(description = "Type of update", example = "PROGRESS_UPDATE")
    private StepUpdate.UpdateType updateType;

    @Schema(description = "Title of the update", example = "Foundation Work Progress")
    private String title;

    @Schema(description = "Comments about the update", example = "Weather was perfect for concrete work. No issues encountered.")
    private String comments;

    @Schema(description = "Progress percentage (0-100)", example = "75", minimum = "0", maximum = "100")
    private Integer progressPercentage;

    @Schema(description = "Any blockers or issues encountered", example = "Material delivery delayed by 2 hours")
    private String blockers;

    @Schema(description = "List of documents to be attached to this update")
    private List<DocumentUploadRequest> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Document upload request")
    public static class DocumentUploadRequest {
        @Schema(description = "File to upload")
        private MultipartFile file;

        @Schema(description = "Original file name", example = "foundation_progress_photo.jpg")
        private String fileName;

        @Schema(description = "Description of the document", example = "Photo showing completed foundation preparation")
        private String description;

        @NotNull(message = "Document type is required")
        @Schema(description = "Type of document", example = "PHOTO")
        private StepUpdateDocument.DocumentType documentType;

        @Schema(description = "Whether the document is public", example = "true")
        private Boolean isPublic;
    }
}
