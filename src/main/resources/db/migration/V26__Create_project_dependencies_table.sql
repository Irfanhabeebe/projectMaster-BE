-- Create project dependencies table for Phase 2 scheduling
CREATE TABLE project_dependencies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    dependent_entity_id UUID NOT NULL,
    dependent_entity_type VARCHAR(20) NOT NULL CHECK (dependent_entity_type IN ('STAGE', 'TASK', 'STEP')),
    depends_on_entity_id UUID NOT NULL,
    depends_on_entity_type VARCHAR(20) NOT NULL CHECK (depends_on_entity_type IN ('STAGE', 'TASK', 'STEP')),
    dependency_type VARCHAR(20) NOT NULL DEFAULT 'FINISH_TO_START' CHECK (dependency_type IN ('FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH', 'START_TO_FINISH')),
    lag_days INTEGER DEFAULT 0,
    lead_days INTEGER DEFAULT 0,
    is_hard_dependency BOOLEAN DEFAULT true,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure no self-dependencies
    CONSTRAINT chk_no_self_dependency CHECK (dependent_entity_id != depends_on_entity_id OR dependent_entity_type != depends_on_entity_type),
    
    -- Ensure same project for both entities
    CONSTRAINT chk_same_project CHECK (
        (dependent_entity_type = 'STAGE' AND dependent_entity_id IN (SELECT id FROM project_stages WHERE project_id = project_dependencies.project_id)) OR
        (dependent_entity_type = 'TASK' AND dependent_entity_id IN (SELECT id FROM project_tasks WHERE project_stage_id IN (SELECT id FROM project_stages WHERE project_id = project_dependencies.project_id))) OR
        (dependent_entity_type = 'STEP' AND dependent_entity_id IN (SELECT id FROM project_steps WHERE project_task_id IN (SELECT id FROM project_tasks WHERE project_stage_id IN (SELECT id FROM project_stages WHERE project_id = project_dependencies.project_id))))
    )
);

-- Create indexes for performance
CREATE INDEX idx_project_dependencies_project_id ON project_dependencies(project_id);
CREATE INDEX idx_project_dependencies_dependent_entity ON project_dependencies(dependent_entity_id, dependent_entity_type);
CREATE INDEX idx_project_dependencies_depends_on_entity ON project_dependencies(depends_on_entity_id, depends_on_entity_type);
CREATE INDEX idx_project_dependencies_type ON project_dependencies(dependency_type);

-- Add comments
COMMENT ON TABLE project_dependencies IS 'Stores dependencies between project entities (stages, tasks, steps) for scheduling calculations';
COMMENT ON COLUMN project_dependencies.dependent_entity_id IS 'ID of the entity that has the dependency';
COMMENT ON COLUMN project_dependencies.dependent_entity_type IS 'Type of the dependent entity (STAGE, TASK, STEP)';
COMMENT ON COLUMN project_dependencies.depends_on_entity_id IS 'ID of the entity that the dependent entity depends on';
COMMENT ON COLUMN project_dependencies.depends_on_entity_type IS 'Type of the entity that is depended upon';
COMMENT ON COLUMN project_dependencies.dependency_type IS 'Type of dependency relationship';
COMMENT ON COLUMN project_dependencies.lag_days IS 'Days to wait after predecessor completes before starting';
COMMENT ON COLUMN project_dependencies.lead_days IS 'Days to start before predecessor completes (negative lag)';
COMMENT ON COLUMN project_dependencies.is_hard_dependency IS 'Whether this is a hard dependency that cannot be overridden';
