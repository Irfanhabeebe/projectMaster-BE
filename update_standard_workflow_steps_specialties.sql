-- Update specialty_id in standard_workflow_steps table
-- This script maps each workflow step to the appropriate specialty based on name/description

-- Site Preparation Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Land Surveying')
WHERE name = 'Site Boundary Survey';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Land Surveying')
WHERE name = 'Building Footprint Marking';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Excavation & Earthmoving')
WHERE name = 'Vegetation Removal';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Site Grading & Drainage')
WHERE name = 'Site Grading';

-- Concrete & Foundations Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Excavation & Earthmoving')
WHERE name = 'Foundation Trenches';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Soil Compaction')
WHERE name = 'Soil Compaction';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Formwork & Reinforcement')
WHERE name = 'Reinforcement Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Concrete Pouring & Finishing')
WHERE name = 'Concrete Pouring';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Drainage System Installation')
WHERE name = 'Stormwater Pipes';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Drainage System Installation')
WHERE name = 'Drainage Testing';

-- Structural Carpentry Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Timber Framing (Joists, Bearers, Studs)')
WHERE name = 'Floor Joists';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Timber Framing (Joists, Bearers, Studs)')
WHERE name = 'Subfloor Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Timber Framing (Joists, Bearers, Studs)')
WHERE name = 'Wall Studs';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Wall Bracing & Structural Reinforcement')
WHERE name = 'Wall Bracing';

-- Roofing Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Roof Truss Installation')
WHERE name = 'Roof Trusses';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Wall Bracing & Structural Reinforcement')
WHERE name = 'Roof Bracing';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Roof Sheeting & Tiling')
WHERE name = 'Roof Underlay';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Roof Sheeting & Tiling')
WHERE name = 'Roof Tiles';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Ridge Capping & Flashings')
WHERE name = 'Ridge Capping';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Fascia & Guttering Installation')
WHERE name = 'Fascia Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Fascia & Guttering Installation')
WHERE name = 'Gutter Installation';

-- Masonry & Cladding Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Bricklaying & Blocklaying')
WHERE name = 'Brick Delivery';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Bricklaying & Blocklaying')
WHERE name = 'Foundation Bricks';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Bricklaying & Blocklaying')
WHERE name = 'Wall Bricklaying';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Bricklaying & Blocklaying')
WHERE name = 'Lintel Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Mortar Pointing & Brick Cleaning')
WHERE name = 'Pointing & Cleaning';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Weatherboard/Cladding Installation')
WHERE name = 'Weatherboard Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Sealant & Waterproofing')
WHERE name = 'Flashings';

-- Windows, Doors & External Finishes Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Painting & Coating (Exterior)')
WHERE name = 'Paint Preparation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Window Installation')
WHERE name = 'Window Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Door Installation')
WHERE name = 'Door Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Sealant & Waterproofing')
WHERE name = 'Sealants & Flashings';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Painting & Coating (Exterior)')
WHERE name = 'Surface Preparation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Painting & Coating (Exterior)')
WHERE name = 'Primer Application';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Painting & Coating (Exterior)')
WHERE name = 'Top Coat Application';

-- Additional Concrete & Foundations Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Formwork & Reinforcement')
WHERE name = 'Excavation & Formwork';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Concrete Pouring & Finishing')
WHERE name = 'Concrete Pouring' AND description = 'Pour and finish concrete';

-- Plumbing & Drainage Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Water Supply Installation')
WHERE name = 'Water Supply Pipes';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Drainage & Waste Systems')
WHERE name = 'Drainage Pipes';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Ventilation & Sewer Vents')
WHERE name = 'Ventilation Pipes';

-- Electrical Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Main Switchboard Installation')
WHERE name = 'Main Electrical Board';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Electrical Rough-in (Wiring, Conduits)')
WHERE name = 'Electrical Wiring';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Electrical Fit-off (Power Points, Switches)')
WHERE name = 'Power Points & Switches';

-- HVAC & Mechanical Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Ductwork Installation')
WHERE name = 'Ductwork Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Heating & Cooling Unit Installation')
WHERE name = 'HVAC Unit Installation';

-- Internal Works Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Ceiling Installation (Plasterboard)')
WHERE name = 'Ceiling Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Wall Lining (Plasterboard)')
WHERE name = 'Wall Lining';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Jointing & Finishing (Plastering)')
WHERE name = 'Jointing & Finishing';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Flooring Installation (Tiles, Timber, Carpet, Vinyl)')
WHERE name = 'Floor Covering';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Internal Painting & Finishing')
WHERE name = 'Internal Painting';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Skirting & Architraves')
WHERE name = 'Skirting & Architraves';

-- Joinery & Fixtures Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Kitchen Cabinet Installation')
WHERE name = 'Kitchen Cabinets';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Kitchen Benchtop Installation')
WHERE name = 'Kitchen Benchtop';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Appliance Installation')
WHERE name = 'Kitchen Appliances';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Bathroom Tiling')
WHERE name = 'Bathroom Tiling';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Bathroom Fixtures & Accessories')
WHERE name = 'Bathroom Fixtures';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Bathroom Fixtures & Accessories')
WHERE name = 'Bathroom Accessories';

-- Electrical & Plumbing Final Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Lighting Fixtures Installation')
WHERE name = 'Light Fixtures';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Electrical Testing & Compliance')
WHERE name = 'Final Testing' AND description = 'Test all electrical systems';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Plumbing Fixtures Installation')
WHERE name = 'Tap Installation';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Plumbing Fixtures Installation')
WHERE name = 'Final Testing' AND description = 'Test all plumbing systems';

-- Final Stages Steps
UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Final Building Inspection & Certification')
WHERE name = 'Final Inspection';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Site Cleaning & Waste Removal')
WHERE name = 'Site Cleanup';

UPDATE standard_workflow_steps 
SET specialty_id = (SELECT id FROM specialties WHERE specialty_name = 'Client Handover & Documentation')
WHERE name = 'Client Handover';

-- Verify the updates
SELECT 
    sws.name as step_name,
    s.specialty_type,
    s.specialty_name,
    sws.specialty_id
FROM standard_workflow_steps sws
LEFT JOIN specialties s ON sws.specialty_id = s.id
ORDER BY sws.name;

-- Check for any steps without specialties
SELECT 
    name,
    description
FROM standard_workflow_steps 
WHERE specialty_id IS NULL
ORDER BY name;
