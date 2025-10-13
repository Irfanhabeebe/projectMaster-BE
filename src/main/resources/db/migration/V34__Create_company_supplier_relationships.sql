-- Migration V34: Create Company Supplier Relationship Tables
-- This migration implements the supplier relationship pattern similar to builder-contractor relationships

-- Update suppliers table to support the new relationship model
-- Rename payment_terms to default_payment_terms
ALTER TABLE suppliers RENAME COLUMN payment_terms TO default_payment_terms;

-- Remove company-specific credit_limit (now in relationships)
ALTER TABLE suppliers DROP COLUMN IF EXISTS credit_limit;

-- Create company_supplier_relationships table
CREATE TABLE IF NOT EXISTS company_supplier_relationships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    supplier_id UUID NOT NULL,
    added_by_user_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    preferred BOOLEAN NOT NULL DEFAULT false,
    account_number VARCHAR(100),
    payment_terms VARCHAR(50),
    credit_limit DECIMAL(15,2),
    discount_rate DECIMAL(5,2),
    contract_start_date DATE,
    contract_end_date DATE,
    delivery_instructions TEXT,
    notes TEXT,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_company_supplier_rel_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_company_supplier_rel_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    CONSTRAINT fk_company_supplier_rel_user FOREIGN KEY (added_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT uq_company_supplier UNIQUE (company_id, supplier_id),
    CONSTRAINT chk_payment_terms CHECK (payment_terms IN ('COD', 'NET_7', 'NET_14', 'NET_30', 'NET_60', 'PREPAID'))
);

-- Create company_supplier_categories table
CREATE TABLE IF NOT EXISTS company_supplier_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_supplier_relationship_id UUID NOT NULL,
    consumable_category_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    is_primary_category BOOLEAN NOT NULL DEFAULT false,
    minimum_order_value DECIMAL(15,2),
    estimated_annual_spend DECIMAL(15,2),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_company_supplier_cat_rel FOREIGN KEY (company_supplier_relationship_id) REFERENCES company_supplier_relationships(id) ON DELETE CASCADE,
    CONSTRAINT fk_company_supplier_cat_category FOREIGN KEY (consumable_category_id) REFERENCES consumable_categories(id) ON DELETE RESTRICT,
    CONSTRAINT uq_company_supplier_category UNIQUE (company_supplier_relationship_id, consumable_category_id)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_company_supplier_rel_company ON company_supplier_relationships (company_id);
CREATE INDEX IF NOT EXISTS idx_company_supplier_rel_supplier ON company_supplier_relationships (supplier_id);
CREATE INDEX IF NOT EXISTS idx_company_supplier_rel_active ON company_supplier_relationships (active);
CREATE INDEX IF NOT EXISTS idx_company_supplier_rel_preferred ON company_supplier_relationships (preferred);
CREATE INDEX IF NOT EXISTS idx_company_supplier_rel_rating ON company_supplier_relationships (rating);
CREATE INDEX IF NOT EXISTS idx_company_supplier_rel_contract_end ON company_supplier_relationships (contract_end_date);

CREATE INDEX IF NOT EXISTS idx_company_supplier_cat_rel ON company_supplier_categories (company_supplier_relationship_id);
CREATE INDEX IF NOT EXISTS idx_company_supplier_cat_category ON company_supplier_categories (consumable_category_id);
CREATE INDEX IF NOT EXISTS idx_company_supplier_cat_primary ON company_supplier_categories (is_primary_category);
CREATE INDEX IF NOT EXISTS idx_company_supplier_cat_active ON company_supplier_categories (active);

-- Add comments for documentation
COMMENT ON TABLE company_supplier_relationships IS 'Company-specific relationships with suppliers, similar to builder_contractor_relationships pattern';
COMMENT ON TABLE company_supplier_categories IS 'Preferred categories for company-supplier relationships, similar to builder_contractor_specialties pattern';

COMMENT ON COLUMN company_supplier_relationships.preferred IS 'Mark supplier as preferred for this company';
COMMENT ON COLUMN company_supplier_relationships.account_number IS 'Company account number with this supplier';
COMMENT ON COLUMN company_supplier_relationships.discount_rate IS 'Negotiated discount percentage for this company';
COMMENT ON COLUMN company_supplier_relationships.rating IS 'Company rating for this supplier (1-5 stars)';

COMMENT ON COLUMN suppliers.default_payment_terms IS 'Default payment terms (can be overridden in company relationships)';

