-- Create crew table
CREATE TABLE crew (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL,
    user_id UUID NOT NULL,
    employee_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    mobile VARCHAR(50),
    date_of_birth DATE,
    address TEXT,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(50),
    emergency_contact_relationship VARCHAR(50),
    tax_file_number VARCHAR(100),
    superannuation_fund VARCHAR(100),
    superannuation_member_number VARCHAR(100),
    bank_account_name VARCHAR(100),
    bank_account_bsb VARCHAR(10),
    bank_account_number VARCHAR(20),
    hire_date DATE NOT NULL,
    termination_date DATE,
    position VARCHAR(100),
    department VARCHAR(100),
    hourly_rate DECIMAL(10,2),
    annual_salary DECIMAL(12,2),
    active BOOLEAN NOT NULL DEFAULT true,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_crew_company FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE,
    CONSTRAINT fk_crew_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_crew_company_id ON crew(company_id);
CREATE INDEX idx_crew_user_id ON crew(user_id);
CREATE INDEX idx_crew_email ON crew(email);
CREATE INDEX idx_crew_employee_id ON crew(employee_id);
CREATE INDEX idx_crew_active ON crew(active);
CREATE INDEX idx_crew_department ON crew(department);
CREATE INDEX idx_crew_position ON crew(position);
CREATE INDEX idx_crew_hire_date ON crew(hire_date);

-- Add comments for documentation
COMMENT ON TABLE crew IS 'Stores information about company crew members (tradies)';
COMMENT ON COLUMN crew.id IS 'Unique identifier for the crew member';
COMMENT ON COLUMN crew.company_id IS 'Reference to the company that employs this crew member';
COMMENT ON COLUMN crew.user_id IS 'Reference to the user account associated with this crew member';
COMMENT ON COLUMN crew.employee_id IS 'Company-specific employee identifier';
COMMENT ON COLUMN crew.first_name IS 'Crew member first name';
COMMENT ON COLUMN crew.last_name IS 'Crew member last name';
COMMENT ON COLUMN crew.email IS 'Crew member email address (must be unique)';
COMMENT ON COLUMN crew.phone IS 'Crew member phone number';
COMMENT ON COLUMN crew.mobile IS 'Crew member mobile number';
COMMENT ON COLUMN crew.date_of_birth IS 'Crew member date of birth';
COMMENT ON COLUMN crew.address IS 'Crew member address';
COMMENT ON COLUMN crew.emergency_contact_name IS 'Name of emergency contact';
COMMENT ON COLUMN crew.emergency_contact_phone IS 'Phone number of emergency contact';
COMMENT ON COLUMN crew.emergency_contact_relationship IS 'Relationship to emergency contact';
COMMENT ON COLUMN crew.tax_file_number IS 'Australian Tax File Number';
COMMENT ON COLUMN crew.superannuation_fund IS 'Superannuation fund name';
COMMENT ON COLUMN crew.superannuation_member_number IS 'Superannuation fund member number';
COMMENT ON COLUMN crew.bank_account_name IS 'Bank account holder name';
COMMENT ON COLUMN crew.bank_account_bsb IS 'Bank BSB code';
COMMENT ON COLUMN crew.bank_account_number IS 'Bank account number';
COMMENT ON COLUMN crew.hire_date IS 'Date when crew member was hired';
COMMENT ON COLUMN crew.termination_date IS 'Date when crew member was terminated (null if still employed)';
COMMENT ON COLUMN crew.position IS 'Job position/title';
COMMENT ON COLUMN crew.department IS 'Department within the company';
COMMENT ON COLUMN crew.hourly_rate IS 'Hourly pay rate';
COMMENT ON COLUMN crew.annual_salary IS 'Annual salary';
COMMENT ON COLUMN crew.active IS 'Whether the crew member is currently active';
COMMENT ON COLUMN crew.notes IS 'Additional notes about the crew member';
COMMENT ON COLUMN crew.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN crew.updated_at IS 'Timestamp when the record was last updated';
