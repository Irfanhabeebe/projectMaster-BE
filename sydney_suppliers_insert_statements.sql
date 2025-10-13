-- Sydney-Based Suppliers for Australian Residential Construction
-- This file contains INSERT statements for suppliers and their category relationships
-- Uses PostgreSQL's gen_random_uuid() function for UUID generation

-- ========================================
-- PART 1: INSERT SUPPLIERS
-- ========================================

INSERT INTO suppliers (id, created_at, updated_at, name, address, abn, email, phone, contact_person, website, supplier_type, payment_terms, credit_limit, active, verified) VALUES

-- Large Retail Chains
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Bunnings Warehouse - Alexandria', '75 O''Riordan St, Alexandria NSW 2015', '63000000001', 'trade.alexandria@bunnings.com.au', '(02) 9698 9800', 'Trade Desk', 'www.bunnings.com.au', 'RETAIL', 'NET_30', 50000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Bunnings Warehouse - Artarmon', '400 Pacific Hwy, Artarmon NSW 2064', '63000000002', 'trade.artarmon@bunnings.com.au', '(02) 9436 1444', 'Trade Desk', 'www.bunnings.com.au', 'RETAIL', 'NET_30', 50000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Mitre 10 - Rozelle', '634 Darling St, Rozelle NSW 2039', '12345678901', 'rozelle@mitre10.com.au', '(02) 9555 1422', 'Sales Manager', 'www.mitre10.com.au', 'RETAIL', 'NET_30', 40000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Home Timber & Hardware - Chatswood', '375 Victoria Ave, Chatswood NSW 2067', '23456789012', 'chatswood@hth.com.au', '(02) 9411 4322', 'Trade Sales', 'www.hth.com.au', 'RETAIL', 'NET_30', 35000.00, true, true),

-- Specialist Plumbing Suppliers
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Reece Plumbing - Auburn', '119-121 Parramatta Rd, Auburn NSW 2144', '34567890123', 'auburn@reece.com.au', '(02) 9646 2411', 'Branch Manager', 'www.reece.com.au', 'SPECIALIST', 'NET_14', 75000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Tradelink Plumbing Centres - Homebush', '3 George St, Homebush NSW 2140', '45678901234', 'homebush@tradelink.com.au', '(02) 9763 9777', 'Trade Counter', 'www.tradelink.com.au', 'SPECIALIST', 'NET_14', 70000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Plumbing Plus - Chullora', '18 Anzac St, Chullora NSW 2190', '56789012345', 'chullora@plumbingplus.com.au', '(02) 9642 5000', 'Sales Team', 'www.plumbingplus.com.au', 'SPECIALIST', 'NET_14', 60000.00, true, true),

-- Specialist Electrical Suppliers
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Haymans Electrical - Alexandria', '191 McEvoy St, Alexandria NSW 2015', '67890123456', 'alexandria@haymans.com.au', '(02) 9319 4455', 'Trade Sales', 'www.haymans.com.au', 'SPECIALIST', 'NET_14', 65000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'L&H Group - Silverwater', '8 Holker St, Silverwater NSW 2128', '78901234567', 'silverwater@lhgroup.com.au', '(02) 9648 6644', 'Account Manager', 'www.lhgroup.com.au', 'SPECIALIST', 'NET_14', 80000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Rexel Electrical Supplies - Mascot', '12-14 Cunneen St, Mascot NSW 2020', '89012345678', 'mascot@rexel.com.au', '(02) 9317 3355', 'Branch Manager', 'www.rexel.com.au', 'SPECIALIST', 'NET_14', 70000.00, true, true),

-- Timber & Building Materials
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Boral Timber - Five Dock', '102 Great North Rd, Five Dock NSW 2046', '90123456789', 'fivedock@boral.com.au', '(02) 9712 0211', 'Trade Centre', 'www.boral.com.au', 'WHOLESALE', 'NET_30', 100000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Engineered Wood Products (EWP) - Revesby', '2-4 Marco Ave, Revesby NSW 2212', '01234567890', 'sydney@ewp.com.au', '(02) 9772 2955', 'Sales Manager', 'www.ewp.com.au', 'SPECIALIST', 'NET_30', 85000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Hurford Wholesale - Ingleburn', 'Unit 1, 3 Cumberland Green, Ingleburn NSW 2565', '11234567890', 'ingleburn@hurford.com.au', '(02) 9605 5522', 'Account Manager', 'www.hurford.com.au', 'WHOLESALE', 'NET_30', 90000.00, true, true),

-- Concrete & Cement
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Boral Concrete & Quarries - Sydney', '153 Parramatta Rd, Concord NSW 2137', '22345678901', 'concrete.sydney@boral.com.au', '13 16 72', 'Customer Service', 'www.boral.com.au', 'MANUFACTURER', 'NET_30', 150000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Holcim Australia - Rosehill', '100 James Ruse Dr, Rosehill NSW 2142', '33456789012', 'rosehill@holcim.com', '1300 465 246', 'Sales Office', 'www.holcim.com.au', 'MANUFACTURER', 'NET_30', 140000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Hanson Construction Materials - Matraville', '1145 Bunnerong Rd, Matraville NSW 2036', '44567890123', 'matraville@hanson.com.au', '(02) 9311 2755', 'Plant Manager', 'www.hanson.com.au', 'MANUFACTURER', 'NET_30', 130000.00, true, true),

-- Roofing Specialists
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Stratco - Smithfield', '604 Woodville Rd, Smithfield NSW 2164', '55678901234', 'smithfield@stratco.com.au', '(02) 9725 3066', 'Sales Team', 'www.stratco.com.au', 'SPECIALIST', 'NET_14', 55000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Fielders Centre - Arndell Park', '20 Arndell St, Arndell Park NSW 2148', '66789012345', 'arndellpark@fielders.com.au', '(02) 9837 3600', 'Trade Counter', 'www.fielders.com.au', 'SPECIALIST', 'NET_14', 60000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Monier Roofing - Smithfield', '15 Christie St, Smithfield NSW 2164', '77890123456', 'sydney@monier.com.au', '1300 667 617', 'Customer Service', 'www.monier.com.au', 'MANUFACTURER', 'NET_30', 80000.00, true, true),

-- Flooring Specialists
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'National Tiles - Artarmon', '2 Hampden Rd, Artarmon NSW 2064', '88901234567', 'artarmon@nationaltiles.com.au', '(02) 9906 1155', 'Showroom Manager', 'www.nationaltiles.com.au', 'SPECIALIST', 'NET_30', 45000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Carpet Court - Alexandria', '58 O''Riordan St, Alexandria NSW 2015', '99012345678', 'alexandria@carpetcourt.com.au', '(02) 9669 4844', 'Sales Consultant', 'www.carpetcourt.com.au', 'SPECIALIST', 'NET_30', 40000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Woodcut Timber Flooring - Annandale', '58 Johnston St, Annandale NSW 2038', '00123456789', 'annandale@woodcut.com.au', '(02) 9566 1066', 'Showroom', 'www.woodcut.com.au', 'SPECIALIST', 'NET_30', 50000.00, true, true),

-- Kitchen & Bathroom Specialists
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Caroma - Norwood (Showroom)', '105-115 Reservoir St, Norwood SA 5067', '10234567890', 'sydney@caroma.com.au', '1800 226 662', 'Specification Team', 'www.caroma.com.au', 'MANUFACTURER', 'NET_30', 75000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Rogerseller - Alexandria', '201-203 O''Riordan St, Alexandria NSW 2015', '20345678901', 'alexandria@rogerseller.com.au', '(02) 8339 6999', 'Trade Sales', 'www.rogerseller.com.au', 'SPECIALIST', 'NET_14', 60000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'The Sink Warehouse - Silverwater', '62 Silverwater Rd, Silverwater NSW 2128', '30456789012', 'silverwater@sinkwarehouse.com.au', '(02) 9648 2766', 'Sales Team', 'www.sinkwarehouse.com.au', 'SPECIALIST', 'NET_14', 45000.00, true, true),

-- Paint & Finishes
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Dulux Trade Centre - Artarmon', '2-8 Langdon St, Artarmon NSW 2064', '40567890123', 'artarmon@dulux.com.au', '(02) 9413 2111', 'Trade Manager', 'www.dulux.com.au', 'MANUFACTURER', 'NET_30', 55000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Taubmans Trade Centre - Mascot', '1440 Botany Rd, Botany NSW 2019', '50678901234', 'mascot@taubmans.com.au', '(02) 9317 3022', 'Trade Sales', 'www.taubmans.com.au', 'MANUFACTURER', 'NET_30', 50000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Haymes Paint - Artarmon', '36 Reserve Rd, Artarmon NSW 2064', '60789012345', 'artarmon@haymespaint.com.au', '(02) 9906 5522', 'Color Consultant', 'www.haymespaint.com.au', 'MANUFACTURER', 'NET_30', 48000.00, true, true),

-- Insulation Specialists
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'CSR Bradford Insulation - Moorebank', '3 Bernera Rd, Moorebank NSW 2170', '70890123456', 'moorebank@csr.com.au', '1300 674 674', 'Sales Office', 'www.bradfordinsulation.com.au', 'MANUFACTURER', 'NET_30', 65000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Knauf Insulation - Smithfield', '15 O''Hara Rd, Smithfield NSW 2164', '80901234567', 'smithfield@knaufinsulation.com.au', '1800 065 056', 'Customer Service', 'www.knaufinsulation.com.au', 'MANUFACTURER', 'NET_30', 62000.00, true, true),

-- Windows & Doors
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Wideline Windows & Doors - Smithfield', '6 Huntingwood Dr, Huntingwood NSW 2148', '90012345678', 'smithfield@wideline.com.au', '(02) 9609 9737', 'Sales Manager', 'www.wideline.com.au', 'MANUFACTURER', 'NET_30', 70000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Trend Windows & Doors - Wetherill Park', '1426 The Horsley Dr, Wetherill Park NSW 2164', '00234567891', 'wetherill@trendwindows.com.au', '(02) 9609 9900', 'Showroom', 'www.trendwindows.com.au', 'MANUFACTURER', 'NET_30', 75000.00, true, true),

-- Hardware & Fasteners
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ITW Buildex - Smithfield', '15-17 Seville St, Smithfield NSW 2164', '11345678902', 'sydney@itwbuildex.com.au', '(02) 9725 3077', 'Technical Sales', 'www.itwbuildex.com.au', 'MANUFACTURER', 'NET_30', 85000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Ramset Fasteners - Smithfield', '10 Christie St, Smithfield NSW 2164', '22456789013', 'smithfield@ramset.com.au', '1300 726 738', 'Sales Team', 'www.ramset.com.au', 'MANUFACTURER', 'NET_30', 80000.00, true, true),

-- Landscaping & Outdoor
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Landscape Supplies Sydney - Silverwater', '32-34 Silverwater Rd, Silverwater NSW 2128', '33567890124', 'silverwater@landscapesupplies.com.au', '(02) 9748 2024', 'Customer Service', 'www.landscapesupplies.com.au', 'SPECIALIST', 'NET_14', 35000.00, true, true),
(gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Gyprock Supplies - Smithfield', '7-9 Factory St, Smithfield NSW 2164', '44678901235', 'smithfield@gyprock.com.au', '1800 497 762', 'Trade Counter', 'www.gyprock.com.au', 'MANUFACTURER', 'NET_30', 72000.00, true, true);


-- ========================================
-- PART 2: SUPPLIER-CATEGORY RELATIONSHIPS
-- ========================================

-- This section maps suppliers to the categories they serve
-- Note: Some suppliers serve multiple categories

INSERT INTO supplier_categories (id, supplier_id, category_id, is_primary_category, active, created_at, updated_at)
SELECT 
    gen_random_uuid() as id,
    s.id as supplier_id,
    c.id as category_id,
    CASE 
        -- Bunnings primary categories
        WHEN s.name LIKE 'Bunnings%' AND c.name = 'General Hardware' THEN true
        -- Mitre 10 primary
        WHEN s.name LIKE 'Mitre 10%' AND c.name = 'General Hardware' THEN true
        -- Reece primary
        WHEN s.name LIKE 'Reece%' AND c.name = 'Plumbing Pipes & Fittings' THEN true
        -- Tradelink primary
        WHEN s.name LIKE 'Tradelink%' AND c.name = 'Plumbing Pipes & Fittings' THEN true
        -- Haymans primary
        WHEN s.name LIKE 'Haymans%' AND c.name = 'Electrical Cables & Wires' THEN true
        -- L&H primary
        WHEN s.name LIKE 'L&H%' AND c.name = 'Electrical Fittings' THEN true
        -- Boral Timber primary
        WHEN s.name LIKE 'Boral Timber%' AND c.name = 'Timber Framing' THEN true
        -- Boral Concrete primary
        WHEN s.name LIKE 'Boral Concrete%' AND c.name = 'Concrete & Cement' THEN true
        -- Stratco primary
        WHEN s.name LIKE 'Stratco%' AND c.name = 'Roofing Materials' THEN true
        -- Monier primary
        WHEN s.name LIKE 'Monier%' AND c.name = 'Roofing Materials' THEN true
        -- National Tiles primary
        WHEN s.name LIKE 'National Tiles%' AND c.name = 'Flooring Materials' THEN true
        -- Caroma primary
        WHEN s.name LIKE 'Caroma%' AND c.name = 'Bathroom Fixtures' THEN true
        -- Dulux primary
        WHEN s.name LIKE 'Dulux%' AND c.name = 'Interior Finishes' THEN true
        -- CSR Bradford primary
        WHEN s.name LIKE 'CSR Bradford%' AND c.name = 'Wall Insulation' THEN true
        -- Wideline primary
        WHEN s.name LIKE 'Wideline%' AND c.name = 'Windows & External Doors' THEN true
        -- Gyprock primary
        WHEN s.name LIKE 'Gyprock%' AND c.name = 'Wall & Ceiling Linings' THEN true
        ELSE false
    END as is_primary_category,
    true as active,
    CURRENT_TIMESTAMP as created_at,
    CURRENT_TIMESTAMP as updated_at
FROM suppliers s
CROSS JOIN consumable_categories c
WHERE 
    -- Bunnings - everything except highly specialized items
    (s.name LIKE 'Bunnings%' AND c.name IN ('Concrete & Cement', 'Waterproofing Materials', 'Timber Framing', 'Wall & Ceiling Linings', 'Roofing Materials', 'Gutters & Downpipes', 'External Cladding', 'External Finishes', 'Windows & External Doors', 'External Hardware', 'Plumbing Pipes & Fittings', 'Kitchen Fixtures', 'Plumbing Accessories', 'Electrical Cables & Wires', 'Electrical Fittings', 'Lighting Fixtures', 'Electrical Accessories', 'Flooring Materials', 'Floor Preparation', 'Floor Finishes', 'Internal Doors', 'Interior Finishes', 'Paint and Finishes', 'General Hardware', 'Structural Fasteners', 'Adhesives & Sealants', 'Site Preparation', 'Drainage & Stormwater', 'Landscaping Materials', 'Safety Equipment', 'Decorative Elements', 'Outdoor Living')) OR
    
    -- Mitre 10 - similar to Bunnings
    (s.name LIKE 'Mitre 10%' AND c.name IN ('Concrete & Cement', 'Waterproofing Materials', 'Timber Framing', 'Wall & Ceiling Linings', 'Roofing Materials', 'Gutters & Downpipes', 'External Finishes', 'External Hardware', 'Plumbing Pipes & Fittings', 'Kitchen Fixtures', 'Plumbing Accessories', 'Electrical Cables & Wires', 'Electrical Fittings', 'Lighting Fixtures', 'Flooring Materials', 'Floor Finishes', 'Internal Doors', 'Interior Finishes', 'Paint and Finishes', 'General Hardware', 'Structural Fasteners', 'Adhesives & Sealants', 'Landscaping Materials', 'Safety Equipment', 'Decorative Elements')) OR
    
    -- Home Timber & Hardware
    (s.name LIKE 'Home Timber%' AND c.name IN ('Timber Framing', 'Internal Doors', 'Flooring Materials', 'General Hardware', 'Structural Fasteners', 'Adhesives & Sealants', 'Paint and Finishes', 'Interior Finishes')) OR
    
    -- Reece Plumbing - all plumbing
    (s.name LIKE 'Reece%' AND c.name IN ('Plumbing Pipes & Fittings', 'Bathroom Fixtures', 'Kitchen Fixtures', 'Hot Water Systems', 'Plumbing Accessories', 'Bathroom Vanities', 'Drainage & Stormwater')) OR
    
    -- Tradelink - all plumbing
    (s.name LIKE 'Tradelink%' AND c.name IN ('Plumbing Pipes & Fittings', 'Bathroom Fixtures', 'Kitchen Fixtures', 'Hot Water Systems', 'Plumbing Accessories', 'Bathroom Vanities', 'Drainage & Stormwater')) OR
    
    -- Plumbing Plus
    (s.name LIKE 'Plumbing Plus%' AND c.name IN ('Plumbing Pipes & Fittings', 'Bathroom Fixtures', 'Kitchen Fixtures', 'Hot Water Systems', 'Plumbing Accessories')) OR
    
    -- Haymans Electrical - all electrical
    (s.name LIKE 'Haymans%' AND c.name IN ('Electrical Cables & Wires', 'Electrical Fittings', 'Lighting Fixtures', 'Electrical Accessories', 'Home Automation', 'Security Systems')) OR
    
    -- L&H Group - all electrical
    (s.name LIKE 'L&H%' AND c.name IN ('Electrical Cables & Wires', 'Electrical Fittings', 'Lighting Fixtures', 'Electrical Accessories', 'Home Automation', 'Security Systems', 'Fire Safety')) OR
    
    -- Rexel - all electrical
    (s.name LIKE 'Rexel%' AND c.name IN ('Electrical Cables & Wires', 'Electrical Fittings', 'Lighting Fixtures', 'Electrical Accessories', 'Home Automation')) OR
    
    -- Boral Timber
    (s.name LIKE 'Boral Timber%' AND c.name IN ('Timber Framing', 'Roof Trusses', 'Wall & Ceiling Linings', 'Formwork Materials', 'Internal Doors', 'Flooring Materials')) OR
    
    -- EWP
    (s.name LIKE 'Engineered Wood%' AND c.name IN ('Timber Framing', 'Roof Trusses', 'Flooring Materials')) OR
    
    -- Hurford
    (s.name LIKE 'Hurford%' AND c.name IN ('Timber Framing', 'Flooring Materials', 'Internal Doors', 'Decorative Elements')) OR
    
    -- Boral Concrete
    (s.name LIKE 'Boral Concrete%' AND c.name IN ('Concrete & Cement', 'Steel Reinforcement', 'Formwork Materials')) OR
    
    -- Holcim
    (s.name LIKE 'Holcim%' AND c.name IN ('Concrete & Cement', 'Steel Reinforcement')) OR
    
    -- Hanson
    (s.name LIKE 'Hanson%' AND c.name IN ('Concrete & Cement', 'Steel Reinforcement', 'Landscaping Materials')) OR
    
    -- Stratco - roofing and outdoor
    (s.name LIKE 'Stratco%' AND c.name IN ('Roofing Materials', 'Gutters & Downpipes', 'Roof Accessories', 'External Cladding', 'Outdoor Living', 'Garage & Shed')) OR
    
    -- Fielders
    (s.name LIKE 'Fielders%' AND c.name IN ('Roofing Materials', 'Gutters & Downpipes', 'Roof Accessories', 'External Cladding')) OR
    
    -- Monier
    (s.name LIKE 'Monier%' AND c.name IN ('Roofing Materials', 'Roof Accessories', 'Gutters & Downpipes')) OR
    
    -- National Tiles
    (s.name LIKE 'National Tiles%' AND c.name IN ('Flooring Materials', 'Floor Preparation', 'Bathroom Fixtures', 'Kitchen Cabinetry')) OR
    
    -- Carpet Court
    (s.name LIKE 'Carpet Court%' AND c.name IN ('Flooring Materials', 'Floor Preparation')) OR
    
    -- Woodcut
    (s.name LIKE 'Woodcut%' AND c.name IN ('Flooring Materials', 'Floor Preparation', 'Floor Finishes')) OR
    
    -- Caroma
    (s.name LIKE 'Caroma%' AND c.name IN ('Bathroom Fixtures', 'Kitchen Fixtures', 'Plumbing Accessories', 'Bathroom Vanities')) OR
    
    -- Rogerseller
    (s.name LIKE 'Rogerseller%' AND c.name IN ('Bathroom Fixtures', 'Kitchen Fixtures', 'Bathroom Vanities')) OR
    
    -- Sink Warehouse
    (s.name LIKE 'The Sink%' AND c.name IN ('Bathroom Fixtures', 'Kitchen Fixtures', 'Plumbing Accessories')) OR
    
    -- Dulux
    (s.name LIKE 'Dulux%' AND c.name IN ('Paint and Finishes', 'Interior Finishes', 'External Finishes', 'Floor Finishes')) OR
    
    -- Taubmans
    (s.name LIKE 'Taubmans%' AND c.name IN ('Paint and Finishes', 'Interior Finishes', 'External Finishes')) OR
    
    -- Haymes Paint
    (s.name LIKE 'Haymes%' AND c.name IN ('Paint and Finishes', 'Interior Finishes', 'External Finishes')) OR
    
    -- CSR Bradford
    (s.name LIKE 'CSR Bradford%' AND c.name IN ('Wall Insulation', 'Roof Insulation', 'Floor Insulation')) OR
    
    -- Knauf
    (s.name LIKE 'Knauf%' AND c.name IN ('Wall Insulation', 'Roof Insulation', 'Floor Insulation', 'Wall & Ceiling Linings')) OR
    
    -- Wideline
    (s.name LIKE 'Wideline%' AND c.name IN ('Windows & External Doors', 'External Hardware')) OR
    
    -- Trend Windows
    (s.name LIKE 'Trend Windows%' AND c.name IN ('Windows & External Doors', 'External Hardware')) OR
    
    -- ITW Buildex
    (s.name LIKE 'ITW%' AND c.name IN ('Structural Fasteners', 'General Hardware')) OR
    
    -- Ramset
    (s.name LIKE 'Ramset%' AND c.name IN ('Structural Fasteners', 'General Hardware', 'Adhesives & Sealants')) OR
    
    -- Landscape Supplies
    (s.name LIKE 'Landscape Supplies%' AND c.name IN ('Landscaping Materials', 'Drainage & Stormwater', 'Site Preparation', 'Concrete & Cement')) OR
    
    -- Gyprock
    (s.name LIKE 'Gyprock%' AND c.name IN ('Wall & Ceiling Linings', 'Internal Doors', 'Adhesives & Sealants'))
    
    AND NOT EXISTS (
        SELECT 1 FROM supplier_categories sc2 
        WHERE sc2.supplier_id = s.id AND sc2.category_id = c.id
    );

-- Verify the inserts
SELECT 
    COUNT(DISTINCT s.id) as total_suppliers,
    COUNT(sc.id) as total_supplier_category_relationships,
    COUNT(CASE WHEN sc.is_primary_category = true THEN 1 END) as primary_relationships
FROM suppliers s
LEFT JOIN supplier_categories sc ON s.id = sc.supplier_id;

