-- V21: Create Standard Workflow Dependencies Tables
-- Add dependency support for standard workflow templates (master level)

-- Standard workflow dependencies table for template-level dependencies
CREATE TABLE standard_workflow_dependencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    standard_workflow_template_id UUID NOT NULL REFERENCES standard_workflow_templates(id) ON DELETE CASCADE,
    dependent_entity_type VARCHAR(20) NOT NULL, -- 'STAGE', 'TASK', or 'STEP'
    dependent_entity_id UUID NOT NULL, -- References standard_workflow_stage_id, standard_workflow_task_id, or standard_workflow_step_id
    depends_on_entity_type VARCHAR(20) NOT NULL, -- 'STAGE', 'TASK', or 'STEP'
    depends_on_entity_id UUID NOT NULL, -- References standard_workflow_stage_id, standard_workflow_task_id, or standard_workflow_step_id
    dependency_type VARCHAR(20) NOT NULL, -- 'FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH'
    lag_days INTEGER DEFAULT 0, -- Delay in days after dependency completes
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_standard_dependency_types CHECK (dependent_entity_type IN ('STAGE', 'TASK', 'STEP') AND depends_on_entity_type IN ('STAGE', 'TASK', 'STEP')),
    CONSTRAINT chk_standard_dependency_type_values CHECK (dependency_type IN ('FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH'))
);

-- Add parallel execution flags to existing standard workflow tables (if not already present)
ALTER TABLE standard_workflow_stages ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE standard_workflow_tasks ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;

-- Add order indexes for parallel execution (if not already present)
ALTER TABLE standard_workflow_tasks ADD COLUMN IF NOT EXISTS order_index INTEGER;
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS order_index INTEGER;

-- Create indexes for performance
CREATE INDEX idx_standard_workflow_dependencies_template ON standard_workflow_dependencies(standard_workflow_template_id);
CREATE INDEX idx_standard_workflow_dependencies_dependent ON standard_workflow_dependencies(dependent_entity_type, dependent_entity_id);
CREATE INDEX idx_standard_workflow_dependencies_depends_on ON standard_workflow_dependencies(depends_on_entity_type, depends_on_entity_id);

-- Add indexes for standard workflow tables
CREATE INDEX IF NOT EXISTS idx_standard_workflow_stages_order ON standard_workflow_stages(standard_workflow_template_id, order_index);
CREATE INDEX IF NOT EXISTS idx_standard_workflow_tasks_order ON standard_workflow_tasks(standard_workflow_stage_id, order_index);
CREATE INDEX IF NOT EXISTS idx_standard_workflow_steps_order ON standard_workflow_steps(standard_workflow_task_id, order_index);
