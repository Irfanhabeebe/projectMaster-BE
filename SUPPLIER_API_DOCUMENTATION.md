# Supplier Management API Documentation

## Overview

The Supplier Management API provides endpoints for managing suppliers at both **master level** (global supplier data) and **company level** (company-specific relationships and terms).

## Architecture

### Two-Level System

1. **Master Level**: Global supplier information shared across all companies
2. **Company Level**: Company-specific relationships with customized terms

Similar to the contracting company pattern:
```
Supplier (Master) → CompanySupplierRelationship (Company-specific)
```

---

## Master Supplier Management

### 1. Create Supplier

Creates a new supplier at the master level.

**Endpoint:** `POST /api/suppliers`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Request Body:**
```json
{
  "name": "Bunnings Warehouse - Alexandria",
  "address": "75 O'Riordan St, Alexandria NSW 2015",
  "abn": "63000000001",
  "email": "trade.alexandria@bunnings.com.au",
  "phone": "(02) 9698 9800",
  "contactPerson": "Trade Desk",
  "website": "www.bunnings.com.au",
  "supplierType": "RETAIL",
  "defaultPaymentTerms": "NET_30",
  "nationalSupplier": true,
  "verified": true
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Supplier created successfully",
  "data": {
    "id": "uuid",
    "name": "Bunnings Warehouse - Alexandria",
    "address": "75 O'Riordan St, Alexandria NSW 2015",
    "abn": "63000000001",
    "email": "trade.alexandria@bunnings.com.au",
    "phone": "(02) 9698 9800",
    "contactPerson": "Trade Desk",
    "website": "www.bunnings.com.au",
    "supplierType": "RETAIL",
    "defaultPaymentTerms": "NET_30",
    "active": true,
    "verified": true,
    "nationalSupplier": true,
    "categories": [],
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T10:00:00Z"
  }
}
```

### 2. Get All Active Suppliers

**Endpoint:** `GET /api/suppliers`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Suppliers retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "name": "Bunnings Warehouse - Alexandria",
      "supplierType": "RETAIL",
      "defaultPaymentTerms": "NET_30",
      "active": true,
      "verified": true,
      "nationalSupplier": true,
      "categories": [
        {
          "categoryId": "uuid",
          "categoryName": "General Hardware",
          "isPrimaryCategory": true
        }
      ]
    }
  ]
}
```

### 3. Get Supplier by ID

**Endpoint:** `GET /api/suppliers/{supplierId}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Response:** `200 OK` (same structure as create response)

### 4. Get Suppliers by Category

**Endpoint:** `GET /api/suppliers/by-category/{categoryId}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Description:** Returns all suppliers that serve a specific category

### 5. Get Suppliers by Type

**Endpoint:** `GET /api/suppliers/by-type/{supplierType}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Supplier Types:**
- `RETAIL` - Retail stores like Bunnings, Mitre 10
- `WHOLESALE` - Wholesale suppliers
- `SPECIALIST` - Specialized suppliers (e.g., plumbing, electrical)
- `ONLINE` - Online-only suppliers
- `MANUFACTURER` - Direct from manufacturer

### 6. Search Suppliers

**Endpoint:** `GET /api/suppliers/search?searchText={text}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Example:** `GET /api/suppliers/search?searchText=bunnings`

### 7. Update Supplier

**Endpoint:** `PUT /api/suppliers/{supplierId}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Request Body:** Same as create request

### 8. Deactivate Supplier

**Endpoint:** `DELETE /api/suppliers/{supplierId}`

**Authorization:** `ADMIN`

**Description:** Soft delete - sets active to false

### 9. Reactivate Supplier

**Endpoint:** `POST /api/suppliers/{supplierId}/reactivate`

**Authorization:** `ADMIN`

---

## Company-Supplier Relationship Management

### 1. Create Company-Supplier Relationship

Establish a relationship between your company and a supplier with company-specific terms.

**Endpoint:** `POST /api/companies/{companyId}/suppliers/{supplierId}/relationship`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Request Body:**
```json
{
  "preferred": true,
  "accountNumber": "TRADE-12345",
  "paymentTerms": "NET_30",
  "creditLimit": 50000.00,
  "discountRate": 10.5,
  "contractStartDate": "2024-01-01",
  "contractEndDate": "2024-12-31",
  "deliveryInstructions": "Deliver to site office, contact foreman on arrival",
  "notes": "Primary supplier for all hardware",
  "rating": 5
}
```

**Field Descriptions:**
- `preferred` - Mark as preferred supplier for your company
- `accountNumber` - Your company's account number with this supplier
- `paymentTerms` - Company-specific payment terms (overrides supplier's default)
- `creditLimit` - Credit limit negotiated with supplier
- `discountRate` - Negotiated discount percentage
- `contractStartDate/EndDate` - Contract period
- `deliveryInstructions` - Company-specific delivery preferences
- `notes` - Internal notes about this relationship
- `rating` - Your company's rating for this supplier (1-5)

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Relationship created successfully",
  "data": {
    "id": "uuid",
    "companyId": "uuid",
    "companyName": "ABC Builders",
    "supplierId": "uuid",
    "supplierName": "Bunnings Warehouse - Alexandria",
    "supplierType": "RETAIL",
    "active": true,
    "preferred": true,
    "accountNumber": "TRADE-12345",
    "paymentTerms": "NET_30",
    "creditLimit": 50000.00,
    "discountRate": 10.5,
    "contractStartDate": "2024-01-01",
    "contractEndDate": "2024-12-31",
    "deliveryInstructions": "Deliver to site office, contact foreman on arrival",
    "notes": "Primary supplier for all hardware",
    "rating": 5,
    "preferredCategories": [],
    "addedByUserName": "John Smith",
    "createdAt": "2024-01-01T10:00:00Z",
    "updatedAt": "2024-01-01T10:00:00Z"
  }
}
```

### 2. Get All Company Suppliers

**Endpoint:** `GET /api/companies/{companyId}/suppliers`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Description:** Get all suppliers that have relationships with your company

**Response:** `200 OK` - Array of relationship objects

### 3. Search Company Suppliers

**Endpoint:** `GET /api/companies/{companyId}/suppliers/search?searchText={text}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Description:** Search suppliers by name for this company with pagination support

**Query Parameters:**
- `searchText` (string) - Search text for supplier name (case-insensitive)
- `page` (integer, optional) - Page number (0-based, default: 0)
- `size` (integer, optional) - Items per page (default: 20)
- `sort` (string, optional) - Sort criteria (e.g., "supplierName,asc", "rating,desc")

**Example:** `GET /api/companies/{companyId}/suppliers/search?searchText=bunnings&page=0&size=10&sort=supplierName,asc`

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "companyId": "uuid",
        "companyName": "ABC Builders",
        "supplierId": "uuid",
        "supplierName": "Bunnings Warehouse - Alexandria",
        "supplierType": "RETAIL",
        "active": true,
        "preferred": true,
        "accountNumber": "TRADE-12345",
        "paymentTerms": "NET_30",
        "creditLimit": 50000.00,
        "discountRate": 10.5,
        "rating": 5,
        "preferredCategories": [],
        "addedByUserName": "John Smith",
        "createdAt": "2024-01-01T10:00:00Z",
        "updatedAt": "2024-01-01T10:00:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 5,
    "totalPages": 1,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 5,
    "empty": false
  }
}
```

**Use Case:** Search for specific suppliers within your company's relationships

### 4. Get Preferred Suppliers

**Endpoint:** `GET /api/companies/{companyId}/suppliers/preferred`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Description:** Get only preferred suppliers for your company

### 5. Get Suppliers by Category

**Endpoint:** `GET /api/companies/{companyId}/suppliers/by-category/{categoryId}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Description:** Get suppliers for a specific category, ordered by:
1. Preferred suppliers first
2. Then by rating (highest first)

**Use Case:** When selecting a supplier for a project step requirement

### 6. Get Specific Relationship

**Endpoint:** `GET /api/companies/{companyId}/suppliers/{supplierId}/relationship`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Description:** Get details of relationship between your company and a specific supplier

### 7. Update Relationship

**Endpoint:** `PUT /api/companies/{companyId}/suppliers/{supplierId}/relationship`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Request Body:** Same as create relationship

**Use Cases:**
- Update account number
- Change payment terms
- Adjust credit limit
- Update discount rate
- Change rating
- Modify delivery instructions

### 8. Deactivate Relationship

**Endpoint:** `DELETE /api/companies/{companyId}/suppliers/{supplierId}/relationship`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Description:** Deactivate relationship (soft delete - sets active to false)

---

## Consumable Categories

### Get All Categories

**Endpoint:** `GET /api/consumable-categories`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Response:**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "name": "Bathroom Fittings",
      "description": "Bathroom fixtures, fittings, and accessories",
      "icon": "bathroom-icon",
      "displayOrder": 1,
      "active": true
    }
  ]
}
```

### Search Categories

**Endpoint:** `GET /api/consumable-categories/search?searchText={text}`

---

## Data Models

### Supplier Types
- `RETAIL` - Bunnings, Mitre 10
- `WHOLESALE` - Bulk suppliers
- `SPECIALIST` - Reece (plumbing), Haymans (electrical)
- `ONLINE` - Online-only suppliers
- `MANUFACTURER` - Caroma, Dulux

### Payment Terms
- `COD` - Cash on Delivery
- `NET_7` - Net 7 days
- `NET_14` - Net 14 days
- `NET_30` - Net 30 days
- `NET_60` - Net 60 days
- `PREPAID` - Payment before delivery

---

## Workflow Integration

### Workflow Template Level

When creating a `WorkflowStepRequirement`:
```json
{
  "workflowStepId": "uuid",
  "categoryId": "uuid",
  "supplierId": "uuid",  // Select from master supplier list
  "itemName": "Vanity Mirror",
  "brand": "Caroma",
  "defaultQuantity": 2,
  "unit": "pcs",
  "estimatedCost": 150.00,
  "procurementType": "BUY"
}
```

### Project Level

When creating a project, requirements are copied:
- Uses company's relationship with the supplier (if exists)
- Applies company-specific discount rates
- Uses company's account number
- Follows company's payment terms

**Smart Selection:**
- System suggests suppliers from company's active relationships
- Preferred suppliers shown first
- Shows company-specific pricing and terms

---

## Use Case Examples

### Example 1: Establishing a Relationship with Bunnings

**Step 1:** Create master supplier (if not exists)
```bash
POST /api/suppliers
{
  "name": "Bunnings Warehouse - Alexandria",
  "supplierType": "RETAIL",
  "defaultPaymentTerms": "NET_30"
}
```

**Step 2:** Create company relationship
```bash
POST /api/companies/{myCompanyId}/suppliers/{bunningsId}/relationship
{
  "preferred": true,
  "accountNumber": "TRADE-98765",
  "paymentTerms": "NET_30",
  "creditLimit": 50000.00,
  "discountRate": 12.0,
  "rating": 5
}
```

### Example 2: Selecting Supplier for Project Step

**Step 1:** Get suppliers for category
```bash
GET /api/companies/{companyId}/suppliers/by-category/{bathroomFittingsId}
```

**Response:** Returns suppliers ordered by preference:
1. Reece Plumbing (preferred: true, rating: 5, discount: 15%)
2. Bunnings (preferred: false, rating: 4, discount: 12%)

**Step 2:** Use in project requirement
- System auto-fills account number
- Applies discount rate
- Uses company-specific payment terms

### Example 3: Multi-Company Scenario

**Bunnings Warehouse - Alexandria (Master):**
- Default terms: NET_30
- Type: RETAIL
- Categories: Hardware, Plumbing, Electrical, etc.

**Company A's Relationship:**
- Account: TRADE-11111
- Terms: NET_30
- Discount: 15% (high volume)
- Rating: 5
- Preferred: true

**Company B's Relationship:**
- Account: TRADE-22222
- Terms: NET_14 (smaller company)
- Discount: 8%
- Rating: 4
- Preferred: false

**Company C:**
- No relationship (uses cash terms)

---

## Complete API Endpoints Summary

### Master Supplier Management
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/suppliers` | Create supplier | ADMIN, PM |
| GET | `/api/suppliers` | Get all active suppliers | ADMIN, PM, TRADIE |
| GET | `/api/suppliers/{id}` | Get supplier by ID | ADMIN, PM, TRADIE |
| GET | `/api/suppliers/by-category/{categoryId}` | Get suppliers by category | ADMIN, PM, TRADIE |
| GET | `/api/suppliers/by-type/{type}` | Get suppliers by type | ADMIN, PM, TRADIE |
| GET | `/api/suppliers/search` | Search suppliers | ADMIN, PM, TRADIE |
| PUT | `/api/suppliers/{id}` | Update supplier | ADMIN, PM |
| DELETE | `/api/suppliers/{id}` | Deactivate supplier | ADMIN |
| POST | `/api/suppliers/{id}/reactivate` | Reactivate supplier | ADMIN |

### Company-Supplier Relationships
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/companies/{companyId}/suppliers/{supplierId}/relationship` | Create relationship | ADMIN, PM |
| GET | `/api/companies/{companyId}/suppliers` | Get company suppliers | ADMIN, PM, TRADIE |
| GET | `/api/companies/{companyId}/suppliers/search` | Search company suppliers | ADMIN, PM, TRADIE |
| GET | `/api/companies/{companyId}/suppliers/preferred` | Get preferred suppliers | ADMIN, PM, TRADIE |
| GET | `/api/companies/{companyId}/suppliers/by-category/{categoryId}` | Get suppliers by category | ADMIN, PM, TRADIE |
| GET | `/api/companies/{companyId}/suppliers/{supplierId}/relationship` | Get specific relationship | ADMIN, PM, TRADIE |
| PUT | `/api/companies/{companyId}/suppliers/{supplierId}/relationship` | Update relationship | ADMIN, PM |
| DELETE | `/api/companies/{companyId}/suppliers/{supplierId}/relationship` | Deactivate relationship | ADMIN, PM |

### Consumable Categories
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/consumable-categories` | Get all categories | ADMIN, PM |
| GET | `/api/consumable-categories/{id}` | Get category by ID | ADMIN, PM |
| GET | `/api/consumable-categories/search` | Search categories | ADMIN, PM |

### Project Step Requirements
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/projects/{projectId}/steps/{stepId}/requirements` | Get step requirements | ADMIN, PM, TRADIE |
| GET | `/api/projects/{projectId}/steps/{stepId}/requirements/template` | Get template requirements | ADMIN, PM, TRADIE |
| GET | `/api/projects/{projectId}/steps/{stepId}/requirements/project-specific` | Get project-specific requirements | ADMIN, PM, TRADIE |
| POST | `/api/projects/{projectId}/steps/{stepId}/requirements` | Add requirement | ADMIN, PM |
| PUT | `/api/projects/{projectId}/steps/{stepId}/requirements/{requirementId}` | Update requirement | ADMIN, PM |
| DELETE | `/api/projects/{projectId}/steps/{stepId}/requirements/{requirementId}` | Delete requirement | ADMIN, PM |

---

## Best Practices

### 1. Supplier Setup Workflow
1. Create master supplier (once)
2. Each company establishes their own relationship
3. Companies configure their terms, discounts, accounts
4. Companies mark preferred suppliers per category

### 2. Project Requirement Workflow
1. Project created from workflow template
2. Requirements copied with template supplier
3. System checks if company has relationship with supplier
4. If yes: uses company's account, discount, terms
5. If no: uses default terms or prompts to establish relationship

### 3. Supplier Selection
1. UI shows suppliers filtered by:
   - Company's active relationships
   - Category relevance
   - Preferred status
   - Rating
2. Displays company-specific info:
   - Your account number
   - Your discount rate
   - Your payment terms
3. Auto-fills requirement with company data

### 4. Multi-Company Benefits
- National suppliers (Bunnings, Reece) created once
- Each company has independent relationships
- Different terms, discounts, ratings per company
- Shared master data, separate business rules

---

## Error Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Invalid request data |
| 403 | Forbidden - insufficient permissions |
| 404 | Entity not found |
| 409 | Conflict - duplicate entry |
| 500 | Internal server error |

---

## Notes

- All supplier and relationship operations are logged
- All monetary values are in AUD
- Dates use ISO 8601 format
- UUIDs used for all entity IDs
- All deletions are soft deletes (active = false)
- Relationships cascade delete when supplier is deleted

---

## Next Steps for UI Development

1. **Supplier Management Screen**: CRUD for master suppliers
2. **Company Relationships Screen**: Manage your company's supplier relationships
3. **Supplier Selection**: When creating requirements, show company's suppliers
4. **Dashboard**: Show preferred suppliers, expiring contracts, spending analytics
5. **Reports**: Compare suppliers, track discounts, analyze spending


