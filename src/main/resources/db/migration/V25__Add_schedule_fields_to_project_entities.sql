-- Add schedule fields to project entities for calculated dates
-- This supports the ProjectScheduleCalculator service

-- Add schedule fields to project_stages
ALTER TABLE project_stages ADD COLUMN planned_start_date DATE;
ALTER TABLE project_stages ADD COLUMN planned_end_date DATE;
ALTER TABLE project_stages ADD COLUMN is_critical_path BOOLEAN DEFAULT FALSE;
ALTER TABLE project_stages ADD COLUMN slack_days INTEGER DEFAULT 0;
ALTER TABLE project_stages ADD COLUMN progress_percentage DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE project_stages ADD COLUMN last_schedule_update TIMESTAMP;

-- Add schedule fields to project_tasks
ALTER TABLE project_tasks ADD COLUMN planned_start_date DATE;
ALTER TABLE project_tasks ADD COLUMN planned_end_date DATE;
ALTER TABLE project_tasks ADD COLUMN is_critical_path BOOLEAN DEFAULT FALSE;
ALTER TABLE project_tasks ADD COLUMN slack_days INTEGER DEFAULT 0;
ALTER TABLE project_tasks ADD COLUMN progress_percentage DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE project_tasks ADD COLUMN last_schedule_update TIMESTAMP;

-- Add schedule fields to project_steps
ALTER TABLE project_steps ADD COLUMN planned_start_date DATE;
ALTER TABLE project_steps ADD COLUMN planned_end_date DATE;
ALTER TABLE project_steps ADD COLUMN is_critical_path BOOLEAN DEFAULT FALSE;
ALTER TABLE project_steps ADD COLUMN slack_days INTEGER DEFAULT 0;
ALTER TABLE project_steps ADD COLUMN progress_percentage DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE project_steps ADD COLUMN last_schedule_update TIMESTAMP;

-- Add schedule fields to adhoc_tasks
ALTER TABLE adhoc_tasks ADD COLUMN planned_start_date DATE;
ALTER TABLE adhoc_tasks ADD COLUMN planned_end_date DATE;
ALTER TABLE adhoc_tasks ADD COLUMN is_critical_path BOOLEAN DEFAULT FALSE;
ALTER TABLE adhoc_tasks ADD COLUMN slack_days INTEGER DEFAULT 0;
ALTER TABLE adhoc_tasks ADD COLUMN progress_percentage DECIMAL(5,2) DEFAULT 0.00;
ALTER TABLE adhoc_tasks ADD COLUMN last_schedule_update TIMESTAMP;

-- Add schedule fields to projects table
ALTER TABLE projects ADD COLUMN planned_start_date DATE;
ALTER TABLE projects ADD COLUMN planned_end_date DATE;
ALTER TABLE projects ADD COLUMN actual_start_date DATE;
ALTER TABLE projects ADD COLUMN actual_end_date DATE;
ALTER TABLE projects ADD COLUMN last_schedule_calculation TIMESTAMP;
ALTER TABLE projects ADD COLUMN schedule_calculation_method VARCHAR(50) DEFAULT 'DEPENDENCY_BASED';

-- Create indexes for performance
CREATE INDEX idx_project_stages_planned_dates ON project_stages(planned_start_date, planned_end_date);
CREATE INDEX idx_project_stages_critical_path ON project_stages(is_critical_path);
CREATE INDEX idx_project_stages_progress ON project_stages(progress_percentage);

CREATE INDEX idx_project_tasks_planned_dates ON project_tasks(planned_start_date, planned_end_date);
CREATE INDEX idx_project_tasks_critical_path ON project_tasks(is_critical_path);
CREATE INDEX idx_project_tasks_progress ON project_tasks(progress_percentage);

CREATE INDEX idx_project_steps_planned_dates ON project_steps(planned_start_date, planned_end_date);
CREATE INDEX idx_project_steps_critical_path ON project_steps(is_critical_path);
CREATE INDEX idx_project_steps_progress ON project_steps(progress_percentage);

CREATE INDEX idx_adhoc_tasks_planned_dates ON adhoc_tasks(planned_start_date, planned_end_date);
CREATE INDEX idx_adhoc_tasks_critical_path ON adhoc_tasks(is_critical_path);
CREATE INDEX idx_adhoc_tasks_progress ON adhoc_tasks(progress_percentage);

CREATE INDEX idx_projects_planned_dates ON projects(planned_start_date, planned_end_date);
CREATE INDEX idx_projects_schedule_calc ON projects(last_schedule_calculation);

-- Add comments
COMMENT ON COLUMN project_stages.planned_start_date IS 'Calculated planned start date based on dependencies';
COMMENT ON COLUMN project_stages.planned_end_date IS 'Calculated planned end date based on dependencies and estimated days';
COMMENT ON COLUMN project_stages.is_critical_path IS 'Whether this stage is on the critical path';
COMMENT ON COLUMN project_stages.slack_days IS 'Number of days this stage can be delayed without affecting project end date';
COMMENT ON COLUMN project_stages.progress_percentage IS 'Current progress percentage (0.00 to 100.00)';
COMMENT ON COLUMN project_stages.last_schedule_update IS 'Timestamp of last schedule calculation update';

COMMENT ON COLUMN project_tasks.planned_start_date IS 'Calculated planned start date based on dependencies';
COMMENT ON COLUMN project_tasks.planned_end_date IS 'Calculated planned end date based on dependencies and estimated days';
COMMENT ON COLUMN project_tasks.is_critical_path IS 'Whether this task is on the critical path';
COMMENT ON COLUMN project_tasks.slack_days IS 'Number of days this task can be delayed without affecting project end date';
COMMENT ON COLUMN project_tasks.progress_percentage IS 'Current progress percentage (0.00 to 100.00)';
COMMENT ON COLUMN project_tasks.last_schedule_update IS 'Timestamp of last schedule calculation update';

COMMENT ON COLUMN project_steps.planned_start_date IS 'Calculated planned start date based on dependencies';
COMMENT ON COLUMN project_steps.planned_end_date IS 'Calculated planned end date based on dependencies and estimated days';
COMMENT ON COLUMN project_steps.is_critical_path IS 'Whether this step is on the critical path';
COMMENT ON COLUMN project_steps.slack_days IS 'Number of days this step can be delayed without affecting project end date';
COMMENT ON COLUMN project_steps.progress_percentage IS 'Current progress percentage (0.00 to 100.00)';
COMMENT ON COLUMN project_steps.last_schedule_update IS 'Timestamp of last schedule calculation update';

COMMENT ON COLUMN adhoc_tasks.planned_start_date IS 'Calculated planned start date based on dependencies';
COMMENT ON COLUMN adhoc_tasks.planned_end_date IS 'Calculated planned end date based on dependencies and estimated days';
COMMENT ON COLUMN adhoc_tasks.is_critical_path IS 'Whether this adhoc task is on the critical path';
COMMENT ON COLUMN adhoc_tasks.slack_days IS 'Number of days this adhoc task can be delayed without affecting project end date';
COMMENT ON COLUMN adhoc_tasks.progress_percentage IS 'Current progress percentage (0.00 to 100.00)';
COMMENT ON COLUMN adhoc_tasks.last_schedule_update IS 'Timestamp of last schedule calculation update';

COMMENT ON COLUMN projects.planned_start_date IS 'Calculated planned project start date';
COMMENT ON COLUMN projects.planned_end_date IS 'Calculated planned project end date based on critical path';
COMMENT ON COLUMN projects.actual_start_date IS 'Actual project start date';
COMMENT ON COLUMN projects.actual_end_date IS 'Actual project end date';
COMMENT ON COLUMN projects.last_schedule_calculation IS 'Timestamp of last schedule calculation';
COMMENT ON COLUMN projects.schedule_calculation_method IS 'Method used for schedule calculation (DEPENDENCY_BASED, MANUAL, etc.)';
