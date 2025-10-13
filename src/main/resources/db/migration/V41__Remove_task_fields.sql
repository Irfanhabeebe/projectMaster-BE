-- Remove unused columns from project_tasks, workflow_tasks, and standard_workflow_tasks
-- These columns are no longer needed:
-- - order_index: Tasks are ordered by creation time and dependencies
-- - required_skills: Not used in the current implementation
-- - requirements: Not used in the current implementation

-- Drop columns from project_tasks
ALTER TABLE project_tasks DROP COLUMN IF EXISTS order_index;
ALTER TABLE project_tasks DROP COLUMN IF EXISTS required_skills;
ALTER TABLE project_tasks DROP COLUMN IF EXISTS requirements;

-- Drop columns from workflow_tasks
ALTER TABLE workflow_tasks DROP COLUMN IF EXISTS order_index;
ALTER TABLE workflow_tasks DROP COLUMN IF EXISTS required_skills;
ALTER TABLE workflow_tasks DROP COLUMN IF EXISTS requirements;

-- Drop columns from standard_workflow_tasks
ALTER TABLE standard_workflow_tasks DROP COLUMN IF EXISTS order_index;
ALTER TABLE standard_workflow_tasks DROP COLUMN IF EXISTS required_skills;
ALTER TABLE standard_workflow_tasks DROP COLUMN IF EXISTS requirements;

-- Add comments for documentation
COMMENT ON TABLE project_tasks IS 'Project tasks - ordered by creation time and dependencies, not by explicit order index';
COMMENT ON TABLE workflow_tasks IS 'Workflow template tasks - ordered by creation time and dependencies';
COMMENT ON TABLE standard_workflow_tasks IS 'Standard workflow tasks - ordered by creation time and dependencies';

