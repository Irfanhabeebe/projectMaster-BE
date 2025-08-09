-- V5: Add Standard Workflow Tables
-- Create tables for standard workflow templates that can be copied to companies

-- Standard workflow templates table
CREATE TABLE standard_workflow_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    active BOOLEAN DEFAULT true,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Standard workflow stages table
CREATE TABLE standard_workflow_stages (
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

-- Standard workflow steps table
CREATE TABLE standard_workflow_steps (
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

-- Add indexes for better performance
CREATE INDEX idx_standard_workflow_templates_active ON standard_workflow_templates(active);
CREATE INDEX idx_standard_workflow_templates_default ON standard_workflow_templates(is_default);
CREATE INDEX idx_standard_workflow_stages_template_id ON standard_workflow_stages(standard_workflow_template_id);
CREATE INDEX idx_standard_workflow_stages_order ON standard_workflow_stages(standard_workflow_template_id, order_index);
CREATE INDEX idx_standard_workflow_steps_stage_id ON standard_workflow_steps(standard_workflow_stage_id);
CREATE INDEX idx_standard_workflow_steps_order ON standard_workflow_steps(standard_workflow_stage_id, order_index);

-- Insert sample standard workflow template
INSERT INTO standard_workflow_templates (id, name, description, category, active, is_default, created_at, updated_at)
VALUES (
    uuid_generate_v4(),
    'Standard Residential Build',
    'Standard workflow template for residential construction projects',
    'RESIDENTIAL',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert standard workflow stages and steps
DO $$
DECLARE
    template_uuid UUID;
    stage1_uuid UUID;
    stage2_uuid UUID;
    stage3_uuid UUID;
BEGIN
    SELECT id INTO template_uuid FROM standard_workflow_templates WHERE name = 'Standard Residential Build';
    
    -- Stage 1: Site Preparation
    stage1_uuid := uuid_generate_v4();
    INSERT INTO standard_workflow_stages (id, standard_workflow_template_id, name, description, order_index, parallel_execution, required_approvals, estimated_duration_days, created_at, updated_at)
    VALUES (
        stage1_uuid,
        template_uuid,
        'Site Preparation',
        'Site survey, permits, and initial preparation',
        1,
        false,
        1,
        5,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    
    -- Stage 2: Foundation
    stage2_uuid := uuid_generate_v4();
    INSERT INTO standard_workflow_stages (id, standard_workflow_template_id, name, description, order_index, parallel_execution, required_approvals, estimated_duration_days, created_at, updated_at)
    VALUES (
        stage2_uuid,
        template_uuid,
        'Foundation',
        'Excavation, footings, and foundation work',
        2,
        false,
        1,
        10,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    
    -- Stage 3: Framing
    stage3_uuid := uuid_generate_v4();
    INSERT INTO standard_workflow_stages (id, standard_workflow_template_id, name, description, order_index, parallel_execution, required_approvals, estimated_duration_days, created_at, updated_at)
    VALUES (
        stage3_uuid,
        template_uuid,
        'Framing',
        'Floor, wall, and roof framing',
        3,
        false,
        1,
        15,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    
    -- Insert standard workflow steps for Site Preparation
    INSERT INTO standard_workflow_steps (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, required_skills, requirements, created_at, updated_at)
    VALUES 
    (uuid_generate_v4(), stage1_uuid, 'Site Survey', 'Conduct detailed site survey', 1, 8, '["surveying"]'::jsonb, '{"equipment": ["survey_tools"]}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage1_uuid, 'Permit Application', 'Apply for building permits', 2, 4, '["administration"]'::jsonb, '{"documents": ["plans", "applications"]}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage1_uuid, 'Site Clearing', 'Clear and prepare the site', 3, 16, '["excavation"]'::jsonb, '{"equipment": ["excavator", "bulldozer"]}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
    
    -- Insert standard workflow steps for Foundation
    INSERT INTO standard_workflow_steps (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, required_skills, requirements, created_at, updated_at)
    VALUES 
    (uuid_generate_v4(), stage2_uuid, 'Excavation', 'Dig foundation trenches', 1, 24, '["excavation"]'::jsonb, '{"equipment": ["excavator"]}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage2_uuid, 'Footings', 'Pour concrete footings', 2, 16, '["concrete"]'::jsonb, '{"materials": ["concrete", "rebar"]}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage2_uuid, 'Foundation Walls', 'Build foundation walls', 3, 32, '["masonry", "concrete"]'::jsonb, '{"materials": ["blocks", "mortar"]}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
    
    -- Insert standard workflow steps for Framing
    INSERT INTO standard_workflow_steps (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, required_skills, requirements, created_at, updated_at)
    VALUES 
    (uuid_generate_v4(), stage3_uuid, 'Floor Framing', 'Install floor joists and subfloor', 1, 40, '["carpentry"]'::jsonb, '{"materials": ["lumber", "nails", "subfloor"]}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage3_uuid, 'Wall Framing', 'Frame exterior and interior walls', 2, 60, '["carpentry"]'::jsonb, '{"materials": ["lumber", "nails", "plates"]}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage3_uuid, 'Roof Framing', 'Install roof trusses and sheathing', 3, 48, '["carpentry"]'::jsonb, '{"materials": ["trusses", "sheathing", "nails"]}'::jsonb, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
    
END $$;