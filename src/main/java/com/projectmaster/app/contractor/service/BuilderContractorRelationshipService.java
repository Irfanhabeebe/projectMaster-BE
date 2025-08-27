package com.projectmaster.app.contractor.service;

import com.projectmaster.app.common.dto.ApiResponse;
import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.contractor.dto.*;
import com.projectmaster.app.contractor.entity.*;
import com.projectmaster.app.contractor.repository.BuilderContractorRelationshipRepository;
import com.projectmaster.app.contractor.repository.ContractingCompanyRepository;
import com.projectmaster.app.user.entity.Company;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.CompanyRepository;
import com.projectmaster.app.workflow.entity.Specialty;
import com.projectmaster.app.workflow.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BuilderContractorRelationshipService {

    private final BuilderContractorRelationshipRepository relationshipRepository;
    private final CompanyRepository companyRepository;
    private final ContractingCompanyRepository contractingCompanyRepository;
    private final SpecialtyService specialtyService;

    /**
     * Create a new builder-contractor relationship
     */
    public BuilderContractorRelationshipResponse createRelationship(
            UUID builderCompanyId, 
            CreateBuilderContractorRelationshipRequest request, 
            User currentUser) {
        
        log.info("Creating builder-contractor relationship for builder company: {}", builderCompanyId);

        // Validate builder company
        Company builderCompany = companyRepository.findById(builderCompanyId)
                .orElseThrow(() -> new EntityNotFoundException("Builder Company", builderCompanyId));

        // Validate contracting company
        ContractingCompany contractingCompany = contractingCompanyRepository.findById(request.getContractingCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Contracting Company", request.getContractingCompanyId()));

        // Check if relationship already exists
        if (relationshipRepository.existsByBuilderCompanyIdAndContractingCompanyId(builderCompanyId, request.getContractingCompanyId())) {
            throw new RuntimeException("Relationship already exists between these companies");
        }

        // Create relationship
        BuilderContractorRelationship relationship = BuilderContractorRelationship.builder()
                .builderCompany(builderCompany)
                .contractingCompany(contractingCompany)
                .addedByUser(currentUser)
                .contractStartDate(request.getContractStartDate())
                .contractEndDate(request.getContractEndDate())
                .paymentTerms(request.getPaymentTerms())
                .notes(request.getNotes())
                .active(true)
                .build();

        relationship = relationshipRepository.save(relationship);

        // Add specialties if provided
        if (request.getSpecialties() != null) {
            for (CreateBuilderContractorRelationshipRequest.RelationshipSpecialtyRequest specialtyRequest : request.getSpecialties()) {
                Specialty specialty = specialtyService.getSpecialtyById(specialtyRequest.getSpecialtyId())
                        .orElseThrow(() -> new EntityNotFoundException("Specialty", specialtyRequest.getSpecialtyId()));

                BuilderContractorSpecialty relationshipSpecialty = BuilderContractorSpecialty.builder()
                        .builderContractorRelationship(relationship)
                        .specialty(specialty)
                        .customNotes(specialtyRequest.getCustomNotes())
                        .preferredRating(specialtyRequest.getPreferredRating())
                        .hourlyRate(specialtyRequest.getHourlyRate())
                        .availabilityStatus(specialtyRequest.getAvailabilityStatus())
                        .active(true)
                        .build();

                relationship.getSpecialties().add(relationshipSpecialty);
            }
        }

        relationship = relationshipRepository.save(relationship);
        return mapToResponse(relationship);
    }

    /**
     * Get relationship by ID
     */
    public BuilderContractorRelationshipResponse getRelationshipById(UUID relationshipId) {
        BuilderContractorRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new EntityNotFoundException("Builder-Contractor Relationship", relationshipId));
        return mapToResponse(relationship);
    }

    /**
     * Get all relationships for a builder company
     */
    public Page<BuilderContractorRelationshipResponse> getRelationshipsForBuilder(
            UUID builderCompanyId, 
            BuilderContractorRelationshipSearchRequest searchRequest) {
        
        // Build pageable
        Sort sort = Sort.by(
            Sort.Direction.fromString(searchRequest.getSortDirection()), 
            searchRequest.getSortBy()
        );
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        // Search with filters using repository
        Page<BuilderContractorRelationship> relationships = relationshipRepository.searchRelationships(
                builderCompanyId,
                searchRequest.getActiveOnly(),
                searchRequest.getSearchText(),
                searchRequest.getSpecialtyType(),
                searchRequest.getSpecialtyName(),
                searchRequest.getAvailabilityStatus(),
                pageable
        );

        return relationships.map(this::mapToResponse);
    }

    /**
     * Update relationship
     */
    public BuilderContractorRelationshipResponse updateRelationship(
            UUID relationshipId, 
            UpdateBuilderContractorRelationshipRequest request) {
        
        BuilderContractorRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new EntityNotFoundException("Builder-Contractor Relationship", relationshipId));

        // Update fields
        if (request.getContractStartDate() != null) {
            relationship.setContractStartDate(request.getContractStartDate());
        }
        if (request.getContractEndDate() != null) {
            relationship.setContractEndDate(request.getContractEndDate());
        }
        if (request.getPaymentTerms() != null) {
            relationship.setPaymentTerms(request.getPaymentTerms());
        }
        if (request.getNotes() != null) {
            relationship.setNotes(request.getNotes());
        }
        if (request.getActive() != null) {
            relationship.setActive(request.getActive());
        }

        // Update specialties if provided
        if (request.getSpecialties() != null) {
            // Clear existing specialties
            relationship.getSpecialties().clear();

            // Add new specialties
            for (UpdateBuilderContractorRelationshipRequest.RelationshipSpecialtyRequest specialtyRequest : request.getSpecialties()) {
                Specialty specialty = specialtyService.getSpecialtyById(specialtyRequest.getSpecialtyId())
                        .orElseThrow(() -> new EntityNotFoundException("Specialty", specialtyRequest.getSpecialtyId()));

                BuilderContractorSpecialty relationshipSpecialty = BuilderContractorSpecialty.builder()
                        .builderContractorRelationship(relationship)
                        .specialty(specialty)
                        .customNotes(specialtyRequest.getCustomNotes())
                        .preferredRating(specialtyRequest.getPreferredRating())
                        .hourlyRate(specialtyRequest.getHourlyRate())
                        .availabilityStatus(specialtyRequest.getAvailabilityStatus())
                        .active(specialtyRequest.getActive() != null ? specialtyRequest.getActive() : true)
                        .build();

                relationship.getSpecialties().add(relationshipSpecialty);
            }
        }

        relationship = relationshipRepository.save(relationship);
        return mapToResponse(relationship);
    }

    /**
     * Deactivate relationship
     */
    public void deactivateRelationship(UUID relationshipId) {
        BuilderContractorRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new EntityNotFoundException("Builder-Contractor Relationship", relationshipId));
        
        relationship.setActive(false);
        relationshipRepository.save(relationship);
        log.info("Deactivated builder-contractor relationship: {}", relationshipId);
    }

    /**
     * Activate relationship
     */
    public void activateRelationship(UUID relationshipId) {
        BuilderContractorRelationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new EntityNotFoundException("Builder-Contractor Relationship", relationshipId));
        
        relationship.setActive(true);
        relationshipRepository.save(relationship);
        log.info("Activated builder-contractor relationship: {}", relationshipId);
    }

    /**
     * Map entity to response DTO
     */
    private BuilderContractorRelationshipResponse mapToResponse(BuilderContractorRelationship relationship) {
        return BuilderContractorRelationshipResponse.builder()
                .id(relationship.getId())
                .builderCompanyId(relationship.getBuilderCompany().getId())
                .builderCompanyName(relationship.getBuilderCompany().getName())
                .contractingCompanyId(relationship.getContractingCompany().getId())
                .contractingCompanyName(relationship.getContractingCompany().getName())
                .contractingCompanyAbn(relationship.getContractingCompany().getAbn())
                .contractingCompanyEmail(relationship.getContractingCompany().getEmail())
                .contractingCompanyPhone(relationship.getContractingCompany().getPhone())
                .contractingCompanyContactPerson(relationship.getContractingCompany().getContactPerson())
                .addedByUserId(relationship.getAddedByUser().getId())
                .addedByUserName(relationship.getAddedByUser().getFullName())
                .addedAt(relationship.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .active(relationship.getActive())
                .contractStartDate(relationship.getContractStartDate())
                .contractEndDate(relationship.getContractEndDate())
                .paymentTerms(relationship.getPaymentTerms())
                .notes(relationship.getNotes())
                .specialties(relationship.getSpecialties().stream()
                        .map(this::mapSpecialtyToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private BuilderContractorRelationshipResponse.RelationshipSpecialtyResponse mapSpecialtyToResponse(
            BuilderContractorSpecialty relationshipSpecialty) {
        return BuilderContractorRelationshipResponse.RelationshipSpecialtyResponse.builder()
                .id(relationshipSpecialty.getId())
                .specialtyId(relationshipSpecialty.getSpecialty().getId())
                .specialtyType(relationshipSpecialty.getSpecialty().getSpecialtyType())
                .specialtyName(relationshipSpecialty.getSpecialty().getSpecialtyName())
                .customNotes(relationshipSpecialty.getCustomNotes())
                .preferredRating(relationshipSpecialty.getPreferredRating())
                .hourlyRate(relationshipSpecialty.getHourlyRate())
                .availabilityStatus(relationshipSpecialty.getAvailabilityStatus())
                .active(relationshipSpecialty.getActive())
                .build();
    }
}
