# Holiday Management API Documentation

## Overview
Simple holiday management system with two levels:
- **Master Holidays**: System-wide holidays (Super User only)
- **Company Holidays**: Company-specific holidays (Admin/Project Manager)

### Automatic Holiday Copy
When a new company is created, all master holidays are automatically copied to that company. This ensures every new company starts with a baseline set of holidays that can be customized later.

## Package Structure
```
com.projectmaster.app.core/
├── entity/
│   ├── MasterHoliday.java
│   └── CompanyHoliday.java
├── repository/
│   ├── MasterHolidayRepository.java
│   └── CompanyHolidayRepository.java
├── service/
│   └── HolidayService.java
├── controller/
│   └── HolidayController.java
└── dto/
    ├── HolidayDateDto.java
    ├── UpdateHolidaysRequest.java
    └── HolidayResponse.java
```

## Database Tables

### master_holidays
- `id` (UUID, PK)
- `holiday_year` (Integer, NOT NULL)
- `holiday_date` (Date, NOT NULL)
- `holiday_name` (String)
- `description` (Text)
- `created_at`, `updated_at` (Timestamps)
- Unique constraint: `(holiday_year, holiday_date)`

### company_holidays
- `id` (UUID, PK)
- `company_id` (UUID, FK → companies, NOT NULL)
- `holiday_year` (Integer, NOT NULL)
- `holiday_date` (Date, NOT NULL)
- `holiday_name` (String)
- `description` (Text)
- `created_at`, `updated_at` (Timestamps)
- Unique constraint: `(company_id, holiday_year, holiday_date)`

## API Endpoints

### 1. Get Master Holidays by Year
```http
GET /api/holidays/master/{year}
Authorization: Bearer <token>
```

**Access**: ADMIN, PROJECT_MANAGER, USER

**Response**:
```json
{
  "success": true,
  "message": "Master holidays retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "holidayYear": 2025,
      "holidayDate": "2025-01-01",
      "holidayName": "New Year's Day",
      "description": "First day of the year"
    }
  ]
}
```

### 2. Get Company Holidays by Year
```http
GET /api/holidays/company/{year}
Authorization: Bearer <token>
```

**Access**: ADMIN, PROJECT_MANAGER, USER  
**Company**: Automatically from logged-in user

**Response**: Same format as master holidays

### 3. Update Master Holidays
```http
PUT /api/holidays/master
Authorization: Bearer <token>
Content-Type: application/json
```

**Access**: SUPER_USER only

**Request Body**:
```json
{
  "holidayYear": 2025,
  "holidays": [
    {
      "holidayDate": "2025-01-01",
      "holidayName": "New Year's Day",
      "description": "First day of the year"
    },
    {
      "holidayDate": "2025-12-25",
      "holidayName": "Christmas Day",
      "description": "Christmas celebration"
    }
  ]
}
```

**Behavior**: 
1. Deletes ALL existing holidays for the year
2. Creates new holidays from the payload

### 4. Update Company Holidays
```http
PUT /api/holidays/company
Authorization: Bearer <token>
Content-Type: application/json
```

**Access**: ADMIN, PROJECT_MANAGER  
**Company**: Automatically from logged-in user

**Request Body**: Same as master holidays

**Behavior**: 
1. Deletes ALL existing holidays for the company and year
2. Creates new holidays from the payload

## Key Features

✅ **Simple Design**: Only essential CRUD operations  
✅ **Security**: Company context from authenticated user  
✅ **Atomic Updates**: Delete + recreate in single transaction  
✅ **Validation**: Input validation on all requests  
✅ **Audit Trail**: Created/updated timestamps on all records  
✅ **Database Constraints**: Unique constraints prevent duplicates  
✅ **Auto-Copy**: Master holidays automatically copied to new companies  

## Usage Examples

### Add Master Holidays for 2025
```bash
curl -X PUT http://localhost:8080/api/holidays/master \
  -H "Authorization: Bearer <super-user-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "holidayYear": 2025,
    "holidays": [
      {
        "holidayDate": "2025-01-01",
        "holidayName": "New Year'\''s Day"
      },
      {
        "holidayDate": "2025-01-26",
        "holidayName": "Australia Day"
      }
    ]
  }'
```

### Get Company Holidays
```bash
curl -X GET http://localhost:8080/api/holidays/company/2025 \
  -H "Authorization: Bearer <token>"
```

### Update Company-Specific Holidays
```bash
curl -X PUT http://localhost:8080/api/holidays/company \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "holidayYear": 2025,
    "holidays": [
      {
        "holidayDate": "2025-03-17",
        "holidayName": "Company Anniversary"
      }
    ]
  }'
```

## Migration
Run `V38__Create_holiday_management_tables.sql` to create the tables.

## Automatic Copy on Company Creation

When a new company is created (via `CompanyService.createCompany()` or `SuperUserService.createCompanyWithAdmin()`), the system automatically:
1. Copies all master holidays to the new company
2. Maintains all holiday attributes (date, name, description)
3. Handles errors gracefully without rolling back company creation

This ensures every company starts with a standard set of holidays.

## Implementation Details

The holiday copy functionality is integrated into:
- `CompanyService.createCompany()` - Regular company creation
- `SuperUserService.createCompanyWithAdmin()` - Super user company creation

Both services call `HolidayService.copyMasterHolidaysToCompany(UUID companyId)` after a company is successfully created. If the copy fails, it's logged but doesn't prevent the company from being created.

## Notes
- `holidayName` and `description` are optional fields
- Update operations are atomic (delete + create in one transaction)
- Company ID is always derived from the logged-in user for security
- **Master holidays require SUPER_USER role** (system-wide), company holidays require ADMIN or PROJECT_MANAGER
- Master holidays are automatically copied to new companies on creation
- Company admins cannot modify master holidays to maintain system-wide consistency

