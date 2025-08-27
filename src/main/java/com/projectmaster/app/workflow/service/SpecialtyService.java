package com.projectmaster.app.workflow.service;

import com.projectmaster.app.workflow.entity.Specialty;
import com.projectmaster.app.workflow.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    /**
     * Get all active specialties ordered by order index
     */
    public List<Specialty> getAllActiveSpecialties() {
        return specialtyRepository.findByActiveTrueOrderByOrderIndexAsc();
    }

    /**
     * Get specialties by type
     */
    public List<Specialty> getSpecialtiesByType(String specialtyType) {
        return specialtyRepository.findBySpecialtyTypeAndActiveTrueOrderByOrderIndexAsc(specialtyType);
    }

    /**
     * Get all specialty types
     */
    public List<String> getAllSpecialtyTypes() {
        return specialtyRepository.findAll().stream()
                .map(Specialty::getSpecialtyType)
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Find specialty by name (case-insensitive)
     */
    public Optional<Specialty> findSpecialtyByName(String specialtyName) {
        return Optional.ofNullable(specialtyRepository.findBySpecialtyNameIgnoreCase(specialtyName));
    }

    /**
     * Search specialties by name containing the given text
     */
    public List<Specialty> searchSpecialtiesByName(String searchText) {
        return specialtyRepository.findBySpecialtyNameContainingIgnoreCase(searchText);
    }

    /**
     * Get specialty by ID
     */
    public Optional<Specialty> getSpecialtyById(UUID id) {
        return specialtyRepository.findById(id);
    }

    /**
     * Create a new specialty
     */
    public Specialty createSpecialty(Specialty specialty) {
        log.info("Creating new specialty: {}", specialty.getSpecialtyName());
        return specialtyRepository.save(specialty);
    }

    /**
     * Update an existing specialty
     */
    public Specialty updateSpecialty(UUID id, Specialty specialtyDetails) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialty not found with id: " + id));
        
        specialty.setSpecialtyType(specialtyDetails.getSpecialtyType());
        specialty.setSpecialtyName(specialtyDetails.getSpecialtyName());
        specialty.setDescription(specialtyDetails.getDescription());
        specialty.setActive(specialtyDetails.getActive());
        specialty.setOrderIndex(specialtyDetails.getOrderIndex());
        
        log.info("Updating specialty: {}", specialty.getSpecialtyName());
        return specialtyRepository.save(specialty);
    }

    /**
     * Deactivate a specialty
     */
    public void deactivateSpecialty(UUID id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialty not found with id: " + id));
        
        specialty.setActive(false);
        specialtyRepository.save(specialty);
        log.info("Deactivated specialty: {}", specialty.getSpecialtyName());
    }

    /**
     * Get specialties suitable for a specific construction phase
     */
    public List<Specialty> getSpecialtiesForConstructionPhase(String phase) {
        switch (phase.toLowerCase()) {
            case "site_preparation":
            case "site preparation":
                return getSpecialtiesByType("Site Preparation");
            case "foundation":
            case "concrete":
                return getSpecialtiesByType("Concrete & Foundations");
            case "framing":
            case "carpentry":
                return getSpecialtiesByType("Structural Carpentry");
            case "roofing":
                return getSpecialtiesByType("Roofing");
            case "masonry":
            case "cladding":
                return getSpecialtiesByType("Masonry & Cladding");
            case "windows":
            case "doors":
            case "external_finishes":
                return getSpecialtiesByType("Windows, Doors & External Finishes");
            case "plumbing":
                return getSpecialtiesByType("Plumbing & Drainage");
            case "electrical":
                return getSpecialtiesByType("Electrical");
            case "hvac":
            case "mechanical":
                return getSpecialtiesByType("HVAC & Mechanical");
            case "internal_works":
            case "internal works":
                return getSpecialtiesByType("Internal Works");
            case "joinery":
            case "fixtures":
                return getSpecialtiesByType("Joinery & Fixtures");
            case "final_stages":
            case "final stages":
                return getSpecialtiesByType("Final Stages");
            default:
                return getAllActiveSpecialties();
        }
    }
}
