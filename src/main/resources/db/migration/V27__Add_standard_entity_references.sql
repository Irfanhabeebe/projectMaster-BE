-- Add standard entity reference fields to workflow entities
ALTER TABLE workflow_stages ADD COLUMN standard_workflow_stage_id UUID REFERENCES standard_workflow_stages(id);
ALTER TABLE workflow_tasks ADD COLUMN standard_workflow_task_id UUID REFERENCES standard_workflow_tasks(id);
ALTER TABLE workflow_steps ADD COLUMN standard_workflow_step_id UUID REFERENCES standard_workflow_steps(id);

-- Add workflow entity reference fields to project entities (these already exist as foreign keys, but we need them as direct ID fields for easier mapping)
-- Note: These are already present as foreign key relationships, but we'll add them as direct ID fields for easier dependency mapping

-- Add indexes for better performance
CREATE INDEX idx_workflow_stages_standard_stage_id ON workflow_stages(standard_workflow_stage_id);
CREATE INDEX idx_workflow_tasks_standard_task_id ON workflow_tasks(standard_workflow_task_id);
CREATE INDEX idx_workflow_steps_standard_step_id ON workflow_steps(standard_workflow_step_id);
