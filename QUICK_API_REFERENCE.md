# Quick API Reference - Field Removals

## Removed Fields Summary

| Field | Type | Removed From | Impact |
|-------|------|--------------|--------|
| `orderIndex` | Integer | ProjectStep, WorkflowStep, StandardWorkflowStep, WorkflowTask | Steps/tasks now ordered by creation time |
| `requiredSkills` | String (JSON) | ProjectStep, WorkflowStep, StandardWorkflowStep, WorkflowTask | No skills tracking on steps |
| `requirements` | String (JSON) | ProjectStep, WorkflowStep, StandardWorkflowStep, WorkflowTask | No requirements tracking on steps |

---

## Affected Endpoints - Quick List

### ✅ Response Changed (Fields Removed)

| Method | Endpoint | Response Impact |
|--------|----------|-----------------|
| `GET` | `/api/projects/{projectId}/workflow` | ❌ Step: `orderIndex`, `requiredSkills`, `requirements` |
| `GET` | `/api/projects/{projectId}/steps/{stepId}` | ❌ Step: `orderIndex`, `requiredSkills`, `requirements` |
| `GET` | `/api/projects/{projectId}/tasks/{taskId}/steps` | ❌ Step: `orderIndex`, `requiredSkills`, `requirements` |
| `GET` | `/api/projects/{projectId}/steps/adhoc` | ❌ Step: `orderIndex`, `requiredSkills`, `requirements` |
| `GET` | `/api/workflow/templates/{templateId}` | ❌ Step & Task: `orderIndex`, `requiredSkills`, `requirements` |
| `GET` | `/api/crew/dashboard` | ❌ Assignment: `stepOrderIndex`, `requiredSkills`, `requirements` |

### ⚠️ Request Changed (Don't Send Fields)

| Method | Endpoint | Request Impact |
|--------|----------|----------------|
| `POST` | `/api/projects/{projectId}/tasks/{taskId}/steps` | ❌ Don't send: `orderIndex`, `requiredSkills`, `requirements` |

### 🆕 New Endpoint

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/projects/{projectId}/recalculate-schedule` | Recalculate project schedule (use when `workflowRebuildRequired` is true) |

---

## Frontend Interface Updates

### Before (Remove These)
```typescript
interface ProjectStepResponse {
  id: string;
  name: string;
  orderIndex?: number;           // ❌ REMOVE
  requiredSkills?: string;       // ❌ REMOVE
  requirements?: string;         // ❌ REMOVE
  estimatedDays?: number;
  // ... other fields
}

interface WorkflowStepDetailResponse {
  id: string;
  name: string;
  orderIndex?: number;           // ❌ REMOVE
  requiredSkills?: string;       // ❌ REMOVE
  requirements?: string;         // ❌ REMOVE
  estimatedDays?: number;
  // ... other fields
}

interface WorkflowTaskDetailResponse {
  id: string;
  name: string;
  orderIndex?: number;           // ❌ REMOVE
  requiredSkills?: string;       // ❌ REMOVE
  requirements?: string;         // ❌ REMOVE
  estimatedDays?: number;
  // ... other fields
}

interface CrewAssignmentDto {
  assignmentId: string;
  stepId: string;
  stepOrderIndex?: number;       // ❌ REMOVE
  requiredSkills?: string;       // ❌ REMOVE
  requirements?: string;         // ❌ REMOVE
  // ... other fields
}

interface CreateAdhocStepRequest {
  name: string;
  orderIndex?: number;           // ❌ REMOVE
  requiredSkills?: string;       // ❌ REMOVE
  requirements?: string;         // ❌ REMOVE
  // ... other fields
}
```

### After (Updated Interfaces)
```typescript
interface ProjectStepResponse {
  id: string;
  name: string;
  description?: string;
  status: StepExecutionStatus;
  estimatedDays?: number;
  startDate?: string;
  endDate?: string;
  actualStartDate?: string;
  actualEndDate?: string;
  notes?: string;
  qualityCheckPassed?: boolean;
  specialty?: SpecialtyResponse;
  assignments?: ProjectStepAssignmentResponse[];
  dependencies?: DependencyResponse[];
}

interface WorkflowStepDetailResponse {
  id: string;
  name: string;
  description?: string;
  estimatedDays?: number;
  specialtyId?: string;
  specialtyName?: string;
  specialtyType?: string;
  version?: number;
  createdAt?: string;
  updatedAt?: string;
  stepRequirements?: WorkflowStepRequirementDetailResponse[];
  dependencies?: WorkflowDependencyDetailResponse[];
}

interface WorkflowTaskDetailResponse {
  id: string;
  name: string;
  description?: string;
  estimatedDays?: number;
  version?: number;
  createdAt?: string;
  updatedAt?: string;
  steps?: WorkflowStepDetailResponse[];
  dependencies?: WorkflowDependencyDetailResponse[];
}

interface CrewAssignmentDto {
  assignmentId: string;
  assignedToType: AssignmentType;
  assignmentStatus: AssignmentStatus;
  assignedDate?: string;
  stepId: string;
  stepName: string;
  stepDescription?: string;
  stepStatus: StepExecutionStatus;
  stepEstimatedDays?: number;
  stepStartDate?: string;
  stepEndDate?: string;
  specialtyName?: string;
  projectId: string;
  projectName: string;
  // ... other fields
}

interface CreateAdhocStepRequest {
  name: string;
  description?: string;
  specialtyId: string;
  estimatedDays?: number;
  plannedStartDate?: string;
  plannedEndDate?: string;
  notes?: string;
  dependsOnRequests?: CreateDependencyRequest[];
  dependentRequests?: CreateDependencyRequest[];
}
```

---

## Component Changes Required

### 1. Step Card Component
```typescript
// ❌ Remove
<div className="step-order-badge">{step.orderIndex}</div>
<div className="step-skills">{step.requiredSkills}</div>
<div className="step-requirements">{step.requirements}</div>

// ✅ Keep
<div className="step-name">{step.name}</div>
<div className="step-status">{step.status}</div>
<div className="step-specialty">{step.specialty?.specialtyName}</div>
<div className="step-dates">{step.startDate} - {step.endDate}</div>
```

### 2. Step Form Component
```typescript
// ❌ Remove these form fields
<input name="orderIndex" />
<input name="requiredSkills" />
<textarea name="requirements" />

// ✅ Keep these
<input name="name" required />
<textarea name="description" />
<select name="specialtyId" required />
<input name="estimatedDays" type="number" />
<input name="plannedStartDate" type="date" />
<input name="plannedEndDate" type="date" />
```

### 3. Workflow Template Viewer
```typescript
// ❌ Remove order index display
<span className="order-badge">{task.orderIndex}</span>
<span className="order-badge">{step.orderIndex}</span>

// ✅ Display in received order
{template.stages.map(stage =>
  stage.tasks.map(task =>
    task.steps.map(step => (
      <StepCard key={step.id} step={step} />
    ))
  )
)}
```

### 4. Schedule Recalculation
```typescript
// ✅ Add this functionality
interface ProjectWorkflowResponse {
  // ... other fields
  workflowRebuildRequired?: boolean;
}

function ProjectWorkflowView({ project }: Props) {
  const handleRecalculate = async () => {
    await api.post(`/api/projects/${project.id}/recalculate-schedule`);
    // Refresh workflow
    await fetchProjectWorkflow(project.id);
  };

  return (
    <div>
      {project.workflowRebuildRequired && (
        <button onClick={handleRecalculate}>
          Recalculate Schedule
        </button>
      )}
      {/* ... rest of workflow */}
    </div>
  );
}
```

---

## API Call Examples

### ✅ Correct - Creating Adhoc Step
```typescript
const createAdhocStep = async (projectId: string, taskId: string) => {
  const response = await api.post(
    `/api/projects/${projectId}/tasks/${taskId}/steps`,
    {
      name: "Install Wiring",
      description: "Install electrical wiring",
      specialtyId: "electrician-uuid",
      estimatedDays: 3,
      plannedStartDate: "2025-01-15",
      plannedEndDate: "2025-01-18",
      notes: "Use copper wiring",
      // ✅ Only send fields that exist
    }
  );
  return response.data;
};
```

### ❌ Incorrect - Don't Send Removed Fields
```typescript
const createAdhocStep = async (projectId: string, taskId: string) => {
  const response = await api.post(
    `/api/projects/${projectId}/tasks/${taskId}/steps`,
    {
      name: "Install Wiring",
      orderIndex: 5,              // ❌ Field doesn't exist
      requiredSkills: "[\"electrician\"]",  // ❌ Field doesn't exist
      requirements: "{}",          // ❌ Field doesn't exist
      specialtyId: "electrician-uuid",
      // ... other fields
    }
  );
  return response.data;
};
```

### ✅ Correct - Fetching Project Workflow
```typescript
const fetchProjectWorkflow = async (projectId: string) => {
  const response = await api.get(`/api/projects/${projectId}/workflow`);
  
  // ✅ Don't expect removed fields
  const workflow = response.data.data;
  workflow.stages.forEach(stage => {
    stage.tasks.forEach(task => {
      task.steps.forEach(step => {
        console.log(step.name);          // ✅ Available
        console.log(step.specialty);     // ✅ Available
        // console.log(step.orderIndex);  // ❌ Undefined
        // console.log(step.requiredSkills); // ❌ Undefined
      });
    });
  });
  
  return workflow;
};
```

---

## Testing Commands

### Test API Responses
```bash
# Get project workflow (should not include removed fields)
curl -X GET "http://localhost:8080/api/projects/{projectId}/workflow" \
  -H "Authorization: Bearer {token}"

# Create adhoc step (should accept request without removed fields)
curl -X POST "http://localhost:8080/api/projects/{projectId}/tasks/{taskId}/steps" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Step",
    "specialtyId": "uuid",
    "estimatedDays": 3
  }'

# Recalculate schedule
curl -X POST "http://localhost:8080/api/projects/{projectId}/recalculate-schedule" \
  -H "Authorization: Bearer {token}"
```

---

## Rollout Checklist

- [ ] Update TypeScript interfaces
- [ ] Remove UI components for removed fields
- [ ] Update API calls to not send removed fields
- [ ] Test create adhoc step form
- [ ] Test project workflow display
- [ ] Test workflow template viewer
- [ ] Test crew dashboard
- [ ] Add schedule recalculation button
- [ ] Test schedule recalculation
- [ ] Update any documentation
- [ ] Clear browser cache / rebuild
- [ ] Test in all environments (dev, staging, prod)

