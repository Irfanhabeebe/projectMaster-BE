# 🚀 Builder-Contractor Relationship API - Swagger Documentation

## 📋 Overview
This document outlines the comprehensive Swagger/OpenAPI documentation that has been added to the Builder-Contractor Relationship API endpoints and DTOs.

## 🔗 API Endpoints with Full Swagger Documentation

### 1. **Create Builder-Contractor Relationship**
- **Endpoint**: `POST /api/builder-contractor-relationships/builder/{builderCompanyId}`
- **Tag**: Builder-Contractor Relationships
- **Summary**: Create Builder-Contractor Relationship
- **Description**: Creates a new relationship between a builder company and a contracting company. This establishes a business partnership with customizable specialties and terms.
- **Security**: Bearer Authentication Required
- **Response Codes**:
  - `201`: Relationship created successfully (with example response)
  - `400`: Invalid request data
  - `401`: Unauthorized
  - `404`: Builder company or contracting company not found
  - `409`: Relationship already exists

### 2. **Get Builder-Contractor Relationship**
- **Endpoint**: `GET /api/builder-contractor-relationships/{relationshipId}`
- **Tag**: Builder-Contractor Relationships
- **Summary**: Get Builder-Contractor Relationship
- **Description**: Retrieves a specific builder-contractor relationship by its ID
- **Security**: Bearer Authentication Required
- **Response Codes**:
  - `200`: Relationship retrieved successfully
  - `401`: Unauthorized
  - `404`: Relationship not found

### 3. **Search Builder-Contractor Relationships**
- **Endpoint**: `POST /api/builder-contractor-relationships/builder/{builderCompanyId}/search`
- **Tag**: Builder-Contractor Relationships
- **Summary**: Search Builder-Contractor Relationships
- **Description**: Searches and filters builder-contractor relationships for a specific builder company. Supports text search, active status filtering, specialty filtering, and pagination.
- **Security**: Bearer Authentication Required
- **Response Codes**:
  - `200`: Search completed successfully (with paginated example response)
  - `400`: Invalid search parameters
  - `401`: Unauthorized
  - `404`: Builder company not found

### 4. **Update Builder-Contractor Relationship**
- **Endpoint**: `PUT /api/builder-contractor-relationships/{relationshipId}`
- **Tag**: Builder-Contractor Relationships
- **Summary**: Update Builder-Contractor Relationship
- **Description**: Updates an existing builder-contractor relationship. Can modify contract terms, specialties, and active status.
- **Security**: Bearer Authentication Required
- **Response Codes**:
  - `200`: Relationship updated successfully
  - `400`: Invalid request data
  - `401`: Unauthorized
  - `404`: Relationship not found

### 5. **Deactivate Builder-Contractor Relationship**
- **Endpoint**: `POST /api/builder-contractor-relationships/{relationshipId}/deactivate`
- **Tag**: Builder-Contractor Relationships
- **Summary**: Deactivate Builder-Contractor Relationship
- **Description**: Deactivates a builder-contractor relationship. The relationship remains in the system but is marked as inactive.
- **Security**: Bearer Authentication Required
- **Response Codes**:
  - `200`: Relationship deactivated successfully
  - `401`: Unauthorized
  - `404`: Relationship not found

### 6. **Activate Builder-Contractor Relationship**
- **Endpoint**: `POST /api/builder-contractor-relationships/{relationshipId}/activate`
- **Tag**: Builder-Contractor Relationships
- **Summary**: Activate Builder-Contractor Relationship
- **Description**: Reactivates a previously deactivated builder-contractor relationship. The relationship becomes active again and can be used for new projects.
- **Security**: Bearer Authentication Required
- **Response Codes**:
  - `200`: Relationship activated successfully
  - `401`: Unauthorized
  - `404`: Relationship not found

## 📊 DTOs with Comprehensive Schema Documentation

### 1. **CreateBuilderContractorRelationshipRequest**
- **Schema Description**: Request DTO for creating a new builder-contractor relationship
- **Fields**:
  - `contractingCompanyId`: ID of the contracting company (required, with example UUID)
  - `contractStartDate`: Start date of the business relationship (required, with example date)
  - `contractEndDate`: End date of the business relationship (optional, with example date)
  - `paymentTerms`: Payment terms and conditions (with example)
  - `notes`: Additional notes about the relationship (with example)
  - `specialties`: List of specialties this contractor will provide (with nested schema)

#### **RelationshipSpecialtyRequest (Nested)**
- **Schema Description**: Specialty details for the contractor relationship
- **Fields**:
  - `specialtyId`: ID of the specialty (required, with example UUID)
  - `customNotes`: Custom notes for this specialty (with example)
  - `preferredRating`: Preferred rating 1-5 (with min/max constraints and example)
  - `hourlyRate`: Hourly rate for this specialty (with example)
  - `availabilityStatus`: Current availability status (with allowable values: available, busy, unavailable)

### 2. **BuilderContractorRelationshipSearchRequest**
- **Schema Description**: Search and filter criteria for builder-contractor relationships
- **Fields**:
  - `searchText`: Text to search in company names, ABN, email, contact person (with example)
  - `activeOnly`: Show only active relationships (default: true, with example)
  - `specialtyType`: Filter by specialty type (with example)
  - `specialtyName`: Filter by specific specialty name (with example)
  - `availabilityStatus`: Filter by availability status (with allowable values)
  - `page`: Page number 0-based (with min constraint and example)
  - `size`: Page size (with min/max constraints and example)
  - `sortBy`: Field to sort by (with allowable values and example)
  - `sortDirection`: Sort direction (with allowable values: ASC, DESC and example)

## 🎯 Key Swagger Features Added

### **Security Documentation**
- All endpoints marked with `@SecurityRequirement(name = "bearerAuth")`
- Clear indication that JWT authentication is required

### **Parameter Documentation**
- All path variables documented with `@Parameter` annotations
- Clear descriptions and examples for each parameter
- Required/optional status clearly indicated

### **Response Documentation**
- Comprehensive response codes with descriptions
- Example responses for complex endpoints
- Clear error scenarios documented

### **Schema Documentation**
- All DTOs documented with `@Schema` annotations
- Field-level documentation with examples
- Constraints and allowable values specified
- Nested object schemas fully documented

### **Tag Organization**
- All endpoints grouped under "Builder-Contractor Relationships" tag
- Consistent naming and organization

## 🌐 Swagger UI Access

Once your application is running, you can access the Swagger UI at:
```
http://localhost:8080/swagger-ui/index.html
```

The Builder-Contractor Relationships section will show:
- All 6 endpoints with full documentation
- Request/response schemas with examples
- Interactive testing capabilities
- Parameter validation and examples

## 🚀 Benefits of This Documentation

1. **Developer Experience**: Clear understanding of API usage
2. **Testing**: Interactive API testing through Swagger UI
3. **Integration**: Frontend developers can easily understand request/response formats
4. **Maintenance**: Self-documenting code that stays in sync with implementation
5. **Onboarding**: New team members can quickly understand the API structure

## ✨ Ready to Use!

Your Builder-Contractor Relationship API is now fully documented in Swagger and ready for:
- Frontend integration
- API testing
- Team collaboration
- Client documentation

All endpoints include comprehensive examples, validation rules, and clear descriptions! 🎉
