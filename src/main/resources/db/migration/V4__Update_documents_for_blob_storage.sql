-- Update documents table to support BLOB storage
-- Drop the existing documents table and recreate with BLOB support

-- First, drop existing indexes and constraints
DROP INDEX IF EXISTS idx_documents_project_id;
DROP INDEX IF EXISTS idx_documents_task_id;
DROP INDEX IF EXISTS idx_documents_type;
DROP INDEX IF EXISTS idx_documents_uploaded_by;

-- Drop the existing documents table
DROP TABLE IF EXISTS documents;

-- Create new document category enum
CREATE TYPE document_category AS ENUM (
    'PROJECT_PLANS', 
    'PERMITS', 
    'CONTRACTS', 
    'INVOICES', 
    'PHOTOS', 
    'REPORTS', 
    'CERTIFICATES', 
    'CORRESPONDENCE', 
    'SPECIFICATIONS',
    'SAFETY_DOCUMENTS',
    'QUALITY_ASSURANCE',
    'OTHER'
);

-- Create updated documents table with BLOB storage
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_content BYTEA NOT NULL, -- BLOB storage for file content
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    document_type document_type NOT NULL,
    document_category document_category NOT NULL DEFAULT 'OTHER',
    description TEXT,
    tags JSONB,
    version INTEGER DEFAULT 1,
    is_public BOOLEAN DEFAULT false,
    is_archived BOOLEAN DEFAULT false,
    checksum VARCHAR(64), -- SHA-256 checksum for integrity
    metadata JSONB, -- Additional metadata like image dimensions, etc.
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_document_reference CHECK (
        (project_id IS NOT NULL AND task_id IS NULL) OR 
        (project_id IS NULL AND task_id IS NOT NULL)
    ),
    CONSTRAINT chk_file_size CHECK (file_size > 0),
    CONSTRAINT chk_version CHECK (version > 0)
);

-- Create document versions table for version control
CREATE TABLE document_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_content BYTEA NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    checksum VARCHAR(64),
    description TEXT,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(document_id, version_number)
);

-- Create document access log table for audit trail
CREATE TABLE document_access_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_type VARCHAR(20) NOT NULL, -- 'VIEW', 'DOWNLOAD', 'UPLOAD', 'UPDATE', 'DELETE'
    ip_address INET,
    user_agent TEXT,
    accessed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create document sharing table for sharing documents with external users
CREATE TABLE document_shares (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    shared_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    share_token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    password_hash VARCHAR(255), -- Optional password protection
    download_limit INTEGER, -- Optional download limit
    download_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_documents_project_id ON documents(project_id);
CREATE INDEX idx_documents_task_id ON documents(task_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_category ON documents(document_category);
CREATE INDEX idx_documents_uploaded_by ON documents(uploaded_by);
CREATE INDEX idx_documents_created_at ON documents(created_at);
CREATE INDEX idx_documents_filename ON documents(filename);
CREATE INDEX idx_documents_tags ON documents USING gin(tags);
CREATE INDEX idx_documents_archived ON documents(is_archived);

CREATE INDEX idx_document_versions_document_id ON document_versions(document_id);
CREATE INDEX idx_document_versions_created_at ON document_versions(created_at);

CREATE INDEX idx_document_access_log_document_id ON document_access_log(document_id);
CREATE INDEX idx_document_access_log_user_id ON document_access_log(user_id);
CREATE INDEX idx_document_access_log_accessed_at ON document_access_log(accessed_at);

CREATE INDEX idx_document_shares_document_id ON document_shares(document_id);
CREATE INDEX idx_document_shares_token ON document_shares(share_token);
CREATE INDEX idx_document_shares_expires_at ON document_shares(expires_at);
CREATE INDEX idx_document_shares_active ON document_shares(is_active);

-- Create trigger to update updated_at timestamp
CREATE TRIGGER trigger_documents_updated_at 
    BEFORE UPDATE ON documents 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Create function to automatically create document version on update
CREATE OR REPLACE FUNCTION create_document_version()
RETURNS TRIGGER AS $$
BEGIN
    -- Only create version if file content has changed
    IF OLD.file_content IS DISTINCT FROM NEW.file_content THEN
        INSERT INTO document_versions (
            document_id, 
            version_number, 
            filename, 
            file_content, 
            file_size, 
            mime_type, 
            checksum, 
            description, 
            created_by
        ) VALUES (
            OLD.id,
            OLD.version,
            OLD.filename,
            OLD.file_content,
            OLD.file_size,
            OLD.mime_type,
            OLD.checksum,
            'Auto-versioned on update',
            NEW.uploaded_by
        );
        
        -- Increment version number
        NEW.version = OLD.version + 1;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for automatic versioning
CREATE TRIGGER trigger_create_document_version
    BEFORE UPDATE ON documents
    FOR EACH ROW
    WHEN (OLD.file_content IS DISTINCT FROM NEW.file_content)
    EXECUTE FUNCTION create_document_version();

-- Add comments for documentation
COMMENT ON TABLE documents IS 'Stores documents with BLOB content for projects and tasks';
COMMENT ON COLUMN documents.file_content IS 'Binary content of the document stored as BYTEA';
COMMENT ON COLUMN documents.checksum IS 'SHA-256 checksum for file integrity verification';
COMMENT ON COLUMN documents.metadata IS 'Additional metadata like image dimensions, document properties, etc.';

COMMENT ON TABLE document_versions IS 'Stores previous versions of documents for version control';
COMMENT ON TABLE document_access_log IS 'Audit trail for document access and operations';
COMMENT ON TABLE document_shares IS 'Manages external sharing of documents with tokens and expiration';