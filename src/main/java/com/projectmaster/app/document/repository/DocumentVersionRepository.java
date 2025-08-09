package com.projectmaster.app.document.repository;

import com.projectmaster.app.document.entity.DocumentVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for DocumentVersion entity
 */
@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    // Find all versions for a document
    List<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(UUID documentId);
    
    Page<DocumentVersion> findByDocumentIdOrderByVersionNumberDesc(UUID documentId, Pageable pageable);
    
    // Find specific version of a document
    Optional<DocumentVersion> findByDocumentIdAndVersionNumber(UUID documentId, Integer versionNumber);
    
    // Find latest version of a document
    @Query("SELECT dv FROM DocumentVersion dv WHERE dv.document.id = :documentId " +
           "AND dv.versionNumber = (SELECT MAX(dv2.versionNumber) FROM DocumentVersion dv2 WHERE dv2.document.id = :documentId)")
    Optional<DocumentVersion> findLatestVersionByDocumentId(@Param("documentId") UUID documentId);
    
    // Find versions created by user
    List<DocumentVersion> findByCreatedByIdOrderByCreatedAtDesc(UUID userId);
    
    // Count versions for a document
    long countByDocumentId(UUID documentId);
    
    // Find versions by checksum (for duplicate detection)
    List<DocumentVersion> findByChecksum(String checksum);
    
    // Find versions by filename pattern
    @Query("SELECT dv FROM DocumentVersion dv WHERE LOWER(dv.filename) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<DocumentVersion> findByFilenameContainingIgnoreCase(@Param("pattern") String pattern);
    
    // Calculate total size of all versions for a document
    @Query("SELECT COALESCE(SUM(dv.fileSize), 0) FROM DocumentVersion dv WHERE dv.document.id = :documentId")
    Long getTotalVersionsSizeByDocument(@Param("documentId") UUID documentId);
    
    // Find versions larger than specified size
    @Query("SELECT dv FROM DocumentVersion dv WHERE dv.fileSize > :size ORDER BY dv.fileSize DESC")
    List<DocumentVersion> findByFileSizeGreaterThan(@Param("size") Long size);
    
    // Delete old versions (keep only latest N versions)
    @Query("SELECT dv FROM DocumentVersion dv WHERE dv.document.id = :documentId " +
           "AND dv.versionNumber NOT IN " +
           "(SELECT dv2.versionNumber FROM DocumentVersion dv2 WHERE dv2.document.id = :documentId " +
           "ORDER BY dv2.versionNumber DESC LIMIT :keepCount)")
    List<DocumentVersion> findOldVersionsToDelete(@Param("documentId") UUID documentId, @Param("keepCount") int keepCount);
}