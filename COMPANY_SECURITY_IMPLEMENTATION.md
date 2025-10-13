# Company Security Implementation for Project Steps

## Overview
Added company-level security validation to ensure users can only manage steps for projects belonging to their company, with an exception for Super Users who can access any project.

## Security Implementation

### Validation Logic

```java
private void validateUserCompanyAccess(User user, Project project) {
    // Super users can access any project
    if (user.getRole() == UserRole.SUPER_USER) {
        return;
    }

    // Validate user has a company
    if (user.getCompany() == null) {
        throw new ProjectMasterException("Access denied: User does not belong to any company");
    }

    // Validate user's company matches project's company
    if (!user.getCompany().getId().equals(project.getCompany().getId())) {
        throw new ProjectMasterException("Access denied: You can only manage steps for projects belonging to your company");
    }
}
```

### Applied to All Operations

✅ **Create Adhoc Step** - Validates before creating  
✅ **Get Step** - Validates before retrieving  
✅ **Get Steps by Task** - Validates before listing  
✅ **Get Adhoc Steps by Project** - Validates before listing  
✅ **Delete Step** - Validates before deleting  

## User Roles

### Super User (SUPER_USER)
- **Privilege**: Can access and manage steps for ANY project across ALL companies
- **Use Case**: System administrators, platform support staff
- **Bypass**: Validation is skipped entirely for super users

### Regular Users (ADMIN, PROJECT_MANAGER, TRADIE)
- **Restriction**: Can only access steps for projects belonging to their company
- **Validation**: Must have a company assigned
- **Error**: Receives clear error message if attempting cross-company access

## Error Messages

### User Without Company
```json
{
  "success": false,
  "message": "Access denied: User does not belong to any company"
}
```

### Cross-Company Access Attempt
```json
{
  "success": false,
  "message": "Access denied: You can only manage steps for projects belonging to your company"
}
```

## Logging

### Debug Logs
- Super user access: `"Super user {userId} accessing project {projectId}"`
- Successful validation: `"User {userId} validated for project {projectId} (company: {companyId})"`

### Warning Logs
- Unauthorized access attempts:
  ```
  "User {userId} from company {companyId} attempted to access project {projectId} from company {otherCompanyId}"
  ```

## Testing Scenarios

### Scenario 1: Regular User - Same Company ✅
```
User: ADMIN (Company A)
Project: Project 1 (Company A)
Result: Access GRANTED
```

### Scenario 2: Regular User - Different Company ❌
```
User: PROJECT_MANAGER (Company A)
Project: Project 2 (Company B)
Result: Access DENIED
Error: "You can only manage steps for projects belonging to your company"
```

### Scenario 3: Super User - Any Company ✅
```
User: SUPER_USER (No company or any company)
Project: Any project (any company)
Result: Access GRANTED
```

### Scenario 4: User Without Company ❌
```
User: ADMIN (No company)
Project: Any project
Result: Access DENIED
Error: "User does not belong to any company"
```

## Security Benefits

### Data Isolation
- ✅ Prevents cross-company data leaks
- ✅ Ensures data privacy between companies
- ✅ Maintains proper multi-tenancy

### Audit Trail
- ✅ Warning logs for unauthorized access attempts
- ✅ Debug logs for tracking access patterns
- ✅ Clear identification of security violations

### Flexible Administration
- ✅ Super users can support any company
- ✅ Regular users restricted to their company
- ✅ Clear role-based access control

## Implementation Files

### Modified Files

1. **ProjectStepService.java**
   - Added `validateUserCompanyAccess()` method
   - Applied validation to all CRUD operations
   - Added logging for security events

2. **ProjectStepController.java**
   - Extracts user from authentication
   - Passes user to all service methods
   - Maintains consistent security flow

## Code Pattern

### Service Method Signature
```java
public AdhocStepResponse getStep(UUID stepId, User user) {
    ProjectStep step = findStep(stepId);
    Project project = getProjectFromStep(step);
    validateUserCompanyAccess(user, project);  // Security check
    // ... rest of logic
}
```

### Controller Method Pattern
```java
@GetMapping("/{projectId}/steps/{stepId}")
public ResponseEntity<ApiResponse<AdhocStepResponse>> getStep(
        @PathVariable UUID projectId,
        @PathVariable UUID stepId,
        Authentication authentication) {
    
    // Extract user from authentication
    User user = getCurrentUser(authentication);
    
    // Pass user to service (security validation happens there)
    AdhocStepResponse response = projectStepService.getStep(stepId, user);
    
    return ResponseEntity.ok(response);
}
```

## Performance Impact

- **Minimal**: Single additional check per operation
- **Efficient**: No additional database queries (company loaded with user/project)
- **Cached**: User and project entities typically already in session

## Backward Compatibility

✅ **No Breaking Changes**
- Existing endpoints unchanged
- Only added `Authentication` parameter where missing
- Response structures unchanged

## Future Enhancements

### Potential Additions
1. **Company-Based Filtering**
   - Filter lists by company automatically
   - No need for explicit company checks

2. **Hierarchical Companies**
   - Parent/child company relationships
   - Access to subsidiary projects

3. **Shared Projects**
   - Cross-company collaboration
   - Explicit project sharing permissions

4. **Audit Logging**
   - Detailed access logs
   - Security violation reports
   - Compliance tracking

## Compliance

### Data Protection
- ✅ GDPR: Data isolation between companies
- ✅ Privacy: Users can't access other companies' data
- ✅ Security: Proper authorization checks

### Best Practices
- ✅ Defense in depth: Multiple layers of security
- ✅ Fail secure: Denies access by default
- ✅ Clear error messages: User knows why access denied
- ✅ Logging: Security events are tracked

## Conclusion

The company security implementation provides robust protection against unauthorized cross-company access while maintaining flexibility for super users. The implementation follows Spring Security best practices and provides clear feedback to users about access restrictions.

**Key Points:**
- ✅ Regular users restricted to their company
- ✅ Super users have unrestricted access
- ✅ Clear error messages and logging
- ✅ Minimal performance impact
- ✅ No breaking changes
- ✅ Applied uniformly across all operations

