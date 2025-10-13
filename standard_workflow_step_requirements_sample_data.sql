-- Sample data for standard_workflow_step_requirements
-- Australian House Construction Materials
-- This script creates realistic material requirements for each workflow step

-- Foundation & Structure Materials
INSERT INTO standard_workflow_step_requirements (id, standard_workflow_step_id, consumable_category_id, item_name, item_description, display_order, active, created_at, updated_at) VALUES
-- Reinforcement Installation
(gen_random_uuid(), 'a308032a-fbd1-43ed-b734-1c82d6147e01', 'de8c8543-19b4-4b8e-ab47-ffce49060dda', 'Rebar - 12mm', 'Steel reinforcement bars for foundation', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'a308032a-fbd1-43ed-b734-1c82d6147e01', 'de8c8543-19b4-4b8e-ab47-ffce49060dda', 'Rebar - 16mm', 'Steel reinforcement bars for foundation', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'a308032a-fbd1-43ed-b734-1c82d6147e01', 'de8c8543-19b4-4b8e-ab47-ffce49060dda', 'Concrete Mesh', 'Welded steel mesh for slab reinforcement', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'a308032a-fbd1-43ed-b734-1c82d6147e01', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Tie Wire', 'Galvanized wire for tying rebar', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Concrete Pouring
(gen_random_uuid(), '46fd3d0c-f692-4f66-a74b-55ef6efbcdbe', '0de71499-e8ab-4526-848d-8197b7a4b73f', 'Concrete Mix - 25MPa', 'Ready-mix concrete for foundation', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '46fd3d0c-f692-4f66-a74b-55ef6efbcdbe', '0de71499-e8ab-4526-848d-8197b7a4b73f', 'Concrete Admixture', 'Plasticizer and accelerator additives', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '46fd3d0c-f692-4f66-a74b-55ef6efbcdbe', '5be5d41d-cf52-4932-b96a-09ba56b3c55e', 'Concrete Vibrator', 'Electric vibrator for concrete consolidation', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Foundation Bricks
(gen_random_uuid(), '7e941823-7943-4320-b770-a0e4868614d5', '68401c1a-2fba-4a44-93f5-9685008a97bb', 'Common Bricks', 'Standard clay bricks for foundation course', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '7e941823-7943-4320-b770-a0e4868614d5', '0de71499-e8ab-4526-848d-8197b7a4b73f', 'Mortar Mix', 'Cement mortar for brick laying', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '7e941823-7943-4320-b770-a0e4868614d5', '22cec27f-5477-487f-8580-892a070008fb', 'Brick Cleaner', 'Acid-based cleaner for brick faces', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Wall Bricklaying
(gen_random_uuid(), '9192cc5d-852b-446c-ae5c-bdfdea4da96c', '68401c1a-2fba-4a44-93f5-9685008a97bb', 'Face Bricks', 'Quality face bricks for external walls', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '9192cc5d-852b-446c-ae5c-bdfdea4da96c', '68401c1a-2fba-4a44-93f5-9685008a97bb', 'Common Bricks', 'Internal brickwork and backing', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '9192cc5d-852b-446c-ae5c-bdfdea4da96c', '0de71499-e8ab-4526-848d-8197b7a4b73f', 'Mortar Mix', 'Cement mortar for wall construction', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '9192cc5d-852b-446c-ae5c-bdfdea4da96c', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Wall Ties', 'Metal ties for cavity wall construction', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Floor Joists
(gen_random_uuid(), '58ece4da-8802-419a-b746-32df6b3dbaae', '10f588b5-cb69-4b89-8400-62331837844b', 'LVL Beams', 'Laminated veneer lumber for floor joists', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '58ece4da-8802-419a-b746-32df6b3dbaae', '10f588b5-cb69-4b89-8400-62331837844b', 'Treated Pine', 'Treated pine for floor bearers', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '58ece4da-8802-419a-b746-32df6b3dbaae', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Joist Hangers', 'Metal hangers for joist connections', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Wall Studs
(gen_random_uuid(), '9627a710-d5f8-4650-affe-6ebd2a132490', '10f588b5-cb69-4b89-8400-62331837844b', 'Treated Pine Studs', '90x45mm treated pine wall studs', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '9627a710-d5f8-4650-affe-6ebd2a132490', '10f588b5-cb69-4b89-8400-62331837844b', 'Treated Pine Plates', '90x45mm treated pine top and bottom plates', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '9627a710-d5f8-4650-affe-6ebd2a132490', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Nails - 90mm', 'Galvanized nails for framing', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Roof Trusses
(gen_random_uuid(), 'f0841097-366c-4ba9-8ce8-bc479ba3cc23', '41e4b0a3-f154-449f-9208-d58ecf4dde58', 'Prefab Roof Trusses', 'Engineered roof trusses', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'f0841097-366c-4ba9-8ce8-bc479ba3cc23', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Truss Bolts', 'High-tensile bolts for truss connections', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'f0841097-366c-4ba9-8ce8-bc479ba3cc23', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Truss Brackets', 'Metal brackets for truss installation', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Roof Tiles
(gen_random_uuid(), 'd10e5fba-853b-4932-9f85-1035d1898320', '92893d80-c165-4908-baf7-f148af52aeba', 'Concrete Roof Tiles', 'Standard concrete roof tiles', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'd10e5fba-853b-4932-9f85-1035d1898320', '92893d80-c165-4908-baf7-f148af52aeba', 'Tile Battens', 'Treated pine battens for tile fixing', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'd10e5fba-853b-4932-9f85-1035d1898320', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Tile Screws', 'Galvanized screws for tile fixing', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Windows & External Doors
(gen_random_uuid(), '3c96a8a1-0db7-435f-9fd2-1fb944ea8918', '860a5be8-9e35-4f6f-bc8a-1f9c04467312', 'Aluminium Windows', 'Double-glazed aluminium windows', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '3c96a8a1-0db7-435f-9fd2-1fb944ea8918', '860a5be8-9e35-4f6f-bc8a-1f9c04467312', 'Window Frames', 'Aluminium window frames', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), '3c96a8a1-0db7-435f-9fd2-1fb944ea8918', '3b2a3a65-bf89-4a34-adde-feb849698579', 'Window Hardware', 'Handles, locks, and window hardware', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- External Doors
(gen_random_uuid(), 'c3d4e5f6-a7b8-9012-3456-789012345678', '860a5be8-9e35-4f6f-bc8a-1f9c04467312', 'External Doors', 'Solid timber or composite external doors', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c3d4e5f6-a7b8-9012-3456-789012345678', '860a5be8-9e35-4f6f-bc8a-1f9c04467312', 'Door Frames', 'Treated pine door frames', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c3d4e5f6-a7b8-9012-3456-789012345678', '3b2a3a65-bf89-4a34-adde-feb849698579', 'Door Hardware', 'Handles, locks, hinges for external doors', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Water Supply Pipes
(gen_random_uuid(), 'd0e1f2a3-b4c5-6789-0123-457789012345', 'd393fa21-8672-4689-a4e9-1cafe28b5fe1', 'Copper Pipes - 20mm', 'Copper water supply pipes', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'd0e1f2a3-b4c5-6789-0123-457789012345', 'd393fa21-8672-4689-a4e9-1cafe28b5fe1', 'Copper Pipes - 15mm', 'Copper water supply pipes', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'd0e1f2a3-b4c5-6789-0123-457789012345', 'd393fa21-8672-4689-a4e9-1cafe28b5fe1', 'Copper Fittings', 'Elbows, tees, and copper fittings', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Drainage Pipes
(gen_random_uuid(), 'e1f2a3b4-c5d6-7890-1234-577890123456', 'd393fa21-8672-4689-a4e9-1cafe28b5fe1', 'PVC Pipes - 100mm', 'PVC drainage pipes', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'e1f2a3b4-c5d6-7890-1234-577890123456', 'd393fa21-8672-4689-a4e9-1cafe28b5fe1', 'PVC Pipes - 150mm', 'PVC main drainage pipes', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'e1f2a3b4-c5d6-7890-1234-577890123456', 'd393fa21-8672-4689-a4e9-1cafe28b5fe1', 'PVC Fittings', 'PVC elbows, tees, and drainage fittings', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Electrical Wiring
(gen_random_uuid(), 'b4c5d6e7-f8a9-0123-4567-990123456789', '2afeb6ae-71df-4387-ab4d-86355d14112e', 'Electrical Cable - 2.5mm', 'Twin and earth electrical cable', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'b4c5d6e7-f8a9-0123-4567-990123456789', '2afeb6ae-71df-4387-ab4d-86355d14112e', 'Electrical Cable - 4mm', 'Twin and earth electrical cable', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'b4c5d6e7-f8a9-0123-4567-990123456789', '33b88a43-4e3e-40da-8197-c85da508d28d', 'Electrical Conduit', 'PVC electrical conduit', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Power Points & Switches
(gen_random_uuid(), 'c5d6e7f8-a9b0-1234-5678-001234567890', '60be5b93-8b1e-461b-b6c4-e90c8142d07e', 'Power Points', 'Double power points with safety switches', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c5d6e7f8-a9b0-1234-5678-001234567890', '60be5b93-8b1e-461b-b6c4-e90c8142d07e', 'Light Switches', 'Single and double light switches', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c5d6e7f8-a9b0-1234-5678-001234567890', '33b88a43-4e3e-40da-8197-c85da508d28d', 'Switch Boxes', 'Electrical switch boxes and mounting brackets', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Wall Lining
(gen_random_uuid(), 'a9b0c1d2-e3f4-5678-9012-445678901234', 'a0fd747a-28df-402a-9a69-b58d7935ddf5', 'Gyprock Sheets', '13mm plasterboard sheets', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'a9b0c1d2-e3f4-5678-9012-445678901234', 'a0fd747a-28df-402a-9a69-b58d7935ddf5', 'Gyprock Corners', 'Metal corner beads for plasterboard', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'a9b0c1d2-e3f4-5678-9012-445678901234', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Gyprock Screws', 'Self-drilling screws for plasterboard', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Ceiling Installation
(gen_random_uuid(), 'f8a9b0c1-d2e3-4567-8901-334567890123', 'a0fd747a-28df-402a-9a69-b58d7935ddf5', 'Ceiling Gyprock', '10mm plasterboard for ceilings', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'f8a9b0c1-d2e3-4567-8901-334567890123', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Ceiling Screws', 'Self-drilling screws for ceiling', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'f8a9b0c1-d2e3-4567-8901-334567890123', 'a0fd747a-28df-402a-9a69-b58d7935ddf5', 'Ceiling Corners', 'Metal corner beads for ceiling', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- External Painting
(gen_random_uuid(), 'e5f6a7b8-c9d0-1234-5678-901234567890', '627c8dd3-452c-48ad-b10a-fe300403fa29', 'Exterior Primer', 'Exterior primer for weatherboard', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'e5f6a7b8-c9d0-1234-5678-901234567890', '627c8dd3-452c-48ad-b10a-fe300403fa29', 'Exterior Paint', 'Acrylic exterior paint', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'e5f6a7b8-c9d0-1234-5678-901234567890', '627c8dd3-452c-48ad-b10a-fe300403fa29', 'Paint Brushes', 'Paint brushes and rollers', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Internal Painting
(gen_random_uuid(), 'd2e3f4a5-b6c7-8901-2345-778901234567', 'b486ffd7-c475-4251-b28b-fe510cdda71c', 'Interior Primer', 'Interior primer for walls', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'd2e3f4a5-b6c7-8901-2345-778901234567', 'b486ffd7-c475-4251-b28b-fe510cdda71c', 'Interior Paint', 'Low-VOC interior paint', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'd2e3f4a5-b6c7-8901-2345-778901234567', 'b486ffd7-c475-4251-b28b-fe510cdda71c', 'Paint Rollers', 'Paint rollers and trays', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Kitchen Cabinets
(gen_random_uuid(), 'f4a5b6c7-d8e9-0123-4567-990123456789', 'f257249a-0515-4e16-b59b-d3edb1802760', 'Kitchen Cabinets', 'Pre-made kitchen cabinets', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'f4a5b6c7-d8e9-0123-4567-990123456789', 'f257249a-0515-4e16-b59b-d3edb1802760', 'Cabinet Hardware', 'Handles, hinges, and cabinet hardware', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'f4a5b6c7-d8e9-0123-4567-990123456789', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Cabinet Screws', 'Screws for cabinet installation', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Kitchen Benchtop
(gen_random_uuid(), 'a5b6c7d8-e9f0-1234-5688-901234567890', 'f257249a-0515-4e16-b59b-d3edb1802760', 'Kitchen Benchtop', 'Laminate or stone benchtop', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'a5b6c7d8-e9f0-1234-5688-901234567890', 'b33f3e68-8819-4a37-8bd5-8dc16c252484', 'Kitchen Sink', 'Stainless steel kitchen sink', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'a5b6c7d8-e9f0-1234-5688-901234567890', 'b33f3e68-8819-4a37-8bd5-8dc16c252484', 'Kitchen Taps', 'Mixer taps for kitchen', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Bathroom Fixtures
(gen_random_uuid(), 'd8e9f0a1-b2c3-4567-8991-234567890123', 'fcb1ea8f-535d-4e56-b3dd-855c21369393', 'Toilet Suite', 'Complete toilet suite', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'd8e9f0a1-b2c3-4567-8991-234567890123', 'fcb1ea8f-535d-4e56-b3dd-855c21369393', 'Basin', 'Bathroom basin and vanity', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'd8e9f0a1-b2c3-4567-8991-234567890123', 'fcb1ea8f-535d-4e56-b3dd-855c21369393', 'Shower Screen', 'Frameless shower screen', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Bathroom Tiling
(gen_random_uuid(), 'c7d8e9f0-a1b2-3456-7880-123456789012', 'cae69649-63e0-4c8c-930a-b8070236779c', 'Bathroom Wall Tiles', 'Ceramic or porcelain wall tiles', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c7d8e9f0-a1b2-3456-7880-123456789012', 'cae69649-63e0-4c8c-930a-b8070236779c', 'Bathroom Floor Tiles', 'Non-slip ceramic floor tiles', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c7d8e9f0-a1b2-3456-7880-123456789012', '22cec27f-5477-487f-8580-892a070008fb', 'Tile Adhesive', 'Waterproof tile adhesive for wet areas', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c7d8e9f0-a1b2-3456-7880-123456789012', '22cec27f-5477-487f-8580-892a070008fb', 'Tile Grout', 'Waterproof grout for bathroom tiles', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c7d8e9f0-a1b2-3456-7880-123456789012', '66167c69-5d69-48a0-b472-c4a292dfb1f5', 'Waterproof Membrane', 'Waterproof membrane for wet areas', 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Bathroom Accessories
(gen_random_uuid(), 'e9f0a1b2-c3d4-5678-9022-345678901234', 'fcb1ea8f-535d-4e56-b3dd-855c21369393', 'Towel Rails', 'Heated towel rails and hooks', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'e9f0a1b2-c3d4-5678-9022-345678901234', 'fcb1ea8f-535d-4e56-b3dd-855c21369393', 'Toilet Roll Holder', 'Wall-mounted toilet roll holder', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'e9f0a1b2-c3d4-5678-9022-345678901234', 'fcb1ea8f-535d-4e56-b3dd-855c21369393', 'Shower Mixer', 'Thermostatic shower mixer', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'e9f0a1b2-c3d4-5678-9022-345678901234', 'fcb1ea8f-535d-4e56-b3dd-855c21369393', 'Basin Mixer', 'Basin mixer taps', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'e9f0a1b2-c3d4-5678-9022-345678901234', 'ec6f9179-ac8a-417b-8d46-8eccff2e03ef', 'Bathroom Mirror', 'Bathroom mirror and mounting hardware', 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'e9f0a1b2-c3d4-5678-9022-345678901234', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Bathroom Hardware', 'Screws, anchors, and mounting hardware', 6, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Floor Covering
(gen_random_uuid(), 'c1d2e3f4-a5b6-7890-1234-667890123456', 'cae69649-63e0-4c8c-930a-b8070236779c', 'Timber Flooring', 'Engineered timber flooring', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c1d2e3f4-a5b6-7890-1234-667890123456', 'cae69649-63e0-4c8c-930a-b8070236779c', 'Floor Underlay', 'Rubber underlay for flooring', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'c1d2e3f4-a5b6-7890-1234-667890123456', '2e6f09ee-2af5-4f74-bb56-0bfe7f842d6d', 'Floor Screws', 'Screws for floor installation', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Light Fixtures
(gen_random_uuid(), 'f0a1b2c3-d4e5-6789-0133-456789012345', 'fba82864-fc29-46c9-99ef-c218b84e6f31', 'LED Downlights', 'Energy-efficient LED downlights', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'f0a1b2c3-d4e5-6789-0133-456789012345', 'fba82864-fc29-46c9-99ef-c218b84e6f31', 'Pendant Lights', 'Decorative pendant light fittings', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(gen_random_uuid(), 'f0a1b2c3-d4e5-6789-0133-456789012345', '33b88a43-4e3e-40da-8197-c85da508d28d', 'Light Fittings', 'Mounting brackets and electrical fittings', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
