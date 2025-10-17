# Workflow Management API Documentation

**Date:** October 15, 2025  
**Version:** 1.0  
**Base URL:** `http://localhost:8080/api`

---

## 📋 Overview

This document provides comprehensive API documentation for the **Workflow Management Module** - a complete system for managing workflow templates, stages, tasks, and steps with full CRUD operations, search, pagination, and dependency management.

### 🎯 Key Features
- ✅ **Workflow Template Management** - Create, read, update, delete templates
- ✅ **Stage Management** - Manage workflow stages within templates
- ✅ **Task Management** - Manage tasks within stages with dependencies
- ✅ **Step Management** - Manage steps within tasks with specialties and dependencies
- ✅ **Search & Pagination** - Advanced filtering and pagination for templates
- ✅ **Dependency Management** - Complex dependency relationships between entities
- ✅ **Category Management** - Organize templates by categories

---

## 🔐 Authentication

All endpoints require JWT authentication with appropriate roles:

**Required Roles:**
- `PROJECT_MANAGER` - Full access to all operations
- `ADMIN` - Full access to all operations
- `USER` - Read-only access to templates

**Headers:**
```http
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

---

## 📊 Workflow Template APIs

### 1. List Workflow Templates (with Search & Pagination)

**Endpoint:** `GET /api/workflow/templates`

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `search` | string | No | - | Search by template name |
| `category` | string | No | - | Filter by category |
| `page` | integer | No | 0 | Page number (0-based) |
| `size` | integer | No | 20 | Page size |
| `sortBy` | string | No | "name" | Sort field |
| `sortDir` | string | No | "asc" | Sort direction (asc/desc) |

**Example Request:**
```http
GET /api/workflow/templates?search=residential&category=CONSTRUCTION&page=0&size=10&sortBy=name&sortDir=asc
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Workflow templates retrieved successfully",
  "data": {
    "content": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "companyId": "company-uuid",
        "companyName": "ABC Construction",
        "name": "Residential Construction",
        "description": "Standard residential construction workflow",
        "category": "CONSTRUCTION",
        "active": true,
        "isDefault": false,
        "createdAt": "2025-10-15T10:30:00Z",
        "updatedAt": "2025-10-15T10:30:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false,
    "numberOfElements": 10
  }
}
```

### 2. Get Template Details

**Endpoint:** `GET /api/workflow/templates/{templateId}`

**Example Request:**
```http
GET /api/workflow/templates/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Template details retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "companyId": "company-uuid",
    "companyName": "ABC Construction",
    "name": "Residential Construction",
    "description": "Standard residential construction workflow",
    "category": "CONSTRUCTION",
    "active": true,
    "isDefault": false,
    "version": 1,
    "createdAt": "2025-10-15T10:30:00Z",
    "updatedAt": "2025-10-15T10:30:00Z",
    "stages": [
      {
        "id": "stage-uuid",
        "name": "Foundation",
        "description": "Foundation work stage",
        "orderIndex": 1,
        "parallelExecution": false,
        "requiredApprovals": 1,
        "estimatedDurationDays": 14,
        "version": 1,
        "createdAt": "2025-10-15T10:30:00Z",
        "updatedAt": "2025-10-15T10:30:00Z",
        "tasks": [
          {
            "id": "task-uuid",
            "name": "Excavation",
            "description": "Site excavation work",
            "estimatedDays": 3,
            "version": 1,
            "createdAt": "2025-10-15T10:30:00Z",
            "updatedAt": "2025-10-15T10:30:00Z",
            "steps": [
              {
                "id": "step-uuid",
                "name": "Site Survey",
                "description": "Survey the construction site",
                "estimatedDays": 1,
                "specialtyId": "specialty-uuid",
                "specialtyName": "Surveyor",
                "specialtyType": "PROFESSIONAL",
                "version": 1,
                "createdAt": "2025-10-15T10:30:00Z",
                "updatedAt": "2025-10-15T10:30:00Z",
                "stepRequirements": [],
                "dependencies": []
              }
            ],
            "dependencies": []
          }
        ]
      }
    ]
  }
}
```

### 3. Create Workflow Template

**Endpoint:** `POST /api/workflow/templates`

**Request Body:**
```json
{
  "name": "Custom Construction Workflow",
  "description": "Custom workflow for specific project requirements",
  "category": "CONSTRUCTION",
  "active": true,
  "isDefault": false,
  "stages": [
    {
      "name": "Planning",
      "description": "Project planning stage",
      "orderIndex": 1,
      "parallelExecution": false,
      "requiredApprovals": 0,
      "estimatedDurationDays": 7,
      "tasks": [
        {
          "name": "Design Review",
          "description": "Review architectural designs",
          "estimatedDays": 2,
          "steps": [
            {
              "name": "Architectural Review",
              "description": "Review architectural plans",
              "estimatedDays": 1,
              "specialtyId": "architect-specialty-uuid",
              "dependencies": []
            }
          ],
          "dependencies": []
        }
      ]
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Workflow template created successfully",
  "data": {
    "id": "new-template-uuid",
    "companyId": "company-uuid",
    "companyName": "ABC Construction",
    "name": "Custom Construction Workflow",
    "description": "Custom workflow for specific project requirements",
    "category": "CONSTRUCTION",
    "active": true,
    "isDefault": false,
    "createdAt": "2025-10-15T11:00:00Z",
    "updatedAt": "2025-10-15T11:00:00Z"
  }
}
```

### 4. Update Workflow Template

**Endpoint:** `PUT /api/workflow/templates/{templateId}`

**Request Body:** (Same as create)

**Response:** (Same as create)

### 5. Delete Workflow Template

**Endpoint:** `DELETE /api/workflow/templates/{templateId}`

**Response:**
```json
{
  "success": true,
  "message": "Workflow template deleted successfully",
  "data": null
}
```

### 6. Clone Workflow Template

**Endpoint:** `POST /api/workflow/templates/{templateId}/clone`

**Description:** Clone an existing workflow template within the same company with a new name. This creates a complete copy of the template including all stages, tasks, steps, dependencies, and requirements.

**Request Body:**
```json
{
  "newTemplateName": "Residential Construction - Copy",
  "description": "Cloned version of the residential construction workflow"
}
```

**Request Body Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `newTemplateName` | string | Yes | Name for the cloned template (1-255 characters) |
| `description` | string | No | Optional description for the cloned template (max 1000 characters) |

**Example Request:**
```http
POST /api/workflow/templates/550e8400-e29b-41d4-a716-446655440000/clone
Authorization: Bearer <token>
Content-Type: application/json

{
  "newTemplateName": "Residential Construction - Modified",
  "description": "Modified version for custom requirements"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Template cloned successfully",
  "data": {
    "id": "new-cloned-template-uuid",
    "companyId": "company-uuid",
    "companyName": "ABC Construction",
    "name": "Residential Construction - Modified",
    "description": "Modified version for custom requirements",
    "category": "CONSTRUCTION",
    "active": true,
    "isDefault": false,
    "createdAt": "2025-10-15T12:00:00Z",
    "updatedAt": "2025-10-15T12:00:00Z"
  }
}
```

**Error Responses:**

**Template Not Found (404):**
```json
{
  "success": false,
  "message": "Failed to clone template: Workflow template not found: template-id",
  "data": null
}
```

**Duplicate Name (409):**
```json
{
  "success": false,
  "message": "Failed to clone template: Template with name 'Residential Construction - Modified' already exists for this company",
  "data": null
}
```

**Validation Error (400):**
```json
{
  "success": false,
  "message": "Failed to clone template: Template name is required",
  "data": null
}
```

**What Gets Cloned:**
- ✅ **Template Properties** - Name, description, category, settings
- ✅ **Stages** - All stages with their properties and order
- ✅ **Tasks** - All tasks within each stage
- ✅ **Steps** - All steps within each task
- ✅ **Dependencies** - All dependency relationships between entities
- ✅ **Requirements** - All step requirements and materials
- ✅ **Specialties** - Specialty assignments for steps

**Use Cases:**
- Create variations of existing workflows
- Customize templates for specific project types
- Create templates based on successful project workflows
- Maintain template versions for different requirements

### 7. Get Template Categories

**Endpoint:** `GET /api/workflow/templates/categories`

**Response:**
```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    "CONSTRUCTION",
    "RENOVATION",
    "MAINTENANCE",
    "CUSTOM"
  ]
}
```

---

## 🏗️ Workflow Stage APIs

### 1. Create Workflow Stage

**Endpoint:** `POST /api/workflow/templates/{templateId}/stages`

**Request Body:**
```json
{
  "name": "Foundation Work",
  "description": "Foundation construction stage",
  "orderIndex": 2,
  "parallelExecution": false,
  "requiredApprovals": 1,
  "estimatedDurationDays": 14
}
```

**Response:**
```json
{
  "success": true,
  "message": "Workflow stage created successfully",
  "data": {
    // Full template detail response with updated stages
  }
}
```

### 2. Update Workflow Stage

**Endpoint:** `PUT /api/workflow/templates/{templateId}/stages/{stageId}`

**Request Body:** (Same as create)

**Response:** (Full template detail response)

### 3. Delete Workflow Stage

**Endpoint:** `DELETE /api/workflow/templates/{templateId}/stages/{stageId}`

**Response:** (Full template detail response)

---

## 📋 Workflow Task APIs

### 1. Create Workflow Task

**Endpoint:** `POST /api/workflow/templates/{templateId}/stages/{stageId}/tasks`

**Request Body:**
```json
{
  "name": "Excavation Work",
  "description": "Site excavation and preparation",
  "estimatedDays": 3,
  "dependencies": [
    {
      "dependentEntityType": "TASK",
      "dependentEntityId": "current-task-uuid",
      "dependsOnEntityType": "TASK",
      "dependsOnEntityId": "previous-task-uuid",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0
    }
  ]
}
```

**Response:** (Full template detail response)

### 2. Update Workflow Task

**Endpoint:** `PUT /api/workflow/templates/{templateId}/stages/{stageId}/tasks/{taskId}`

**Request Body:** (Same as create)

**Response:** (Full template detail response)

### 3. Delete Workflow Task

**Endpoint:** `DELETE /api/workflow/templates/{templateId}/stages/{stageId}/tasks/{taskId}`

**Response:** (Full template detail response)

---

## ⚙️ Workflow Step APIs

### 1. Create Workflow Step

**Endpoint:** `POST /api/workflow/templates/{templateId}/stages/{stageId}/tasks/{taskId}/steps`

**Request Body:**
```json
{
  "name": "Site Survey",
  "description": "Survey the construction site",
  "estimatedDays": 1,
  "specialtyId": "surveyor-specialty-uuid",
  "dependencies": [
    {
      "dependentEntityType": "STEP",
      "dependentEntityId": "current-step-uuid",
      "dependsOnEntityType": "STEP",
      "dependsOnEntityId": "previous-step-uuid",
      "dependencyType": "FINISH_TO_START",
      "lagDays": 0
    }
  ]
}
```

**Response:** (Full template detail response)

### 2. Update Workflow Step

**Endpoint:** `PUT /api/workflow/templates/{templateId}/stages/{stageId}/tasks/{taskId}/steps/{stepId}`

**Request Body:** (Same as create)

**Response:** (Full template detail response)

### 3. Delete Workflow Step

**Endpoint:** `DELETE /api/workflow/templates/{templateId}/stages/{stageId}/tasks/{taskId}/steps/{stepId}`

**Response:** (Full template detail response)

---

## 📦 Step Requirements Management

### 1. Get Step Requirements

**Endpoint:** `GET /api/workflow/steps/{stepId}/requirements`

**Description:** Retrieve all material requirements for a specific workflow step.

**Headers:**
```
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "message": "Requirements retrieved successfully",
  "data": [
    {
      "id": "requirement-uuid-1",
      "itemName": "Concrete Mix",
      "itemDescription": "Ready-mix concrete for foundation",
      "displayOrder": 1,
      "categoryId": "category-uuid",
      "categoryName": "Concrete & Cement",
      "categoryGroup": "Building Materials",
      "supplierId": "supplier-uuid",
      "supplierName": "ABC Concrete Supply",
      "brand": "Premium Mix",
      "model": "PM-4000",
      "defaultQuantity": "10",
      "unit": "cubic meters",
      "estimatedCost": "1500.00",
      "procurementType": "BUY",
      "isOptional": false,
      "notes": "Ensure proper curing",
      "supplierItemCode": "PMC-4000",
      "templateNotes": "Standard concrete mix for residential",
      "createdAt": "2025-10-15T10:00:00Z",
      "updatedAt": "2025-10-15T10:00:00Z",
      "customerSelectable": true
    },
    {
      "id": "requirement-uuid-2",
      "itemName": "Steel Reinforcement",
      "itemDescription": "Rebar for concrete reinforcement",
      "displayOrder": 2,
      "categoryId": "category-uuid-2",
      "categoryName": "Steel & Metal",
      "categoryGroup": "Building Materials",
      "supplierId": "supplier-uuid-2",
      "supplierName": "Steel Works Ltd",
      "brand": "SteelMax",
      "model": "SM-R12",
      "defaultQuantity": "500",
      "unit": "kg",
      "estimatedCost": "2500.00",
      "procurementType": "BUY",
      "isOptional": false,
      "notes": "Grade 60 steel",
      "supplierItemCode": "SW-R12-60",
      "templateNotes": "Standard rebar for foundation",
      "createdAt": "2025-10-15T10:00:00Z",
      "updatedAt": "2025-10-15T10:00:00Z",
      "customerSelectable": true
    }
  ]
}
```

**Error Responses:**
- `404` - Step not found
- `403` - Access denied (step doesn't belong to user's company)

### 2. Bulk Update Step Requirements

**Endpoint:** `PUT /api/workflow/steps/{stepId}/requirements`

**Description:** Bulk update, create, and delete requirements for a workflow step. This endpoint performs a "smart" update:
- Updates existing requirements that match (by category + item name)
- Creates new requirements that don't exist
- Deletes requirements that are not in the request payload

**Request Body:**
```json
[
  {
    "itemName": "Concrete Mix",
    "itemDescription": "Ready-mix concrete for foundation",
    "displayOrder": 1,
    "categoryId": "category-uuid",
    "supplierId": "supplier-uuid",
    "brand": "Premium Mix",
    "model": "PM-4000",
    "defaultQuantity": "10",
    "unit": "cubic meters",
    "estimatedCost": "1500.00",
    "procurementType": "BUY",
    "isOptional": false,
    "notes": "Ensure proper curing",
    "supplierItemCode": "PMC-4000",
    "templateNotes": "Standard concrete mix for residential",
    "customerSelectable": true
  }
]
```

**Response:**
```json
{
  "success": true,
  "message": "Requirements updated successfully",
  "data": "Updated 1 requirements"
}
```

---

## 🔗 Dependency Types

**Available Dependency Types:**
- `FINISH_TO_START` - Task/Step B starts after Task/Step A finishes
- `START_TO_START` - Task/Step B starts when Task/Step A starts
- `FINISH_TO_FINISH` - Task/Step B finishes when Task/Step A finishes
- `START_TO_FINISH` - Task/Step B finishes when Task/Step A starts

**Entity Types:**
- `STAGE` - Workflow stage
- `TASK` - Workflow task
- `STEP` - Workflow step

---

## 📱 Frontend Implementation Guide

### 1. Workflow List Screen

**Features to implement:**
- Search box for template names
- Category filter dropdown
- Pagination controls
- Sort options (name, created date, updated date)
- Template cards with basic info
- "View Details" button for each template
- "Clone Template" button for each template

**Example React Component:**
```typescript
interface WorkflowTemplate {
  id: string;
  name: string;
  description: string;
  category: string;
  active: boolean;
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
}

const WorkflowListScreen = () => {
  const [templates, setTemplates] = useState<WorkflowTemplate[]>([]);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchTemplates = async () => {
    const params = new URLSearchParams({
      search,
      category,
      page: page.toString(),
      size: '20',
      sortBy: 'name',
      sortDir: 'asc'
    });
    
    const response = await fetch(`/api/workflow/templates?${params}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    const data = await response.json();
    setTemplates(data.data.content);
    setTotalPages(data.data.totalPages);
  };

  const cloneTemplate = async (templateId: string, newName: string) => {
    try {
      const response = await fetch(`/api/workflow/templates/${templateId}/clone`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          newTemplateName: newName,
          description: `Cloned from ${templates.find(t => t.id === templateId)?.name}`
        })
      });
      
      const data = await response.json();
      if (data.success) {
        alert('Template cloned successfully!');
        fetchTemplates(); // Refresh the list
      } else {
        alert('Failed to clone template: ' + data.message);
      }
    } catch (error) {
      alert('Error cloning template: ' + error);
    }
  };

  useEffect(() => {
    fetchTemplates();
  }, [search, category, page]);

  return (
    <div>
      <div className="filters">
        <input 
          type="text" 
          placeholder="Search templates..." 
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select value={category} onChange={(e) => setCategory(e.target.value)}>
          <option value="">All Categories</option>
          <option value="CONSTRUCTION">Construction</option>
          <option value="RENOVATION">Renovation</option>
        </select>
      </div>
      
      <div className="template-grid">
        {templates.map(template => (
          <div key={template.id} className="template-card">
            <h3>{template.name}</h3>
            <p>{template.description}</p>
            <span className="category">{template.category}</span>
            <div className="actions">
              <button onClick={() => window.open(`/workflow/${template.id}`, '_blank')}>
                View Details
              </button>
              <button onClick={() => {
                const newName = prompt('Enter new template name:', `${template.name} - Copy`);
                if (newName) cloneTemplate(template.id, newName);
              }}>
                Clone Template
              </button>
            </div>
          </div>
        ))}
      </div>
      
      <Pagination 
        currentPage={page} 
        totalPages={totalPages} 
        onPageChange={setPage} 
      />
    </div>
  );
};
```

### 2. Template Detail Screen

**Features to implement:**
- Template header with basic info
- Stages list with expand/collapse
- Tasks list within each stage
- Steps list within each task
- Dependency visualization
- Edit buttons for each entity
- Add new stage/task/step buttons

**Example React Component:**
```typescript
interface TemplateDetail {
  id: string;
  name: string;
  description: string;
  category: string;
  stages: StageDetail[];
}

interface StageDetail {
  id: string;
  name: string;
  description: string;
  orderIndex: number;
  tasks: TaskDetail[];
}

interface TaskDetail {
  id: string;
  name: string;
  description: string;
  estimatedDays: number;
  steps: StepDetail[];
  dependencies: DependencyDetail[];
}

interface StepDetail {
  id: string;
  name: string;
  description: string;
  estimatedDays: number;
  specialtyId: string;
  specialtyName: string;
  dependencies: DependencyDetail[];
}

const TemplateDetailScreen = ({ templateId }: { templateId: string }) => {
  const [template, setTemplate] = useState<TemplateDetail | null>(null);
  const [expandedStages, setExpandedStages] = useState<Set<string>>(new Set());
  const [expandedTasks, setExpandedTasks] = useState<Set<string>>(new Set());

  const fetchTemplate = async () => {
    const response = await fetch(`/api/workflow/templates/${templateId}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    const data = await response.json();
    setTemplate(data.data);
  };

  const toggleStage = (stageId: string) => {
    const newExpanded = new Set(expandedStages);
    if (newExpanded.has(stageId)) {
      newExpanded.delete(stageId);
    } else {
      newExpanded.add(stageId);
    }
    setExpandedStages(newExpanded);
  };

  const toggleTask = (taskId: string) => {
    const newExpanded = new Set(expandedTasks);
    if (newExpanded.has(taskId)) {
      newExpanded.delete(taskId);
    } else {
      newExpanded.add(taskId);
    }
    setExpandedTasks(newExpanded);
  };

  useEffect(() => {
    fetchTemplate();
  }, [templateId]);

  if (!template) return <div>Loading...</div>;

  return (
    <div className="template-detail">
      <div className="template-header">
        <h1>{template.name}</h1>
        <p>{template.description}</p>
        <span className="category">{template.category}</span>
      </div>

      <div className="stages">
        {template.stages.map(stage => (
          <div key={stage.id} className="stage">
            <div className="stage-header" onClick={() => toggleStage(stage.id)}>
              <h3>{stage.name}</h3>
              <span className="order">#{stage.orderIndex}</span>
              <button>Edit</button>
            </div>
            
            {expandedStages.has(stage.id) && (
              <div className="tasks">
                {stage.tasks.map(task => (
                  <div key={task.id} className="task">
                    <div className="task-header" onClick={() => toggleTask(task.id)}>
                      <h4>{task.name}</h4>
                      <span className="duration">{task.estimatedDays} days</span>
                      <button>Edit</button>
                    </div>
                    
                    {expandedTasks.has(task.id) && (
                      <div className="steps">
                        {task.steps.map(step => (
                          <div key={step.id} className="step">
                            <h5>{step.name}</h5>
                            <span className="specialty">{step.specialtyName}</span>
                            <span className="duration">{step.estimatedDays} days</span>
                            <button>Edit</button>
                          </div>
                        ))}
                        <button className="add-step">Add Step</button>
                      </div>
                    )}
                  </div>
                ))}
                <button className="add-task">Add Task</button>
              </div>
            )}
          </div>
        ))}
        <button className="add-stage">Add Stage</button>
      </div>
    </div>
  );
};
```

### 3. Create/Edit Forms

**Template Form:**
```typescript
const TemplateForm = ({ template, onSave }: { template?: TemplateDetail, onSave: (template: TemplateDetail) => void }) => {
  const [formData, setFormData] = useState({
    name: template?.name || '',
    description: template?.description || '',
    category: template?.category || '',
    active: template?.active ?? true,
    isDefault: template?.isDefault ?? false
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const url = template 
      ? `/api/workflow/templates/${template.id}`
      : '/api/workflow/templates';
    
    const method = template ? 'PUT' : 'POST';
    
    const response = await fetch(url, {
      method,
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(formData)
    });
    
    const data = await response.json();
    if (data.success) {
      onSave(data.data);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-group">
        <label>Template Name</label>
        <input 
          type="text" 
          value={formData.name}
          onChange={(e) => setFormData({...formData, name: e.target.value})}
          required
        />
      </div>
      
      <div className="form-group">
        <label>Description</label>
        <textarea 
          value={formData.description}
          onChange={(e) => setFormData({...formData, description: e.target.value})}
        />
      </div>
      
      <div className="form-group">
        <label>Category</label>
        <select 
          value={formData.category}
          onChange={(e) => setFormData({...formData, category: e.target.value})}
        >
          <option value="">Select Category</option>
          <option value="CONSTRUCTION">Construction</option>
          <option value="RENOVATION">Renovation</option>
          <option value="MAINTENANCE">Maintenance</option>
        </select>
      </div>
      
      <div className="form-group">
        <label>
          <input 
            type="checkbox" 
            checked={formData.active}
            onChange={(e) => setFormData({...formData, active: e.target.checked})}
          />
          Active
        </label>
      </div>
      
      <div className="form-group">
        <label>
          <input 
            type="checkbox" 
            checked={formData.isDefault}
            onChange={(e) => setFormData({...formData, isDefault: e.target.checked})}
          />
          Default Template
        </label>
      </div>
      
      <button type="submit">
        {template ? 'Update Template' : 'Create Template'}
      </button>
    </form>
  );
};
```

---

## 🚨 Error Handling

**Common Error Responses:**

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

**HTTP Status Codes:**
- `200` - Success
- `201` - Created
- `400` - Bad Request (validation errors)
- `401` - Unauthorized
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `409` - Conflict (duplicate name, etc.)
- `500` - Internal Server Error

---

## 📝 Notes for Frontend Team

### 1. **State Management**
- Use React Context or Redux for managing template state
- Cache template details to avoid unnecessary API calls
- Implement optimistic updates for better UX

### 2. **Form Validation**
- Validate required fields on frontend before API calls
- Show loading states during API operations
- Handle validation errors gracefully

### 3. **Dependency Visualization**
- Consider using a flowchart library (like React Flow) for dependency visualization
- Show dependency types with different colors/styles
- Allow drag-and-drop for reordering stages/tasks/steps

### 4. **Performance**
- Implement virtual scrolling for large template lists
- Use pagination for templates with many stages/tasks/steps
- Debounce search input to avoid excessive API calls

### 5. **User Experience**
- Provide confirmation dialogs for delete operations
- Show success/error messages for all operations
- Implement breadcrumb navigation for deep template structures

---

## 🔧 Testing

**Test Scenarios:**
1. **Template CRUD** - Create, read, update, delete templates
2. **Template Cloning** - Clone existing templates with new names
3. **Search & Pagination** - Test search functionality and pagination controls
4. **Stage Management** - Add, edit, delete stages within templates
5. **Task Management** - Add, edit, delete tasks with dependencies
6. **Step Management** - Add, edit, delete steps with specialties
7. **Dependency Management** - Create and visualize dependencies
8. **Error Handling** - Test error scenarios and edge cases
9. **Permission Testing** - Test with different user roles
10. **Clone Validation** - Test duplicate name validation for cloned templates

---

## 📞 Support

For questions or issues with the API:
1. Check the error response messages
2. Verify authentication and permissions
3. Ensure request body matches the expected format
4. Contact the backend team for technical support

---

**Happy Coding! 🚀**
