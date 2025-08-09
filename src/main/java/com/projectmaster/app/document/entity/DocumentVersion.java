package com.projectmaster.app.document.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entity representing a version of a document for version control
 */
@Entity
@Table(name = "document_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @NotNull
    private Document document;

    @Column(name = "version_number", nullable = false)
    @Positive
    private Integer versionNumber;

    @Column(name = "filename", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String filename;

    @Lob
    @Column(name = "file_content", nullable = false)
    @NotNull
    private byte[] fileContent;

    @Column(name = "file_size", nullable = false)
    @Positive
    private Long fileSize;

    @Column(name = "mime_type", nullable = false)
    @NotBlank
    @Size(max = 100)
    private String mimeType;

    @Column(name = "checksum")
    @Size(max = 64)
    private String checksum;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @NotNull
    private User createdBy;

    // Utility methods
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}