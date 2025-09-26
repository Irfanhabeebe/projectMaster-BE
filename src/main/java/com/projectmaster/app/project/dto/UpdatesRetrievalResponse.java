package com.projectmaster.app.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for updates retrieval")
public class UpdatesRetrievalResponse {

    @Schema(description = "List of step updates")
    private List<StepUpdateResponse> updates;

    @Schema(description = "Total number of updates found")
    private Long totalElements;

    @Schema(description = "Total number of pages")
    private Integer totalPages;

    @Schema(description = "Current page number")
    private Integer currentPage;

    @Schema(description = "Page size")
    private Integer pageSize;

    @Schema(description = "Whether there are more pages")
    private Boolean hasNext;

    @Schema(description = "Whether there are previous pages")
    private Boolean hasPrevious;

    @Schema(description = "Number of elements in current page")
    private Integer numberOfElements;

    @Schema(description = "Summary statistics")
    private UpdateSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Summary statistics for updates")
    public static class UpdateSummary {
        @Schema(description = "Total number of updates")
        private Long totalUpdates;

        @Schema(description = "Number of milestone updates")
        private Long milestoneUpdates;

        @Schema(description = "Number of updates with documents")
        private Long updatesWithDocuments;

        @Schema(description = "Number of different update types")
        private Long updateTypeCount;

        @Schema(description = "Number of different document types")
        private Long documentTypeCount;

        @Schema(description = "Total number of documents")
        private Long totalDocuments;

        @Schema(description = "Total file size of all documents in bytes")
        private Long totalFileSize;

        @Schema(description = "Most recent update date")
        private String mostRecentUpdate;

        @Schema(description = "Oldest update date")
        private String oldestUpdate;
    }
}
