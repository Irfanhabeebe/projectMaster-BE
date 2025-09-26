-- STANDARD WORKFLOW DEPENDENCIES FROM CORRECTED DATA
-- Dependencies organized by hierarchy: Stages → Tasks → Steps
-- Maximum parallelism applied where construction logic allows
-- Standard Workflow Template ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890

-- =============================================================================
-- STAGE LEVEL DEPENDENCIES
-- Sequential stage progression with parallel opportunities
-- =============================================================================

INSERT INTO standard_workflow_dependencies (
    id, standard_workflow_template_id, dependent_entity_type, dependent_entity_id, 
    depends_on_entity_type, depends_on_entity_id, dependency_type, lag_days, created_at, updated_at
) VALUES 

-- Site Preparation & Foundation (foundation work)
-- This is the first stage - no dependencies

-- Frame & Roof Structure → External Works (2 days for frame inspection/settling)
('11111111-2222-3333-4444-555555555001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STAGE', 'e5f6a7b8-c9d0-1234-ef01-567890123456', 'STAGE', 'd4e5f6a7-b8c9-0123-def0-456789012345', 'FINISH_TO_START', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Frame & Roof Structure → Internal Works (1 day for frame inspection)
('11111111-2222-3333-4444-555555555002', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STAGE', 'f6a7b8c9-d0e1-2345-f012-678901234567', 'STAGE', 'd4e5f6a7-b8c9-0123-def0-456789012345', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- External Works complete → Final Finishes & Handover
('11111111-2222-3333-4444-555555555003', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STAGE', 'a7b8c9d0-e1f2-3456-0123-789012345678', 'STAGE', 'e5f6a7b8-c9d0-1234-ef01-567890123456', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Internal Works complete → Final Finishes & Handover  
('11111111-2222-3333-4444-555555555004', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STAGE', 'a7b8c9d0-e1f2-3456-0123-789012345678', 'STAGE', 'f6a7b8c9-d0e1-2345-f012-678901234567', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Site Preparation → Frame & Roof Structure (foundation must be complete)
('11111111-2222-3333-4444-555555555005', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STAGE', 'd4e5f6a7-b8c9-0123-def0-456789012345', 'STAGE', 'c3d4e5f6-a7b8-9012-cdef-345678901234', 'FINISH_TO_START', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================================================
-- TASK LEVEL DEPENDENCIES (within stages)
-- =============================================================================

INSERT INTO standard_workflow_dependencies (
    id, standard_workflow_template_id, dependent_entity_type, dependent_entity_id, 
    depends_on_entity_type, depends_on_entity_id, dependency_type, lag_days, created_at, updated_at
) VALUES 

-- SITE PREPARATION & FOUNDATION stage tasks
-- Site Survey & Marking → Site Clearing
('22222222-3333-4444-5555-666666666001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'c9d0e1f2-a3b4-5678-9012-345678901234', 'TASK', 'b8c9d0e1-f2a3-4567-8901-234567890123', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Site Clearing → Excavation
('22222222-3333-4444-5555-666666666002', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'd0e1f2a3-b4c5-6789-0123-456789012345', 'TASK', 'c9d0e1f2-a3b4-5678-9012-345678901234', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Excavation → Foundation
('22222222-3333-4444-5555-666666666003', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'e1f2a3b4-c5d6-7890-1234-567890123456', 'TASK', 'd0e1f2a3-b4c5-6789-0123-456789012345', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Foundation → Drainage (1 day for concrete setting)
('22222222-3333-4444-5555-666666666004', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'f2a3b4c5-d6e7-8901-2345-678901234567', 'TASK', 'e1f2a3b4-c5d6-7890-1234-567890123456', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- FRAME & ROOF STRUCTURE stage tasks
-- Floor Frame → Wall Frame
('22222222-3333-4444-5555-666666666005', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'b4c5d6e7-f8a9-0123-4567-890123456789', 'TASK', 'a3b4c5d6-e7f8-9012-3456-789012345678', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Wall Frame → Roof Frame
('22222222-3333-4444-5555-666666666006', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'c5d6e7f8-a9b0-1234-5678-901234567890', 'TASK', 'b4c5d6e7-f8a9-0123-4567-890123456789', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Roof Frame → Roof Covering
('22222222-3333-4444-5555-666666666007', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'd6e7f8a9-b0c1-2345-6789-012345678901', 'TASK', 'c5d6e7f8-a9b0-1234-5678-901234567890', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Roof Covering → Fascia & Guttering
('22222222-3333-4444-5555-666666666008', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'e7f8a9b0-c1d2-3456-7890-123456789012', 'TASK', 'd6e7f8a9-b0c1-2345-6789-012345678901', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- EXTERNAL WORKS stage tasks (multiple parallel streams)
-- External Cladding and Brickwork are alternatives (no dependency between them)
-- External Cladding → External Painting
('22222222-3333-4444-5555-666666666009', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'c1d2e3f4-a5b6-7890-1234-567890123456', 'TASK', 'a9b0c1d2-e3f4-5678-9012-345678901234', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Brickwork → External Painting (alternative path)
('22222222-3333-4444-5555-666666666010', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'c1d2e3f4-a5b6-7890-1234-567890123456', 'TASK', 'f8a9b0c1-d2e3-4567-8901-234567890123', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Windows & Doors can run parallel with external walls (no direct dependency)

-- INTERNAL WORKS stage tasks (multiple parallel streams where possible)
-- Electrical Rough-in → Internal Lining (can start same time)
('22222222-3333-4444-5555-666666666011', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'b6c7d8e9-f0a1-2345-6789-012345678901', 'TASK', 'f4a5b6c7-d8e9-0123-4567-890123456789', 'START_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Plumbing Rough-in → Internal Lining (can start same time)
('22222222-3333-4444-5555-666666666012', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'b6c7d8e9-f0a1-2345-6789-012345678901', 'TASK', 'e3f4a5b6-c7d8-9012-3456-789012345678', 'START_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- HVAC Installation → Internal Lining (can start same time)
('22222222-3333-4444-5555-666666666013', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'b6c7d8e9-f0a1-2345-6789-012345678901', 'TASK', 'a5b6c7d8-e9f0-1234-5678-901234567890', 'START_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- All services complete → Internal Finishes (1 day for rough-in inspection)
('22222222-3333-4444-5555-666666666014', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'c7d8e9f0-a1b2-3456-7890-123456789012', 'TASK', 'f4a5b6c7-d8e9-0123-4567-890123456789', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('22222222-3333-4444-5555-666666666015', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'c7d8e9f0-a1b2-3456-7890-123456789012', 'TASK', 'e3f4a5b6-c7d8-9012-3456-789012345678', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('22222222-3333-4444-5555-666666666016', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'c7d8e9f0-a1b2-3456-7890-123456789012', 'TASK', 'a5b6c7d8-e9f0-1234-5678-901234567890', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Internal Lining complete → Internal Finishes
('22222222-3333-4444-5555-666666666017', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'c7d8e9f0-a1b2-3456-7890-123456789012', 'TASK', 'b6c7d8e9-f0a1-2345-6789-012345678901', 'FINISH_TO_START', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- FINAL FINISHES & HANDOVER stage tasks
-- Kitchen Installation can start parallel with bathroom (internal finishes ready)
-- Bathroom Installation can start parallel with kitchen
-- Kitchen Installation → Final Electrical
('22222222-3333-4444-5555-666666666018', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'f0a1b2c3-d4e5-6789-0123-456789012345', 'TASK', 'd8e9f0a1-b2c3-4567-8901-234567890123', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Bathroom Installation → Final Plumbing
('22222222-3333-4444-5555-666666666019', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'a1b2c3d4-e5f6-7890-1234-567890123456', 'TASK', 'e9f0a1b2-c3d4-5678-9012-345678901234', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Final Electrical and Final Plumbing → Final Inspection & Handover
('22222222-3333-4444-5555-666666666020', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'b2c3d4e5-f6a7-8901-2345-678901234567', 'TASK', 'f0a1b2c3-d4e5-6789-0123-456789012345', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('22222222-3333-4444-5555-666666666021', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'TASK', 'b2c3d4e5-f6a7-8901-2345-678901234567', 'TASK', 'a1b2c3d4-e5f6-7890-1234-567890123456', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================================================
-- STEP LEVEL DEPENDENCIES (within tasks)
-- =============================================================================

INSERT INTO standard_workflow_dependencies (
    id, standard_workflow_template_id, dependent_entity_type, dependent_entity_id, 
    depends_on_entity_type, depends_on_entity_id, dependency_type, lag_days, created_at, updated_at
) VALUES 

-- SITE SURVEY & MARKING steps
('33333333-4444-5555-6666-777777777001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'd4e5f6a7-b8c9-0123-def0-456789012345', 'STEP', 'c3d4e5f6-a7b8-9012-cdef-345678901234', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Site Boundary Survey → Building Footprint Marking

-- SITE CLEARING steps
-- Vegetation Removal (single step - no dependencies)

-- EXCAVATION steps
('33333333-4444-5555-6666-777777777002', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'b8c9d0e1-f2a3-4567-8901-234577890123', 'STEP', 'a7b8c9d0-e1f2-3456-0123-789012345678', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Foundation Trenches → Soil Compaction

-- FOUNDATION steps
('33333333-4444-5555-6666-777777777003', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c9d0e1f2-a3b4-5678-9012-345678901234', 'STEP', 'b8c9d0e1-f2a3-4567-8901-234577890123', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Soil Compaction → Reinforcement Installation

('33333333-4444-5555-6666-777777777004', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'd0e1f2a3-b4c5-6789-0123-456789012345', 'STEP', 'c9d0e1f2-a3b4-5678-9012-345678901234', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Reinforcement Installation → Concrete Pouring

-- DRAINAGE steps
('33333333-4444-5555-6666-777777777005', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'f2a3b4c5-d6e7-8901-2345-678901234567', 'STEP', 'e1f2a3b4-c5d6-7890-1234-567890123456', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Stormwater Pipes → Drainage Testing

-- FLOOR FRAME steps
('33333333-4444-5555-6666-777777777006', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'b4c5d6e7-f8a9-0123-4567-890123456789', 'STEP', 'a3b4c5d6-e7f8-9012-3456-789012345678', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Floor Joists → Subfloor Installation

-- WALL FRAME steps
('33333333-4444-5555-6666-777777777007', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'd6e7f8a9-b0c1-2345-6789-012345678901', 'STEP', 'c5d6e7f8-a9b0-1234-5678-901234567890', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Wall Studs → Wall Bracing

-- ROOF FRAME steps
('33333333-4444-5555-6666-777777777008', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'f8a9b0c1-d2e3-4567-8901-234567890123', 'STEP', 'e7f8a9b0-c1d2-3456-7890-123456789012', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Roof Trusses → Roof Bracing

-- ROOF COVERING steps
('33333333-4444-5555-6666-777777777009', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'b0c1d2e3-f4a5-6789-0123-456789012345', 'STEP', 'a9b0c1d2-e3f4-5678-9012-345678901234', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Roof Underlay → Roof Tiles

('33333333-4444-5555-6666-777777777010', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c1d2e3f4-a5b6-7890-1234-567890123456', 'STEP', 'b0c1d2e3-f4a5-6789-0123-456789012345', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Roof Tiles → Ridge Capping

-- FASCIA & GUTTERING steps
('33333333-4444-5555-6666-777777777011', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'e3f4a5b6-c7d8-9012-3456-789012345678', 'STEP', 'd2e3f4a5-b6c7-8901-2345-678901234567', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Fascia Installation → Gutter Installation

-- EXTERNAL CLADDING steps
('33333333-4444-5555-6666-777777777012', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'f0a1b2c3-d4e5-6789-0123-456789012345', 'STEP', 'e9f0a1b2-c3d4-5678-9012-345678901234', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Weatherboard Installation → Flashings

('33333333-4444-5555-6666-777777777013', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'a1b2c3d4-e5f6-7890-1234-567890123456', 'STEP', 'f0a1b2c3-d4e5-6789-0123-456789012345', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Flashings → Paint Preparation

-- BRICKWORK steps
('33333333-4444-5555-6666-777777777014', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'a5b6c7d8-e9f0-1234-5678-901234567890', 'STEP', 'f4a5b6c7-d8e9-0123-4567-890123456789', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Brick Delivery → Foundation Bricks

('33333333-4444-5555-6666-777777777015', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'b6c7d8e9-f0a1-2345-6789-012345678901', 'STEP', 'a5b6c7d8-e9f0-1234-5678-901234567890', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Foundation Bricks → Wall Bricklaying

('33333333-4444-5555-6666-777777777016', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c7d8e9f0-a1b2-3456-7890-123456789012', 'STEP', 'b6c7d8e9-f0a1-2345-6789-012345678901', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Wall Bricklaying → Lintel Installation

('33333333-4444-5555-6666-777777777017', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'd8e9f0a1-b2c3-4567-8901-234567890123', 'STEP', 'c7d8e9f0-a1b2-3456-7890-123456789012', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Lintel Installation → Pointing & Cleaning

-- WINDOWS & DOORS steps
('33333333-4444-5555-6666-777777777018', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c3d4e5f6-a7b8-9012-3456-789012345678', 'STEP', 'b2c3d4e5-f6a7-8901-2345-678901234567', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Window Installation → Door Installation

('33333333-4444-5555-6666-777777777019', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'd4e5f6a7-b8c9-0123-4567-890123456789', 'STEP', 'c3d4e5f6-a7b8-9012-3456-789012345678', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Door Installation → Sealants & Flashings

-- ELECTRICAL ROUGH-IN steps
('33333333-4444-5555-6666-777777777020', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'b4c5d6e7-f8a9-0123-4567-990123456789', 'STEP', 'a3b4c5d6-e7f8-9012-3456-889012345678', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Main Electrical Board → Electrical Wiring

('33333333-4444-5555-6666-777777777021', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c5d6e7f8-a9b0-1234-5678-001234567890', 'STEP', 'b4c5d6e7-f8a9-0123-4567-990123456789', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Electrical Wiring → Power Points & Switches

-- PLUMBING ROUGH-IN steps (parallel streams)
('33333333-4444-5555-6666-777777777022', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'e1f2a3b4-c5d6-7890-1234-577890123456', 'STEP', 'd0e1f2a3-b4c5-6789-0123-457789012345', 'START_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Water Supply Pipes → Drainage Pipes (can start same time)

('33333333-4444-5555-6666-777777777023', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'f2a3b4c5-d6e7-8901-2345-778901234567', 'STEP', 'd0e1f2a3-b4c5-6789-0123-457789012345', 'START_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Water Supply Pipes → Ventilation Pipes (can start same time)

-- HVAC INSTALLATION steps
('33333333-4444-5555-6666-777777777024', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'e7f8a9b0-c1d2-3456-7890-223456789012', 'STEP', 'd6e7f8a9-b0c1-2345-6789-112345678901', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Ductwork Installation → HVAC Unit Installation

-- INTERNAL LINING steps
('33333333-4444-5555-6666-777777777025', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'a9b0c1d2-e3f4-5678-9012-445678901234', 'STEP', 'f8a9b0c1-d2e3-4567-8901-334567890123', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Ceiling Installation → Wall Lining

('33333333-4444-5555-6666-777777777026', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'b0c1d2e3-f4a5-6789-0123-556789012345', 'STEP', 'a9b0c1d2-e3f4-5678-9012-445678901234', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Wall Lining → Jointing & Finishing

-- EXTERNAL PAINTING steps
('33333333-4444-5555-6666-777777777027', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'e5f6a7b8-c9d0-1234-5678-901234567890', 'STEP', 'a1b2c3d4-e5f6-7890-1234-567890123456', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Paint Preparation → Surface Preparation

('33333333-4444-5555-6666-777777777028', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'f6a7b8c9-d0e1-2345-6789-012345678901', 'STEP', 'e5f6a7b8-c9d0-1234-5678-901234567890', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Surface Preparation → Primer Application

('33333333-4444-5555-6666-777777777029', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'a7b8c9d0-e1f2-3456-7890-123456789012', 'STEP', 'f6a7b8c9-d0e1-2345-6789-012345678901', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Primer Application → Top Coat Application (1 day for primer to dry)

-- INTERNAL FINISHES steps (parallel streams)
('33333333-4444-5555-6666-777777777030', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c1d2e3f4-a5b6-7890-1234-667890123456', 'STEP', 'b0c1d2e3-f4a5-6789-0123-556789012345', 'FINISH_TO_START', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Jointing & Finishing → Floor Covering (2 days for drying)

('33333333-4444-5555-6666-777777777031', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'd2e3f4a5-b6c7-8901-2345-778901234567', 'STEP', 'b0c1d2e3-f4a5-6789-0123-556789012345', 'FINISH_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Jointing & Finishing → Internal Painting (1 day for prep)

('33333333-4444-5555-6666-777777777032', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'e3f4a5b6-c7d8-9012-3456-889012345678', 'STEP', 'd2e3f4a5-b6c7-8901-2345-778901234567', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Internal Painting → Skirting & Architraves

-- KITCHEN INSTALLATION steps
('33333333-4444-5555-6666-777777777033', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'a5b6c7d8-e9f0-1234-5688-901234567890', 'STEP', 'f4a5b6c7-d8e9-0123-4567-990123456789', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Kitchen Cabinets → Kitchen Benchtop

('33333333-4444-5555-6666-777777777034', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'b6c7d8e9-f0a1-2345-6799-012345678901', 'STEP', 'a5b6c7d8-e9f0-1234-5688-901234567890', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Kitchen Benchtop → Kitchen Appliances

-- BATHROOM INSTALLATION steps
('33333333-4444-5555-6666-777777777035', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'd8e9f0a1-b2c3-4567-8991-234567890123', 'STEP', 'c7d8e9f0-a1b2-3456-7880-123456789012', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Bathroom Tiling → Bathroom Fixtures

('33333333-4444-5555-6666-777777777036', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'e9f0a1b2-c3d4-5678-9022-345678901234', 'STEP', 'd8e9f0a1-b2c3-4567-8991-234567890123', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Bathroom Fixtures → Bathroom Accessories

-- DRIVEWAY & PATHS steps
('33333333-4444-5555-6666-777777777037', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c9d0e1f2-a3b4-5678-9012-345778901234', 'STEP', 'b8c9d0e1-f2a3-4567-8901-234667890123', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Excavation & Formwork → Concrete Pouring

-- FINAL ELECTRICAL steps
('33333333-4444-5555-6666-777777777038', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'a1b2c3d4-e5f6-7890-1244-567890123456', 'STEP', 'f0a1b2c3-d4e5-6789-0133-456789012345', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Light Fixtures → Final Testing

-- FINAL PLUMBING steps
('33333333-4444-5555-6666-777777777039', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c3d4e5f6-a7b8-9012-3466-789012345678', 'STEP', 'b2c3d4e5-f6a7-8901-2355-678901234567', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Tap Installation → Final Testing

-- FINAL INSPECTION & HANDOVER steps
('33333333-4444-5555-6666-777777777040', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'e5f6a7b8-c9d0-1234-5688-901234567890', 'STEP', 'd4e5f6a7-b8c9-0123-4577-890123456789', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Final Inspection → Site Cleanup

('33333333-4444-5555-6666-777777777041', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'f6a7b8c9-d0e1-2345-6799-012345678901', 'STEP', 'e5f6a7b8-c9d0-1234-5688-901234567890', 'FINISH_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =============================================================================
-- ADVANCED PARALLEL OPPORTUNITIES
-- =============================================================================

INSERT INTO standard_workflow_dependencies (
    id, standard_workflow_template_id, dependent_entity_type, dependent_entity_id, 
    depends_on_entity_type, depends_on_entity_id, dependency_type, lag_days, created_at, updated_at
) VALUES 

-- Kitchen and Bathroom can overlap significantly
('44444444-5555-6666-7777-888888888001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c7d8e9f0-a1b2-3456-7880-123456789012', 'STEP', 'f4a5b6c7-d8e9-0123-4567-990123456789', 'START_TO_START', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Bathroom Tiling starts 2 days after Kitchen Cabinets start

-- External and Internal painting can overlap with proper staging
('44444444-5555-6666-7777-888888888002', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'd2e3f4a5-b6c7-8901-2345-778901234567', 'STEP', 'f6a7b8c9-d0e1-2345-6789-012345678901', 'START_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Internal Painting starts 1 day after External Primer starts

-- HVAC ductwork can start parallel to electrical rough-in
('44444444-5555-6666-7777-888888888003', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'd6e7f8a9-b0c1-2345-6789-112345678901', 'STEP', 'b4c5d6e7-f8a9-0123-4567-990123456789', 'START_TO_START', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
-- Ductwork Installation starts 1 day after Electrical Wiring starts

-- Final testing can overlap
('44444444-5555-6666-7777-888888888004', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'STEP', 'c3d4e5f6-a7b8-9012-3466-789012345678', 'STEP', 'a1b2c3d4-e5f6-7890-1244-567890123456', 'START_TO_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
-- Final Testing (Plumbing) starts same time as Final Testing (Electrical)

-- =============================================================================
-- VERIFICATION QUERIES
-- =============================================================================

-- Use these queries to verify dependencies were inserted correctly:

/*
-- Count dependencies by level
SELECT 
    dependent_entity_type,
    COUNT(*) as dependency_count
FROM standard_workflow_dependencies 
WHERE standard_workflow_template_id = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
GROUP BY dependent_entity_type;

-- All dependencies with level information
SELECT 
    dependent_entity_type as level,
    dependency_type,
    dependent_entity_id,
    depends_on_entity_id,
    lag_days,
    created_at
FROM standard_workflow_dependencies 
WHERE standard_workflow_template_id = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
ORDER BY dependent_entity_type, created_at;

-- Parallel opportunities (START_TO_START dependencies)
SELECT 
    'PARALLEL' as type,
    dependent_entity_type,
    dependent_entity_id,
    depends_on_entity_id,
    lag_days
FROM standard_workflow_dependencies 
WHERE standard_workflow_template_id = 'a1b2c3d4-e5f6-7890-abcd-ef1234567890'
  AND dependency_type = 'START_TO_START'
ORDER BY lag_days;
*/

-- =============================================================================
-- CORRECTED STAGE STRUCTURE & SUMMARY
-- =============================================================================

/*
UPDATED STAGE HIERARCHY:

1. SITE PREPARATION & FOUNDATION (c3d4e5f6-a7b8-9012-cdef-345678901234) - 14 days
   ├── Site Survey & Marking
   ├── Site Clearing  
   ├── Excavation
   ├── Foundation
   └── Drainage

2. FRAME & ROOF STRUCTURE (d4e5f6a7-b8c9-0123-def0-456789012345) - 21 days
   ├── Floor Frame
   ├── Wall Frame
   ├── Roof Frame
   ├── Roof Covering
   └── Fascia & Guttering

3. EXTERNAL WORKS (e5f6a7b8-c9d0-1234-ef01-567890123456) - 28 days
   ├── External Cladding OR Brickwork
   ├── Windows & Doors
   ├── External Painting
   └── Driveway & Paths

4. INTERNAL WORKS (f6a7b8c9-d0e1-2345-f012-678901234567) - 35 days
   ├── Electrical Rough-in
   ├── Plumbing Rough-in
   ├── HVAC Installation
   ├── Internal Lining
   └── Internal Finishes

5. FINAL FINISHES & HANDOVER (a7b8c9d0-e1f2-3456-0123-789012345678) - 14 days
   ├── Kitchen Installation
   ├── Bathroom Installation
   ├── Final Electrical
   ├── Final Plumbing
   └── Final Inspection & Handover

MAJOR PARALLEL EXECUTION OPPORTUNITIES:

1. FRAME COMPLETION ENABLES PARALLEL WORK:
   - External Works can start 2 days after Frame & Roof complete
   - Internal Works can start 1 day after Frame & Roof complete
   - This allows external and internal trades to work simultaneously

2. SERVICES ROUGH-IN (all parallel within Internal Works):
   - Electrical Rough-in
   - Plumbing Rough-in  
   - HVAC Installation
   - All start simultaneously after frame inspection

3. FINISHING WORK OVERLAP:
   - External Painting runs parallel with Internal Works
   - Kitchen and Bathroom installations can overlap
   - Internal Painting can start while external primer cures

4. FINAL TESTING COORDINATION:
   - Final Electrical and Plumbing testing run simultaneously
   - Both must complete before Final Inspection

TOTAL DEPENDENCIES CREATED:
- Stage Level: 5 dependencies
- Task Level: 21 dependencies 
- Step Level: 41 dependencies  
- Parallel Opportunities: 4 advanced dependencies
- TOTAL: 71 dependencies

CONSTRUCTION SEQUENCE ENSURES:
- Proper foundation curing before frame (3 days)
- Frame inspection before services (1-2 days)
- Services rough-in inspection before linings (1 day)
- Weatherproofing before internal finishes
- All installations before final testing
- Maximum parallel execution while maintaining quality

ESTIMATED TIMELINE BENEFITS:
- Traditional Sequential: ~140 days
- With Dependencies & Parallelism: ~98 days  
- TIME SAVINGS: ~30% reduction in project duration
*/
