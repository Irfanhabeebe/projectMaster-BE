# API Changes Summary - Step Fields Removal

## Overview
This document lists all API endpoints affected by the removal of `orderIndex`, `requiredSkills`, and `requirements` fields from Step entities.

## Fields Removed
- **orderIndex** (Integer) - Explicit ordering of steps
- **requiredSkills** (String/JSON) - Skills required for steps
- **requirements** (String/JSON) - Requirements for steps

---

## 1. PROJECT WORKFLOW ENDPOINT

### GET `/api/projects/{projectId}/workflow`
**Access**: ADMIN, PROJECT_MANAGER, USER

**Changes to Response**:
```json
{
  "stages": [
    {
      "tasks": [
        {
          "steps": [
            {
              "id": "uuid",
              "name": "Step Name",
              "description": "Description",
              "status": "NOT_STARTED",
              
              // ❌ REMOVED FIELDS:
              // "orderIndex": 1,
              // "requiredSkills": "[\"skill1\", \"skill2\"]",
              // "requirements": "{\"key\": \"value\"}",
              
              // ✅ REMAINING FIELDS:
              "estimatedDays": 5,
              "startDate": "2025-01-15",
              "endDate": "2025-01-20",
              "actualStartDate": null,
              "actualEndDate": null,
              "notes": "Step notes",
              "qualityCheckPassed": false,
              "specialty": {
                "id": "uuid",
                "specialtyName": "Electrician",
                "specialtyType": "Trade"
              },
              "assignments": [...],
              "dependencies": [...]
            }
          ]
        }
      ]
    }
  ]
}
```

**UI Impact**: 
- Remove any display of step order index
- Remove skills/requirements display from step cards
- Steps will be displayed in creation order

---

## 2. PROJECT STEP ENDPOINTS

### POST `/api/projects/{projectId}/tasks/{projectTaskId}/steps`
**Access**: ADMIN, PROJECT_MANAGER

**Request Body Changes**:
```json
{
  "name": "Adhoc Step Name",
  "description": "Description",
  "specialtyId": "uuid",
  "estimatedDays": 3,
  "plannedStartDate": "2025-01-15",
  "plannedEndDate": "2025-01-18",
  "notes": "Notes",
  
  // ❌ REMOVED FIELDS - Don't send these:
  // "orderIndex": 1,
  // "requiredSkills": "[\"skill1\"]",
  // "requirements": "{\"key\": \"value\"}",
  
  // Optional dependency fields:
  "dependsOnRequests": [...],
  "dependentRequests": [...]
}
```

**Response Changes**: Same as workflow endpoint above

### GET `/api/projects/{projectId}/steps/{stepId}`
**Access**: ADMIN, PROJECT_MANAGER, TRADIE

**Response Changes**: Same step structure as above (fields removed)

### GET `/api/projects/{projectId}/tasks/{projectTaskId}/steps`
**Access**: ADMIN, PROJECT_MANAGER, TRADIE

**Response Changes**: Array of steps with fields removed (same structure as above)

### GET `/api/projects/{projectId}/steps/adhoc`
**Access**: ADMIN, PROJECT_MANAGER, TRADIE

**Response Changes**: Array of adhoc steps with fields removed

### DELETE `/api/projects/{projectId}/steps/{stepId}`
**Access**: ADMIN, PROJECT_MANAGER

**No Changes**: Delete operations unaffected

---

## 3. WORKFLOW TEMPLATE ENDPOINTS

### GET `/api/workflow/templates/{templateId}`
**Access**: PROJECT_MANAGER, ADMIN, USER

**Response Changes**:
```json
{
  "id": "uuid",
  "name": "Template Name",
  "stages": [
    {
      "tasks": [
        {
          "id": "uuid",
          "name": "Task Name",
          
          // ❌ REMOVED FIELDS from task level too:
          // "orderIndex": 1,
          // "requiredSkills": "[\"skill1\"]",
          // "requirements": "{\"key\": \"value\"}",
          
          // ✅ REMAINING FIELDS:
          "estimatedDays": 5,
          "version": 1,
          
          "steps": [
            {
              "id": "uuid",
              "name": "Step Name",
              
              // ❌ REMOVED FIELDS:
              // "orderIndex": 1,
              // "requiredSkills": "[\"skill1\"]",
              // "requirements": "{\"key\": \"value\"}",
              
              // ✅ REMAINING FIELDS:
              "estimatedDays": 2,
              "specialtyId": "uuid",
              "specialtyName": "Plumber",
              "specialtyType": "Trade",
              "version": 1,
              "stepRequirements": [...],
              "dependencies": [...]
            }
          ]
        }
      ]
    }
  ]
}
```

**UI Impact**:
- Remove order index display from workflow template viewer
- Remove skills/requirements display from template steps and tasks
- Template steps will be displayed in creation order

### GET `/api/workflow/templates`
**Access**: PROJECT_MANAGER, ADMIN, USER

**Response Changes**: List of templates (summary view, no step details affected)

### POST `/api/workflow/templates/{templateId}/clone`
**Access**: PROJECT_MANAGER, ADMIN

**No Changes to Request/Response Structure**: Clone operation handles ordering internally

---

## 4. CREW DASHBOARD ENDPOINT

### GET `/api/crew/dashboard`
**Access**: ADMIN, PROJECT_MANAGER, TRADIE

**Response Changes**:
```json
{
  "assignments": [
    {
      "assignmentId": "uuid",
      "stepId": "uuid",
      "stepName": "Step Name",
      "stepDescription": "Description",
      "stepStatus": "NOT_STARTED",
      
      // ❌ REMOVED FIELDS:
      // "stepOrderIndex": 1,
      // "requiredSkills": "[\"skill1\"]",
      // "requirements": "{\"key\": \"value\"}",
      
      // ✅ REMAINING FIELDS:
      "stepEstimatedDays": 3,
      "stepStartDate": "2025-01-15",
      "stepEndDate": "2025-01-18",
      "stepActualStartDate": null,
      "stepActualEndDate": null,
      "specialtyName": "Electrician",
      "projectId": "uuid",
      "projectName": "Project Name",
      ...
    }
  ]
}
```

**UI Impact**:
- Remove step order index from crew dashboard cards
- Remove skills/requirements display from assignment cards

---

## 5. ASSIGNMENT ENDPOINTS

### GET `/api/project-step-assignments/step/{stepId}`
**Access**: ADMIN, PROJECT_MANAGER, USER

**Response Changes**: Step assignment responses don't directly include step details with removed fields, but step lookups will reflect changes

### POST `/api/project-step-assignments`
**Access**: ADMIN, PROJECT_MANAGER

**No Changes to Request**: Assignment creation unaffected

### GET `/api/project-step-assignments/recommendations/{specialtyId}`
**Access**: ADMIN, PROJECT_MANAGER

**No Changes**: Recommendation response unaffected by step field changes

---

## 6. STEP UPDATE ENDPOINTS

### POST `/api/projects/{projectId}/steps/{stepId}/updates`
**Access**: ADMIN, PROJECT_MANAGER, TRADIE

**No Changes**: Step update operations unaffected

### GET `/api/projects/{projectId}/steps/{stepId}/updates`
**Access**: ADMIN, PROJECT_MANAGER, TRADIE

**No Changes**: Update history unaffected

---

## 7. PROJECT RECALCULATION ENDPOINT (NEW)

### POST `/api/projects/{projectId}/recalculate-schedule`
**Access**: ADMIN, PROJECT_MANAGER

**Description**: Recalculates estimated start and end dates for non-completed stages, tasks, and steps

**Request**: None (path parameter only)

**Response**:
```json
{
  "success": true,
  "message": "Project schedule recalculated successfully",
  "data": "Project schedule recalculated successfully for project: {projectId}"
}
```

**UI Usage**: Call this endpoint when `workflowRebuildRequired` flag is true

---

## SUMMARY OF UI CHANGES REQUIRED

### 1. **Remove Field References**
Delete all references to these fields in your TypeScript interfaces:
```typescript
// ❌ Remove these from Step interface:
interface Step {
  // orderIndex?: number;        // REMOVE
  // requiredSkills?: string;    // REMOVE
  // requirements?: string;      // REMOVE
}

// ❌ Remove these from Task interface (in workflow templates):
interface WorkflowTask {
  // orderIndex?: number;        // REMOVE
  // requiredSkills?: string;    // REMOVE
  // requirements?: string;      // REMOVE
}
```

### 2. **Update Step Display Components**
- **Step Cards**: Remove order index badge/number
- **Step Forms**: Remove order input field
- **Step Details**: Remove skills and requirements sections
- **Step Lists**: Display steps in the order received (now ordered by creation time)

### 3. **Update Workflow Template Components**
- **Template Viewer**: Remove order display from steps and tasks
- **Template Builder**: Remove order/skills/requirements inputs for steps
- **Template Clone**: No changes needed (handled server-side)

### 4. **Update Crew Dashboard**
- **Assignment Cards**: Remove order index and skills display
- **Assignment Filters**: Remove skills-based filtering if implemented

### 5. **Update Forms**
When creating adhoc steps, remove these fields from the form:
- Order Index input
- Required Skills input
- Requirements input

### 6. **Add Schedule Recalculation**
Add a button to trigger schedule recalculation:
```typescript
// When workflowRebuildRequired === true
async function recalculateSchedule(projectId: string) {
  await api.post(`/api/projects/${projectId}/recalculate-schedule`);
  // Refresh project workflow
  await fetchProjectWorkflow(projectId);
}
```

---

## ORDERING BEHAVIOR

### **Old Behavior**:
Steps were ordered by explicit `orderIndex` field.

### **New Behavior**:
Steps are ordered by `createdAt` timestamp:
- Template-based steps: Created in template order during project creation
- Adhoc steps: Appear in the order they were created
- Dependency-based execution order is maintained (unchanged)

**UI Recommendation**: 
- Display steps in the order returned by the API
- No manual reordering UI needed
- Use dependencies to control execution flow

---

## TESTING CHECKLIST

- [ ] Project workflow endpoint displays steps without removed fields
- [ ] Create adhoc step form doesn't send removed fields
- [ ] Workflow template viewer doesn't show removed fields
- [ ] Crew dashboard displays assignments without removed fields
- [ ] Step cards render correctly without order index
- [ ] No TypeScript compilation errors after interface updates
- [ ] Schedule recalculation button works when `workflowRebuildRequired` is true
- [ ] Step ordering appears correct (by creation time)
- [ ] No broken API calls trying to send removed fields

---

## MIGRATION NOTES

1. **Database Migration**: V40 will automatically drop the columns
2. **No Data Loss**: Fields were not actively used, no data migration needed
3. **Backward Compatibility**: None - this is a breaking change
4. **Frontend Must Update**: Old frontends will fail if they rely on these fields

---

## SUPPORT

If you encounter issues:
1. Check browser console for API errors
2. Verify TypeScript interfaces are updated
3. Ensure no forms are sending removed fields
4. Check that step ordering looks correct
5. Review the `ORDER_INDEX_REMOVAL_SUMMARY.md` for technical details

