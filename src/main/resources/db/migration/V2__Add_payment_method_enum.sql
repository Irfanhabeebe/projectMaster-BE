-- Add payment method enum
CREATE TYPE payment_method AS ENUM ('CASH', 'BANK_TRANSFER', 'CREDIT_CARD', 'DEBIT_CARD', 'CHEQUE', 'PAYPAL', 'STRIPE', 'OTHER');

-- Update payments table to use the enum
ALTER TABLE payments ALTER COLUMN payment_method TYPE payment_method USING payment_method::payment_method;

-- Add updated_at column to invoice_line_items table for consistency
ALTER TABLE invoice_line_items ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

-- Add updated_at column to payments table for consistency  
ALTER TABLE payments ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;