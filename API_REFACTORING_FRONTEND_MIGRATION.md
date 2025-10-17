# API Refactoring - Frontend Migration Guide

**Date:** October 15, 2025  
**Version:** 2.0  
**Breaking Changes:** YES - Request DTOs simplified

---

## 🎯 What Changed

We've **simplified and unified** the Task and Step APIs by removing redundant data from request bodies. Parent IDs are now **only** in URL paths, not duplicated in request bodies.

### Benefits for Frontend

✅ **Cleaner API** - No duplicate IDs in path and body  
✅ **Single DTO** - Same request object for create and update  
✅ **Less Code** - Fewer TypeScript interfaces to maintain  
✅ **Type Safety** - Impossible to have mismatched IDs  
✅ **Better DX** - More intuitive API design  

---

## 📋 Summary of Changes

| Entity | Change | Impact |
|--------|--------|--------|
| **Steps** | Removed `projectTaskId` from request body | Use path parameter only |
| **Tasks** | Removed `projectStageId` from request body | Use path parameter only |
| **Both** | Merged Create/Update DTOs into single DTO | Use same interface for both operations |

---

## 🔄 Migration Steps

### Step 1: Update TypeScript Interfaces

#### Before (Old):
```typescript
// Separate interfaces for create and update
interface CreateStepRequest {
  projectTaskId: string;  // ❌ Removed
  name: string;
  description?: string;
  specialtyId: string;
  estimatedDays?: number;
  plannedStartDate?: string;
  plannedEndDate?: string;
  notes?: string;
  assignment?: StepAssignment;
  dependsOn?: StepDependency[];
  dependents?: StepDependency[];
}

interface UpdateStepRequest {
  // No projectTaskId
  name: string;
  description?: string;
  specialtyId: string;
  estimatedDays?: number;
  plannedStartDate?: string;
  plannedEndDate?: string;
  notes?: string;
  assignment?: StepAssignment;
  dependsOn?: StepDependency[];
  dependents?: StepDependency[];
}
```

#### After (New):
```typescript
// Single unified interface
interface StepRequest {
  // No projectTaskId - it comes from URL path
  name: string;
  description?: string;
  specialtyId: string;
  estimatedDays?: number;
  plannedStartDate?: string;  // ISO format: YYYY-MM-DD
  plannedEndDate?: string;    // ISO format: YYYY-MM-DD
  notes?: string;
  assignment?: StepAssignment;
  dependsOn?: StepDependency[];
  dependents?: StepDependency[];
}

interface StepAssignment {
  assignedToType: 'CREW' | 'CONTRACTING_COMPANY';
  crewId?: string;
  contractingCompanyId?: string;
  notes?: string;
  hourlyRate?: number;
  estimatedDays?: number;
}

interface StepDependency {
  entityType: 'STEP' | 'TASK' | 'STAGE';
  entityId: string;
  dependencyType?: 'FINISH_TO_START' | 'START_TO_START' | 'FINISH_TO_FINISH' | 'START_TO_FINISH';
  lagDays?: number;
  notes?: string;
}
```

#### Task Interfaces (Similar Pattern):
```typescript
// Before: CreateTaskRequest had projectStageId
// After: TaskRequest - no projectStageId

interface TaskRequest {
  // No projectStageId - it comes from URL path
  name: string;
  description?: string;
  estimatedDays?: number;
  plannedStartDate?: string;
  plannedEndDate?: string;
  notes?: string;
  dependsOn?: TaskDependency[];
  dependents?: TaskDependency[];
}

interface TaskDependency {
  entityType: 'TASK' | 'STAGE';
  entityId: string;
  dependencyType?: 'FINISH_TO_START' | 'START_TO_START' | 'FINISH_TO_FINISH' | 'START_TO_FINISH';
  lagDays?: number;
  notes?: string;
}
```

---

### Step 2: Update API Calls

#### Steps API

##### Create Step - BEFORE ❌
```typescript
const createStep = async (projectId: string, taskId: string, stepData: any) => {
  const response = await fetch(
    `/api/projects/${projectId}/tasks/${taskId}/steps`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        projectTaskId: taskId,  // ❌ REMOVE THIS
        name: stepData.name,
        description: stepData.description,
        specialtyId: stepData.specialtyId,
        // ... rest of fields
      })
    }
  );
  return response.json();
};
```

##### Create Step - AFTER ✅
```typescript
const createStep = async (projectId: string, taskId: string, stepData: StepRequest) => {
  const response = await fetch(
    `/api/projects/${projectId}/tasks/${taskId}/steps`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(stepData)  // ✅ Clean - no taskId in body
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return response.json();
};
```

##### Update Step - BEFORE ❌
```typescript
const updateStep = async (projectId: string, stepId: string, updateData: UpdateStepRequest) => {
  // Used different interface
  const response = await fetch(
    `/api/projects/${projectId}/steps/${stepId}`,
    {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(updateData)
    }
  );
  return response.json();
};
```

##### Update Step - AFTER ✅
```typescript
const updateStep = async (projectId: string, stepId: string, stepData: StepRequest) => {
  // Uses SAME interface as create!
  const response = await fetch(
    `/api/projects/${projectId}/steps/${stepId}`,
    {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(stepData)  // ✅ Same StepRequest interface
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return response.json();
};
```

#### Tasks API

##### Create Task - BEFORE ❌
```typescript
const createTask = async (projectId: string, stageId: string, taskData: any) => {
  const response = await fetch(
    `/api/projects/${projectId}/stages/${stageId}/tasks`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        projectStageId: stageId,  // ❌ REMOVE THIS
        name: taskData.name,
        description: taskData.description,
        // ... rest of fields
      })
    }
  );
  return response.json();
};
```

##### Create Task - AFTER ✅
```typescript
const createTask = async (projectId: string, stageId: string, taskData: TaskRequest) => {
  const response = await fetch(
    `/api/projects/${projectId}/stages/${stageId}/tasks`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(taskData)  // ✅ Clean - no stageId in body
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return response.json();
};
```

##### Update Task - AFTER ✅
```typescript
const updateTask = async (projectId: string, taskId: string, taskData: TaskRequest) => {
  // Uses SAME TaskRequest interface as create!
  const response = await fetch(
    `/api/projects/${projectId}/tasks/${taskId}`,
    {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(taskData)  // ✅ Same TaskRequest interface
    }
  );
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return response.json();
};
```

---

### Step 3: Update Form Components

#### Step Form Component - React Example

```typescript
import React, { useState } from 'react';

interface StepFormProps {
  projectId: string;
  taskId: string;
  initialData?: StepRequest;  // For edit mode
  onSuccess: () => void;
}

const StepForm: React.FC<StepFormProps> = ({ 
  projectId, 
  taskId, 
  initialData, 
  onSuccess 
}) => {
  const [formData, setFormData] = useState<StepRequest>(
    initialData || {
      name: '',
      specialtyId: '',
      description: '',
      estimatedDays: 0,
      plannedStartDate: '',
      plannedEndDate: '',
      notes: '',
      assignment: null,
      dependsOn: [],
      dependents: []
    }
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (initialData) {
        // Update existing step
        await updateStep(projectId, initialData.id, formData);
      } else {
        // Create new step
        await createStep(projectId, taskId, formData);
      }
      onSuccess();
    } catch (error) {
      console.error('Failed to save step:', error);
      alert(error.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        value={formData.name}
        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
        placeholder="Step Name"
        required
      />
      
      <select
        value={formData.specialtyId}
        onChange={(e) => setFormData({ ...formData, specialtyId: e.target.value })}
        required
      >
        <option value="">Select Specialty</option>
        {/* Populate from /api/specialties */}
      </select>
      
      <textarea
        value={formData.description || ''}
        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
        placeholder="Description"
      />
      
      <input
        type="number"
        value={formData.estimatedDays || ''}
        onChange={(e) => setFormData({ 
          ...formData, 
          estimatedDays: parseInt(e.target.value) || 0 
        })}
        placeholder="Estimated Days"
        min="0"
      />
      
      <input
        type="date"
        value={formData.plannedStartDate || ''}
        onChange={(e) => setFormData({ ...formData, plannedStartDate: e.target.value })}
      />
      
      <input
        type="date"
        value={formData.plannedEndDate || ''}
        onChange={(e) => setFormData({ ...formData, plannedEndDate: e.target.value })}
      />
      
      <textarea
        value={formData.notes || ''}
        onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
        placeholder="Notes"
      />
      
      {/* Dependency selectors */}
      <DependencySelector
        label="Depends On"
        value={formData.dependsOn}
        onChange={(deps) => setFormData({ ...formData, dependsOn: deps })}
      />
      
      <button type="submit">
        {initialData ? 'Update Step' : 'Create Step'}
      </button>
    </form>
  );
};
```

#### Task Form Component - React Example

```typescript
interface TaskFormProps {
  projectId: string;
  stageId: string;
  initialData?: TaskRequest;  // For edit mode
  onSuccess: () => void;
}

const TaskForm: React.FC<TaskFormProps> = ({ 
  projectId, 
  stageId, 
  initialData, 
  onSuccess 
}) => {
  const [formData, setFormData] = useState<TaskRequest>(
    initialData || {
      name: '',
      description: '',
      estimatedDays: 0,
      plannedStartDate: '',
      plannedEndDate: '',
      notes: '',
      dependsOn: [],
      dependents: []
    }
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (initialData) {
        // Update existing task
        await updateTask(projectId, initialData.id, formData);
      } else {
        // Create new task
        await createTask(projectId, stageId, formData);
      }
      onSuccess();
    } catch (error) {
      console.error('Failed to save task:', error);
      alert(error.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Similar structure to StepForm */}
      <input
        type="text"
        value={formData.name}
        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
        placeholder="Task Name"
        required
      />
      
      {/* ... other fields ... */}
      
      <button type="submit">
        {initialData ? 'Update Task' : 'Create Task'}
      </button>
    </form>
  );
};
```

---

## 📝 Complete API Reference

### Steps API

#### 1. Create Step
```http
POST /api/projects/{projectId}/tasks/{taskId}/steps
```

**Request Body:**
```json
{
  "name": "Install Custom Feature",
  "description": "Install custom outdoor feature",
  "specialtyId": "550e8400-e29b-41d4-a716-446655440000",
  "estimatedDays": 3,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-03",
  "notes": "Client special requirement",
  "assignment": {
    "assignedToType": "CREW",
    "crewId": "crew-uuid",
    "hourlyRate": 75.50,
    "estimatedDays": 3
  },
  "dependsOn": [
    {
      "entityType": "STEP",
      "entityId": "previous-step-uuid",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0
    }
  ],
  "dependents": []
}
```

**Response (201):**
```json
{
  "success": true,
  "message": "Adhoc step created successfully",
  "data": {
    "id": "new-step-uuid",
    "name": "Install Custom Feature",
    "projectTaskId": "task-uuid",
    "specialtyId": "specialty-uuid",
    "specialtyName": "Carpentry",
    "status": "NOT_STARTED",
    "adhocStepFlag": true,
    "assignments": [
      {
        "assignmentId": "assignment-uuid",
        "assignedToType": "CREW",
        "assignedToId": "crew-uuid",
        "assignedToName": "John Smith",
        "status": "PENDING"
      }
    ],
    "dependsOn": [...],
    "dependents": [],
    "createdAt": "2025-10-15T16:45:00.000Z",
    "updatedAt": "2025-10-15T16:45:00.000Z"
  }
}
```

#### 2. Update Step
```http
PUT /api/projects/{projectId}/steps/{stepId}
```

**Request Body:** (Same as Create - use `StepRequest`)
```json
{
  "name": "Install Custom Feature (Updated)",
  "description": "Updated description",
  "specialtyId": "550e8400-e29b-41d4-a716-446655440000",
  "estimatedDays": 4,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-04",
  "notes": "Updated notes",
  "assignment": null,
  "dependsOn": null,
  "dependents": null
}
```

**Important Notes:**
- Send **ALL fields** (required fields must always be present)
- Use `null` to keep existing values for:
  - `assignment` - keeps existing assignment
  - `dependsOn` - keeps existing dependencies
  - `dependents` - keeps existing dependents
- Use `[]` to **remove all** dependencies
- Use `[...]` to **replace** with new list

---

### Tasks API

#### 1. Create Task
```http
POST /api/projects/{projectId}/stages/{stageId}/tasks
```

**Request Body:**
```json
{
  "name": "Custom Landscaping",
  "description": "Install custom landscaping features",
  "estimatedDays": 5,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-05",
  "notes": "Native plants only",
  "dependsOn": [],
  "dependents": []
}
```

#### 2. Update Task
```http
PUT /api/projects/{projectId}/tasks/{taskId}
```

**Request Body:** (Same as Create - use `TaskRequest`)
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

## 🔧 Code Migration Examples

### Example 1: Create Step Form

#### Before ❌
```typescript
const handleCreateStep = async (formData: any) => {
  const requestBody = {
    projectTaskId: taskId,  // Duplicated from URL
    name: formData.name,
    description: formData.description,
    specialtyId: formData.specialtyId,
    estimatedDays: formData.estimatedDays,
    plannedStartDate: formData.plannedStartDate,
    plannedEndDate: formData.plannedEndDate,
    notes: formData.notes,
    assignment: formData.assignment,
    dependsOn: formData.dependsOn || [],
    dependents: formData.dependents || []
  };
  
  const response = await fetch(
    `/api/projects/${projectId}/tasks/${taskId}/steps`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    }
  );
  
  return response.json();
};
```

#### After ✅
```typescript
const handleCreateStep = async (formData: StepRequest) => {
  // Clean - no manual manipulation needed
  const response = await fetch(
    `/api/projects/${projectId}/tasks/${taskId}/steps`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(formData)  // Direct pass-through
    }
  );
  
  if (!response.ok) {
    throw new Error('Failed to create step');
  }
  
  return response.json();
};
```

### Example 2: Edit Step Form Pre-population

#### Before ❌
```typescript
const loadStepForEdit = async (stepId: string) => {
  const response = await fetch(
    `/api/projects/${projectId}/steps/${stepId}`,
    { headers: { 'Authorization': `Bearer ${token}` } }
  );
  
  const { data: step } = await response.json();
  
  // Had to use different interface for update
  const updateFormData: UpdateStepRequest = {
    name: step.name,
    description: step.description,
    specialtyId: step.specialtyId,
    estimatedDays: step.estimatedDays,
    plannedStartDate: step.plannedStartDate,
    plannedEndDate: step.plannedEndDate,
    notes: step.notes,
    assignment: step.assignments?.[0] || null,
    dependsOn: step.dependsOn || [],
    dependents: step.dependents || []
  };
  
  setFormData(updateFormData);
};
```

#### After ✅
```typescript
const loadStepForEdit = async (stepId: string) => {
  const response = await fetch(
    `/api/projects/${projectId}/steps/${stepId}`,
    { headers: { 'Authorization': `Bearer ${token}` } }
  );
  
  const { data: step } = await response.json();
  
  // Direct mapping - same interface!
  const formData: StepRequest = {
    name: step.name,
    description: step.description,
    specialtyId: step.specialtyId,
    estimatedDays: step.estimatedDays,
    plannedStartDate: step.plannedStartDate,
    plannedEndDate: step.plannedEndDate,
    notes: step.notes,
    assignment: step.assignments?.[0] || null,
    dependsOn: step.dependsOn || [],
    dependents: step.dependents || []
  };
  
  setFormData(formData);
};
```

### Example 3: Reusable Form Component

```typescript
interface UnifiedStepFormProps {
  mode: 'create' | 'edit';
  projectId: string;
  taskId?: string;  // Required for create mode
  stepId?: string;  // Required for edit mode
  onSuccess: () => void;
}

const UnifiedStepForm: React.FC<UnifiedStepFormProps> = ({
  mode,
  projectId,
  taskId,
  stepId,
  onSuccess
}) => {
  const [formData, setFormData] = useState<StepRequest>({
    name: '',
    specialtyId: '',
    // ... defaults
  });

  useEffect(() => {
    if (mode === 'edit' && stepId) {
      loadStepData(stepId);
    }
  }, [mode, stepId]);

  const loadStepData = async (id: string) => {
    const response = await fetch(
      `/api/projects/${projectId}/steps/${id}`,
      { headers: { 'Authorization': `Bearer ${token}` } }
    );
    const { data } = await response.json();
    
    // Map response to form (same structure!)
    setFormData({
      name: data.name,
      description: data.description,
      specialtyId: data.specialtyId,
      estimatedDays: data.estimatedDays,
      plannedStartDate: data.plannedStartDate,
      plannedEndDate: data.plannedEndDate,
      notes: data.notes,
      assignment: data.assignments?.[0] || null,
      dependsOn: data.dependsOn || [],
      dependents: data.dependents || []
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      if (mode === 'create') {
        await createStep(projectId, taskId!, formData);
      } else {
        await updateStep(projectId, stepId!, formData);
      }
      onSuccess();
    } catch (error) {
      console.error('Failed to save:', error);
      alert(error.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields - same for both modes */}
      <button type="submit">
        {mode === 'create' ? 'Create Step' : 'Update Step'}
      </button>
    </form>
  );
};
```

---

## ⚠️ Common Migration Pitfalls

### 1. Forgetting to Remove Parent ID

❌ **WRONG:**
```typescript
// Still including taskId in body
const body = {
  projectTaskId: taskId,  // ❌ Remove this!
  name: 'New Step'
};
```

✅ **CORRECT:**
```typescript
// Only in URL path
const url = `/api/projects/${projectId}/tasks/${taskId}/steps`;
const body = {
  name: 'New Step'  // ✅ Clean
};
```

### 2. Using Different Interfaces for Create/Update

❌ **WRONG:**
```typescript
interface CreateStepRequest { ... }
interface UpdateStepRequest { ... }  // ❌ Delete this

const createStep = (data: CreateStepRequest) => { ... }
const updateStep = (data: UpdateStepRequest) => { ... }
```

✅ **CORRECT:**
```typescript
interface StepRequest { ... }  // ✅ Single interface

const createStep = (data: StepRequest) => { ... }
const updateStep = (data: StepRequest) => { ... }  // Same type!
```

### 3. Not Handling Null vs Empty Array for Dependencies

**Understanding:**
- `dependsOn: null` → Don't change existing dependencies
- `dependsOn: []` → Remove all dependencies
- `dependsOn: [...]` → Replace with new list

```typescript
// Update only notes - keep dependencies unchanged
const updateOnlyNotes = async (stepId: string, newNotes: string) => {
  const stepData: StepRequest = {
    ...existingStepData,  // All current values
    notes: newNotes,
    dependsOn: null,      // ✅ Keep existing
    dependents: null      // ✅ Keep existing
  };
  
  await updateStep(projectId, stepId, stepData);
};

// Remove all dependencies
const clearDependencies = async (stepId: string) => {
  const stepData: StepRequest = {
    ...existingStepData,
    dependsOn: [],        // ✅ Clear all
    dependents: []        // ✅ Clear all
  };
  
  await updateStep(projectId, stepId, stepData);
};
```

---

## 🧪 Testing Checklist

Before deploying frontend changes:

### Steps
- [ ] Create step without dependencies works
- [ ] Create step with dependencies works
- [ ] taskId NOT sent in request body (verify in network tab)
- [ ] Update step - notes only (dependencies stay unchanged)
- [ ] Update step - clear all dependencies with `[]`
- [ ] Update step - add new dependencies
- [ ] Same form component works for both create and edit
- [ ] TypeScript types compile without errors

### Tasks
- [ ] Create task without dependencies works
- [ ] Create task with dependencies works
- [ ] stageId NOT sent in request body (verify in network tab)
- [ ] Update task - notes only (dependencies stay unchanged)
- [ ] Update task - clear all dependencies with `[]`
- [ ] Update task - add new dependencies
- [ ] Same form component works for both create and edit

---

## 🚀 Deployment Strategy

### Phase 1: Update TypeScript Interfaces
1. Create new `StepRequest` interface (remove `projectTaskId`)
2. Create new `TaskRequest` interface (remove `projectStageId`)
3. Delete old `CreateStepRequest`, `UpdateStepRequest` interfaces
4. Delete old `CreateTaskRequest`, `UpdateTaskRequest` interfaces

### Phase 2: Update API Service Layer
1. Update `createStep()` function - remove `projectTaskId` from body
2. Update `updateStep()` function - use `StepRequest` type
3. Update `createTask()` function - remove `projectStageId` from body
4. Update `updateTask()` function - use `TaskRequest` type

### Phase 3: Update React Components
1. Refactor step forms to use unified `StepRequest`
2. Refactor task forms to use unified `TaskRequest`
3. Update edit mode data loading
4. Test all CRUD operations

### Phase 4: Cleanup
1. Remove old interfaces
2. Remove any helper functions that manipulated parent IDs
3. Update documentation/comments

---

## 📊 Before/After Comparison

### Request Size Reduction

**Before:**
```json
POST /api/projects/{projectId}/tasks/{taskId}/steps
{
  "projectTaskId": "550e8400-e29b-41d4-a716-446655440000",  // 36 chars
  "name": "Step Name",
  "description": "...",
  // ... rest of fields
}
// Total: ~500 bytes
```

**After:**
```json
POST /api/projects/{projectId}/tasks/{taskId}/steps
{
  "name": "Step Name",
  "description": "...",
  // ... rest of fields
}
// Total: ~450 bytes (10% smaller)
```

### Code Reduction

**Before:**
- 4 TypeScript interfaces (CreateStep, UpdateStep, CreateTask, UpdateTask)
- Manual ID manipulation in forms
- Risk of ID mismatch
- ~200 lines of interface code

**After:**
- 2 TypeScript interfaces (StepRequest, TaskRequest)
- Direct pass-through in forms
- No ID mismatch possible
- ~100 lines of interface code

**Savings: 50% less code!**

---

## 🎓 Developer Experience Improvements

### 1. Form Reusability
```typescript
// One component for both create and edit!
<StepForm 
  mode={isEditing ? 'edit' : 'create'}
  projectId={projectId}
  taskId={taskId}
  stepId={isEditing ? stepId : undefined}
  onSuccess={handleSuccess}
/>
```

### 2. Type Safety
```typescript
// TypeScript prevents ID mismatch at compile time
const createStep = (
  projectId: string, 
  taskId: string,        // ✅ From path
  data: StepRequest      // ✅ No taskId field exists
) => {
  // Impossible to send taskId in body!
};
```

### 3. Simpler State Management
```typescript
// Before: Different state shapes for create/edit
const [createFormData, setCreateFormData] = useState<CreateStepRequest>({...});
const [updateFormData, setUpdateFormData] = useState<UpdateStepRequest>({...});

// After: Same state shape
const [formData, setFormData] = useState<StepRequest>({...});
```

---

## 📱 Mobile/React Native Considerations

The same patterns apply:

```typescript
// React Native example
const createStep = async (taskId: string, stepData: StepRequest) => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/projects/${projectId}/tasks/${taskId}/steps`,
      {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${await getToken()}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(stepData)  // ✅ Clean
      }
    );
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Create step failed:', error);
    throw error;
  }
};
```

---

## 🆘 Support & Troubleshooting

### Issue: "Field projectTaskId is required"

**Cause:** Still sending old request format  
**Fix:** Remove `projectTaskId` from request body

### Issue: TypeScript compilation error

**Cause:** Still using old interfaces  
**Fix:** Update to new `StepRequest`/`TaskRequest` interfaces

### Issue: 400 Bad Request

**Cause:** Missing required fields  
**Fix:** Ensure `name` and `specialtyId` (for steps) are always sent

### Issue: Dependencies not being saved

**Cause:** Sending `undefined` instead of `null` or `[]`  
**Fix:** Use `null` to keep existing, `[]` to clear, `[...]` to replace

---

## 📞 Contact

For questions about this migration:
- Backend changes: Check service layer signatures
- API endpoints: Unchanged (only request bodies simplified)
- Breaking changes: Yes - request DTOs changed

---

## ✅ Final Checklist

Before marking migration complete:

**Backend:**
- [x] New `StepRequest` DTO created
- [x] New `TaskRequest` DTO created
- [x] Controllers updated
- [x] Services updated
- [x] No linting errors

**Frontend:**
- [ ] TypeScript interfaces updated
- [ ] Step create API calls updated
- [ ] Step update API calls updated
- [ ] Task create API calls updated
- [ ] Task update API calls updated
- [ ] Form components refactored
- [ ] Old interfaces removed
- [ ] All tests passing
- [ ] QA testing complete

---

**Document Version:** 2.0  
**Last Updated:** October 15, 2025  
**Breaking Change:** YES  
**Backward Compatible:** NO - Frontend must update before using

---

## 🎉 Summary

The API is now cleaner, simpler, and more maintainable. Parent IDs belong in URL paths, not request bodies. This is a one-time breaking change that improves the API for long-term use.

**Key Takeaway:** One DTO to rule them all! 🚀

