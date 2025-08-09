package com.projectmaster.app.document.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import com.projectmaster.app.common.enums.DocumentCategory;
import com.projectmaster.app.common.enums.DocumentType;
import com.projectmaster.app.project.entity.Project;
import com.projectmaster.app.task.entity.Task;
import com.projectmaster.app.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

/**
 * Entity representing a document with BLOB storage
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @NotNull
    private User uploadedBy;

    @Column(name = "filename", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    @NotBlank
    @Size(max = 255)
    private String originalFilename;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    @NotNull
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_category", nullable = false)
    @NotNull
    private DocumentCategory documentCategory = DocumentCategory.OTHER;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "tags", columnDefinition = "jsonb")
    private String tags; // JSON string for tags

    @Builder.Default
    @Column(name = "version", nullable = false)
    @Positive
    private Integer version = 1;

    @Builder.Default
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Builder.Default
    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @Column(name = "checksum")
    @Size(max = 64)
    private String checksum;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // JSON string for additional metadata

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DocumentVersion> versions;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DocumentAccessLog> accessLogs;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DocumentShare> shares;

    // Utility methods
    public boolean isProjectDocument() {
        return project != null;
    }

    public boolean isTaskDocument() {
        return task != null;
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}