-- Migration to add adhoc task support
-- This allows tasks to be created manually without workflow templates

-- Add adhoc_task_flag column to project_tasks table
ALTER TABLE project_tasks ADD COLUMN IF NOT EXISTS adhoc_task_flag BOOLEAN NOT NULL DEFAULT false;

-- Make workflow_task_id nullable to support adhoc tasks
-- Adhoc tasks won't have a workflow_task_id since they're not based on templates
ALTER TABLE project_tasks ALTER COLUMN workflow_task_id DROP NOT NULL;

-- Add comment for clarity
COMMENT ON COLUMN project_tasks.adhoc_task_flag IS 'Flag indicating if this is an adhoc task (true) or a template-based task (false). Adhoc tasks are manually created and don''t have workflow_task_id.';
COMMENT ON COLUMN project_tasks.workflow_task_id IS 'Reference to workflow task template. NULL for adhoc tasks.';

-- Create index for querying adhoc tasks
CREATE INDEX IF NOT EXISTS idx_project_tasks_adhoc_flag ON project_tasks(adhoc_task_flag) WHERE adhoc_task_flag = true;

