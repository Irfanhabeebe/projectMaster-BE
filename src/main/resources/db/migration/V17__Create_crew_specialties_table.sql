-- Create crew_specialties table
CREATE TABLE crew_specialties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crew_id UUID NOT NULL,
    specialty_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    custom_notes TEXT,
    proficiency_rating INTEGER CHECK (proficiency_rating >= 1 AND proficiency_rating <= 5),
    hourly_rate DECIMAL(10,2),
    availability_status VARCHAR(50),
    years_experience INTEGER,
    certifications TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_crew_specialties_crew_id FOREIGN KEY (crew_id) REFERENCES crew(id) ON DELETE CASCADE,
    CONSTRAINT fk_crew_specialties_specialty_id FOREIGN KEY (specialty_id) REFERENCES specialties(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_crew_specialties_crew_id ON crew_specialties(crew_id);
CREATE INDEX idx_crew_specialties_specialty_id ON crew_specialties(specialty_id);
CREATE INDEX idx_crew_specialties_active ON crew_specialties(active);
CREATE INDEX idx_crew_specialties_availability_status ON crew_specialties(availability_status);

-- Add comments
COMMENT ON TABLE crew_specialties IS 'Stores the specialties and skills of crew members';
COMMENT ON COLUMN crew_specialties.id IS 'Unique identifier for the crew specialty';
COMMENT ON COLUMN crew_specialties.crew_id IS 'Reference to the crew member';
COMMENT ON COLUMN crew_specialties.specialty_id IS 'Reference to the specialty';
COMMENT ON COLUMN crew_specialties.active IS 'Whether this specialty is currently active for the crew member';
COMMENT ON COLUMN crew_specialties.custom_notes IS 'Custom notes about the crew member''s expertise in this specialty';
COMMENT ON COLUMN crew_specialties.proficiency_rating IS 'Rating from 1-5 indicating crew member''s skill level in this specialty';
COMMENT ON COLUMN crew_specialties.hourly_rate IS 'Hourly rate for this specific specialty';
COMMENT ON COLUMN crew_specialties.availability_status IS 'Current availability status for this specialty';
COMMENT ON COLUMN crew_specialties.years_experience IS 'Years of experience in this specialty';
COMMENT ON COLUMN crew_specialties.certifications IS 'Relevant certifications for this specialty';
COMMENT ON COLUMN crew_specialties.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN crew_specialties.updated_at IS 'Timestamp when the record was last updated';
