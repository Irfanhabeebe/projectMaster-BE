package com.projectmaster.app.project.entity;

import com.projectmaster.app.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "step_update_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StepUpdateDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_update_id", nullable = false)
    private StepUpdate stepUpdate;

    @Column(name = "file_name", nullable = false)
    private String fileName; // Unique system filename

    @Column(name = "original_file_name")
    private String originalFileName; // Display filename

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "document_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (uploadDate == null) {
            uploadDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
    }

    public enum DocumentType {
        PHOTO,
        VIDEO,
        DOCUMENT,
        DRAWING,
        SPECIFICATION,
        INSPECTION_REPORT,
        SAFETY_CERTIFICATE,
        QUALITY_CHECKLIST,
        MEASUREMENT_RECORD,
        PERMIT,
        INVOICE,
        RECEIPT,
        OTHER
    }
}
