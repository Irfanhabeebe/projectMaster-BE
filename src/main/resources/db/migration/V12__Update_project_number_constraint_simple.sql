-- Simple approach: Drop the specific constraint and add the new one
-- If this fails, you'll need to check the actual constraint name in your database

-- Drop the existing unique constraint (replace with actual constraint name if different)
ALTER TABLE projects DROP CONSTRAINT IF EXISTS uk7v20lprro6l97vftxcdhpejbo;

-- Add the new composite unique constraint
ALTER TABLE projects ADD CONSTRAINT uk_projects_company_project_number UNIQUE (company_id, project_number);

-- Create an index for better performance
CREATE INDEX idx_projects_company_project_number ON projects(company_id, project_number);
