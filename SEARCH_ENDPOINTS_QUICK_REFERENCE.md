# Search Endpoints - Quick Reference

## 🔍 All Search Endpoints

### 1. Company Search

**Endpoint**: `POST /api/companies/search`  
**Access**: SUPER_USER, ADMIN  
**Scope**: All companies (not company-scoped)

```json
{
  "searchText": "ABC Construction",
  "activeOnly": true,
  "page": 0,
  "size": 20,
  "sortBy": "name",              // name, createdAt, email, active
  "sortDirection": "ASC"
}
```

**Searches**: name, email, phone, tax number

---

### 2. Supplier Search

**Endpoint**: `POST /api/suppliers/search`  
**Access**: ADMIN, PROJECT_MANAGER, TRADIE  
**Scope**: User's company suppliers

```json
{
  "searchText": "bunnings",
  "activeOnly": true,
  "page": 0,
  "size": 20,
  "sortBy": "name",              // name, createdAt, verified, active, supplierType
  "sortDirection": "ASC",
  
  // Optional supplier-specific filters:
  "supplierType": "RETAIL",      // RETAIL, WHOLESALE, SPECIALIST, ONLINE, MANUFACTURER
  "verified": true,
  "categoryGroup": "Electrical",
  "categoryName": "Cables",
  "paymentTerms": "NET_30"       // COD, NET_7, NET_14, NET_30, NET_60, PREPAID
}
```

**Searches**: name, ABN, email, contact person

---

### 3. Customer Search

**Endpoint**: `POST /api/customers/search`  
**Access**: ADMIN, PROJECT_MANAGER, USER  
**Scope**: User's company customers

```json
{
  "searchText": "john",
  "activeOnly": true,
  "page": 0,
  "size": 20,
  "sortBy": "firstName",         // firstName, lastName, email, createdAt, active
  "sortDirection": "ASC"
}
```

**Searches**: firstName, lastName, email, phone

---

## 📋 Common Request Fields

All search requests support these fields:

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `searchText` | String | null | Text to search (partial matching) |
| `activeOnly` | Boolean | true | Show only active records |
| `page` | Integer | 0 | Page number (0-based) |
| `size` | Integer | 20 | Records per page (max: 100) |
| `sortBy` | String | "name" | Field to sort by |
| `sortDirection` | String | "ASC" | ASC or DESC |

---

## 📊 Common Response Format

All search endpoints return the same structure:

```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      { /* entity 1 */ },
      { /* entity 2 */ }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "offset": 0
    },
    "totalElements": 45,
    "totalPages": 3,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false,
    "numberOfElements": 2,
    "empty": false
  }
}
```

---

## 🚀 Quick Examples

### Get First Page (Default Settings)

```bash
POST /api/{entity}/search
{}
```
Returns: First 20 active records, sorted by name ascending

### Search with Text

```bash
POST /api/{entity}/search
{
  "searchText": "search term"
}
```

### Get All (Active and Inactive)

```bash
POST /api/{entity}/search
{
  "activeOnly": false,
  "size": 100
}
```

### Sort by Date (Newest First)

```bash
POST /api/{entity}/search
{
  "sortBy": "createdAt",
  "sortDirection": "DESC"
}
```

### Pagination (Page 3)

```bash
POST /api/{entity}/search
{
  "page": 2,
  "size": 25
}
```

---

## 💡 TypeScript Example

```typescript
// Base interface
interface BaseSearchRequest {
  searchText?: string;
  activeOnly?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

// Reusable search function
async function search<T>(
  endpoint: string, 
  request: BaseSearchRequest
): Promise<Page<T>> {
  const response = await api.post(endpoint, request);
  return response.data.data;
}

// Usage
const companies = await search<CompanyDto>('/api/companies/search', {
  searchText: 'construction',
  page: 0,
  size: 20
});

const suppliers = await search<SupplierResponse>('/api/suppliers/search', {
  searchText: 'bunnings',
  supplierType: 'RETAIL',
  verified: true
});

const customers = await search<CustomerResponse>('/api/customers/search', {
  searchText: 'john',
  sortBy: 'lastName',
  sortDirection: 'ASC'
});
```

---

## ⚠️ Deprecated Endpoints

These endpoints still work but are deprecated:

| Old Endpoint | New Endpoint |
|--------------|--------------|
| `GET /api/companies/search?q={text}` | `POST /api/companies/search` |
| `GET /api/customers/search?searchTerm={text}` | `POST /api/customers/search` |

**Migration Deadline**: TBD  
**Action Required**: Update frontend to use new POST endpoints

---

## 🔧 Troubleshooting

### No Results Returned
- Check `activeOnly` filter (try setting to `false`)
- Verify search text is correct
- Check pagination parameters

### Sorting Not Working
- Verify `sortBy` field exists for that entity
- Check `sortDirection` is "ASC" or "DESC"

### Access Denied
- Check user role matches endpoint requirements
- For customer/supplier: ensure user belongs to correct company

---

## Quick Access

| What | Endpoint | Access |
|------|----------|--------|
| **All companies** | POST `/api/companies/search` | Super User, Admin |
| **My suppliers** | POST `/api/suppliers/search` | Admin, PM, Tradie |
| **My customers** | POST `/api/customers/search` | Admin, PM, User |

**Tip**: Send empty `{}` to get first page with defaults!

