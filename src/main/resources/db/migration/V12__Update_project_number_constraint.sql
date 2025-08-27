-- Remove the global unique constraint on project_number
-- First, find the constraint name (it might be different in your database)
-- You can check with: SELECT conname FROM pg_constraint WHERE conrelid = 'projects'::regclass AND contype = 'u';

-- Drop the existing unique constraint (replace 'uk7v20lprro6l97vftxcdhpejbo' with the actual constraint name from your database)
-- ALTER TABLE projects DROP CONSTRAINT IF EXISTS uk7v20lprro6l97vftxcdhpejbo;

-- Alternative approach: Drop all unique constraints on project_number column
DO $$
DECLARE
    constraint_name text;
BEGIN
    -- Find and drop the unique constraint on project_number
    SELECT conname INTO constraint_name 
    FROM pg_constraint 
    WHERE conrelid = 'projects'::regclass 
      AND contype = 'u' 
      AND EXISTS (
          SELECT 1 FROM pg_attribute 
          WHERE attrelid = conrelid 
            AND attname = 'project_number' 
            AND attnum = ANY(conkey)
      );
    
    IF constraint_name IS NOT NULL THEN
        EXECUTE 'ALTER TABLE projects DROP CONSTRAINT ' || constraint_name;
        RAISE NOTICE 'Dropped constraint: %', constraint_name;
    ELSE
        RAISE NOTICE 'No unique constraint found on project_number column';
    END IF;
END $$;

-- Add the new composite unique constraint
ALTER TABLE projects ADD CONSTRAINT uk_projects_company_project_number UNIQUE (company_id, project_number);

-- Create an index for better performance on the composite key
CREATE INDEX idx_projects_company_project_number ON projects(company_id, project_number);
