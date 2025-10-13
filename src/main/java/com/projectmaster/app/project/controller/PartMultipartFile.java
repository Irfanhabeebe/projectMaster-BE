package com.projectmaster.app.project.controller;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Adapter class to convert javax.servlet.http.Part to Spring's MultipartFile
 */
public class PartMultipartFile implements MultipartFile {
    
    private final Part part;
    
    public PartMultipartFile(Part part) {
        this.part = part;
    }
    
    @Override
    @NonNull
    public String getName() {
        return part.getName();
    }
    
    @Override
    public String getOriginalFilename() {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            String[] parts = contentDisposition.split(";");
            for (String part : parts) {
                if (part.trim().startsWith("filename=")) {
                    String filename = part.substring(part.indexOf("=") + 1).trim();
                    // Remove quotes if present
                    if (filename.startsWith("\"") && filename.endsWith("\"")) {
                        filename = filename.substring(1, filename.length() - 1);
                    }
                    return filename;
                }
            }
        }
        return null;
    }
    
    @Override
    public String getContentType() {
        return part.getContentType();
    }
    
    @Override
    public boolean isEmpty() {
        return part.getSize() == 0;
    }
    
    @Override
    public long getSize() {
        return part.getSize();
    }
    
    @Override
    @NonNull
    public byte[] getBytes() throws IOException {
        try (InputStream inputStream = part.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }
    
    @Override
    @NonNull
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }
    
    @Override
    public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
        part.write(dest.getAbsolutePath());
    }
}
