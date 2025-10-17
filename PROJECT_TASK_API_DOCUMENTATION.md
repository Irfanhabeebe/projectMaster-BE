# Project Task Management API Documentation

**Version:** 1.0  
**Base URL:** `/api/projects`  
**Authentication:** Bearer Token (JWT)

---

## Overview

This API provides complete CRUD operations for managing project tasks within stages. Tasks can be:
- **Adhoc Tasks**: Manually created by project managers (not from templates)
- **Template-based Tasks**: Generated from workflow templates

Tasks sit in the project hierarchy: **Project → Stage → Task → Step**

---

## Authentication

All endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

---

## Endpoints Summary

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/projects/{projectId}/stages/{stageId}/tasks` | Create adhoc task | ADMIN, PM |
| GET | `/projects/{projectId}/tasks/{taskId}` | Get task details | ADMIN, PM, TRADIE |
| GET | `/projects/{projectId}/stages/{stageId}/tasks` | List tasks in stage | ADMIN, PM, TRADIE |
| GET | `/projects/{projectId}/tasks/adhoc` | List adhoc tasks | ADMIN, PM, TRADIE |
| PUT | `/projects/{projectId}/tasks/{taskId}` | Update task | ADMIN, PM |
| DELETE | `/projects/{projectId}/tasks/{taskId}` | Delete task | ADMIN, PM |

**Roles:**
- ADMIN = Administrator
- PM = Project Manager
- TRADIE = Tradesperson/Worker

---

## API Endpoints

### 1. Create Adhoc Task

Create a new manually-added task in a project stage.

**Endpoint:**  
`POST /api/projects/{projectId}/stages/{stageId}/tasks`

**Permissions:** ADMIN, PROJECT_MANAGER

**Path Parameters:**
- `projectId` (UUID, required) - Project ID
- `stageId` (UUID, required) - Project stage ID

**Request Body:**
```json
{
  "name": "Custom Landscaping Work",
  "description": "Install custom landscaping features as per client specifications",
  "estimatedDays": 5,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-05",
  "notes": "Client wants native Australian plants only",
  "dependsOn": [
    {
      "entityType": "TASK",
      "entityId": "550e8400-e29b-41d4-a716-446655440000",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0,
      "notes": "Wait for excavation to complete"
    }
  ],
  "dependents": [
    {
      "entityType": "TASK",
      "entityId": "660e8400-e29b-41d4-a716-446655440000",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 2,
      "notes": "Final inspection depends on this"
    }
  ]
}
```

**Request Fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | Task name (max 255 chars) |
| description | string | No | Detailed description |
| estimatedDays | integer | No | Estimated duration in days |
| plannedStartDate | date | No | Planned start (YYYY-MM-DD) |
| plannedEndDate | date | No | Planned end (YYYY-MM-DD) |
| notes | string | No | Additional notes |
| dependsOn | array | No | List of dependencies (what this task depends on) |
| dependents | array | No | List of dependents (what depends on this task) |

**Dependency Object:**

| Field | Type | Required | Description | Values |
|-------|------|----------|-------------|--------|
| entityType | string | Yes | Type of entity | TASK, STAGE |
| entityId | UUID | Yes | Entity UUID | |
| dependencyType | string | No | Type of dependency | FINISH_TO_START (default), START_TO_START, FINISH_TO_FINISH, START_TO_FINISH |
| lagDays | integer | No | Delay in days (default: 0) | |
| notes | string | No | Dependency notes | |

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Adhoc task created successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Custom Landscaping Work",
    "description": "Install custom landscaping features as per client specifications",
    "projectStageId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "NOT_STARTED",
    "estimatedDays": 5,
    "plannedStartDate": "2025-11-01",
    "plannedEndDate": "2025-11-05",
    "actualStartDate": null,
    "actualEndDate": null,
    "notes": "Client wants native Australian plants only",
    "adhocTaskFlag": true,
    "dependsOn": [
      {
        "dependencyId": "dep-uuid-1",
        "entityType": "TASK",
        "entityId": "550e8400-e29b-41d4-a716-446655440000",
        "entityName": "Excavation Work",
        "dependencyType": "FINISH_TO_START",
        "lagDays": 0,
        "status": "PENDING"
      }
    ],
    "dependents": [
      {
        "dependencyId": "dep-uuid-2",
        "entityType": "TASK",
        "entityId": "660e8400-e29b-41d4-a716-446655440000",
        "entityName": "Final Inspection",
        "dependencyType": "FINISH_TO_START",
        "lagDays": 2,
        "status": "PENDING"
      }
    ],
    "createdAt": "2025-10-15T16:45:00.000Z",
    "updatedAt": "2025-10-15T16:45:00.000Z"
  }
}
```

**Status Values:**
- `NOT_STARTED` - Task hasn't begun
- `IN_PROGRESS` - Work actively happening
- `ON_HOLD` - Temporarily paused
- `COMPLETED` - Task finished
- `CANCELLED` - Task cancelled

**Error Responses:**

**400 Bad Request - Circular Dependency:**
```json
{
  "success": false,
  "message": "Circular dependency detected: Adding these dependencies would create a cycle in the workflow.",
  "errors": null
}
```

**400 Bad Request - Validation Error:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "name": "Task name is required",
    "estimatedDays": "Must be a positive number"
  }
}
```

**403 Forbidden:**
```json
{
  "success": false,
  "message": "Access denied: You can only manage tasks for projects belonging to your company",
  "errors": null
}
```

**404 Not Found:**
```json
{
  "success": false,
  "message": "ProjectStage not found with id: 550e8400-e29b-41d4-a716-446655440000",
  "errors": null
}
```

---

### 2. Get Task Details

Retrieve details of a specific task (adhoc or template-based).

**Endpoint:**  
`GET /api/projects/{projectId}/tasks/{taskId}`

**Permissions:** ADMIN, PROJECT_MANAGER, TRADIE

**Path Parameters:**
- `projectId` (UUID, required) - Project ID
- `taskId` (UUID, required) - Task ID

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Task retrieved successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Custom Landscaping Work",
    "description": "Install custom landscaping features",
    "projectStageId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "IN_PROGRESS",
    "estimatedDays": 5,
    "plannedStartDate": "2025-11-01",
    "plannedEndDate": "2025-11-05",
    "actualStartDate": "2025-11-01",
    "actualEndDate": null,
    "notes": "Client wants native Australian plants only",
    "adhocTaskFlag": true,
    "dependsOn": [...],
    "dependents": [...],
    "createdAt": "2025-10-15T16:45:00.000Z",
    "updatedAt": "2025-10-15T17:30:00.000Z"
  }
}
```

**Error Response (404):**
```json
{
  "success": false,
  "message": "ProjectTask not found with id: 123e4567-e89b-12d3-a456-426614174000"
}
```

---

### 3. List Tasks in Stage

Get all tasks (both adhoc and template-based) for a specific project stage.

**Endpoint:**  
`GET /api/projects/{projectId}/stages/{stageId}/tasks`

**Permissions:** ADMIN, PROJECT_MANAGER, TRADIE

**Path Parameters:**
- `projectId` (UUID, required) - Project ID
- `stageId` (UUID, required) - Project stage ID

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Tasks retrieved successfully",
  "data": [
    {
      "id": "task-1-uuid",
      "name": "Foundation Work",
      "description": "Concrete foundation as per plans",
      "projectStageId": "550e8400-e29b-41d4-a716-446655440000",
      "status": "COMPLETED",
      "estimatedDays": 3,
      "plannedStartDate": "2025-10-01",
      "plannedEndDate": "2025-10-03",
      "actualStartDate": "2025-10-01",
      "actualEndDate": "2025-10-03",
      "notes": null,
      "adhocTaskFlag": false,
      "dependsOn": [],
      "dependents": [
        {
          "dependencyId": "dep-uuid",
          "entityType": "TASK",
          "entityId": "task-2-uuid",
          "entityName": "Frame Erection",
          "dependencyType": "FINISH_TO_START",
          "lagDays": 0,
          "status": "COMPLETED"
        }
      ],
      "createdAt": "2025-09-15T10:00:00.000Z",
      "updatedAt": "2025-10-03T16:00:00.000Z"
    },
    {
      "id": "task-2-uuid",
      "name": "Frame Erection",
      "description": "Timber frame construction",
      "projectStageId": "550e8400-e29b-41d4-a716-446655440000",
      "status": "IN_PROGRESS",
      "estimatedDays": 5,
      "plannedStartDate": "2025-10-04",
      "plannedEndDate": "2025-10-08",
      "actualStartDate": "2025-10-04",
      "actualEndDate": null,
      "notes": "Using treated pine",
      "adhocTaskFlag": false,
      "dependsOn": [
        {
          "dependencyId": "dep-uuid",
          "entityType": "TASK",
          "entityId": "task-1-uuid",
          "entityName": "Foundation Work",
          "dependencyType": "FINISH_TO_START",
          "lagDays": 0,
          "status": "COMPLETED"
        }
      ],
      "dependents": [],
      "createdAt": "2025-09-15T10:00:00.000Z",
      "updatedAt": "2025-10-04T09:00:00.000Z"
    },
    {
      "id": "task-3-uuid",
      "name": "Custom Landscaping Work",
      "description": "Adhoc task for special landscaping",
      "projectStageId": "550e8400-e29b-41d4-a716-446655440000",
      "status": "NOT_STARTED",
      "estimatedDays": 5,
      "plannedStartDate": "2025-11-01",
      "plannedEndDate": "2025-11-05",
      "actualStartDate": null,
      "actualEndDate": null,
      "notes": "Client special request",
      "adhocTaskFlag": true,
      "dependsOn": [],
      "dependents": [],
      "createdAt": "2025-10-15T16:45:00.000Z",
      "updatedAt": "2025-10-15T16:45:00.000Z"
    }
  ]
}
```

**Notes:**
- Tasks are ordered by `createdAt` timestamp
- Empty array returned if no tasks found
- Mix of adhoc (`adhocTaskFlag: true`) and template-based (`adhocTaskFlag: false`) tasks

---

### 4. List Adhoc Tasks in Project

Get all adhoc tasks (manually created, not from templates) across all stages in a project.

**Endpoint:**  
`GET /api/projects/{projectId}/tasks/adhoc`

**Permissions:** ADMIN, PROJECT_MANAGER, TRADIE

**Path Parameters:**
- `projectId` (UUID, required) - Project ID

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Adhoc tasks retrieved successfully",
  "data": [
    {
      "id": "adhoc-task-1-uuid",
      "name": "Custom Landscaping Work",
      "description": "Special client request",
      "projectStageId": "stage-1-uuid",
      "status": "NOT_STARTED",
      "estimatedDays": 5,
      "plannedStartDate": "2025-11-01",
      "plannedEndDate": "2025-11-05",
      "actualStartDate": null,
      "actualEndDate": null,
      "notes": "Native plants only",
      "adhocTaskFlag": true,
      "dependsOn": [],
      "dependents": [],
      "createdAt": "2025-10-15T16:45:00.000Z",
      "updatedAt": "2025-10-15T16:45:00.000Z"
    },
    {
      "id": "adhoc-task-2-uuid",
      "name": "Additional Electrical Work",
      "description": "Extra power points requested",
      "projectStageId": "stage-2-uuid",
      "status": "COMPLETED",
      "estimatedDays": 2,
      "plannedStartDate": "2025-10-20",
      "plannedEndDate": "2025-10-21",
      "actualStartDate": "2025-10-20",
      "actualEndDate": "2025-10-21",
      "notes": "Client variation",
      "adhocTaskFlag": true,
      "dependsOn": [],
      "dependents": [],
      "createdAt": "2025-10-10T14:00:00.000Z",
      "updatedAt": "2025-10-21T17:00:00.000Z"
    }
  ]
}
```

**Use Cases:**
- Display all manually added tasks for project overview
- Track custom work outside standard workflow
- Filter adhoc tasks for reporting

---

### 5. Update Task

Update an existing task (both adhoc and template-based can be updated).

**Endpoint:**  
`PUT /api/projects/{projectId}/tasks/{taskId}`

**Permissions:** ADMIN, PROJECT_MANAGER

**Path Parameters:**
- `projectId` (UUID, required) - Project ID
- `taskId` (UUID, required) - Task ID to update

**Request Body:**
```json
{
  "name": "Custom Landscaping Work (Updated)",
  "description": "Install custom landscaping features with native plants",
  "estimatedDays": 7,
  "plannedStartDate": "2025-11-03",
  "plannedEndDate": "2025-11-09",
  "notes": "Updated: Client changed to include irrigation system",
  "dependsOn": [
    {
      "entityType": "TASK",
      "entityId": "550e8400-e29b-41d4-a716-446655440000",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 1,
      "notes": "Wait for irrigation pipes to arrive"
    }
  ],
  "dependents": []
}
```

**Request Fields:** Same as Create, all fields required (send existing values if no change)

**Important Notes:**

1. **Dependency Handling:**
   - `dependsOn: null` → No change to dependencies
   - `dependsOn: []` → Remove all dependencies
   - `dependsOn: [...]` → Replace with new list

2. **Workflow Rebuild Flag:**
   - Set to `true` only when:
     - Dates change (`plannedStartDate` or `plannedEndDate`)
     - Dependencies actually change
   - NOT set when only `description` or `notes` change

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Task updated successfully",
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Custom Landscaping Work (Updated)",
    "description": "Install custom landscaping features with native plants",
    "projectStageId": "550e8400-e29b-41d4-a716-446655440000",
    "status": "NOT_STARTED",
    "estimatedDays": 7,
    "plannedStartDate": "2025-11-03",
    "plannedEndDate": "2025-11-09",
    "actualStartDate": null,
    "actualEndDate": null,
    "notes": "Updated: Client changed to include irrigation system",
    "adhocTaskFlag": true,
    "dependsOn": [
      {
        "dependencyId": "new-dep-uuid",
        "entityType": "TASK",
        "entityId": "550e8400-e29b-41d4-a716-446655440000",
        "entityName": "Excavation Work",
        "dependencyType": "FINISH_TO_START",
        "lagDays": 1,
        "status": "PENDING"
      }
    ],
    "dependents": [],
    "createdAt": "2025-10-15T16:45:00.000Z",
    "updatedAt": "2025-10-15T18:20:00.000Z"
  }
}
```

**Error Responses:**

**400 Bad Request - Circular Dependency:**
```json
{
  "success": false,
  "message": "Circular dependency detected: Updating these dependencies would create a cycle in the workflow."
}
```

**404 Not Found:**
```json
{
  "success": false,
  "message": "ProjectTask not found with id: 123e4567-e89b-12d3-a456-426614174000"
}
```

---

### 6. Delete Task

Delete a task and all associated data (steps, dependencies).

**Endpoint:**  
`DELETE /api/projects/{projectId}/tasks/{taskId}`

**Permissions:** ADMIN, PROJECT_MANAGER

**Path Parameters:**
- `projectId` (UUID, required) - Project ID
- `taskId` (UUID, required) - Task ID to delete

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Task deleted successfully",
  "data": null
}
```

**What Gets Deleted:**
1. The task itself
2. All steps belonging to this task (cascade delete)
3. All step requirements under those steps
4. All dependencies where this task depends on others
5. All dependencies where others depend on this task

**Important:**
- If task has dependencies, the project's `workflowRebuildRequired` flag is set to `true`
- Operation is **irreversible** - confirm with user before calling
- Both adhoc and template-based tasks can be deleted

**Error Response (404):**
```json
{
  "success": false,
  "message": "ProjectTask not found with id: 123e4567-e89b-12d3-a456-426614174000"
}
```

---

## Common Error Responses

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized: Token expired or invalid"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied: You can only manage tasks for projects belonging to your company"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "An unexpected error occurred",
  "errors": null
}
```

---

## Frontend Integration Guide

### 1. Task List Screen

**API Call:**
```javascript
// Get all tasks for a stage
const response = await fetch(
  `/api/projects/${projectId}/stages/${stageId}/tasks`,
  {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  }
);
const result = await response.json();
const tasks = result.data; // Array of tasks
```

**Display:**
- Show task cards with name, status, dates
- Badge for adhoc tasks: `adhocTaskFlag === true`
- Dependency indicators (dependsOn count, dependents count)
- Progress bar based on status

### 2. Create Adhoc Task Form

**Form Fields:**
- Task Name* (text input, required)
- Description (textarea)
- Estimated Days (number input)
- Planned Start Date (date picker)
- Planned End Date (date picker)
- Notes (textarea)
- Dependencies (multi-select from available tasks/stages)

**API Call:**
```javascript
const createTask = async (taskData) => {
  const response = await fetch(
    `/api/projects/${projectId}/stages/${stageId}/tasks`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(taskData)
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return await response.json();
};
```

**Validation:**
- Check for circular dependencies on frontend (optional)
- Validate date range (end >= start)
- Show warning when adding dependencies

### 3. Update Task Form

**Pre-populate:**
```javascript
// Fetch existing task data
const response = await fetch(
  `/api/projects/${projectId}/tasks/${taskId}`,
  {
    headers: { 'Authorization': `Bearer ${token}` }
  }
);
const { data: task } = await response.json();

// Pre-fill form with task data
form.name.value = task.name;
form.description.value = task.description;
// ... etc
```

**Update Call:**
```javascript
const updateTask = async (taskId, updates) => {
  const response = await fetch(
    `/api/projects/${projectId}/tasks/${taskId}`,
    {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updates)
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    // Handle circular dependency error specially
    if (error.message.includes('Circular dependency')) {
      alert('Cannot create circular dependencies!');
    }
    throw new Error(error.message);
  }
  
  return await response.json();
};
```

**Important:**
- Always send ALL fields (even unchanged ones)
- Use `null` for dependencies to keep existing ones
- Use `[]` to remove all dependencies
- Use `[...]` to replace dependencies

### 4. Delete Task

**Confirmation Dialog:**
```javascript
const deleteTask = async (taskId) => {
  // Show confirmation
  const confirmed = confirm(
    'Are you sure? This will delete the task and all its steps permanently.'
  );
  
  if (!confirmed) return;
  
  const response = await fetch(
    `/api/projects/${projectId}/tasks/${taskId}`,
    {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  
  if (!response.ok) {
    throw new Error('Failed to delete task');
  }
  
  return await response.json();
};
```

### 5. Dependency Management

**Fetch Available Tasks for Dependencies:**
```javascript
// Get all tasks in the same stage (can depend on same stage tasks)
const response = await fetch(
  `/api/projects/${projectId}/stages/${stageId}/tasks`,
  {
    headers: { 'Authorization': `Bearer ${token}` }
  }
);
const { data: availableTasks } = await response.json();

// Filter out the current task (can't depend on itself)
const dependencyOptions = availableTasks
  .filter(t => t.id !== currentTaskId)
  .map(t => ({
    value: t.id,
    label: t.name,
    type: 'TASK'
  }));
```

**Dependency UI Component:**
```jsx
<DependencySelector
  label="Depends On (this task depends on):"
  options={dependencyOptions}
  selected={task.dependsOn}
  onChange={(deps) => {
    // deps format: [{ entityType, entityId, dependencyType, lagDays }]
    setTask({ ...task, dependsOn: deps });
  }}
/>

<DependencySelector
  label="Dependents (these depend on this task):"
  options={dependencyOptions}
  selected={task.dependents}
  onChange={(deps) => {
    setTask({ ...task, dependents: deps });
  }}
/>
```

### 6. Status Badge Component

```jsx
const TaskStatusBadge = ({ status }) => {
  const statusConfig = {
    NOT_STARTED: { color: 'gray', label: 'Not Started' },
    IN_PROGRESS: { color: 'blue', label: 'In Progress' },
    ON_HOLD: { color: 'yellow', label: 'On Hold' },
    COMPLETED: { color: 'green', label: 'Completed' },
    CANCELLED: { color: 'red', label: 'Cancelled' }
  };
  
  const config = statusConfig[status] || statusConfig.NOT_STARTED;
  
  return (
    <span className={`badge badge-${config.color}`}>
      {config.label}
    </span>
  );
};
```

### 7. Error Handling

```javascript
const handleApiError = (error, response) => {
  if (response.status === 400 && error.message.includes('Circular dependency')) {
    return 'Cannot add this dependency - it would create a circular reference!';
  }
  if (response.status === 403) {
    return 'You do not have permission to perform this action';
  }
  if (response.status === 404) {
    return 'Task not found - it may have been deleted';
  }
  return error.message || 'An unexpected error occurred';
};
```

---

## Testing Checklist

- [ ] Create adhoc task with no dependencies
- [ ] Create adhoc task with dependencies
- [ ] Try to create circular dependency (should fail with 400)
- [ ] Update task - only notes (rebuild flag NOT set)
- [ ] Update task - change dates (rebuild flag SET)
- [ ] Update task - add/remove dependencies (rebuild flag SET)
- [ ] Get task details
- [ ] List all tasks in a stage
- [ ] List only adhoc tasks
- [ ] Delete task with dependencies
- [ ] Delete task without dependencies
- [ ] Try to access task from different company (should fail with 403)
- [ ] Try with invalid token (should fail with 401)

---

## Rate Limiting & Best Practices

1. **Caching:**
   - Cache task lists for 30 seconds
   - Invalidate cache after create/update/delete
   
2. **Optimistic Updates:**
   - Update UI immediately on user action
   - Roll back if API call fails
   
3. **Batch Operations:**
   - If creating multiple tasks, do sequentially to avoid race conditions
   - Show progress indicator
   
4. **Date Handling:**
   - Always use ISO 8601 format: `YYYY-MM-DD`
   - Handle timezone conversions on frontend
   
5. **Large Lists:**
   - Consider pagination if >50 tasks per stage
   - Implement virtual scrolling for performance

---

## Support

For questions or issues:
- Backend API: Check server logs
- Authentication: Verify JWT token is valid
- Permissions: Ensure user has correct role
- Circular Dependencies: Use dependency graph visualization tool

---

**Document Version:** 1.0  
**Last Updated:** October 15, 2025

