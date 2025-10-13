-- Migration V33: Create Consumable Management System Tables
-- This migration creates all the tables needed for the consumable management system

-- Create consumable categories table
CREATE TABLE IF NOT EXISTS consumable_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category_group VARCHAR(100),
    icon VARCHAR(100),
    display_order INTEGER NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create unique index on category name (case-insensitive)
CREATE UNIQUE INDEX IF NOT EXISTS idx_consumable_categories_name_unique 
ON consumable_categories (LOWER(name));

-- Create suppliers table
CREATE TABLE IF NOT EXISTS suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    address TEXT,
    abn VARCHAR(11),
    email VARCHAR(255),
    phone VARCHAR(50),
    contact_person VARCHAR(255),
    website VARCHAR(500),
    supplier_type VARCHAR(50) NOT NULL DEFAULT 'RETAIL',
    payment_terms VARCHAR(50) NOT NULL DEFAULT 'NET_30',
    credit_limit DECIMAL(15,2),
    active BOOLEAN NOT NULL DEFAULT true,
    verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_supplier_type CHECK (supplier_type IN ('RETAIL', 'WHOLESALE', 'SPECIALIST', 'ONLINE', 'MANUFACTURER')),
    CONSTRAINT chk_payment_terms CHECK (payment_terms IN ('COD', 'NET_7', 'NET_14', 'NET_30', 'NET_60', 'PREPAID'))
);

-- Create unique indexes on supplier fields
CREATE UNIQUE INDEX IF NOT EXISTS idx_suppliers_name_unique ON suppliers (LOWER(name));
CREATE UNIQUE INDEX IF NOT EXISTS idx_suppliers_abn_unique ON suppliers (abn) WHERE abn IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_suppliers_email_unique ON suppliers (email) WHERE email IS NOT NULL;

-- Create supplier categories junction table
CREATE TABLE IF NOT EXISTS supplier_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_id UUID NOT NULL,
    category_id UUID NOT NULL,
    is_primary_category BOOLEAN NOT NULL DEFAULT false,
    notes TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_supplier_categories_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    CONSTRAINT fk_supplier_categories_category FOREIGN KEY (category_id) REFERENCES consumable_categories(id) ON DELETE CASCADE,
    CONSTRAINT uq_supplier_category UNIQUE (supplier_id, category_id)
);

-- Create workflow step requirements table
CREATE TABLE IF NOT EXISTS workflow_step_requirements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_step_id UUID NOT NULL,
    consumable_category_id UUID NOT NULL,
    supplier_id UUID,
    item_name VARCHAR(500) NOT NULL,
    brand VARCHAR(255),
    model VARCHAR(255),
    default_quantity DECIMAL(10,2),
    unit VARCHAR(20),
    estimated_cost DECIMAL(15,2),
    procurement_type VARCHAR(50) NOT NULL DEFAULT 'BUY',
    is_optional BOOLEAN NOT NULL DEFAULT false,
    notes TEXT,
    display_order INTEGER NOT NULL DEFAULT 1,
    supplier_item_code VARCHAR(100),
    template_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_workflow_step_requirements_workflow_step FOREIGN KEY (workflow_step_id) REFERENCES workflow_steps(id) ON DELETE CASCADE,
    CONSTRAINT fk_workflow_step_requirements_category FOREIGN KEY (consumable_category_id) REFERENCES consumable_categories(id),
    CONSTRAINT fk_workflow_step_requirements_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT chk_workflow_procurement_type CHECK (procurement_type IN ('BUY', 'PROVIDED_BY_CONTRACTOR', 'ALREADY_OWNED', 'CLIENT_PROVIDES'))
);

-- Create project step requirements table
CREATE TABLE IF NOT EXISTS project_step_requirements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_step_id UUID NOT NULL,
    workflow_step_requirement_id UUID,
    consumable_category_id UUID NOT NULL,
    supplier_id UUID,
    item_name VARCHAR(500) NOT NULL,
    brand VARCHAR(255),
    model VARCHAR(255),
    quantity DECIMAL(10,2) NOT NULL,
    unit VARCHAR(20),
    estimated_cost DECIMAL(15,2),
    actual_cost DECIMAL(15,2),
    procurement_type VARCHAR(50) NOT NULL DEFAULT 'BUY',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    is_optional BOOLEAN NOT NULL DEFAULT false,
    notes TEXT,
    display_order INTEGER NOT NULL DEFAULT 1,
    supplier_item_code VARCHAR(100),
    supplier_quote_number VARCHAR(100),
    quote_expiry_date DATE,
    required_delivery_date DATE,
    delivery_instructions TEXT,
    is_template_copied BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_project_step_requirements_project_step FOREIGN KEY (project_step_id) REFERENCES project_steps(id) ON DELETE CASCADE,
    CONSTRAINT fk_project_step_requirements_workflow_requirement FOREIGN KEY (workflow_step_requirement_id) REFERENCES workflow_step_requirements(id),
    CONSTRAINT fk_project_step_requirements_category FOREIGN KEY (consumable_category_id) REFERENCES consumable_categories(id),
    CONSTRAINT fk_project_step_requirements_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT chk_project_procurement_type CHECK (procurement_type IN ('BUY', 'PROVIDED_BY_CONTRACTOR', 'ALREADY_OWNED', 'CLIENT_PROVIDES')),
    CONSTRAINT chk_requirement_status CHECK (status IN ('PENDING', 'QUOTED', 'ORDERED', 'RECEIVED', 'INSTALLED', 'CANCELLED'))
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_consumable_categories_active_display_order ON consumable_categories (active, display_order);
CREATE INDEX IF NOT EXISTS idx_suppliers_active ON suppliers (active);
CREATE INDEX IF NOT EXISTS idx_supplier_categories_supplier ON supplier_categories (supplier_id);
CREATE INDEX IF NOT EXISTS idx_supplier_categories_category ON supplier_categories (category_id);
CREATE INDEX IF NOT EXISTS idx_workflow_step_requirements_workflow_step ON workflow_step_requirements (workflow_step_id);
CREATE INDEX IF NOT EXISTS idx_workflow_step_requirements_category ON workflow_step_requirements (consumable_category_id);
CREATE INDEX IF NOT EXISTS idx_workflow_step_requirements_supplier ON workflow_step_requirements (supplier_id);
CREATE INDEX IF NOT EXISTS idx_project_step_requirements_project_step ON project_step_requirements (project_step_id);
CREATE INDEX IF NOT EXISTS idx_project_step_requirements_workflow_requirement ON project_step_requirements (workflow_step_requirement_id);
CREATE INDEX IF NOT EXISTS idx_project_step_requirements_category ON project_step_requirements (consumable_category_id);
CREATE INDEX IF NOT EXISTS idx_project_step_requirements_supplier ON project_step_requirements (supplier_id);
CREATE INDEX IF NOT EXISTS idx_project_step_requirements_status ON project_step_requirements (status);
CREATE INDEX IF NOT EXISTS idx_project_step_requirements_template_copied ON project_step_requirements (is_template_copied);

-- Insert some default consumable categories
INSERT INTO consumable_categories (id, name, description, display_order, active, created_at, updated_at) 
SELECT * FROM (VALUES
    (gen_random_uuid(), 'Bathroom Fittings', 'Bathroom fixtures, fittings, and accessories', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Electrical Components', 'Electrical wiring, fixtures, and components', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Plumbing Materials', 'Plumbing pipes, fittings, and fixtures', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Flooring Materials', 'Flooring tiles, carpets, and installation materials', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Kitchen Appliances', 'Kitchen appliances and fixtures', 5, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Paint and Finishes', 'Paint, primers, and finishing materials', 6, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Hardware and Fasteners', 'Screws, bolts, nails, and other hardware', 7, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Insulation Materials', 'Insulation batts, foam, and related materials', 8, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Roofing Materials', 'Roof tiles, gutters, and roofing accessories', 9, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Windows and Doors', 'Windows, doors, and related hardware', 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
) AS v(id, name, description, display_order, active, created_at, updated_at)
WHERE NOT EXISTS (
    SELECT 1 FROM consumable_categories WHERE LOWER(consumable_categories.name) = LOWER(v.name)
);

-- Insert some default suppliers
INSERT INTO suppliers (id, name, supplier_type, payment_terms, active, verified, created_at, updated_at)
SELECT * FROM (VALUES
    (gen_random_uuid(), 'Bunnings Warehouse', 'RETAIL', 'NET_30', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Mitre 10', 'RETAIL', 'NET_30', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Reece Plumbing', 'SPECIALIST', 'NET_14', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Haymans Electrical', 'SPECIALIST', 'NET_14', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Caroma', 'MANUFACTURER', 'NET_30', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'Dulux', 'MANUFACTURER', 'NET_30', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
) AS v(id, name, supplier_type, payment_terms, active, verified, created_at, updated_at)
WHERE NOT EXISTS (
    SELECT 1 FROM suppliers WHERE LOWER(suppliers.name) = LOWER(v.name)
);

-- Create some default supplier-category relationships
INSERT INTO supplier_categories (id, supplier_id, category_id, is_primary_category, active, created_at, updated_at)
SELECT 
    gen_random_uuid() as id,
    s.id as supplier_id,
    c.id as category_id,
    CASE 
        WHEN s.name = 'Bunnings Warehouse' AND c.name = 'Hardware and Fasteners' THEN true
        WHEN s.name = 'Mitre 10' AND c.name = 'Hardware and Fasteners' THEN true
        WHEN s.name = 'Reece Plumbing' AND c.name = 'Plumbing Materials' THEN true
        WHEN s.name = 'Haymans Electrical' AND c.name = 'Electrical Components' THEN true
        WHEN s.name = 'Caroma' AND c.name = 'Bathroom Fittings' THEN true
        WHEN s.name = 'Dulux' AND c.name = 'Paint and Finishes' THEN true
        ELSE false
    END as is_primary_category,
    true as active,
    CURRENT_TIMESTAMP as created_at,
    CURRENT_TIMESTAMP as updated_at
FROM suppliers s
CROSS JOIN consumable_categories c
WHERE 
    (s.name = 'Bunnings Warehouse' AND c.name IN ('Bathroom Fittings', 'Electrical Components', 'Plumbing Materials', 'Flooring Materials', 'Kitchen Appliances', 'Paint and Finishes', 'Hardware and Fasteners', 'Insulation Materials', 'Roofing Materials', 'Windows and Doors')) OR
    (s.name = 'Mitre 10' AND c.name IN ('Bathroom Fittings', 'Electrical Components', 'Plumbing Materials', 'Flooring Materials', 'Kitchen Appliances', 'Paint and Finishes', 'Hardware and Fasteners', 'Insulation Materials', 'Roofing Materials', 'Windows and Doors')) OR
    (s.name = 'Reece Plumbing' AND c.name = 'Plumbing Materials') OR
    (s.name = 'Haymans Electrical' AND c.name = 'Electrical Components') OR
    (s.name = 'Caroma' AND c.name = 'Bathroom Fittings') OR
    (s.name = 'Dulux' AND c.name = 'Paint and Finishes')
    AND NOT EXISTS (
        SELECT 1 FROM supplier_categories sc2 
        WHERE sc2.supplier_id = s.id AND sc2.category_id = c.id
    );
