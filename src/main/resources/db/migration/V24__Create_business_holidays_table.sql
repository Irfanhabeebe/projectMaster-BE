-- Create business holidays table for Australian public holidays
-- This supports the Business Calendar Service for project scheduling

CREATE TABLE business_holidays (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    holiday_date DATE NOT NULL,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_type VARCHAR(50) NOT NULL,
    state_code VARCHAR(10), -- NULL for national holidays, state code for state-specific
    is_fixed_date BOOLEAN NOT NULL DEFAULT true, -- false for holidays like Easter that move each year
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_business_holidays_date ON business_holidays(holiday_date);
CREATE INDEX idx_business_holidays_state ON business_holidays(state_code);
CREATE INDEX idx_business_holidays_type ON business_holidays(holiday_type);
CREATE INDEX idx_business_holidays_year ON business_holidays(EXTRACT(YEAR FROM holiday_date));

-- Add comments
COMMENT ON TABLE business_holidays IS 'Australian public holidays for business day calculations';
COMMENT ON COLUMN business_holidays.holiday_date IS 'The date of the holiday';
COMMENT ON COLUMN business_holidays.holiday_name IS 'Display name of the holiday';
COMMENT ON COLUMN business_holidays.holiday_type IS 'NATIONAL, STATE, or REGIONAL';
COMMENT ON COLUMN business_holidays.state_code IS 'Australian state/territory code (NSW, VIC, QLD, etc.)';
COMMENT ON COLUMN business_holidays.is_fixed_date IS 'True for fixed dates like Jan 1, false for calculated dates like Easter';

