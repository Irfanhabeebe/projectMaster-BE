-- Create step_status enum with READY_TO_START included
-- This migration creates the step_status enum with all required values

-- First, check if step_status exists, if not create it
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'step_status') THEN
        CREATE TYPE step_status AS ENUM ('NOT_STARTED', 'READY_TO_START', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED', 'BLOCKED');
    ELSE
        -- If it exists, add READY_TO_START if not already present
        IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'READY_TO_START' AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'step_status')) THEN
            ALTER TYPE step_status ADD VALUE 'READY_TO_START';
        END IF;
    END IF;
END $$;

-- Update any existing project_steps that might need this status
-- (This is optional - you can run this if you want to update existing data)
-- UPDATE project_steps SET status = 'READY_TO_START' WHERE status = 'NOT_STARTED' AND [some condition];

-- Note: The enum now supports: 'NOT_STARTED', 'READY_TO_START', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED', 'BLOCKED'
