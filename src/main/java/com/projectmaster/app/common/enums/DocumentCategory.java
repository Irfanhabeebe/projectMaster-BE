package com.projectmaster.app.common.enums;

/**
 * Enumeration for document categories in construction projects
 */
public enum DocumentCategory {
    PROJECT_PLANS("Project Plans", "Architectural drawings, blueprints, and design documents"),
    PERMITS("Permits", "Building permits, licenses, and regulatory approvals"),
    CONTRACTS("Contracts", "Contracts, agreements, and legal documents"),
    INVOICES("Invoices", "Invoices, receipts, and billing documents"),
    PHOTOS("Photos", "Progress photos, site images, and visual documentation"),
    REPORTS("Reports", "Progress reports, inspection reports, and status updates"),
    CERTIFICATES("Certificates", "Certificates, warranties, and compliance documents"),
    CORRESPONDENCE("Correspondence", "Emails, letters, and communication records"),
    SPECIFICATIONS("Specifications", "Technical specifications and material lists"),
    SAFETY_DOCUMENTS("Safety Documents", "Safety plans, risk assessments, and incident reports"),
    QUALITY_ASSURANCE("Quality Assurance", "Quality control documents and test results"),
    OTHER("Other", "Miscellaneous documents not fitting other categories");

    private final String displayName;
    private final String description;

    DocumentCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get category by display name (case-insensitive)
     */
    public static DocumentCategory fromDisplayName(String displayName) {
        if (displayName == null) {
            return OTHER;
        }
        
        for (DocumentCategory category : values()) {
            if (category.displayName.equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        return OTHER;
    }

    /**
     * Check if this category is typically used for project-level documents
     */
    public boolean isProjectLevel() {
        return this == PROJECT_PLANS || 
               this == PERMITS || 
               this == CONTRACTS || 
               this == SPECIFICATIONS ||
               this == SAFETY_DOCUMENTS;
    }

    /**
     * Check if this category is typically used for task-level documents
     */
    public boolean isTaskLevel() {
        return this == PHOTOS || 
               this == REPORTS || 
               this == QUALITY_ASSURANCE ||
               this == CORRESPONDENCE;
    }
}