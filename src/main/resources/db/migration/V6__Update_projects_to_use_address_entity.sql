-- Migration to update projects table to use address entity
-- Step 1: Add address_id column
ALTER TABLE projects ADD COLUMN address_id UUID;

-- Step 2: Add foreign key constraint
ALTER TABLE projects ADD CONSTRAINT fk_projects_address_id 
    FOREIGN KEY (address_id) REFERENCES addresses(id);

-- Step 3: Create indexes for better performance
CREATE INDEX idx_projects_address_id ON projects(address_id);

-- Step 4: Make the old address column nullable to allow transition
ALTER TABLE projects ALTER COLUMN address DROP NOT NULL;

-- Step 5: Drop the old address column (after data migration if needed)
-- Note: This should be done after ensuring all existing address data is migrated
-- ALTER TABLE projects DROP COLUMN address; 