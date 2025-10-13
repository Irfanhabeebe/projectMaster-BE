-- Drop all consumable management tables to recreate them cleanly
-- Run this if you need to reset the consumable management system

-- Drop tables in correct order (respecting foreign key constraints)
DROP TABLE IF EXISTS project_step_requirements CASCADE;
DROP TABLE IF EXISTS workflow_step_requirements CASCADE;
DROP TABLE IF EXISTS supplier_categories CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;
DROP TABLE IF EXISTS consumable_categories CASCADE;

-- Verify tables are dropped
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('consumable_categories', 'suppliers', 'supplier_categories', 'workflow_step_requirements', 'project_step_requirements');

