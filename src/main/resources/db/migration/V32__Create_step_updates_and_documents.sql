-- Create step_updates table
CREATE TABLE IF NOT EXISTS step_updates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_step_id UUID NOT NULL,
    updated_by_user_id UUID NOT NULL,
    update_type VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    comments TEXT,
    progress_percentage INTEGER CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    update_date TIMESTAMP NOT NULL,
    blockers TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_step_updates_project_step FOREIGN KEY (project_step_id) REFERENCES project_steps(id) ON DELETE CASCADE,
    CONSTRAINT fk_step_updates_user FOREIGN KEY (updated_by_user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create step_update_documents table
CREATE TABLE IF NOT EXISTS step_update_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    step_update_id UUID NOT NULL,
       file_name VARCHAR(255) NOT NULL,
       original_file_name VARCHAR(255),
       file_extension VARCHAR(10),
       mime_type VARCHAR(100),
       document_type VARCHAR(50) NOT NULL,
    description TEXT,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_step_update_documents_step_update FOREIGN KEY (step_update_id) REFERENCES step_updates(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_step_updates_project_step_id ON step_updates(project_step_id);
CREATE INDEX IF NOT EXISTS idx_step_updates_updated_by_user_id ON step_updates(updated_by_user_id);
CREATE INDEX IF NOT EXISTS idx_step_updates_update_type ON step_updates(update_type);
CREATE INDEX IF NOT EXISTS idx_step_updates_update_date ON step_updates(update_date DESC);
CREATE INDEX IF NOT EXISTS idx_step_updates_is_milestone ON step_updates(is_milestone);

CREATE INDEX IF NOT EXISTS idx_step_update_documents_step_update_id ON step_update_documents(step_update_id);
CREATE INDEX IF NOT EXISTS idx_step_update_documents_document_type ON step_update_documents(document_type);
CREATE INDEX IF NOT EXISTS idx_step_update_documents_upload_date ON step_update_documents(upload_date DESC);

-- Create composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_step_updates_project_step_update_date ON step_updates(project_step_id, update_date DESC);

-- Create trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_step_updates_updated_at 
    BEFORE UPDATE ON step_updates 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_step_update_documents_updated_at 
    BEFORE UPDATE ON step_update_documents 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
