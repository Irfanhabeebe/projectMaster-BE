package com.projectmaster.app.project.dto;

import com.projectmaster.app.project.entity.StepUpdate;
import com.projectmaster.app.project.entity.StepUpdateDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for retrieving updates at different levels")
public class UpdatesRetrievalRequest {

    @Schema(description = "Level to retrieve updates for", example = "STEP", allowableValues = {"STEP", "TASK", "STAGE", "PROJECT"})
    private UpdateLevel level;

    @Schema(description = "ID of the entity (step, task, stage, or project)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String entityId;

    @Schema(description = "Filter by update types", example = "[\"PROGRESS_UPDATE\", \"MILESTONE_REACHED\"]")
    private List<StepUpdate.UpdateType> updateTypes;

    @Schema(description = "Filter by document types", example = "[\"PHOTO\", \"DOCUMENT\"]")
    private List<StepUpdateDocument.DocumentType> documentTypes;

    @Schema(description = "Filter updates from this date", example = "2024-01-01T00:00:00")
    private LocalDateTime fromDate;

    @Schema(description = "Filter updates until this date", example = "2024-12-31T23:59:59")
    private LocalDateTime toDate;

    @Schema(description = "Only include milestone updates", example = "false")
    private Boolean milestonesOnly;

    @Schema(description = "Only include updates with documents", example = "false")
    private Boolean withDocumentsOnly;

    @Schema(description = "Search term for title, description, or comments", example = "foundation")
    private String searchTerm;

    @Schema(description = "Page number for pagination", example = "0", minimum = "0")
    private Integer page;

    @Schema(description = "Page size for pagination", example = "20", minimum = "1", maximum = "100")
    private Integer size;

    @Schema(description = "Sort field", example = "updateDate", allowableValues = {"updateDate", "createdAt", "title"})
    private String sortBy;

    @Schema(description = "Sort direction", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection;

    public enum UpdateLevel {
        STEP,
        TASK,
        STAGE,
        PROJECT
    }

}
