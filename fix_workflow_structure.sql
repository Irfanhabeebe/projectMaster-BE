-- Fix workflow structure to support 3-layer (Stages -> Tasks -> Steps)
-- This script should be run manually to update the database structure

-- 1. Fix workflow_templates table version column issue
ALTER TABLE workflow_templates ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
UPDATE workflow_templates SET version = 1 WHERE version IS NULL;

-- 2. Add missing columns to project_stages table
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS order_index INTEGER DEFAULT 0;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS required_approvals INTEGER DEFAULT 0;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS estimated_duration_days INTEGER;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS workflow_template_version INTEGER;
ALTER TABLE project_stages ADD COLUMN IF NOT EXISTS workflow_stage_version INTEGER;

-- 3. Create project_tasks table
CREATE TABLE IF NOT EXISTS project_tasks (
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
    order_index INTEGER DEFAULT 0,
    description TEXT,
    estimated_hours INTEGER,
    required_skills JSONB,
    requirements JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create workflow_tasks table if it doesn't exist
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

-- 5. Create standard_workflow_tasks table if it doesn't exist
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

-- 6. Add missing columns to project_steps table
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS project_task_id UUID REFERENCES project_tasks(id) ON DELETE CASCADE;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS order_index INTEGER DEFAULT 0;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS estimated_hours INTEGER;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS required_skills JSONB;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS requirements JSONB;
ALTER TABLE project_steps ADD COLUMN IF NOT EXISTS quality_check_passed BOOLEAN;

-- 7. Add missing columns to standard_workflow_steps table
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS standard_workflow_task_id UUID REFERENCES standard_workflow_tasks(id) ON DELETE CASCADE;
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS order_index INTEGER DEFAULT 0;
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS estimated_hours INTEGER;
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS required_skills JSONB;
ALTER TABLE standard_workflow_steps ADD COLUMN IF NOT EXISTS requirements JSONB;

-- 8. Add missing columns to workflow_steps table
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS workflow_task_id UUID REFERENCES workflow_tasks(id) ON DELETE CASCADE;
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS order_index INTEGER DEFAULT 0;
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS estimated_hours INTEGER;
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS required_skills JSONB;
ALTER TABLE workflow_steps ADD COLUMN IF NOT EXISTS requirements JSONB;

-- 9. Add missing columns to standard_workflow_stages table
ALTER TABLE standard_workflow_stages ADD COLUMN IF NOT EXISTS order_index INTEGER DEFAULT 0;
ALTER TABLE standard_workflow_stages ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE standard_workflow_stages ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE standard_workflow_stages ADD COLUMN IF NOT EXISTS required_approvals INTEGER DEFAULT 0;
ALTER TABLE standard_workflow_stages ADD COLUMN IF NOT EXISTS estimated_duration_days INTEGER;

-- 10. Add missing columns to workflow_stages table
ALTER TABLE workflow_stages ADD COLUMN IF NOT EXISTS order_index INTEGER DEFAULT 0;
ALTER TABLE workflow_stages ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE workflow_stages ADD COLUMN IF NOT EXISTS parallel_execution BOOLEAN DEFAULT false;
ALTER TABLE workflow_stages ADD COLUMN IF NOT EXISTS required_approvals INTEGER DEFAULT 0;
ALTER TABLE workflow_stages ADD COLUMN IF NOT EXISTS estimated_duration_days INTEGER; 