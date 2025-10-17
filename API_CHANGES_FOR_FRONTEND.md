# API Changes - Steps & Tasks

**Date:** October 15, 2025  
**Breaking Change:** YES - Request body structure simplified

---

## 🔄 What Changed

**Parent IDs removed from request bodies** - they're already in the URL path.

| Entity | Removed Field | Now Use |
|--------|---------------|---------|
| Steps | `projectTaskId` | Path parameter only |
| Tasks | `projectStageId` | Path parameter only |

**Bonus:** Same request object for both create and update operations.

---

## 📝 Steps API

### Create Step

**Endpoint:** `POST /api/projects/{projectId}/tasks/{taskId}/steps`

**Old Request ❌:**
```json
{
  "projectTaskId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Install Custom Feature",
  "description": "...",
  "specialtyId": "specialty-uuid",
  "estimatedDays": 3,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-03",
  "notes": "...",
  "assignment": {...},
  "dependsOn": [...],
  "dependents": [...]
}
```

**New Request ✅:**
```json
{
  "name": "Install Custom Feature",
  "description": "...",
  "specialtyId": "specialty-uuid",
  "estimatedDays": 3,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-03",
  "notes": "...",
  "assignment": {...},
  "dependsOn": [...],
  "dependents": [...]
}
```

### Update Step

**Endpoint:** `PUT /api/projects/{projectId}/steps/{stepId}`

**Request:** (Same as create - use same TypeScript interface)
```json
{
  "name": "Install Custom Feature (Updated)",
  "description": "Updated description",
  "specialtyId": "specialty-uuid",
  "estimatedDays": 4,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-04",
  "notes": "Updated notes",
  "assignment": null,
  "dependsOn": null,
  "dependents": null
}
```

**Dependency Handling:**
- `dependsOn: null` → Keep existing dependencies
- `dependsOn: []` → Remove all dependencies
- `dependsOn: [...]` → Replace with new list

---

## 📝 Tasks API

### Create Task

**Endpoint:** `POST /api/projects/{projectId}/stages/{stageId}/tasks`

**Old Request ❌:**
```json
{
  "projectStageId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Custom Landscaping",
  "description": "...",
  "estimatedDays": 5,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-05",
  "notes": "...",
  "dependsOn": [...],
  "dependents": [...]
}
```

**New Request ✅:**
```json
{
  "name": "Custom Landscaping",
  "description": "...",
  "estimatedDays": 5,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-05",
  "notes": "...",
  "dependsOn": [...],
  "dependents": [...]
}
```

### Update Task

**Endpoint:** `PUT /api/projects/{projectId}/tasks/{taskId}`

**Request:** (Same as create - use same TypeScript interface)
```json
{
  "name": "Custom Landscaping (Updated)",
  "description": "Updated description",
  "estimatedDays": 7,
  "plannedStartDate": "2025-11-03",
  "plannedEndDate": "2025-11-09",
  "notes": "Updated notes",
  "dependsOn": null,
  "dependents": null
}
```

---

## 💻 TypeScript Updates

### Before ❌

```typescript
interface CreateStepRequest {
  projectTaskId: string;  // Remove
  name: string;
  description?: string;
  specialtyId: string;
  // ... rest
}

interface UpdateStepRequest {
  name: string;
  description?: string;
  specialtyId: string;
  // ... rest
}

interface CreateTaskRequest {
  projectStageId: string;  // Remove
  name: string;
  // ... rest
}

interface UpdateTaskRequest {
  name: string;
  // ... rest
}
```

### After ✅

```typescript
// Single interface for both create and update!
interface StepRequest {
  name: string;
  description?: string;
  specialtyId: string;
  estimatedDays?: number;
  plannedStartDate?: string;  // YYYY-MM-DD
  plannedEndDate?: string;    // YYYY-MM-DD
  notes?: string;
  assignment?: {
    assignedToType: 'CREW' | 'CONTRACTING_COMPANY';
    crewId?: string;
    contractingCompanyId?: string;
    notes?: string;
    hourlyRate?: number;
    estimatedDays?: number;
  };
  dependsOn?: Array<{
    entityType: 'STEP' | 'TASK' | 'STAGE';
    entityId: string;
    dependencyType?: 'FINISH_TO_START' | 'START_TO_START' | 'FINISH_TO_FINISH' | 'START_TO_FINISH';
    lagDays?: number;
    notes?: string;
  }>;
  dependents?: Array<{
    entityType: 'STEP' | 'TASK' | 'STAGE';
    entityId: string;
    dependencyType?: 'FINISH_TO_START' | 'START_TO_START' | 'FINISH_TO_FINISH' | 'START_TO_FINISH';
    lagDays?: number;
    notes?: string;
  }>;
}

// Single interface for both create and update!
interface TaskRequest {
  name: string;
  description?: string;
  estimatedDays?: number;
  plannedStartDate?: string;  // YYYY-MM-DD
  plannedEndDate?: string;    // YYYY-MM-DD
  notes?: string;
  dependsOn?: Array<{
    entityType: 'TASK' | 'STAGE';
    entityId: string;
    dependencyType?: 'FINISH_TO_START' | 'START_TO_START' | 'FINISH_TO_FINISH' | 'START_TO_FINISH';
    lagDays?: number;
    notes?: string;
  }>;
  dependents?: Array<{
    entityType: 'TASK' | 'STAGE';
    entityId: string;
    dependencyType?: 'FINISH_TO_START' | 'START_TO_START' | 'FINISH_TO_FINISH' | 'START_TO_FINISH';
    lagDays?: number;
    notes?: string;
  }>;
}
```

---

## 🔧 Code Changes

### Steps

```typescript
// Create Step - BEFORE ❌
const createStep = async (taskId: string, data: any) => {
  await fetch(`/api/projects/${projectId}/tasks/${taskId}/steps`, {
    method: 'POST',
    body: JSON.stringify({
      projectTaskId: taskId,  // ❌ REMOVE THIS LINE
      name: data.name,
      // ... rest
    })
  });
};

// Create Step - AFTER ✅
const createStep = async (taskId: string, data: StepRequest) => {
  await fetch(`/api/projects/${projectId}/tasks/${taskId}/steps`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)  // ✅ Clean - no taskId
  });
};

// Update Step - AFTER ✅ (uses SAME interface)
const updateStep = async (stepId: string, data: StepRequest) => {
  await fetch(`/api/projects/${projectId}/steps/${stepId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)  // ✅ Same StepRequest
  });
};
```

### Tasks

```typescript
// Create Task - BEFORE ❌
const createTask = async (stageId: string, data: any) => {
  await fetch(`/api/projects/${projectId}/stages/${stageId}/tasks`, {
    method: 'POST',
    body: JSON.stringify({
      projectStageId: stageId,  // ❌ REMOVE THIS LINE
      name: data.name,
      // ... rest
    })
  });
};

// Create Task - AFTER ✅
const createTask = async (stageId: string, data: TaskRequest) => {
  await fetch(`/api/projects/${projectId}/stages/${stageId}/tasks`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)  // ✅ Clean - no stageId
  });
};

// Update Task - AFTER ✅ (uses SAME interface)
const updateTask = async (taskId: string, data: TaskRequest) => {
  await fetch(`/api/projects/${projectId}/tasks/${taskId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)  // ✅ Same TaskRequest
  });
};
```

---

## ⚠️ Important Notes

### 1. Update Mode - Send All Fields
When updating, you must send **all required fields**, not just changed ones:

```typescript
// ✅ CORRECT - Send all fields
const updateStep = {
  name: existingStep.name,           // Keep existing
  description: existingStep.description,
  specialtyId: existingStep.specialtyId,
  estimatedDays: existingStep.estimatedDays,
  plannedStartDate: existingStep.plannedStartDate,
  plannedEndDate: existingStep.plannedEndDate,
  notes: "Updated notes only",       // Changed field
  assignment: null,                  // Keep existing
  dependsOn: null,                   // Keep existing
  dependents: null                   // Keep existing
};
```

### 2. Dependency Management

| Value | Meaning |
|-------|---------|
| `null` | Don't change existing dependencies |
| `[]` | Remove all dependencies |
| `[{...}, {...}]` | Replace with this list |

```typescript
// Update only notes - keep everything else
const data: StepRequest = {
  ...existingStep,
  notes: "Updated notes",
  dependsOn: null,      // ✅ Keep existing
  dependents: null      // ✅ Keep existing
};

// Clear all dependencies
const data: StepRequest = {
  ...existingStep,
  dependsOn: [],        // ✅ Clear all
  dependents: []        // ✅ Clear all
};
```

---

## ✅ Migration Checklist

**Required Changes:**
- [ ] Remove `projectTaskId` from step create requests
- [ ] Remove `projectStageId` from task create requests
- [ ] Use `StepRequest` interface for both create and update
- [ ] Use `TaskRequest` interface for both create and update
- [ ] Delete old interfaces (`CreateStepRequest`, `UpdateStepRequest`, etc.)
- [ ] Test create operations
- [ ] Test update operations

---

## 🧪 Quick Test

```bash
# Test Step Creation (should work without projectTaskId)
curl -X POST http://localhost:8080/api/projects/{projectId}/tasks/{taskId}/steps \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Step","specialtyId":"specialty-uuid"}'

# Test Task Creation (should work without projectStageId)
curl -X POST http://localhost:8080/api/projects/{projectId}/stages/{stageId}/tasks \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Task"}'
```

---

## 📞 Questions?

- **"Will old requests still work?"** No - remove parent ID fields
- **"Same interface for create and update?"** Yes! Use `StepRequest` and `TaskRequest` for both
- **"What about responses?"** No changes - responses are the same

---

**TL;DR:** 
1. Remove `projectTaskId` from step requests
2. Remove `projectStageId` from task requests  
3. Use same TypeScript interface for create and update
4. That's it! 🎉

