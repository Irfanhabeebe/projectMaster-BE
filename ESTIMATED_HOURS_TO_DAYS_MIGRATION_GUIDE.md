# Estimated Hours to Days Migration Guide

## 📋 Overview
This document outlines the complete migration from `estimatedHours` to `estimatedDays` across the ProjectMaster application. This change improves project planning for residential construction by using business days instead of hours.

## 🎯 Migration Rationale
- **Better Project Planning**: Days are more practical for residential construction tasks
- **Easier Calendar Integration**: Direct day-based scheduling without conversion
- **Clearer Client Communication**: "2 days" vs "16 hours" is more intuitive
- **Improved Dependency Management**: Works better with the existing dependency system

---

## 🔄 Frontend Changes Required

### 1. Request DTOs/Interfaces

#### Assignment Creation
```typescript
// ❌ OLD Interface
interface CreateAssignmentRequest {
  projectStepId: string;
  assignedToType: 'CREW' | 'CONTRACTING_COMPANY';
  crewId?: string;
  contractingCompanyId?: string;
  notes?: string;
  hourlyRate?: number;
  estimatedHours?: number;  // ❌ REMOVE THIS
}

// ✅ NEW Interface
interface CreateAssignmentRequest {
  projectStepId: string;
  assignedToType: 'CREW' | 'CONTRACTING_COMPANY';
  crewId?: string;
  contractingCompanyId?: string;
  notes?: string;
  hourlyRate?: number;
  estimatedDays?: number;   // ✅ ADD THIS
}
```

### 2. Response DTOs/Interfaces

#### Assignment Responses
```typescript
// ❌ OLD Interface
interface AssignmentResponse {
  id: string;
  projectStepId: string;
  assignedToType: string;
  status: string;
  // ... other fields
  estimatedHours?: number;     // ❌ REMOVE
}

// ✅ NEW Interface  
interface AssignmentResponse {
  id: string;
  projectStepId: string;
  assignedToType: string;
  status: string;
  // ... other fields
  estimatedDays?: number;      // ✅ ADD
}
```

#### Crew Assignment DTO
```typescript
// ❌ OLD Interface
interface CrewAssignmentDto {
  assignmentId: string;
  // ... assignment fields
  estimatedHours?: number;        // ❌ REMOVE
  // ... project/stage/task fields
  stepEstimatedHours?: number;    // ❌ REMOVE
  // ... other fields
}

// ✅ NEW Interface
interface CrewAssignmentDto {
  assignmentId: string;
  // ... assignment fields
  estimatedDays?: number;         // ✅ ADD
  // ... project/stage/task fields  
  stepEstimatedDays?: number;     // ✅ ADD
  // ... other fields
}
```

#### Project Workflow Response
```typescript
// ❌ OLD Interface
interface ProjectWorkflowResponse {
  id: string;
  name: string;
  stages: Array<{
    id: string;
    name: string;
    tasks: Array<{
      id: string;
      name: string;
      estimatedHours?: number;    // ❌ REMOVE
      steps: Array<{
        id: string;
        name: string;
        estimatedHours?: number;  // ❌ REMOVE
      }>;
    }>;
  }>;
}

// ✅ NEW Interface
interface ProjectWorkflowResponse {
  id: string;
  name: string;
  stages: Array<{
    id: string;
    name: string;
    tasks: Array<{
      id: string;
      name: string;
      estimatedDays?: number;     // ✅ ADD
      steps: Array<{
        id: string;
        name: string;
        estimatedDays?: number;   // ✅ ADD
      }>;
    }>;
  }>;
}
```

### 3. API Endpoint Changes

#### Affected Endpoints:

**Assignment Management:**
- `POST /api/project-step-assignments` - Request body field change
- `GET /api/project-step-assignments/step/{stepId}` - Response field change
- `GET /api/project-step-assignments/crew/{crewId}` - Response field change
- `GET /api/project-step-assignments/contracting-company/{companyId}` - Response field change

**Crew Dashboard:**
- `POST /api/crew/assignments/search` - Response field changes
- `GET /api/crew/assignments` - Response field changes

**Project Workflow:**
- `GET /api/projects/{projectId}/workflow` - Response field changes

### 4. UI Component Changes

#### Form Updates
```typescript
// ❌ OLD Form Field
<FormField
  label="Estimated Hours"
  name="estimatedHours"
  type="number"
  min={1}
  max={240}
  placeholder="Enter estimated hours (e.g., 16)"
/>

// ✅ NEW Form Field
<FormField
  label="Estimated Days"
  name="estimatedDays"
  type="number"
  min={1}
  max={30}
  placeholder="Enter estimated days (e.g., 2)"
/>
```

#### Display Components
```typescript
// ❌ OLD Display
const formatDuration = (hours: number): string => {
  const days = Math.ceil(hours / 8);
  return `${hours} hours (${days} days)`;
};

// ✅ NEW Display
const formatDuration = (days: number): string => {
  return `${days} ${days === 1 ? 'day' : 'days'}`;
};
```

#### Calendar/Timeline Components
```typescript
// ❌ OLD Calendar Logic
const calculateEndDate = (startDate: Date, estimatedHours: number): Date => {
  const days = Math.ceil(estimatedHours / 8); // Convert hours to days
  return addBusinessDays(startDate, days);
};

// ✅ NEW Calendar Logic  
const calculateEndDate = (startDate: Date, estimatedDays: number): Date => {
  return addBusinessDays(startDate, estimatedDays); // Direct day calculation
};
```

### 5. Validation Updates

```typescript
// ❌ OLD Validation
const assignmentValidation = {
  estimatedHours: {
    min: 1,
    max: 240, // ~30 days * 8 hours
    message: "Hours must be between 1 and 240"
  }
};

// ✅ NEW Validation
const assignmentValidation = {
  estimatedDays: {
    min: 1,
    max: 30, // 30 business days
    message: "Days must be between 1 and 30"
  }
};
```

### 6. Search/Filter Updates

```typescript
// ❌ OLD Filter Options
const durationFilters = [
  { label: "Short (1-8 hours)", min: 1, max: 8 },
  { label: "Medium (9-24 hours)", min: 9, max: 24 },
  { label: "Long (25+ hours)", min: 25, max: 999 }
];

// ✅ NEW Filter Options
const durationFilters = [
  { label: "Short (1 day)", min: 1, max: 1 },
  { label: "Medium (2-5 days)", min: 2, max: 5 },
  { label: "Long (6+ days)", min: 6, max: 30 }
];
```

---

## 📊 Database Changes Applied

### Tables Updated:
1. `standard_workflow_tasks` - `estimated_hours` → `estimated_days`
2. `workflow_tasks` - `estimated_hours` → `estimated_days`
3. `project_tasks` - `estimated_hours` → `estimated_days`
4. `standard_workflow_steps` - `estimated_hours` → `estimated_days`
5. `workflow_steps` - `estimated_hours` → `estimated_days`
6. `project_steps` - `estimated_hours` → `estimated_days`
7. `project_step_assignments` - `estimated_hours` → `estimated_days`

### Migration Script: `V23__Replace_estimated_hours_with_estimated_days.sql`
- Renames all `estimated_hours` columns to `estimated_days`
- Adds descriptive comments to columns
- Includes optional data conversion logic (commented out)

---

## ✅ Testing Checklist

### Backend Testing:
- [x] All entities compile without errors
- [x] Database migration runs successfully
- [x] Assignment creation accepts `estimatedDays`
- [x] Workflow copying propagates `estimatedDays` correctly
- [x] Critical path calculator uses day-based calculations

### Frontend Testing Required:
- [ ] Assignment creation form uses `estimatedDays`
- [ ] Crew dashboard displays correct day values
- [ ] Project workflow timeline shows days correctly
- [ ] Calendar views reflect day-based scheduling
- [ ] Search/filter functionality works with day ranges
- [ ] Form validation accepts appropriate day ranges (1-30)
- [ ] Display components show "X days" instead of "X hours"
- [ ] Timeline calculations work with days directly

### Integration Testing:
- [ ] End-to-end assignment creation workflow
- [ ] Crew member assignment acceptance
- [ ] Project workflow visualization
- [ ] Dependency management with day-based calculations

---

## 🚀 Deployment Notes

### Deployment Order:
1. **Database Migration**: Run `V23__Replace_estimated_hours_with_estimated_days.sql`
2. **Backend Deployment**: Deploy updated backend with `estimatedDays` support
3. **Frontend Deployment**: Deploy updated frontend with new interfaces

### Data Considerations:
- No data conversion is performed by default
- If existing hours data needs conversion: uncomment conversion logic in migration
- Conversion formula: `estimated_days = CEIL(estimated_hours / 8.0)`

### Rollback Plan:
- Database: Create reverse migration to rename columns back
- Backend: Revert to previous version
- Frontend: Revert to previous interfaces

---

## 📞 Support

### Key Changes Summary:
- **Field Name**: `estimatedHours` → `estimatedDays`
- **Data Type**: Still `Integer`, but represents days instead of hours
- **Validation Range**: 1-240 hours → 1-30 days
- **Display**: Direct day display instead of hour-to-day conversion
- **Calculations**: All timeline/calendar logic simplified to work with days

### Questions/Issues:
Contact the backend team for:
- API endpoint questions
- Database migration issues
- Business logic clarifications

---

*Generated: $(date)
Backend Migration Version: V23*
