package com.projectmaster.app.workflow.controller;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.workflow.entity.Specialty;
import com.projectmaster.app.workflow.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/specialties")
@RequiredArgsConstructor
@Slf4j
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    /**
     * Get all active specialties
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Specialty>>> getAllSpecialties() {
        try {
            List<Specialty> specialties = specialtyService.getAllActiveSpecialties();
            return ResponseEntity.ok(ApiResponse.success(specialties, "Specialties retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving specialties", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve specialties: " + e.getMessage()));
        }
    }

    /**
     * Get specialties by type
     */
    @GetMapping("/type/{specialtyType}")
    public ResponseEntity<ApiResponse<List<Specialty>>> getSpecialtiesByType(@PathVariable String specialtyType) {
        try {
            List<Specialty> specialties = specialtyService.getSpecialtiesByType(specialtyType);
            return ResponseEntity.ok(ApiResponse.success(specialties, 
                "Specialties for type '" + specialtyType + "' retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving specialties by type: {}", specialtyType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve specialties by type: " + e.getMessage()));
        }
    }

    /**
     * Get all specialty types
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<String>>> getAllSpecialtyTypes() {
        try {
            List<String> types = specialtyService.getAllSpecialtyTypes();
            return ResponseEntity.ok(ApiResponse.success(types, "Specialty types retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving specialty types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve specialty types: " + e.getMessage()));
        }
    }

    /**
     * Get specialty by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Specialty>> getSpecialtyById(@PathVariable UUID id) {
        try {
            return specialtyService.getSpecialtyById(id)
                    .map(specialty -> ResponseEntity.ok(ApiResponse.success(specialty, "Specialty retrieved successfully")))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving specialty with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve specialty: " + e.getMessage()));
        }
    }

    /**
     * Search specialties by name
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Specialty>>> searchSpecialties(@RequestParam String name) {
        try {
            List<Specialty> specialties = specialtyService.searchSpecialtiesByName(name);
            return ResponseEntity.ok(ApiResponse.success(specialties, 
                "Specialties matching '" + name + "' retrieved successfully"));
        } catch (Exception e) {
            log.error("Error searching specialties by name: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to search specialties: " + e.getMessage()));
        }
    }

    /**
     * Get specialties for construction phase
     */
    @GetMapping("/phase/{phase}")
    public ResponseEntity<ApiResponse<List<Specialty>>> getSpecialtiesForPhase(@PathVariable String phase) {
        try {
            List<Specialty> specialties = specialtyService.getSpecialtiesForConstructionPhase(phase);
            return ResponseEntity.ok(ApiResponse.success(specialties, 
                "Specialties for phase '" + phase + "' retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving specialties for phase: {}", phase, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve specialties for phase: " + e.getMessage()));
        }
    }

    /**
     * Create a new specialty
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Specialty>> createSpecialty(@RequestBody Specialty specialty) {
        try {
            Specialty createdSpecialty = specialtyService.createSpecialty(specialty);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdSpecialty, "Specialty created successfully"));
        } catch (Exception e) {
            log.error("Error creating specialty", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create specialty: " + e.getMessage()));
        }
    }

    /**
     * Update an existing specialty
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Specialty>> updateSpecialty(@PathVariable UUID id, @RequestBody Specialty specialty) {
        try {
            Specialty updatedSpecialty = specialtyService.updateSpecialty(id, specialty);
            return ResponseEntity.ok(ApiResponse.success(updatedSpecialty, "Specialty updated successfully"));
        } catch (Exception e) {
            log.error("Error updating specialty with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update specialty: " + e.getMessage()));
        }
    }

    /**
     * Deactivate a specialty
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateSpecialty(@PathVariable UUID id) {
        try {
            specialtyService.deactivateSpecialty(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Specialty deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deactivating specialty with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to deactivate specialty: " + e.getMessage()));
        }
    }
}
