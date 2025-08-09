-- Fix version columns that are causing issues
-- This script should be run manually to update the database structure

-- Fix workflow_stages table version column
ALTER TABLE workflow_stages ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE workflow_stages SET version = 1 WHERE version IS NULL;

-- Fix workflow_steps table version column
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE workflow_steps SET version = 1 WHERE version IS NULL;

-- Fix standard_workflow_stages table version column
ALTER TABLE standard_workflow_stages ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE standard_workflow_stages SET version = 1 WHERE version IS NULL;

-- Fix standard_workflow_steps table version column
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE standard_workflow_steps SET version = 1 WHERE version IS NULL;

-- Fix workflow_tasks table version column
ALTER TABLE workflow_tasks ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE workflow_tasks SET version = 1 WHERE version IS NULL;

-- Fix standard_workflow_tasks table version column
ALTER TABLE standard_workflow_tasks ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE standard_workflow_tasks SET version = 1 WHERE version IS NULL;

-- Fix project_tasks table version column
ALTER TABLE project_tasks ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE project_tasks SET version = 1 WHERE version IS NULL;

-- Fix project_steps table version column
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE project_steps SET version = 1 WHERE version IS NULL; 