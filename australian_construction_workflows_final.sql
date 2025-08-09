-- Australian Construction Workflows (Final with Unique UUIDs)
-- Standard Single Story Construction Template

-- =====================================================
-- 1. WORKFLOW TEMPLATES
-- =====================================================

INSERT INTO standard_workflow_templates (id, name, description, category, active, is_default, created_at, updated_at) VALUES
('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Standard Single Story Construction', 'Complete workflow for single story residential construction in Australia', 'Residential Construction', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================
-- 2. SINGLE STORY CONSTRUCTION STAGES
-- =====================================================

INSERT INTO standard_workflow_stages (id, standard_workflow_template_id, name, description, order_index, parallel_execution, required_approvals, estimated_duration_days, created_at, updated_at) VALUES
('c3d4e5f6-a7b8-9012-cdef-345678901234', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Site Preparation & Foundation', 'Site clearing, excavation, and foundation work', 1, false, 0, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d4e5f6a7-b8c9-0123-def0-456789012345', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Frame & Roof Structure', 'Timber frame construction and roof installation', 2, false, 0, 21, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5f6a7b8-c9d0-1234-ef01-567890123456', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'External Works', 'Brickwork, cladding, and external finishes', 3, false, 0, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f6a7b8c9-d0e1-2345-f012-678901234567', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Internal Works', 'Plumbing, electrical, and internal finishes', 4, false, 0, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a7b8c9d0-e1f2-3456-0123-789012345678', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'Final Finishes & Handover', 'Final touches, cleaning, and handover', 5, false, 0, 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================
-- 3. SINGLE STORY CONSTRUCTION TASKS
-- =====================================================

-- Stage 1: Site Preparation & Foundation
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('b8c9d0e1-f2a3-4567-8901-234567890123', 'c3d4e5f6-a7b8-9012-cdef-345678901234', 'Site Survey & Marking', 'Survey site boundaries and mark building footprint', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c9d0e1f2-a3b4-5678-9012-345678901234', 'c3d4e5f6-a7b8-9012-cdef-345678901234', 'Site Clearing', 'Remove vegetation and clear site', 2, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d0e1f2a3-b4c5-6789-0123-456789012345', 'c3d4e5f6-a7b8-9012-cdef-345678901234', 'Excavation', 'Excavate foundation trenches', 3, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e1f2a3b4-c5d6-7890-1234-567890123456', 'c3d4e5f6-a7b8-9012-cdef-345678901234', 'Foundation', 'Pour concrete foundation', 4, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f2a3b4c5-d6e7-8901-2345-678901234567', 'c3d4e5f6-a7b8-9012-cdef-345678901234', 'Drainage', 'Install stormwater drainage', 5, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 2: Frame & Roof Structure
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('a3b4c5d6-e7f8-9012-3456-789012345678', 'd4e5f6a7-b8c9-0123-def0-456789012345', 'Floor Frame', 'Install floor joists and subfloor', 1, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b4c5d6e7-f8a9-0123-4567-890123456789', 'd4e5f6a7-b8c9-0123-def0-456789012345', 'Wall Frame', 'Build wall frames', 2, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c5d6e7f8-a9b0-1234-5678-901234567890', 'd4e5f6a7-b8c9-0123-def0-456789012345', 'Roof Frame', 'Install roof trusses and bracing', 3, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d6e7f8a9-b0c1-2345-6789-012345678901', 'd4e5f6a7-b8c9-0123-def0-456789012345', 'Roof Covering', 'Install roof tiles or metal sheeting', 4, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e7f8a9b0-c1d2-3456-7890-123456789012', 'd4e5f6a7-b8c9-0123-def0-456789012345', 'Fascia & Guttering', 'Install fascia boards and guttering', 5, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 3: External Works
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('f8a9b0c1-d2e3-4567-8901-234567890123', 'e5f6a7b8-c9d0-1234-ef01-567890123456', 'Brickwork', 'Lay external brick walls', 1, 48, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a9b0c1d2-e3f4-5678-9012-345678901234', 'e5f6a7b8-c9d0-1234-ef01-567890123456', 'External Cladding', 'Install weatherboard or other cladding', 2, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b0c1d2e3-f4a5-6789-0123-456789012345', 'e5f6a7b8-c9d0-1234-ef01-567890123456', 'Windows & Doors', 'Install external windows and doors', 3, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c1d2e3f4-a5b6-7890-1234-567890123456', 'e5f6a7b8-c9d0-1234-ef01-567890123456', 'External Painting', 'Paint external walls and trim', 4, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d2e3f4a5-b6c7-8901-2345-678901234567', 'e5f6a7b8-c9d0-1234-ef01-567890123456', 'Driveway & Paths', 'Pour concrete driveway and paths', 5, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 4: Internal Works
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('e3f4a5b6-c7d8-9012-3456-789012345678', 'f6a7b8c9-d0e1-2345-f012-678901234567', 'Plumbing Rough-in', 'Install plumbing pipes and fixtures', 1, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f4a5b6c7-d8e9-0123-4567-890123456789', 'f6a7b8c9-d0e1-2345-f012-678901234567', 'Electrical Rough-in', 'Install electrical wiring and conduits', 2, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a5b6c7d8-e9f0-1234-5678-901234567890', 'f6a7b8c9-d0e1-2345-f012-678901234567', 'HVAC Installation', 'Install heating and cooling systems', 3, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b6c7d8e9-f0a1-2345-6789-012345678901', 'f6a7b8c9-d0e1-2345-f012-678901234567', 'Internal Lining', 'Install plasterboard walls and ceilings', 4, 56, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c7d8e9f0-a1b2-3456-7890-123456789012', 'f6a7b8c9-d0e1-2345-f012-678901234567', 'Internal Finishes', 'Install flooring, painting, and fixtures', 5, 64, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Stage 5: Final Finishes & Handover
INSERT INTO standard_workflow_tasks (id, standard_workflow_stage_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('d8e9f0a1-b2c3-4567-8901-234567890123', 'a7b8c9d0-e1f2-3456-0123-789012345678', 'Kitchen Installation', 'Install kitchen cabinets and appliances', 1, 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e9f0a1b2-c3d4-5678-9012-345678901234', 'a7b8c9d0-e1f2-3456-0123-789012345678', 'Bathroom Installation', 'Install bathroom fixtures and tiling', 2, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f0a1b2c3-d4e5-6789-0123-456789012345', 'a7b8c9d0-e1f2-3456-0123-789012345678', 'Final Electrical', 'Install light fixtures and final electrical', 3, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1b2c3d4-e5f6-7890-1234-567890123456', 'a7b8c9d0-e1f2-3456-0123-789012345678', 'Final Plumbing', 'Install taps, toilets, and final plumbing', 4, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b2c3d4e5-f6a7-8901-2345-678901234567', 'a7b8c9d0-e1f2-3456-0123-789012345678', 'Final Inspection & Handover', 'Final inspection and handover to client', 5, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================
-- 4. SINGLE STORY CONSTRUCTION STEPS (Sample)
-- =====================================================

-- Task: Site Survey & Marking
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('c3d4e5f6-a7b8-9012-cdef-345678901234', 'b8c9d0e1-f2a3-4567-8901-234567890123', 'Site Boundary Survey', 'Survey and mark property boundaries', 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d4e5f6a7-b8c9-0123-def0-456789012345', 'b8c9d0e1-f2a3-4567-8901-234567890123', 'Building Footprint Marking', 'Mark building outline and corners', 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Site Clearing
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('e5f6a7b8-c9d0-1234-ef01-567890123456', 'c9d0e1f2-a3b4-5678-9012-345678901234', 'Vegetation Removal', 'Remove trees and vegetation', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f6a7b8c9-d0e1-2345-f012-678901234567', 'c9d0e1f2-a3b4-5678-9012-345678901234', 'Site Grading', 'Grade site for proper drainage', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Excavation
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('a7b8c9d0-e1f2-3456-0123-789012345678', 'd0e1f2a3-b4c5-6789-0123-456789012345', 'Foundation Trenches', 'Excavate foundation trenches', 1, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b8c9d0e1-f2a3-4567-8901-234577890123', 'd0e1f2a3-b4c5-6789-0123-456789012345', 'Soil Compaction', 'Compact soil in foundation area', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Foundation
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('c9d0e1f2-a3b4-5678-9012-345678901234', 'e1f2a3b4-c5d6-7890-1234-567890123456', 'Reinforcement Installation', 'Install steel reinforcement', 1, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d0e1f2a3-b4c5-6789-0123-456789012345', 'e1f2a3b4-c5d6-7890-1234-567890123456', 'Concrete Pouring', 'Pour and finish concrete foundation', 2, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Drainage
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('e1f2a3b4-c5d6-7890-1234-567890123456', 'f2a3b4c5-d6e7-8901-2345-678901234567', 'Stormwater Pipes', 'Install stormwater drainage pipes', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f2a3b4c5-d6e7-8901-2345-678901234567', 'f2a3b4c5-d6e7-8901-2345-678901234567', 'Drainage Testing', 'Test drainage system', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Floor Frame
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('a3b4c5d6-e7f8-9012-3456-789012345678', 'a3b4c5d6-e7f8-9012-3456-789012345678', 'Floor Joists', 'Install floor joists and bearers', 1, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b4c5d6e7-f8a9-0123-4567-890123456789', 'a3b4c5d6-e7f8-9012-3456-789012345678', 'Subfloor Installation', 'Install subfloor sheeting', 2, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Wall Frame
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('c5d6e7f8-a9b0-1234-5678-901234567890', 'b4c5d6e7-f8a9-0123-4567-890123456789', 'Wall Studs', 'Install wall studs and plates', 1, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d6e7f8a9-b0c1-2345-6789-012345678901', 'b4c5d6e7-f8a9-0123-4567-890123456789', 'Wall Bracing', 'Install wall bracing and ties', 2, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Roof Frame
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('e7f8a9b0-c1d2-3456-7890-123456789012', 'c5d6e7f8-a9b0-1234-5678-901234567890', 'Roof Trusses', 'Install roof trusses', 1, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f8a9b0c1-d2e3-4567-8901-234567890123', 'c5d6e7f8-a9b0-1234-5678-901234567890', 'Roof Bracing', 'Install roof bracing and ties', 2, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Roof Covering
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('a9b0c1d2-e3f4-5678-9012-345678901234', 'd6e7f8a9-b0c1-2345-6789-012345678901', 'Roof Underlay', 'Install roof underlay and insulation', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b0c1d2e3-f4a5-6789-0123-456789012345', 'd6e7f8a9-b0c1-2345-6789-012345678901', 'Roof Tiles', 'Install roof tiles or metal sheeting', 2, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c1d2e3f4-a5b6-7890-1234-567890123456', 'd6e7f8a9-b0c1-2345-6789-012345678901', 'Ridge Capping', 'Install ridge capping and flashings', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Fascia & Guttering
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('d2e3f4a5-b6c7-8901-2345-678901234567', 'e7f8a9b0-c1d2-3456-7890-123456789012', 'Fascia Installation', 'Install fascia boards and trim', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e3f4a5b6-c7d8-9012-3456-789012345678', 'e7f8a9b0-c1d2-3456-7890-123456789012', 'Gutter Installation', 'Install guttering and downpipes', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Brickwork
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('f4a5b6c7-d8e9-0123-4567-890123456789', 'f8a9b0c1-d2e3-4567-8901-234567890123', 'Brick Delivery', 'Deliver and stack bricks on site', 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a5b6c7d8-e9f0-1234-5678-901234567890', 'f8a9b0c1-d2e3-4567-8901-234567890123', 'Foundation Bricks', 'Lay foundation course of bricks', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b6c7d8e9-f0a1-2345-6789-012345678901', 'f8a9b0c1-d2e3-4567-8901-234567890123', 'Wall Bricklaying', 'Lay brick walls with proper bonding', 3, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c7d8e9f0-a1b2-3456-7890-123456789012', 'f8a9b0c1-d2e3-4567-8901-234567890123', 'Lintel Installation', 'Install lintels above openings', 4, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d8e9f0a1-b2c3-4567-8901-234567890123', 'f8a9b0c1-d2e3-4567-8901-234567890123', 'Pointing & Cleaning', 'Point mortar joints and clean bricks', 5, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: External Cladding
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('e9f0a1b2-c3d4-5678-9012-345678901234', 'a9b0c1d2-e3f4-5678-9012-345678901234', 'Weatherboard Installation', 'Install weatherboard cladding', 1, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f0a1b2c3-d4e5-6789-0123-456789012345', 'a9b0c1d2-e3f4-5678-9012-345678901234', 'Flashings', 'Install flashings and sealants', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1b2c3d4-e5f6-7890-1234-567890123456', 'a9b0c1d2-e3f4-5678-9012-345678901234', 'Paint Preparation', 'Prepare cladding for painting', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Windows & Doors
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('b2c3d4e5-f6a7-8901-2345-678901234567', 'b0c1d2e3-f4a5-6789-0123-456789012345', 'Window Installation', 'Install external windows and frames', 1, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3d4e5f6-a7b8-9012-3456-789012345678', 'b0c1d2e3-f4a5-6789-0123-456789012345', 'Door Installation', 'Install external doors and frames', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d4e5f6a7-b8c9-0123-4567-890123456789', 'b0c1d2e3-f4a5-6789-0123-456789012345', 'Sealants & Flashings', 'Install sealants and flashings', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: External Painting
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('e5f6a7b8-c9d0-1234-5678-901234567890', 'c1d2e3f4-a5b6-7890-1234-567890123456', 'Surface Preparation', 'Prepare surfaces for painting', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f6a7b8c9-d0e1-2345-6789-012345678901', 'c1d2e3f4-a5b6-7890-1234-567890123456', 'Primer Application', 'Apply primer coat to surfaces', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a7b8c9d0-e1f2-3456-7890-123456789012', 'c1d2e3f4-a5b6-7890-1234-567890123456', 'Top Coat Application', 'Apply final paint coats', 3, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Driveway & Paths
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('b8c9d0e1-f2a3-4567-8901-234667890123', 'd2e3f4a5-b6c7-8901-2345-678901234567', 'Excavation & Formwork', 'Excavate and form concrete areas', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c9d0e1f2-a3b4-5678-9012-345778901234', 'd2e3f4a5-b6c7-8901-2345-678901234567', 'Concrete Pouring', 'Pour and finish concrete', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Plumbing Rough-in
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('d0e1f2a3-b4c5-6789-0123-457789012345', 'e3f4a5b6-c7d8-9012-3456-789012345678', 'Water Supply Pipes', 'Install main water supply pipes', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e1f2a3b4-c5d6-7890-1234-577890123456', 'e3f4a5b6-c7d8-9012-3456-789012345678', 'Drainage Pipes', 'Install drainage and waste pipes', 2, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f2a3b4c5-d6e7-8901-2345-778901234567', 'e3f4a5b6-c7d8-9012-3456-789012345678', 'Ventilation Pipes', 'Install ventilation and vent pipes', 3, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Electrical Rough-in
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('a3b4c5d6-e7f8-9012-3456-889012345678', 'f4a5b6c7-d8e9-0123-4567-890123456789', 'Main Electrical Board', 'Install main electrical switchboard', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b4c5d6e7-f8a9-0123-4567-990123456789', 'f4a5b6c7-d8e9-0123-4567-890123456789', 'Electrical Wiring', 'Install electrical wiring and conduits', 2, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c5d6e7f8-a9b0-1234-5678-001234567890', 'f4a5b6c7-d8e9-0123-4567-890123456789', 'Power Points & Switches', 'Install power points and switches', 3, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: HVAC Installation
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('d6e7f8a9-b0c1-2345-6789-112345678901', 'a5b6c7d8-e9f0-1234-5678-901234567890', 'Ductwork Installation', 'Install HVAC ductwork and vents', 1, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e7f8a9b0-c1d2-3456-7890-223456789012', 'a5b6c7d8-e9f0-1234-5678-901234567890', 'HVAC Unit Installation', 'Install heating and cooling units', 2, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Internal Lining
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('f8a9b0c1-d2e3-4567-8901-334567890123', 'b6c7d8e9-f0a1-2345-6789-012345678901', 'Ceiling Installation', 'Install plasterboard ceilings', 1, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a9b0c1d2-e3f4-5678-9012-445678901234', 'b6c7d8e9-f0a1-2345-6789-012345678901', 'Wall Lining', 'Install plasterboard walls', 2, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b0c1d2e3-f4a5-6789-0123-556789012345', 'b6c7d8e9-f0a1-2345-6789-012345678901', 'Jointing & Finishing', 'Joint and finish plasterboard', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Internal Finishes
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('c1d2e3f4-a5b6-7890-1234-667890123456', 'c7d8e9f0-a1b2-3456-7890-123456789012', 'Floor Covering', 'Install floor coverings and tiles', 1, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d2e3f4a5-b6c7-8901-2345-778901234567', 'c7d8e9f0-a1b2-3456-7890-123456789012', 'Internal Painting', 'Paint internal walls and ceilings', 2, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e3f4a5b6-c7d8-9012-3456-889012345678', 'c7d8e9f0-a1b2-3456-7890-123456789012', 'Skirting & Architraves', 'Install skirting boards and architraves', 3, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Kitchen Installation
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('f4a5b6c7-d8e9-0123-4567-990123456789', 'd8e9f0a1-b2c3-4567-8901-234567890123', 'Kitchen Cabinets', 'Install kitchen cabinets and drawers', 1, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a5b6c7d8-e9f0-1234-5688-901234567890', 'd8e9f0a1-b2c3-4567-8901-234567890123', 'Kitchen Benchtop', 'Install kitchen benchtop and sink', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b6c7d8e9-f0a1-2345-6799-012345678901', 'd8e9f0a1-b2c3-4567-8901-234567890123', 'Kitchen Appliances', 'Install kitchen appliances', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Bathroom Installation
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('c7d8e9f0-a1b2-3456-7880-123456789012', 'e9f0a1b2-c3d4-5678-9012-345678901234', 'Bathroom Tiling', 'Install bathroom wall and floor tiles', 1, 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d8e9f0a1-b2c3-4567-8991-234567890123', 'e9f0a1b2-c3d4-5678-9012-345678901234', 'Bathroom Fixtures', 'Install toilet, basin, and shower', 2, 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e9f0a1b2-c3d4-5678-9022-345678901234', 'e9f0a1b2-c3d4-5678-9012-345678901234', 'Bathroom Accessories', 'Install bathroom accessories and fittings', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Final Electrical
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('f0a1b2c3-d4e5-6789-0133-456789012345', 'f0a1b2c3-d4e5-6789-0123-456789012345', 'Light Fixtures', 'Install light fixtures and fittings', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1b2c3d4-e5f6-7890-1244-567890123456', 'f0a1b2c3-d4e5-6789-0123-456789012345', 'Final Testing', 'Test all electrical systems', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Final Plumbing
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('b2c3d4e5-f6a7-8901-2355-678901234567', 'a1b2c3d4-e5f6-7890-1234-567890123456', 'Tap Installation', 'Install taps and plumbing fixtures', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c3d4e5f6-a7b8-9012-3466-789012345678', 'a1b2c3d4-e5f6-7890-1234-567890123456', 'Final Testing', 'Test all plumbing systems', 2, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task: Final Inspection & Handover
INSERT INTO standard_workflow_steps (id, standard_workflow_task_id, name, description, order_index, estimated_hours, created_at, updated_at) VALUES
('d4e5f6a7-b8c9-0123-4577-890123456789', 'b2c3d4e5-f6a7-8901-2345-678901234567', 'Final Inspection', 'Conduct final building inspection', 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e5f6a7b8-c9d0-1234-5688-901234567890', 'b2c3d4e5-f6a7-8901-2345-678901234567', 'Site Cleanup', 'Clean site and remove debris', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f6a7b8c9-d0e1-2345-6799-012345678901', 'b2c3d4e5-f6a7-8901-2345-678901234567', 'Client Handover', 'Handover keys and documentation', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 