-- Add contracting company support to users table
ALTER TABLE users ADD COLUMN contracting_company_id UUID;
ALTER TABLE users ADD CONSTRAINT fk_users_contracting_company FOREIGN KEY (contracting_company_id) REFERENCES contracting_companies(id);

-- Create index for better performance
CREATE INDEX idx_users_contracting_company_id ON users(contracting_company_id);

-- Update existing users to ensure company_id is nullable (since users can now be associated with either type)
-- This is already the case based on the existing schema, but documenting it here
