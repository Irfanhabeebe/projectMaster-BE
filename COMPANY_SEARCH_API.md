# Company Search API Documentation

## Overview
Enhanced company search endpoint with proper pagination, sorting, and filtering capabilities.

---

## New Search Endpoint

### POST `/api/companies/search`

**Access**: SUPER_USER, ADMIN

**Description**: Search companies with advanced filtering, sorting, and pagination support.

---

## Request Format

### CompanySearchRequest

```json
{
  "searchText": "ABC Construction",
  "activeOnly": true,
  "page": 0,
  "size": 20,
  "sortBy": "name",
  "sortDirection": "ASC"
}
```

### Request Parameters

| Field | Type | Required | Default | Description | Allowed Values |
|-------|------|----------|---------|-------------|----------------|
| `searchText` | String | No | null | Search text (searches name, email, phone, tax number) | Any text |
| `activeOnly` | Boolean | No | true | Show only active companies | true, false |
| `page` | Integer | No | 0 | Page number (0-based) | 0-∞ |
| `size` | Integer | No | 20 | Page size | 1-100 |
| `sortBy` | String | No | "name" | Field to sort by | "name", "createdAt", "email", "active" |
| `sortDirection` | String | No | "ASC" | Sort direction | "ASC", "DESC" |

---

## Response Format

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "name": "ABC Construction Company",
        "address": "123 Main St, Sydney NSW 2000",
        "phone": "02-1234-5678",
        "email": "contact@abc-construction.com.au",
        "website": "https://www.abc-construction.com.au",
        "taxNumber": "ABN 12 345 678 901",
        "active": true,
        "createdAt": "2025-01-15T10:30:00Z",
        "updatedAt": "2025-01-20T14:15:00Z"
      },
      {
        "id": "650e8400-e29b-41d4-a716-446655440001",
        "name": "ABC Plumbing Services",
        "address": "456 George St, Melbourne VIC 3000",
        "phone": "03-9876-5432",
        "email": "info@abc-plumbing.com.au",
        "website": "https://www.abc-plumbing.com.au",
        "taxNumber": "ABN 98 765 432 109",
        "active": true,
        "createdAt": "2025-02-01T09:00:00Z",
        "updatedAt": "2025-02-05T16:30:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 1,
    "totalElements": 2,
    "last": true,
    "size": 20,
    "number": 0,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "numberOfElements": 2,
    "first": true,
    "empty": false
  }
}
```

### Error Response (400 Bad Request)

```json
{
  "success": false,
  "message": "Invalid search parameters",
  "data": null
}
```

### Error Response (500 Internal Server Error)

```json
{
  "success": false,
  "message": "Error searching companies: Database connection failed",
  "data": null
}
```

---

## Search Behavior

### Search Text Matching
The `searchText` parameter searches across multiple fields using partial matching (LIKE):
- Company name
- Email address
- Phone number
- Tax number (ABN/ACN)

**Example**: Searching for "ABC" will match:
- "ABC Construction"
- "XYZ Company" with email "contact@abc.com"
- "Builder Co" with phone "123-ABC-4567"
- Company with tax number "ABN ABC 123"

### Active Filter
- `activeOnly: true` (default) - Returns only active companies
- `activeOnly: false` - Returns all companies (active and inactive)

### Sorting Options

| Sort By | Description |
|---------|-------------|
| `name` | Company name (default) |
| `createdAt` | Creation date |
| `email` | Email address |
| `active` | Active status |

**Sort Direction**: ASC (ascending) or DESC (descending)

---

## Usage Examples

### Example 1: Basic Search

**Request**:
```bash
POST /api/companies/search
Content-Type: application/json
Authorization: Bearer {token}

{
  "searchText": "construction",
  "activeOnly": true
}
```

**Response**: Returns first page (20 items) of active companies with "construction" in name/email/phone/tax number, sorted by name ascending.

---

### Example 2: Pagination

**Request**:
```bash
POST /api/companies/search
Content-Type: application/json
Authorization: Bearer {token}

{
  "searchText": "",
  "activeOnly": true,
  "page": 2,
  "size": 10
}
```

**Response**: Returns page 3 (0-based index) with 10 active companies per page.

---

### Example 3: Custom Sorting

**Request**:
```bash
POST /api/companies/search
Content-Type: application/json
Authorization: Bearer {token}

{
  "searchText": "",
  "activeOnly": false,
  "sortBy": "createdAt",
  "sortDirection": "DESC",
  "page": 0,
  "size": 50
}
```

**Response**: Returns all companies (active and inactive), sorted by creation date (newest first), maximum 50 per page.

---

### Example 4: Find All Inactive Companies

**Request**:
```bash
POST /api/companies/search
Content-Type: application/json
Authorization: Bearer {token}

{
  "searchText": "",
  "activeOnly": false,
  "sortBy": "active",
  "sortDirection": "ASC"
}
```

**Response**: Returns all companies sorted by active status (inactive companies first).

---

## Frontend Integration

### TypeScript Interface

```typescript
interface CompanySearchRequest {
  searchText?: string;
  activeOnly?: boolean;
  page?: number;
  size?: number;
  sortBy?: 'name' | 'createdAt' | 'email' | 'active';
  sortDirection?: 'ASC' | 'DESC';
}

interface CompanyDto {
  id: string;
  name: string;
  address?: string;
  phone?: string;
  email?: string;
  website?: string;
  taxNumber?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    offset: number;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}
```

### React Hook Example

```typescript
import { useState, useEffect } from 'react';
import axios from 'axios';

function useCompanySearch() {
  const [companies, setCompanies] = useState<PageResponse<CompanyDto> | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const searchCompanies = async (searchRequest: CompanySearchRequest) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await axios.post<ApiResponse<PageResponse<CompanyDto>>>(
        '/api/companies/search',
        searchRequest
      );
      
      if (response.data.success) {
        setCompanies(response.data.data);
      } else {
        setError(response.data.message);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to search companies');
    } finally {
      setLoading(false);
    }
  };

  return { companies, loading, error, searchCompanies };
}

// Usage in component
function CompanySearchPage() {
  const { companies, loading, error, searchCompanies } = useCompanySearch();
  const [searchText, setSearchText] = useState('');
  const [activeOnly, setActiveOnly] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);

  const handleSearch = () => {
    searchCompanies({
      searchText,
      activeOnly,
      page: currentPage,
      size: 20,
      sortBy: 'name',
      sortDirection: 'ASC'
    });
  };

  const handlePageChange = (newPage: number) => {
    setCurrentPage(newPage);
    searchCompanies({
      searchText,
      activeOnly,
      page: newPage,
      size: 20,
      sortBy: 'name',
      sortDirection: 'ASC'
    });
  };

  return (
    <div>
      <input 
        value={searchText} 
        onChange={(e) => setSearchText(e.target.value)}
        placeholder="Search companies..."
      />
      <label>
        <input 
          type="checkbox" 
          checked={activeOnly}
          onChange={(e) => setActiveOnly(e.target.checked)}
        />
        Active Only
      </label>
      <button onClick={handleSearch}>Search</button>

      {loading && <p>Loading...</p>}
      {error && <p>Error: {error}</p>}
      
      {companies && (
        <>
          <div>
            {companies.content.map(company => (
              <div key={company.id}>
                <h3>{company.name}</h3>
                <p>{company.email}</p>
                <p>{company.phone}</p>
                <span>{company.active ? 'Active' : 'Inactive'}</span>
              </div>
            ))}
          </div>
          
          <Pagination 
            currentPage={companies.number}
            totalPages={companies.totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </div>
  );
}
```

---

## Legacy Endpoint (Deprecated)

### GET `/api/companies/search?q={searchTerm}`

**Status**: ⚠️ DEPRECATED - Use POST `/api/companies/search` instead

**Why Deprecated**: 
- No pagination support
- No sorting options
- No active/inactive filtering
- Only searches name and email
- Returns all results at once (performance issues with large datasets)

**Migration Path**: Update your frontend to use the new POST endpoint with `CompanySearchRequest`.

---

## Comparison: Old vs New

| Feature | Old Endpoint (GET) | New Endpoint (POST) |
|---------|-------------------|---------------------|
| Method | GET /search?q=text | POST /search |
| Pagination | ❌ No | ✅ Yes |
| Sorting | ❌ No | ✅ Yes (4 fields) |
| Active Filter | ❌ Only active | ✅ Configurable |
| Search Fields | Name, Email | Name, Email, Phone, Tax Number |
| Response Format | List | Page (with metadata) |
| Performance | Poor (all results) | Good (paginated) |

---

## Testing

### cURL Examples

#### Basic Search
```bash
curl -X POST "http://localhost:8080/api/companies/search" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "searchText": "construction",
    "activeOnly": true,
    "page": 0,
    "size": 20
  }'
```

#### Search All Companies (Active and Inactive)
```bash
curl -X POST "http://localhost:8080/api/companies/search" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "searchText": "",
    "activeOnly": false,
    "page": 0,
    "size": 50,
    "sortBy": "createdAt",
    "sortDirection": "DESC"
  }'
```

#### Search with Custom Sorting
```bash
curl -X POST "http://localhost:8080/api/companies/search" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "searchText": "plumbing",
    "activeOnly": true,
    "page": 1,
    "size": 10,
    "sortBy": "email",
    "sortDirection": "ASC"
  }'
```

---

## Implementation Details

### Database Query
The search query performs:
1. **Active Filter**: Filters by active status if `activeOnly = true`
2. **Text Search**: Searches across 4 fields using LIKE with partial matching
3. **Dynamic Sorting**: Uses CASE statements for dynamic field sorting
4. **Pagination**: Handled by Spring Data JPA with `Pageable`

### Performance
- **Indexed Fields**: name, email (recommended for better search performance)
- **Pagination**: Prevents loading all records at once
- **Efficient Query**: Single query with all filters combined

---

## Migration Guide

### From Old Endpoint (GET)

**Before**:
```typescript
// Old approach
const searchCompanies = async (searchText: string) => {
  const response = await api.get(`/api/companies/search?q=${searchText}`);
  return response.data.data; // Returns List<CompanyDto>
};
```

**After**:
```typescript
// New approach
const searchCompanies = async (searchRequest: CompanySearchRequest) => {
  const response = await api.post('/api/companies/search', searchRequest);
  return response.data.data; // Returns Page<CompanyDto>
};

// Usage
const result = await searchCompanies({
  searchText: "construction",
  activeOnly: true,
  page: 0,
  size: 20,
  sortBy: "name",
  sortDirection: "ASC"
});

// Access companies
const companies = result.content;
const totalCompanies = result.totalElements;
const totalPages = result.totalPages;
```

---

## Best Practices

### 1. Default Values
All optional fields have sensible defaults. Minimum required request:
```json
{}
```
This will return the first 20 active companies sorted by name.

### 2. Search Optimization
For better UX, implement debouncing on search input:
```typescript
const debouncedSearch = useDebounce(searchText, 300); // 300ms delay

useEffect(() => {
  searchCompanies({
    searchText: debouncedSearch,
    activeOnly: true,
    page: 0,
    size: 20
  });
}, [debouncedSearch]);
```

### 3. Pagination
Always show pagination controls when `totalPages > 1`:
```typescript
if (companies.totalPages > 1) {
  // Show pagination component
}
```

### 4. Loading States
Show loading indicator during search:
```typescript
{loading && <Spinner />}
{!loading && companies && <CompanyList companies={companies.content} />}
```

---

## Security

- **Access Control**: Only SUPER_USER and ADMIN roles can search companies
- **Validation**: Request validated using `@Valid` annotation
- **SQL Injection Protection**: Parameters are properly escaped by JPA

---

## Related Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/companies` | GET | Get all active companies (no pagination) |
| `/api/companies/{id}` | GET | Get company by ID |
| `/api/companies` | POST | Create new company |
| `/api/companies/{id}` | PUT | Update company |
| `/api/companies/{id}/activate` | POST | Activate company |
| `/api/companies/{id}/deactivate` | POST | Deactivate company |

---

## Change Log

### October 13, 2025
- ✅ Created `CompanySearchRequest` DTO
- ✅ Added `searchCompanies()` method to `CompanyRepository`
- ✅ Added `searchCompanies(CompanySearchRequest)` method to `CompanyService`
- ✅ Added POST `/api/companies/search` endpoint to `CompanyController`
- ✅ Deprecated old GET `/api/companies/search` endpoint
- ✅ Added Swagger documentation
- ✅ Build verified successfully

---

## Support

For issues or questions:
- Check this documentation
- Review similar Supplier Search API implementation
- Contact the backend development team

