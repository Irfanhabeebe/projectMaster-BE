-- Add customer_selectable column to all requirement tables
-- Migration: V43__Add_customer_selectable_to_requirements.sql

-- Add customer_selectable to standard_workflow_step_requirements
ALTER TABLE standard_workflow_step_requirements 
ADD COLUMN customer_selectable BOOLEAN NOT NULL DEFAULT TRUE;

-- Add customer_selectable to workflow_step_requirements  
ALTER TABLE workflow_step_requirements 
ADD COLUMN customer_selectable BOOLEAN NOT NULL DEFAULT TRUE;

-- Add customer_selectable to project_step_requirements
ALTER TABLE project_step_requirements 
ADD COLUMN customer_selectable BOOLEAN NOT NULL DEFAULT TRUE;

-- Add indexes for better query performance
CREATE INDEX idx_standard_workflow_step_requirements_customer_selectable 
ON standard_workflow_step_requirements(customer_selectable);

CREATE INDEX idx_workflow_step_requirements_customer_selectable 
ON workflow_step_requirements(customer_selectable);

CREATE INDEX idx_project_step_requirements_customer_selectable 
ON project_step_requirements(customer_selectable);
