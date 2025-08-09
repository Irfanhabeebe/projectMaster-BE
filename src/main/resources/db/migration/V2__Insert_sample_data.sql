-- Insert sample company
INSERT INTO companies (id, name, address, phone, email, website, tax_number, active, created_at, updated_at)
VALUES (
    uuid_generate_v4(),
    'ABC Construction Company',
    '123 Builder Street, Construction City, CC 12345',
    '+1-555-0123',
    'info@abcconstruction.com',
    'https://www.abcconstruction.com',
    'TAX123456789',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Get the company ID for reference
DO $$
DECLARE
    company_uuid UUID;
BEGIN
    SELECT id INTO company_uuid FROM companies WHERE name = 'ABC Construction Company';
    
    -- Insert sample admin user
    INSERT INTO users (id, company_id, email, password_hash, first_name, last_name, phone, role, active, email_verified, created_at, updated_at)
    VALUES (
        uuid_generate_v4(),
        company_uuid,
        'admin@abcconstruction.com',
        'hashed_password_here', -- This will be replaced by actual password encoding
        'John',
        'Admin',
        '+1-555-0124',
        'ADMIN',
        true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    
    -- Insert sample project manager
    INSERT INTO users (id, company_id, email, password_hash, first_name, last_name, phone, role, active, email_verified, created_at, updated_at)
    VALUES (
        uuid_generate_v4(),
        company_uuid,
        'pm@abcconstruction.com',
        'hashed_password_here',
        'Jane',
        'Manager',
        '+1-555-0125',
        'PROJECT_MANAGER',
        true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    
    -- Insert sample tradie
    INSERT INTO users (id, company_id, email, password_hash, first_name, last_name, phone, role, active, email_verified, created_at, updated_at)
    VALUES (
        uuid_generate_v4(),
        company_uuid,
        'tradie@abcconstruction.com',
        'hashed_password_here',
        'Mike',
        'Builder',
        '+1-555-0126',
        'TRADIE',
        true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    
    -- Insert sample customer
    INSERT INTO customers (id, company_id, first_name, last_name, email, phone, address, notes, active, created_at, updated_at)
    VALUES (
        uuid_generate_v4(),
        company_uuid,
        'Robert',
        'Smith',
        'robert.smith@email.com',
        '+1-555-0127',
        '456 Oak Avenue, Residential Area, RA 67890',
        'First-time customer, building family home',
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
    
    -- Insert sample workflow template
    INSERT INTO workflow_templates (id, company_id, name, description, category, active, is_default, created_at, updated_at)
    VALUES (
        uuid_generate_v4(),
        company_uuid,
        'Standard Residential Build',
        'Standard workflow for residential construction projects',
        'RESIDENTIAL',
        true,
        true,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
END $$;

-- Insert workflow stages for the template
DO $$
DECLARE
    template_uuid UUID;
    stage1_uuid UUID;
    stage2_uuid UUID;
    stage3_uuid UUID;
BEGIN
    SELECT id INTO template_uuid FROM workflow_templates WHERE name = 'Standard Residential Build';
    
    -- Stage 1: Site Preparation
    stage1_uuid := uuid_generate_v4();
    INSERT INTO workflow_stages (id, workflow_template_id, name, description, order_index, parallel_execution, required_approvals, estimated_duration_days, created_at, updated_at)
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
    INSERT INTO workflow_stages (id, workflow_template_id, name, description, order_index, parallel_execution, required_approvals, estimated_duration_days, created_at, updated_at)
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
    INSERT INTO workflow_stages (id, workflow_template_id, name, description, order_index, parallel_execution, required_approvals, estimated_duration_days, created_at, updated_at)
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
    
    -- Insert workflow steps for Site Preparation
    INSERT INTO workflow_steps (id, workflow_stage_id, name, description, order_index, estimated_hours, required_skills, requirements, created_at, updated_at)
    VALUES 
    (uuid_generate_v4(), stage1_uuid, 'Site Survey', 'Conduct detailed site survey', 1, 8, '["surveying"]', '{"equipment": ["survey_tools"]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage1_uuid, 'Permit Application', 'Apply for building permits', 2, 4, '["administration"]', '{"documents": ["plans", "applications"]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage1_uuid, 'Site Clearing', 'Clear and prepare the site', 3, 16, '["excavation"]', '{"equipment": ["excavator", "bulldozer"]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
    
    -- Insert workflow steps for Foundation
    INSERT INTO workflow_steps (id, workflow_stage_id, name, description, order_index, estimated_hours, required_skills, requirements, created_at, updated_at)
    VALUES 
    (uuid_generate_v4(), stage2_uuid, 'Excavation', 'Dig foundation trenches', 1, 24, '["excavation"]', '{"equipment": ["excavator"]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage2_uuid, 'Footings', 'Pour concrete footings', 2, 16, '["concrete"]', '{"materials": ["concrete", "rebar"]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage2_uuid, 'Foundation Walls', 'Build foundation walls', 3, 32, '["masonry", "concrete"]', '{"materials": ["blocks", "mortar"]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
    
    -- Insert workflow steps for Framing
    INSERT INTO workflow_steps (id, workflow_stage_id, name, description, order_index, estimated_hours, required_skills, requirements, created_at, updated_at)
    VALUES 
    (uuid_generate_v4(), stage3_uuid, 'Floor Framing', 'Install floor joists and subfloor', 1, 40, '["carpentry"]', '{"materials": ["lumber", "nails", "subfloor"]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage3_uuid, 'Wall Framing', 'Frame exterior and interior walls', 2, 60, '["carpentry"]', '{"materials": ["lumber", "nails", "plates"]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), stage3_uuid, 'Roof Framing', 'Install roof trusses and sheathing', 3, 48, '["carpentry"]', '{"materials": ["trusses", "sheathing", "nails"]}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
    
END $$;