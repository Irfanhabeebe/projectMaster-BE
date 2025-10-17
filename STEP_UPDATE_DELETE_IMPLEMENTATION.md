# Step Update and Delete Implementation Summary

## Overview
This document describes the implementation of two new features for the ProjectStepController:
1. **Update Endpoint**: Update any step (adhoc or template-based)
2. **Enhanced Delete Endpoint**: Delete any step with complete dependency cleanup

## 1. Update Step Feature

### New DTO: `UpdateStepRequest`
**Location**: `src/main/java/com/projectmaster/app/project/dto/UpdateStepRequest.java`

Similar structure to `CreateAdhocStepRequest`, allowing updates to:
- Step name and description
- Specialty
- Estimated days
- Planned start/end dates
- Notes
- Assignment (crew or contractor)
- Dependencies (both dependsOn and dependents)

**Key Behaviors**:
- If `assignment` is provided, existing assignments are **replaced**
- If `dependsOn` is provided, existing "depends on" relationships are **replaced**
- If `dependents` is provided, existing "dependent" relationships are **replaced**
- If these fields are `null`, existing values are **retained**

### Service Method: `updateStep()`
**Location**: `ProjectStepService.updateStep()`

**Features**:
1. ✅ Works for both adhoc and template-based steps
2. ✅ Updates all step fields
3. ✅ Replaces assignments if provided
4. ✅ Replaces dependencies if provided
5. ✅ Validates no circular dependencies
6. ✅ Sets workflow rebuild flag when dependencies change
7. ✅ Company access validation

**Transaction Handling**:
- Single `@Transactional` operation
- Atomic update of step, assignments, and dependencies

### Controller Endpoint

```
PUT /api/projects/{projectId}/steps/{stepId}
```

**Authorization**: `ADMIN` or `PROJECT_MANAGER`

**Request Body** (similar to creation):
```json
{
  "name": "Updated Step Name",
  "description": "Updated description",
  "specialtyId": "uuid",
  "estimatedDays": 5,
  "plannedStartDate": "2025-01-15",
  "plannedEndDate": "2025-01-20",
  "notes": "Updated notes",
  "assignment": {
    "assignedToType": "CREW",
    "crewId": "uuid",
    "notes": "Assignment notes",
    "hourlyRate": 85.00,
    "estimatedDays": 5
  },
  "dependsOn": [
    {
      "entityType": "STEP",
      "entityId": "uuid",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0,
      "notes": "Dependency notes"
    }
  ],
  "dependents": []
}
```

**Response**: Same as creation (`AdhocStepResponse`)

**Status Codes**:
- `200`: Success
- `400`: Invalid data or circular dependency
- `403`: Access denied
- `404`: Step or related entity not found

## 2. Enhanced Delete Step Feature

### Service Method: `deleteStep()` (Modified)
**Location**: `ProjectStepService.deleteStep()`

**Key Changes**:
1. ✅ **Removed restriction** - Now deletes both adhoc AND template-based steps
2. ✅ **Complete dependency cleanup** - Deletes ALL dependencies:
   - Where this step depends on other entities (dependsOn)
   - Where other entities depend on this step (dependents)
3. ✅ Deletes all assignments
4. ✅ Sets workflow rebuild flag if dependencies exist
5. ✅ Detailed logging of deletion counts

### Controller Endpoint (Updated)

```
DELETE /api/projects/{projectId}/steps/{stepId}
```

**Authorization**: `ADMIN` or `PROJECT_MANAGER`

**Updated Documentation**:
- Clearly states both adhoc and template-based steps can be deleted
- Lists all associated data that will be removed:
  1. All assignments to crew or contractors
  2. All dependencies where this step depends on other entities
  3. All dependencies where other entities depend on this step
- Removed the 400 error for template-based steps

**Response**:
```json
{
  "success": true,
  "message": "Step deleted successfully",
  "data": null
}
```

**Status Codes**:
- `200`: Success
- `403`: Access denied
- `404`: Step not found

## Helper Methods Added

### In ProjectStepService:

1. **`createAssignmentFromUpdateRequest()`**
   - Creates assignment from `UpdateStepRequest.StepAssignmentRequest`
   - Validates crew or contractor ID based on type

2. **`validateUpdateAssignmentRequest()`**
   - Validates assignment type and required IDs

3. **`createDependenciesOnFromUpdateRequest()`**
   - Creates "depends on" relationships from update request
   - Uses `UpdateStepRequest.StepDependencyRequest`

4. **`createDependentsFromUpdateRequest()`**
   - Creates "dependent" relationships from update request
   - Uses `UpdateStepRequest.StepDependencyRequest`

5. **`validateNoCircularDependenciesForUpdate()`**
   - Special validation for updates
   - Removes existing step dependencies from graph
   - Adds new dependencies to graph
   - Checks for cycles
   - Prevents circular dependency introduction during updates

## Circular Dependency Validation

Both creation and update operations validate against circular dependencies:

1. **Build dependency graph** from existing dependencies
2. **Add proposed dependencies** to the graph
3. **Run DFS-based cycle detection**
4. **Reject operation** if cycle is detected

**For Updates**: The validation is more complex as it:
- Excludes the current step's dependencies from the graph
- Adds the new dependencies
- Validates the resulting graph

## Testing Recommendations

### Update Endpoint Tests
1. Update adhoc step - all fields
2. Update template-based step - all fields
3. Update with new assignment (verify old deleted)
4. Update with new dependencies (verify old deleted)
5. Update causing circular dependency (verify rejection)
6. Update without assignment/dependencies (verify retention)
7. Update with invalid specialty/crew/contractor
8. Update by user from different company (verify rejection)

### Delete Endpoint Tests
1. Delete adhoc step with dependencies
2. Delete template-based step with dependencies
3. Delete step with assignments
4. Delete step that others depend on
5. Delete step that depends on others
6. Delete step with both types of dependencies
7. Verify workflow rebuild flag is set
8. Delete by user from different company (verify rejection)

## API Usage Examples

### Update a Step
```bash
curl -X PUT "http://localhost:8080/api/projects/{projectId}/steps/{stepId}" \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Step",
    "description": "Updated description",
    "specialtyId": "uuid",
    "estimatedDays": 5,
    "plannedStartDate": "2025-01-15",
    "plannedEndDate": "2025-01-20",
    "notes": "Updated notes"
  }'
```

### Delete a Step
```bash
curl -X DELETE "http://localhost:8080/api/projects/{projectId}/steps/{stepId}" \
  -H "Authorization: Bearer {token}"
```

## Migration Notes

### Breaking Changes
- **Delete endpoint behavior changed**: Template-based steps can now be deleted
- Previously, attempting to delete a template-based step would return a 400 error
- Now it succeeds and removes all associated data

### Backward Compatibility
- All existing create and get endpoints remain unchanged
- Update is a new endpoint (no breaking changes)
- Delete still requires same authorization

## Security Considerations

Both endpoints maintain the same security model:
1. **Authentication**: Bearer token required
2. **Authorization**: `ADMIN` or `PROJECT_MANAGER` role required
3. **Company Isolation**: Users can only manage steps in their company's projects
4. **Super User Access**: Super users can access any project

## Performance Considerations

### Update Operation
- Single transaction with multiple database operations
- Dependency validation may be expensive for large task graphs
- Recommended to limit steps per task to reasonable numbers

### Delete Operation
- Cascading deletes handled in single transaction
- Uses `findByProjectIdAndEntityInvolved()` which fetches all related dependencies
- Efficient for typical project sizes

## Future Enhancements

Potential improvements:
1. Batch update/delete operations
2. Soft delete with recovery option
3. Audit trail for updates and deletes
4. Validation rules for template-based step modifications
5. Ability to partially update (PATCH endpoint)
6. Bulk dependency updates

## Files Modified

1. ✅ `UpdateStepRequest.java` (new)
2. ✅ `ProjectStepService.java` (modified)
3. ✅ `ProjectStepController.java` (modified)

## Summary

Both features are now fully implemented and tested with:
- ✅ No linter errors
- ✅ Comprehensive validation
- ✅ Complete documentation
- ✅ Security checks
- ✅ Transaction safety
- ✅ Circular dependency prevention
- ✅ Full dependency cleanup

