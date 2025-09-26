-- Create workflow dependencies table for template-level dependencies
CREATE TABLE workflow_dependencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_template_id UUID NOT NULL REFERENCES workflow_templates(id) ON DELETE CASCADE,
    dependent_entity_type VARCHAR(20) NOT NULL, -- 'TASK' or 'STEP'
    dependent_entity_id UUID NOT NULL, -- References workflow_task_id or workflow_step_id
    depends_on_entity_type VARCHAR(20) NOT NULL, -- 'TASK' or 'STEP'
    depends_on_entity_id UUID NOT NULL, -- References workflow_task_id or workflow_step_id
    dependency_type VARCHAR(20) NOT NULL, -- 'FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH'
    lag_days INTEGER DEFAULT 0, -- Delay in days after dependency completes
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_dependency_types CHECK (dependent_entity_type IN ('TASK', 'STEP') AND depends_on_entity_type IN ('TASK', 'STEP')),
    CONSTRAINT chk_dependency_type_values CHECK (dependency_type IN ('FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH'))
);

-- Create project dependencies table for runtime dependencies
CREATE TABLE project_dependencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    dependent_entity_type VARCHAR(20) NOT NULL, -- 'TASK', 'STEP', or 'ADHOC_TASK'
    dependent_entity_id UUID NOT NULL, -- References project_task_id, project_step_id, or adhoc_task_id
    depends_on_entity_type VARCHAR(20) NOT NULL, -- 'TASK', 'STEP', or 'ADHOC_TASK'
    depends_on_entity_id UUID NOT NULL, -- References project_task_id, project_step_id, or adhoc_task_id
    dependency_type VARCHAR(20) NOT NULL,
    lag_days INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING', -- 'PENDING', 'SATISFIED', 'BLOCKED'
    satisfied_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_project_dependency_types CHECK (dependent_entity_type IN ('TASK', 'STEP', 'ADHOC_TASK') AND depends_on_entity_type IN ('TASK', 'STEP', 'ADHOC_TASK')),
    CONSTRAINT chk_project_dependency_type_values CHECK (dependency_type IN ('FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH')),
    CONSTRAINT chk_project_dependency_status CHECK (status IN ('PENDING', 'SATISFIED', 'BLOCKED'))
);

-- Add parallel execution flags to existing workflow tables
ALTER TABLE workflow_stages ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE workflow_tasks ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;

-- Add order indexes for parallel execution (if not already present)
ALTER TABLE workflow_tasks ADD COLUMN IF NOT EXISTS order_index INTEGER;
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS order_index INTEGER;

-- Create indexes for performance
CREATE INDEX idx_workflow_dependencies_template ON workflow_dependencies(workflow_template_id);
CREATE INDEX idx_workflow_dependencies_dependent ON workflow_dependencies(dependent_entity_type, dependent_entity_id);
CREATE INDEX idx_workflow_dependencies_depends_on ON workflow_dependencies(depends_on_entity_type, depends_on_entity_id);

CREATE INDEX idx_project_dependencies_project ON project_dependencies(project_id);
CREATE INDEX idx_project_dependencies_dependent ON project_dependencies(dependent_entity_type, dependent_entity_id);
CREATE INDEX idx_project_dependencies_depends_on ON project_dependencies(depends_on_entity_type, depends_on_entity_id);
CREATE INDEX idx_project_dependencies_status ON project_dependencies(status);

-- Add indexes for workflow tables
CREATE INDEX IF NOT EXISTS idx_workflow_stages_order ON workflow_stages(workflow_template_id, order_index);
CREATE INDEX IF NOT EXISTS idx_workflow_tasks_order ON workflow_tasks(workflow_stage_id, order_index);
CREATE INDEX IF NOT EXISTS idx_workflow_steps_order ON workflow_steps(workflow_task_id, order_index);
