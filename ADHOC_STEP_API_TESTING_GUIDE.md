# Adhoc Step API Testing Guide

## Prerequisites

### Authentication
All endpoints require JWT authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Required Roles
- **Create/Delete**: ADMIN or PROJECT_MANAGER
- **View**: ADMIN, PROJECT_MANAGER, or TRADIE

### Base URL
```
http://localhost:8080/api
```

## Testing Scenarios

### Scenario 1: Create Simple Adhoc Step

**Endpoint**: `POST /api/projects/{projectId}/tasks/{projectTaskId}/steps`

**Request**:
```json
{
  "name": "Install Custom Deck Feature",
  "description": "Install custom timber deck feature as per client request",
  "specialtyId": "550e8400-e29b-41d4-a716-446655440001",
  "estimatedDays": 2,
  "notes": "Client will provide custom timber"
}
```

**Expected Response**: `201 Created`
```json
{
  "success": true,
  "message": "Adhoc step created successfully",
  "data": {
    "id": "...",
    "name": "Install Custom Deck Feature",
    "adhocStepFlag": true,
    "status": "NOT_STARTED",
    "assignments": [],
    "dependsOn": [],
    "dependents": []
  }
}
```

---

### Scenario 2: Create Adhoc Step with Crew Assignment

**Endpoint**: `POST /api/projects/{projectId}/tasks/{projectTaskId}/steps`

**Request**:
```json
{
  "name": "Emergency Plumbing Repair",
  "description": "Fix unexpected pipe leak discovered during inspection",
  "specialtyId": "550e8400-e29b-41d4-a716-446655440002",
  "estimatedDays": 1,
  "assignment": {
    "assignedToType": "CREW",
    "crewId": "650e8400-e29b-41d4-a716-446655440001",
    "hourlyRate": 85.00,
    "estimatedDays": 1,
    "notes": "Urgent repair required"
  }
}
```

**Expected Response**: `201 Created`
- Includes assignment in response
- Assignment status is PENDING

---

### Scenario 3: Create Adhoc Step with Contractor Assignment

**Endpoint**: `POST /api/projects/{projectId}/tasks/{projectTaskId}/steps`

**Request**:
```json
{
  "name": "Specialized Electrical Installation",
  "description": "Install specialized electrical system for home theater",
  "specialtyId": "550e8400-e29b-41d4-a716-446655440003",
  "estimatedDays": 3,
  "assignment": {
    "assignedToType": "CONTRACTING_COMPANY",
    "contractingCompanyId": "750e8400-e29b-41d4-a716-446655440001",
    "hourlyRate": 120.00,
    "estimatedDays": 3,
    "notes": "Contractor has specific home theater experience"
  }
}
```

**Expected Response**: `201 Created`
- Includes contractor assignment
- Assignment status is PENDING

---

### Scenario 4: Create Adhoc Step with Dependencies

**Endpoint**: `POST /api/projects/{projectId}/tasks/{projectTaskId}/steps`

**Request**:
```json
{
  "name": "Custom Feature Installation",
  "description": "Install custom outdoor feature",
  "specialtyId": "550e8400-e29b-41d4-a716-446655440001",
  "estimatedDays": 3,
  "dependsOn": [
    {
      "entityType": "STEP",
      "entityId": "450e8400-e29b-41d4-a716-446655440001",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0,
      "notes": "Foundation must be complete first"
    }
  ],
  "dependents": [
    {
      "entityType": "STEP",
      "entityId": "450e8400-e29b-41d4-a716-446655440002",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 1,
      "notes": "Allow curing time before proceeding"
    }
  ]
}
```

**Expected Response**: `201 Created`
- Includes dependencies in response
- Project's `workflowRebuildRequired` flag is set to `true`

---

### Scenario 5: Create Complete Adhoc Step (All Features)

**Endpoint**: `POST /api/projects/{projectId}/tasks/{projectTaskId}/steps`

**Request**:
```json
{
  "name": "Custom Kitchen Installation",
  "description": "Install custom kitchen cabinetry and fixtures as per client specifications",
  "specialtyId": "550e8400-e29b-41d4-a716-446655440004",
  "estimatedDays": 5,
  "plannedStartDate": "2025-11-01",
  "plannedEndDate": "2025-11-06",
  "notes": "Client provided custom design. Materials to be delivered by 2025-10-30",
  "assignment": {
    "assignedToType": "CREW",
    "crewId": "650e8400-e29b-41d4-a716-446655440001",
    "hourlyRate": 90.00,
    "estimatedDays": 3,
    "notes": "Lead carpenter"
  },
  "dependsOn": [
    {
      "entityType": "STEP",
      "entityId": "450e8400-e29b-41d4-a716-446655440003",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0,
      "notes": "Plumbing rough-in must be complete"
    },
    {
      "entityType": "STEP",
      "entityId": "450e8400-e29b-41d4-a716-446655440004",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0,
      "notes": "Electrical rough-in must be complete"
    }
  ],
  "dependents": [
    {
      "entityType": "STEP",
      "entityId": "450e8400-e29b-41d4-a716-446655440005",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 2,
      "notes": "Countertop installation requires cabinet completion"
    }
  ]
}
```

**Expected Response**: `201 Created`
- Complete step with all details
- Multiple assignments listed
- All dependencies included
- `workflowRebuildRequired` flag set

---

### Scenario 6: Get Step by ID

**Endpoint**: `GET /api/projects/{projectId}/steps/{stepId}`

**Expected Response**: `200 OK`
```json
{
  "success": true,
  "message": "Adhoc step retrieved successfully",
  "data": {
    "id": "...",
    "name": "...",
    "description": "...",
    "status": "NOT_STARTED",
    "adhocStepFlag": true,
    "assignments": [...],
    "dependsOn": [...],
    "dependents": [...]
  }
}
```

---

### Scenario 7: Get All Steps for Project Task

**Endpoint**: `GET /api/projects/{projectId}/tasks/{projectTaskId}/steps`

**Note**: This returns all steps (both adhoc and template-based) for the task

**Expected Response**: `200 OK`
```json
{
  "success": true,
  "message": "Adhoc steps retrieved successfully",
  "data": [
    {
      "id": "...",
      "name": "...",
      "adhocStepFlag": true,
      ...
    },
    {
      "id": "...",
      "name": "...",
      "adhocStepFlag": true,
      ...
    }
  ]
}
```

---

### Scenario 8: Get All Adhoc Steps for Project

**Endpoint**: `GET /api/projects/{projectId}/steps/adhoc`

**Expected Response**: `200 OK`
- Returns all adhoc steps across all tasks in the project
- Useful for project overview

---

### Scenario 9: Delete Step

**Endpoint**: `DELETE /api/projects/{projectId}/steps/{stepId}`

**Note**: Only adhoc steps can be deleted. Template-based steps will return a 400 error.

**Expected Response**: `200 OK`
```json
{
  "success": true,
  "message": "Adhoc step deleted successfully"
}
```

**Side Effects**:
- All assignments deleted
- All dependencies deleted
- If dependencies existed, `workflowRebuildRequired` flag set to `true`

---

## Error Testing Scenarios

### Test 1: Missing Required Fields

**Request**:
```json
{
  "name": "Test Step"
  // Missing specialtyId
}
```

**Expected Response**: `400 Bad Request`
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "specialtyId": "Specialty is required"
  }
}
```

---

### Test 2: Invalid Project Task ID

**Request**: Valid JSON with non-existent projectTaskId

**Expected Response**: `404 Not Found`
```json
{
  "success": false,
  "message": "ProjectTask not found with id: ..."
}
```

---

### Test 3: Invalid Specialty ID

**Request**: Valid JSON with non-existent specialtyId

**Expected Response**: `404 Not Found`
```json
{
  "success": false,
  "message": "Specialty not found with id: ..."
}
```

---

### Test 4: Invalid Assignment (Crew ID missing when type is CREW)

**Request**:
```json
{
  "name": "Test Step",
  "specialtyId": "550e8400-e29b-41d4-a716-446655440001",
  "assignments": [
    {
      "assignedToType": "CREW"
      // Missing crewId
    }
  ]
}
```

**Expected Response**: `400 Bad Request`
```json
{
  "success": false,
  "message": "Crew ID is required for CREW assignment type"
}
```

---

### Test 5: Attempting to Delete Template-Based Step

**Request**: `DELETE /api/projects/{projectId}/adhoc-steps/{templateStepId}`

**Expected Response**: `400 Bad Request`
```json
{
  "success": false,
  "message": "Cannot delete non-adhoc step"
}
```

---

### Test 6: Unauthorized Access (TRADIE trying to create)

**Request**: POST with TRADIE role

**Expected Response**: `403 Forbidden`
```json
{
  "success": false,
  "message": "Access denied"
}
```

---

## Postman Collection Setup

### Environment Variables
```json
{
  "baseUrl": "http://localhost:8080/api",
  "projectId": "your-project-id",
  "projectTaskId": "your-task-id",
  "specialtyId": "your-specialty-id",
  "crewId": "your-crew-id",
  "contractorId": "your-contractor-id",
  "dependencyStepId": "existing-step-id",
  "token": "your-jwt-token"
}
```

### Pre-request Script (for all requests)
```javascript
// Auto-set Authorization header
pm.request.headers.add({
    key: 'Authorization',
    value: 'Bearer ' + pm.environment.get('token')
});

// Set Content-Type
pm.request.headers.add({
    key: 'Content-Type',
    value: 'application/json'
});
```

### Test Script (for create endpoints)
```javascript
// Store created step ID for later use
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set('adhocStepId', jsonData.data.id);
    pm.test('Step created successfully', function() {
        pm.expect(jsonData.success).to.be.true;
        pm.expect(jsonData.data.adhocStepFlag).to.be.true;
    });
}
```

---

## cURL Examples

### Create Simple Adhoc Step
```bash
curl -X POST "http://localhost:8080/api/projects/{projectId}/tasks/{taskId}/steps" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Custom Installation",
    "specialtyId": "550e8400-e29b-41d4-a716-446655440001",
    "estimatedDays": 3
  }'
```

### Get Step
```bash
curl -X GET "http://localhost:8080/api/projects/{projectId}/steps/{stepId}" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Delete Step (Adhoc Only)
```bash
curl -X DELETE "http://localhost:8080/api/projects/{projectId}/steps/{stepId}" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Database Verification Queries

### Check Adhoc Step Created
```sql
SELECT id, name, adhoc_step_flag, workflow_step_id, specialty_id, status
FROM project_steps
WHERE adhoc_step_flag = true
ORDER BY created_at DESC;
```

### Check Workflow Rebuild Flag
```sql
SELECT id, name, workflow_rebuild_required
FROM projects
WHERE workflow_rebuild_required = true;
```

### Check Assignments Created
```sql
SELECT psa.id, psa.assigned_to_type, psa.status, ps.name as step_name
FROM project_step_assignments psa
JOIN project_steps ps ON psa.project_step_id = ps.id
WHERE ps.adhoc_step_flag = true;
```

### Check Dependencies Created
```sql
SELECT pd.id, pd.dependency_type, pd.status, 
       pd.dependent_entity_type, pd.depends_on_entity_type
FROM project_dependencies pd
WHERE pd.dependent_entity_type = 'STEP' 
  AND pd.dependent_entity_id IN (
    SELECT id FROM project_steps WHERE adhoc_step_flag = true
  );
```

---

## Integration Testing Checklist

- [ ] Create adhoc step without assignments/dependencies
- [ ] Create adhoc step with single crew assignment
- [ ] Create adhoc step with single contractor assignment
- [ ] Create adhoc step with multiple assignments
- [ ] Create adhoc step with "depends on" dependencies
- [ ] Create adhoc step with "dependents" dependencies
- [ ] Create adhoc step with both types of dependencies
- [ ] Create complete adhoc step with all features
- [ ] Retrieve adhoc step by ID
- [ ] List adhoc steps for task
- [ ] List adhoc steps for project
- [ ] Delete adhoc step without dependencies
- [ ] Delete adhoc step with dependencies
- [ ] Verify workflow rebuild flag is set when dependencies exist
- [ ] Test all validation errors
- [ ] Test permission enforcement (roles)
- [ ] Verify database constraints
- [ ] Test transaction rollback on error

---

## Performance Testing

### Load Testing Parameters
- Create 50 adhoc steps concurrently
- Each with 2 assignments and 3 dependencies
- Measure response time and database load
- Verify no deadlocks or constraint violations

### Expected Performance
- Simple creation: < 200ms
- With assignments: < 300ms
- With dependencies: < 400ms
- Bulk retrieval: < 500ms

---

## Monitoring and Logging

### Log Entries to Verify
```
INFO: Creating adhoc step 'Custom Installation' for project task {taskId}
INFO: Adhoc step created with ID: {stepId}
INFO: Created 2 assignments for adhoc step {stepId}
INFO: Created 3 'depends on' dependencies for adhoc step {stepId}
INFO: Set workflow rebuild required flag for project {projectId}
```

### Error Logs to Watch For
```
ERROR: EntityNotFoundException: ProjectTask not found
ERROR: EntityNotFoundException: Specialty not found
ERROR: ProjectMasterException: Crew ID is required for CREW assignment type
ERROR: ProjectMasterException: Cannot delete non-adhoc step
```

---

## Conclusion

This testing guide provides comprehensive scenarios for testing the Adhoc Step API. Use these examples to validate the implementation and ensure all features work as expected.

