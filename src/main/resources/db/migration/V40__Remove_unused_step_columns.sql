-- Remove unused columns from project_steps, workflow_steps, and standard_workflow_steps
-- These columns are no longer needed:
-- - order_index: Steps are ordered by creation time and dependencies
-- - required_skills: Not used in the current implementation
-- - requirements: Not used in the current implementation

-- Drop columns from project_steps
ALTER TABLE project_steps DROP COLUMN IF EXISTS order_index;
ALTER TABLE project_steps DROP COLUMN IF EXISTS required_skills;
ALTER TABLE project_steps DROP COLUMN IF EXISTS requirements;

-- Drop columns from workflow_steps
ALTER TABLE workflow_steps DROP COLUMN IF EXISTS order_index;
ALTER TABLE workflow_steps DROP COLUMN IF EXISTS required_skills;
ALTER TABLE workflow_steps DROP COLUMN IF EXISTS requirements;

-- Drop columns from standard_workflow_steps
ALTER TABLE standard_workflow_steps DROP COLUMN IF EXISTS order_index;
ALTER TABLE standard_workflow_steps DROP COLUMN IF EXISTS required_skills;
ALTER TABLE standard_workflow_steps DROP COLUMN IF EXISTS requirements;

-- Add comments for documentation
COMMENT ON TABLE project_steps IS 'Project steps - ordered by creation time and dependencies, not by explicit order index';
COMMENT ON TABLE workflow_steps IS 'Workflow template steps - ordered by creation time and dependencies';
COMMENT ON TABLE standard_workflow_steps IS 'Standard workflow steps - ordered by creation time and dependencies';

