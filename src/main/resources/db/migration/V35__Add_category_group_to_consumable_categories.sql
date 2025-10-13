-- Migration V35: Add category_group to consumable_categories
-- This migration adds grouping capability to organize categories by construction phase

-- Add category_group column
ALTER TABLE consumable_categories ADD COLUMN IF NOT EXISTS category_group VARCHAR(100);

-- Add index for filtering by group
CREATE INDEX IF NOT EXISTS idx_consumable_categories_category_group ON consumable_categories (category_group);

-- Add comment
COMMENT ON COLUMN consumable_categories.category_group IS 'Logical grouping of categories by construction phase (e.g., Foundation & Structure, Plumbing, Electrical)';

-- Update existing categories with their groups based on display_order ranges

-- Foundation & Structure (Categories 1-4)
UPDATE consumable_categories SET category_group = 'Foundation & Structure'
WHERE name IN ('Concrete & Cement', 'Steel Reinforcement', 'Formwork Materials', 'Waterproofing Materials');

-- Framing & Structure (Categories 5-8)
UPDATE consumable_categories SET category_group = 'Framing & Structure'
WHERE name IN ('Timber Framing', 'Metal Framing', 'Roof Trusses', 'Wall & Ceiling Linings');

-- Roofing (Categories 9-12)
UPDATE consumable_categories SET category_group = 'Roofing'
WHERE name IN ('Roofing Materials', 'Gutters & Downpipes', 'Roof Insulation', 'Roof Accessories');

-- Exterior (Categories 13-16)
UPDATE consumable_categories SET category_group = 'Exterior'
WHERE name IN ('External Cladding', 'External Finishes', 'Windows & External Doors', 'External Hardware');

-- Plumbing (Categories 17-21)
UPDATE consumable_categories SET category_group = 'Plumbing'
WHERE name IN ('Plumbing Pipes & Fittings', 'Bathroom Fixtures', 'Kitchen Fixtures', 'Hot Water Systems', 'Plumbing Accessories');

-- Electrical (Categories 22-26)
UPDATE consumable_categories SET category_group = 'Electrical'
WHERE name IN ('Electrical Cables & Wires', 'Electrical Fittings', 'Lighting Fixtures', 'Electrical Accessories', 'Home Automation');

-- Flooring (Categories 27-29)
UPDATE consumable_categories SET category_group = 'Flooring'
WHERE name IN ('Flooring Materials', 'Floor Preparation', 'Floor Finishes');

-- Interior (Categories 30-34)
UPDATE consumable_categories SET category_group = 'Interior'
WHERE name IN ('Internal Doors', 'Interior Finishes', 'Kitchen Cabinetry', 'Bathroom Vanities', 'Wardrobes & Storage');

-- Insulation & Energy (Categories 35-38)
UPDATE consumable_categories SET category_group = 'Insulation & Energy'
WHERE name IN ('Wall Insulation', 'Floor Insulation', 'Solar Systems', 'Energy Efficiency');

-- Hardware & Fasteners (Categories 39-41)
UPDATE consumable_categories SET category_group = 'Hardware & Fasteners'
WHERE name IN ('Structural Fasteners', 'General Hardware', 'Adhesives & Sealants');

-- Site & Safety (Categories 42-45)
UPDATE consumable_categories SET category_group = 'Site & Safety'
WHERE name IN ('Site Preparation', 'Drainage & Stormwater', 'Landscaping Materials', 'Safety Equipment');

-- Appliances & Fixtures (Categories 46-48)
UPDATE consumable_categories SET category_group = 'Appliances & Fixtures'
WHERE name IN ('Kitchen Appliances', 'Laundry Fixtures', 'HVAC Systems');

-- Finishing Touches (Categories 49-51)
UPDATE consumable_categories SET category_group = 'Finishing Touches'
WHERE name IN ('Decorative Elements', 'Security Systems', 'Outdoor Living');

-- Specialized (Categories 52-55)
UPDATE consumable_categories SET category_group = 'Specialized'
WHERE name IN ('Fire Safety', 'Accessibility Features', 'Pool & Spa', 'Garage & Shed');

-- Also update the default categories from V33 migration
UPDATE consumable_categories SET category_group = 'Plumbing' WHERE name = 'Bathroom Fittings';
UPDATE consumable_categories SET category_group = 'Electrical' WHERE name = 'Electrical Components';
UPDATE consumable_categories SET category_group = 'Plumbing' WHERE name = 'Plumbing Materials';
UPDATE consumable_categories SET category_group = 'Flooring' WHERE name = 'Flooring Materials';
UPDATE consumable_categories SET category_group = 'Appliances & Fixtures' WHERE name = 'Kitchen Appliances';
UPDATE consumable_categories SET category_group = 'Interior' WHERE name = 'Paint and Finishes';
UPDATE consumable_categories SET category_group = 'Hardware & Fasteners' WHERE name = 'Hardware and Fasteners';
UPDATE consumable_categories SET category_group = 'Insulation & Energy' WHERE name = 'Insulation Materials';
UPDATE consumable_categories SET category_group = 'Roofing' WHERE name = 'Roofing Materials';
UPDATE consumable_categories SET category_group = 'Exterior' WHERE name = 'Windows and Doors';

-- Verify the grouping
SELECT 
    category_group,
    COUNT(*) as category_count,
    STRING_AGG(name, ', ' ORDER BY display_order) as categories
FROM consumable_categories
WHERE active = true
GROUP BY category_group
ORDER BY MIN(display_order);

