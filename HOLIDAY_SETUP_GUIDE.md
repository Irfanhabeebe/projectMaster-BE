# Holiday Setup Guide

Complete guide to set up NSW Australian public holidays (2025-2030) for your ProjectMaster application.

## Prerequisites

1. Database migration `V38__Create_holiday_management_tables.sql` must be run first
2. Database connection details ready
3. SUPER_USER access (if using API)

## Step-by-Step Setup

### Step 1: Create Holiday Tables (One-time)

Run the Flyway migration to create the tables:

```bash
# Migration runs automatically on application startup
# Or run manually:
mvn flyway:migrate
```

This creates:
- `master_holidays` table
- `company_holidays` table

### Step 2: Insert Master Holidays

Insert all NSW public holidays from 2025-2030 into the master_holidays table:

```bash
psql -h localhost -U your_username -d projectmaster_db -f nsw_master_holidays_2025_2030.sql
```

**What this does:**
- Inserts 66 holidays (11 per year × 6 years)
- Includes NSW-specific holidays (Bank Holiday, Labour Day)
- These will be automatically copied to NEW companies

### Step 3: Backfill Holidays to Existing Companies

Copy all master holidays to companies that already exist in the system:

```bash
psql -h localhost -U your_username -d projectmaster_db -f copy_master_holidays_to_existing_companies.sql
```

**What this does:**
- Loops through all active companies
- Copies all master holidays to each company
- Skips holidays that already exist (safe to run multiple times)
- Shows progress with NOTICE messages
- Displays summary at the end

### Step 4: Verify

Check the results in the output of Step 3, or run:

```sql
-- Check master holidays count
SELECT holiday_year, COUNT(*) as holiday_count 
FROM master_holidays 
GROUP BY holiday_year 
ORDER BY holiday_year;

-- Check company holidays count per company
SELECT 
    c.name AS company_name,
    COUNT(ch.id) AS holiday_count
FROM companies c
LEFT JOIN company_holidays ch ON c.id = ch.company_id
WHERE c.active = true
GROUP BY c.id, c.name
ORDER BY c.name;
```

## Expected Results

After running all scripts:

- ✅ **Master Holidays**: 66 holidays (2025-2030)
- ✅ **Company Holidays**: 66 holidays per company
- ✅ **New Companies**: Auto-receive all master holidays on creation

## Script Details

### nsw_master_holidays_2025_2030.sql

**Purpose**: Populate master_holidays table  
**Records**: 66 holidays  
**Safe to rerun**: No (will create duplicates)  
**When to use**: Once during initial setup

**Holidays included (per year):**
1. New Year's Day (Jan 1)
2. Australia Day (Jan 26)
3. Good Friday (variable)
4. Easter Saturday (variable)
5. Easter Monday (variable)
6. Anzac Day (Apr 25)
7. King's Birthday (variable - 2nd Mon in June)
8. Bank Holiday (variable - 1st Mon in August) - NSW
9. Labour Day (variable - 1st Mon in October) - NSW
10. Christmas Day (Dec 25)
11. Boxing Day (Dec 26)

### copy_master_holidays_to_existing_companies.sql

**Purpose**: Backfill holidays to existing companies  
**Records**: 66 holidays × number of active companies  
**Safe to rerun**: Yes (checks for duplicates)  
**When to use**: 
- After inserting master holidays
- When you add more master holidays later
- To sync holidays to companies

**Features:**
- ✅ Only processes active companies
- ✅ Skips holidays that already exist
- ✅ Shows progress with NOTICE messages
- ✅ Displays verification query at the end
- ✅ Safe to run multiple times

## Example Output

When you run `copy_master_holidays_to_existing_companies.sql`, you'll see:

```
NOTICE:  Starting holiday copy process...
NOTICE:  Found 3 active companies
NOTICE:  Processing company: ABC Construction (ID: 123e4567-e89b-12d3-a456-426614174000)
NOTICE:    -> Copied 66 holidays to company: ABC Construction
NOTICE:  Processing company: XYZ Builders (ID: 223e4567-e89b-12d3-a456-426614174001)
NOTICE:    -> Copied 66 holidays to company: XYZ Builders
NOTICE:  Processing company: DEF Projects (ID: 323e4567-e89b-12d3-a456-426614174002)
NOTICE:    -> Copied 66 holidays to company: DEF Projects
NOTICE:  ========================================
NOTICE:  Holiday copy process completed!
NOTICE:  Total companies processed: 3
NOTICE:  Total holidays copied: 198
NOTICE:  ========================================

-- Then shows verification results
```

## Future Updates

To add holidays for future years (e.g., 2031-2035):

1. Create new SQL file with additional holidays
2. Run it to insert into master_holidays table
3. Run `copy_master_holidays_to_existing_companies.sql` again
4. All companies will receive the new holidays

## Maintenance

### Adding a Holiday

```sql
INSERT INTO master_holidays (id, holiday_year, holiday_date, holiday_name, description, created_at, updated_at)
VALUES (gen_random_uuid(), 2031, '2031-01-01', 'New Year''s Day', 'First day of the calendar year', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Then copy to all companies
-- Run: copy_master_holidays_to_existing_companies.sql
```

### Updating a Holiday

Use the API with SUPER_USER credentials:

```bash
curl -X PUT http://localhost:8080/api/holidays/master \
  -H "Authorization: Bearer <super-user-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "holidayYear": 2025,
    "holidays": [...]
  }'
```

## Troubleshooting

### Problem: Holidays not copied to new companies

**Solution**: Check if auto-copy is working in CompanyService and SuperUserService

### Problem: Duplicate holidays error

**Solution**: The unique constraint prevents duplicates. Clean up first:

```sql
DELETE FROM company_holidays WHERE company_id = 'company-uuid' AND holiday_year = 2025;
```

### Problem: Need to reset all holidays

```sql
-- Remove all company holidays
TRUNCATE TABLE company_holidays;

-- Remove all master holidays
TRUNCATE TABLE master_holidays;

-- Start fresh from Step 2
```

## API Alternative

Instead of SQL scripts, you can use the API (SUPER_USER only):

```bash
# Upload holidays via API
curl -X PUT http://localhost:8080/api/holidays/master \
  -H "Authorization: Bearer <super-user-token>" \
  -H "Content-Type: application/json" \
  -d @holidays_2025.json
```

But SQL scripts are faster for bulk operations!

## Summary

| Script | Purpose | Run Frequency |
|--------|---------|---------------|
| `V38__Create_holiday_management_tables.sql` | Create tables | Once (automatic) |
| `nsw_master_holidays_2025_2030.sql` | Insert master holidays | Once initially |
| `copy_master_holidays_to_existing_companies.sql` | Backfill to companies | As needed |

✅ **Recommended Order**: Step 1 → Step 2 → Step 3 → Step 4 (Verify)

