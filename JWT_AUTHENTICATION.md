# JWT Authentication & Security Implementation

## Overview

The ProjectMaster application now includes a complete JWT (JSON Web Token) authentication system with role-based access control. This implementation provides secure, stateless authentication that integrates seamlessly with the existing user management system.

## Architecture

### Core Components

1. **JWT Service** (`JwtService.java`)
   - Token generation and validation
   - Claims extraction and verification
   - Configurable expiration times

2. **Authentication Service** (`AuthenticationService.java`)
   - User authentication logic
   - Token refresh functionality
   - Logout management

3. **Custom UserDetailsService** (`CustomUserDetailsService.java`)
   - Spring Security integration
   - User loading from database
   - Role mapping

4. **JWT Authentication Filter** (`JwtAuthenticationFilter.java`)
   - Request interception
   - Token validation
   - Security context setup

5. **Security Configuration** (`SecurityConfig.java`)
   - Spring Security setup
   - Role-based access control
   - CORS configuration

## Authentication Endpoints

### Base URL: `/auth`

#### 1. Login
- **Endpoint**: `POST /auth/login`
- **Description**: Authenticate user and receive JWT tokens
- **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "userpassword"
}
```
- **Success Response** (200):
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "ADMIN",
      "company": {
        "id": 1,
        "name": "Company Name"
      }
    }
  },
  "timestamp": "2025-07-11T08:00:00Z"
}
```
- **Error Response** (401):
```json
{
  "success": false,
  "message": "Invalid email or password",
  "timestamp": "2025-07-11T08:00:00Z"
}
```

#### 2. Refresh Token
- **Endpoint**: `POST /auth/refresh`
- **Description**: Get new access token using refresh token
- **Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- **Success Response** (200):
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "timestamp": "2025-07-11T08:00:00Z"
}
```

#### 3. Logout
- **Endpoint**: `POST /auth/logout`
- **Description**: Invalidate current session
- **Headers**: `Authorization: Bearer <access_token>`
- **Success Response** (200):
```json
{
  "success": true,
  "message": "Logged out successfully",
  "timestamp": "2025-07-11T08:00:00Z"
}
```

#### 4. Validate User
- **Endpoint**: `GET /auth/me`
- **Description**: Get current authenticated user information
- **Headers**: `Authorization: Bearer <access_token>`
- **Success Response** (200):
```json
{
  "success": true,
  "message": "User information retrieved successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "ADMIN",
    "company": {
      "id": 1,
      "name": "Company Name"
    }
  },
  "timestamp": "2025-07-11T08:00:00Z"
}
```

## Role-Based Access Control

### User Roles
- **ADMIN**: Full system access
- **PROJECT_MANAGER**: Project and user management
- **TRADIE**: Project participation and task management
- **CUSTOMER**: Limited access to own projects

### Endpoint Access Control

#### Public Endpoints (No Authentication Required)
- `/auth/**` - Authentication endpoints
- `/public/**` - Public resources
- `/actuator/health` - Health check
- `/swagger-ui/**`, `/v3/api-docs/**` - API documentation

#### Admin Only
- `/admin/**` - Administrative functions

#### Admin & Project Manager
- `/companies/**` - Company management
- `/users/**` - User management
- `/customers/**` - Customer management

#### Admin, Project Manager & Tradie
- `/projects/**` - Project management

#### All Authenticated Users
- All other endpoints require authentication

## JWT Configuration

### Token Properties
- **Access Token Expiration**: 24 hours (86400000 ms)
- **Refresh Token Expiration**: 7 days (604800000 ms)
- **Algorithm**: HMAC SHA-256
- **Secret**: Configurable via `jwt.secret` property

### Configuration Properties
```properties
# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

## Security Features

### 1. Password Security
- Integration with existing `SimplePasswordEncoder`
- SHA-256 hashing with salt
- Secure password verification

### 2. Token Security
- Stateless JWT tokens
- Configurable expiration times
- Secure token validation
- Refresh token rotation

### 3. CORS Configuration
- Configurable allowed origins
- Support for all HTTP methods
- Credential support enabled

### 4. Exception Handling
- Comprehensive error responses
- Security-specific exception handling
- Consistent API response format

## Usage Examples

### 1. Authentication Flow
```bash
# 1. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}'

# 2. Use access token for protected endpoints
curl -X GET http://localhost:8080/users \
  -H "Authorization: Bearer <access_token>"

# 3. Refresh token when needed
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh_token>"}'
```

### 2. Frontend Integration
```javascript
// Login
const loginResponse = await fetch('/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

const { data } = await loginResponse.json();
localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('refreshToken', data.refreshToken);

// Authenticated requests
const response = await fetch('/api/protected-endpoint', {
  headers: {
    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
  }
});
```

## Database Integration

### User Authentication
- Uses existing `User` entity with `passwordHash` field
- Integrates with `Company` relationship
- Supports user roles and status checking

### Password Encoding
- Compatible with existing `SimplePasswordEncoder`
- Maintains backward compatibility
- Secure password verification

## Error Handling

### Common Error Responses

#### 401 Unauthorized
```json
{
  "success": false,
  "message": "Invalid email or password",
  "timestamp": "2025-07-11T08:00:00Z"
}
```

#### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied",
  "timestamp": "2025-07-11T08:00:00Z"
}
```

#### 400 Bad Request
```json
{
  "success": false,
  "message": "Invalid request format",
  "timestamp": "2025-07-11T08:00:00Z"
}
```

## Development Notes

### Testing
- Application runs successfully with H2 in-memory database
- All endpoints are accessible and functional
- Security filters are properly configured

### Production Considerations
- Update `jwt.secret` with a secure, randomly generated key
- Configure appropriate CORS origins
- Set up proper database credentials
- Consider token blacklisting for enhanced security
- Implement rate limiting for authentication endpoints

### Future Enhancements
- Token blacklisting/revocation
- Multi-factor authentication
- OAuth2 integration
- Session management
- Audit logging