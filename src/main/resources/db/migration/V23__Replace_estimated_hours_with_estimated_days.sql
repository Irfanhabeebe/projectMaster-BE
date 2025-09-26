-- Replace estimated_hours with estimated_days across all workflow entities
-- This migration renames the estimated_hours columns to estimated_days for better project planning

-- Update standard_workflow_tasks table
ALTER TABLE standard_workflow_tasks 
    RENAME COLUMN estimated_hours TO estimated_days;

-- Update workflow_tasks table  
ALTER TABLE workflow_tasks 
    RENAME COLUMN estimated_hours TO estimated_days;

-- Update project_tasks table
ALTER TABLE project_tasks 
    RENAME COLUMN estimated_hours TO estimated_days;

-- Update standard_workflow_steps table
ALTER TABLE standard_workflow_steps 
    RENAME COLUMN estimated_hours TO estimated_days;

-- Update workflow_steps table  
ALTER TABLE workflow_steps 
    RENAME COLUMN estimated_hours TO estimated_days;

-- Update project_steps table
ALTER TABLE project_steps 
    RENAME COLUMN estimated_hours TO estimated_days;

-- Update project_step_assignments table
ALTER TABLE project_step_assignments 
    RENAME COLUMN estimated_hours TO estimated_days;

-- Update adhoc_tasks table
ALTER TABLE adhoc_tasks 
    RENAME COLUMN estimated_hours TO estimated_days;

-- Optional: Update existing data to convert hours to days (assuming 8 hours = 1 day)
-- Uncomment the following lines if you want to convert existing data
-- UPDATE standard_workflow_steps SET estimated_days = CEIL(estimated_days / 8.0) WHERE estimated_days IS NOT NULL;
-- UPDATE workflow_steps SET estimated_days = CEIL(estimated_days / 8.0) WHERE estimated_days IS NOT NULL;
-- UPDATE project_steps SET estimated_days = CEIL(estimated_days / 8.0) WHERE estimated_days IS NOT NULL;
-- UPDATE project_step_assignments SET estimated_days = CEIL(estimated_days / 8.0) WHERE estimated_days IS NOT NULL;

-- Add comments to the columns for clarity
COMMENT ON COLUMN standard_workflow_tasks.estimated_days IS 'Estimated duration in business days for this task';
COMMENT ON COLUMN workflow_tasks.estimated_days IS 'Estimated duration in business days for this task';
COMMENT ON COLUMN project_tasks.estimated_days IS 'Estimated duration in business days for this task';
COMMENT ON COLUMN standard_workflow_steps.estimated_days IS 'Estimated duration in business days for this step';
COMMENT ON COLUMN workflow_steps.estimated_days IS 'Estimated duration in business days for this step';
COMMENT ON COLUMN project_steps.estimated_days IS 'Estimated duration in business days for this step';
COMMENT ON COLUMN project_step_assignments.estimated_days IS 'Estimated duration in business days for this assignment';
COMMENT ON COLUMN adhoc_tasks.estimated_days IS 'Estimated duration in business days for this adhoc task';
