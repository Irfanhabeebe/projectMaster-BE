-- Australian Residential House Construction - Standard Consumable Categories
-- This file contains INSERT statements for consumable categories specific to Australian residential construction
-- Uses PostgreSQL's gen_random_uuid() function for UUID generation

-- Foundation and Structure Categories
INSERT INTO consumable_categories (id, created_at, updated_at, active, category_group, name, description, display_order) VALUES
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Foundation & Structure', 'Concrete & Cement', 'Concrete, cement, aggregates, and related materials for foundations and structural work', 1),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Foundation & Structure', 'Steel Reinforcement', 'Rebar, mesh, steel beams, and structural steel components', 2),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Foundation & Structure', 'Formwork Materials', 'Timber formwork, plywood, form ties, and concrete forming materials', 3),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Foundation & Structure', 'Waterproofing Materials', 'Waterproof membranes, sealants, and damp-proofing materials', 4),

-- Framing and Structure Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Framing & Structure', 'Timber Framing', 'Structural timber, LVL, I-joists, and engineered wood products', 5),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Framing & Structure', 'Metal Framing', 'Steel framing, C-section steel, and metal structural components', 6),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Framing & Structure', 'Roof Trusses', 'Pre-fabricated roof trusses, rafters, and roof framing components', 7),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Framing & Structure', 'Wall & Ceiling Linings', 'Gyprock, plasterboard, VJ boards, and internal wall linings', 8),

-- Roofing Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Roofing', 'Roofing Materials', 'Roof tiles, metal roofing, shingles, and roof covering materials', 9),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Roofing', 'Gutters & Downpipes', 'Gutters, downpipes, flashings, and roof drainage systems', 10),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Roofing', 'Roof Insulation', 'Roof insulation batts, reflective foil, and thermal barriers', 11),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Roofing', 'Roof Accessories', 'Ridge capping, valley gutters, roof vents, and roof hardware', 12),

-- Exterior Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Exterior', 'External Cladding', 'Weatherboard, brick veneer, render, and external wall cladding', 13),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Exterior', 'External Finishes', 'Paint, stains, sealers, and external finishing materials', 14),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Exterior', 'Windows & External Doors', 'Windows, external doors, frames, and glazing materials', 15),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Exterior', 'External Hardware', 'Door handles, locks, hinges, and external door/window hardware', 16),

-- Plumbing Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Plumbing', 'Plumbing Pipes & Fittings', 'Copper pipes, PVC pipes, fittings, and plumbing connections', 17),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Plumbing', 'Bathroom Fixtures', 'Toilets, basins, baths, showers, and bathroom fixtures', 18),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Plumbing', 'Kitchen Fixtures', 'Kitchen sinks, taps, and kitchen plumbing fixtures', 19),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Plumbing', 'Hot Water Systems', 'Hot water units, solar systems, and water heating equipment', 20),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Plumbing', 'Plumbing Accessories', 'Plumbing valves, connectors, and miscellaneous plumbing parts', 21),

-- Electrical Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Electrical', 'Electrical Cables & Wires', 'Power cables, data cables, and electrical wiring', 22),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Electrical', 'Electrical Fittings', 'Power points, light switches, and electrical outlets', 23),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Electrical', 'Lighting Fixtures', 'Light fittings, LED lights, and lighting systems', 24),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Electrical', 'Electrical Accessories', 'Circuit breakers, junction boxes, and electrical hardware', 25),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Electrical', 'Home Automation', 'Smart switches, sensors, and home automation systems', 26),

-- Flooring Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Flooring', 'Flooring Materials', 'Timber flooring, tiles, carpets, and floor covering materials', 27),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Flooring', 'Floor Preparation', 'Underlay, floor leveling compounds, and floor preparation materials', 28),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Flooring', 'Floor Finishes', 'Stains, varnishes, sealers, and floor finishing products', 29),

-- Interior Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Interior', 'Internal Doors', 'Internal doors, door frames, and interior door hardware', 30),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Interior', 'Interior Finishes', 'Internal paint, wallpaper, and interior finishing materials', 31),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Interior', 'Kitchen Cabinetry', 'Kitchen cabinets, benchtops, and kitchen storage solutions', 32),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Interior', 'Bathroom Vanities', 'Bathroom vanities, mirrors, and bathroom storage', 33),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Interior', 'Wardrobes & Storage', 'Built-in wardrobes, storage systems, and custom storage solutions', 34),

-- Insulation & Energy Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Insulation & Energy', 'Wall Insulation', 'Wall insulation batts, cavity insulation, and thermal barriers', 35),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Insulation & Energy', 'Floor Insulation', 'Floor insulation, underfloor insulation, and thermal flooring', 36),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Insulation & Energy', 'Solar Systems', 'Solar panels, inverters, and renewable energy systems', 37),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Insulation & Energy', 'Energy Efficiency', 'Double glazing, thermal breaks, and energy efficiency products', 38),

-- Hardware & Fasteners Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Hardware & Fasteners', 'Structural Fasteners', 'Bolts, screws, nails, and structural connection hardware', 39),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Hardware & Fasteners', 'General Hardware', 'Screws, nails, brackets, and general construction hardware', 40),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Hardware & Fasteners', 'Adhesives & Sealants', 'Construction adhesives, sealants, and bonding materials', 41),

-- Site & Safety Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Site & Safety', 'Site Preparation', 'Site clearing, excavation, and site preparation materials', 42),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Site & Safety', 'Drainage & Stormwater', 'Stormwater pipes, drains, and drainage system components', 43),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Site & Safety', 'Landscaping Materials', 'Garden soil, mulch, plants, and landscaping supplies', 44),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Site & Safety', 'Safety Equipment', 'Safety barriers, signage, and construction safety equipment', 45),

-- Appliances & Fixtures Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Appliances & Fixtures', 'Kitchen Appliances', 'Dishwashers, ovens, cooktops, and kitchen appliances', 46),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Appliances & Fixtures', 'Laundry Fixtures', 'Washing machine connections, laundry tubs, and laundry fixtures', 47),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Appliances & Fixtures', 'HVAC Systems', 'Air conditioning, heating, and ventilation systems', 48),

-- Finishing Touches Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Finishing Touches', 'Decorative Elements', 'Moldings, trim, architraves, and decorative building elements', 49),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Finishing Touches', 'Security Systems', 'Security cameras, alarms, and home security systems', 50),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Finishing Touches', 'Outdoor Living', 'Decking, pergolas, outdoor kitchens, and outdoor living features', 51),

-- Specialized Categories
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Specialized', 'Fire Safety', 'Smoke alarms, fire extinguishers, and fire safety equipment', 52),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Specialized', 'Accessibility Features', 'Handrails, ramps, and accessibility modifications', 53),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Specialized', 'Pool & Spa', 'Pool equipment, spa systems, and water feature components', 54),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true, 'Specialized', 'Garage & Shed', 'Garage doors, shed materials, and external storage solutions', 55);

-- Add some comments for reference
COMMENT ON TABLE consumable_categories IS 'Standard consumable categories for Australian residential house construction';
COMMENT ON COLUMN consumable_categories.name IS 'Category name - should be descriptive and specific to Australian construction practices';
COMMENT ON COLUMN consumable_categories.description IS 'Detailed description of what items belong in this category';
COMMENT ON COLUMN consumable_categories.display_order IS 'Order for display in UI - lower numbers appear first';

-- Verify the insert worked
SELECT 
    COUNT(*) as total_categories,
    MIN(display_order) as min_order,
    MAX(display_order) as max_order
FROM consumable_categories;
