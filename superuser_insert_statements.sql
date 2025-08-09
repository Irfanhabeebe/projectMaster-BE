-- INSERT statements to create a superuser with admin access
-- Username: superuser@pm.com
-- Password: Password@123 (hashed)

-- OPTION 1: Enable uuid-ossp extension (if not already enabled)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- OPTION 2: Use PostgreSQL's built-in gen_random_uuid() (PostgreSQL 13+)
-- If you get an error with uuid_generate_v4(), use gen_random_uuid() instead

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
    gen_random_uuid(),
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
    gen_random_uuid(),
    (SELECT id FROM companies WHERE name = 'ProjectMaster Admin Company' LIMIT 1),
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