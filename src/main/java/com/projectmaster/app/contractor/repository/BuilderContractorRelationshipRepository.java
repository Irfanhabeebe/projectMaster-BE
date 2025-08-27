package com.projectmaster.app.contractor.repository;

import com.projectmaster.app.contractor.entity.BuilderContractorRelationship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BuilderContractorRelationshipRepository extends JpaRepository<BuilderContractorRelationship, UUID> {

    // Find all relationships for a builder company
    Page<BuilderContractorRelationship> findByBuilderCompanyId(UUID builderCompanyId, Pageable pageable);
    
    // Find active relationships for a builder company
    List<BuilderContractorRelationship> findByBuilderCompanyIdAndActiveTrue(UUID builderCompanyId);
    
    // Find relationships by contracting company
    List<BuilderContractorRelationship> findByContractingCompanyId(UUID contractingCompanyId);
    
    // Check if relationship exists
    boolean existsByBuilderCompanyIdAndContractingCompanyId(UUID builderCompanyId, UUID contractingCompanyId);
    
    // Search with filters - Case sensitive text search to avoid bytea issue
    @Query("SELECT bcr FROM BuilderContractorRelationship bcr " +
           "JOIN bcr.builderCompany bc " +
           "JOIN bcr.contractingCompany cc " +
           "WHERE bc.id = :builderCompanyId " +
           "AND (:activeOnly = false OR bcr.active = true) " +
           "AND (:searchText IS NULL OR :searchText = '' OR " +
           "    cc.name LIKE CONCAT('%', :searchText, '%') OR " +
           "    cc.abn LIKE CONCAT('%', :searchText, '%') OR " +
           "    cc.email LIKE CONCAT('%', :searchText, '%') OR " +
           "    cc.contactPerson LIKE CONCAT('%', :searchText, '%')) " +
           "AND (:specialtyType IS NULL OR EXISTS (" +
           "    SELECT 1 FROM BuilderContractorSpecialty s " +
           "    JOIN s.specialty sp " +
           "    WHERE sp.specialtyType = :specialtyType AND s.builderContractorRelationship.id = bcr.id)) " +
           "AND (:specialtyName IS NULL OR EXISTS (" +
           "    SELECT 1 FROM BuilderContractorSpecialty s " +
           "    JOIN s.specialty sp " +
           "    WHERE sp.specialtyName = :specialtyName AND s.builderContractorRelationship.id = bcr.id)) " +
           "AND (:availabilityStatus IS NULL OR EXISTS (" +
           "    SELECT 1 FROM BuilderContractorSpecialty s " +
           "    WHERE s.availabilityStatus = :availabilityStatus AND s.builderContractorRelationship.id = bcr.id))")
    Page<BuilderContractorRelationship> searchRelationships(
            @Param("builderCompanyId") UUID builderCompanyId,
            @Param("activeOnly") Boolean activeOnly,
            @Param("searchText") String searchText,
            @Param("specialtyType") String specialtyType,
            @Param("specialtyName") String specialtyName,
            @Param("availabilityStatus") String availabilityStatus,
            Pageable pageable);
}
