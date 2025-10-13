-- Create master_holidays table
CREATE TABLE master_holidays (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    holiday_year INTEGER NOT NULL,
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_master_holidays_year_date UNIQUE (holiday_year, holiday_date)
);

-- Create company_holidays table
CREATE TABLE company_holidays (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    holiday_year INTEGER NOT NULL,
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_company_holidays_company_year_date UNIQUE (company_id, holiday_year, holiday_date)
);

-- Create indexes for better query performance
CREATE INDEX idx_master_holidays_year ON master_holidays(holiday_year);
CREATE INDEX idx_master_holidays_date ON master_holidays(holiday_date);
CREATE INDEX idx_company_holidays_company_year ON company_holidays(company_id, holiday_year);
CREATE INDEX idx_company_holidays_date ON company_holidays(holiday_date);

-- Add comments for documentation
COMMENT ON TABLE master_holidays IS 'Master/system-level holidays that apply globally';
COMMENT ON TABLE company_holidays IS 'Company-specific holidays that apply to individual companies';
COMMENT ON COLUMN master_holidays.holiday_year IS 'Year of the holiday';
COMMENT ON COLUMN master_holidays.holiday_date IS 'Date of the holiday';
COMMENT ON COLUMN company_holidays.company_id IS 'Reference to the company';
COMMENT ON COLUMN company_holidays.holiday_year IS 'Year of the holiday';
COMMENT ON COLUMN company_holidays.holiday_date IS 'Date of the holiday';

