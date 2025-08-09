-- Drop the old address column from projects table
-- This is safe for a new POC with no existing data

-- Step 1: Drop the old address column
ALTER TABLE projects DROP COLUMN address;

-- Step 2: Verify the change
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'projects' 
ORDER BY ordinal_position; 