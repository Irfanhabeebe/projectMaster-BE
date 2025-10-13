-- Remove ADHOC_TASK from project_dependencies constraint
-- Since AdhocTask entity is removed and adhoc functionality is handled by ProjectTask with adhoc flag

-- Drop old constraint
ALTER TABLE project_dependencies DROP CONSTRAINT IF EXISTS chk_project_dependency_types;

-- Add new constraint without ADHOC_TASK
ALTER TABLE project_dependencies ADD CONSTRAINT chk_project_dependency_types 
    CHECK (dependent_entity_type IN ('TASK', 'STEP', 'STAGE') AND depends_on_entity_type IN ('TASK', 'STEP', 'STAGE'));

-- Drop adhoc_tasks table if it exists (cleanup)
DROP TABLE IF EXISTS adhoc_tasks CASCADE;

-- Add comment for documentation
COMMENT ON CONSTRAINT chk_project_dependency_types ON project_dependencies IS 
    'Valid entity types: TASK, STEP, STAGE. ADHOC_TASK removed as adhoc functionality is now handled via adhoc flag on ProjectTask';

