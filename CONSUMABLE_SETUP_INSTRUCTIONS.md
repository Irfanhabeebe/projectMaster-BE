# Consumable Management System Setup Instructions

## Current Situation
Your database has the old version of the consumable tables with `created_by_user_id` columns that don't match the entity definitions.

## Solution: Clean Setup

### Option 1: Using Flyway (Recommended)

1. **Drop the existing tables:**
   ```sql
   DROP TABLE IF EXISTS project_step_requirements CASCADE;
   DROP TABLE IF EXISTS workflow_step_requirements CASCADE;
   DROP TABLE IF EXISTS supplier_categories CASCADE;
   DROP TABLE IF EXISTS suppliers CASCADE;
   DROP TABLE IF EXISTS consumable_categories CASCADE;
   ```

2. **Restart your Spring Boot application**
   - Flyway will automatically detect that V33 migration needs to run
   - It will create all tables with the correct schema

3. **Run the data inserts:**
   ```bash
   # Insert Australian categories (55 categories)
   psql -U your_user -d your_database -f australian_residential_consumable_categories.sql
   
   # Insert Sydney suppliers (35 suppliers + relationships)
   psql -U your_user -d your_database -f sydney_suppliers_insert_statements.sql
   ```

### Option 2: Manual Setup

1. **Drop existing tables:**
   ```bash
   psql -U your_user -d your_database -f drop_consumable_tables.sql
   ```

2. **Run the migration script manually:**
   ```bash
   # The V33 migration creates tables WITHOUT created_by_user_id
   psql -U your_user -d your_database -f src/main/resources/db/migration/V33__Create_consumable_management_tables.sql
   ```

3. **Run the data inserts:**
   ```bash
   psql -U your_user -d your_database -f australian_residential_consumable_categories.sql
   psql -U your_user -d your_database -f sydney_suppliers_insert_statements.sql
   ```

## Correct Table Schema

The correct schema (matching the entities) should have **ONLY** these columns:

### consumable_categories
```sql
- id (UUID)
- name (VARCHAR)
- description (TEXT)
- icon (VARCHAR)
- display_order (INTEGER)
- active (BOOLEAN)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### suppliers
```sql
- id (UUID)
- name (VARCHAR)
- address (TEXT)
- abn (VARCHAR)
- email (VARCHAR)
- phone (VARCHAR)
- contact_person (VARCHAR)
- website (VARCHAR)
- supplier_type (VARCHAR)
- payment_terms (VARCHAR)
- credit_limit (DECIMAL)
- active (BOOLEAN)
- verified (BOOLEAN)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### supplier_categories
```sql
- id (UUID)
- supplier_id (UUID FK)
- category_id (UUID FK)
- is_primary_category (BOOLEAN)
- notes (TEXT)
- active (BOOLEAN)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### workflow_step_requirements
```sql
- id (UUID)
- workflow_step_id (UUID FK)
- consumable_category_id (UUID FK)
- supplier_id (UUID FK)
- item_name (VARCHAR)
- brand (VARCHAR)
- model (VARCHAR)
- default_quantity (DECIMAL)
- unit (VARCHAR)
- estimated_cost (DECIMAL)
- procurement_type (VARCHAR)
- is_optional (BOOLEAN)
- notes (TEXT)
- display_order (INTEGER)
- supplier_item_code (VARCHAR)
- template_notes (TEXT)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### project_step_requirements
```sql
- id (UUID)
- project_step_id (UUID FK)
- workflow_step_requirement_id (UUID FK)
- consumable_category_id (UUID FK)
- supplier_id (UUID FK)
- item_name (VARCHAR)
- brand (VARCHAR)
- model (VARCHAR)
- quantity (DECIMAL)
- unit (VARCHAR)
- estimated_cost (DECIMAL)
- actual_cost (DECIMAL)
- procurement_type (VARCHAR)
- status (VARCHAR)
- is_optional (BOOLEAN)
- notes (TEXT)
- display_order (INTEGER)
- supplier_item_code (VARCHAR)
- supplier_quote_number (VARCHAR)
- quote_expiry_date (DATE)
- required_delivery_date (DATE)
- delivery_instructions (TEXT)
- is_template_copied (BOOLEAN)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

## Verification

After setup, verify with:

```sql
-- Check table structures
\d consumable_categories
\d suppliers
\d supplier_categories

-- Verify data
SELECT COUNT(*) as categories FROM consumable_categories;
SELECT COUNT(*) as suppliers FROM suppliers;
SELECT COUNT(*) as relationships FROM supplier_categories;

-- Should show:
-- categories: 65 (10 from V33 + 55 from Australian file)
-- suppliers: 41 (6 from V33 + 35 from Sydney file)
-- relationships: Multiple (depends on mapping)
```

## Files Included

1. `drop_consumable_tables.sql` - Drops all consumable tables
2. `V33__Create_consumable_management_tables.sql` - Migration with correct schema
3. `australian_residential_consumable_categories.sql` - 55 Australian categories
4. `sydney_suppliers_insert_statements.sql` - 35 Sydney suppliers + relationships

All files are now aligned with the entity definitions (no `created_by_user_id` columns)!

