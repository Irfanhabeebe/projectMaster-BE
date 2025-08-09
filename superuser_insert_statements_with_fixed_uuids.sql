-- INSERT statements to create a superuser with admin access
-- Username: superuser@pm.com
-- Password: Password@123 (hashed)
-- This version uses fixed UUIDs to avoid UUID generation function issues

-- First, insert a company for the superuser
INSERT INTO companies (
    id,
    name,
    address,
    phone,
    email,
    website,
    tax_number,
    active,
    created_at,
    updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'ProjectMaster Admin Company',
    'Admin Office',
    '+1-000-000-0000',
    'admin@projectmaster.com',
    'https://projectmaster.com',
    'ADMIN-TAX-001',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert the superuser with ADMIN role
INSERT INTO users (
    id,
    company_id,
    email,
    password_hash,
    first_name,
    last_name,
    phone,
    role,
    active,
    email_verified,
    last_login_at,
    created_at,
    updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440000',
    'superuser@pm.com',
    'DbxaNIw+9bVlM9buIRvIGfLt7xsaY2HqDG+UWzAkI4tLPXmlqIOx+CWtKdEULz3b',
    'Super',
    'User',
    '+1-000-000-0001',
    'ADMIN',
    true,
    true,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Verification query to check if the superuser was created successfully
-- SELECT u.id, u.email, u.first_name, u.last_name, u.role, u.active, c.name as company_name
-- FROM users u
-- JOIN companies c ON u.company_id = c.id
-- WHERE u.email = 'superuser@pm.com';