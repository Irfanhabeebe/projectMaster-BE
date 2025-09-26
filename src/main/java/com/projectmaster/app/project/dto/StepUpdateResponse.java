package com.projectmaster.app.project.dto;

import com.projectmaster.app.project.entity.StepUpdate;
import com.projectmaster.app.project.entity.StepUpdateDocument;
import com.projectmaster.app.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for step update")
public class StepUpdateResponse {

    @Schema(description = "Update ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Step ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private UUID stepId;

    @Schema(description = "Step name", example = "Foundation Preparation")
    private String stepName;

    @Schema(description = "Task name", example = "Site Preparation")
    private String taskName;

    @Schema(description = "Stage name", example = "Foundation Stage")
    private String stageName;

    @Schema(description = "Project name", example = "Residential Building Project")
    private String projectName;

    @Schema(description = "User who created the update")
    private UserSummaryResponse updatedBy;

    @Schema(description = "Type of update", example = "PROGRESS_UPDATE")
    private StepUpdate.UpdateType updateType;

    @Schema(description = "Title of the update", example = "Foundation Work Progress")
    private String title;

    @Schema(description = "Comments about the update", example = "Weather was perfect for concrete work. No issues encountered.")
    private String comments;

    @Schema(description = "Progress percentage (0-100)", example = "75")
    private Integer progressPercentage;

    @Schema(description = "Date when the update was made", example = "2024-01-15T10:30:00")
    private LocalDateTime updateDate;

    @Schema(description = "Any blockers or issues encountered", example = "Material delivery delayed by 2 hours")
    private String blockers;

    @Schema(description = "Documents attached to this update")
    private List<DocumentResponse> documents;

    @Schema(description = "Date when the record was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date when the record was last updated", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Document response")
    public static class DocumentResponse {
        @Schema(description = "Document ID", example = "550e8400-e29b-41d4-a716-446655440002")
        private UUID id;

        @Schema(description = "File name", example = "foundation_progress_photo.jpg")
        private String fileName;

        @Schema(description = "Original file name", example = "foundation_progress_photo.jpg")
        private String originalFileName;

        @Schema(description = "File extension", example = "jpg")
        private String fileExtension;

            @Schema(description = "MIME type", example = "image/jpeg")
            private String mimeType;

            @Schema(description = "Description of the document", example = "Photo showing completed foundation preparation")
            private String description;

        @Schema(description = "Type of document", example = "PHOTO")
        private StepUpdateDocument.DocumentType documentType;

        @Schema(description = "Date when the document was uploaded", example = "2024-01-15T10:30:00")
        private LocalDateTime uploadDate;

        @Schema(description = "Whether the document is public", example = "true")
        private Boolean isPublic;

        @Schema(description = "Download URL for the document", example = "/api/documents/download/550e8400-e29b-41d4-a716-446655440002")
        private String downloadUrl;

        @Schema(description = "Date when the record was created", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "Date when the record was last updated", example = "2024-01-15T10:30:00")
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User summary for response")
    public static class UserSummaryResponse {
        @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440003")
        private UUID id;

        @Schema(description = "First name", example = "John")
        private String firstName;

        @Schema(description = "Last name", example = "Smith")
        private String lastName;

        @Schema(description = "Email address", example = "john.smith@example.com")
        private String email;

        @Schema(description = "Role", example = "TRADIE")
        private String role;

        @Schema(description = "Company name", example = "ABC Construction")
        private String companyName;
    }
}
