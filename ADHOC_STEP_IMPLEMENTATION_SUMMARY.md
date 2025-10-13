# Adhoc Step Implementation Summary

## Overview
This document summarizes the implementation of the Adhoc Step feature, which allows project managers and admin users to manually add custom steps to projects that are not part of the workflow template.

## Changes Made

### 1. Entity Changes

#### ProjectStep.java
**Location**: `/src/main/java/com/projectmaster/app/project/entity/ProjectStep.java`

**Changes**:
- Made `workflowStep` field nullable (removed `nullable = false` constraint)
- Added `adhocStepFlag` boolean field with default value `false`
- Removed unused import `com.projectmaster.app.common.enums.StageStatus`

**Justification**:
- Nullable `workflowStep` allows steps to exist without a template reference
- `adhocStepFlag` distinguishes manually created steps from template-based steps

#### Project.java
**Location**: `/src/main/java/com/projectmaster/app/project/entity/Project.java`

**Changes**:
- Added `workflowRebuildRequired` boolean field with default value `false`

**Justification**:
- Signals when project schedule needs recalculation due to adhoc changes
- Helps maintain schedule accuracy when dependencies are modified

### 2. Database Migration

#### V37__Add_adhoc_step_support.sql
**Location**: `/src/main/resources/db/migration/V37__Add_adhoc_step_support.sql`

**Changes**:
- Altered `project_steps` table to make `workflow_step_id` nullable
- Added `adhoc_step_flag` column to `project_steps` table
- Added `workflow_rebuild_required` column to `projects` table
- Created indexes for efficient querying:
  - `idx_project_steps_adhoc_flag` on `project_steps(adhoc_step_flag)`
  - `idx_projects_rebuild_required` on `projects(workflow_rebuild_required)`
- Added column comments for documentation

### 3. DTOs Created

#### CreateAdhocStepRequest.java
**Location**: `/src/main/java/com/projectmaster/app/project/dto/CreateAdhocStepRequest.java`

**Purpose**: Request DTO for creating adhoc steps

**Key Fields**:
- Basic step info: name, description, specialtyId, estimatedDays
- Planning dates: plannedStartDate, plannedEndDate
- Assignment: Single crew/contractor assignment (only one active assignment per step)
- Dependencies: Lists for dependsOn and dependents
- Validation annotations for required fields

**Nested DTOs**:
- `StepAssignmentRequest`: For crew/contractor assignments
- `StepDependencyRequest`: For dependency relationships

**Excluded Fields** (as per requirements):
- `order_index` - Auto-calculated
- `required_skills` - Legacy, not needed
- `requirements` - Legacy, not needed
- `status` - Backend-managed
- `actual_start_date` - Backend-managed
- `actual_end_date` - Backend-managed

#### AdhocStepResponse.java
**Location**: `/src/main/java/com/projectmaster/app/project/dto/AdhocStepResponse.java`

**Purpose**: Response DTO containing complete adhoc step information

**Key Features**:
- Complete step details including status and dates
- List of assignments with basic info
- List of dependencies (both depends on and dependents)
- Timestamps (createdAt, updatedAt)

**Nested DTOs**:
- `AssignmentInfo`: Assignment summary
- `DependencyInfo`: Dependency relationship details

### 4. Service Layer

#### ProjectStepService.java
**Location**: `/src/main/java/com/projectmaster/app/project/service/ProjectStepService.java`

**Purpose**: Unified business logic for all step management (both adhoc and template-based)

**Key Methods**:
1. `createAdhocStep()` - Creates adhoc step with assignments and dependencies
2. `getStep()` - Retrieves single step by ID (works for all steps)
3. `getStepsByProjectTask()` - Lists all steps for a task
4. `getAdhocStepsByProject()` - Lists only adhoc steps in a project
5. `deleteStep()` - Deletes step (only adhoc steps allowed)

**Key Features**:
- Validates all required entities (task, specialty, crew, contractors)
- Auto-calculates `orderIndex` based on existing steps
- Creates assignments with PENDING status
- Creates bidirectional dependencies
- Sets `workflowRebuildRequired` flag when dependencies are added
- Comprehensive error handling with specific exceptions
- Transaction management for data consistency
- Works with both adhoc and template-based steps

**Dependencies Injected**:
- ProjectStepRepository
- ProjectTaskRepository
- SpecialtyRepository
- ProjectStepAssignmentRepository
- ProjectDependencyRepository
- CrewRepository
- ContractingCompanyRepository
- ProjectRepository

### 5. Controller Layer

#### ProjectStepController.java
**Location**: `/src/main/java/com/projectmaster/app/project/controller/ProjectStepController.java`

**Purpose**: REST API endpoints for unified step management (both adhoc and template-based)

**Endpoints**:

1. **POST** `/api/projects/{projectId}/tasks/{projectTaskId}/steps`
   - Create adhoc step
   - Authorization: ADMIN, PROJECT_MANAGER
   - Returns: 201 Created with AdhocStepResponse

2. **GET** `/api/projects/{projectId}/steps/{stepId}`
   - Get single step (adhoc or template-based)
   - Authorization: ADMIN, PROJECT_MANAGER, TRADIE
   - Returns: 200 OK with AdhocStepResponse

3. **GET** `/api/projects/{projectId}/tasks/{projectTaskId}/steps`
   - Get all steps for task (both adhoc and template-based)
   - Authorization: ADMIN, PROJECT_MANAGER, TRADIE
   - Returns: 200 OK with List<AdhocStepResponse>

4. **GET** `/api/projects/{projectId}/steps/adhoc`
   - Get all adhoc steps for project
   - Authorization: ADMIN, PROJECT_MANAGER, TRADIE
   - Returns: 200 OK with List<AdhocStepResponse>

5. **DELETE** `/api/projects/{projectId}/steps/{stepId}`
   - Delete step (only adhoc steps allowed)
   - Authorization: ADMIN, PROJECT_MANAGER
   - Returns: 200 OK

**Features**:
- Comprehensive Swagger/OpenAPI documentation
- Role-based access control
- Consistent error responses
- User context from authentication
- Unified approach for both adhoc and template-based steps

### 6. Documentation

#### ADHOC_STEP_FEATURE_DOCUMENTATION.md
**Location**: Root directory

**Contents**:
- Feature overview and key capabilities
- Database schema changes
- Complete API documentation with examples
- Business logic explanation
- Validation rules
- Use cases and examples
- Error handling guide
- Integration points
- Testing recommendations
- Migration guide
- Future enhancements
- Troubleshooting guide

## Key Design Decisions

### 1. Unified Controller and Service
**Decision**: Use single `ProjectStepController` and `ProjectStepService` for all steps instead of separate adhoc-specific controllers

**Rationale**:
- Steps are steps, regardless of origin (adhoc or template)
- Reduces code duplication
- Simplifies API for consumers
- Easier to add update functionality later for all steps
- Consistent patterns across the application
- Future updates/deletes can work uniformly

### 2. Nullable Workflow Step Reference
**Decision**: Made `workflowStep` nullable instead of creating a separate table for adhoc steps

**Rationale**:
- Maintains single data model for all steps
- Simplifies queries and workflow processing
- Reduces code duplication
- Adhoc steps participate in workflow naturally

### 3. Adhoc Flag vs Separate Table
**Decision**: Used boolean flag `adhocStepFlag` to distinguish adhoc steps

**Rationale**:
- Simple and performant
- Easy to query (with indexed flag)
- No need for complex joins
- Consistent with existing architecture patterns

### 4. Workflow Rebuild Flag
**Decision**: Added project-level flag instead of automatic recalculation

**Rationale**:
- Allows batching of schedule updates
- More efficient for multiple adhoc changes
- Gives control over when expensive calculations run
- Clear indication that schedule needs attention

### 5. Simultaneous Assignment Creation
**Decision**: Allow assignments to be created with the step

**Rationale**:
- Better user experience (single API call)
- Maintains data consistency
- Follows pattern established by adhoc tasks
- Reduces round trips

### 6. Bidirectional Dependency Support
**Decision**: Support both "depends on" and "dependents" in creation request

**Rationale**:
- Flexible dependency modeling
- Matches how users think about relationships
- Single creation point for complex dependency graphs
- Reduces API calls

### 7. Order Index Auto-Calculation
**Decision**: Auto-calculate orderIndex instead of requiring from user

**Rationale**:
- Prevents ordering conflicts
- Simplifies UI requirements
- Consistent with step creation patterns
- Can be manually adjusted later if needed

## Testing Considerations

### Unit Tests Needed
- [x] Step creation with various field combinations
- [x] Assignment validation
- [x] Dependency creation (both types)
- [x] Order index calculation
- [x] Workflow rebuild flag setting
- [x] Delete with cleanup

### Integration Tests Needed
- [ ] Complete creation flow with database
- [ ] Assignment creation and retrieval
- [ ] Dependency persistence and querying
- [ ] Authorization enforcement
- [ ] Error scenarios

### Manual Testing Checklist
- [ ] Create adhoc step with no assignments/dependencies
- [ ] Create adhoc step with crew assignment
- [ ] Create adhoc step with contractor assignment
- [ ] Create adhoc step with multiple assignments
- [ ] Create adhoc step with depends on relationships
- [ ] Create adhoc step with dependent relationships
- [ ] Verify workflow rebuild flag is set correctly
- [ ] Delete adhoc step and verify cleanup
- [ ] Attempt to delete template-based step (should fail)
- [ ] Verify role-based access control

## Migration Path

### For Existing Data
1. Run migration script V37
2. All existing steps automatically have `adhocStepFlag = false`
3. All existing projects have `workflowRebuildRequired = false`
4. No data cleanup needed

### For Application Code
- No changes required to existing workflow processing
- Adhoc steps are handled transparently
- Existing step queries work unchanged

## Performance Considerations

### Indexes Added
- `project_steps(adhoc_step_flag)` - For filtering adhoc steps
- `projects(workflow_rebuild_required)` - For finding projects needing rebuild

### Query Optimization
- Service methods use indexed queries
- Lazy loading for relationships
- Batch processing where possible

### Transaction Management
- All mutations are transactional
- Rollback on any failure
- Consistency guaranteed

## Security

### Role-Based Access
- Only ADMIN and PROJECT_MANAGER can create/delete adhoc steps
- TRADIE can view adhoc steps
- Enforced at controller level with @PreAuthorize

### Data Validation
- Entity existence validation
- Assignment type validation
- Dependency validation
- Prevention of circular dependencies (database level)

## Backward Compatibility

### Existing Functionality
- All existing step operations work unchanged
- Template-based steps unaffected
- Workflow execution compatible
- Schedule calculation compatible

### API Versioning
- New endpoints added (no existing endpoint changes)
- No breaking changes to existing APIs
- New fields have sensible defaults

## Known Limitations

### Current Implementation
1. Adhoc steps cannot be converted to template steps
2. No bulk creation support
3. Order index is auto-assigned (no manual override)
4. Dependency entity names not fetched (requires additional queries)

### Future Improvements
- Template creation from adhoc steps
- Bulk operations
- Manual order specification
- Enhanced dependency information
- Cost impact analysis

## Deployment Steps

1. **Database Migration**
   ```bash
   # Migration runs automatically on application startup
   # V37__Add_adhoc_step_support.sql
   ```

2. **Application Deployment**
   - No configuration changes needed
   - No environment variables required
   - No external service dependencies

3. **Verification**
   - Check migration completed successfully
   - Verify new columns exist
   - Test API endpoints with authentication
   - Verify role-based access control

4. **Rollback Plan** (if needed)
   - Revert to previous application version
   - Run rollback migration (remove columns)
   - No data loss for template-based steps

## Support Information

### Monitoring
- Log messages at INFO level for creation/deletion
- Error logs for validation failures
- Transaction logs for debugging

### Metrics to Track
- Number of adhoc steps created per project
- Adhoc step success rate
- Common specialties for adhoc steps
- Workflow rebuild frequency

### Common Issues
See ADHOC_STEP_FEATURE_DOCUMENTATION.md "Support and Troubleshooting" section

## Conclusion

The Adhoc Step feature has been successfully implemented with a unified approach:
- ✅ Entity changes (ProjectStep, Project)
- ✅ Database migration
- ✅ Complete DTOs (request/response)
- ✅ Unified service layer (ProjectStepService) for all steps
- ✅ Unified controller (ProjectStepController) for all steps
- ✅ REST API endpoints with security
- ✅ Comprehensive documentation
- ✅ No linter errors
- ✅ Backward compatibility maintained
- ✅ Extensible for future update/delete operations

The implementation uses a unified approach where both adhoc and template-based steps are managed through the same controller and service, following the principle that "a step is a step" regardless of its origin. This design:
- Reduces code duplication
- Simplifies the API
- Makes future enhancements easier
- Provides consistent patterns
- Maintains data consistency
- Offers flexibility for real-world project variations while preserving workflow template benefits

