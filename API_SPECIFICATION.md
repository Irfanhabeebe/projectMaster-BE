# ProjectMaster API Specification

## 1. API Overview

The ProjectMaster API provides RESTful endpoints for managing residential construction projects, workflows, users, and related resources. All endpoints follow REST conventions and return JSON responses.

### 1.1 Base Configuration

- **Base URL**: `/api/v1`
- **Content Type**: `application/json`
- **Authentication**: JWT Bearer Token
- **API Version**: v1

### 1.2 Response Format

All API responses follow a consistent format:

```json
{
  "success": true,
  "data": {},
  "message": "Operation completed successfully",
  "timestamp": "2025-01-07T07:34:00Z",
  "errors": []
}
```

### 1.3 Error Response Format

```json
{
  "success": false,
  "data": null,
  "message": "Operation failed",
  "timestamp": "2025-01-07T07:34:00Z",
  "errors": [
    {
      "code": "VALIDATION_ERROR",
      "field": "email",
      "message": "Email is required"
    }
  ]
}
```

## 2. Authentication Endpoints

### 2.1 User Authentication

#### POST /auth/login
Authenticate user and return JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 3600,
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "PROJECT_MANAGER",
      "companyId": "company_uuid"
    }
  }
}
```

#### POST /auth/refresh
Refresh JWT token using refresh token.

**Request Body:**
```json
{
  "refreshToken": "refresh_token_here"
}
```

#### POST /auth/logout
Logout user and invalidate tokens.

**Headers:** `Authorization: Bearer <token>`

## 3. User Management Endpoints

### 3.1 User Operations

#### GET /users
List users (Admin/PM only).

**Query Parameters:**
- `page` (int): Page number (default: 0)
- `size` (int): Page size (default: 20)
- `role` (string): Filter by role
- `active` (boolean): Filter by active status

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "email": "user@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "role": "TRADIE",
        "active": true,
        "createdAt": "2025-01-07T07:34:00Z"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "size": 20,
    "number": 0
  }
}
```

#### POST /users
Create new user (Admin only).

**Request Body:**
```json
{
  "email": "newuser@example.com",
  "firstName": "Jane",
  "lastName": "Smith",
  "phone": "+1-555-0123",
  "role": "TRADIE",
  "password": "temporaryPassword123"
}
```

#### GET /users/profile
Get current user profile.

#### PUT /users/profile
Update current user profile.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1-555-0123"
}
```

## 4. Project Management Endpoints

### 4.1 Project Operations

#### GET /projects
List projects accessible to current user.

**Query Parameters:**
- `page`, `size`: Pagination
- `status`: Filter by project status
- `customerId`: Filter by customer
- `startDate`, `endDate`: Date range filter

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "uuid",
        "projectNumber": "PRJ-2025-001",
        "name": "Smith Family Home",
        "status": "IN_PROGRESS",
        "address": "123 Main St, City",
        "budget": 250000.00,
        "startDate": "2025-01-15",
        "expectedEndDate": "2025-06-15",
        "progressPercentage": 35,
        "customer": {
          "id": "uuid",
          "firstName": "John",
          "lastName": "Smith",
          "email": "john.smith@email.com"
        },
        "createdAt": "2025-01-07T07:34:00Z"
      }
    ],
    "totalElements": 25,
    "totalPages": 2,
    "size": 20,
    "number": 0
  }
}
```

#### POST /projects
Create new project.

**Request Body:**
```json
{
  "name": "Johnson Family Home",
  "description": "3-bedroom residential construction",
  "address": "456 Oak Ave, City",
  "customerId": "customer_uuid",
  "workflowTemplateId": "template_uuid",
  "budget": 300000.00,
  "startDate": "2025-02-01",
  "expectedEndDate": "2025-08-01"
}
```

#### GET /projects/{id}
Get project details.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "projectNumber": "PRJ-2025-001",
    "name": "Smith Family Home",
    "description": "2-story residential construction",
    "status": "IN_PROGRESS",
    "address": "123 Main St, City",
    "budget": 250000.00,
    "startDate": "2025-01-15",
    "expectedEndDate": "2025-06-15",
    "actualEndDate": null,
    "progressPercentage": 35,
    "customer": {
      "id": "uuid",
      "firstName": "John",
      "lastName": "Smith",
      "email": "john.smith@email.com",
      "phone": "+1-555-0123"
    },
    "workflowTemplate": {
      "id": "uuid",
      "name": "Standard Residential Build"
    },
    "assignedUsers": [
      {
        "userId": "uuid",
        "firstName": "Mike",
        "lastName": "Johnson",
        "role": "MANAGER"
      }
    ],
    "stages": [
      {
        "id": "uuid",
        "name": "Foundation",
        "status": "COMPLETED",
        "startDate": "2025-01-15",
        "endDate": "2025-01-30",
        "actualStartDate": "2025-01-15",
        "actualEndDate": "2025-01-28"
      }
    ],
    "createdAt": "2025-01-07T07:34:00Z",
    "updatedAt": "2025-01-07T07:34:00Z"
  }
}
```

#### PUT /projects/{id}
Update project.

#### DELETE /projects/{id}
Delete project (Admin only).

### 4.2 Project Assignments

#### GET /projects/{id}/assignments
Get project team assignments.

#### POST /projects/{id}/assignments
Assign user to project.

**Request Body:**
```json
{
  "userId": "user_uuid",
  "role": "SUPERVISOR"
}
```

#### DELETE /projects/{id}/assignments/{userId}
Remove user from project.

## 5. Workflow Management Endpoints

### 5.1 Workflow Execution

#### GET /projects/{id}/stages
Get project stages with current status.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "Site Preparation",
      "status": "COMPLETED",
      "orderIndex": 1,
      "startDate": "2025-01-15",
      "endDate": "2025-01-20",
      "actualStartDate": "2025-01-15",
      "actualEndDate": "2025-01-19",
      "steps": [
        {
          "id": "uuid",
          "name": "Site Survey",
          "status": "COMPLETED",
          "orderIndex": 1,
          "estimatedHours": 8,
          "actualHours": 6
        }
      ]
    }
  ]
}
```

#### POST /projects/{id}/stages/{stageId}/start
Start a project stage.

**Request Body:**
```json
{
  "notes": "Starting foundation work",
  "metadata": {
    "weather": "clear",
    "temperature": "22C"
  }
}
```

#### POST /projects/{id}/stages/{stageId}/complete
Complete a project stage.

#### POST /projects/{id}/stages/{stageId}/block
Block a project stage.

**Request Body:**
```json
{
  "reason": "Waiting for permit approval",
  "expectedResolutionDate": "2025-01-25"
}
```

### 5.2 Step Management

#### GET /projects/{id}/steps
Get all project steps.

#### PUT /projects/{id}/steps/{stepId}
Update step status and details.

**Request Body:**
```json
{
  "status": "IN_PROGRESS",
  "notes": "Work in progress, 50% complete",
  "actualStartDate": "2025-01-20"
}
```

#### POST /projects/{id}/steps/{stepId}/complete
Complete a project step.

**Request Body:**
```json
{
  "notes": "Step completed successfully",
  "qualityCheckPassed": true,
  "completionPhotos": ["photo1.jpg", "photo2.jpg"]
}
```

### 5.3 Workflow Templates

#### GET /workflow-templates
Get available workflow templates.

#### POST /workflow-templates
Create new workflow template (Admin only).

#### GET /workflow-templates/{id}
Get workflow template details.

## 6. Task Management Endpoints

### 6.1 Task Operations

#### GET /tasks
Get tasks assigned to current user.

**Query Parameters:**
- `status`: Filter by task status
- `priority`: Filter by priority
- `projectId`: Filter by project
- `dueDate`: Filter by due date

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "title": "Install electrical wiring",
      "description": "Install wiring for main floor",
      "status": "IN_PROGRESS",
      "priority": "HIGH",
      "dueDate": "2025-01-25",
      "estimatedHours": 16,
      "actualHours": 8,
      "completionPercentage": 50,
      "project": {
        "id": "uuid",
        "name": "Smith Family Home",
        "projectNumber": "PRJ-2025-001"
      },
      "assignedTo": [
        {
          "userId": "uuid",
          "firstName": "John",
          "lastName": "Electrician",
          "role": "ASSIGNEE"
        }
      ],
      "createdAt": "2025-01-20T09:00:00Z"
    }
  ]
}
```

#### POST /tasks
Create new task.

**Request Body:**
```json
{
  "projectStepId": "step_uuid",
  "title": "Install plumbing fixtures",
  "description": "Install all bathroom and kitchen fixtures",
  "priority": "MEDIUM",
  "dueDate": "2025-01-30",
  "estimatedHours": 12,
  "assignedUsers": [
    {
      "userId": "user_uuid",
      "role": "ASSIGNEE"
    }
  ]
}
```

#### PUT /tasks/{id}
Update task.

#### POST /tasks/{id}/time
Log time entry for task.

**Request Body:**
```json
{
  "startTime": "2025-01-20T08:00:00Z",
  "endTime": "2025-01-20T12:00:00Z",
  "description": "Installed main electrical panel"
}
```

#### GET /tasks/{id}/time
Get time entries for task.

## 7. Customer Management Endpoints

### 7.1 Customer Operations

#### GET /customers
List customers.

#### POST /customers
Create new customer.

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "email": "jane.doe@email.com",
  "phone": "+1-555-0123",
  "address": "789 Pine St, City",
  "secondaryContactName": "John Doe",
  "secondaryContactPhone": "+1-555-0124"
}
```

#### GET /customers/{id}
Get customer details.

#### PUT /customers/{id}
Update customer.

#### GET /customers/{id}/projects
Get customer's projects.

## 8. Document Management Endpoints

### 8.1 Document Operations

#### GET /projects/{id}/documents
Get project documents.

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "filename": "foundation_inspection.pdf",
      "originalFilename": "Foundation Inspection Report.pdf",
      "documentType": "PDF",
      "fileSize": 2048576,
      "description": "Foundation inspection report",
      "tags": ["inspection", "foundation"],
      "uploadedBy": {
        "id": "uuid",
        "firstName": "John",
        "lastName": "Inspector"
      },
      "createdAt": "2025-01-20T10:00:00Z"
    }
  ]
}
```

#### POST /projects/{id}/documents
Upload project document.

**Request:** Multipart form data
- `file`: Document file
- `description`: Document description
- `tags`: JSON array of tags
- `isPublic`: Boolean (customer visibility)

#### GET /documents/{id}/download
Download document.

#### DELETE /documents/{id}
Delete document.

#### POST /tasks/{id}/photos
Upload task photos.

## 9. Invoicing Endpoints

### 9.1 Invoice Operations

#### GET /projects/{id}/invoices
Get project invoices.

#### POST /projects/{id}/invoices
Create new invoice.

**Request Body:**
```json
{
  "issueDate": "2025-01-20",
  "dueDate": "2025-02-20",
  "lineItems": [
    {
      "description": "Foundation work - completed",
      "quantity": 1,
      "unitPrice": 15000.00
    },
    {
      "description": "Materials - concrete and rebar",
      "quantity": 1,
      "unitPrice": 5000.00
    }
  ],
  "notes": "Payment due within 30 days"
}
```

#### GET /invoices/{id}
Get invoice details.

#### PUT /invoices/{id}
Update invoice.

#### POST /invoices/{id}/send
Send invoice to customer.

#### POST /invoices/{id}/payments
Record payment.

**Request Body:**
```json
{
  "amount": 20000.00,
  "paymentDate": "2025-01-25",
  "paymentMethod": "BANK_TRANSFER",
  "referenceNumber": "TXN123456",
  "notes": "Full payment received"
}
```

## 10. Reporting Endpoints

### 10.1 Dashboard and Reports

#### GET /dashboard
Get dashboard data for current user.

**Response:**
```json
{
  "success": true,
  "data": {
    "activeProjects": 5,
    "completedProjects": 12,
    "overdueTasks": 3,
    "upcomingDeadlines": [
      {
        "projectName": "Smith Family Home",
        "taskTitle": "Electrical inspection",
        "dueDate": "2025-01-25"
      }
    ],
    "recentActivity": [
      {
        "type": "STAGE_COMPLETED",
        "message": "Foundation stage completed for Smith Family Home",
        "timestamp": "2025-01-20T15:30:00Z"
      }
    ]
  }
}
```

#### GET /reports/project-summary
Get project summary report.

#### GET /reports/user-workload
Get user workload report.

#### GET /reports/financial-summary
Get financial summary report.

## 11. Notification Endpoints

### 11.1 Notification Management

#### GET /notifications
Get user notifications.

#### PUT /notifications/{id}/read
Mark notification as read.

#### POST /notifications/preferences
Update notification preferences.

**Request Body:**
```json
{
  "emailNotifications": true,
  "smsNotifications": false,
  "pushNotifications": true,
  "notificationTypes": [
    "TASK_ASSIGNED",
    "STAGE_COMPLETED",
    "APPROVAL_REQUIRED"
  ]
}
```

## 12. Error Codes

### 12.1 HTTP Status Codes

- `200 OK`: Successful operation
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict
- `422 Unprocessable Entity`: Validation errors
- `500 Internal Server Error`: Server error

### 12.2 Application Error Codes

- `VALIDATION_ERROR`: Input validation failed
- `AUTHENTICATION_FAILED`: Invalid credentials
- `AUTHORIZATION_DENIED`: Insufficient permissions
- `RESOURCE_NOT_FOUND`: Requested resource not found
- `WORKFLOW_VALIDATION_ERROR`: Workflow rule violation
- `BUSINESS_RULE_VIOLATION`: Business logic constraint violated
- `EXTERNAL_SERVICE_ERROR`: External service unavailable

## 13. Rate Limiting

API endpoints are rate-limited to prevent abuse:

- **Authentication endpoints**: 5 requests per minute per IP
- **General API endpoints**: 100 requests per minute per user
- **File upload endpoints**: 10 requests per minute per user

Rate limit headers are included in responses:
- `X-RateLimit-Limit`: Request limit per window
- `X-RateLimit-Remaining`: Remaining requests in current window
- `X-RateLimit-Reset`: Time when the rate limit resets

## 14. Webhooks

### 14.1 Webhook Events

ProjectMaster can send webhook notifications for key events:

- `project.created`
- `project.completed`
- `stage.started`
- `stage.completed`
- `task.assigned`
- `task.completed`
- `invoice.created`
- `payment.received`

### 14.2 Webhook Configuration

#### POST /webhooks
Configure webhook endpoint.

**Request Body:**
```json
{
  "url": "https://your-app.com/webhooks/projectmaster",
  "events": ["project.completed", "invoice.created"],
  "secret": "your_webhook_secret"
}
```

This API specification provides comprehensive coverage of all ProjectMaster functionality with clear request/response examples and proper error handling.