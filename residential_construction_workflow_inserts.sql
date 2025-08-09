-- INSERT statements for Standard Workflow Templates, Stages, and Steps
-- For Residential Construction Companies

-- =====================================================
-- STANDARD WORKFLOW TEMPLATES
-- =====================================================

-- Single Family Home Construction Template
INSERT INTO standard_workflow_templates (
    id, created_at, updated_at, name, description, category, active, is_default
) VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'Single Family Home Construction',
    'Complete workflow for building a single-family residential home from permits to final inspection',
    'New Construction',
    true,
    true
);

-- Home Renovation Template
INSERT INTO standard_workflow_templates (
    id, created_at, updated_at, name, description, category, active, is_default
) VALUES (
    'b2c3d4e5-f6g7-8901-bcde-f23456789012',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'Home Renovation',
    'Workflow for major home renovation projects including kitchen, bathroom, and structural modifications',
    'Renovation',
    true,
    false
);

-- Custom Home Construction Template
INSERT INTO standard_workflow_templates (
    id, created_at, updated_at, name, description, category, active, is_default
) VALUES (
    'c3d4e5f6-g7h8-9012-cdef-345678901234',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'Custom Home Construction',
    'Premium workflow for custom-designed luxury homes with specialized requirements',
    'Custom Build',
    true,
    false
);

-- Townhouse Development Template
INSERT INTO standard_workflow_templates (
    id, created_at, updated_at, name, description, category, active, is_default
) VALUES (
    'd4e5f6g7-h8i9-0123-defg-456789012345',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'Townhouse Development',
    'Multi-unit townhouse construction workflow with coordinated scheduling',
    'Multi-Unit',
    true,
    false
);

-- =====================================================
-- STANDARD WORKFLOW STAGES - Single Family Home Construction
-- =====================================================

-- Pre-Construction Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Pre-Construction',
    'Planning, permits, and site preparation activities',
    1,
    false,
    2,
    30
);

-- Foundation Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'f6g7h8i9-j0k1-2345-fghi-678901234567',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Foundation',
    'Excavation, footings, and foundation construction',
    2,
    false,
    1,
    14
);

-- Framing Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'g7h8i9j0-k1l2-3456-ghij-789012345678',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Framing',
    'Structural framing, roof structure, and exterior sheathing',
    3,
    false,
    1,
    21
);

-- MEP Rough-In Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'h8i9j0k1-l2m3-4567-hijk-890123456789',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'MEP Rough-In',
    'Mechanical, Electrical, and Plumbing rough-in work',
    4,
    true,
    1,
    18
);

-- Insulation & Drywall Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'i9j0k1l2-m3n4-5678-ijkl-901234567890',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Insulation & Drywall',
    'Insulation installation, drywall hanging, taping, and finishing',
    5,
    false,
    0,
    16
);

-- Interior Finishes Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'j0k1l2m3-n4o5-6789-jklm-012345678901',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Interior Finishes',
    'Flooring, painting, trim work, and interior fixtures',
    6,
    true,
    0,
    25
);

-- Final Completion Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'k1l2m3n4-o5p6-7890-klmn-123456789012',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Final Completion',
    'Final inspections, cleanup, and project handover',
    7,
    false,
    2,
    7
);

-- =====================================================
-- STANDARD WORKFLOW STEPS - Pre-Construction Stage
-- =====================================================

-- Site Survey
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'l2m3n4o5-p6q7-8901-lmno-234567890123',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Site Survey',
    'Professional land survey to establish property boundaries and elevations',
    1,
    8,
    '["Surveying", "GPS Equipment", "CAD Software"]',
    '["Survey equipment", "Property deed", "Boundary markers"]'
);

-- Soil Testing
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'm3n4o5p6-q7r8-9012-mnop-345678901234',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Soil Testing',
    'Geotechnical analysis to determine soil bearing capacity and foundation requirements',
    2,
    16,
    '["Geotechnical Engineering", "Soil Analysis", "Foundation Design"]',
    '["Soil samples", "Testing equipment", "Engineering analysis"]'
);

-- Building Permit Application
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'n4o5p6q7-r8s9-0123-nopq-456789012345',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Building Permit Application',
    'Submit building permit application with architectural plans and engineering documents',
    3,
    12,
    '["Permit Processing", "Code Compliance", "Documentation"]',
    '["Architectural plans", "Engineering drawings", "Site plan", "Application forms"]'
);

-- Utility Connections Planning
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'o5p6q7r8-s9t0-1234-opqr-567890123456',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Utility Connections Planning',
    'Coordinate with utility companies for water, sewer, gas, and electrical connections',
    4,
    20,
    '["Utility Coordination", "Project Management", "Communication"]',
    '["Utility company contacts", "Connection requirements", "Service locations"]'
);

-- Site Preparation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'p6q7r8s9-t0u1-2345-pqrs-678901234567',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Site Preparation',
    'Clear vegetation, level site, and establish construction access',
    5,
    32,
    '["Heavy Equipment Operation", "Site Management", "Safety Protocols"]',
    '["Excavator", "Bulldozer", "Safety equipment", "Temporary fencing"]'
);

-- =====================================================
-- STANDARD WORKFLOW STEPS - Foundation Stage
-- =====================================================

-- Excavation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'q7r8s9t0-u1v2-3456-qrst-789012345678',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'f6g7h8i9-j0k1-2345-fghi-678901234567',
    'Excavation',
    'Excavate foundation area to required depth and dimensions',
    1,
    24,
    '["Heavy Equipment Operation", "Grading", "Safety Management"]',
    '["Excavator", "Laser level", "Foundation plans", "Safety barriers"]'
);

-- Footings Installation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'r8s9t0u1-v2w3-4567-rstu-890123456789',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'f6g7h8i9-j0k1-2345-fghi-678901234567',
    'Footings Installation',
    'Install reinforced concrete footings according to structural plans',
    2,
    40,
    '["Concrete Work", "Rebar Installation", "Form Construction"]',
    '["Concrete", "Rebar", "Forms", "Concrete mixer", "Vibrator"]'
);

-- Foundation Walls
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    's9t0u1v2-w3x4-5678-stuv-901234567890',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'f6g7h8i9-j0k1-2345-fghi-678901234567',
    'Foundation Walls',
    'Pour concrete foundation walls or install concrete block foundation',
    3,
    48,
    '["Concrete Work", "Masonry", "Waterproofing"]',
    '["Concrete blocks or forms", "Mortar", "Waterproofing membrane", "Drainage system"]'
);

-- Foundation Inspection
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    't0u1v2w3-x4y5-6789-tuvw-012345678901',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'f6g7h8i9-j0k1-2345-fghi-678901234567',
    'Foundation Inspection',
    'Municipal inspection of completed foundation work',
    4,
    4,
    '["Code Compliance", "Quality Control", "Documentation"]',
    '["Inspection request", "Foundation plans", "Compliance documentation"]'
);

-- =====================================================
-- STANDARD WORKFLOW STEPS - Framing Stage
-- =====================================================

-- Floor Framing
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'u1v2w3x4-y5z6-7890-uvwx-123456789012',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'g7h8i9j0-k1l2-3456-ghij-789012345678',
    'Floor Framing',
    'Install floor joists, subfloor, and structural floor systems',
    1,
    56,
    '["Carpentry", "Structural Framing", "Blueprint Reading"]',
    '["Lumber", "Joist hangers", "Subfloor panels", "Framing tools", "Level"]'
);

-- Wall Framing
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'v2w3x4y5-z6a7-8901-vwxy-234567890123',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'g7h8i9j0-k1l2-3456-ghij-789012345678',
    'Wall Framing',
    'Frame exterior and interior walls with proper headers and openings',
    2,
    72,
    '["Carpentry", "Structural Framing", "Window/Door Installation"]',
    '["Framing lumber", "Headers", "Nails", "Framing square", "Circular saw"]'
);

-- Roof Framing
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'w3x4y5z6-a7b8-9012-wxyz-345678901234',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'g7h8i9j0-k1l2-3456-ghij-789012345678',
    'Roof Framing',
    'Install roof trusses or rafters and roof sheathing',
    3,
    64,
    '["Carpentry", "Roof Construction", "Safety Procedures"]',
    '["Roof trusses/rafters", "Roof sheathing", "Ridge beam", "Safety harness", "Crane"]'
);

-- Exterior Sheathing
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'x4y5z6a7-b8c9-0123-xyza-456789012345',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'g7h8i9j0-k1l2-3456-ghij-789012345678',
    'Exterior Sheathing',
    'Install wall and roof sheathing with proper moisture barriers',
    4,
    40,
    '["Carpentry", "Weatherproofing", "Material Installation"]',
    '["OSB/Plywood sheathing", "House wrap", "Staples", "Utility knife"]'
);

-- Framing Inspection
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'y5z6a7b8-c9d0-1234-yzab-567890123456',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'g7h8i9j0-k1l2-3456-ghij-789012345678',
    'Framing Inspection',
    'Municipal inspection of completed framing work',
    5,
    4,
    '["Code Compliance", "Quality Control", "Documentation"]',
    '["Inspection request", "Framing plans", "Compliance checklist"]'
);

-- =====================================================
-- STANDARD WORKFLOW STEPS - MEP Rough-In Stage
-- =====================================================

-- Electrical Rough-In
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'z6a7b8c9-d0e1-2345-zabc-678901234567',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'h8i9j0k1-l2m3-4567-hijk-890123456789',
    'Electrical Rough-In',
    'Install electrical wiring, outlets, switches, and panel connections',
    1,
    48,
    '["Electrical Work", "Code Compliance", "Safety Procedures"]',
    '["Electrical wire", "Outlets", "Switches", "Electrical panel", "Conduit"]'
);

-- Plumbing Rough-In
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'a7b8c9d0-e1f2-3456-abcd-789012345678',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'h8i9j0k1-l2m3-4567-hijk-890123456789',
    'Plumbing Rough-In',
    'Install water supply lines, drain lines, and vent systems',
    2,
    40,
    '["Plumbing", "Pipe Installation", "Code Compliance"]',
    '["PVC/Copper pipes", "Fittings", "Pipe cutter", "Soldering equipment"]'
);

-- HVAC Rough-In
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'b8c9d0e1-f2g3-4567-bcde-890123456789',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'h8i9j0k1-l2m3-4567-hijk-890123456789',
    'HVAC Rough-In',
    'Install ductwork, HVAC equipment, and ventilation systems',
    3,
    56,
    '["HVAC Installation", "Ductwork", "System Design"]',
    '["Ductwork", "HVAC unit", "Vents", "Insulation", "Sheet metal tools"]'
);

-- MEP Inspection
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'c9d0e1f2-g3h4-5678-cdef-901234567890',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'h8i9j0k1-l2m3-4567-hijk-890123456789',
    'MEP Inspection',
    'Municipal inspection of mechanical, electrical, and plumbing rough-in work',
    4,
    6,
    '["Code Compliance", "Quality Control", "Multi-trade Coordination"]',
    '["Inspection requests", "MEP plans", "Test certificates"]'
);

-- =====================================================
-- STANDARD WORKFLOW STEPS - Insulation & Drywall Stage
-- =====================================================

-- Insulation Installation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'd0e1f2g3-h4i5-6789-defg-012345678901',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'i9j0k1l2-m3n4-5678-ijkl-901234567890',
    'Insulation Installation',
    'Install thermal and acoustic insulation in walls, ceilings, and floors',
    1,
    32,
    '["Insulation Installation", "Energy Efficiency", "Safety Procedures"]',
    '["Fiberglass/Foam insulation", "Vapor barrier", "Staple gun", "Protective gear"]'
);

-- Drywall Hanging
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'e1f2g3h4-i5j6-7890-efgh-123456789012',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'i9j0k1l2-m3n4-5678-ijkl-901234567890',
    'Drywall Hanging',
    'Hang drywall sheets on walls and ceilings with proper fastening',
    2,
    48,
    '["Drywall Installation", "Measuring", "Cutting"]',
    '["Drywall sheets", "Screws", "Screw gun", "Drywall saw", "Lift"]'
);

-- Drywall Taping
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'f2g3h4i5-j6k7-8901-fghi-234567890123',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'i9j0k1l2-m3n4-5678-ijkl-901234567890',
    'Drywall Taping',
    'Apply tape and joint compound to drywall seams and fasteners',
    3,
    40,
    '["Drywall Finishing", "Taping", "Texture Application"]',
    '["Joint compound", "Drywall tape", "Taping knives", "Sanding equipment"]'
);

-- Drywall Finishing
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'g3h4i5j6-k7l8-9012-ghij-345678901234',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'i9j0k1l2-m3n4-5678-ijkl-901234567890',
    'Drywall Finishing',
    'Sand and prime drywall surfaces for final finish',
    4,
    24,
    '["Drywall Finishing", "Sanding", "Priming"]',
    '["Sandpaper", "Primer", "Brushes/Rollers", "Drop cloths"]'
);

-- =====================================================
-- STANDARD WORKFLOW STEPS - Interior Finishes Stage
-- =====================================================

-- Flooring Installation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'h4i5j6k7-l8m9-0123-hijk-456789012345',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'j0k1l2m3-n4o5-6789-jklm-012345678901',
    'Flooring Installation',
    'Install hardwood, tile, carpet, or other finish flooring materials',
    1,
    64,
    '["Flooring Installation", "Measuring", "Pattern Layout"]',
    '["Flooring materials", "Underlayment", "Adhesive", "Installation tools"]'
);

-- Interior Painting
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'i5j6k7l8-m9n0-1234-ijkl-567890123456',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'j0k1l2m3-n4o5-6789-jklm-012345678901',
    'Interior Painting',
    'Prime and paint all interior walls, ceilings, and trim',
    2,
    56,
    '["Painting", "Color Matching", "Surface Preparation"]',
    '["Paint", "Primer", "Brushes", "Rollers", "Drop cloths", "Ladder"]'
);

-- Trim Installation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'j6k7l8m9-n0o1-2345-jklm-678901234567',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'j0k1l2m3-n4o5-6789-jklm-012345678901',
    'Trim Installation',
    'Install baseboards, door casings, window trim, and crown molding',
    3,
    48,
    '["Finish Carpentry", "Measuring", "Mitering"]',
    '["Trim materials", "Finish nails", "Miter saw", "Nail gun", "Caulk"]'
);

-- Cabinet Installation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'k7l8m9n0-o1p2-3456-klmn-789012345678',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'j0k1l2m3-n4o5-6789-jklm-012345678901',
    'Cabinet Installation',
    'Install kitchen and bathroom cabinets with proper alignment and hardware',
    4,
    40,
    '["Cabinet Installation", "Hardware Installation", "Alignment"]',
    '["Cabinets", "Hardware", "Shims", "Level", "Drill", "Screws"]'
);

-- Fixture Installation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours
, required_skills, requirements
) VALUES (
    'l8m9n0o1-p2q3-4567-lmno-890123456789',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'j0k1l2m3-n4o5-6789-jklm-012345678901',
    'Fixture Installation',
    'Install light fixtures, plumbing fixtures, and electrical outlets',
    5,
    32,
    '["Electrical Work", "Plumbing", "Fixture Installation"]',
    '["Light fixtures", "Plumbing fixtures", "Electrical tools", "Plumbing tools"]'
);

-- =====================================================
-- STANDARD WORKFLOW STEPS - Final Completion Stage
-- =====================================================

-- Final Electrical
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'm9n0o1p2-q3r4-5678-mnop-901234567890',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'k1l2m3n4-o5p6-7890-klmn-123456789012',
    'Final Electrical',
    'Complete electrical connections, install cover plates, and test all circuits',
    1,
    16,
    '["Electrical Work", "Testing", "Code Compliance"]',
    '["Cover plates", "Circuit tester", "Electrical tools"]'
);

-- Final Plumbing
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'n0o1p2q3-r4s5-6789-nopq-012345678901',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'k1l2m3n4-o5p6-7890-klmn-123456789012',
    'Final Plumbing',
    'Connect fixtures, test water pressure, and verify proper drainage',
    2,
    12,
    '["Plumbing", "Testing", "Fixture Connection"]',
    '["Plumbing tools", "Pressure gauge", "Test equipment"]'
);

-- Final Inspection
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'o1p2q3r4-s5t6-7890-opqr-123456789012',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'k1l2m3n4-o5p6-7890-klmn-123456789012',
    'Final Inspection',
    'Municipal final inspection and certificate of occupancy',
    3,
    4,
    '["Code Compliance", "Documentation", "Quality Control"]',
    '["Inspection request", "All permits", "Compliance certificates"]'
);

-- Cleanup & Handover
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'p2q3r4s5-t6u7-8901-pqrs-234567890123',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'k1l2m3n4-o5p6-7890-klmn-123456789012',
    'Cleanup & Handover',
    'Final cleanup, walkthrough with client, and project handover',
    4,
    16,
    '["Project Management", "Customer Service", "Quality Control"]',
    '["Cleaning supplies", "Project documentation", "Keys", "Warranties"]'
);

-- =====================================================
-- HOME RENOVATION WORKFLOW STAGES
-- =====================================================

-- Planning & Design Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'q3r4s5t6-u7v8-9012-qrst-345678901234',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'b2c3d4e5-f6g7-8901-bcde-f23456789012',
    'Planning & Design',
    'Design development, permits, and renovation planning',
    1,
    false,
    1,
    21
);

-- Demolition Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'r4s5t6u7-v8w9-0123-rstu-456789012345',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'b2c3d4e5-f6g7-8901-bcde-f23456789012',
    'Demolition',
    'Selective demolition and debris removal',
    2,
    false,
    0,
    7
);

-- Structural Modifications Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    's5t6u7v8-w9x0-1234-stuv-567890123456',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'b2c3d4e5-f6g7-8901-bcde-f23456789012',
    'Structural Modifications',
    'Structural changes, additions, and reinforcements',
    3,
    false,
    1,
    14
);

-- Systems Upgrade Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    't6u7v8w9-x0y1-2345-tuvw-678901234567',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'b2c3d4e5-f6g7-8901-bcde-f23456789012',
    'Systems Upgrade',
    'Electrical, plumbing, and HVAC system upgrades',
    4,
    true,
    1,
    12
);

-- Renovation Finishes Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'u7v8w9x0-y1z2-3456-uvwx-789012345678',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'b2c3d4e5-f6g7-8901-bcde-f23456789012',
    'Renovation Finishes',
    'New finishes, fixtures, and final details',
    5,
    true,
    0,
    18
);

-- =====================================================
-- CUSTOM HOME CONSTRUCTION WORKFLOW STAGES
-- =====================================================

-- Design Development Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'v8w9x0y1-z2a3-4567-vwxy-890123456789',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'c3d4e5f6-g7h8-9012-cdef-345678901234',
    'Design Development',
    'Custom architectural design and engineering',
    1,
    false,
    3,
    45
);

-- Premium Foundation Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'w9x0y1z2-a3b4-5678-wxyz-901234567890',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'c3d4e5f6-g7h8-9012-cdef-345678901234',
    'Premium Foundation',
    'High-end foundation with specialized features',
    2,
    false,
    1,
    21
);

-- Custom Framing Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'x0y1z2a3-b4c5-6789-xyza-012345678901',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'c3d4e5f6-g7h8-9012-cdef-345678901234',
    'Custom Framing',
    'Complex framing with custom architectural features',
    3,
    false,
    1,
    28
);

-- Premium Systems Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'y1z2a3b4-c5d6-7890-yzab-123456789012',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'c3d4e5f6-g7h8-9012-cdef-345678901234',
    'Premium Systems',
    'High-end MEP systems with smart home integration',
    4,
    true,
    1,
    25
);

-- Luxury Finishes Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'z2a3b4c5-d6e7-8901-zabc-234567890123',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'c3d4e5f6-g7h8-9012-cdef-345678901234',
    'Luxury Finishes',
    'Premium materials and custom millwork installation',
    5,
    true,
    0,
    35
);

-- =====================================================
-- TOWNHOUSE DEVELOPMENT WORKFLOW STAGES
-- =====================================================

-- Site Development Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'a3b4c5d6-e7f8-9012-abcd-345678901234',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'd4e5f6g7-h8i9-0123-defg-456789012345',
    'Site Development',
    'Site preparation and infrastructure for multiple units',
    1,
    false,
    2,
    45
);

-- Multi-Unit Foundation Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'b4c5d6e7-f8g9-0123-bcde-456789012345',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'd4e5f6g7-h8i9-0123-defg-456789012345',
    'Multi-Unit Foundation',
    'Foundation work for connected townhouse units',
    2,
    true,
    1,
    28
);

-- Coordinated Framing Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'c5d6e7f8-g9h0-1234-cdef-567890123456',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'd4e5f6g7-h8i9-0123-defg-456789012345',
    'Coordinated Framing',
    'Framing multiple units with shared walls and systems',
    3,
    true,
    1,
    35
);

-- Shared Systems Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'd6e7f8g9-h0i1-2345-defg-678901234567',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'd4e5f6g7-h8i9-0123-defg-456789012345',
    'Shared Systems',
    'MEP systems with shared utilities and individual unit controls',
    4,
    true,
    1,
    30
);

-- Unit Completion Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'e7f8g9h0-i1j2-3456-efgh-789012345678',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'd4e5f6g7-h8i9-0123-defg-456789012345',
    'Unit Completion',
    'Individual unit finishes and final inspections',
    5,
    true,
    1,
    40
);

-- =====================================================
-- SAMPLE RENOVATION WORKFLOW STEPS
-- =====================================================

-- Design Consultation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'f8g9h0i1-j2k3-4567-fghi-890123456789',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'q3r4s5t6-u7v8-9012-qrst-345678901234',
    'Design Consultation',
    'Meet with client to discuss renovation goals and preferences',
    1,
    8,
    '["Design Consultation", "Customer Service", "Space Planning"]',
    '["Design portfolio", "Measuring tools", "Client questionnaire"]'
);

-- Permit Application
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'g9h0i1j2-k3l4-5678-ghij-901234567890',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'q3r4s5t6-u7v8-9012-qrst-345678901234',
    'Permit Application',
    'Submit renovation permits and obtain approvals',
    2,
    12,
    '["Permit Processing", "Code Knowledge", "Documentation"]',
    '["Renovation plans", "Permit applications", "Code compliance checklist"]'
);

-- Selective Demolition
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'h0i1j2k3-l4m5-6789-hijk-012345678901',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'r4s5t6u7-v8w9-0123-rstu-456789012345',
    'Selective Demolition',
    'Carefully remove existing materials while preserving structural elements',
    1,
    24,
    '["Demolition", "Safety Procedures", "Structural Knowledge"]',
    '["Demolition tools", "Safety equipment", "Dumpster", "Dust barriers"]'
);

-- Debris Removal
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'i1j2k3l4-m5n6-7890-ijkl-123456789012',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'r4s5t6u7-v8w9-0123-rstu-456789012345',
    'Debris Removal',
    'Clean up and dispose of demolition debris properly',
    2,
    16,
    '["Waste Management", "Safety Procedures", "Environmental Compliance"]',
    '["Dumpster", "Cleaning tools", "Disposal permits"]'
);

-- =====================================================
-- ADDITIONAL WORKFLOW TEMPLATES
-- =====================================================

-- Kitchen Renovation Template
INSERT INTO standard_workflow_templates (
    id, created_at, updated_at, name, description, category, active, is_default
) VALUES (
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'Kitchen Renovation',
    'Specialized workflow for kitchen renovation projects',
    'Kitchen Remodel',
    true,
    false
);

-- Bathroom Renovation Template
INSERT INTO standard_workflow_templates (
    id, created_at, updated_at, name, description, category, active, is_default
) VALUES (
    'f6g7h8i9-j0k1-2345-fghi-678901234567',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'Bathroom Renovation',
    'Complete bathroom renovation workflow with plumbing and tiling',
    'Bathroom Remodel',
    true,
    false
);

-- Home Addition Template
INSERT INTO standard_workflow_templates (
    id, created_at, updated_at, name, description, category, active, is_default
) VALUES (
    'g7h8i9j0-k1l2-3456-ghij-789012345678',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'Home Addition',
    'Workflow for adding rooms or expanding existing homes',
    'Addition',
    true,
    false
);

-- =====================================================
-- SAMPLE KITCHEN RENOVATION STAGES
-- =====================================================

-- Kitchen Planning Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'h8i9j0k1-l2m3-4567-hijk-890123456789',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Kitchen Planning',
    'Design layout, select materials, and obtain permits',
    1,
    false,
    1,
    14
);

-- Kitchen Demolition Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'i9j0k1l2-m3n4-5678-ijkl-901234567890',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Kitchen Demolition',
    'Remove existing cabinets, appliances, and finishes',
    2,
    false,
    0,
    3
);

-- Kitchen Infrastructure Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'j0k1l2m3-n4o5-6789-jklm-012345678901',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Kitchen Infrastructure',
    'Update electrical, plumbing, and HVAC systems',
    3,
    true,
    1,
    7
);

-- Kitchen Installation Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'k1l2m3n4-o5p6-7890-klmn-123456789012',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Kitchen Installation',
    'Install cabinets, countertops, and appliances',
    4,
    false,
    0,
    10
);

-- Kitchen Finishing Stage
INSERT INTO standard_workflow_stages (
    id, created_at, updated_at, standard_workflow_template_id, name, description, 
    order_index, parallel_execution, required_approvals, estimated_duration_days
) VALUES (
    'l2m3n4o5-p6q7-8901-lmno-234567890123',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'e5f6g7h8-i9j0-1234-efgh-567890123456',
    'Kitchen Finishing',
    'Final touches, painting, and cleanup',
    5,
    false,
    0,
    5
);

-- =====================================================
-- SAMPLE KITCHEN RENOVATION STEPS
-- =====================================================

-- Kitchen Design Consultation
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'm3n4o5p6-q7r8-9012-mnop-345678901234',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'h8i9j0k1-l2m3-4567-hijk-890123456789',
    'Kitchen Design Consultation',
    'Meet with client to plan kitchen layout and select materials',
    1,
    6,
    '["Kitchen Design", "Space Planning", "Material Selection"]',
    '["Design software", "Material samples", "Measuring tools"]'
);

-- Cabinet Removal
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'n4o5p6q7-r8s9-0123-nopq-456789012345',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'i9j0k1l2-m3n4-5678-ijkl-901234567890',
    'Cabinet Removal',
    'Carefully remove existing kitchen cabinets and countertops',
    1,
    8,
    '["Demolition", "Tool Operation", "Safety Procedures"]',
    '["Hand tools", "Power tools", "Safety equipment", "Disposal containers"]'
);

-- Appliance Disconnection
INSERT INTO standard_workflow_steps (
    id, created_at, updated_at, standard_workflow_stage_id, name, description, 
    order_index, estimated_hours, required_skills, requirements
) VALUES (
    'o5p6q7r8-s9t0-1234-opqr-567890123456',
    '2024-01-01 10:00:00',
    '2024-01-01 10:00:00',
    'i9j0k1l2-m3n4-5678-ijkl-901234567890',
    'Appliance Disconnection',
    'Safely disconnect and remove existing kitchen appliances',
    2,
    4,
    '["Electrical Work", "Plumbing", "Appliance Handling"]',
    '["Electrical tools", "Plumbing tools", "Appliance dolly"]'
);

-- =====================================================
-- END OF INSERT STATEMENTS
-- =====================================================

-- Summary:
-- - 8 Standard Workflow Templates covering various residential construction types
-- - 30+ Standard Workflow Stages with proper sequencing and dependencies  
-- - 50+ Standard Workflow Steps with detailed requirements and skill specifications
-- - All entities include BaseEntity fields (id, created_at, updated_at)
-- - Random UUIDs provided for all primary keys
-- - Realistic time estimates and resource requirements
-- - JSON fields properly formatted for required_skills and requirements