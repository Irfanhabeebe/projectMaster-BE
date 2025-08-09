package com.projectmaster.app.document.exception;

/**
 * Exception thrown when document upload fails
 */
public class DocumentUploadException extends DocumentException {
    
    public DocumentUploadException(String message) {
        super(message);
    }
    
    public DocumentUploadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static DocumentUploadException fileTooLarge(long fileSize, long maxSize) {
        return new DocumentUploadException(
            String.format("File size %d bytes exceeds maximum allowed size of %d bytes", fileSize, maxSize)
        );
    }
    
    public static DocumentUploadException unsupportedFileType(String mimeType) {
        return new DocumentUploadException("Unsupported file type: " + mimeType);
    }
    
    public static DocumentUploadException emptyFile() {
        return new DocumentUploadException("Cannot upload empty file");
    }
    
    public static DocumentUploadException invalidFileName(String filename) {
        return new DocumentUploadException("Invalid filename: " + filename);
    }
}