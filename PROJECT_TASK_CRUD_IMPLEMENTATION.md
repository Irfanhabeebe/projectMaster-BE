# Project Task CRUD Implementation

## Overview
This document describes the implementation of CRUD operations for `ProjectTask` entities, similar to the existing `ProjectStep` implementation. Project tasks can now be created, updated, and deleted both as adhoc (manually added) and template-based tasks.

## Components Implemented

### 1. Entity Updates

**File**: `ProjectTask.java`

- Added `adhocTaskFlag` field (Boolean, default: false) to distinguish adhoc tasks from template-based tasks
- Made `workflowTask` nullable to support adhoc tasks that aren't based on workflow templates
- Maintains relationship with `ProjectStage` and contains list of `ProjectStep`

### 2. DTOs Created

#### CreateAdhocTaskRequest
**File**: `CreateAdhocTaskRequest.java`

Request DTO for creating adhoc tasks with:
- Basic fields: name, description, estimatedDays, dates, notes
- `projectStageId`: Parent stage reference
- `dependsOn`: List of dependencies (what this task depends on)
- `dependents`: List of dependents (what depends on this task)
- Nested `TaskDependencyRequest` class for dependency specification

#### UpdateTaskRequest
**File**: `UpdateTaskRequest.java`

Request DTO for updating existing tasks with:
- All editable fields from create request
- Dependency lists (null = no change, empty = remove all, populated = replace)
- Works for both adhoc and template-based tasks

#### AdhocTaskResponse
**File**: `AdhocTaskResponse.java`

Response DTO containing:
- Task details (id, name, description, status, dates)
- `adhocTaskFlag` indicator
- `projectStageId` reference
- Dependencies (dependsOn and dependents) with entity names
- Timestamps (createdAt, updatedAt using Instant)
- Nested `DependencyInfo` class for dependency details

### 3. Service Layer

**File**: `ProjectTaskService.java`

Implements full CRUD with business logic:

#### Create Operation
- `createAdhocTask()`: Creates new adhoc task
- Validates project stage existence
- Validates user company access
- Creates task dependencies (both dependsOn and dependents)
- Validates circular dependencies before creation
- Sets workflow rebuild flag if dependencies are created

#### Read Operations
- `getTask()`: Get single task by ID
- `getTasksByProjectStage()`: Get all tasks for a stage
- `getAdhocTasksByProject()`: Get only adhoc tasks for a project

#### Update Operation
- `updateTask()`: Updates existing task
- Captures old dates to detect changes
- Compares dependencies to determine actual changes
- Only sets rebuild flag when:
  - Dates change (plannedStartDate or plannedEndDate)
  - Dependencies actually change (not just sent in request)
- Does NOT set flag for description/notes-only changes

#### Delete Operation
- `deleteTask()`: Deletes task and all related data
- Cascade deletes all associated steps
- Removes all dependencies (both directions)
- Sets rebuild flag if dependencies existed

#### Helper Methods
- Circular dependency validation (DFS-based cycle detection)
- Dependency comparison (signature-based)
- Date change detection
- Entity name fetching for response building
- User company access validation

### 4. Controller Layer

**File**: `ProjectTaskController.java`

REST endpoints with proper security:

| Method | Endpoint | Description | Security |
|--------|----------|-------------|----------|
| POST | `/api/projects/{projectId}/stages/{projectStageId}/tasks` | Create adhoc task | ADMIN, PROJECT_MANAGER |
| GET | `/api/projects/{projectId}/tasks/{taskId}` | Get task by ID | ADMIN, PROJECT_MANAGER, TRADIE |
| GET | `/api/projects/{projectId}/stages/{projectStageId}/tasks` | Get tasks by stage | ADMIN, PROJECT_MANAGER, TRADIE |
| GET | `/api/projects/{projectId}/tasks/adhoc` | Get adhoc tasks | ADMIN, PROJECT_MANAGER, TRADIE |
| PUT | `/api/projects/{projectId}/tasks/{taskId}` | Update task | ADMIN, PROJECT_MANAGER |
| DELETE | `/api/projects/{projectId}/tasks/{taskId}` | Delete task | ADMIN, PROJECT_MANAGER |

All endpoints:
- Include proper Swagger documentation
- Validate user authentication
- Return standardized `ApiResponse<T>` format
- Log operations for audit trail

### 5. Database Migration

**File**: `V42__Add_adhoc_task_support.sql`

Changes:
- Added `adhoc_task_flag` column (BOOLEAN, default: false)
- Made `workflow_task_id` nullable
- Added comments for clarity
- Created index on `adhoc_task_flag` for efficient querying

## Key Features

### 1. Circular Dependency Detection
- Uses DFS-based cycle detection algorithm
- Validates before both create and update operations
- Prevents invalid workflow configurations
- Scoped to tasks within the same stage

### 2. Smart Rebuild Flag Management
The workflow rebuild flag is set to `true` ONLY when:
- ✅ Planned start date changes
- ✅ Planned end date changes
- ✅ Dependencies actually change (added, removed, or modified)

The flag is NOT set when only these change:
- ❌ Description
- ❌ Notes
- ❌ Name
- ❌ Dependencies sent but unchanged (e.g., sending same dependency list)

### 3. Dependency Comparison
- Creates "signatures" for dependencies: `entityType:entityId:dependencyType:lagDays`
- Uses Set comparison for efficient detection
- Handles both "dependsOn" and "dependents" relationships
- Prevents unnecessary database operations

### 4. Security & Access Control
- Super users can access any project
- Regular users restricted to their company's projects
- Role-based endpoint access (ADMIN, PROJECT_MANAGER, TRADIE)
- Comprehensive validation at every operation

## Usage Examples

### Create Adhoc Task

```json
POST /api/projects/{projectId}/stages/{stageId}/tasks
{
  "name": "Custom Landscaping",
  "description": "Install custom landscaping features",
  "estimatedDays": 5,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-05",
  "notes": "Client special requirements",
  "dependsOn": [
    {
      "entityType": "TASK",
      "entityId": "task-uuid",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0
    }
  ]
}
```

### Update Task (Only Notes)

```json
PUT /api/projects/{projectId}/tasks/{taskId}
{
  "name": "Custom Landscaping",
  "description": "Install custom landscaping features",
  "estimatedDays": 5,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-05",
  "notes": "Updated notes - client changed requirements"
}
// Result: Rebuild flag NOT set (only notes changed)
```

### Update Task (Change Dates)

```json
PUT /api/projects/{projectId}/tasks/{taskId}
{
  "name": "Custom Landscaping",
  "description": "Install custom landscaping features",
  "estimatedDays": 5,
  "plannedStartDate": "2025-11-05",  // Changed
  "plannedEndDate": "2025-11-10",    // Changed
  "notes": "Updated notes"
}
// Result: Rebuild flag SET (dates changed)
```

### Delete Task

```http
DELETE /api/projects/{projectId}/tasks/{taskId}
// Result: Task deleted, all steps deleted, dependencies removed, rebuild flag set if dependencies existed
```

## Testing Scenarios

1. **Create adhoc task without dependencies** → Success, no rebuild flag
2. **Create adhoc task with dependencies** → Success, rebuild flag set
3. **Create with circular dependency** → 400 error, clear message
4. **Update only notes** → Success, no rebuild flag
5. **Update dates** → Success, rebuild flag set
6. **Update dependencies** → Success, rebuild flag set only if changed
7. **Delete task with dependencies** → Success, rebuild flag set
8. **Delete task without dependencies** → Success, no rebuild flag
9. **Access task from different company** → 403 Forbidden
10. **Super user access any task** → Success

## Comparison with ProjectStep Implementation

| Feature | ProjectTask | ProjectStep |
|---------|------------|-------------|
| Circular dependency check | ✅ Yes | ✅ Yes |
| Smart rebuild flag | ✅ Yes | ✅ Yes |
| Dependency comparison | ✅ Yes | ✅ Yes |
| Adhoc support | ✅ Yes | ✅ Yes |
| Cascade delete children | ✅ Yes (steps) | ✅ Yes (requirements) |
| Company access validation | ✅ Yes | ✅ Yes |
| Assignment support | ❌ No | ✅ Yes (crew/contractor) |

## Repository Methods Used

From `ProjectTaskRepository`:
- `findByProjectStageIdOrderByCreatedAt()` - Get tasks by stage
- `findByProjectStagesProjectId()` - Get tasks by project
- `findById()` - Get single task
- `save()` - Save/update task
- `delete()` - Delete task

## Future Enhancements

1. **Task Assignment**: Add support for assigning tasks to crews or contractors (similar to steps)
2. **Task Requirements**: Add requirement management for tasks
3. **Task Progress Tracking**: Enhanced status workflow and progress percentages
4. **Bulk Operations**: Batch create/update/delete for multiple tasks
5. **Task Templates**: Reusable task templates beyond workflow templates
6. **Task Cloning**: Clone task with all dependencies and steps

## Related Files

- Entity: `ProjectTask.java`
- DTOs: `CreateAdhocTaskRequest.java`, `UpdateTaskRequest.java`, `AdhocTaskResponse.java`
- Service: `ProjectTaskService.java`
- Controller: `ProjectTaskController.java`
- Repository: `ProjectTaskRepository.java`
- Migration: `V42__Add_adhoc_task_support.sql`
- Similar Implementation: `ProjectStepService.java`, `ProjectStepController.java`

