-- V10: Create contracting company tables
-- This migration creates tables for managing contracting companies and their relationships

-- Enable uuid-ossp extension if it doesn't exist
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create contracting companies table
CREATE TABLE contracting_companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL UNIQUE,
    address TEXT NOT NULL,
    abn VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    contact_person VARCHAR(255),
    active BOOLEAN DEFAULT true,
    verified BOOLEAN DEFAULT false,
    created_by_user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create contracting company specialties table
CREATE TABLE contracting_company_specialties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    contracting_company_id UUID NOT NULL REFERENCES contracting_companies(id) ON DELETE CASCADE,
    specialty_id UUID NOT NULL REFERENCES specialties(id),
    active BOOLEAN DEFAULT true,
    years_experience INTEGER,
    certification_details TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create contracting company users table
CREATE TABLE contracting_company_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    contracting_company_id UUID NOT NULL REFERENCES contracting_companies(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT true,
    assigned_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create builder contractor relationships table
CREATE TABLE builder_contractor_relationships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    builder_company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    contracting_company_id UUID NOT NULL REFERENCES contracting_companies(id) ON DELETE CASCADE,
    added_by_user_id UUID NOT NULL REFERENCES users(id),
    active BOOLEAN DEFAULT true,
    contract_start_date DATE,
    contract_end_date DATE,
    payment_terms TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create builder contractor specialties table
CREATE TABLE builder_contractor_specialties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    builder_contractor_relationship_id UUID NOT NULL REFERENCES builder_contractor_relationships(id) ON DELETE CASCADE,
    specialty_id UUID NOT NULL REFERENCES specialties(id),
    active BOOLEAN DEFAULT true,
    custom_notes TEXT,
    preferred_rating INTEGER CHECK (preferred_rating >= 1 AND preferred_rating <= 5),
    hourly_rate DECIMAL(10,2),
    availability_status VARCHAR(20) DEFAULT 'available' CHECK (availability_status IN ('available', 'busy', 'unavailable')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create project step assignments table
CREATE TABLE project_step_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_step_id UUID NOT NULL REFERENCES project_steps(id) ON DELETE CASCADE,
    contracting_company_id UUID NOT NULL REFERENCES contracting_companies(id),
    assigned_by_user_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    assigned_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_date TIMESTAMP WITH TIME ZONE,
    declined_date TIMESTAMP WITH TIME ZONE,
    decline_reason TEXT,
    start_date TIMESTAMP WITH TIME ZONE,
    estimated_completion_date TIMESTAMP WITH TIME ZONE,
    actual_completion_date TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    hourly_rate DECIMAL(10,2),
    total_hours INTEGER,
    total_cost DECIMAL(15,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_contracting_companies_name ON contracting_companies(name);
CREATE INDEX idx_contracting_companies_abn ON contracting_companies(abn);
CREATE INDEX idx_contracting_companies_email ON contracting_companies(email);
CREATE INDEX idx_contracting_companies_active ON contracting_companies(active);
CREATE INDEX idx_contracting_companies_created_by ON contracting_companies(created_by_user_id);

CREATE INDEX idx_contracting_company_specialties_company_id ON contracting_company_specialties(contracting_company_id);
CREATE INDEX idx_contracting_company_specialties_specialty_id ON contracting_company_specialties(specialty_id);
CREATE INDEX idx_contracting_company_specialties_active ON contracting_company_specialties(active);

CREATE INDEX idx_contracting_company_users_company_id ON contracting_company_users(contracting_company_id);
CREATE INDEX idx_contracting_company_users_user_id ON contracting_company_users(user_id);
CREATE INDEX idx_contracting_company_users_active ON contracting_company_users(active);

CREATE INDEX idx_builder_contractor_relationships_builder_id ON builder_contractor_relationships(builder_company_id);
CREATE INDEX idx_builder_contractor_relationships_contractor_id ON builder_contractor_relationships(contracting_company_id);
CREATE INDEX idx_builder_contractor_relationships_active ON builder_contractor_relationships(active);

CREATE INDEX idx_builder_contractor_specialties_relationship_id ON builder_contractor_specialties(builder_contractor_relationship_id);
CREATE INDEX idx_builder_contractor_specialties_specialty_id ON builder_contractor_specialties(specialty_id);

CREATE INDEX idx_project_step_assignments_step_id ON project_step_assignments(project_step_id);
CREATE INDEX idx_project_step_assignments_contractor_id ON project_step_assignments(contracting_company_id);
CREATE INDEX idx_project_step_assignments_status ON project_step_assignments(status);
CREATE INDEX idx_project_step_assignments_assigned_date ON project_step_assignments(assigned_date);

-- Create unique constraints
CREATE UNIQUE INDEX idx_contracting_company_specialties_unique ON contracting_company_specialties(contracting_company_id, specialty_id);
CREATE UNIQUE INDEX idx_contracting_company_users_unique ON contracting_company_users(contracting_company_id, user_id);
CREATE UNIQUE INDEX idx_builder_contractor_relationships_unique ON builder_contractor_relationships(builder_company_id, contracting_company_id);
CREATE UNIQUE INDEX idx_builder_contractor_specialties_unique ON builder_contractor_specialties(builder_contractor_relationship_id, specialty_id);
CREATE UNIQUE INDEX idx_project_step_assignments_unique ON project_step_assignments(project_step_id, contracting_company_id);

-- Add comments for documentation
COMMENT ON TABLE contracting_companies IS 'Master list of contracting companies (tradies) that can be hired by builders';
COMMENT ON TABLE contracting_company_specialties IS 'Specialties that each contracting company offers';
COMMENT ON TABLE contracting_company_users IS 'Users associated with contracting companies';
COMMENT ON TABLE builder_contractor_relationships IS 'Relationships between builder companies and contracting companies';
COMMENT ON TABLE builder_contractor_specialties IS 'Customizable specialties for each builder-contractor relationship';
COMMENT ON TABLE project_step_assignments IS 'Assignments of project steps to contracting companies';
