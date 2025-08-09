-- Add SUPER_USER to the user_role enum
ALTER TYPE user_role ADD VALUE 'SUPER_USER';

-- Update users table to allow company_id to be nullable for super users
ALTER TABLE users ALTER COLUMN company_id DROP NOT NULL;

-- Add constraint to ensure only SUPER_USER role can have null company_id
ALTER TABLE users ADD CONSTRAINT chk_super_user_company 
    CHECK (
        (role = 'SUPER_USER' AND company_id IS NULL) OR 
        (role != 'SUPER_USER' AND company_id IS NOT NULL)
    );

-- Create index for super users
CREATE INDEX idx_users_super_user ON users(role) WHERE role = 'SUPER_USER';