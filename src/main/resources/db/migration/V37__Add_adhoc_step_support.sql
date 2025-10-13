-- Add adhoc step support to project_steps table
-- Make workflow_step_id nullable to support adhoc steps
ALTER TABLE project_steps 
    ALTER COLUMN workflow_step_id DROP NOT NULL;

-- Make order_index nullable as dependencies will determine order
ALTER TABLE project_steps 
    ALTER COLUMN order_index DROP NOT NULL;

-- Add adhoc_step_flag to track manually created steps
ALTER TABLE project_steps 
    ADD COLUMN IF NOT EXISTS adhoc_step_flag BOOLEAN NOT NULL DEFAULT false;

-- Add workflow_rebuild_required flag to projects table
ALTER TABLE projects 
    ADD COLUMN IF NOT EXISTS workflow_rebuild_required BOOLEAN NOT NULL DEFAULT false;

-- Add index for querying adhoc steps
CREATE INDEX IF NOT EXISTS idx_project_steps_adhoc_flag ON project_steps(adhoc_step_flag);

-- Add index for querying projects needing rebuild
CREATE INDEX IF NOT EXISTS idx_projects_rebuild_required ON projects(workflow_rebuild_required);

-- Add comments for clarity
COMMENT ON COLUMN project_steps.adhoc_step_flag IS 'Flag indicating if this step was manually added by a project manager/admin (true) or copied from workflow template (false)';
COMMENT ON COLUMN project_steps.workflow_step_id IS 'Reference to the workflow step template. Nullable to support adhoc steps that are not based on templates';
COMMENT ON COLUMN project_steps.order_index IS 'Legacy ordering field. Nullable as dependencies determine execution order. Will be deprecated in future releases.';
COMMENT ON COLUMN projects.workflow_rebuild_required IS 'Flag indicating if the project schedule needs to be recalculated due to adhoc changes like adding steps or dependencies';

