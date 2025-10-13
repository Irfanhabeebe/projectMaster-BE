-- ============================================
-- Copy Master Holidays to All Existing Companies
-- ============================================
-- This script loops through all companies and copies all master holidays to each company
-- Useful for backfilling holidays to companies created before master holidays were added
-- ============================================

DO $$
DECLARE
    company_record RECORD;
    master_holiday_record RECORD;
    total_companies INTEGER := 0;
    total_holidays_copied INTEGER := 0;
    holidays_per_company INTEGER := 0;
BEGIN
    -- Get count of companies
    SELECT COUNT(*) INTO total_companies FROM companies WHERE active = true;
    
    RAISE NOTICE 'Starting holiday copy process...';
    RAISE NOTICE 'Found % active companies', total_companies;
    
    -- Loop through each active company
    FOR company_record IN 
        SELECT id, name FROM companies WHERE active = true
    LOOP
        RAISE NOTICE 'Processing company: % (ID: %)', company_record.name, company_record.id;
        
        holidays_per_company := 0;
        
        -- Loop through each master holiday
        FOR master_holiday_record IN 
            SELECT 
                holiday_year,
                holiday_date,
                holiday_name,
                description
            FROM master_holidays
            ORDER BY holiday_year, holiday_date
        LOOP
            -- Check if this holiday already exists for this company
            IF NOT EXISTS (
                SELECT 1 
                FROM company_holidays 
                WHERE company_id = company_record.id 
                AND holiday_year = master_holiday_record.holiday_year 
                AND holiday_date = master_holiday_record.holiday_date
            ) THEN
                -- Insert the holiday for this company
                INSERT INTO company_holidays (
                    id,
                    company_id,
                    holiday_year,
                    holiday_date,
                    holiday_name,
                    description,
                    created_at,
                    updated_at
                ) VALUES (
                    gen_random_uuid(),
                    company_record.id,
                    master_holiday_record.holiday_year,
                    master_holiday_record.holiday_date,
                    master_holiday_record.holiday_name,
                    master_holiday_record.description,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP
                );
                
                holidays_per_company := holidays_per_company + 1;
                total_holidays_copied := total_holidays_copied + 1;
            END IF;
        END LOOP;
        
        RAISE NOTICE '  -> Copied % holidays to company: %', holidays_per_company, company_record.name;
    END LOOP;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Holiday copy process completed!';
    RAISE NOTICE 'Total companies processed: %', total_companies;
    RAISE NOTICE 'Total holidays copied: %', total_holidays_copied;
    RAISE NOTICE '========================================';
    
END $$;

-- Verify the results
SELECT 
    c.name AS company_name,
    COUNT(ch.id) AS holiday_count,
    MIN(ch.holiday_date) AS earliest_holiday,
    MAX(ch.holiday_date) AS latest_holiday
FROM companies c
LEFT JOIN company_holidays ch ON c.id = ch.company_id
WHERE c.active = true
GROUP BY c.id, c.name
ORDER BY c.name;

