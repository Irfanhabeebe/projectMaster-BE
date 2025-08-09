# ProjectMaster API Endpoints Documentation

This document provides a comprehensive list of all available API endpoints with their expected input and output payloads, parameters, and authentication requirements.

## Base URL
```
http://localhost:8080
```

## Authentication
Most endpoints require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Common Response Format
All API responses follow this standard format:
```json
{
  "success": boolean,
  "message": "string",
  "data": object | array | null,
  "timestamp": "ISO-8601 timestamp"
}
```

---

## 1. Authentication Endpoints

### 1.1 Login
- **Endpoint:** `POST /auth/login`
- **Authentication:** None required
- **Description:** Authenticate user and get access token

**Request Body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "string",
    "refreshToken": "string",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userId": "uuid",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "fullName": "string",
    "role": "ADMIN|PROJECT_MANAGER|USER|SUPER_USER",
    "companyId": "uuid",
    "companyName": "string",
    "lastLoginAt": "ISO-8601 timestamp"
  }
}
```

### 1.2 Refresh Token
- **Endpoint:** `POST /auth/refresh`
- **Authentication:** None required
- **Description:** Refresh access token using refresh token

**Request Body:**
```json
{
  "refreshToken": "string"
}
```

**Response:** Same as login response

### 1.3 Logout
- **Endpoint:** `POST /auth/logout`
- **Authentication:** Required
- **Description:** Logout user and invalidate token

**Request:** No body required (token from Authorization header)

**Response:**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

### 1.4 Get Current User
- **Endpoint:** `GET /auth/me`
- **Authentication:** Required
- **Description:** Validate authentication and get user status

**Response:**
```json
{
  "success": true,
  "message": "User is authenticated",
  "data": "Authenticated"
}
```

---

## 2. Project Management Endpoints

### 2.1 Create Project
- **Endpoint:** `POST /api/projects`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Create a new project

**Request Body:**
```json
{
  "customerId": "uuid",
  "workflowTemplateId": "uuid",
  "projectNumber": "string (max 50)",
  "name": "string (max 255)",
  "description": "string (max 1000, optional)",
  "address": "string",
  "budget": "decimal (positive)",
  "startDate": "YYYY-MM-DD (optional)",
  "expectedEndDate": "YYYY-MM-DD (optional)",
  "status": "PLANNING|IN_PROGRESS|ON_HOLD|COMPLETED|CANCELLED (default: PLANNING)",
  "progressPercentage": "integer 0-100 (default: 0)",
  "notes": "string (optional)"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Project created successfully",
  "data": {
    "id": "uuid",
    "companyId": "uuid",
    "companyName": "string",
    "customerId": "uuid",
    "customerName": "string",
    "workflowTemplateId": "uuid",
    "workflowTemplateName": "string",
    "projectNumber": "string",
    "name": "string",
    "description": "string",
    "address": "string",
    "budget": "decimal",
    "startDate": "YYYY-MM-DD",
    "expectedEndDate": "YYYY-MM-DD",
    "actualEndDate": "YYYY-MM-DD",
    "status": "enum",
    "progressPercentage": "integer",
    "notes": "string",
    "createdAt": "ISO-8601 timestamp",
    "updatedAt": "ISO-8601 timestamp"
  }
}
```

### 2.2 Get Project by ID
- **Endpoint:** `GET /api/projects/{projectId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get project details by ID

**Path Parameters:**
- `projectId`: UUID

**Response:** Same as create project response

### 2.3 Get All Projects
- **Endpoint:** `GET /api/projects`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get paginated list of projects for user's company

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)
- `sort`: string (optional, e.g., "name,asc")

**Response:**
```json
{
  "success": true,
  "message": "Projects retrieved successfully",
  "data": {
    "content": [/* array of project objects */],
    "pageable": {
      "sort": {},
      "pageNumber": 0,
      "pageSize": 20
    },
    "totalElements": "integer",
    "totalPages": "integer",
    "first": "boolean",
    "last": "boolean"
  }
}
```

### 2.4 Search Projects
- **Endpoint:** `GET /api/projects/search`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Search projects by term

**Query Parameters:**
- `searchTerm`: string (required)
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Same as get all projects

### 2.5 Get Projects by Status
- **Endpoint:** `GET /api/projects/status/{status}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get projects filtered by status

**Path Parameters:**
- `status`: PLANNING|IN_PROGRESS|ON_HOLD|COMPLETED|CANCELLED

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Same as get all projects

### 2.6 Update Project
- **Endpoint:** `PUT /api/projects/{projectId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Update project details

**Path Parameters:**
- `projectId`: UUID

**Request Body:** Same as create project (all fields optional for update)

**Response:** Same as create project response

### 2.7 Delete Project
- **Endpoint:** `DELETE /api/projects/{projectId}`
- **Authentication:** Required (ADMIN)
- **Description:** Delete a project

**Path Parameters:**
- `projectId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Project deleted successfully",
  "data": null
}
```

### 2.8 Get Overdue Projects
- **Endpoint:** `GET /api/projects/overdue`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get list of overdue projects

**Response:**
```json
{
  "success": true,
  "message": "Overdue projects retrieved successfully",
  "data": [/* array of project objects */]
}
```

### 2.9 Get Project Statistics
- **Endpoint:** `GET /api/projects/statistics`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get project statistics for user's company

**Response:**
```json
{
  "success": true,
  "message": "Project statistics retrieved successfully",
  "data": {
    "totalProjects": "integer",
    "activeProjects": "integer",
    "completedProjects": "integer",
    "overdueProjects": "integer",
    "totalBudget": "decimal",
    "averageCompletionTime": "integer (days)"
  }
}
```

---

## 3. Task Management Endpoints

### 3.1 Create Task
- **Endpoint:** `POST /api/tasks`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Create a new task

**Request Body:**
```json
{
  "projectStepId": "uuid",
  "title": "string (max 255)",
  "description": "string (max 1000, optional)",
  "priority": "LOW|MEDIUM|HIGH|URGENT (default: MEDIUM)",
  "status": "OPEN|IN_PROGRESS|BLOCKED|COMPLETED|CANCELLED (default: OPEN)",
  "dueDate": "YYYY-MM-DD (optional)",
  "startDate": "YYYY-MM-DD (optional)",
  "estimatedHours": "integer 1-1000 (optional)",
  "actualHours": "integer 0-1000 (default: 0)",
  "completionPercentage": "integer 0-100 (default: 0)",
  "assignedToId": "uuid (optional)",
  "tags": "string (max 500, optional)",
  "isMilestone": "boolean (default: false)",
  "storyPoints": "integer 1-100 (optional)",
  "blockedReason": "string (max 1000, optional)",
  "dependsOnTaskIds": ["uuid array (optional)"],
  "tagList": ["string array (optional)"]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Task created successfully",
  "data": {
    "id": "uuid",
    "projectStepId": "uuid",
    "projectStepName": "string",
    "projectName": "string",
    "title": "string",
    "description": "string",
    "priority": "enum",
    "status": "enum",
    "dueDate": "YYYY-MM-DD",
    "startDate": "YYYY-MM-DD",
    "estimatedHours": "integer",
    "actualHours": "integer",
    "completionPercentage": "integer",
    "createdById": "uuid",
    "createdByName": "string",
    "assignedToId": "uuid",
    "assignedToName": "string",
    "tags": "string",
    "isMilestone": "boolean",
    "storyPoints": "integer",
    "blockedReason": "string",
    "lastActivityAt": "ISO-8601 timestamp",
    "createdAt": "ISO-8601 timestamp",
    "updatedAt": "ISO-8601 timestamp",
    "isOverdue": "boolean",
    "isBlocked": "boolean",
    "isAssigned": "boolean",
    "totalLoggedMinutes": "integer",
    "totalLoggedHours": "double",
    "dependencyCount": "integer",
    "commentCount": "integer",
    "attachmentCount": "integer",
    "tagList": ["string array"],
    "lastActivityDescription": "string",
    "lastActivityByName": "string",
    "hasActiveTimeEntry": "boolean",
    "activeTimeEntryMinutes": "integer"
  }
}
```

### 3.2 Get Task by ID
- **Endpoint:** `GET /api/tasks/{taskId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get task details by ID

**Path Parameters:**
- `taskId`: UUID

**Response:** Same as create task response

### 3.3 Get Tasks by Project Step
- **Endpoint:** `GET /api/tasks/project-step/{projectStepId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get paginated tasks for a project step

**Path Parameters:**
- `projectStepId`: UUID

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Paginated task list

### 3.4 Get Tasks by Project
- **Endpoint:** `GET /api/tasks/project/{projectId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get paginated tasks for a project

**Path Parameters:**
- `projectId`: UUID

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Paginated task list

### 3.5 Get Tasks by Status
- **Endpoint:** `GET /api/tasks/status/{status}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get tasks filtered by status

**Path Parameters:**
- `status`: OPEN|IN_PROGRESS|BLOCKED|COMPLETED|CANCELLED

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Paginated task list

### 3.6 Get Tasks by Priority
- **Endpoint:** `GET /api/tasks/priority/{priority}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get tasks filtered by priority

**Path Parameters:**
- `priority`: LOW|MEDIUM|HIGH|URGENT

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Paginated task list

### 3.7 Get My Tasks
- **Endpoint:** `GET /api/tasks/my-tasks`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get tasks assigned to current user

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Paginated task list

### 3.8 Search Tasks
- **Endpoint:** `GET /api/tasks/project-step/{projectStepId}/search`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Search tasks within a project step

**Path Parameters:**
- `projectStepId`: UUID

**Query Parameters:**
- `searchTerm`: string (required)
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Paginated task list

### 3.9 Update Task
- **Endpoint:** `PUT /api/tasks/{taskId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Update task details

**Path Parameters:**
- `taskId`: UUID

**Request Body:** Same as create task (all fields optional)

**Response:** Same as create task response

### 3.10 Delete Task
- **Endpoint:** `DELETE /api/tasks/{taskId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Delete a task

**Path Parameters:**
- `taskId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Task deleted successfully",
  "data": null
}
```

### 3.11 Get Overdue Tasks
- **Endpoint:** `GET /api/tasks/overdue`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get list of overdue tasks

**Response:**
```json
{
  "success": true,
  "message": "Overdue tasks retrieved successfully",
  "data": [/* array of task objects */]
}
```

### 3.12 Get High Priority Tasks
- **Endpoint:** `GET /api/tasks/high-priority`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get high priority incomplete tasks

**Response:**
```json
{
  "success": true,
  "message": "High priority tasks retrieved successfully",
  "data": [/* array of task objects */]
}
```

### 3.13 Get My Active Tasks
- **Endpoint:** `GET /api/tasks/my-active-tasks`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get active tasks for current user

**Response:**
```json
{
  "success": true,
  "message": "Active tasks retrieved successfully",
  "data": [/* array of task objects */]
}
```

### 3.14 Get Tasks Requiring Attention
- **Endpoint:** `GET /api/tasks/requiring-attention`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get tasks that require attention

**Response:**
```json
{
  "success": true,
  "message": "Tasks requiring attention retrieved successfully",
  "data": [/* array of task objects */]
}
```

### 3.15 Get Task Statistics
- **Endpoint:** `GET /api/tasks/project-step/{projectStepId}/statistics`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get task statistics for a project step

**Path Parameters:**
- `projectStepId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Task statistics retrieved successfully",
  "data": {
    "totalTasks": "integer",
    "completedTasks": "integer",
    "inProgressTasks": "integer",
    "blockedTasks": "integer",
    "overdueTasks": "integer",
    "averageCompletionTime": "double (hours)"
  }
}
```

---

## 4. Task Management Operations

### 4.1 Assign Task
- **Endpoint:** `POST /api/task-management/assign`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Assign a task to a user

**Request Body:**
```json
{
  "taskId": "uuid",
  "assigneeId": "uuid"
}
```

**Response:** Task object

### 4.2 Unassign Task
- **Endpoint:** `POST /api/task-management/{taskId}/unassign`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Unassign a task

**Path Parameters:**
- `taskId`: UUID

**Response:** Task object

### 4.3 Start Time Tracking
- **Endpoint:** `POST /api/task-management/time-tracking/start`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Start time tracking for a task

**Request Body:**
```json
{
  "taskId": "uuid",
  "description": "string (optional)"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Time tracking started successfully",
  "data": {
    "id": "uuid",
    "taskId": "uuid",
    "userId": "uuid",
    "startTime": "ISO-8601 timestamp",
    "endTime": null,
    "duration": null,
    "description": "string"
  }
}
```

### 4.4 Stop Time Tracking
- **Endpoint:** `POST /api/task-management/time-tracking/{taskId}/stop`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Stop time tracking for a task

**Path Parameters:**
- `taskId`: UUID

**Response:** Time entry object with endTime and duration filled

### 4.5 Add Comment
- **Endpoint:** `POST /api/task-management/comments`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Add a comment to a task

**Request Body:**
```json
{
  "taskId": "uuid",
  "comment": "string"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Comment added successfully",
  "data": {
    "id": "uuid",
    "taskId": "uuid",
    "userId": "uuid",
    "userName": "string",
    "comment": "string",
    "createdAt": "ISO-8601 timestamp"
  }
}
```

### 4.6 Block Task
- **Endpoint:** `POST /api/task-management/{taskId}/block`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Block a task with reason

**Path Parameters:**
- `taskId`: UUID

**Query Parameters:**
- `reason`: string (required)

**Response:** Task object

### 4.7 Unblock Task
- **Endpoint:** `POST /api/task-management/{taskId}/unblock`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Unblock a task

**Path Parameters:**
- `taskId`: UUID

**Response:** Task object

### 4.8 Update Task Progress
- **Endpoint:** `POST /api/task-management/{taskId}/progress`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Update task completion percentage

**Path Parameters:**
- `taskId`: UUID

**Query Parameters:**
- `completionPercentage`: integer 0-100 (required)

**Response:** Task object

---

## 5. Notification Endpoints

### 5.1 Get User Notifications
- **Endpoint:** `GET /api/notifications`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get paginated notifications for current user

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:**
```json
{
  "success": true,
  "message": "Notifications retrieved successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "userId": "uuid",
        "taskId": "uuid",
        "type": "TASK_ASSIGNED|TASK_COMPLETED|TASK_OVERDUE|etc",
        "title": "string",
        "message": "string",
        "isRead": "boolean",
        "createdAt": "ISO-8601 timestamp"
      }
    ],
    "pageable": {},
    "totalElements": "integer",
    "totalPages": "integer"
  }
}
```

### 5.2 Get Unread Notifications
- **Endpoint:** `GET /api/notifications/unread`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get unread notifications for current user

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Same as get user notifications

### 5.3 Get Unread Notification Count
- **Endpoint:** `GET /api/notifications/unread/count`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get count of unread notifications

**Response:**
```json
{
  "success": true,
  "message": "Unread notification count retrieved successfully",
  "data": 5
}
```

### 5.4 Mark Notification as Read
- **Endpoint:** `POST /api/notifications/{notificationId}/read`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Mark a notification as read

**Path Parameters:**
- `notificationId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Notification marked as read successfully",
  "data": null
}
```

### 5.5 Mark All Notifications as Read
- **Endpoint:** `POST /api/notifications/read-all`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Mark all notifications as read for current user

**Response:**
```json
{
  "success": true,
  "message": "All notifications marked as read successfully",
  "data": null
}
```

### 5.6 Delete Notification
- **Endpoint:** `DELETE /api/notifications/{notificationId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Delete a notification

**Path Parameters:**
- `notificationId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Notification deleted successfully",
  "data": null
}
```

---

## 6. Task Reporting Endpoints

### 6.1 Get Project Task Statistics
- **Endpoint:** `GET /api/task-reporting/project/{projectId}/statistics`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get comprehensive task statistics for a project

**Path Parameters:**
- `projectId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Project task statistics retrieved successfully",
  "data": {
    "totalTasks": "integer",
    "completedTasks": "integer",
    "inProgressTasks": "integer",
    "blockedTasks": "integer",
    "overdueTasks": "integer",
    "averageCompletionTime": "double",
    "tasksByPriority": {
      "LOW": "integer",
      "MEDIUM": "integer",
      "HIGH": "integer",
      "URGENT": "integer"
    },
    "tasksByStatus": {
      "OPEN": "integer",
      "IN_PROGRESS": "integer",
      "BLOCKED": "integer",
      "COMPLETED": "integer",
      "CANCELLED": "integer"
    }
  }
}
```

### 6.2 Get User Productivity Statistics
- **Endpoint:** `GET /api/task-reporting/user/{userId}/productivity`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, or own user)
- **Description:** Get productivity statistics for a user

**Path Parameters:**
- `userId`: UUID

**Query Parameters:**
- `startDate`: YYYY-MM-DD (required)
- `endDate`: YYYY-MM-DD (required)

**Response:**
```json
{
  "success": true,
  "message": "User productivity statistics retrieved successfully",
  "data": {
    "totalTasksCompleted": "integer",
    "totalHoursLogged": "double",
    "averageTaskCompletionTime": "double",
    "productivityScore": "double",
    "tasksCompletedOnTime": "integer",
    "tasksCompletedLate": "integer"
  }
}
```

### 6.3 Get Time Tracking Report
- **Endpoint:** `GET /api/task-reporting/project/{projectId}/time-tracking`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get time tracking report for a project

**Path Parameters:**
- `projectId`: UUID

**Query Parameters:**
- `startDate`: YYYY-MM-DD (required)
- `endDate`: YYYY-MM-DD (required)

**Response:**
```json
{
  "success": true,
  "message": "Time tracking report retrieved successfully",
  "data": {
    "totalHoursLogged": "double",
    "totalEstimatedHours": "double",
    "timeVariance": "double",
    "userTimeBreakdown": [
      {
        "userId": "uuid",
        "userName": "string",
        "hoursLogged": "double"
      }
    ],
    "taskTimeBreakdown": [
      {
        "taskId": "uuid",
        "taskTitle": "string",
        "hoursLogged": "double",
        "estimatedHours": "integer"
      }
    ]
  }
}
```

### 6.4 Get Task Completion Trends
- **Endpoint:** `GET /api/task-reporting/project/{projectId}/completion-trends`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get task completion trends for a project

**Path Parameters:**
- `projectId`: UUID

**Query Parameters:**
- `days`: integer (default: 30)

**Response:**
```json
{
  "success": true,
  "message": "Task completion trends retrieved successfully",
  "data": {
    "dailyCompletions": [
      {
        "date": "YYYY-MM-DD",
        "completedTasks": "integer"
      }
    ],
    "averageDailyCompletions": "double",
    "trendDirection": "UP|DOWN|STABLE"
  }
}
```

### 6.5 Get Team Performance Metrics
- **Endpoint:** `GET /api/task-reporting/project/{projectId}/team-performance`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get team performance metrics for a project

**Path Parameters:**
- `projectId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Team performance metrics retrieved successfully",
  "data": {
    "teamMembers": [
      {
        "userId": "uuid",
        "userName": "string",
        "tasksAssigned": "integer",
        "tasksCompleted": "integer",
        "completionRate": "double",
        "averageTaskTime": "double"
      }
    ],
    "overallTeamPerformance": "double",
    "topPerformer": {
      "userId": "uuid",
      "userName": "string",
      "score": "double"
    }
  }
}
```

---

## 7. Document Management Endpoints

### 7.1 Upload Document
- **Endpoint:** `POST /api/documents/upload`
- **Authentication:** Required
- **Description:** Upload a document to a project or task
- **Content-Type:** `multipart/form-data`

**Request Body (Form Data):**
```
file: MultipartFile (required)
projectId: UUID (optional, either projectId or taskId required)
taskId: UUID (optional, either projectId or taskId required)
documentCategory: SPECIFICATION|DRAWING|PHOTO|REPORT|CONTRACT|OTHER (required)
description: string (max 1000, optional)
tags: string array (optional)
isPublic: boolean (default: false)
metadata: JSON string (optional)
```

**Response:**
```json
{
  "success": true,
  "message": "Document uploaded successfully",
  "data": {
    "id": "uuid",
    "filename": "string",
    "originalFilename": "string",
    "mimeType": "string",
    "fileSize": "long",
    "documentCategory": "enum",
    "description": "string",
    "tags": ["string array"],
    "isPublic": "boolean",
    "projectId": "uuid",
    "taskId": "uuid",
    "uploadedById": "uuid",
    "uploadedByName": "string",
    "createdAt": "ISO-8601 timestamp"
  }
}
```

### 7.2 Download Document
- **Endpoint:** `GET /api/documents/{documentId}/download`
- **Authentication:** Required
- **Description:** Download a document

**Path Parameters:**
- `documentId`: UUID

**Response:** Binary file with appropriate headers

### 7.3 Get Document Metadata
- **Endpoint:** `GET /api/documents/{documentId}`
- **Authentication:** Required
- **Description:** Get document metadata

**Path Parameters:**
- `documentId`: UUID

**Response:** Same as upload document response

### 7.4 Update Document
- **Endpoint:** `PUT /api/documents/{documentId}`
- **Authentication:** Required
- **Description:** Update document metadata

**Path Parameters:**
- `documentId`: UUID

**Request Body:**
```json
{
  "description": "string (optional)",
  "tags": ["string array (optional)"],
  "isPublic": "boolean (optional)",
  "documentCategory": "enum (optional)"
}
```

**Response:** Same as upload document response

### 7.5 Delete Document
- **Endpoint:** `DELETE /api/documents/{documentId}`
- **Authentication:** Required
- **Description:** Delete (archive) a document

**Path Parameters:**
- `documentId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Document deleted successfully",
  "data": "Document deleted successfully"
}
```

### 7.6 Search Documents
- **Endpoint:** `POST /api/documents/search`
- **Authentication:** Required
- **Description:** Search documents with filters

**Request Body:**
```json
{
  "searchTerm": "string (optional)",
  "projectId": "uuid (optional)",
  "taskId": "uuid (optional)",
  "documentCategory": "enum
   "documentCategory": "SPECIFICATION|DRAWING|PHOTO|REPORT|CONTRACT|OTHER (optional)",
  "tags": ["string array (optional)"],
  "startDate": "YYYY-MM-DD (optional)",
  "endDate": "YYYY-MM-DD (optional)",
  "page": "integer (default: 0)",
  "size": "integer (default: 20)"
}
```

**Response:** Paginated document list

### 7.7 Get Documents by Project
- **Endpoint:** `GET /api/documents/project/{projectId}`
- **Authentication:** Required
- **Description:** Get all documents for a project

**Path Parameters:**
- `projectId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Project documents retrieved successfully",
  "data": [/* array of document objects */]
}
```

### 7.8 Get Documents by Task
- **Endpoint:** `GET /api/documents/task/{taskId}`
- **Authentication:** Required
- **Description:** Get all documents for a task

**Path Parameters:**
- `taskId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Task documents retrieved successfully",
  "data": [/* array of document objects */]
}
```

### 7.9 Stream Document
- **Endpoint:** `GET /api/documents/{documentId}/stream`
- **Authentication:** Required
- **Description:** Stream document for inline viewing (images, PDFs, videos)

**Path Parameters:**
- `documentId`: UUID

**Response:** Binary stream with inline content disposition

### 7.10 Get Document Thumbnail
- **Endpoint:** `GET /api/documents/{documentId}/thumbnail`
- **Authentication:** Required
- **Description:** Get thumbnail for image documents

**Path Parameters:**
- `documentId`: UUID

**Query Parameters:**
- `size`: integer (default: 200)

**Response:** Binary image data

### 7.11 Get Document Statistics
- **Endpoint:** `GET /api/documents/stats`
- **Authentication:** Required
- **Description:** Get document statistics

**Query Parameters:**
- `projectId`: UUID (optional)
- `taskId`: UUID (optional)

**Response:**
```json
{
  "success": true,
  "message": "Document statistics retrieved successfully",
  "data": {
    "totalDocuments": "long",
    "totalSize": "long (bytes)",
    "documentsByType": "object",
    "documentsByCategory": "object",
    "recentUploads": "long"
  }
}
```

---

## 8. Invoice Management Endpoints

### 8.1 Create Invoice
- **Endpoint:** `POST /api/invoices`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Create a new invoice

**Request Body:**
```json
{
  "projectId": "uuid",
  "invoiceNumber": "string (max 50)",
  "issueDate": "YYYY-MM-DD",
  "dueDate": "YYYY-MM-DD",
  "status": "DRAFT|SENT|PAID|OVERDUE|CANCELLED (default: DRAFT)",
  "notes": "string (max 1000, optional)",
  "lineItems": [
    {
      "description": "string",
      "quantity": "decimal",
      "unitPrice": "decimal",
      "amount": "decimal"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Invoice created successfully",
  "data": {
    "id": "uuid",
    "projectId": "uuid",
    "projectName": "string",
    "projectNumber": "string",
    "customerName": "string",
    "invoiceNumber": "string",
    "issueDate": "YYYY-MM-DD",
    "dueDate": "YYYY-MM-DD",
    "subtotal": "decimal",
    "taxAmount": "decimal",
    "totalAmount": "decimal",
    "totalPaid": "decimal",
    "outstandingAmount": "decimal",
    "status": "enum",
    "notes": "string",
    "createdById": "uuid",
    "createdByName": "string",
    "createdAt": "ISO-8601 timestamp",
    "updatedAt": "ISO-8601 timestamp",
    "lineItems": [/* array of line item objects */],
    "payments": [/* array of payment objects */],
    "isOverdue": "boolean",
    "isFullyPaid": "boolean"
  }
}
```

### 8.2 Get Invoice by ID
- **Endpoint:** `GET /api/invoices/{invoiceId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get invoice details by ID

**Path Parameters:**
- `invoiceId`: UUID

**Response:** Same as create invoice response

### 8.3 Get All Invoices
- **Endpoint:** `GET /api/invoices`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Get paginated list of invoices for user's company

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Paginated invoice list

### 8.4 Search Invoices
- **Endpoint:** `POST /api/invoices/search`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER, USER)
- **Description:** Search invoices with filters

**Request Body:**
```json
{
  "searchTerm": "string (optional)",
  "status": "enum (optional)",
  "projectId": "uuid (optional)",
  "startDate": "YYYY-MM-DD (optional)",
  "endDate": "YYYY-MM-DD (optional)",
  "minAmount": "decimal (optional)",
  "maxAmount": "decimal (optional)"
}
```

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Paginated invoice list

### 8.5 Update Invoice
- **Endpoint:** `PUT /api/invoices/{invoiceId}`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Update invoice details

**Path Parameters:**
- `invoiceId`: UUID

**Request Body:** Same as create invoice (all fields optional)

**Response:** Same as create invoice response

### 8.6 Delete Invoice
- **Endpoint:** `DELETE /api/invoices/{invoiceId}`
- **Authentication:** Required (ADMIN)
- **Description:** Delete an invoice

**Path Parameters:**
- `invoiceId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Invoice deleted successfully",
  "data": null
}
```

### 8.7 Send Invoice
- **Endpoint:** `POST /api/invoices/{invoiceId}/send`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Send invoice (change status to SENT)

**Path Parameters:**
- `invoiceId`: UUID

**Response:** Invoice object with updated status

### 8.8 Add Payment
- **Endpoint:** `POST /api/invoices/payments`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Add payment to an invoice

**Request Body:**
```json
{
  "invoiceId": "uuid",
  "amount": "decimal",
  "paymentDate": "YYYY-MM-DD",
  "paymentMethod": "CASH|CREDIT_CARD|BANK_TRANSFER|CHECK|OTHER",
  "reference": "string (optional)",
  "notes": "string (optional)"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Payment added successfully",
  "data": {
    "id": "uuid",
    "invoiceId": "uuid",
    "amount": "decimal",
    "paymentDate": "YYYY-MM-DD",
    "paymentMethod": "enum",
    "reference": "string",
    "notes": "string",
    "createdById": "uuid",
    "createdByName": "string",
    "createdAt": "ISO-8601 timestamp"
  }
}
```

### 8.9 Get Overdue Invoices
- **Endpoint:** `GET /api/invoices/overdue`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get list of overdue invoices

**Response:**
```json
{
  "success": true,
  "message": "Overdue invoices retrieved successfully",
  "data": [/* array of invoice objects */]
}
```

### 8.10 Get Invoice Statistics
- **Endpoint:** `GET /api/invoices/statistics`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get invoice statistics for user's company

**Response:**
```json
{
  "success": true,
  "message": "Invoice statistics retrieved successfully",
  "data": {
    "totalInvoices": "integer",
    "totalAmount": "decimal",
    "totalPaid": "decimal",
    "totalOutstanding": "decimal",
    "overdueInvoices": "integer",
    "overdueAmount": "decimal",
    "averagePaymentTime": "double (days)"
  }
}
```

---

## 9. User Management Endpoints

### 9.1 Create User
- **Endpoint:** `POST /users`
- **Authentication:** Required
- **Description:** Create a new user

**Request Body:**
```json
{
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string",
  "role": "ADMIN|PROJECT_MANAGER|USER",
  "companyId": "uuid",
  "isActive": "boolean (default: true)"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "uuid",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "fullName": "string",
    "role": "enum",
    "companyId": "uuid",
    "companyName": "string",
    "isActive": "boolean",
    "createdAt": "ISO-8601 timestamp",
    "updatedAt": "ISO-8601 timestamp"
  }
}
```

### 9.2 Get User by ID
- **Endpoint:** `GET /users/{id}`
- **Authentication:** Required
- **Description:** Get user details by ID

**Path Parameters:**
- `id`: UUID

**Response:** Same as create user response

### 9.3 Get User by Email
- **Endpoint:** `GET /users/email/{email}`
- **Authentication:** Required
- **Description:** Get user details by email

**Path Parameters:**
- `email`: string

**Response:** Same as create user response or 404 if not found

### 9.4 Get Users by Company
- **Endpoint:** `GET /users/company/{companyId}`
- **Authentication:** Required
- **Description:** Get paginated users for a company

**Path Parameters:**
- `companyId`: UUID

**Query Parameters:**
- `page`: integer (default: 0)
- `size`: integer (default: 20)

**Response:** Paginated user list

### 9.5 Get Active Users by Company
- **Endpoint:** `GET /users/company/{companyId}/active`
- **Authentication:** Required
- **Description:** Get active users for a company

**Path Parameters:**
- `companyId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [/* array of user objects */]
}
```

### 9.6 Update User
- **Endpoint:** `PUT /users/{id}`
- **Authentication:** Required
- **Description:** Update user details

**Path Parameters:**
- `id`: UUID

**Request Body:** Same as create user (all fields optional)

**Response:** Same as create user response

### 9.7 Deactivate User
- **Endpoint:** `POST /users/{id}/deactivate`
- **Authentication:** Required
- **Description:** Deactivate a user

**Path Parameters:**
- `id`: UUID

**Response:**
```json
{
  "success": true,
  "message": "User deactivated successfully",
  "data": null
}
```

### 9.8 Activate User
- **Endpoint:** `POST /users/{id}/activate`
- **Authentication:** Required
- **Description:** Activate a user

**Path Parameters:**
- `id`: UUID

**Response:**
```json
{
  "success": true,
  "message": "User activated successfully",
  "data": null
}
```

---

## 10. Company Management Endpoints

### 10.1 Create Company
- **Endpoint:** `POST /companies`
- **Authentication:** Required
- **Description:** Create a new company

**Request Body:**
```json
{
  "name": "string",
  "address": "string (optional)",
  "phone": "string (optional)",
  "email": "string (optional)",
  "website": "string (optional)",
  "isActive": "boolean (default: true)"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Company created successfully",
  "data": {
    "id": "uuid",
    "name": "string",
    "address": "string",
    "phone": "string",
    "email": "string",
    "website": "string",
    "isActive": "boolean",
    "createdAt": "ISO-8601 timestamp",
    "updatedAt": "ISO-8601 timestamp"
  }
}
```

### 10.2 Get Company by ID
- **Endpoint:** `GET /companies/{id}`
- **Authentication:** Required
- **Description:** Get company details by ID

**Path Parameters:**
- `id`: UUID

**Response:** Same as create company response

### 10.3 Get All Active Companies
- **Endpoint:** `GET /companies`
- **Authentication:** Required
- **Description:** Get all active companies

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [/* array of company objects */]
}
```

### 10.4 Search Companies
- **Endpoint:** `GET /companies/search`
- **Authentication:** Required
- **Description:** Search companies by name

**Query Parameters:**
- `q`: string (required)

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [/* array of company objects */]
}
```

### 10.5 Update Company
- **Endpoint:** `PUT /companies/{id}`
- **Authentication:** Required
- **Description:** Update company details

**Path Parameters:**
- `id`: UUID

**Request Body:** Same as create company (all fields optional)

**Response:** Same as create company response

### 10.6 Deactivate Company
- **Endpoint:** `POST /companies/{id}/deactivate`
- **Authentication:** Required
- **Description:** Deactivate a company

**Path Parameters:**
- `id`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Company deactivated successfully",
  "data": null
}
```

### 10.7 Activate Company
- **Endpoint:** `POST /companies/{id}/activate`
- **Authentication:** Required
- **Description:** Activate a company

**Path Parameters:**
- `id`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Company activated successfully",
  "data": null
}
```

---

## 11. Super User Management Endpoints

### 11.1 Create Company with Admin
- **Endpoint:** `POST /super-admin/companies-with-admin`
- **Authentication:** Required (SUPER_USER)
- **Description:** Create a company with an admin user

**Request Body:**
```json
{
  "company": {
    "name": "string",
    "address": "string (optional)",
    "phone": "string (optional)",
    "email": "string (optional)",
    "website": "string (optional)"
  },
  "admin": {
    "email": "string",
    "password": "string",
    "firstName": "string",
    "lastName": "string"
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "Company and admin user created successfully",
  "data": {
    "company": {/* company object */},
    "admin": {/* user object */}
  }
}
```

### 11.2 Get All Companies (Super User)
- **Endpoint:** `GET /super-admin/companies`
- **Authentication:** Required (SUPER_USER)
- **Description:** Get all companies including inactive ones

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [/* array of company objects */]
}
```

### 11.3 Get All Users (Super User)
- **Endpoint:** `GET /super-admin/users`
- **Authentication:** Required (SUPER_USER)
- **Description:** Get all users across all companies

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [/* array of user objects */]
}
```

### 11.4 Get Users by Company (Super User)
- **Endpoint:** `GET /super-admin/companies/{companyId}/users`
- **Authentication:** Required (SUPER_USER)
- **Description:** Get users for a specific company

**Path Parameters:**
- `companyId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [/* array of user objects */]
}
```

### 11.5 Create Super User
- **Endpoint:** `POST /super-admin/super-user`
- **Authentication:** Required (SUPER_USER)
- **Description:** Create additional super user

**Request Body:**
```json
{
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Super user created successfully",
  "data": {/* user object with SUPER_USER role */}
}
```

### 11.6 Get All Super Users
- **Endpoint:** `GET /super-admin/super-users`
- **Authentication:** Required (SUPER_USER)
- **Description:** Get all super users

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [/* array of super user objects */]
}
```

### 11.7 Check Super User Exists
- **Endpoint:** `GET /super-admin/super-users/exists`
- **Authentication:** None required
- **Description:** Check if any super user exists (for initial setup)

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": true
}
```

### 11.8 Deactivate Company and Users
- **Endpoint:** `POST /super-admin/companies/{companyId}/deactivate`
- **Authentication:** Required (SUPER_USER)
- **Description:** Deactivate company and all its users

**Path Parameters:**
- `companyId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Company and all its users deactivated successfully",
  "data": null
}
```

### 11.9 Create Initial Super User
- **Endpoint:** `POST /super-admin/initial-super-user`
- **Authentication:** None required
- **Description:** Create the first super user (only when none exists)

**Request Body:**
```json
{
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Initial super user created successfully",
  "data": {/* user object with SUPER_USER role */}
}
```

---

## 12. Workflow Management Endpoints

### 12.1 Start Project Stage
- **Endpoint:** `POST /api/v1/workflow/projects/{projectId}/stages/{stageId}/start`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Start a project stage

**Path Parameters:**
- `projectId`: UUID
- `stageId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Stage started successfully",
  "data": {
    "success": "boolean",
    "message": "string",
    "executedActions": ["string array"],
    "warnings": ["string array"],
    "errors": ["string array"]
  }
}
```

### 12.2 Complete Project Stage
- **Endpoint:** `POST /api/v1/workflow/projects/{projectId}/stages/{stageId}/complete`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Complete a project stage

**Path Parameters:**
- `projectId`: UUID
- `stageId`: UUID

**Response:** Same as start stage response

### 12.3 Start Project Step
- **Endpoint:** `POST /api/v1/workflow/projects/{projectId}/steps/{stepId}/start`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Start a project step

**Path Parameters:**
- `projectId`: UUID
- `stepId`: UUID

**Response:** Same as start stage response

### 12.4 Complete Project Step
- **Endpoint:** `POST /api/v1/workflow/projects/{projectId}/steps/{stepId}/complete`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Complete a project step

**Path Parameters:**
- `projectId`: UUID
- `stepId`: UUID

**Response:** Same as start stage response

### 12.5 Get Available Transitions
- **Endpoint:** `GET /api/v1/workflow/projects/{projectId}/transitions`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Get available workflow transitions for a project

**Path Parameters:**
- `projectId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Available transitions retrieved successfully",
  "data": [
    {
      "type": "START_STAGE|COMPLETE_STAGE|START_STEP|COMPLETE_STEP",
      "targetId": "uuid",
      "targetName": "string",
      "description": "string",
      "canExecute": "boolean",
      "requirements": ["string array"]
    }
  ]
}
```

### 12.6 Check if Stage Can Start
- **Endpoint:** `GET /api/v1/workflow/projects/{projectId}/stages/{stageId}/can-start`
- **Authentication:** Required (ADMIN, PROJECT_MANAGER)
- **Description:** Check if a stage can be started

**Path Parameters:**
- `projectId`: UUID
- `stageId`: UUID

**Response:**
```json
{
  "success": true,
  "message": "Transition check completed",
  "data": true
}
```

---

## Error Responses

All endpoints may return error responses in the following format:

### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "field": "error message"
  },
  "timestamp": "ISO-8601 timestamp"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Authentication required",
  "data": null,
  "timestamp": "ISO-8601 timestamp"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied",
  "data": null,
  "timestamp": "ISO-8601 timestamp"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Resource not found",
  "data": null,
  "timestamp": "ISO-8601 timestamp"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Internal server error",
  "data": null,
  "timestamp": "ISO-8601 timestamp"
}
```

---

## Enums Reference

### UserRole
- `SUPER_USER`
- `ADMIN`
- `PROJECT_MANAGER`
- `USER`

### ProjectStatus
- `PLANNING`
- `IN_PROGRESS`
- `ON_HOLD`
- `COMPLETED`
- `CANCELLED`

### TaskStatus
- `OPEN`
- `IN_PROGRESS`
- `BLOCKED`
- `COMPLETED`
- `CANCELLED`

### TaskPriority
- `LOW`
- `MEDIUM`
- `HIGH`
- `URGENT`

### InvoiceStatus
- `DRAFT`
- `SENT`
- `PAID`
- `OVERDUE`
- `CANCELLED`

### PaymentMethod
- `CASH`
- `CREDIT_CARD`
- `BANK_TRANSFER`
- `CHECK`
- `OTHER`

### DocumentCategory
- `SPECIFICATION`
- `DRAWING`
- `PHOTO`
- `REPORT`
- `CONTRACT`
- `OTHER`

### NotificationType
- `TASK_ASSIGNED`
- `TASK_COMPLETED`
- `TASK_OVERDUE`
- `PROJECT_STARTED`
- `PROJECT_COMPLETED`
- `INVOICE_SENT`
- `PAYMENT_RECEIVED`

---

## Notes

1. All timestamps are in ISO-8601 format (e.g., "2023-12-07T10:30:00Z")
2. All UUIDs are in standard UUID format (e.g., "123e4567-e89b-12d3-a456-426614174000")
3. Pagination follows Spring Boot's standard format with `page`, `size`, and `sort` parameters
4. File uploads use `multipart/form-data` content type
5. All monetary amounts are represented as decimal values
6. Date fields use YYYY-MM-DD format
7. Authentication tokens should be included in the Authorization header as "Bearer {token}"
8. All endpoints return the standard ApiResponse format unless otherwise specified