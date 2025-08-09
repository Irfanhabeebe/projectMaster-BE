-- V8: Update standard_workflow_steps to use 3-layer structure (Stages -> Tasks -> Steps)
-- This migration updates the standard_workflow_steps table to reference tasks instead of stages directly

-- First, add the new column for task reference
ALTER TABLE standard_workflow_steps ADD COLUMN standard_workflow_task_id UUID;

-- Create the standard_workflow_tasks table if it doesn't exist
CREATE TABLE IF NOT EXISTS standard_workflow_tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    standard_workflow_stage_id UUID NOT NULL REFERENCES standard_workflow_stages(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    estimated_hours INTEGER,
    required_skills JSONB,
    requirements JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint for the new task reference
ALTER TABLE standard_workflow_steps ADD CONSTRAINT fk_standard_workflow_steps_task 
    FOREIGN KEY (standard_workflow_task_id) REFERENCES standard_workflow_tasks(id) ON DELETE CASCADE;

-- Create index for the new foreign key
CREATE INDEX idx_standard_workflow_steps_task_id ON standard_workflow_steps(standard_workflow_task_id);

-- For existing data, we'll create default tasks for each stage
-- This is a temporary migration to preserve existing data
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at)
SELECT 
    uuid_generate_v4(),
    sws.id,
    sws.name || ' - Default Task',
    'Default task created during migration from 2-layer to 3-layer structure',
    1,
    8,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM standard_workflow_stages sws;

-- Update existing steps to reference the default tasks
UPDATE standard_workflow_steps 
SET standard_workflow_task_id = (
    SELECT swt.id 
    FROM standard_workflow_tasks swt 
    WHERE swt.standard_workflow_stage_id = standard_workflow_steps.standard_workflow_stage_id
    LIMIT 1
);

-- Make the task reference NOT NULL
ALTER TABLE standard_workflow_steps ALTER COLUMN standard_workflow_task_id SET NOT NULL;

-- Drop the old stage reference column
ALTER TABLE standard_workflow_steps DROP COLUMN standard_workflow_stage_id;

-- Drop the old index
DROP INDEX IF EXISTS idx_standard_workflow_steps_stage_id;

-- Create new index for task-based ordering
CREATE INDEX idx_standard_workflow_steps_task_order ON standard_workflow_steps(standard_workflow_task_id, order_index); 