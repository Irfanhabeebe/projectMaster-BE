-- Remove financial fields from crew table
ALTER TABLE crew DROP COLUMN IF EXISTS tax_file_number;
ALTER TABLE crew DROP COLUMN IF EXISTS superannuation_fund;
ALTER TABLE crew DROP COLUMN IF EXISTS superannuation_member_number;
ALTER TABLE crew DROP COLUMN IF EXISTS bank_account_name;
ALTER TABLE crew DROP COLUMN IF EXISTS bank_account_bsb;
ALTER TABLE crew DROP COLUMN IF EXISTS bank_account_number;
ALTER TABLE crew DROP COLUMN IF EXISTS hourly_rate;
ALTER TABLE crew DROP COLUMN IF EXISTS annual_salary;

-- Update table comment to reflect the changes
COMMENT ON TABLE crew IS 'Stores information about company crew members (tradies) - simplified version without financial details';
