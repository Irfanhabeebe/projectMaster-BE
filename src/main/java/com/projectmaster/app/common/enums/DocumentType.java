package com.projectmaster.app.common.enums;

/**
 * Enumeration for document types based on file format
 */
public enum DocumentType {
    IMAGE("Image files (JPG, PNG, GIF, etc.)"),
    PDF("PDF documents"),
    DOCUMENT("Text documents (DOC, DOCX, TXT, etc.)"),
    SPREADSHEET("Spreadsheet files (XLS, XLSX, CSV, etc.)"),
    VIDEO("Video files (MP4, AVI, MOV, etc.)"),
    OTHER("Other file types");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determine document type from MIME type
     */
    public static DocumentType fromMimeType(String mimeType) {
        if (mimeType == null) {
            return OTHER;
        }
        
        String lowerMimeType = mimeType.toLowerCase();
        
        if (lowerMimeType.startsWith("image/")) {
            return IMAGE;
        } else if (lowerMimeType.equals("application/pdf")) {
            return PDF;
        } else if (lowerMimeType.startsWith("text/") || 
                   lowerMimeType.contains("document") ||
                   lowerMimeType.contains("word") ||
                   lowerMimeType.contains("rtf")) {
            return DOCUMENT;
        } else if (lowerMimeType.contains("spreadsheet") ||
                   lowerMimeType.contains("excel") ||
                   lowerMimeType.contains("csv")) {
            return SPREADSHEET;
        } else if (lowerMimeType.startsWith("video/")) {
            return VIDEO;
        } else {
            return OTHER;
        }
    }
}