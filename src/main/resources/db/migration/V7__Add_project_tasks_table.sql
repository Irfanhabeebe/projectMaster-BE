-- Add project_tasks table to support 3-layer workflow structure
CREATE TABLE project_tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_stage_id UUID NOT NULL REFERENCES project_stages(id) ON DELETE CASCADE,
    workflow_task_id UUID NOT NULL REFERENCES workflow_tasks(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    status step_status DEFAULT 'NOT_STARTED',
    start_date DATE,
    end_date DATE,
    actual_start_date DATE,
    actual_end_date DATE,
    notes TEXT,
    quality_check_passed BOOLEAN,
    description TEXT,
    order_index INTEGER NOT NULL DEFAULT 0,
    estimated_hours INTEGER,
    required_skills JSONB,
    requirements JSONB,
    workflow_task_version INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add workflow_tasks table if it doesn't exist
CREATE TABLE IF NOT EXISTS workflow_tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_stage_id UUID NOT NULL REFERENCES workflow_stages(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    estimated_hours INTEGER,
    required_skills JSONB,
    requirements JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add standard_workflow_tasks table if it doesn't exist
CREATE TABLE IF NOT EXISTS standard_workflow_tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    standard_workflow_stage_id UUID NOT NULL REFERENCES standard_workflow_stages(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    estimated_hours INTEGER,
    required_skills JSONB,
    requirements JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add standard_workflow_stages table if it doesn't exist
CREATE TABLE IF NOT EXISTS standard_workflow_stages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    standard_workflow_template_id UUID NOT NULL REFERENCES standard_workflow_templates(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    parallel_execution BOOLEAN DEFAULT false,
    required_approvals INTEGER DEFAULT 0,
    estimated_duration_days INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add standard_workflow_templates table if it doesn't exist
CREATE TABLE IF NOT EXISTS standard_workflow_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    active BOOLEAN DEFAULT true,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add standard_workflow_steps table if it doesn't exist
CREATE TABLE IF NOT EXISTS standard_workflow_steps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    standard_workflow_task_id UUID NOT NULL REFERENCES standard_workflow_tasks(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    estimated_hours INTEGER,
    required_skills JSONB,
    requirements JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Update existing standard_workflow_steps to add task_id column
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS standard_workflow_task_id UUID REFERENCES standard_workflow_tasks(id) ON DELETE CASCADE;

-- Fix workflow_templates table version column issue
ALTER TABLE workflow_templates ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE workflow_templates SET version = 1 WHERE version IS NULL;

-- Add workflow_task_id column to existing workflow_steps table if it exists
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS workflow_task_id UUID REFERENCES workflow_tasks(id) ON DELETE CASCADE;

-- Update existing workflow_steps to reference workflow_tasks instead of workflow_stages
-- First, create a temporary column
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS temp_workflow_task_id UUID;

-- Update the temporary column based on existing workflow_stage_id
-- This is a placeholder - in a real migration, you'd need to create the workflow_tasks first
-- For now, we'll just add the column and let the application handle the data migration

-- Update project_steps table to reference project_tasks instead of project_stages
ALTER TABLE project_steps ADD COLUMN project_task_id UUID REFERENCES project_tasks(id) ON DELETE CASCADE;

-- Add missing columns to project_stages table
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS order_index INTEGER DEFAULT 0;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS required_approvals INTEGER DEFAULT 0;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS estimated_duration_days INTEGER;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS workflow_template_version INTEGER;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS workflow_stage_version INTEGER;

-- Add missing columns to project_steps table
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS order_index INTEGER DEFAULT 0;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS estimated_hours INTEGER;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS required_skills JSONB;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS requirements JSONB;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS workflow_step_version INTEGER;

-- Create indexes for the new tables
CREATE INDEX idx_project_tasks_stage_id ON project_tasks(project_stage_id);
CREATE INDEX idx_project_tasks_status ON project_tasks(status);
CREATE INDEX idx_project_steps_task_id ON project_steps(project_task_id);

CREATE INDEX idx_workflow_tasks_stage_id ON workflow_tasks(workflow_stage_id);
CREATE INDEX idx_workflow_tasks_order ON workflow_tasks(workflow_stage_id, order_index);

CREATE INDEX idx_standard_workflow_templates_company_id ON standard_workflow_templates(company_id);
CREATE INDEX idx_standard_workflow_templates_active ON standard_workflow_templates(active);

CREATE INDEX idx_standard_workflow_stages_template_id ON standard_workflow_stages(standard_workflow_template_id);
CREATE INDEX idx_standard_workflow_stages_order ON standard_workflow_stages(standard_workflow_template_id, order_index);

CREATE INDEX idx_standard_workflow_tasks_stage_id ON standard_workflow_tasks(standard_workflow_stage_id);
CREATE INDEX idx_standard_workflow_tasks_order ON standard_workflow_tasks(standard_workflow_stage_id, order_index);

CREATE INDEX idx_standard_workflow_steps_task_id ON standard_workflow_steps(standard_workflow_task_id);
CREATE INDEX idx_standard_workflow_steps_order ON standard_workflow_steps(standard_workflow_task_id, order_index); 