package com.projectmaster.app.crew.dto;

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
@Schema(description = "Response object for assignment search with pagination")
public class AssignmentSearchResponse {

    @Schema(description = "List of assignments matching the search criteria")
    private List<CrewAssignmentDto> assignments;

    @Schema(description = "Pagination information")
    private PaginationInfo pagination;

    @Schema(description = "Search summary statistics")
    private SearchSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Pagination information")
    public static class PaginationInfo {
        @Schema(description = "Current page number (0-based)", example = "0")
        private int currentPage;

        @Schema(description = "Page size", example = "20")
        private int pageSize;

        @Schema(description = "Total number of elements", example = "150")
        private long totalElements;

        @Schema(description = "Total number of pages", example = "8")
        private int totalPages;

        @Schema(description = "Whether this is the first page", example = "true")
        private boolean first;

        @Schema(description = "Whether this is the last page", example = "false")
        private boolean last;

        @Schema(description = "Number of elements in current page", example = "20")
        private int numberOfElements;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Search summary statistics")
    public static class SearchSummary {
        @Schema(description = "Total assignments found", example = "150")
        private long totalFound;

        @Schema(description = "Number of pending assignments", example = "25")
        private long pendingCount;

        @Schema(description = "Number of accepted assignments", example = "45")
        private long acceptedCount;

        @Schema(description = "Number of declined assignments", example = "5")
        private long declinedCount;

        @Schema(description = "Number of overdue assignments", example = "12")
        private long overdueCount;

        @Schema(description = "Number of assignments that can be started", example = "30")
        private long canStartCount;

        @Schema(description = "Number of assignments that can be completed", example = "15")
        private long canCompleteCount;
    }
}
