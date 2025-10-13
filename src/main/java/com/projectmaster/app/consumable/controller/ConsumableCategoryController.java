package com.projectmaster.app.consumable.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.consumable.entity.ConsumableCategory;
import com.projectmaster.app.consumable.service.ConsumableCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/consumable-categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consumable Categories", description = "API for managing consumable categories")
public class ConsumableCategoryController {

    private final ConsumableCategoryService consumableCategoryService;

    /**
     * Get all active consumable categories
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Get all active consumable categories", description = "Retrieve all active consumable categories ordered by display order")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<ConsumableCategory>>> getAllCategories() {
        try {
            List<ConsumableCategory> categories = consumableCategoryService.getAllActiveCategories();
            return ResponseEntity.ok(ApiResponse.<List<ConsumableCategory>>builder()
                    .success(true)
                    .message("Categories retrieved successfully")
                    .data(categories)
                    .build());
        } catch (Exception e) {
            log.error("Error retrieving consumable categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ConsumableCategory>>builder()
                            .success(false)
                            .message("Failed to retrieve categories: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Get consumable category by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Get consumable category by ID", description = "Retrieve a specific consumable category by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<ConsumableCategory>> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable UUID id) {
        try {
            return consumableCategoryService.getCategoryById(id)
                    .map(category -> ResponseEntity.ok(ApiResponse.<ConsumableCategory>builder()
                            .success(true)
                            .message("Category retrieved successfully")
                            .data(category)
                            .build()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving consumable category with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ConsumableCategory>builder()
                            .success(false)
                            .message("Failed to retrieve category: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Search consumable categories by name
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Search consumable categories", description = "Search consumable categories by name")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<ConsumableCategory>>> searchCategories(
            @Parameter(description = "Search text") @RequestParam String searchText) {
        try {
            List<ConsumableCategory> categories = consumableCategoryService.searchCategories(searchText);
            return ResponseEntity.ok(ApiResponse.<List<ConsumableCategory>>builder()
                    .success(true)
                    .message("Search completed successfully")
                    .data(categories)
                    .build());
        } catch (Exception e) {
            log.error("Error searching consumable categories", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<List<ConsumableCategory>>builder()
                            .success(false)
                            .message("Failed to search categories: " + e.getMessage())
                            .build());
        }
    }
}
