-- V22: Enhance project dependencies for parallel execution and critical path analysis

-- Add critical path analysis fields to project_dependencies table
ALTER TABLE project_dependencies ADD COLUMN IF NOT EXISTS is_critical_path BOOLEAN DEFAULT false;
ALTER TABLE project_dependencies ADD COLUMN IF NOT EXISTS slack_days INTEGER DEFAULT 0;
ALTER TABLE project_dependencies ADD COLUMN IF NOT EXISTS expected_duration_days INTEGER;
ALTER TABLE project_dependencies ADD COLUMN IF NOT EXISTS notes TEXT;

-- Add parallel execution support to existing workflow entities
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE project_tasks ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;

-- Add estimated duration fields for better critical path calculation
ALTER TABLE project_tasks ADD COLUMN IF NOT EXISTS estimated_duration_days INTEGER;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS estimated_duration_hours INTEGER;

-- Create indexes for performance optimization
CREATE INDEX IF NOT EXISTS idx_project_dependencies_critical_path ON project_dependencies(project_id, is_critical_path);
CREATE INDEX IF NOT EXISTS idx_project_dependencies_slack ON project_dependencies(project_id, slack_days);
CREATE INDEX IF NOT EXISTS idx_project_tasks_parallel ON project_tasks(project_stage_id, parallel_execution);
CREATE INDEX IF NOT EXISTS idx_project_steps_parallel ON project_steps(project_task_id, parallel_execution);

-- Add comments for clarity
COMMENT ON COLUMN project_dependencies.is_critical_path IS 'Indicates if this dependency is part of the critical path';
COMMENT ON COLUMN project_dependencies.slack_days IS 'Number of days this dependency can be delayed without affecting project completion';
COMMENT ON COLUMN project_dependencies.expected_duration_days IS 'Expected duration for the dependency relationship';
COMMENT ON COLUMN project_tasks.parallel_execution IS 'Indicates if this task can run in parallel with other tasks in the same stage';
COMMENT ON COLUMN project_steps.parallel_execution IS 'Indicates if this step can run in parallel with other steps in the same task';
