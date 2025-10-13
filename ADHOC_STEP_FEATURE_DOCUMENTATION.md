# Adhoc Step Feature Documentation

## Overview

The Adhoc Step feature allows project managers and admin users to manually add custom steps to a project that are not part of the original workflow template. This provides flexibility to handle unique requirements, unexpected situations, or client-specific requests during project execution.

## Key Features

### 1. Manual Step Creation
- Project managers and admins can create custom steps without being constrained by workflow templates
- Steps can be added to any project task at any time
- Each adhoc step has a flag (`adhocStepFlag`) to distinguish it from template-based steps

### 2. Workflow Template Independence
- Adhoc steps have a nullable `workflowStep` reference (set to null for adhoc steps)
- They are not bound to workflow templates, allowing complete flexibility
- They follow the same data model as regular steps for consistency

### 3. Specialty Assignment
- When creating an adhoc step, the user must specify the required specialty
- This ensures proper skill matching when assigning crew or contractors

### 4. Crew/Contractor Assignments
- Assignment can be created simultaneously with the step
- Supports both internal crew and external contractors
- **Only one active assignment per step** (though multiple historical assignments may exist)
- Assignment details include:
  - Assignment type (CREW or CONTRACTING_COMPANY)
  - Hourly rate (optional)
  - Estimated days (optional)
  - Notes (optional)

### 5. Dependency Management
- Adhoc steps can have dependencies on other steps, tasks, stages, or adhoc tasks
- Two types of dependency relationships:
  - **Depends On**: Other entities that must be completed before this step can start
  - **Dependents**: Other entities that depend on this step being completed
- Dependency types supported:
  - FINISH_TO_START (default)
  - START_TO_START
  - FINISH_TO_FINISH
  - START_TO_FINISH
- Optional lag days can be specified for each dependency

### 6. Schedule Rebuild Notification
- When dependencies are added or modified, the `workflowRebuildRequired` flag is set on the project
- This signals that the project schedule needs to be recalculated
- The flag helps maintain schedule accuracy when adhoc changes are made

## Database Schema Changes

### ProjectStep Entity Changes
```sql
-- Make workflow_step_id nullable to support adhoc steps
ALTER TABLE project_steps 
    ALTER COLUMN workflow_step_id DROP NOT NULL;

-- Add adhoc_step_flag to track manually created steps
ALTER TABLE project_steps 
    ADD COLUMN adhoc_step_flag BOOLEAN NOT NULL DEFAULT false;
```

### Project Entity Changes
```sql
-- Add workflow_rebuild_required flag
ALTER TABLE projects 
    ADD COLUMN workflow_rebuild_required BOOLEAN NOT NULL DEFAULT false;
```

## API Endpoints

All step operations are handled through the unified `ProjectStepController`, which manages both adhoc and template-based steps.

### 1. Create Adhoc Step
**POST** `/api/projects/{projectId}/tasks/{projectTaskId}/steps`

**Authorization**: ADMIN or PROJECT_MANAGER

**Request Body**:
```json
{
  "name": "Install Custom Feature",
  "description": "Install custom outdoor feature as per client specification",
  "specialtyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "estimatedDays": 3,
  "plannedStartDate": "2025-10-15",
  "plannedEndDate": "2025-10-18",
  "notes": "Client provided custom specifications",
  "assignment": {
    "assignedToType": "CREW",
    "crewId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "hourlyRate": 75.50,
    "estimatedDays": 3,
    "notes": "Experienced with custom installations"
  },
  "dependsOn": [
    {
      "entityType": "STEP",
      "entityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0,
      "notes": "Foundation must be complete"
    }
  ],
  "dependents": [
    {
      "entityType": "STEP",
      "entityId": "3fa85f64-5717-4562-b3fc-2c963f66afa7",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 1,
      "notes": "Allow one day for curing"
    }
  ]
}
```

**Response**: `201 Created`
```json
{
  "success": true,
  "message": "Adhoc step created successfully",
  "data": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa8",
    "name": "Install Custom Feature",
    "description": "Install custom outdoor feature as per client specification",
    "projectTaskId": "3fa85f64-5717-4562-b3fc-2c963f66afa5",
    "specialtyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "specialtyName": "Carpentry",
    "status": "NOT_STARTED",
    "estimatedDays": 3,
    "plannedStartDate": "2025-10-15",
    "plannedEndDate": "2025-10-18",
    "adhocStepFlag": true,
    "assignments": [...],
    "dependsOn": [...],
    "dependents": [...],
    "createdAt": "2025-10-11T10:00:00Z",
    "updatedAt": "2025-10-11T10:00:00Z"
  }
}
```

### 2. Get Step by ID
**GET** `/api/projects/{projectId}/steps/{stepId}`

**Authorization**: ADMIN, PROJECT_MANAGER, or TRADIE

**Response**: `200 OK` - Returns the step (adhoc or template-based) with assignments and dependencies

### 3. Get All Steps for a Project Task
**GET** `/api/projects/{projectId}/tasks/{projectTaskId}/steps`

**Authorization**: ADMIN, PROJECT_MANAGER, or TRADIE

**Response**: `200 OK` - Returns list of all steps (both adhoc and template-based) for the specified task

### 4. Get All Adhoc Steps for a Project
**GET** `/api/projects/{projectId}/steps/adhoc`

**Authorization**: ADMIN, PROJECT_MANAGER, or TRADIE

**Response**: `200 OK` - Returns list of all adhoc steps (manually added) across all tasks in the project

### 5. Delete Step
**DELETE** `/api/projects/{projectId}/steps/{stepId}`

**Authorization**: ADMIN or PROJECT_MANAGER

**Response**: `200 OK`
```json
{
  "success": true,
  "message": "Adhoc step deleted successfully"
}
```

**Notes**:
- Only adhoc steps can be deleted (steps with `adhocStepFlag = true`)
- Template-based steps cannot be deleted (will return 400 error)
- Deletes all associated assignments
- Deletes all associated dependencies
- Sets `workflowRebuildRequired` flag if dependencies existed

## Business Logic

### Step Creation Process
1. Validate project task exists
2. Validate specialty exists
3. Create the adhoc step with:
   - `adhocStepFlag` set to `true`
   - `workflowStep` set to `null`
   - `status` set to `NOT_STARTED`
   - Auto-incremented `orderIndex`
4. Create assignments if provided
5. Create dependencies if provided
6. Set `workflowRebuildRequired` flag on project if dependencies were created

### Assignment Creation
- Validates assignment type matches provided entity (crew or contractor)
- Creates assignment with `PENDING` status
- Associates assignment with the step
- Sets assignment date to current timestamp

### Dependency Creation
- Creates bidirectional dependencies based on request
- Sets dependency status to `PENDING`
- Supports all dependency types (FINISH_TO_START, etc.)
- Allows lag days to be specified

### Schedule Rebuild Flag
The `workflowRebuildRequired` flag is set to `true` when:
- Dependencies are added to an adhoc step
- Dependencies are removed (during deletion)
- Dependencies are modified

This flag indicates that the project schedule calculation needs to be re-run to account for the new dependencies.

## Validation Rules

### Required Fields
- `projectTaskId` - Must be a valid project task
- `name` - Cannot be blank
- `specialtyId` - Must be a valid specialty

### Optional Fields
- `description`
- `estimatedDays`
- `plannedStartDate`
- `plannedEndDate`
- `notes`
- `assignments` - Array of assignments (can be empty)
- `dependsOn` - Array of dependencies (can be empty)
- `dependents` - Array of dependent entities (can be empty)

### Assignment Validation
- If `assignedToType` is `CREW`, `crewId` must be provided
- If `assignedToType` is `CONTRACTING_COMPANY`, `contractingCompanyId` must be provided
- Assignment entity IDs must reference valid entities

### Dependency Validation
- Dependency entity IDs must reference valid entities
- Cannot create circular dependencies (enforced at database level)

## Excluded Attributes

The following attributes from the ProjectStep entity are **NOT** included in the adhoc step DTO and are not user-configurable:

### Excluded from Input
- `order_index` - Auto-calculated based on existing steps
- `required_skills` - Legacy field not used for adhoc steps
- `requirements` - Legacy field not used for adhoc steps

### Backend-Set Only (Not from UI)
- `status` - Set to `NOT_STARTED` on creation, updated through workflow
- `actual_start_date` - Set when step execution begins
- `actual_end_date` - Set when step is completed

## Use Cases

### 1. Client Change Request
A client requests a custom feature mid-project:
1. Project manager creates adhoc step "Install Custom Feature"
2. Assigns specialty (e.g., Carpentry)
3. Assigns available crew member
4. Creates dependency on foundation step
5. System sets workflow rebuild flag
6. Schedule is recalculated to account for new step

### 2. Unexpected Repair Work
Damage discovered during construction:
1. Project manager creates adhoc step "Repair Water Damage"
2. Assigns specialty (e.g., Plumbing)
3. Assigns contractor with plumbing expertise
4. Creates dependencies to ensure repair happens before continuing
5. Other steps are rescheduled automatically

### 3. Inspection Requirements
Additional inspection required by local council:
1. Project manager creates adhoc step "Additional Structural Inspection"
2. Assigns specialty (e.g., Building Inspection)
3. Creates dependencies on structural work completion
4. Adds subsequent steps as dependents (cannot proceed until inspection passes)

## Error Handling

### Common Error Responses

**404 Not Found**
- Project task not found
- Specialty not found
- Crew or contractor not found (for assignments)
- Dependency entity not found

**400 Bad Request**
- Missing required fields
- Invalid assignment type/entity combination
- Attempting to delete non-adhoc step
- Invalid dependency configuration

**403 Forbidden**
- User does not have ADMIN or PROJECT_MANAGER role

## Integration Points

### With Workflow Engine
- Adhoc steps participate in the workflow execution like template-based steps
- Status transitions follow the same rules
- Dependencies are respected during execution

### With Scheduling System
- Adhoc steps are included in schedule calculations
- Dependencies affect start dates
- `workflowRebuildRequired` flag triggers recalculation

### With Assignment System
- Uses existing assignment infrastructure
- Crew and contractor notifications work the same way
- Assignment acceptance/rejection follows existing flows

## Testing Recommendations

### Unit Tests
- Test step creation with various combinations of assignments and dependencies
- Test validation rules
- Test deletion with dependency cleanup
- Test workflow rebuild flag setting

### Integration Tests
- Test complete flow from creation to assignment to execution
- Test dependency resolution with adhoc steps
- Test schedule recalculation trigger
- Test permission enforcement

### End-to-End Tests
- Create project with template
- Add adhoc steps
- Assign crew/contractors
- Execute workflow including adhoc steps
- Verify scheduling and dependencies

## Migration Guide

### For Existing Projects
1. Run migration V37__Add_adhoc_step_support.sql
2. Existing steps will have `adhocStepFlag = false`
3. No data migration needed as all existing steps reference workflow templates
4. `workflowRebuildRequired` starts as `false` for all projects

### For Existing Code
- No changes required to existing workflow processing code
- Adhoc steps are handled transparently in workflow execution
- Existing queries work unchanged (adhoc steps are just ProjectStep entities)

## Future Enhancements

### Potential Improvements
1. **Template Creation from Adhoc Steps**
   - Allow converting successful adhoc steps into reusable templates
   
2. **Adhoc Step Analytics**
   - Track common adhoc step patterns
   - Suggest template improvements based on frequently added adhoc steps

3. **Bulk Operations**
   - Create multiple adhoc steps at once
   - Copy adhoc steps between projects

4. **Advanced Scheduling**
   - Auto-suggest optimal placement for adhoc steps
   - Minimize schedule impact when inserting adhoc steps

5. **Cost Tracking**
   - Track additional costs from adhoc steps
   - Compare planned vs actual budget impact

## Support and Troubleshooting

### Common Issues

**Issue**: Adhoc step not appearing in workflow
- **Solution**: Ensure `adhocStepFlag` is set to `true` and step belongs to correct task

**Issue**: Dependencies not being respected
- **Solution**: Check that `workflowRebuildRequired` flag was set and schedule was recalculated

**Issue**: Cannot assign crew/contractor
- **Solution**: Verify entity IDs are valid and entities are active

**Issue**: Cannot delete step
- **Solution**: Ensure step has `adhocStepFlag = true`. Use standard step update APIs for template-based steps.

## Conclusion

The Adhoc Step feature provides essential flexibility for handling real-world construction project variations while maintaining the structure and scheduling benefits of the workflow template system. It seamlessly integrates with existing project management, scheduling, and assignment features.

