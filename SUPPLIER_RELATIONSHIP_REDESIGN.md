## Supplier Relationship Redesign

# Overview

The supplier system has been redesigned to follow the same pattern as the contracting company relationships, separating **master supplier data** from **company-specific relationships**.

## Architecture Comparison

### Contracting Company Pattern (Current)
```
ContractingCompany (Master) 
    ↓
BuilderContractorRelationship (Company-specific)
    ↓
BuilderContractorSpecialty (Relationship-specific specialties)
```

### New Supplier Pattern
```
Supplier (Master)
    ↓
CompanySupplierRelationship (Company-specific)
    ↓
CompanySupplierCategory (Relationship-specific preferred categories)
```

## Entity Structure

### 1. Supplier (Master Level)
**Purpose:** Global supplier information shared across all companies

**Fields:**
- Basic Info: name, address, abn, email, phone, contact_person, website
- Classification: supplier_type (RETAIL, WHOLESALE, SPECIALIST, ONLINE, MANUFACTURER)
- Defaults: default_payment_terms (can be overridden in relationship)
- Status: active, verified, national_supplier
- Relationships: categories (SupplierCategory), companyRelationships

**Key Changes:**
- ✅ Removed: `payment_terms` (moved to relationship)
- ✅ Removed: `credit_limit` (moved to relationship)
- ✅ Added: `default_payment_terms` (default only)
- ✅ Added: `national_supplier` (boolean flag)
- ✅ Added: `companyRelationships` (one-to-many)

### 2. SupplierCategory (Master Level)
**Purpose:** Maps suppliers to the categories they serve globally

**Fields:**
- supplier_id, category_id
- is_primary_category (supplier's primary specialty)
- notes, active

**No changes** - remains at master level

### 3. CompanySupplierRelationship (Company Level) **NEW**
**Purpose:** Represents a specific builder company's relationship with a supplier

**Fields:**
- company_id, supplier_id, added_by_user_id
- active, preferred (mark as preferred supplier)
- account_number (company's account with supplier)
- payment_terms (company-specific, overrides default)
- credit_limit (company-specific limit)
- discount_rate (negotiated discount %)
- contract_start_date, contract_end_date
- delivery_instructions (company-specific)
- notes, rating (1-5)
- preferredCategories (CompanySupplierCategory)

**Key Features:**
- Tracks company-specific account details
- Allows company-specific payment terms and credit limits
- Supports rating and preference marking
- Contract management with start/end dates
- Company-specific delivery instructions

### 4. CompanySupplierCategory (Company Level) **NEW**
**Purpose:** Preferred categories for a specific company-supplier relationship

**Fields:**
- company_supplier_relationship_id, category_id
- active, is_primary_category (primary for this company)
- minimum_order_value (company-specific)
- estimated_annual_spend (for budgeting)
- notes

**Key Features:**
- Company can mark their preferred categories for each supplier
- Track minimum order values per category
- Estimate annual spending for better supplier management

## Benefits of This Approach

### 1. **Data Integrity**
- Master supplier data maintained centrally
- No data duplication across companies
- Single source of truth for supplier information

### 2. **Flexibility**
- Each company can:
  - Have their own account numbers
  - Negotiate their own payment terms
  - Set their own credit limits
  - Mark preferred suppliers and categories
  - Rate suppliers independently

### 3. **Multi-Company Support**
- Perfect for scenarios where:
  - Multiple builders use the same supplier
  - Each builder has different terms with the supplier
  - Suppliers serve different categories for different companies

### 4. **Scalability**
- Easy to add new companies without duplicating supplier data
- Supports national supplier networks
- Handles regional vs. national suppliers

### 5. **Business Intelligence**
- Track which suppliers are most preferred across companies
- Analyze company-specific spending patterns
- Monitor contract expiry dates
- Compare terms across companies

## Use Cases

### Scenario 1: National Supplier Chain
**Bunnings Warehouse:**
- Master: Single Bunnings entry with all locations
- Relationships: Each builder company has their own:
  - Trade account number
  - Negotiated discount rate
  - Preferred categories
  - Delivery preferences

### Scenario 2: Multiple Builders, Same Supplier
**Reece Plumbing:**
- Master: Single Reece entry
- Company A: NET_14 terms, 10% discount, preferred for all plumbing
- Company B: NET_30 terms, 5% discount, preferred only for fixtures
- Company C: Not yet established relationship

### Scenario 3: Preferred Suppliers by Category
**Company X has relationships with:**
- Bunnings: General hardware (not preferred)
- Ramset: Fasteners (preferred, primary category)
- ITW Buildex: Structural fasteners (preferred, backup)

## Migration Path

### Phase 1: Create New Tables
```sql
CREATE TABLE company_supplier_relationships (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL REFERENCES companies(id),
    supplier_id UUID NOT NULL REFERENCES suppliers(id),
    added_by_user_id UUID NOT NULL REFERENCES users(id),
    active BOOLEAN NOT NULL DEFAULT true,
    preferred BOOLEAN NOT NULL DEFAULT false,
    account_number VARCHAR(100),
    payment_terms VARCHAR(50),
    credit_limit DECIMAL(15,2),
    discount_rate DECIMAL(5,2),
    contract_start_date DATE,
    contract_end_date DATE,
    delivery_instructions TEXT,
    notes TEXT,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_id, supplier_id)
);

CREATE TABLE company_supplier_categories (
    id UUID PRIMARY KEY,
    company_supplier_relationship_id UUID NOT NULL REFERENCES company_supplier_relationships(id) ON DELETE CASCADE,
    consumable_category_id UUID NOT NULL REFERENCES consumable_categories(id),
    active BOOLEAN NOT NULL DEFAULT true,
    is_primary_category BOOLEAN NOT NULL DEFAULT false,
    minimum_order_value DECIMAL(15,2),
    estimated_annual_spend DECIMAL(15,2),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(company_supplier_relationship_id, consumable_category_id)
);
```

### Phase 2: Update Suppliers Table
```sql
-- Rename payment_terms to default_payment_terms
ALTER TABLE suppliers RENAME COLUMN payment_terms TO default_payment_terms;

-- Remove credit_limit (now in relationships)
ALTER TABLE suppliers DROP COLUMN IF EXISTS credit_limit;

-- Add national_supplier flag
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS national_supplier BOOLEAN NOT NULL DEFAULT false;

-- Update national suppliers
UPDATE suppliers SET national_supplier = true 
WHERE name LIKE '%Bunnings%' OR name LIKE '%Mitre%' OR name LIKE '%Reece%';
```

### Phase 3: Data Migration (Optional)
If you have existing company-specific data in suppliers table, migrate it to relationships:
```sql
-- Create relationships from existing supplier data
INSERT INTO company_supplier_relationships (
    id, company_id, supplier_id, added_by_user_id, 
    payment_terms, credit_limit, created_at, updated_at
)
SELECT 
    gen_random_uuid(),
    c.id, -- for each company
    s.id, -- for each supplier
    (SELECT id FROM users WHERE company_id = c.id AND role = 'ADMIN' LIMIT 1),
    s.default_payment_terms,
    s.credit_limit, -- if this column still exists
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM suppliers s
CROSS JOIN companies c
WHERE s.active = true;
```

## API Changes

### New Endpoints

#### Company Supplier Relationships
```
POST   /api/companies/{companyId}/suppliers/{supplierId}/relationship
GET    /api/companies/{companyId}/suppliers
GET    /api/companies/{companyId}/suppliers/preferred
GET    /api/companies/{companyId}/suppliers/by-category/{categoryId}
PUT    /api/companies/{companyId}/suppliers/{supplierId}/relationship
DELETE /api/companies/{companyId}/suppliers/{supplierId}/relationship
```

#### Company Supplier Categories
```
POST   /api/companies/{companyId}/suppliers/{supplierId}/categories
GET    /api/companies/{companyId}/suppliers/{supplierId}/categories
PUT    /api/companies/{companyId}/suppliers/{supplierId}/categories/{categoryId}
DELETE /api/companies/{companyId}/suppliers/{supplierId}/categories/{categoryId}
```

### Updated Endpoints
- Master supplier endpoints remain unchanged
- Add `?companyId=xxx` parameter to filter by company relationship

## Workflow Integration

### When creating WorkflowStepRequirement:
- Select from master `Supplier` list
- Optionally specify preferred supplier per company

### When creating ProjectStepRequirement:
- Auto-select from company's established supplier relationships
- Show only suppliers with active relationships for the company
- Respect preferred suppliers and categories
- Use company-specific payment terms and account numbers

### When ordering:
- Use company's account number automatically
- Apply company-specific discount rates
- Follow company-specific delivery instructions
- Track spending against estimated annual spend

## Files Created

1. **Entities:**
   - `CompanySupplierRelationship.java`
   - `CompanySupplierCategory.java`

2. **Repositories:**
   - `CompanySupplierRelationshipRepository.java`
   - `CompanySupplierCategoryRepository.java`

3. **Updated:**
   - `Supplier.java` (added relationship field, renamed payment_terms)

4. **Documentation:**
   - `SUPPLIER_RELATIONSHIP_REDESIGN.md` (this file)

## Next Steps

1. ✅ Create migration script (V34)
2. ⏳ Create service layer for relationship management
3. ⏳ Create DTOs for requests/responses
4. ⏳ Create REST controllers
5. ⏳ Update existing services to use relationships
6. ⏳ Create UI for managing supplier relationships

## Backward Compatibility

- Master supplier data remains accessible
- SupplierCategory relationships continue to work
- Existing workflow templates not affected
- Gradual migration: companies can establish relationships over time

