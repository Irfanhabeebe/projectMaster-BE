-- Insert Construction Specialties
-- This script can be run manually to populate the specialties table

-- First, enable the uuid-ossp extension if it doesn't exist
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- First, create the specialties table if it doesn't exist
CREATE TABLE IF NOT EXISTS specialties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    specialty_type VARCHAR(100) NOT NULL,
    specialty_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT true,
    order_index INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Clear existing data (optional - comment out if you want to keep existing data)
-- DELETE FROM specialties;

-- Insert the master list of construction specialties
INSERT INTO specialties (specialty_type, specialty_name, description, order_index) VALUES
-- Site Preparation
('Site Preparation', 'Land Surveying', 'Conduct detailed site survey and measurements', 1),
('Site Preparation', 'Excavation & Earthmoving', 'Excavate and move earth for foundation preparation', 2),
('Site Preparation', 'Site Grading & Drainage', 'Grade site and install drainage systems', 3),
('Site Preparation', 'Soil Compaction', 'Compact soil to required density for foundation', 4),

-- Concrete & Foundations
('Concrete & Foundations', 'Formwork & Reinforcement', 'Install formwork and steel reinforcement', 5),
('Concrete & Foundations', 'Concrete Pouring & Finishing', 'Pour and finish concrete structures', 6),
('Concrete & Foundations', 'Foundation Construction', 'Build foundation structures', 7),
('Concrete & Foundations', 'Drainage System Installation', 'Install foundation drainage systems', 8),

-- Structural Carpentry
('Structural Carpentry', 'Timber Framing (Joists, Bearers, Studs)', 'Install structural timber framing', 9),
('Structural Carpentry', 'Roof Truss Installation', 'Install roof trusses and structural elements', 10),
('Structural Carpentry', 'Wall Bracing & Structural Reinforcement', 'Install wall bracing and reinforcement', 11),

-- Roofing
('Roofing', 'Roof Carpentry', 'Install roof carpentry elements', 12),
('Roofing', 'Roof Sheeting & Tiling', 'Install roof covering materials', 13),
('Roofing', 'Ridge Capping & Flashings', 'Install ridge caps and flashings', 14),
('Roofing', 'Fascia & Guttering Installation', 'Install fascia boards and gutters', 15),

-- Masonry & Cladding
('Masonry & Cladding', 'Bricklaying & Blocklaying', 'Lay bricks and blocks for walls', 16),
('Masonry & Cladding', 'Mortar Pointing & Brick Cleaning', 'Point mortar joints and clean bricks', 17),
('Masonry & Cladding', 'Stone Masonry (if used)', 'Install stone masonry elements', 18),
('Masonry & Cladding', 'Weatherboard/Cladding Installation', 'Install external cladding materials', 19),

-- Windows, Doors & External Finishes
('Windows, Doors & External Finishes', 'Window Installation', 'Install windows and frames', 20),
('Windows, Doors & External Finishes', 'Door Installation', 'Install doors and frames', 21),
('Windows, Doors & External Finishes', 'Sealant & Waterproofing', 'Apply sealants and waterproofing', 22),
('Windows, Doors & External Finishes', 'Painting & Coating (Exterior)', 'Apply exterior paint and coatings', 23),

-- Plumbing & Drainage
('Plumbing & Drainage', 'Stormwater Plumbing', 'Install stormwater drainage systems', 24),
('Plumbing & Drainage', 'Water Supply Installation', 'Install water supply systems', 25),
('Plumbing & Drainage', 'Drainage & Waste Systems', 'Install waste and drainage systems', 26),
('Plumbing & Drainage', 'Ventilation & Sewer Vents', 'Install ventilation and sewer vent systems', 27),
('Plumbing & Drainage', 'Plumbing Fixtures Installation', 'Install plumbing fixtures and appliances', 28),

-- Electrical
('Electrical', 'Main Switchboard Installation', 'Install main electrical switchboard', 29),
('Electrical', 'Electrical Rough-in (Wiring, Conduits)', 'Install electrical wiring and conduits', 30),
('Electrical', 'Electrical Fit-off (Power Points, Switches)', 'Install electrical outlets and switches', 31),
('Electrical', 'Lighting Fixtures Installation', 'Install lighting fixtures and systems', 32),
('Electrical', 'Electrical Testing & Compliance', 'Test electrical systems for compliance', 33),

-- HVAC & Mechanical
('HVAC & Mechanical', 'Ductwork Installation', 'Install HVAC ductwork systems', 34),
('HVAC & Mechanical', 'Heating & Cooling Unit Installation', 'Install heating and cooling units', 35),
('HVAC & Mechanical', 'Ventilation Systems', 'Install ventilation systems', 36),

-- Internal Works
('Internal Works', 'Ceiling Installation (Plasterboard)', 'Install ceiling plasterboard', 37),
('Internal Works', 'Wall Lining (Plasterboard)', 'Install wall plasterboard lining', 38),
('Internal Works', 'Jointing & Finishing (Plastering)', 'Apply plastering and finishing', 39),
('Internal Works', 'Internal Painting & Finishing', 'Apply internal paint and finishes', 40),
('Internal Works', 'Flooring Installation (Tiles, Timber, Carpet, Vinyl)', 'Install various flooring materials', 41),
('Internal Works', 'Skirting & Architraves', 'Install skirting boards and architraves', 42),

-- Joinery & Fixtures
('Joinery & Fixtures', 'Kitchen Cabinet Installation', 'Install kitchen cabinets and storage', 43),
('Joinery & Fixtures', 'Kitchen Benchtop Installation', 'Install kitchen benchtops', 44),
('Joinery & Fixtures', 'Appliance Installation', 'Install kitchen and laundry appliances', 45),
('Joinery & Fixtures', 'Bathroom Tiling', 'Install bathroom tiles and finishes', 46),
('Joinery & Fixtures', 'Bathroom Fixtures & Accessories', 'Install bathroom fixtures and accessories', 47),

-- Final Stages
('Final Stages', 'Site Cleaning & Waste Removal', 'Clean site and remove construction waste', 48),
('Final Stages', 'Final Building Inspection & Certification', 'Conduct final inspections and obtain certification', 49),
('Final Stages', 'Client Handover & Documentation', 'Hand over project to client with documentation', 50);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_specialties_type ON specialties(specialty_type);
CREATE INDEX IF NOT EXISTS idx_specialties_active ON specialties(active);
CREATE INDEX IF NOT EXISTS idx_specialties_order ON specialties(order_index);
CREATE UNIQUE INDEX IF NOT EXISTS idx_specialties_name_unique ON specialties(specialty_name);

-- Verify the data was inserted
SELECT specialty_type, COUNT(*) as count 
FROM specialties 
GROUP BY specialty_type 
ORDER BY specialty_type;

-- Show all specialties
SELECT id, specialty_type, specialty_name, order_index 
FROM specialties 
ORDER BY order_index;
