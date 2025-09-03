-- Create project_step_photos table for photo attachments to project steps
CREATE TABLE project_step_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_step_id UUID NOT NULL,
    uploaded_by_user_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    description TEXT,
    photo_type VARCHAR(50),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    tags VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_project_step_photos_project_step 
        FOREIGN KEY (project_step_id) REFERENCES project_steps(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_step_photos_uploaded_by_user 
        FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    CONSTRAINT idx_project_step_photos_project_step_id 
        UNIQUE (project_step_id, id)
);

-- Create indexes for better query performance
CREATE INDEX idx_project_step_photos_project_step_id_created_at 
    ON project_step_photos(project_step_id, created_at DESC);

CREATE INDEX idx_project_step_photos_photo_type 
    ON project_step_photos(project_step_id, photo_type);

CREATE INDEX idx_project_step_photos_uploaded_by_user 
    ON project_step_photos(uploaded_by_user_id);

CREATE INDEX idx_project_step_photos_is_public 
    ON project_step_photos(is_public);

-- Add comments for documentation
COMMENT ON TABLE project_step_photos IS 'Stores photos attached to project steps with metadata';
COMMENT ON COLUMN project_step_photos.project_step_id IS 'Reference to the project step this photo belongs to';
COMMENT ON COLUMN project_step_photos.uploaded_by_user_id IS 'User who uploaded this photo';
COMMENT ON COLUMN project_step_photos.file_name IS 'Generated unique filename for storage';
COMMENT ON COLUMN project_step_photos.original_file_name IS 'Original filename when uploaded';
COMMENT ON COLUMN project_step_photos.file_path IS 'Full path to the stored file';
COMMENT ON COLUMN project_step_photos.file_size IS 'File size in bytes';
COMMENT ON COLUMN project_step_photos.mime_type IS 'MIME type of the file';
COMMENT ON COLUMN project_step_photos.description IS 'Optional description of the photo';
COMMENT ON COLUMN project_step_photos.photo_type IS 'Type of photo (BEFORE, DURING, AFTER, QUALITY_CHECK, ISSUE, PROGRESS, COMPLETION)';
COMMENT ON COLUMN project_step_photos.latitude IS 'GPS latitude coordinate (optional)';
COMMENT ON COLUMN project_step_photos.longitude IS 'GPS longitude coordinate (optional)';
COMMENT ON COLUMN project_step_photos.is_public IS 'Whether this photo is publicly visible';
COMMENT ON COLUMN project_step_photos.tags IS 'Comma-separated tags for categorization';
