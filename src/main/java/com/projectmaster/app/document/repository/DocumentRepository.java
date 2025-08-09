package com.projectmaster.app.document.repository;

import com.projectmaster.app.common.enums.DocumentCategory;
import com.projectmaster.app.common.enums.DocumentType;
import com.projectmaster.app.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Document entity
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    // Find documents by project
    List<Document> findByProjectIdAndIsArchivedFalse(UUID projectId);
    
    Page<Document> findByProjectIdAndIsArchivedFalse(UUID projectId, Pageable pageable);
    
    // Find documents by task
    List<Document> findByTaskIdAndIsArchivedFalse(UUID taskId);
    
    Page<Document> findByTaskIdAndIsArchivedFalse(UUID taskId, Pageable pageable);
    
    // Find documents by type
    List<Document> findByDocumentTypeAndIsArchivedFalse(DocumentType documentType);
    
    // Find documents by category
    List<Document> findByDocumentCategoryAndIsArchivedFalse(DocumentCategory documentCategory);
    
    // Find documents by project and category
    List<Document> findByProjectIdAndDocumentCategoryAndIsArchivedFalse(
        UUID projectId, DocumentCategory documentCategory);
    
    // Find documents by task and category
    List<Document> findByTaskIdAndDocumentCategoryAndIsArchivedFalse(
        UUID taskId, DocumentCategory documentCategory);
    
    // Find public documents
    List<Document> findByIsPublicTrueAndIsArchivedFalse();
    
    // Find documents uploaded by user
    List<Document> findByUploadedByIdAndIsArchivedFalse(UUID userId);
    
    Page<Document> findByUploadedByIdAndIsArchivedFalse(UUID userId, Pageable pageable);
    
    // Find documents by filename pattern
    @Query("SELECT d FROM Document d WHERE d.filename LIKE %:pattern% AND d.isArchived = false")
    List<Document> findByFilenameContainingIgnoreCaseAndIsArchivedFalse(@Param("pattern") String pattern);
    
    // Find documents by original filename pattern
    @Query("SELECT d FROM Document d WHERE LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :pattern, '%')) AND d.isArchived = false")
    List<Document> findByOriginalFilenameContainingIgnoreCaseAndIsArchivedFalse(@Param("pattern") String pattern);
    
    // Find documents by checksum (for duplicate detection)
    Optional<Document> findByChecksumAndIsArchivedFalse(String checksum);
    
    List<Document> findAllByChecksumAndIsArchivedFalse(String checksum);
    
    // Find documents created within date range
    @Query("SELECT d FROM Document d WHERE d.createdAt BETWEEN :startDate AND :endDate AND d.isArchived = false")
    List<Document> findByCreatedAtBetweenAndIsArchivedFalse(
        @Param("startDate") Instant startDate, 
        @Param("endDate") Instant endDate);
    
    // Find documents by size range
    @Query("SELECT d FROM Document d WHERE d.fileSize BETWEEN :minSize AND :maxSize AND d.isArchived = false")
    List<Document> findByFileSizeBetweenAndIsArchivedFalse(
        @Param("minSize") Long minSize, 
        @Param("maxSize") Long maxSize);
    
    // Find large documents (over specified size)
    @Query("SELECT d FROM Document d WHERE d.fileSize > :size AND d.isArchived = false")
    List<Document> findByFileSizeGreaterThanAndIsArchivedFalse(@Param("size") Long size);
    
    // Complex search query
    @Query("SELECT d FROM Document d WHERE " +
           "(:projectId IS NULL OR d.project.id = :projectId) AND " +
           "(:taskId IS NULL OR d.task.id = :taskId) AND " +
           "(:documentType IS NULL OR d.documentType = :documentType) AND " +
           "(:documentCategory IS NULL OR d.documentCategory = :documentCategory) AND " +
           "(:uploadedById IS NULL OR d.uploadedBy.id = :uploadedById) AND " +
           "(:searchTerm IS NULL OR LOWER(d.filename) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(d.originalFilename) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           " LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "d.isArchived = false")
    Page<Document> searchDocuments(
        @Param("projectId") UUID projectId,
        @Param("taskId") UUID taskId,
        @Param("documentType") DocumentType documentType,
        @Param("documentCategory") DocumentCategory documentCategory,
        @Param("uploadedById") UUID uploadedById,
        @Param("searchTerm") String searchTerm,
        Pageable pageable);
    
    // Count documents by project
    long countByProjectIdAndIsArchivedFalse(UUID projectId);
    
    // Count documents by task
    long countByTaskIdAndIsArchivedFalse(UUID taskId);
    
    // Count documents by type
    long countByDocumentTypeAndIsArchivedFalse(DocumentType documentType);
    
    // Count documents by category
    long countByDocumentCategoryAndIsArchivedFalse(DocumentCategory documentCategory);
    
    // Calculate total file size by project
    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM Document d WHERE d.project.id = :projectId AND d.isArchived = false")
    Long getTotalFileSizeByProject(@Param("projectId") UUID projectId);
    
    // Calculate total file size by task
    @Query("SELECT COALESCE(SUM(d.fileSize), 0) FROM Document d WHERE d.task.id = :taskId AND d.isArchived = false")
    Long getTotalFileSizeByTask(@Param("taskId") UUID taskId);
    
    // Find documents that need cleanup (archived and old)
    @Query("SELECT d FROM Document d WHERE d.isArchived = true AND d.updatedAt < :cutoffDate")
    List<Document> findArchivedDocumentsOlderThan(@Param("cutoffDate") Instant cutoffDate);
    
    // Find documents by company (through project or task)
    @Query("SELECT DISTINCT d FROM Document d WHERE " +
           "(d.project IS NOT NULL AND d.project.company.id = :companyId) OR " +
           "(d.task IS NOT NULL AND d.task.projectStep.projectTask.projectStage.project.company.id = :companyId) " +
           "AND d.isArchived = false")
    List<Document> findByCompanyIdAndIsArchivedFalse(@Param("companyId") UUID companyId);
    
    @Query("SELECT DISTINCT d FROM Document d WHERE " +
           "(d.project IS NOT NULL AND d.project.company.id = :companyId) OR " +
           "(d.task IS NOT NULL AND d.task.projectStep.projectTask.projectStage.project.company.id = :companyId) " +
           "AND d.isArchived = false")
    Page<Document> findByCompanyIdAndIsArchivedFalse(@Param("companyId") UUID companyId, Pageable pageable);
}