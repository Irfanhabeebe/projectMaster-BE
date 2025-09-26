-- Remove redundant start_date and end_date columns from project entities
-- These are replaced by planned_start_date, planned_end_date, actual_start_date, actual_end_date

-- Remove redundant columns from projects table
ALTER TABLE projects DROP COLUMN IF EXISTS start_date;

-- Remove redundant columns from project_stages table
ALTER TABLE project_stages DROP COLUMN IF EXISTS start_date;
ALTER TABLE project_stages DROP COLUMN IF EXISTS end_date;

-- Remove redundant columns from project_tasks table
ALTER TABLE project_tasks DROP COLUMN IF EXISTS start_date;
ALTER TABLE project_tasks DROP COLUMN IF EXISTS end_date;

-- Remove redundant columns from project_steps table
ALTER TABLE project_steps DROP COLUMN IF EXISTS start_date;
ALTER TABLE project_steps DROP COLUMN IF EXISTS end_date;

-- Remove redundant columns from adhoc_tasks table
ALTER TABLE adhoc_tasks DROP COLUMN IF EXISTS start_date;
ALTER TABLE adhoc_tasks DROP COLUMN IF EXISTS end_date;

-- Add comments explaining the new date structure
COMMENT ON COLUMN projects.planned_start_date IS 'Calculated planned project start date based on dependencies';
COMMENT ON COLUMN projects.planned_end_date IS 'Calculated planned project end date based on critical path';
COMMENT ON COLUMN projects.actual_start_date IS 'Actual project start date when work began';
COMMENT ON COLUMN projects.actual_end_date IS 'Actual project end date when work completed';

COMMENT ON COLUMN project_stages.planned_start_date IS 'Calculated planned stage start date based on dependencies';
COMMENT ON COLUMN project_stages.planned_end_date IS 'Calculated planned stage end date based on dependencies and estimated days';
COMMENT ON COLUMN project_stages.actual_start_date IS 'Actual stage start date when work began';
COMMENT ON COLUMN project_stages.actual_end_date IS 'Actual stage end date when work completed';

COMMENT ON COLUMN project_tasks.planned_start_date IS 'Calculated planned task start date based on dependencies';
COMMENT ON COLUMN project_tasks.planned_end_date IS 'Calculated planned task end date based on dependencies and estimated days';
COMMENT ON COLUMN project_tasks.actual_start_date IS 'Actual task start date when work began';
COMMENT ON COLUMN project_tasks.actual_end_date IS 'Actual task end date when work completed';

COMMENT ON COLUMN project_steps.planned_start_date IS 'Calculated planned step start date based on dependencies';
COMMENT ON COLUMN project_steps.planned_end_date IS 'Calculated planned step end date based on dependencies and estimated days';
COMMENT ON COLUMN project_steps.actual_start_date IS 'Actual step start date when work began';
COMMENT ON COLUMN project_steps.actual_end_date IS 'Actual step end date when work completed';

COMMENT ON COLUMN adhoc_tasks.planned_start_date IS 'Calculated planned adhoc task start date based on dependencies';
COMMENT ON COLUMN adhoc_tasks.planned_end_date IS 'Calculated planned adhoc task end date based on dependencies and estimated days';
COMMENT ON COLUMN adhoc_tasks.actual_start_date IS 'Actual adhoc task start date when work began';
COMMENT ON COLUMN adhoc_tasks.actual_end_date IS 'Actual adhoc task end date when work completed';

