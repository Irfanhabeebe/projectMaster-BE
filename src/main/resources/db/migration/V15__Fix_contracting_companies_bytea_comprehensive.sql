-- V15: Comprehensive fix for contracting companies bytea data issue
-- This migration will properly convert binary data to text and ensure correct column types

-- Step 1: Check current data types and identify problematic data
-- First, let's see what we're working with
DO $$ 
DECLARE
    rec RECORD;
BEGIN
    -- Log current column types
    RAISE NOTICE 'Current column types in contracting_companies:';
    FOR rec IN 
        SELECT column_name, data_type, character_maximum_length 
        FROM information_schema.columns 
        WHERE table_name = 'contracting_companies' 
        AND column_name IN ('name', 'abn', 'email', 'phone', 'contact_person', 'address')
        ORDER BY column_name
    LOOP
        RAISE NOTICE 'Column: %, Type: %, Max Length: %', rec.column_name, rec.data_type, rec.character_maximum_length;
    END LOOP;
END $$;

-- Step 2: Create backup of current data
CREATE TABLE IF NOT EXISTS contracting_companies_backup AS 
SELECT * FROM contracting_companies;

-- Step 3: Identify and fix bytea data
-- Check if we have any binary data (where octet_length != char_length for text fields)
UPDATE contracting_companies 
SET name = CASE 
    WHEN name IS NOT NULL AND octet_length(name) != char_length(name) 
    THEN convert_from(name::bytea, 'UTF8')
    ELSE name
END;

UPDATE contracting_companies 
SET abn = CASE 
    WHEN abn IS NOT NULL AND octet_length(abn) != char_length(abn) 
    THEN convert_from(abn::bytea, 'UTF8')
    ELSE abn
END;

UPDATE contracting_companies 
SET email = CASE 
    WHEN email IS NOT NULL AND octet_length(email) != char_length(email) 
    THEN convert_from(email::bytea, 'UTF8')
    ELSE email
END;

UPDATE contracting_companies 
SET phone = CASE 
    WHEN phone IS NOT NULL AND octet_length(phone) != char_length(phone) 
    THEN convert_from(phone::bytea, 'UTF8')
    ELSE phone
END;

UPDATE contracting_companies 
SET contact_person = CASE 
    WHEN contact_person IS NOT NULL AND octet_length(contact_person) != char_length(contact_person) 
    THEN convert_from(contact_person::bytea, 'UTF8')
    ELSE contact_person
END;

UPDATE contracting_companies 
SET address = CASE 
    WHEN address IS NOT NULL AND octet_length(address) != char_length(address) 
    THEN convert_from(address::bytea, 'UTF8')
    ELSE address
END;

-- Step 4: Force column type conversion to ensure proper typing
-- Drop and recreate columns with correct types
ALTER TABLE contracting_companies 
    ALTER COLUMN name TYPE VARCHAR(255) USING COALESCE(name::VARCHAR(255), ''),
    ALTER COLUMN abn TYPE VARCHAR(11) USING COALESCE(abn::VARCHAR(11), ''),
    ALTER COLUMN email TYPE VARCHAR(255) USING COALESCE(email::VARCHAR(255), ''),
    ALTER COLUMN phone TYPE VARCHAR(50) USING COALESCE(phone::VARCHAR(50), ''),
    ALTER COLUMN contact_person TYPE VARCHAR(255) USING COALESCE(contact_person::VARCHAR(255), ''),
    ALTER COLUMN address TYPE TEXT USING COALESCE(address::TEXT, '');

-- Step 5: Clean up any remaining issues and set proper constraints
-- Ensure NOT NULL constraints for required fields
ALTER TABLE contracting_companies 
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN abn SET NOT NULL,
    ALTER COLUMN email SET NOT NULL;

-- Step 6: Refresh table statistics
REINDEX TABLE contracting_companies;
ANALYZE contracting_companies;

-- Step 7: Verify the fix worked
DO $$ 
DECLARE
    rec RECORD;
    issue_count INTEGER := 0;
BEGIN
    -- Check for any remaining bytea issues
    FOR rec IN 
        SELECT id, name, abn, email, phone, contact_person
        FROM contracting_companies 
        WHERE (name IS NOT NULL AND octet_length(name) != char_length(name))
           OR (abn IS NOT NULL AND octet_length(abn) != char_length(abn))
           OR (email IS NOT NULL AND octet_length(email) != char_length(email))
           OR (phone IS NOT NULL AND octet_length(phone) != char_length(phone))
           OR (contact_person IS NOT NULL AND octet_length(contact_person) != char_length(contact_person))
    LOOP
        RAISE WARNING 'Still have bytea issue in contracting_companies id: %', rec.id;
        issue_count := issue_count + 1;
    END LOOP;
    
    IF issue_count = 0 THEN
        RAISE NOTICE 'SUCCESS: All bytea issues have been resolved in contracting_companies table';
    ELSE
        RAISE NOTICE 'WARNING: % records still have bytea issues', issue_count;
    END IF;
END $$;

-- Optional: Drop backup table after verification (uncomment if needed)
-- DROP TABLE IF EXISTS contracting_companies_backup;

