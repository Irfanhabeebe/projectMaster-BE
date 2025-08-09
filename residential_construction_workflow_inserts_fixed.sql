-- INSERT statements for Workflow Templates, Stages, and Steps
-- For Residential Construction Companies
-- Fixed version with proper UUIDs and table names

-- First, let's create a sample company to reference
INSERT INTO companies (
    id, name, address, phone, email, active, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'Sample Construction Company',
    '123 Main St, Construction City, CC 12345',
    '+1-555-0123',
    'info@sampleconstruction.com',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- WORKFLOW TEMPLATES
-- =====================================================

-- Single Family Home Construction Template
INSERT INTO workflow_templates (
    id, company_id, name, description, category, active, is_default, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440000',
    'Single Family Home Construction',
    'Complete workflow for building a single-family residential home from permits to final inspection',
    'New Construction',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Home Renovation Template
INSERT INTO workflow_templates (
    id, company_id, name, description, category, active, is_default, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440002',
    '550e8400-e29b-41d4-a716-446655440000',
    'Home Renovation',
    'Workflow for major home renovation projects including kitchen, bathroom, and structural modifications',
    'Renovation',
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Custom Home Construction Template
INSERT INTO workflow_templates (
    id, company_id, name, description, category, active, is_default, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440003',
    '550e8400-e29b-41d4-a716-446655440000',
    'Custom Home Construction',
    'Premium workflow for custom-designed luxury homes with specialized requirements',
    'Custom Build',
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Renovation Template
INSERT INTO workflow_templates (
    id, company_id, name, description, category, active, is_default, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440004',
    '550e8400-e29b-41d4-a716-446655440000',
    'Kitchen Renovation',
    'Specialized workflow for kitchen renovation projects',
    'Kitchen Remodel',
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- WORKFLOW STAGES - Single Family Home Construction
-- =====================================================

-- Pre-Construction Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440010',
    '550e8400-e29b-41d4-a716-446655440001',
    'Pre-Construction',
    'Planning, permits, and site preparation activities',
    1,
    false,
    2,
    30,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Foundation Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440011',
    '550e8400-e29b-41d4-a716-446655440001',
    'Foundation',
    'Excavation, footings, and foundation construction',
    2,
    false,
    1,
    14,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Framing Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440012',
    '550e8400-e29b-41d4-a716-446655440001',
    'Framing',
    'Structural framing, roof structure, and exterior sheathing',
    3,
    false,
    1,
    21,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- MEP Rough-In Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440013',
    '550e8400-e29b-41d4-a716-446655440001',
    'MEP Rough-In',
    'Mechanical, Electrical, and Plumbing rough-in work',
    4,
    true,
    1,
    18,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insulation & Drywall Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440014',
    '550e8400-e29b-41d4-a716-446655440001',
    'Insulation & Drywall',
    'Insulation installation, drywall hanging, taping, and finishing',
    5,
    false,
    0,
    16,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Interior Finishes Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440015',
    '550e8400-e29b-41d4-a716-446655440001',
    'Interior Finishes',
    'Flooring, painting, trim work, and interior fixtures',
    6,
    true,
    0,
    25,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Final Completion Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440016',
    '550e8400-e29b-41d4-a716-446655440001',
    'Final Completion',
    'Final inspections, cleanup, and project handover',
    7,
    false,
    2,
    7,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- WORKFLOW STEPS - Pre-Construction Stage
-- =====================================================

-- Site Survey
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440100',
    '550e8400-e29b-41d4-a716-446655440010',
    'Site Survey',
    'Professional land survey to establish property boundaries and elevations',
    1,
    8,
    '["Surveying", "GPS Equipment", "CAD Software"]',
    '["Survey equipment", "Property deed", "Boundary markers"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Soil Testing
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440101',
    '550e8400-e29b-41d4-a716-446655440010',
    'Soil Testing',
    'Geotechnical analysis to determine soil bearing capacity and foundation requirements',
    2,
    16,
    '["Geotechnical Engineering", "Soil Analysis", "Foundation Design"]',
    '["Soil samples", "Testing equipment", "Engineering analysis"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Building Permit Application
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440102',
    '550e8400-e29b-41d4-a716-446655440010',
    'Building Permit Application',
    'Submit building permit application with architectural plans and engineering documents',
    3,
    12,
    '["Permit Processing", "Code Compliance", "Documentation"]',
    '["Architectural plans", "Engineering drawings", "Site plan", "Application forms"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Utility Connections Planning
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440103',
    '550e8400-e29b-41d4-a716-446655440010',
    'Utility Connections Planning',
    'Coordinate with utility companies for water, sewer, gas, and electrical connections',
    4,
    20,
    '["Utility Coordination", "Project Management", "Communication"]',
    '["Utility company contacts", "Connection requirements", "Service locations"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Site Preparation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440104',
    '550e8400-e29b-41d4-a716-446655440010',
    'Site Preparation',
    'Clear vegetation, level site, and establish construction access',
    5,
    32,
    '["Heavy Equipment Operation", "Site Management", "Safety Protocols"]',
    '["Excavator", "Bulldozer", "Safety equipment", "Temporary fencing"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- WORKFLOW STEPS - Foundation Stage
-- =====================================================

-- Excavation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440110',
    '550e8400-e29b-41d4-a716-446655440011',
    'Excavation',
    'Excavate foundation area to required depth and dimensions',
    1,
    24,
    '["Heavy Equipment Operation", "Grading", "Safety Management"]',
    '["Excavator", "Laser level", "Foundation plans", "Safety barriers"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Footings Installation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440111',
    '550e8400-e29b-41d4-a716-446655440011',
    'Footings Installation',
    'Install reinforced concrete footings according to structural plans',
    2,
    40,
    '["Concrete Work", "Rebar Installation", "Form Construction"]',
    '["Concrete", "Rebar", "Forms", "Concrete mixer", "Vibrator"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Foundation Walls
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440112',
    '550e8400-e29b-41d4-a716-446655440011',
    'Foundation Walls',
    'Pour concrete foundation walls or install concrete block foundation',
    3,
    48,
    '["Concrete Work", "Masonry", "Waterproofing"]',
    '["Concrete blocks or forms", "Mortar", "Waterproofing membrane", "Drainage system"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Foundation Inspection
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440113',
    '550e8400-e29b-41d4-a716-446655440011',
    'Foundation Inspection',
    'Municipal inspection of completed foundation work',
    4,
    4,
    '["Code Compliance", "Quality Control", "Documentation"]',
    '["Inspection request", "Foundation plans", "Compliance documentation"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- WORKFLOW STEPS - Framing Stage
-- =====================================================

-- Floor Framing
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440120',
    '550e8400-e29b-41d4-a716-446655440012',
    'Floor Framing',
    'Install floor joists, subfloor, and structural floor systems',
    1,
    56,
    '["Carpentry", "Structural Framing", "Blueprint Reading"]',
    '["Lumber", "Joist hangers", "Subfloor panels", "Framing tools", "Level"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Wall Framing
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440121',
    '550e8400-e29b-41d4-a716-446655440012',
    'Wall Framing',
    'Frame exterior and interior walls with proper headers and openings',
    2,
    72,
    '["Carpentry", "Structural Framing", "Window/Door Installation"]',
    '["Framing lumber", "Headers", "Nails", "Framing square", "Circular saw"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Roof Framing
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440122',
    '550e8400-e29b-41d4-a716-446655440012',
    'Roof Framing',
    'Install roof trusses or rafters and roof sheathing',
    3,
    64,
    '["Carpentry", "Roof Construction", "Safety Procedures"]',
    '["Roof trusses/rafters", "Roof sheathing", "Ridge beam", "Safety harness", "Crane"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Framing Inspection
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440123',
    '550e8400-e29b-41d4-a716-446655440012',
    'Framing Inspection',
    'Municipal inspection of completed framing work',
    4,
    4,
    '["Code Compliance", "Quality Control", "Documentation"]',
    '["Inspection request", "Framing plans", "Compliance checklist"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- WORKFLOW STEPS - MEP Rough-In Stage
-- =====================================================

-- Electrical Rough-In
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440130',
    '550e8400-e29b-41d4-a716-446655440013',
    'Electrical Rough-In',
    'Install electrical wiring, outlets, switches, and panel connections',
    1,
    48,
    '["Electrical Work", "Code Compliance", "Safety Procedures"]',
    '["Electrical wire", "Outlets", "Switches", "Electrical panel", "Conduit"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Plumbing Rough-In
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440131',
    '550e8400-e29b-41d4-a716-446655440013',
    'Plumbing Rough-In',
    'Install water supply lines, drain lines, and vent systems',
    2,
    40,
    '["Plumbing", "Pipe Installation", "Code Compliance"]',
    '["PVC/Copper pipes", "Fittings", "Pipe cutter", "Soldering equipment"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- HVAC Rough-In
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440132',
    '550e8400-e29b-41d4-a716-446655440013',
    'HVAC Rough-In',
    'Install ductwork, HVAC equipment, and ventilation systems',
    3,
    56,
    '["HVAC Installation", "Ductwork", "System Design"]',
    '["Ductwork", "HVAC unit", "Vents", "Insulation", "Sheet metal tools"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- MEP Inspection
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440133',
    '550e8400-e29b-41d4-a716-446655440013',
    'MEP Inspection',
    'Municipal inspection of mechanical, electrical, and plumbing rough-in work',
    4,
    6,
    '["Code Compliance", "Quality Control", "Multi-trade Coordination"]',
    '["Inspection requests", "MEP plans", "Test certificates"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- WORKFLOW STEPS - Insulation & Drywall Stage
-- =====================================================

-- Insulation Installation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440140',
    '550e8400-e29b-41d4-a716-446655440014',
    'Insulation Installation',
    'Install thermal and acoustic insulation in walls, ceilings, and floors',
    1,
    32,
    '["Insulation Installation", "Energy Efficiency", "Safety Procedures"]',
    '["Fiberglass/Foam insulation", "Vapor barrier", "Staple gun", "Protective gear"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Drywall Hanging
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440141',
    '550e8400-e29b-41d4-a716-446655440014',
    'Drywall Hanging',
    'Hang drywall sheets on walls and ceilings with proper fastening',
    2,
    48,
    '["Drywall Installation", "Measuring", "Cutting"]',
    '["Drywall sheets", "Screws", "Screw gun", "Drywall saw", "Lift"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Drywall Finishing
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440142',
    '550e8400-e29b-41d4-a716-446655440014',
    'Drywall Finishing',
    'Apply tape, joint compound, sand, and prime drywall surfaces',
    3,
    40,
    '["Drywall Finishing", "Taping", "Sanding", "Priming"]',
    '["Joint compound", "Drywall tape", "Taping knives", "Sandpaper", "Primer"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- WORKFLOW STEPS - Interior Finishes Stage
-- =====================================================

-- Flooring Installation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440150',
    '550e8400-e29b-41d4-a716-446655440015',
    'Flooring Installation',
    'Install hardwood, tile, carpet, or other finish flooring materials',
    1,
    64,
    '["Flooring Installation", "Measuring", "Pattern Layout"]',
    '["Flooring materials", "Underlayment", "Adhesive", "Installation tools"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Interior Painting
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440151',
    '550e8400-e29b-41d4-a716-446655440015',
    'Interior Painting',
    'Prime and paint all interior walls, ceilings, and trim',
    2,
    56,
    '["Painting", "Color Matching", "Surface Preparation"]',
    '["Paint", "Primer", "Brushes", "Rollers", "Drop cloths", "Ladder"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Cabinet Installation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440152',
    '550e8400-e29b-41d4-a716-446655440015',
    'Cabinet Installation',
    'Install kitchen and bathroom cabinets with proper alignment and hardware',
    3,
    40,
    '["Cabinet Installation", "Hardware Installation", "Alignment"]',
    '["Cabinets", "Hardware", "Shims", "Level", "Drill", "Screws"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Fixture Installation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440153',
    '550e8400-e29b-41d4-a716-446655440015',
    'Fixture Installation',
    'Install light fixtures, plumbing fixtures, and electrical outlets',
    4,
    32,
    '["Electrical Work", "Plumbing", "Fixture Installation"]',
    '["Light fixtures", "Plumbing fixtures", "Electrical tools", "Plumbing tools"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- WORKFLOW STEPS - Final Completion Stage
-- =====================================================

-- Final Electrical
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440160',
    '550e8400-e29b-41d4-a716-446655440016',
    'Final Electrical',
    'Complete electrical connections, install cover plates, and test all circuits',
    1,
    16,
    '["Electrical Work", "Testing", "Code Compliance"]',
    '["Cover plates", "Circuit tester", "Electrical tools"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Final Plumbing
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440161',
    '550e8400-e29b-41d4-a716-446655440016',
    'Final Plumbing',
    'Connect fixtures, test water pressure, and verify proper drainage',
    2,
    12,
    '["Plumbing", "Testing", "Fixture Connection"]',
    '["Plumbing tools", "Pressure gauge", "Test equipment"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Final Inspection
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440162',
    '550e8400-e29b-41d4-a716-446655440016',
    'Final Inspection',
    'Municipal final inspection and certificate of occupancy',
    3,
    4,
    '["Code Compliance", "Documentation", "Quality Control"]',
    '["Inspection request", "All permits", "Compliance certificates"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Cleanup & Handover
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440163',
    '550e8400-e29b-41d4-a716-446655440016',
    'Cleanup & Handover',
    'Final cleanup, walkthrough with client, and project handover',
    4,
    16,
    '["Project Management", "Customer Service", "Quality Control"]',
    '["Cleaning supplies", "Project documentation", "Keys", "Warranties"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- KITCHEN RENOVATION WORKFLOW STAGES
-- =====================================================

-- Kitchen Planning Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440020',
    '550e8400-e29b-41d4-a716-446655440004',
    'Kitchen Planning',
    'Design layout, select materials, and obtain permits',
    1,
    false,
    1,
    14,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Demolition Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440021',
    '550e8400-e29b-41d4-a716-446655440004',
    'Kitchen Demolition',
    'Remove existing cabinets, appliances, and finishes',
    2,
    false,
    0,
    3,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Infrastructure Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440022',
    '550e8400-e29b-41d4-a716-446655440004',
    'Kitchen Infrastructure',
    'Update electrical, plumbing, and HVAC systems',
    3,
    true,
    1,
    7,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Installation Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440023',
    '550e8400-e29b-41d4-a716-446655440004',
    'Kitchen Installation',
    'Install cabinets, countertops, and appliances',
    4,
    false,
    0,
    10,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Finishing Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440024',
    '550e8400-e29b-41d4-a716-446655440004',
    'Kitchen Finishing',
    'Final touches, painting, and cleanup',
    5,
    false,
    0,
    5,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- KITCHEN RENOVATION WORKFLOW STEPS
-- =====================================================

-- Kitchen Design Consultation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440200',
    '550e8400-e29b-41d4-a716-446655440020',
    'Kitchen Design Consultation',
    'Meet with client to plan kitchen layout and select materials',
    1,
    6,
    '["Kitchen Design", "Space Planning", "Material Selection"]',
    '["Design software", "Material samples", "Measuring tools"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Permit Application
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440201',
    '550e8400-e29b-41d4-a716-446655440020',
    'Kitchen Permit Application',
    'Submit renovation permits for kitchen modifications',
    2,
    8,
    '["Permit Processing", "Code Knowledge", "Documentation"]',
    '["Renovation plans", "Permit applications", "Code compliance checklist"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Cabinet Removal
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440210',
    '550e8400-e29b-41d4-a716-446655440021',
    'Cabinet Removal',
    'Carefully remove existing kitchen cabinets and countertops',
    1,
    8,
    '["Demolition", "Tool Operation", "Safety Procedures"]',
    '["Hand tools", "Power tools", "Safety equipment", "Disposal containers"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Appliance Disconnection
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440211',
    '550e8400-e29b-41d4-a716-446655440021',
    'Appliance Disconnection',
    'Safely disconnect and remove existing kitchen appliances',
    2,
    4,
    '["Electrical Work", "Plumbing", "Appliance Handling"]',
    '["Electrical tools", "Plumbing tools", "Appliance dolly"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Electrical Update
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440220',
    '550e8400-e29b-41d4-a716-446655440022',
    'Kitchen Electrical Update',
    'Update electrical wiring for new kitchen layout and appliances',
    1,
    16,
    '["Electrical Work", "Code Compliance", "Kitchen Systems"]',
    '["Electrical wire", "Outlets", "GFCI breakers", "Electrical tools"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Plumbing Update
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440221',
    '550e8400-e29b-41d4-a716-446655440022',
    'Kitchen Plumbing Update',
    'Relocate or update plumbing for sink and dishwasher',
    2,
    12,
    '["Plumbing", "Pipe Installation", "Kitchen Systems"]',
    '["Pipes", "Fittings", "Shut-off valves", "Plumbing tools"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- New Cabinet Installation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440230',
    '550e8400-e29b-41d4-a716-446655440023',
    'New Cabinet Installation',
    'Install new kitchen cabinets with proper alignment and hardware',
    1,
    24,
    '["Cabinet Installation", "Hardware Installation", "Precision Measuring"]',
    '["Kitchen cabinets", "Hardware", "Shims", "Level", "Drill"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Countertop Installation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440231',
    '550e8400-e29b-41d4-a716-446655440023',
    'Countertop Installation',
    'Template, fabricate, and install kitchen countertops',
    2,
    16,
    '["Countertop Installation", "Template Making", "Stone/Laminate Work"]',
    '["Countertop material", "Template material", "Adhesive", "Support brackets"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Appliance Installation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440232',
    '550e8400-e29b-41d4-a716-446655440023',
    'Kitchen Appliance Installation',
    'Install and connect new kitchen appliances',
    3,
    12,
    '["Appliance Installation", "Electrical Connection", "Plumbing Connection"]',
    '["New appliances", "Connection kits", "Installation hardware"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Final Touches
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440240',
    '550e8400-e29b-41d4-a716-446655440024',
    'Kitchen Final Touches',
    'Install backsplash, trim, and final hardware',
    1,
    16,
    '["Tile Installation", "Finish Carpentry", "Detail Work"]',
    '["Backsplash tile", "Trim materials", "Grout", "Caulk", "Hardware"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Kitchen Final Cleanup
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440241',
    '550e8400-e29b-41d4-a716-446655440024',
    'Kitchen Final Cleanup',
    'Deep clean kitchen and prepare for client handover',
    2,
    8,
    '["Cleaning", "Quality Control", "Customer Service"]',
    '["Cleaning supplies", "Protective coverings", "Touch-up materials"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- HOME RENOVATION WORKFLOW STAGES
-- =====================================================

-- Renovation Planning Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440030',
    '550e8400-e29b-41d4-a716-446655440002',
    'Renovation Planning',
    'Design development, permits, and renovation planning',
    1,
    false,
    1,
    21,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Renovation Demolition Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440031',
    '550e8400-e29b-41d4-a716-446655440002',
    'Renovation Demolition',
    'Selective demolition and debris removal',
    2,
    false,
    0,
    7,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Structural Modifications Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440032',
    '550e8400-e29b-41d4-a716-446655440002',
    'Structural Modifications',
    'Structural changes, additions, and reinforcements',
    3,
    false,
    1,
    14,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Systems Upgrade Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440033',
    '550e8400-e29b-41d4-a716-446655440002',
    'Systems Upgrade',
    'Electrical, plumbing, and HVAC system upgrades',
    4,
    true,
    1,
    12,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Renovation Finishes Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440034',
    '550e8400-e29b-41d4-a716-446655440002',
    'Renovation Finishes',
    'New finishes, fixtures, and final details',
    5,
    true,
    0,
    18,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- CUSTOM HOME CONSTRUCTION WORKFLOW STAGES
-- =====================================================

-- Custom Design Development Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440040',
    '550e8400-e29b-41d4-a716-446655440003',
    'Custom Design Development',
    'Custom architectural design and engineering',
    1,
    false,
    3,
    45,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Premium Foundation Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440041',
    '550e8400-e29b-41d4-a716-446655440003',
    'Premium Foundation',
    'High-end foundation with specialized features',
    2,
    false,
    1,
    21,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Custom Framing Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440042',
    '550e8400-e29b-41d4-a716-446655440003',
    'Custom Framing',
    'Complex framing with custom architectural features',
    3,
    false,
    1,
    28,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Premium Systems Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440043',
    '550e8400-e29b-41d4-a716-446655440003',
    'Premium Systems',
    'High-end MEP systems with smart home integration',
    4,
    true,
    1,
    25,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Luxury Finishes Stage
INSERT INTO workflow_stages (
    id, workflow_template_id, name, description, order_index, parallel_execution, 
    required_approvals, estimated_duration_days, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440044',
    '550e8400-e29b-41d4-a716-446655440003',
    'Luxury Finishes',
    'Premium materials and custom millwork installation',
    5,
    true,
    0,
    35,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- SAMPLE RENOVATION WORKFLOW STEPS
-- =====================================================

-- Renovation Design Consultation
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440300',
    '550e8400-e29b-41d4-a716-446655440030',
    'Renovation Design Consultation',
    'Meet with client to discuss renovation goals and preferences',
    1,
    8,
    '["Design Consultation", "Customer Service", "Space Planning"]',
    '["Design portfolio", "Measuring tools", "Client questionnaire"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Renovation Permit Application
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440301',
    '550e8400-e29b-41d4-a716-446655440030',
    'Renovation Permit Application',
    'Submit renovation permits and obtain approvals',
    2,
    12,
    '["Permit Processing", "Code Knowledge", "Documentation"]',
    '["Renovation plans", "Permit applications", "Code compliance checklist"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Selective Demolition
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440310',
    '550e8400-e29b-41d4-a716-446655440031',
    'Selective Demolition',
    'Carefully remove existing materials while preserving structural elements',
    1,
    24,
    '["Demolition", "Safety Procedures", "Structural Knowledge"]',
    '["Demolition tools", "Safety equipment", "Dumpster", "Dust barriers"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Debris Removal
INSERT INTO workflow_steps (
    id, workflow_stage_id, name, description, order_index, estimated_hours, 
    required_skills, requirements, created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440311',
    '550e8400-e29b-41d4-a716-446655440031',
    'Debris Removal',
    'Clean up and dispose of demolition debris properly',
    2,
    16,
    '["Waste Management", "Safety Procedures", "Environmental Compliance"]',
    '["Dumpster", "Cleaning tools", "Disposal permits"]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- =====================================================
-- END OF INSERT STATEMENTS
-- =====================================================

-- Summary:
-- - 4 Workflow Templates covering various residential construction types
-- - 20+ Workflow Stages with proper sequencing and dependencies  
-- - 40+ Workflow Steps with detailed requirements and skill specifications
-- - All entities include proper BaseEntity fields (id, created_at, updated_at)
-- - Valid UUIDs provided for all primary keys
-- - Realistic time estimates and resource requirements
-- - Proper foreign key references between templates, stages, and steps
-- - JSON fields properly formatted for required_skills and requirements
-- - Sample company record included for reference