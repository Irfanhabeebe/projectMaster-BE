-- Create standard_workflow_step_requirements table
CREATE TABLE standard_workflow_step_requirements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    standard_workflow_step_id UUID NOT NULL,
    consumable_category_id UUID NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    item_description TEXT,
    display_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_standard_workflow_step_requirements_step 
        FOREIGN KEY (standard_workflow_step_id) 
        REFERENCES standard_workflow_steps(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_standard_workflow_step_requirements_category 
        FOREIGN KEY (consumable_category_id) 
        REFERENCES consumable_categories(id) ON DELETE CASCADE
);

-- Add indexes for performance
CREATE INDEX idx_standard_workflow_step_requirements_step_id ON standard_workflow_step_requirements(standard_workflow_step_id);
CREATE INDEX idx_standard_workflow_step_requirements_category_id ON standard_workflow_step_requirements(consumable_category_id);
CREATE INDEX idx_standard_workflow_step_requirements_active ON standard_workflow_step_requirements(active);

-- Add item_description column to workflow_step_requirements table
ALTER TABLE workflow_step_requirements 
ADD COLUMN item_description TEXT;
