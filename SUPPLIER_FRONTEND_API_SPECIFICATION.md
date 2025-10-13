# Supplier Management - Frontend API Specification

**Version:** 1.0  
**Last Updated:** October 2025  
**Base URL:** `http://localhost:8080/api`

---

## Table of Contents

1. [Authentication](#authentication)
2. [Master Supplier Management](#master-supplier-management)
3. [Company-Supplier Relationships](#company-supplier-relationships)
4. [Consumable Categories](#consumable-categories)
5. [Enums and Constants](#enums-and-constants)
6. [Error Handling](#error-handling)

---

## Authentication

All endpoints require JWT authentication via Bearer token.

**Header:**
```
Authorization: Bearer <your-jwt-token>
```

**Role Requirements:**
- `ADMIN` - Full access
- `PROJECT_MANAGER` - Can create/update suppliers and relationships
- `TRADIE` - Read-only access

---

## Master Supplier Management

### 1. Create Supplier

Create a new supplier in the master database (shared across all companies).

**Endpoint:** `POST /api/suppliers`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Request Headers:**
```
Content-Type: application/json
Authorization: Bearer <token>
```

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
  "verified": true,
  "categories": [
    "403c1ca1-ddd6-4b64-b802-3c237e19ca35",
    "0de71499-e8ab-4526-848d-8197b7a4b73f"
  ]
}
```

**Field Validations:**
- `name` - Required, non-blank
- `abn` - Optional, must be 11 digits if provided
- `email` - Optional, must be valid email format
- `supplierType` - One of: `RETAIL`, `WHOLESALE`, `SPECIALIST`, `ONLINE`, `MANUFACTURER`
- `defaultPaymentTerms` - One of: `COD`, `NET_7`, `NET_14`, `NET_30`, `NET_60`, `PREPAID`
- `categories` - Optional, array of category UUIDs that this supplier serves

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Supplier created successfully",
  "data": {
    "id": "d325c0f3-7c01-44fc-a898-6c17b236a6ce",
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
    "categories": [],
    "createdAt": "2025-10-01T08:22:35.286796Z",
    "updatedAt": "2025-10-01T08:22:35.286796Z"
  }
}
```

**Error Response:** `409 Conflict`
```json
{
  "success": false,
  "message": "Supplier with name 'Bunnings Warehouse - Alexandria' already exists",
  "data": null
}
```

---

### 2. Get All Active Suppliers

Retrieve all active suppliers from the master database.

**Endpoint:** `GET /api/suppliers`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Request Headers:**
```
Authorization: Bearer <token>
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Suppliers retrieved successfully",
  "data": [
    {
      "id": "d325c0f3-7c01-44fc-a898-6c17b236a6ce",
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
      "categories": [
        {
          "categoryId": "403c1ca1-ddd6-4b64-b802-3c237e19ca35",
          "categoryName": "General Hardware",
          "isPrimaryCategory": true
        },
        {
          "categoryId": "0de71499-e8ab-4526-848d-8197b7a4b73f",
          "categoryName": "Plumbing Materials",
          "isPrimaryCategory": false
        }
      ],
      "createdAt": "2025-10-01T08:22:35.286796Z",
      "updatedAt": "2025-10-01T08:22:35.286796Z"
    }
  ]
}
```

---

### 3. Get Supplier by ID

**Endpoint:** `GET /api/suppliers/{supplierId}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Path Parameters:**
- `supplierId` (UUID) - The supplier's ID

**Response:** `200 OK` - Same structure as single supplier object above

**Error Response:** `404 Not Found`
```json
{
  "success": false,
  "message": "Supplier not found with ID: d325c0f3-7c01-44fc-a898-6c17b236a6ce",
  "data": null
}
```

---

### 4. Get Suppliers by Category

Get all suppliers that serve a specific consumable category.

**Endpoint:** `GET /api/suppliers/by-category/{categoryId}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Path Parameters:**
- `categoryId` (UUID) - The consumable category ID

**Use Case:** When you need to find all suppliers that provide "Bathroom Fittings"

**Response:** `200 OK` - Array of supplier objects

---

### 5. Get Suppliers by Type

**Endpoint:** `GET /api/suppliers/by-type/{supplierType}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Path Parameters:**
- `supplierType` - One of: `RETAIL`, `WHOLESALE`, `SPECIALIST`, `ONLINE`, `MANUFACTURER`

**Example:** `GET /api/suppliers/by-type/RETAIL`

**Response:** `200 OK` - Array of supplier objects

---

### 6. Search Suppliers

Search suppliers by name (case-insensitive, partial match).

**Endpoint:** `GET /api/suppliers/search`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Query Parameters:**
- `searchText` (string) - Search text

**Example:** `GET /api/suppliers/search?searchText=bunnings`

**Response:** `200 OK` - Array of supplier objects matching search

---

### 7. Update Supplier

**Endpoint:** `PUT /api/suppliers/{supplierId}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Path Parameters:**
- `supplierId` (UUID) - The supplier's ID

**Request Body:** Same as Create Supplier request

**Response:** `200 OK` - Updated supplier object

---

### 8. Deactivate Supplier

Soft delete - sets supplier's active status to false.

**Endpoint:** `DELETE /api/suppliers/{supplierId}`

**Authorization:** `ADMIN`

**Path Parameters:**
- `supplierId` (UUID) - The supplier's ID

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Supplier deactivated successfully",
  "data": null
}
```

---

### 9. Reactivate Supplier

**Endpoint:** `POST /api/suppliers/{supplierId}/reactivate`

**Authorization:** `ADMIN`

**Path Parameters:**
- `supplierId` (UUID) - The supplier's ID

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Supplier reactivated successfully",
  "data": null
}
```

---

## Company-Supplier Relationships

### 1. Create Company-Supplier Relationship

Establish a relationship between your company and a supplier with company-specific terms.

**Endpoint:** `POST /api/companies/{companyId}/suppliers/{supplierId}/relationship`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Path Parameters:**
- `companyId` (UUID) - Your company's ID
- `supplierId` (UUID) - The supplier's ID

**Request Headers:**
```
Content-Type: application/json
Authorization: Bearer <token>
```

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
  "deliveryInstructions": "Deliver to site office between 7am-3pm. Contact foreman John on 0412 345 678",
  "notes": "Primary hardware supplier. Preferred for all general construction materials.",
  "rating": 5,
  "preferredCategories": [
    "403c1ca1-ddd6-4b64-b802-3c237e19ca35",
    "0de71499-e8ab-4526-848d-8197b7a4b73f"
  ]
}
```

**Field Descriptions:**
- `preferred` (boolean, optional) - Mark as preferred supplier for your company
- `accountNumber` (string, optional) - Your company's account number with this supplier
- `paymentTerms` (enum, optional) - Company-specific payment terms. If not provided, uses supplier's default
- `creditLimit` (decimal, optional) - Credit limit negotiated with supplier
- `discountRate` (decimal, optional) - Negotiated discount percentage (0-100)
- `contractStartDate` (date, optional) - Contract start date (format: YYYY-MM-DD)
- `contractEndDate` (date, optional) - Contract end date (format: YYYY-MM-DD)
- `deliveryInstructions` (string, optional) - Company-specific delivery instructions
- `notes` (string, optional) - Internal notes about this relationship
- `rating` (integer, optional) - Your company's rating for this supplier (1-5)
- `preferredCategories` (array, optional) - Array of category UUIDs that are preferred from this supplier

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "Relationship created successfully",
  "data": {
    "id": "8f3a2b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
    "companyId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
    "companyName": "ABC Builders Pty Ltd",
    "supplierId": "d325c0f3-7c01-44fc-a898-6c17b236a6ce",
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
    "deliveryInstructions": "Deliver to site office between 7am-3pm. Contact foreman John on 0412 345 678",
    "notes": "Primary hardware supplier. Preferred for all general construction materials.",
    "rating": 5,
    "preferredCategories": [],
    "addedByUserName": "John Smith",
    "createdAt": "2025-10-01T08:30:00.000Z",
    "updatedAt": "2025-10-01T08:30:00.000Z"
  }
}
```

**Error Response:** `409 Conflict`
```json
{
  "success": false,
  "message": "Relationship already exists between this company and supplier",
  "data": null
}
```

---

### 2. Get All Company Suppliers

Get all suppliers that have active relationships with your company.

**Endpoint:** `GET /api/companies/{companyId}/suppliers`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Path Parameters:**
- `companyId` (UUID) - Your company's ID

**Query Parameters:**
- `page` (integer, optional) - Page number (0-based, default: 0)
- `size` (integer, optional) - Items per page (default: 20)
- `sort` (string, optional) - Sort criteria (e.g., "supplierName,asc", "rating,desc")

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Suppliers retrieved successfully",
  "data": [
    {
      "id": "8f3a2b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
      "companyId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
      "companyName": "ABC Builders Pty Ltd",
      "supplierId": "d325c0f3-7c01-44fc-a898-6c17b236a6ce",
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
      "deliveryInstructions": "Deliver to site office between 7am-3pm",
      "notes": "Primary hardware supplier",
      "rating": 5,
      "preferredCategories": [
        {
          "categoryId": "cat-uuid-1",
          "categoryName": "General Hardware",
          "isPrimaryCategory": true,
          "minimumOrderValue": 500.00,
          "estimatedAnnualSpend": 25000.00
        },
        {
          "categoryId": "cat-uuid-2",
          "categoryName": "Electrical Components",
          "isPrimaryCategory": false,
          "minimumOrderValue": 300.00,
          "estimatedAnnualSpend": 15000.00
        }
      ],
      "addedByUserName": "John Smith",
      "createdAt": "2025-10-01T08:30:00.000Z",
      "updatedAt": "2025-10-01T08:30:00.000Z"
    },
    {
      "id": "9g4b3c2d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
      "companyId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
      "companyName": "ABC Builders Pty Ltd",
      "supplierId": "e436d1g4-8d12-55gd-b909-7d28c347d7ef",
      "supplierName": "Reece Plumbing - Auburn",
      "supplierType": "SPECIALIST",
      "active": true,
      "preferred": true,
      "accountNumber": "PLM-98765",
      "paymentTerms": "NET_14",
      "creditLimit": 75000.00,
      "discountRate": 15.0,
      "contractStartDate": "2024-01-01",
      "contractEndDate": "2025-12-31",
      "deliveryInstructions": "Deliver to plumber's storage area",
      "notes": "Primary plumbing supplier",
      "rating": 5,
      "preferredCategories": [
        {
          "categoryId": "cat-uuid-3",
          "categoryName": "Plumbing Materials",
          "isPrimaryCategory": true,
          "minimumOrderValue": 1000.00,
          "estimatedAnnualSpend": 50000.00
        },
        {
          "categoryId": "cat-uuid-4",
          "categoryName": "Bathroom Fixtures",
          "isPrimaryCategory": true,
          "minimumOrderValue": 800.00,
          "estimatedAnnualSpend": 35000.00
        }
      ],
      "addedByUserName": "Jane Doe",
      "createdAt": "2025-09-15T10:15:00.000Z",
      "updatedAt": "2025-10-01T09:20:00.000Z"
    }
  ]
}
```

---

### 3. Search Company Suppliers

Search suppliers by name within your company's relationships with pagination support.

**Endpoint:** `GET /api/companies/{companyId}/suppliers/search`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Path Parameters:**
- `companyId` (UUID) - Your company's ID

**Query Parameters:**
- `searchText` (string) - Search text for supplier name (case-insensitive, partial match)
- `page` (integer, optional) - Page number (0-based, default: 0)
- `size` (integer, optional) - Items per page (default: 20)
- `sort` (string, optional) - Sort criteria (e.g., "supplierName,asc", "rating,desc")

**Example Request:**
```
GET /api/companies/1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d/suppliers/search?searchText=bunnings&page=0&size=10&sort=supplierName,asc
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      {
        "id": "8f3a2b1c-4d5e-6f7a-8b9c-0d1e2f3a4b5c",
        "companyId": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
        "companyName": "ABC Builders Pty Ltd",
        "supplierId": "d325c0f3-7c01-44fc-a898-6c17b236a6ce",
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
        "deliveryInstructions": "Deliver to site office between 7am-3pm",
        "notes": "Primary hardware supplier",
        "rating": 5,
        "preferredCategories": [
          {
            "categoryId": "cat-uuid-1",
            "categoryName": "General Hardware",
            "isPrimaryCategory": true,
            "minimumOrderValue": 500.00,
            "estimatedAnnualSpend": 25000.00
          }
        ],
        "addedByUserName": "John Smith",
        "createdAt": "2025-10-01T08:30:00.000Z",
        "updatedAt": "2025-10-01T08:30:00.000Z"
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
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 1,
    "empty": false
  }
}
```

**Use Case:** Search for specific suppliers within your company's relationships when you have many suppliers and need to find them quickly.

---

### 4. Get Preferred Suppliers

Get only suppliers marked as preferred for your company.

**Endpoint:** `GET /api/companies/{companyId}/suppliers/preferred`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Path Parameters:**
- `companyId` (UUID) - Your company's ID

**Response:** `200 OK` - Array of relationship objects (only where `preferred: true`)

**Use Case:** Quick access to your go-to suppliers

---

### 5. Get Suppliers by Category

Get suppliers for a specific category, ordered by preference and rating.

**Endpoint:** `GET /api/companies/{companyId}/suppliers/by-category/{categoryId}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Path Parameters:**
- `companyId` (UUID) - Your company's ID
- `categoryId` (UUID) - The consumable category ID

**Response:** `200 OK` - Array of relationship objects, ordered by:
1. Preferred suppliers first
2. Then by rating (highest to lowest)

**Use Case:** When selecting a supplier for a project step requirement

**Example Request:**
```
GET /api/companies/1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d/suppliers/by-category/plumbing-cat-uuid
```

**Example Response:**
```json
{
  "success": true,
  "message": "Suppliers retrieved successfully",
  "data": [
    {
      "id": "rel-uuid-1",
      "supplierName": "Reece Plumbing - Auburn",
      "preferred": true,
      "rating": 5,
      "discountRate": 15.0,
      "accountNumber": "PLM-98765",
      "paymentTerms": "NET_14"
    },
    {
      "id": "rel-uuid-2",
      "supplierName": "Tradelink Plumbing - Homebush",
      "preferred": false,
      "rating": 4,
      "discountRate": 12.0,
      "accountNumber": "TL-54321",
      "paymentTerms": "NET_14"
    }
  ]
}
```

---

### 6. Get Specific Relationship

Get details of your company's relationship with a specific supplier.

**Endpoint:** `GET /api/companies/{companyId}/suppliers/{supplierId}/relationship`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`, `TRADIE`

**Path Parameters:**
- `companyId` (UUID) - Your company's ID
- `supplierId` (UUID) - The supplier's ID

**Response:** `200 OK` - Single relationship object

**Error Response:** `404 Not Found`
```json
{
  "success": false,
  "message": "Relationship not found",
  "data": null
}
```

---

### 7. Update Company-Supplier Relationship

Update your company's relationship details with a supplier.

**Endpoint:** `PUT /api/companies/{companyId}/suppliers/{supplierId}/relationship`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Path Parameters:**
- `companyId` (UUID) - Your company's ID
- `supplierId` (UUID) - The supplier's ID

**Request Headers:**
```
Content-Type: application/json
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "preferred": true,
  "accountNumber": "TRADE-12345-UPDATED",
  "paymentTerms": "NET_14",
  "creditLimit": 75000.00,
  "discountRate": 12.5,
  "contractStartDate": "2024-01-01",
  "contractEndDate": "2025-12-31",
  "deliveryInstructions": "Updated delivery instructions",
  "notes": "Upgraded to preferred supplier",
  "rating": 5
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Response:** `200 OK` - Updated relationship object

---

### 8. Deactivate Relationship

Deactivate your company's relationship with a supplier (soft delete).

**Endpoint:** `DELETE /api/companies/{companyId}/suppliers/{supplierId}/relationship`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Path Parameters:**
- `companyId` (UUID) - Your company's ID
- `supplierId` (UUID) - The supplier's ID

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Relationship deactivated successfully",
  "data": null
}
```

---

## Consumable Categories

### 1. Get All Categories

**Endpoint:** `GET /api/consumable-categories`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "403c1ca1-ddd6-4b64-b802-3c237e19ca35",
      "name": "Concrete & Cement",
      "description": "Concrete, cement, aggregates, and related materials for foundations and structural work",
      "icon": null,
      "displayOrder": 1,
      "active": true,
      "createdAt": "2025-10-01T08:00:00.000Z",
      "updatedAt": "2025-10-01T08:00:00.000Z"
    },
    {
      "id": "0de71499-e8ab-4526-848d-8197b7a4b73f",
      "name": "Steel Reinforcement",
      "description": "Rebar, mesh, steel beams, and structural steel components",
      "icon": null,
      "displayOrder": 2,
      "active": true,
      "createdAt": "2025-10-01T08:00:00.000Z",
      "updatedAt": "2025-10-01T08:00:00.000Z"
    }
  ]
}
```

---

### 2. Get Category by ID

**Endpoint:** `GET /api/consumable-categories/{categoryId}`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Path Parameters:**
- `categoryId` (UUID) - The category's ID

**Response:** `200 OK` - Single category object

---

### 3. Search Categories

**Endpoint:** `GET /api/consumable-categories/search`

**Authorization:** `ADMIN`, `PROJECT_MANAGER`

**Query Parameters:**
- `searchText` (string) - Search text

**Example:** `GET /api/consumable-categories/search?searchText=plumbing`

**Response:** `200 OK` - Array of matching categories

---

## Enums and Constants

### Supplier Types
```typescript
enum SupplierType {
  RETAIL = "RETAIL",                    // Bunnings, Mitre 10
  WHOLESALE = "WHOLESALE",              // Bulk suppliers
  SPECIALIST = "SPECIALIST",            // Reece, Haymans
  ONLINE = "ONLINE",                    // Online-only
  MANUFACTURER = "MANUFACTURER"         // Caroma, Dulux
}
```

### Payment Terms
```typescript
enum PaymentTerms {
  COD = "COD",                          // Cash on Delivery
  NET_7 = "NET_7",                      // Net 7 days
  NET_14 = "NET_14",                    // Net 14 days
  NET_30 = "NET_30",                    // Net 30 days
  NET_60 = "NET_60",                    // Net 60 days
  PREPAID = "PREPAID"                   // Payment before delivery
}
```

---

## Error Handling

### Standard Error Response Structure
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### HTTP Status Codes

| Code | Description | When It Occurs |
|------|-------------|----------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request data, validation failed |
| 403 | Forbidden | User doesn't have required role |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate entry (ABN, email, name already exists) |
| 500 | Internal Server Error | Server-side error |

### Common Validation Errors

**Invalid ABN:**
```json
{
  "success": false,
  "message": "ABN must be 11 digits",
  "data": null
}
```

**Duplicate Supplier:**
```json
{
  "success": false,
  "message": "Supplier with email 'trade@bunnings.com.au' already exists",
  "data": null
}
```

**Relationship Already Exists:**
```json
{
  "success": false,
  "message": "Relationship already exists between this company and supplier",
  "data": null
}
```

**Invalid Rating:**
```json
{
  "success": false,
  "message": "Rating must be between 1 and 5",
  "data": null
}
```

---

## Frontend Integration Examples

### Example 1: Create Supplier and Establish Relationship

```typescript
// Step 1: Create master supplier
const createSupplierResponse = await fetch('/api/suppliers', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    name: "Bunnings Warehouse - Alexandria",
    address: "75 O'Riordan St, Alexandria NSW 2015",
    abn: "63000000001",
    email: "trade.alexandria@bunnings.com.au",
    phone: "(02) 9698 9800",
    contactPerson: "Trade Desk",
    website: "www.bunnings.com.au",
    supplierType: "RETAIL",
    defaultPaymentTerms: "NET_30",
    nationalSupplier: true,
    verified: true
  })
});

const supplier = await createSupplierResponse.json();
const supplierId = supplier.data.id;

// Step 2: Create company relationship
const createRelationshipResponse = await fetch(
  `/api/companies/${companyId}/suppliers/${supplierId}/relationship`,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      preferred: true,
      accountNumber: "TRADE-12345",
      paymentTerms: "NET_30",
      creditLimit: 50000.00,
      discountRate: 10.5,
      rating: 5
    })
  }
);

const relationship = await createRelationshipResponse.json();
console.log('Relationship created:', relationship.data);
```

---

### Example 2: Get Suppliers for Dropdown

```typescript
// Get all company suppliers for dropdown selection
async function getCompanySuppliers(companyId: string) {
  const response = await fetch(`/api/companies/${companyId}/suppliers`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  const result = await response.json();
  
  // Transform for dropdown
  return result.data.map(rel => ({
    value: rel.supplierId,
    label: rel.supplierName,
    accountNumber: rel.accountNumber,
    discount: rel.discountRate,
    preferred: rel.preferred,
    rating: rel.rating
  }));
}
```

---

### Example 3: Smart Supplier Selection by Category

```typescript
// Get recommended suppliers for a specific category
async function getSuppliersForCategory(companyId: string, categoryId: string) {
  const response = await fetch(
    `/api/companies/${companyId}/suppliers/by-category/${categoryId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  const result = await response.json();
  
  // Data is already sorted by preference and rating
  return result.data.map(rel => ({
    id: rel.supplierId,
    name: rel.supplierName,
    preferred: rel.preferred,
    rating: rel.rating,
    discount: rel.discountRate,
    accountNumber: rel.accountNumber,
    paymentTerms: rel.paymentTerms,
    // Auto-fill these in the requirement form
    estimatedDiscount: rel.discountRate,
    suggestedPaymentTerms: rel.paymentTerms
  }));
}
```

---

### Example 4: Update Supplier Rating

```typescript
async function updateSupplierRating(
  companyId: string, 
  supplierId: string, 
  newRating: number
) {
  const response = await fetch(
    `/api/companies/${companyId}/suppliers/${supplierId}/relationship`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        rating: newRating
      })
    }
  );
  
  return await response.json();
}
```

---

### Example 5: Search and Filter Suppliers

```typescript
// Search master suppliers
async function searchSuppliers(searchText: string) {
  const response = await fetch(
    `/api/suppliers/search?searchText=${encodeURIComponent(searchText)}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  return await response.json();
}

// Search company suppliers with pagination
async function searchCompanySuppliers(
  companyId: string, 
  searchText: string, 
  page: number = 0, 
  size: number = 10
) {
  const response = await fetch(
    `/api/companies/${companyId}/suppliers/search?searchText=${encodeURIComponent(searchText)}&page=${page}&size=${size}&sort=supplierName,asc`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  return await response.json();
}

// Filter by type
async function getRetailSuppliers() {
  const response = await fetch('/api/suppliers/by-type/RETAIL', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return await response.json();
}
```

---

## UI Workflow Recommendations

### Supplier Management Screen

**Tab 1: Master Suppliers**
- List all suppliers (from `/api/suppliers`)
- Search functionality
- Filter by type
- Create/Edit/Deactivate suppliers

**Tab 2: Our Relationships**
- List company's supplier relationships (from `/api/companies/{id}/suppliers`)
- Show account numbers, discounts, ratings
- Mark preferred suppliers
- Edit relationship details

**Tab 3: Preferred Suppliers**
- Quick view of preferred suppliers only
- Group by category
- Show discount rates and ratings

### Project Requirement Form

**When Adding a Requirement:**
1. User selects category (e.g., "Bathroom Fixtures")
2. System calls `/api/companies/{companyId}/suppliers/by-category/{categoryId}`
3. Shows suppliers ordered by preference
4. Auto-fills:
   - Account number
   - Discount rate
   - Payment terms
5. User enters item name, quantity, etc.

### Supplier Selection Component
```typescript
interface SupplierSelectorProps {
  companyId: string;
  categoryId: string;
  onSelect: (supplier: SupplierRelationship) => void;
}

// Component automatically:
// 1. Fetches suppliers for category
// 2. Shows preferred suppliers first (with star icon)
// 3. Displays rating (5 stars)
// 4. Shows discount rate
// 5. Indicates account number presence
```

### Company Supplier Search Component
```typescript
interface CompanySupplierSearchProps {
  companyId: string;
  onSelect: (supplier: CompanySupplierRelationship) => void;
}

const CompanySupplierSearch: React.FC<CompanySupplierSearchProps> = ({ 
  companyId, 
  onSelect 
}) => {
  const [searchText, setSearchText] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  const handleSearch = async (value: string, page: number = 0) => {
    if (value.length >= 2) {
      setLoading(true);
      try {
        const response = await fetch(
          `/api/companies/${companyId}/suppliers/search?searchText=${encodeURIComponent(value)}&page=${page}&size=10&sort=supplierName,asc`,
          {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          }
        );
        const result = await response.json();
        setSearchResults(result.data.content);
        setTotalPages(result.data.totalPages);
        setCurrentPage(page);
      } catch (error) {
        console.error('Search failed:', error);
      } finally {
        setLoading(false);
      }
    } else {
      setSearchResults([]);
    }
  };

  const handlePageChange = (page: number) => {
    handleSearch(searchText, page);
  };

  return (
    <div className="supplier-search">
      <input
        type="text"
        placeholder="Search suppliers..."
        value={searchText}
        onChange={(e) => {
          setSearchText(e.target.value);
          handleSearch(e.target.value);
        }}
      />
      
      {loading && <div>Searching...</div>}
      
      <div className="search-results">
        {searchResults.map(supplier => (
          <div 
            key={supplier.id} 
            className="supplier-item"
            onClick={() => onSelect(supplier)}
          >
            <div className="supplier-name">
              {supplier.supplierName}
              {supplier.preferred && <span className="preferred-badge">⭐</span>}
            </div>
            <div className="supplier-details">
              <span>Rating: {'★'.repeat(supplier.rating)}</span>
              <span>Discount: {supplier.discountRate}%</span>
              <span>Account: {supplier.accountNumber}</span>
            </div>
          </div>
        ))}
      </div>
      
      {totalPages > 1 && (
        <div className="pagination">
          <button 
            disabled={currentPage === 0}
            onClick={() => handlePageChange(currentPage - 1)}
          >
            Previous
          </button>
          <span>Page {currentPage + 1} of {totalPages}</span>
          <button 
            disabled={currentPage >= totalPages - 1}
            onClick={() => handlePageChange(currentPage + 1)}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};
```

---

## Testing Checklist

### Master Supplier Operations
- [ ] Create supplier with all fields
- [ ] Create supplier with minimum fields (only name)
- [ ] Get all suppliers
- [ ] Search suppliers
- [ ] Filter by type
- [ ] Filter by category
- [ ] Update supplier details
- [ ] Deactivate supplier
- [ ] Reactivate supplier
- [ ] Handle duplicate ABN error
- [ ] Handle duplicate email error
- [ ] Handle duplicate name error

### Company Relationship Operations
- [ ] Create relationship with all fields
- [ ] Create relationship with minimum fields
- [ ] Get all company relationships
- [ ] Search company suppliers with pagination
- [ ] Search company suppliers with sorting
- [ ] Search company suppliers with empty results
- [ ] Get preferred suppliers only
- [ ] Get suppliers by category
- [ ] Get specific relationship
- [ ] Update relationship (change rating)
- [ ] Update relationship (change discount)
- [ ] Update relationship (toggle preferred)
- [ ] Deactivate relationship
- [ ] Handle duplicate relationship error
- [ ] Verify relationship not found error

### Category Operations
- [ ] Get all categories
- [ ] Search categories
- [ ] Get category by ID

---

## Data Flow Summary

```
┌─────────────────────────────────────────────────────────────┐
│ 1. MASTER LEVEL (Shared Across All Companies)              │
├─────────────────────────────────────────────────────────────┤
│ Supplier                                                     │
│  ├─ Basic Info (name, address, ABN, email, phone)          │
│  ├─ Classification (type: RETAIL/WHOLESALE/etc.)           │
│  ├─ Default Terms (default_payment_terms)                  │
│  └─ Categories (SupplierCategory - what they offer)        │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. COMPANY LEVEL (Company-Specific)                        │
├─────────────────────────────────────────────────────────────┤
│ CompanySupplierRelationship                                 │
│  ├─ Account Details (account_number)                       │
│  ├─ Custom Terms (payment_terms, credit_limit)             │
│  ├─ Negotiated Rates (discount_rate)                       │
│  ├─ Preferences (preferred, rating)                        │
│  ├─ Contract (start_date, end_date)                        │
│  ├─ Instructions (delivery_instructions)                   │
│  └─ Preferred Categories (CompanySupplierCategory)         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. PROJECT LEVEL (Auto-Populated)                          │
├─────────────────────────────────────────────────────────────┤
│ ProjectStepRequirement                                      │
│  ├─ Uses company's relationship data                       │
│  ├─ Auto-fills account number                              │
│  ├─ Applies discount rate                                  │
│  └─ Uses company's payment terms                           │
└─────────────────────────────────────────────────────────────┘
```

---

## Support

For questions or issues:
1. Check this specification document
2. Review Swagger UI at: `http://localhost:8080/swagger-ui.html`
3. Check backend logs for detailed error messages
4. Refer to `SUPPLIER_RELATIONSHIP_REDESIGN.md` for architecture details

---

**Document Version:** 1.0  
**API Version:** Compatible with backend version using migrations V33, V34  
**Last Updated:** October 1, 2025

