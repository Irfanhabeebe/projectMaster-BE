-- Australian Double Story Construction Workflows
-- Standard Double Story Construction Template

-- =====================================================
-- 1. WORKFLOW TEMPLATES
-- =====================================================

INSERT INTO standard_workflow_templates (id, name, description, category, active, is_default, created_at, updated_at) VALUES
('b2c3d4e5-f6a7-8901-bcde-f23456789012', 'Standard Double Story Construction', 'Complete workflow for double story residential construction in Australia', 'Residential Construction', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================
-- 2. DOUBLE STORY CONSTRUCTION STAGES
-- =====================================================

INSERT INTO standard_workflow_stages (id, standard_workflow_template_id, name, description, order_index, parallel_execution, required_approvals, estimated_duration_days, created_at, updated_at) VALUES
('f7a8b9c0-d1e2-3456-7890-123456789012', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'Site Preparation & Foundation', 'Site clearing, excavation, and foundation work', 1, false, 0, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a8b9c0d1-e2f3-4567-8901-234567890123', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'Ground Floor Frame & Structure', 'Ground floor timber frame construction', 2, false, 0, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b9c0d1e2-f3a4-5678-9012-345678901234', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'First Floor Frame & Structure', 'First floor and upper level construction', 3, false, 0, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c0d1e2f3-a4b5-6789-0123-456789012345', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'Roof Structure & Covering', 'Roof trusses and roof covering installation', 4, false, 0, 21, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d1e2f3a4-b5c6-7890-1234-567890123456', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'External Works', 'Brickwork, cladding, and external finishes', 5, false, 0, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e2f3a4b5-c6d7-8901-2345-678901234567', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'Internal Works', 'Plumbing, electrical, and internal finishes', 6, false, 0, 42, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f3a4b5c6-d7e8-9012-3456-789012345678', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'Staircase & Internal Access', 'Staircase construction and internal access', 7, false, 0, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a4b5c6d7-e8f9-0123-4567-890123456789', 'b2c3d4e5-f6a7-8901-bcde-f23456789012', 'Final Finishes & Handover', 'Final touches, cleaning, and handover', 8, false, 0, 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================
-- 6. DOUBLE STORY CONSTRUCTION TASKS
-- =====================================================

-- Stage 1: Site Preparation & Foundation (Enhanced for double story)
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('b5c6d7e8-f9a0-1234-5678-901234567890', 'f7a8b9c0-d1e2-3456-7890-123456789012', 'Site Survey & Marking', 'Survey site boundaries and mark building footprint', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c6d7e8f9-a0b1-2345-6789-012345678901', 'f7a8b9c0-d1e2-3456-7890-123456789012', 'Site Clearing', 'Remove vegetation and clear site', 2, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d7e8f9a0-b1c2-3456-7890-123456789012', 'f7a8b9c0-d1e2-3456-7890-123456789012', 'Deep Excavation', 'Deep excavation for double story foundation', 3, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e8f9a0b1-c2d3-4567-8901-234567890123', 'f7a8b9c0-d1e2-3456-7890-123456789012', 'Reinforced Foundation', 'Pour reinforced concrete foundation', 4, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f9a0b1c2-d3e4-5678-9012-345678901234', 'f7a8b9c0-d1e2-3456-7890-123456789012', 'Drainage Installation', 'Install stormwater and sewer drainage', 5, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 2: Ground Floor Frame & Structure
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('a0b1c2d3-e4f5-6789-0123-456789012345', 'a8b9c0d1-e2f3-4567-8901-234567890123', 'Ground Floor Frame', 'Install ground floor timber frame', 1, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b1c2d3e4-f5a6-7890-1234-567890123456', 'a8b9c0d1-e2f3-4567-8901-234567890123', 'Ground Floor Walls', 'Build ground floor wall frames', 2, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c2d3e4f5-a6b7-8901-2345-678901234567', 'a8b9c0d1-e2f3-4567-8901-234567890123', 'Ground Floor Sheeting', 'Install ground floor wall sheeting', 3, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d3e4f5a6-b7c8-9012-3456-789012345678', 'a8b9c0d1-e2f3-4567-8901-234567890123', 'Ground Floor Ceiling', 'Install ground floor ceiling structure', 4, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 3: First Floor Frame & Structure
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('e4f5a6b7-c8d9-0123-4567-890123456789', 'b9c0d1e2-f3a4-5678-9012-345678901234', 'First Floor Frame', 'Install first floor timber frame', 1, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f5a6b7c8-d9e0-1234-5678-901234567890', 'b9c0d1e2-f3a4-5678-9012-345678901234', 'First Floor Walls', 'Build first floor wall frames', 2, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a6b7c8d9-e0f1-2345-6789-012345678901', 'b9c0d1e2-f3a4-5678-9012-345678901234', 'First Floor Sheeting', 'Install first floor wall sheeting', 3, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b7c8d9e0-f1a2-3456-7890-123456789012', 'b9c0d1e2-f3a4-5678-9012-345678901234', 'First Floor Ceiling', 'Install first floor ceiling structure', 4, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 4: Roof Structure & Covering
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('c8d9e0f1-a2b3-4567-8901-234567890123', 'c0d1e2f3-a4b5-6789-0123-456789012345', 'Roof Truss Installation', 'Install roof trusses and bracing', 1, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d9e0f1a2-b3c4-5678-9012-345678901234', 'c0d1e2f3-a4b5-6789-0123-456789012345', 'Roof Covering', 'Install roof tiles or metal sheeting', 2, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e0f1a2b3-c4d5-6789-0123-456789012345', 'c0d1e2f3-a4b5-6789-0123-456789012345', 'Fascia & Guttering', 'Install fascia boards and guttering', 3, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 5: External Works (Enhanced for double story)
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('f1a2b3c4-d5e6-7890-1234-567890123456', 'd1e2f3a4-b5c6-7890-1234-567890123456', 'Ground Floor Brickwork', 'Lay ground floor external brick walls', 1, 48, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a2b3c4d5-e6f7-8901-2345-678901234567', 'd1e2f3a4-b5c6-7890-1234-567890123456', 'First Floor Brickwork', 'Lay first floor external brick walls', 2, 48, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b3c4d5e6-f7a8-9012-3456-789012345678', 'd1e2f3a4-b5c6-7890-1234-567890123456', 'External Cladding', 'Install weatherboard or other cladding', 3, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c4d5e6f7-a8b9-0123-4567-890123456789', 'd1e2f3a4-b5c6-7890-1234-567890123456', 'Windows & Doors', 'Install external windows and doors', 4, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d5e6f7a8-b9c0-1234-5678-901234567890', 'd1e2f3a4-b5c6-7890-1234-567890123456', 'External Painting', 'Paint external walls and trim', 5, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e6f7a8b9-c0d1-2345-6789-012345678901', 'd1e2f3a4-b5c6-7890-1234-567890123456', 'Driveway & Paths', 'Pour concrete driveway and paths', 6, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 6: Internal Works (Enhanced for double story)
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('f7a8b9c0-d1e2-3456-7890-123456789012', 'e2f3a4b5-c6d7-8901-2345-678901234567', 'Ground Floor Plumbing', 'Install ground floor plumbing systems', 1, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a8b9c0d1-e2f3-4567-8901-234567890123', 'e2f3a4b5-c6d7-8901-2345-678901234567', 'First Floor Plumbing', 'Install first floor plumbing systems', 2, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b9c0d1e2-f3a4-5678-9012-345678901234', 'e2f3a4b5-c6d7-8901-2345-678901234567', 'Ground Floor Electrical', 'Install ground floor electrical systems', 3, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c0d1e2f3-a4b5-6789-0123-456789012345', 'e2f3a4b5-c6d7-8901-2345-678901234567', 'First Floor Electrical', 'Install first floor electrical systems', 4, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d1e2f3a4-b5c6-7890-1234-567890123456', 'e2f3a4b5-c6d7-8901-2345-678901234567', 'HVAC Installation', 'Install heating and cooling systems', 5, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e2f3a4b5-c6d7-8901-2345-678901234567', 'e2f3a4b5-c6d7-8901-2345-678901234567', 'Internal Lining', 'Install plasterboard walls and ceilings', 6, 56, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f3a4b5c6-d7e8-9012-3456-789012345678', 'e2f3a4b5-c6d7-8901-2345-678901234567', 'Internal Finishes', 'Install flooring, painting, and fixtures', 7, 64, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 7: Staircase & Internal Access
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('a4b5c6d7-e8f9-0123-4567-890123456789', 'f3a4b5c6-d7e8-9012-3456-789012345678', 'Staircase Frame', 'Build staircase frame and structure', 1, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b5c6d7e8-f9a0-1234-5678-901234567890', 'f3a4b5c6-d7e8-9012-3456-789012345678', 'Staircase Treads', 'Install staircase treads and risers', 2, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c6d7e8f9-a0b1-2345-6789-012345678901', 'f3a4b5c6-d7e8-9012-3456-789012345678', 'Staircase Finishes', 'Finish staircase with handrails and trim', 3, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 8: Final Finishes & Handover
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('d7e8f9a0-b1c2-3456-7890-123456789012', 'a4b5c6d7-e8f9-0123-4567-890123456789', 'Kitchen Installation', 'Install kitchen cabinets and appliances', 1, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e8f9a0b1-c2d3-4567-8901-234567890123', 'a4b5c6d7-e8f9-0123-4567-890123456789', 'Bathroom Installation', 'Install bathroom fixtures and tiling', 2, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f9a0b1c2-d3e4-5678-9012-345678901234', 'a4b5c6d7-e8f9-0123-4567-890123456789', 'Final Electrical', 'Install light fixtures and final electrical', 3, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a0b1c2d3-e4f5-6789-0123-456789012345', 'a4b5c6d7-e8f9-0123-4567-890123456789', 'Final Plumbing', 'Install taps, toilets, and final plumbing', 4, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b1c2d3e4-f5a6-7890-1234-567890123456', 'a4b5c6d7-e8f9-0123-4567-890123456789', 'Final Inspection & Handover', 'Final inspection and handover to client', 5, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 