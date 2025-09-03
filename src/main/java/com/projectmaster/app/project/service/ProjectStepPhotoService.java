package com.projectmaster.app.project.service;

import com.projectmaster.app.common.exception.EntityNotFoundException;
import com.projectmaster.app.common.exception.ProjectMasterException;
import com.projectmaster.app.project.dto.ProjectStepPhotoDto;
import com.projectmaster.app.project.entity.ProjectStep;
import com.projectmaster.app.project.entity.ProjectStepPhoto;
import com.projectmaster.app.project.repository.ProjectStepPhotoRepository;
import com.projectmaster.app.project.repository.ProjectStepRepository;
import com.projectmaster.app.user.entity.User;
import com.projectmaster.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectStepPhotoService {

    private final ProjectStepPhotoRepository photoRepository;
    private final ProjectStepRepository projectStepRepository;
    private final UserRepository userRepository;

    // Configuration - in a real application, these would come from application.properties
    private static final String UPLOAD_DIR = "uploads/project-steps";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

    /**
     * Upload a photo for a project step
     */
    public ProjectStepPhotoDto uploadPhoto(UUID projectStepId, MultipartFile file, String description, 
                                         String photoType, UUID uploadedByUserId) {
        log.info("Uploading photo for project step {} by user {}", projectStepId, uploadedByUserId);

        // Validate project step exists
        ProjectStep projectStep = projectStepRepository.findById(projectStepId)
                .orElseThrow(() -> new EntityNotFoundException("ProjectStep not found with id: " + projectStepId));

        // Validate user exists
        User user = userRepository.findById(uploadedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + uploadedByUserId));

        // Validate file
        validateFile(file);

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR, projectStepId.toString());
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new ProjectMasterException("Failed to create upload directory: " + e.getMessage());
        }

        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = "photo_" + Instant.now().toEpochMilli() + "." + fileExtension;
        Path filePath = uploadPath.resolve(fileName);

        // Save file to disk
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ProjectMasterException("Failed to save file: " + e.getMessage());
        }

        // Create photo entity
        ProjectStepPhoto photo = ProjectStepPhoto.builder()
                .projectStep(projectStep)
                .uploadedByUser(user)
                .fileName(fileName)
                .originalFileName(originalFileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .description(description)
                .photoType(photoType)
                .isPublic(false) // Default to private
                .build();

        ProjectStepPhoto savedPhoto = photoRepository.save(photo);
        log.info("Photo uploaded successfully with ID: {}", savedPhoto.getId());

        return mapToDto(savedPhoto);
    }

    /**
     * Get all photos for a project step
     */
    @Transactional(readOnly = true)
    public List<ProjectStepPhotoDto> getPhotosByProjectStep(UUID projectStepId) {
        log.info("Fetching photos for project step: {}", projectStepId);

        List<ProjectStepPhoto> photos = photoRepository.findByProjectStepIdOrderByCreatedAtDesc(projectStepId);
        return photos.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get photos by project step and photo type
     */
    @Transactional(readOnly = true)
    public List<ProjectStepPhotoDto> getPhotosByProjectStepAndType(UUID projectStepId, String photoType) {
        log.info("Fetching photos for project step {} with type: {}", projectStepId, photoType);

        List<ProjectStepPhoto> photos = photoRepository.findByProjectStepIdAndPhotoTypeOrderByCreatedAtDesc(projectStepId, photoType);
        return photos.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific photo by ID
     */
    @Transactional(readOnly = true)
    public ProjectStepPhotoDto getPhotoById(UUID photoId) {
        log.info("Fetching photo with ID: {}", photoId);

        ProjectStepPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with id: " + photoId));

        return mapToDto(photo);
    }

    /**
     * Update photo metadata
     */
    public ProjectStepPhotoDto updatePhotoMetadata(UUID photoId, String description, String photoType, 
                                                 Boolean isPublic, String tags) {
        log.info("Updating photo metadata for photo: {}", photoId);

        ProjectStepPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with id: " + photoId));

        if (description != null) {
            photo.setDescription(description);
        }
        if (photoType != null) {
            photo.setPhotoType(photoType);
        }
        if (isPublic != null) {
            photo.setIsPublic(isPublic);
        }
        if (tags != null) {
            photo.setTags(tags);
        }

        ProjectStepPhoto savedPhoto = photoRepository.save(photo);
        log.info("Photo metadata updated successfully");

        return mapToDto(savedPhoto);
    }

    /**
     * Delete a photo
     */
    public void deletePhoto(UUID photoId) {
        log.info("Deleting photo with ID: {}", photoId);

        ProjectStepPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found with id: " + photoId));

        // Delete file from disk
        try {
            Files.deleteIfExists(Paths.get(photo.getFilePath()));
        } catch (IOException e) {
            log.warn("Failed to delete file from disk: {}", e.getMessage());
        }

        // Delete from database
        photoRepository.delete(photo);
        log.info("Photo deleted successfully");
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ProjectMasterException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ProjectMasterException("File size exceeds maximum allowed size of " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new ProjectMasterException("File name is null");
        }

        String fileExtension = getFileExtension(originalFileName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new ProjectMasterException("File type not allowed. Allowed types: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ProjectMasterException("File must be an image");
        }
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Format file size in human readable format
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Map entity to DTO
     */
    private ProjectStepPhotoDto mapToDto(ProjectStepPhoto photo) {
        return ProjectStepPhotoDto.builder()
                .id(photo.getId())
                .projectStepId(photo.getProjectStep().getId())
                .uploadedByUserId(photo.getUploadedByUser().getId())
                .uploadedByUserName(photo.getUploadedByUser().getFirstName() + " " + photo.getUploadedByUser().getLastName())
                .fileName(photo.getFileName())
                .originalFileName(photo.getOriginalFileName())
                .filePath(photo.getFilePath())
                .fileSize(photo.getFileSize())
                .mimeType(photo.getMimeType())
                .description(photo.getDescription())
                .photoType(photo.getPhotoType())
                .photoTypeDisplayName(photo.getPhotoType() != null ? 
                        ProjectStepPhoto.PhotoType.valueOf(photo.getPhotoType()).getDisplayName() : null)
                .latitude(photo.getLatitude())
                .longitude(photo.getLongitude())
                .isPublic(photo.getIsPublic())
                .tags(photo.getTags())
                .uploadedAt(photo.getCreatedAt())
                .updatedAt(photo.getUpdatedAt())
                .fileSizeFormatted(formatFileSize(photo.getFileSize()))
                .photoUrl("/api/project-steps/photos/" + photo.getId() + "/view")
                .thumbnailUrl("/api/project-steps/photos/" + photo.getId() + "/thumbnail")
                .build();
    }
}
