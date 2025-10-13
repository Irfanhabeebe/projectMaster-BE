# Supplier API - Quick Reference for Frontend

## Base URL
```
http://localhost:8080/api
```

## Authentication
All requests require JWT token in header:
```
Authorization: Bearer <your-jwt-token>
```

---

## 1️⃣ MASTER SUPPLIER APIS

### Create Supplier
```http
POST /api/suppliers
Content-Type: application/json

REQUEST:
{
  "name": "Bunnings Warehouse - Alexandria",          // Required
  "address": "75 O'Riordan St, Alexandria NSW 2015",  // Optional
  "abn": "63000000001",                                // Optional, 11 digits
  "email": "trade.alexandria@bunnings.com.au",         // Optional, valid email
  "phone": "(02) 9698 9800",                           // Optional
  "contactPerson": "Trade Desk",                       // Optional
  "website": "www.bunnings.com.au",                    // Optional
  "supplierType": "RETAIL",                            // Optional: RETAIL|WHOLESALE|SPECIALIST|ONLINE|MANUFACTURER
  "defaultPaymentTerms": "NET_30",                     // Optional: COD|NET_7|NET_14|NET_30|NET_60|PREPAID
  "verified": true,                                    // Optional, default: false
  "categories": [                                      // Optional, array of category UUIDs
    "403c1ca1-ddd6-4b64-b802-3c237e19ca35",           // General Hardware
    "0de71499-e8ab-4526-848d-8197b7a4b73f"            // Plumbing Materials
  ]
}

RESPONSE: 201 Created
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

### Get All Suppliers
```http
GET /api/suppliers

RESPONSE: 200 OK
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
      "categories": [
        {
          "categoryId": "uuid",
          "categoryName": "General Hardware",
          "categoryGroup": "Hardware & Fasteners",
          "isPrimaryCategory": true
        },
        {
          "categoryId": "uuid-2",
          "categoryName": "Plumbing Materials",
          "categoryGroup": "Plumbing",
          "isPrimaryCategory": false
        }
      ],
      "createdAt": "2025-10-01T08:22:35Z",
      "updatedAt": "2025-10-01T08:22:35Z"
    }
  ]
}
```

### Get Supplier by ID
```http
GET /api/suppliers/{supplierId}

RESPONSE: 200 OK
// Same structure as single supplier in array above
```

### Search Suppliers (with Pagination)
```http
GET /api/suppliers/search?searchText=bunnings&page=0&size=10&sort=name,asc

RESPONSE: 200 OK
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      // Array of supplier objects
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
    "totalElements": 25,
    "totalPages": 3,
    "last": false,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 10,
    "empty": false
  }
}

// Pagination Parameters:
// page=0 (default: 0) - Page number (0-indexed)
// size=10 (default: 20) - Number of items per page
// sort=name,asc - Sort by field (optional, can be: name, supplierType, etc.)
```

### Get Suppliers by Category
```http
GET /api/suppliers/by-category/{categoryId}

RESPONSE: 200 OK
// Array of suppliers that serve this category
```

### Get Suppliers by Type
```http
GET /api/suppliers/by-type/RETAIL

RESPONSE: 200 OK
// Array of suppliers of this type
```

### Update Supplier
```http
PUT /api/suppliers/{supplierId}
Content-Type: application/json

REQUEST: Same as Create Supplier (all fields optional)

RESPONSE: 200 OK
// Updated supplier object
```

### Deactivate Supplier
```http
DELETE /api/suppliers/{supplierId}

RESPONSE: 200 OK
{
  "success": true,
  "message": "Supplier deactivated successfully",
  "data": null
}
```

### Reactivate Supplier
```http
POST /api/suppliers/{supplierId}/reactivate

RESPONSE: 200 OK
{
  "success": true,
  "message": "Supplier reactivated successfully",
  "data": null
}
```

---

## 2️⃣ COMPANY-SUPPLIER RELATIONSHIP APIS

### Create Relationship
```http
POST /api/companies/{companyId}/suppliers/{supplierId}/relationship
Content-Type: application/json

REQUEST:
{
  "preferred": true,                                   // Optional, default: false
  "accountNumber": "TRADE-12345",                      // Optional
  "paymentTerms": "NET_30",                            // Optional: COD|NET_7|NET_14|NET_30|NET_60|PREPAID
  "creditLimit": 50000.00,                             // Optional, decimal
  "discountRate": 10.5,                                // Optional, 0-100
  "contractStartDate": "2024-01-01",                   // Optional, format: YYYY-MM-DD
  "contractEndDate": "2024-12-31",                     // Optional, format: YYYY-MM-DD
  "deliveryInstructions": "Deliver to site office",   // Optional
  "notes": "Primary supplier for hardware",            // Optional
  "rating": 5,                                         // Optional, 1-5
  "preferredCategories": [                             // Optional, array of category UUIDs
    "403c1ca1-ddd6-4b64-b802-3c237e19ca35",           // General Hardware
    "0de71499-e8ab-4526-848d-8197b7a4b73f"            // Electrical Components
  ]
}

RESPONSE: 201 Created
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
    "deliveryInstructions": "Deliver to site office",
    "notes": "Primary supplier for hardware",
    "rating": 5,
    "preferredCategories": [
      {
        "categoryId": "403c1ca1-ddd6-4b64-b802-3c237e19ca35",
        "categoryName": "General Hardware",
        "categoryGroup": "Hardware & Fasteners",
        "isPrimaryCategory": false,
        "minimumOrderValue": null,
        "estimatedAnnualSpend": null
      },
      {
        "categoryId": "0de71499-e8ab-4526-848d-8197b7a4b73f",
        "categoryName": "Electrical Components",
        "categoryGroup": "Electrical",
        "isPrimaryCategory": false,
        "minimumOrderValue": null,
        "estimatedAnnualSpend": null
      }
    ],
    "addedByUserName": "John Smith",
    "createdAt": "2025-10-01T08:30:00.000Z",
    "updatedAt": "2025-10-01T08:30:00.000Z"
  }
}
```

### Get All Company Suppliers (with Pagination)
```http
GET /api/companies/{companyId}/suppliers?page=0&size=10&sort=supplierName,asc

RESPONSE: 200 OK
{
  "success": true,
  "message": "Suppliers retrieved successfully",
  "data": {
    "content": [
      // Array of relationship objects as shown above
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 15,
    "totalPages": 2,
    "last": false,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 10,
    "empty": false
  }
}

// Pagination Parameters:
// page=0 - Page number (0-indexed)
// size=10 - Items per page
// sort=supplierName,asc - Sort by field (rating,desc for highest rated first)
```

### Search Company Suppliers (with Pagination)
```http
GET /api/companies/{companyId}/suppliers/search?searchText=bunnings&page=0&size=10&sort=supplierName,asc

RESPONSE: 200 OK
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      // Array of relationship objects matching search criteria
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
    "totalElements": 3,
    "totalPages": 1,
    "last": true,
    "first": true,
    "size": 10,
    "number": 0,
    "numberOfElements": 3,
    "empty": false
  }
}

// Search Parameters:
// searchText=bunnings - Search text (case-insensitive, partial match)
// page=0 - Page number (0-indexed)
// size=10 - Items per page
// sort=supplierName,asc - Sort by field (rating,desc for highest rated first)
```

### Get Preferred Suppliers Only
```http
GET /api/companies/{companyId}/suppliers/preferred

RESPONSE: 200 OK
// Array of relationships where preferred=true
```

### Get Suppliers by Category (Smart Selection)
```http
GET /api/companies/{companyId}/suppliers/by-category/{categoryId}

RESPONSE: 200 OK
// Array of relationships, ordered by:
// 1. Preferred first
// 2. Then by rating (highest to lowest)
```

### Get Specific Relationship
```http
GET /api/companies/{companyId}/suppliers/{supplierId}/relationship

RESPONSE: 200 OK
// Single relationship object
```

### Update Relationship
```http
PUT /api/companies/{companyId}/suppliers/{supplierId}/relationship
Content-Type: application/json

REQUEST: Same as Create Relationship (all fields optional)
// Only provided fields will be updated

RESPONSE: 200 OK
// Updated relationship object
```

### Deactivate Relationship
```http
DELETE /api/companies/{companyId}/suppliers/{supplierId}/relationship

RESPONSE: 200 OK
{
  "success": true,
  "message": "Relationship deactivated successfully",
  "data": null
}
```

---

## 3️⃣ CONSUMABLE CATEGORY APIS

### Get All Categories (Master List)
**Use Case:** Populate multi-select dropdowns when creating suppliers or relationships

```http
GET /api/consumable-categories

RESPONSE: 200 OK
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "403c1ca1-ddd6-4b64-b802-3c237e19ca35",
      "name": "Concrete & Cement",
      "description": "Concrete, cement, aggregates, and related materials for foundations and structural work",
      "categoryGroup": "Foundation & Structure",
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
      "categoryGroup": "Foundation & Structure",
      "icon": null,
      "displayOrder": 2,
      "active": true,
      "createdAt": "2025-10-01T08:00:00.000Z",
      "updatedAt": "2025-10-01T08:00:00.000Z"
    },
    {
      "id": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
      "name": "Plumbing Materials",
      "description": "Plumbing pipes, fittings, and fixtures",
      "categoryGroup": "Plumbing",
      "icon": null,
      "displayOrder": 3,
      "active": true,
      "createdAt": "2025-10-01T08:00:00.000Z",
      "updatedAt": "2025-10-01T08:00:00.000Z"
    }
    // ... 52 more categories (55 total for Australian construction)
  ]
}
```

### Get Category by ID
```http
GET /api/consumable-categories/{categoryId}

RESPONSE: 200 OK
{
  "success": true,
  "message": "Category retrieved successfully",
  "data": {
    "id": "403c1ca1-ddd6-4b64-b802-3c237e19ca35",
    "name": "Concrete & Cement",
    "description": "Concrete, cement, aggregates, and related materials for foundations and structural work",
    "icon": null,
    "displayOrder": 1,
    "active": true,
    "createdAt": "2025-10-01T08:00:00.000Z",
    "updatedAt": "2025-10-01T08:00:00.000Z"
  }
}
```

### Search Categories
**Use Case:** Filter categories while user types in search box

```http
GET /api/consumable-categories/search?searchText=plumbing

RESPONSE: 200 OK
{
  "success": true,
  "message": "Search completed successfully",
  "data": [
    {
      "id": "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
      "name": "Plumbing Materials",
      "description": "Plumbing pipes, fittings, and fixtures",
      "displayOrder": 3,
      "active": true
    },
    {
      "id": "2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "name": "Plumbing Pipes & Fittings",
      "description": "Copper pipes, PVC pipes, fittings, and plumbing connections",
      "displayOrder": 17,
      "active": true
    },
    {
      "id": "3c4d5e6f-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
      "name": "Plumbing Accessories",
      "description": "Plumbing valves, connectors, and miscellaneous plumbing parts",
      "displayOrder": 21,
      "active": true
    }
  ]
}
```

---

## 🎨 FRONTEND COMPONENT EXAMPLES

### Category Multi-Select (For Supplier Creation)
```typescript
// Step 1: Fetch all categories
const categoriesResponse = await fetch('/api/consumable-categories', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data: categories } = await categoriesResponse.json();

// Step 2: Transform for multi-select component
const categoryOptions = categories.map(cat => ({
  value: cat.id,
  label: cat.name,
  description: cat.description
}));

// Step 3: When user submits supplier form
const selectedCategoryIds = ['uuid-1', 'uuid-2', 'uuid-3']; // From multi-select
const supplierData = {
  name: "Reece Plumbing - Auburn",
  supplierType: "SPECIALIST",
  defaultPaymentTerms: "NET_14",
  categories: selectedCategoryIds  // ← Pass array of UUIDs
};

const response = await fetch('/api/suppliers', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify(supplierData)
});
```

### Category Search with Autocomplete
```typescript
// Real-time search as user types
const searchCategories = async (searchText: string) => {
  const response = await fetch(
    `/api/consumable-categories/search?searchText=${encodeURIComponent(searchText)}`,
    { headers: { 'Authorization': `Bearer ${token}` } }
  );
  const { data } = await response.json();
  return data;
};

// Usage in React component
const [searchResults, setSearchResults] = useState([]);

const handleSearch = async (value: string) => {
  if (value.length >= 2) {
    const results = await searchCategories(value);
    setSearchResults(results);
  }
};
```

### Supplier Dropdown
```typescript
// Fetch and display company's suppliers
const response = await fetch(`/api/companies/${companyId}/suppliers`);
const { data } = await response.json();

const options = data.map(rel => ({
  value: rel.supplierId,
  label: `${rel.supplierName} ${rel.preferred ? '⭐' : ''} (${rel.discountRate}% off)`,
  accountNumber: rel.accountNumber,
  paymentTerms: rel.paymentTerms,
  categories: rel.preferredCategories.map(c => c.categoryName).join(', ')
}));
```

### Company Supplier Search Component
```typescript
// Search company suppliers with pagination
const searchCompanySuppliers = async (
  companyId: string, 
  searchText: string, 
  page: number = 0, 
  size: number = 10
) => {
  const response = await fetch(
    `/api/companies/${companyId}/suppliers/search?searchText=${encodeURIComponent(searchText)}&page=${page}&size=${size}&sort=supplierName,asc`,
    { headers: { 'Authorization': `Bearer ${token}` } }
  );
  const { data } = await response.json();
  return data;
};

// Usage in React component
const [searchResults, setSearchResults] = useState([]);
const [currentPage, setCurrentPage] = useState(0);

const handleSearch = async (value: string) => {
  if (value.length >= 2) {
    const results = await searchCompanySuppliers(companyId, value, 0, 10);
    setSearchResults(results.content);
    setCurrentPage(0);
  } else {
    setSearchResults([]);
  }
};

const handlePageChange = async (page: number) => {
  const results = await searchCompanySuppliers(companyId, searchText, page, 10);
  setSearchResults(results.content);
  setCurrentPage(page);
};
```

### Category-Based Supplier Selector
```typescript
// Show suppliers for selected category
const categoryId = selectedCategory.id;
const response = await fetch(
  `/api/companies/${companyId}/suppliers/by-category/${categoryId}`
);
const { data } = await response.json();

// Data is already sorted by preference and rating
// Show preferred suppliers with badges/stars
```

### Supplier Rating Component
```typescript
// Update rating
const updateRating = async (newRating: number) => {
  await fetch(
    `/api/companies/${companyId}/suppliers/${supplierId}/relationship`,
    {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rating: newRating })
    }
  );
};
```

---

## 📋 ENUMS (TypeScript)

```typescript
enum SupplierType {
  RETAIL = "RETAIL",
  WHOLESALE = "WHOLESALE",
  SPECIALIST = "SPECIALIST",
  ONLINE = "ONLINE",
  MANUFACTURER = "MANUFACTURER"
}

enum PaymentTerms {
  COD = "COD",
  NET_7 = "NET_7",
  NET_14 = "NET_14",
  NET_30 = "NET_30",
  NET_60 = "NET_60",
  PREPAID = "PREPAID"
}
```

---

## ⚠️ COMMON ERRORS

### 400 Bad Request
```json
{ "success": false, "message": "ABN must be 11 digits" }
{ "success": false, "message": "Rating must be between 1 and 5" }
```

### 403 Forbidden
```json
{ "success": false, "message": "Access denied" }
```

### 404 Not Found
```json
{ "success": false, "message": "Supplier not found with ID: xxx" }
{ "success": false, "message": "Relationship not found" }
```

### 409 Conflict
```json
{ "success": false, "message": "Supplier with name 'XXX' already exists" }
{ "success": false, "message": "Supplier with ABN '12345678901' already exists" }
{ "success": false, "message": "Relationship already exists between this company and supplier" }
```

---

## 🚀 QUICK START GUIDE

### Step 1: Display Suppliers
```javascript
// Get all company suppliers
GET /api/companies/{companyId}/suppliers
```

### Step 2: Create New Relationship
```javascript
// User selects a supplier from master list
// Then create relationship
POST /api/companies/{companyId}/suppliers/{supplierId}/relationship
```

### Step 3: Use in Project
```javascript
// When creating project requirement, fetch suppliers by category
GET /api/companies/{companyId}/suppliers/by-category/{categoryId}
// Shows preferred suppliers first with their discount rates
```

---

## 📊 RESPONSE WRAPPER

All responses follow this structure:
```typescript
interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T | null;
}
```

**Success Example:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* actual data */ }
}
```

**Error Example:**
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## 🔑 KEY POINTS FOR FRONTEND

1. **Two Levels:** Master suppliers vs. company relationships
2. **Categories Management:**
   - **Master Level:** `categories[]` array when creating supplier (what they offer)
   - **Company Level:** `preferredCategories[]` when creating relationship (what you prefer from them)
3. **Create Flow:** 
   - Get categories list first
   - Create supplier with selected categories
   - Create relationship with preferred categories subset
4. **Smart Selection:** Use `/by-category/` endpoint for category-based selection
5. **Preferred Suppliers:** Show ⭐ for `preferred: true`
6. **Rating Display:** Show 1-5 stars for `rating` field
7. **Discount Badge:** Show discount percentage prominently
8. **Account Number:** Display when present, helps users identify existing relationships
9. **All Fields Optional:** Except supplier name (required) in create supplier

---

## 🔄 COMPLETE WORKFLOW EXAMPLE

### Scenario: Add Reece Plumbing as a Supplier

```typescript
// STEP 1: Fetch all available categories
const categoriesResponse = await fetch('/api/consumable-categories');
const { data: allCategories } = await categoriesResponse.json();

// STEP 2: User selects categories that Reece offers
const selectedCategories = [
  "plumbing-materials-uuid",
  "bathroom-fixtures-uuid",
  "hot-water-systems-uuid",
  "drainage-stormwater-uuid"
];

// STEP 3: Create master supplier record
const createSupplierResponse = await fetch('/api/suppliers', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    name: "Reece Plumbing - Auburn",
    address: "119-121 Parramatta Rd, Auburn NSW 2144",
    abn: "34567890123",
    email: "auburn@reece.com.au",
    phone: "(02) 9646 2411",
    contactPerson: "Branch Manager",
    website: "www.reece.com.au",
    supplierType: "SPECIALIST",
    defaultPaymentTerms: "NET_14",
    nationalSupplier: false,
    verified: true,
    categories: selectedCategories  // ← What Reece offers
  })
});

const { data: supplier } = await createSupplierResponse.json();
const supplierId = supplier.id;

// STEP 4: Create company relationship with preferred categories
// User selects which categories they prefer from Reece
const preferredCategories = [
  "plumbing-materials-uuid",   // We prefer Reece for plumbing
  "bathroom-fixtures-uuid"      // and bathroom fixtures
  // NOT hot-water or drainage (we use other suppliers)
];

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
      accountNumber: "PLM-98765",
      paymentTerms: "NET_14",
      creditLimit: 75000.00,
      discountRate: 15.0,
      rating: 5,
      deliveryInstructions: "Deliver to plumber's storage area, rear entrance",
      notes: "Primary plumbing supplier, excellent service",
      preferredCategories: preferredCategories  // ← What we prefer from Reece
    })
  }
);

// STEP 5: Result - Now when creating project requirements:
// - For "Plumbing Materials" → Reece shows first (preferred + high rating)
// - For "Bathroom Fixtures" → Reece shows first
// - For "Hot Water Systems" → Reece available but not preferred
// - Auto-fills: Account PLM-98765, 15% discount, NET_14 terms
```

---

## 📋 CATEGORY MANAGEMENT SUMMARY

### Master Level (Supplier)
```
Reece Plumbing offers:
✓ Plumbing Materials
✓ Bathroom Fixtures  
✓ Hot Water Systems
✓ Drainage & Stormwater
```

### Company Level (Relationship)
```
ABC Builders prefers Reece for:
⭐ Plumbing Materials (primary)
⭐ Bathroom Fixtures (primary)
   Hot Water Systems (available but not preferred)
   Drainage & Stormwater (available but not preferred)
```

### Benefits
- Frontend shows ALL categories Reece offers
- But highlights the ones YOUR company prefers
- Smart filtering: "Show me suppliers preferred for Plumbing Materials"
- Auto-apply: Account number, discount rate, payment terms

---

## 🎨 UI GROUPING WITH categoryGroup

### Group Categories in UI

The `categoryGroup` field allows you to group the 55 categories in the UI:

```typescript
// Group categories by categoryGroup
const groupedCategories = categories.reduce((groups, category) => {
  const group = category.categoryGroup || 'Other';
  if (!groups[group]) {
    groups[group] = [];
  }
  groups[group].push(category);
  return groups;
}, {});

// Result:
{
  "Foundation & Structure": [
    { id: "...", name: "Concrete & Cement", ... },
    { id: "...", name: "Steel Reinforcement", ... },
    { id: "...", name: "Formwork Materials", ... },
    { id: "...", name: "Waterproofing Materials", ... }
  ],
  "Plumbing": [
    { id: "...", name: "Plumbing Pipes & Fittings", ... },
    { id: "...", name: "Bathroom Fixtures", ... },
    { id: "...", name: "Kitchen Fixtures", ... },
    { id: "...", name: "Hot Water Systems", ... },
    { id: "...", name: "Plumbing Accessories", ... }
  ],
  "Electrical": [ ... ],
  ...
}
```

### Display as Grouped Multi-Select

```jsx
// React example
{Object.entries(groupedCategories).map(([group, categories]) => (
  <optgroup key={group} label={group}>
    {categories.map(cat => (
      <option key={cat.id} value={cat.id}>
        {cat.name}
      </option>
    ))}
  </optgroup>
))}
```

### Category Groups Available

1. **Foundation & Structure** (4 categories)
2. **Framing & Structure** (4 categories)
3. **Roofing** (4 categories)
4. **Exterior** (4 categories)
5. **Plumbing** (5 categories)
6. **Electrical** (5 categories)
7. **Flooring** (3 categories)
8. **Interior** (5 categories)
9. **Insulation & Energy** (4 categories)
10. **Hardware & Fasteners** (3 categories)
11. **Site & Safety** (4 categories)
12. **Appliances & Fixtures** (3 categories)
13. **Finishing Touches** (3 categories)
14. **Specialized** (4 categories)

**Total: 14 groups, 55 categories**

---

**Ready to integrate!** 🎉

