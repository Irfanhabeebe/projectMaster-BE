# 📋 Step Update API Documentation for Frontend Development

## Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Base URL](#base-url)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)
7. [Frontend Implementation Examples](#frontend-implementation-examples)
8. [File Upload Guidelines](#file-upload-guidelines)

---

## Overview

The Step Update API allows users to create, read, update, and delete step updates with file attachments for construction project management. This API supports progress tracking, milestone management, and document handling with automatic MIME type detection.

### Key Features
- ✅ Create step updates with file uploads
- ✅ Update existing step updates
- ✅ Search and filter updates at multiple levels (Step/Task/Stage/Project)
- ✅ Download documents with proper MIME types
- ✅ Role-based authorization
- ✅ Automatic MIME type derivation from file extensions
- ✅ Progress tracking and milestone management

---

## Authentication

All endpoints require JWT authentication via the `Authorization` header:

```http
Authorization: Bearer <your-jwt-token>
```

### Authorization Levels
- **Create/Update/Delete**: `ADMIN`, `PROJECT_MANAGER`, `TRADIE`
- **Read/Download**: `ADMIN`, `PROJECT_MANAGER`, `TRADIE`, `CLIENT`

---

## Base URL

```
https://your-api-domain.com/api/step-updates
```

---

## API Endpoints

### 1. Create Step Update

Create a new step update with optional file uploads.

**Endpoint:**
```http
POST /api/step-updates
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

**Request (Multipart Form Data):**
```javascript
const formData = new FormData();

// Add the request JSON as a part
const requestData = {
  stepId: 'a1b2c3d4-e5f6-7890-1234-567890abcdef',
  updateType: 'PROGRESS_UPDATE',
  title: 'Foundation Work Progress',
  comments: 'Weather was perfect for concrete work. No issues encountered.',
  progressPercentage: 75,
  blockers: 'Material delivery delayed by 2 hours',
  documents: [
    {
      fileName: 'foundation_progress_photo.jpg',
      description: 'Photo showing completed foundation preparation',
      documentType: 'PHOTO',
      isPublic: true
    }
  ]
};
formData.append('request', new Blob([JSON.stringify(requestData)], { type: 'application/json' }));

// Add files as separate parts (one for each document)
formData.append('documents[0].file', file1); // File object for first document
formData.append('documents[1].file', file2); // File object for second document
```

**Request Structure:**
The request consists of multiple parts:
1. **`request`** - JSON object containing step update data
2. **`documents[0].file`**, **`documents[1].file`**, etc. - File objects for each document

**Document Structure:**
| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `fileName` | String | ❌ | Original file name for display | `"foundation_progress_photo.jpg"` |
| `description` | String | ❌ | Description of the document | `"Photo showing completed foundation preparation"` |
| `documentType` | String | ✅ | Type of document | `"PHOTO"` |
| `isPublic` | Boolean | ❌ | Whether document is public | `true` |
| `file` | File | ✅ | The actual file to upload | `File object` |

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Step update created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "stepId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
    "stepName": "Foundation Preparation",
    "taskName": "Site Preparation",
    "stageName": "Foundation Stage",
    "projectName": "Residential Building",
    "updatedBy": {
      "id": "user-123",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "role": "TRADIE",
      "companyName": "ABC Construction"
    },
    "updateType": "PROGRESS_UPDATE",
    "title": "Foundation Work Progress",
    "comments": "Weather was perfect for concrete work. No issues encountered.",
    "progressPercentage": 75,
    "updateDate": "2024-01-15T10:30:00",
    "blockers": "Material delivery delayed by 2 hours",
    "documents": [
      {
        "id": "doc-123",
        "fileName": "foundation_progress_photo.jpg",
        "fileExtension": "jpg",
        "mimeType": "image/jpeg",
        "description": "Photo showing completed foundation preparation",
        "documentType": "PHOTO",
        "uploadDate": "2024-01-15T10:30:00",
        "isPublic": true,
        "downloadUrl": "/api/step-updates/documents/doc-123/download",
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      }
    ],
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

---

### 2. Update Step Update

Update an existing step update (does not support file uploads - use create for files).

**Endpoint:**
```http
PUT /api/step-updates/{updateId}
Content-Type: application/json
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "stepId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "updateType": "MILESTONE_REACHED",
  "title": "Foundation Completed",
  "comments": "Foundation work completed successfully. Ready for next phase.",
  "progressPercentage": 100,
  "blockers": null
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Step update updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "stepId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
    "stepName": "Foundation Preparation",
    "taskName": "Site Preparation",
    "stageName": "Foundation Stage",
    "projectName": "Residential Building",
    "updatedBy": {
      "id": "user-123",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "role": "TRADIE",
      "companyName": "ABC Construction"
    },
    "updateType": "MILESTONE_REACHED",
    "title": "Foundation Completed",
    "comments": "Foundation work completed successfully. Ready for next phase.",
    "progressPercentage": 100,
    "updateDate": "2024-01-15T10:30:00",
    "blockers": null,
    "documents": [],
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T11:45:00"
  }
}
```

---

### 3. Get Step Update

Retrieve a specific step update by ID.

**Endpoint:**
```http
GET /api/step-updates/{updateId}
Authorization: Bearer <token>
```

**Response (200 OK):**
Same as create/update response format.

---

### 4. Delete Step Update

Delete a step update and all associated documents.

**Endpoint:**
```http
DELETE /api/step-updates/{updateId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Step update deleted successfully",
  "data": null
}
```

---

### 5. Search Step Updates

Search and retrieve step updates with advanced filtering capabilities.

**Endpoint:**
```http
POST /api/step-updates/search
Content-Type: application/json
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "level": "PROJECT",
  "entityId": "project-uuid-here",
  "updateTypes": ["PROGRESS_UPDATE", "MILESTONE_REACHED"],
  "documentTypes": ["PHOTO", "DOCUMENT"],
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-01-31T23:59:59",
  "milestonesOnly": false,
  "hasDocuments": true,
  "searchText": "foundation",
  "page": 0,
  "size": 10,
  "sortBy": "updateDate",
  "sortDirection": "DESC"
}
```

**Request Parameters:**
| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `level` | String | ✅ | Entity level | `"STEP"`, `"TASK"`, `"STAGE"`, `"PROJECT"` |
| `entityId` | String (UUID) | ✅ | ID of the entity at specified level | `"project-uuid-here"` |
| `updateTypes` | String[] | ❌ | Filter by update types | `["PROGRESS_UPDATE", "MILESTONE_REACHED"]` |
| `documentTypes` | String[] | ❌ | Filter by document types | `["PHOTO", "DOCUMENT"]` |
| `startDate` | String (ISO) | ❌ | Start date filter | `"2024-01-01T00:00:00"` |
| `endDate` | String (ISO) | ❌ | End date filter | `"2024-01-31T23:59:59"` |
| `milestonesOnly` | Boolean | ❌ | Only milestone updates | `false` |
| `hasDocuments` | Boolean | ❌ | Only updates with documents | `true` |
| `searchText` | String | ❌ | Search in title/comments | `"foundation"` |
| `page` | Integer | ❌ | Page number (0-based) | `0` |
| `size` | Integer | ❌ | Page size | `10` |
| `sortBy` | String | ❌ | Sort field | `"updateDate"` |
| `sortDirection` | String | ❌ | Sort direction | `"ASC"` or `"DESC"` |

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Step updates retrieved successfully",
  "data": {
    "updates": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "stepId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
        "stepName": "Foundation Preparation",
        "taskName": "Site Preparation",
        "stageName": "Foundation Stage",
        "projectName": "Residential Building",
        "updatedBy": {
          "id": "user-123",
          "firstName": "John",
          "lastName": "Doe",
          "email": "john.doe@example.com",
          "role": "TRADIE",
          "companyName": "ABC Construction"
        },
        "updateType": "PROGRESS_UPDATE",
        "title": "Foundation Work Progress",
        "comments": "Weather was perfect for concrete work.",
        "progressPercentage": 75,
        "updateDate": "2024-01-15T10:30:00",
        "blockers": "Material delivery delayed",
        "documents": [
          {
            "id": "doc-123",
            "fileName": "uuid-generated-filename.jpg",
            "originalFileName": "foundation_progress_photo.jpg",
            "fileExtension": "jpg",
            "mimeType": "image/jpeg",
            "description": "Photo showing completed foundation preparation",
            "documentType": "PHOTO",
            "uploadDate": "2024-01-15T10:30:00",
            "isPublic": true,
            "downloadUrl": "/api/step-updates/documents/doc-123/download",
            "createdAt": "2024-01-15T10:30:00",
            "updatedAt": "2024-01-15T10:30:00"
          }
        ],
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "currentPage": 0,
    "pageSize": 10,
    "summary": {
      "totalUpdates": 25,
      "milestoneUpdates": 5,
      "updatesWithDocuments": 18,
      "presentUpdateTypes": ["PROGRESS_UPDATE", "MILESTONE_REACHED", "ISSUE_REPORTED"],
      "mostRecentUpdate": "2024-01-15T10:30:00"
    }
  }
}
```

---

### 6. Download Document

Download a document file with proper MIME type headers.

**Endpoint:**
```http
GET /api/step-updates/documents/{documentId}/download
Authorization: Bearer <token>
```

**Response (200 OK):**
- **Content-Type**: Based on MIME type (e.g., `image/jpeg`, `application/pdf`)
- **Content-Disposition**: `attachment; filename="original-filename.ext"`
- **Body**: Binary file content

---

### 7. Delete Document

Delete a specific document from a step update.

**Endpoint:**
```http
DELETE /api/step-updates/documents/{documentId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Document deleted successfully",
  "data": null
}
```

---

## Data Models

### Update Types

```javascript
const UPDATE_TYPES = {
  PROGRESS_UPDATE: 'PROGRESS_UPDATE',
  MILESTONE_REACHED: 'MILESTONE_REACHED',
  ISSUE_REPORTED: 'ISSUE_REPORTED',
  QUALITY_CHECK: 'QUALITY_CHECK',
  SAFETY_INCIDENT: 'SAFETY_INCIDENT',
  GENERAL_COMMENT: 'GENERAL_COMMENT',
  PHOTO_DOCUMENTATION: 'PHOTO_DOCUMENTATION',
  COMPLETION_NOTICE: 'COMPLETION_NOTICE'
};
```

### Document Types

```javascript
const DOCUMENT_TYPES = {
  PHOTO: 'PHOTO',
  VIDEO: 'VIDEO',
  DOCUMENT: 'DOCUMENT',
  DRAWING: 'DRAWING',
  SPECIFICATION: 'SPECIFICATION',
  INSPECTION_REPORT: 'INSPECTION_REPORT',
  SAFETY_CERTIFICATE: 'SAFETY_CERTIFICATE',
  QUALITY_CHECKLIST: 'QUALITY_CHECKLIST',
  MEASUREMENT_RECORD: 'MEASUREMENT_RECORD',
  PERMIT: 'PERMIT',
  INVOICE: 'INVOICE',
  RECEIPT: 'RECEIPT',
  OTHER: 'OTHER'
};
```

### MIME Types (Auto-derived)

The system automatically derives MIME types from file extensions:

```javascript
const MIME_TYPE_MAPPING = {
  // Images
  'jpg': 'image/jpeg',
  'jpeg': 'image/jpeg',
  'png': 'image/png',
  'gif': 'image/gif',
  'webp': 'image/webp',
  'bmp': 'image/bmp',
  'svg': 'image/svg+xml',
  
  // Documents
  'pdf': 'application/pdf',
  'doc': 'application/msword',
  'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'xls': 'application/vnd.ms-excel',
  'xlsx': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  'ppt': 'application/vnd.ms-powerpoint',
  'pptx': 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
  'txt': 'text/plain',
  'html': 'text/html',
  'css': 'text/css',
  'js': 'application/javascript',
  'json': 'application/json',
  'xml': 'application/xml',
  
  // Media
  'mp4': 'video/mp4',
  'avi': 'video/x-msvideo',
  'mov': 'video/quicktime',
  'wmv': 'video/x-ms-wmv',
  'mp3': 'audio/mpeg',
  'wav': 'audio/wav',
  'ogg': 'audio/ogg',
  
  // Archives
  'zip': 'application/zip',
  'rar': 'application/x-rar-compressed',
  '7z': 'application/x-7z-compressed',
  'tar': 'application/x-tar',
  'gz': 'application/gzip',
  
  // Default
  'default': 'application/octet-stream'
};
```

---

## Error Handling

### Common Error Responses

**400 Bad Request:**
```json
{
  "success": false,
  "message": "Invalid request data",
  "data": null
}
```

**401 Unauthorized:**
```json
{
  "success": false,
  "message": "Authentication required",
  "data": null
}
```

**403 Forbidden:**
```json
{
  "success": false,
  "message": "User is not authorized to update this step. User must be assigned to the step or be a project manager.",
  "data": null
}
```

**404 Not Found:**
```json
{
  "success": false,
  "message": "Step update not found: 550e8400-e29b-41d4-a716-446655440000",
  "data": null
}
```

**500 Internal Server Error:**
```json
{
  "success": false,
  "message": "Internal server error occurred",
  "data": null
}
```

---

## Frontend Implementation Examples

### 1. Create Step Update with File Upload

```javascript
const createStepUpdate = async (stepId, updateData, documents) => {
  try {
    const formData = new FormData();
    
    // Prepare request data
    const requestData = {
      stepId: stepId,
      updateType: updateData.updateType,
      title: updateData.title,
      comments: updateData.comments,
      progressPercentage: updateData.progressPercentage,
      blockers: updateData.blockers,
      documents: documents.map(doc => ({
        fileName: doc.fileName,
        description: doc.description,
        documentType: doc.documentType,
        isPublic: doc.isPublic
      }))
    };
    
    // Add request JSON as a part
    formData.append('request', new Blob([JSON.stringify(requestData)], { type: 'application/json' }));
    
    // Add files as separate parts
    documents.forEach((doc, index) => {
      if (doc.file) {
        formData.append(`documents[${index}].file`, doc.file);
      }
    });

    const response = await fetch('/api/step-updates', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`
      },
      body: formData
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Error creating step update:', error);
    throw error;
  }
};
```

### 2. Search Step Updates

```javascript
const searchStepUpdates = async (searchRequest) => {
  try {
    const response = await fetch('/api/step-updates/search', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify(searchRequest)
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Error searching step updates:', error);
    throw error;
  }
};

// Example usage
const searchProjectUpdates = async (projectId) => {
  const searchRequest = {
    level: 'PROJECT',
    entityId: projectId,
    page: 0,
    size: 20,
    sortBy: 'updateDate',
    sortDirection: 'DESC'
  };
  
  return await searchStepUpdates(searchRequest);
};
```

### 3. Update Step Update

```javascript
const updateStepUpdate = async (updateId, updateData) => {
  try {
    const response = await fetch(`/api/step-updates/${updateId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify(updateData)
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Error updating step update:', error);
    throw error;
  }
};
```

### 4. Download Document

```javascript
const downloadDocument = async (documentId, originalFileName) => {
  try {
    const response = await fetch(`/api/step-updates/documents/${documentId}/download`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${getAuthToken()}`
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // Create blob and download
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.style.display = 'none';
    a.href = url;
    a.download = originalFileName || 'document';
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  } catch (error) {
    console.error('Error downloading document:', error);
    throw error;
  }
};
```

### 5. React Hook Example

```javascript
import { useState, useCallback } from 'react';

const useStepUpdates = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const createUpdate = useCallback(async (stepId, updateData, files = []) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await createStepUpdate(stepId, updateData, files);
      return result;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const searchUpdates = useCallback(async (searchRequest) => {
    setLoading(true);
    setError(null);
    
    try {
      const result = await searchStepUpdates(searchRequest);
      return result;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    loading,
    error,
    createUpdate,
    searchUpdates
  };
};

export default useStepUpdates;
```

---

## File Upload Guidelines

### 1. Supported File Types
- **Images**: JPG, JPEG, PNG, GIF, WebP, BMP, SVG
- **Documents**: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT
- **Media**: MP4, AVI, MOV, WMV, MP3, WAV, OGG
- **Archives**: ZIP, RAR, 7Z, TAR, GZ

### 2. File Size Limits
- Check with your backend configuration for specific limits
- Recommended maximum: 10MB per file

### 3. File Validation (Frontend)

```javascript
const validateFile = (file) => {
  const maxSize = 10 * 1024 * 1024; // 10MB
  const allowedTypes = [
    'image/jpeg', 'image/png', 'image/gif', 'image/webp',
    'application/pdf', 'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'video/mp4', 'audio/mpeg'
  ];

  if (file.size > maxSize) {
    throw new Error('File size exceeds 10MB limit');
  }

  if (!allowedTypes.includes(file.type)) {
    throw new Error('File type not supported');
  }

  return true;
};
```

### 4. Multiple File Upload

```javascript
const handleMultipleFileUpload = (files) => {
  const validFiles = [];
  const errors = [];

  Array.from(files).forEach((file, index) => {
    try {
      validateFile(file);
      validFiles.push(file);
    } catch (error) {
      errors.push(`File ${index + 1}: ${error.message}`);
    }
  });

  if (errors.length > 0) {
    console.warn('File validation errors:', errors);
  }

  return { validFiles, errors };
};
```

---

## Usage Examples

### Complete React Component Example

```jsx
import React, { useState } from 'react';
import useStepUpdates from './hooks/useStepUpdates';

const StepUpdateForm = ({ stepId, onSuccess }) => {
  const { loading, error, createUpdate } = useStepUpdates();
  const [formData, setFormData] = useState({
    updateType: 'PROGRESS_UPDATE',
    title: '',
    comments: '',
    progressPercentage: 0,
    blockers: ''
  });
  const [files, setFiles] = useState([]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const result = await createUpdate(stepId, formData, files);
      onSuccess(result.data);
      // Reset form
      setFormData({
        updateType: 'PROGRESS_UPDATE',
        title: '',
        comments: '',
        progressPercentage: 0,
        blockers: ''
      });
      setFiles([]);
    } catch (err) {
      console.error('Failed to create update:', err);
    }
  };

  const handleFileChange = (e) => {
    const selectedFiles = Array.from(e.target.files);
    const { validFiles, errors } = handleMultipleFileUpload(selectedFiles);
    
    if (errors.length > 0) {
      alert(`File validation errors:\n${errors.join('\n')}`);
    }
    
    setFiles(validFiles);
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>Update Type:</label>
        <select 
          value={formData.updateType} 
          onChange={(e) => setFormData(prev => ({ ...prev, updateType: e.target.value }))}
        >
          <option value="PROGRESS_UPDATE">Progress Update</option>
          <option value="MILESTONE_REACHED">Milestone Reached</option>
          <option value="ISSUE_REPORTED">Issue Reported</option>
          <option value="QUALITY_CHECK">Quality Check</option>
          <option value="PHOTO_DOCUMENTATION">Photo Documentation</option>
        </select>
      </div>

      <div>
        <label>Title:</label>
        <input 
          type="text"
          value={formData.title}
          onChange={(e) => setFormData(prev => ({ ...prev, title: e.target.value }))}
        />
      </div>

      <div>
        <label>Comments:</label>
        <textarea 
          value={formData.comments}
          onChange={(e) => setFormData(prev => ({ ...prev, comments: e.target.value }))}
        />
      </div>

      <div>
        <label>Progress Percentage:</label>
        <input 
          type="number"
          min="0"
          max="100"
          value={formData.progressPercentage}
          onChange={(e) => setFormData(prev => ({ ...prev, progressPercentage: parseInt(e.target.value) }))}
        />
      </div>

      <div>
        <label>Blockers:</label>
        <textarea 
          value={formData.blockers}
          onChange={(e) => setFormData(prev => ({ ...prev, blockers: e.target.value }))}
        />
      </div>

      <div>
        <label>Files:</label>
        <input 
          type="file"
          multiple
          onChange={handleFileChange}
        />
        {files.length > 0 && (
          <div>
            <p>Selected files:</p>
            <ul>
              {files.map((file, index) => (
                <li key={index}>{file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)</li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {error && <div style={{ color: 'red' }}>Error: {error}</div>}

      <button type="submit" disabled={loading}>
        {loading ? 'Creating...' : 'Create Update'}
      </button>
    </form>
  );
};

export default StepUpdateForm;
```

---

## Notes for Frontend Developers

1. **Authentication**: Always include the JWT token in the Authorization header
2. **File Upload**: Use FormData for file uploads, not JSON
3. **Error Handling**: Always handle HTTP errors and display user-friendly messages
4. **File Validation**: Validate files on the frontend before upload
5. **Progress Indication**: Show upload progress for better UX
6. **MIME Types**: The backend automatically derives MIME types, so you don't need to specify them
7. **Pagination**: Use the search endpoint's pagination for large datasets
8. **Date Formats**: Use ISO 8601 format for dates (YYYY-MM-DDTHH:mm:ss)

For any questions or clarifications, please contact the backend development team.
