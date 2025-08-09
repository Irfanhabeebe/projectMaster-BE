-- Fix for projects table address column constraint issue
-- This script makes the old address column nullable to allow the transition to address_id

-- Step 1: Make the old address column nullable
ALTER TABLE projects ALTER COLUMN address DROP NOT NULL;

-- Step 2: Verify the change
SELECT column_name, is_nullable, data_type 
FROM information_schema.columns 
WHERE table_name = 'projects' AND column_name = 'address'; 