# Super User Implementation Guide

## Overview

The application now supports a **Super User** role that has the following characteristics:

1. **Super users can add companies and admin users** - Only super users can create new companies with their initial admin users
2. **Super users exist across all companies** - They are not tied to any specific company
3. **Super users have access to everything** - They can view and manage all companies, users, and data across the entire system
4. **Company-specific operations** - After a super user creates a company with an admin, that admin manages the company-specific operations

## Key Features Implemented

### 1. Super User Role
- Added `SUPER_USER` to the `UserRole` enum
- Super users don't belong to any company (company_id is null)
- Database constraint ensures only SUPER_USER role can have null company_id

### 2. Database Changes
- Updated user_role enum to include 'SUPER_USER'
- Modified users table to allow nullable company_id for super users
- Added database constraint to enforce super user rules
- Created migration file: `V2__Add_super_user_role.sql`

### 3. Super User Service (`SuperUserService`)
- `createCompanyWithAdmin()` - Create a company with its admin user
- `getAllCompanies()` - View all companies in the system
- `getAllUsers()` - View all users across all companies
- `getUsersByCompany()` - View users for a specific company
- `createSuperUser()` - Create additional super users
- `deactivateCompanyAndUsers()` - Deactivate a company and all its users

### 4. Super User Controller (`SuperUserController`)
- **POST** `/super-admin/companies-with-admin` - Create company with admin
- **GET** `/super-admin/companies` - Get all companies
- **GET** `/super-admin/users` - Get all users
- **GET** `/super-admin/companies/{companyId}/users` - Get users by company
- **POST** `/super-admin/super-users` - Create additional super users
- **GET** `/super-admin/super-users` - Get all super users
- **GET** `/super-admin/super-users/exists` - Check if super user exists (public)
- **POST** `/super-admin/initial-super-user` - Create initial super user (public, one-time)
- **POST** `/super-admin/companies/{companyId}/deactivate` - Deactivate company

### 5. Security Configuration
- Super user endpoints require `SUPER_USER` role
- Super users have access to all existing endpoints
- Initial super user creation is public but controlled by business logic

### 6. Authentication Updates
- Updated authentication service to handle super users without companies
- JWT tokens properly handle null company IDs for super users
- Login responses include null company information for super users

## Usage Instructions

### Initial Setup

1. **Create the first super user** (one-time setup):
```bash
POST /super-admin/initial-super-user
{
  "email": "superuser@pm.com",
  "password": "securepassword",
  "firstName": "Super",
  "lastName": "User",
  "role": "SUPER_USER"
}
```

2. **Check if super user exists**:
```bash
GET /super-admin/super-users/exists
```

### Super User Operations

1. **Create a company with admin user**:
```bash
POST /super-admin/companies-with-admin
{
  "companyName": "ABC Construction",
  "companyAddress": "123 Main St",
  "companyPhone": "+1234567890",
  "companyEmail": "info@abc.com",
  "adminEmail": "admin@abc.com",
  "adminPassword": "adminpassword",
  "adminFirstName": "John",
  "adminLastName": "Doe",
  "adminRole": "ADMIN"
}
```

2. **View all companies**:
```bash
GET /super-admin/companies
```

3. **View all users across companies**:
```bash
GET /super-admin/users
```

4. **View users for a specific company**:
```bash
GET /super-admin/companies/{companyId}/users
```

### Authentication

Super users authenticate the same way as other users:
```bash
POST /auth/login
{
  "email": "superuser@pm.com",
  "password": "securepassword"
}
```

The response will include `companyId: null` and `companyName: null` for super users.

## Security Model

1. **Super User Access**: Can access all endpoints and data across all companies
2. **Admin User Access**: Can manage their specific company's data
3. **Other Roles**: Limited to their company and role-specific permissions

## Database Schema

The super user implementation includes:
- `user_role` enum with 'SUPER_USER' value
- `users.company_id` nullable for super users
- Database constraint ensuring only super users can have null company_id
- Index on super user role for performance

## Error Handling

- Attempting to create initial super user when one exists returns 403 Forbidden
- Super user creation validates that only SUPER_USER role is allowed
- Company creation validates unique names and emails
- All operations include proper error messages and codes

## Best Practices

1. **Limit Super Users**: Create only the minimum number of super users needed
2. **Secure Credentials**: Use strong passwords for super user accounts
3. **Audit Trail**: Monitor super user activities (logging is implemented)
4. **Company Management**: Let company admins handle day-to-day operations
5. **Initial Setup**: Use the initial super user endpoint only once during system setup

The super user system provides complete administrative control while maintaining proper separation of concerns between system-wide administration and company-specific management.