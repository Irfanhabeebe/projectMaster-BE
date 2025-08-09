# ProjectMaster React Frontend Specification

## 1. System Overview

**ProjectMaster** is a comprehensive construction project management system with a Spring Boot backend providing REST APIs. This specification outlines the requirements for building a modern React frontend that interfaces with the existing backend system.

### 1.1 Backend System Summary
- **Technology**: Spring Boot 3.5.3 with Java 21
- **Database**: PostgreSQL with comprehensive schema
- **Authentication**: JWT-based with role-based access control
- **API Base URL**: `/api/v1` (configurable)
- **Response Format**: Standardized `ApiResponse<T>` wrapper

### 1.2 User Roles & Permissions
- **ADMIN**: Full system access, user management, company settings
- **PROJECT_MANAGER**: Project creation/management, team assignments, reporting
- **TRADIE**: Task execution, time tracking, document uploads
- **CUSTOMER**: Project visibility, document access, communication

## 2. API Integration Specification

### 2.1 Standard API Response Format
All API responses follow this structure:
```typescript
interface ApiResponse<T> {
  success: boolean
  data: T | null
  message: string
  timestamp: string
  errors: ApiError[]
}

interface ApiError {
  code: string
  field?: string
  message: string
}
```

### 2.2 Authentication Endpoints
```typescript
// Base URL: /auth
interface AuthAPI {
  login(credentials: LoginRequest): Promise<ApiResponse<LoginResponse>>
  refresh(token: RefreshTokenRequest): Promise<ApiResponse<LoginResponse>>
  logout(): Promise<ApiResponse<void>>
  getCurrentUser(): Promise<ApiResponse<string>>
}

interface LoginRequest {
  email: string
  password: string
}

interface LoginResponse {
  token: string
  refreshToken: string
  expiresIn: number
  user: {
    id: string
    email: string
    firstName: string
    lastName: string
    role: 'ADMIN' | 'PROJECT_MANAGER' | 'TRADIE' | 'CUSTOMER'
    companyId: string
  }
}

interface RefreshTokenRequest {
  refreshToken: string
}
```

### 2.3 Project Management API
```typescript
// Base URL: /api/projects
interface ProjectAPI {
  // CRUD Operations
  createProject(request: CreateProjectRequest): Promise<ApiResponse<ProjectDto>>
  getProject(id: string): Promise<ApiResponse<ProjectDto>>
  getProjects(params: PaginationParams): Promise<ApiResponse<Page<ProjectDto>>>
  updateProject(id: string, request: UpdateProjectRequest): Promise<ApiResponse<ProjectDto>>
  deleteProject(id: string): Promise<ApiResponse<void>>
  
  // Filtering & Search
  searchProjects(searchTerm: string, params: PaginationParams): Promise<ApiResponse<Page<ProjectDto>>>
  getProjectsByStatus(status: ProjectStatus, params: PaginationParams): Promise<ApiResponse<Page<ProjectDto>>>
  getOverdueProjects(): Promise<ApiResponse<ProjectDto[]>>
  getProjectStatistics(): Promise<ApiResponse<ProjectStatistics>>
}

interface ProjectDto {
  id: string
  projectNumber: string
  name: string
  description: string
  status: 'PLANNING' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED'
  address: string
  budget: number
  startDate: string
  expectedEndDate: string
  actualEndDate?: string
  progressPercentage: number
  customer: {
    id: string
    firstName: string
    lastName: string
    email: string
    phone: string
  }
  workflowTemplate: {
    id: string
    name: string
  }
  assignedUsers: Array<{
    userId: string
    firstName: string
    lastName: string
    role: string
  }>
  stages: ProjectStageDto[]
  createdAt: string
  updatedAt: string
}

interface CreateProjectRequest {
  name: string
  description: string
  address: string
  customerId: string
  workflowTemplateId: string
  budget: number
  startDate: string
  expectedEndDate: string
}

interface UpdateProjectRequest {
  name?: string
  description?: string
  address?: string
  budget?: number
  startDate?: string
  expectedEndDate?: string
  status?: ProjectStatus
}

interface ProjectStatistics {
  totalProjects: number
  activeProjects: number
  completedProjects: number
  overdueProjects: number
  totalBudget: number
  completedBudget: number
}
```

### 2.4 Task Management API
```typescript
// Base URL: /api/tasks
interface TaskAPI {
  createTask(request: CreateTaskRequest): Promise<ApiResponse<TaskDto>>
  getTask(id: string): Promise<ApiResponse<TaskDto>>
  getTasksByProject(projectId: string, params: PaginationParams): Promise<ApiResponse<Page<TaskDto>>>
  getTasksByProjectStep(stepId: string, params: PaginationParams): Promise<ApiResponse<Page<TaskDto>>>
  getMyTasks(params: PaginationParams): Promise<ApiResponse<Page<TaskDto>>>
  getMyActiveTasks(): Promise<ApiResponse<TaskDto[]>>
  updateTask(id: string, request: UpdateTaskRequest): Promise<ApiResponse<TaskDto>>
  deleteTask(id: string): Promise<ApiResponse<void>>
  
  // Filtering
  getTasksByStatus(status: TaskStatus, params: PaginationParams): Promise<ApiResponse<Page<TaskDto>>>
  getTasksByPriority(priority: TaskPriority, params: PaginationParams): Promise<ApiResponse<Page<TaskDto>>>
  getOverdueTasks(): Promise<ApiResponse<TaskDto[]>>
  getHighPriorityTasks(): Promise<ApiResponse<TaskDto[]>>
  getTasksRequiringAttention(): Promise<ApiResponse<TaskDto[]>>
  getTaskStatistics(projectStepId: string): Promise<ApiResponse<TaskStatistics>>
}

interface TaskDto {
  id: string
  title: string
  description: string
  status: 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
  dueDate: string
  estimatedHours: number
  actualHours: number
  completionPercentage: number
  project: {
    id: string
    name: string
    projectNumber: string
  }
  assignedTo: Array<{
    userId: string
    firstName: string
    lastName: string
    role: string
  }>
  createdAt: string
  updatedAt: string
}

interface CreateTaskRequest {
  projectStepId: string
  title: string
  description: string
  priority: TaskPriority
  dueDate: string
  estimatedHours: number
  assignedUsers: Array<{
    userId: string
    role: string
  }>
}

interface UpdateTaskRequest {
  title?: string
  description?: string
  priority?: TaskPriority
  status?: TaskStatus
  dueDate?: string
  estimatedHours?: number
  completionPercentage?: number
}
```

### 2.5 Document Management API
```typescript
// Base URL: /api/documents
interface DocumentAPI {
  uploadDocument(request: FormData): Promise<ApiResponse<DocumentDto>>
  downloadDocument(id: string): Promise<Blob>
  getDocument(id: string): Promise<ApiResponse<DocumentDto>>
  updateDocument(id: string, request: DocumentUpdateRequest): Promise<ApiResponse<DocumentDto>>
  deleteDocument(id: string): Promise<ApiResponse<string>>
  
  searchDocuments(request: DocumentSearchRequest): Promise<ApiResponse<Page<DocumentDto>>>
  getDocumentsByProject(projectId: string): Promise<ApiResponse<DocumentDto[]>>
  getDocumentsByTask(taskId: string): Promise<ApiResponse<DocumentDto[]>>
  streamDocument(id: string): Promise<Blob>
  getDocumentThumbnail(id: string, size?: number): Promise<Blob>
}

interface DocumentDto {
  id: string
  filename: string
  originalFilename: string
  documentType: 'IMAGE' | 'PDF' | 'DOCUMENT' | 'SPREADSHEET' | 'VIDEO' | 'OTHER'
  fileSize: number
  description: string
  tags: string[]
  isPublic: boolean
  uploadedBy: {
    id: string
    firstName: string
    lastName: string
  }
  createdAt: string
}

interface DocumentUploadRequest {
  file: File
  description?: string
  tags?: string[]
  isPublic?: boolean
  projectId?: string
  taskId?: string
}

interface DocumentUpdateRequest {
  description?: string
  tags?: string[]
  isPublic?: boolean
}

interface DocumentSearchRequest {
  searchTerm?: string
  documentType?: string
  tags?: string[]
  projectId?: string
  taskId?: string
  uploadedBy?: string
  dateFrom?: string
  dateTo?: string
  page?: number
  size?: number
}
```

### 2.6 Invoice Management API
```typescript
// Base URL: /api/invoices
interface InvoiceAPI {
  createInvoice(request: CreateInvoiceRequest): Promise<ApiResponse<InvoiceDto>>
  getInvoice(id: string): Promise<ApiResponse<InvoiceDto>>
  getInvoices(params: PaginationParams): Promise<ApiResponse<Page<InvoiceDto>>>
  updateInvoice(id: string, request: UpdateInvoiceRequest): Promise<ApiResponse<InvoiceDto>>
  deleteInvoice(id: string): Promise<ApiResponse<void>>
  
  searchInvoices(request: InvoiceSearchRequest, params: PaginationParams): Promise<ApiResponse<Page<InvoiceDto>>>
  sendInvoice(id: string): Promise<ApiResponse<InvoiceDto>>
  addPayment(request: CreatePaymentRequest): Promise<ApiResponse<PaymentDto>>
  getOverdueInvoices(): Promise<ApiResponse<InvoiceDto[]>>
  getInvoiceStatistics(): Promise<ApiResponse<InvoiceStatistics>>
}

interface InvoiceDto {
  id: string
  invoiceNumber: string
  issueDate: string
  dueDate: string
  subtotal: number
  taxAmount: number
  totalAmount: number
  status: 'DRAFT' | 'SENT' | 'PAID' | 'OVERDUE' | 'CANCELLED'
  notes: string
  project: {
    id: string
    name: string
    projectNumber: string
  }
  lineItems: InvoiceLineItemDto[]
  payments: PaymentDto[]
  createdAt: string
  updatedAt: string
}

interface InvoiceLineItemDto {
  id: string
  description: string
  quantity: number
  unitPrice: number
  lineTotal: number
}

interface PaymentDto {
  id: string
  amount: number
  paymentDate: string
  paymentMethod: 'CASH' | 'CHEQUE' | 'BANK_TRANSFER' | 'CREDIT_CARD'
  referenceNumber?: string
  notes?: string
  createdAt: string
}

interface CreateInvoiceRequest {
  projectId: string
  issueDate: string
  dueDate: string
  lineItems: Array<{
    description: string
    quantity: number
    unitPrice: number
  }>
  notes?: string
}

interface CreatePaymentRequest {
  invoiceId: string
  amount: number
  paymentDate: string
  paymentMethod: string
  referenceNumber?: string
  notes?: string
}
```

### 2.7 User Management API
```typescript
// Base URL: /users
interface UserAPI {
  createUser(request: CreateUserRequest): Promise<ApiResponse<UserDto>>
  getUserById(id: string): Promise<ApiResponse<UserDto>>
  getUserByEmail(email: string): Promise<ApiResponse<UserDto>>
  getUsersByCompany(companyId: string, params: PaginationParams): Promise<ApiResponse<Page<UserDto>>>
  getActiveUsersByCompany(companyId: string): Promise<ApiResponse<UserDto[]>>
  updateUser(id: string, request: CreateUserRequest): Promise<ApiResponse<UserDto>>
  deactivateUser(id: string): Promise<ApiResponse<void>>
  activateUser(id: string): Promise<ApiResponse<void>>
}

interface UserDto {
  id: string
  email: string
  firstName: string
  lastName: string
  phone?: string
  role: UserRole
  active: boolean
  emailVerified: boolean
  lastLoginAt?: string
  company: {
    id: string
    name: string
  }
  createdAt: string
  updatedAt: string
}

interface CreateUserRequest {
  email: string
  firstName: string
  lastName: string
  phone?: string
  role: UserRole
  password: string
  companyId: string
}
```

### 2.8 Company Management API
```typescript
// Base URL: /companies
interface CompanyAPI {
  createCompany(request: CompanyDto): Promise<ApiResponse<CompanyDto>>
  getCompanyById(id: string): Promise<ApiResponse<CompanyDto>>
  getAllActiveCompanies(): Promise<ApiResponse<CompanyDto[]>>
  searchCompanies(query: string): Promise<ApiResponse<CompanyDto[]>>
  updateCompany(id: string, request: CompanyDto): Promise<ApiResponse<CompanyDto>>
  deactivateCompany(id: string): Promise<ApiResponse<void>>
  activateCompany(id: string): Promise<ApiResponse<void>>
}

interface CompanyDto {
  id: string
  name: string
  address?: string
  phone?: string
  email?: string
  website?: string
  taxNumber?: string
  active: boolean
  createdAt: string
  updatedAt: string
}
```

### 2.9 Workflow Management API
```typescript
// Base URL: /api/v1/workflow
interface WorkflowAPI {
  // Stage Management
  startStage(projectId: string, stageId: string): Promise<ApiResponse<WorkflowExecutionResult>>
  completeStage(projectId: string, stageId: string): Promise<ApiResponse<WorkflowExecutionResult>>
  
  // Step Management
  startStep(projectId: string, stepId: string): Promise<ApiResponse<WorkflowExecutionResult>>
  completeStep(projectId: string, stepId: string): Promise<ApiResponse<WorkflowExecutionResult>>
  
  // Workflow Analysis
  getAvailableTransitions(projectId: string): Promise<ApiResponse<AvailableTransition[]>>
  canStartStage(projectId: string, stageId: string): Promise<ApiResponse<boolean>>
}

interface WorkflowExecutionResult {
  success: boolean
  message: string
  projectId: string
  stageId?: string
  stepId?: string
  nextAvailableActions: string[]
  warnings: string[]
}

interface AvailableTransition {
  id: string
  name: string
  type: 'STAGE' | 'STEP'
  canExecute: boolean
  requirements: string[]
  estimatedDuration?: number
}
```

### 2.10 Task Management API (Advanced Features)
```typescript
// Base URL: /api/task-management
interface TaskManagementAPI {
  // Task Assignment
  assignTask(request: AssignTaskRequest): Promise<ApiResponse<TaskDto>>
  unassignTask(taskId: string): Promise<ApiResponse<TaskDto>>
  
  // Time Tracking
  startTimeTracking(request: StartTimeEntryRequest): Promise<ApiResponse<TaskTimeEntryDto>>
  stopTimeTracking(taskId: string): Promise<ApiResponse<TaskTimeEntryDto>>
  
  // Task Comments
  addComment(request: AddCommentRequest): Promise<ApiResponse<TaskCommentDto>>
  
  // Task Status Management
  blockTask(taskId: string, reason: string): Promise<ApiResponse<TaskDto>>
  unblockTask(taskId: string): Promise<ApiResponse<TaskDto>>
  updateProgress(taskId: string, completionPercentage: number): Promise<ApiResponse<TaskDto>>
}

interface AssignTaskRequest {
  taskId: string
  assigneeId: string
  role?: string
}

interface StartTimeEntryRequest {
  taskId: string
  description?: string
}

interface TaskTimeEntryDto {
  id: string
  taskId: string
  userId: string
  startTime: string
  endTime?: string
  durationMinutes?: number
  description?: string
  createdAt: string
}

interface AddCommentRequest {
  taskId: string
  comment: string
  isInternal?: boolean
}

interface TaskCommentDto {
  id: string
  taskId: string
  userId: string
  comment: string
  isInternal: boolean
  author: {
    id: string
    firstName: string
    lastName: string
  }
  createdAt: string
}
```

### 2.11 Notification API
```typescript
// Base URL: /api/notifications
interface NotificationAPI {
  getUserNotifications(params: PaginationParams): Promise<ApiResponse<Page<TaskNotification>>>
  getUnreadNotifications(params: PaginationParams): Promise<ApiResponse<Page<TaskNotification>>>
  getUnreadNotificationCount(): Promise<ApiResponse<number>>
  markAsRead(notificationId: string): Promise<ApiResponse<void>>
  markAllAsRead(): Promise<ApiResponse<void>>
  deleteNotification(notificationId: string): Promise<ApiResponse<void>>
}

interface TaskNotification {
  id: string
  userId: string
  title: string
  message: string
  type: 'TASK_ASSIGNED' | 'TASK_COMPLETED' | 'TASK_OVERDUE' | 'STAGE_COMPLETED' | 'PROJECT_UPDATED'
  isRead: boolean
  relatedEntityId?: string
  relatedEntityType?: 'TASK' | 'PROJECT' | 'STAGE'
  createdAt: string
  readAt?: string
}
```

### 2.12 Task Reporting API
```typescript
// Base URL: /api/task-reporting
interface TaskReportingAPI {
  getProjectTaskStatistics(projectId: string): Promise<ApiResponse<ProjectTaskStatistics>>
  getUserProductivityStatistics(userId: string, startDate: string, endDate: string): Promise<ApiResponse<UserProductivityStatistics>>
  getTimeTrackingReport(projectId: string, startDate: string, endDate: string): Promise<ApiResponse<TimeTrackingReport>>
  getTaskCompletionTrends(projectId: string, days?: number): Promise<ApiResponse<TaskCompletionTrends>>
  getTeamPerformanceMetrics(projectId: string): Promise<ApiResponse<TeamPerformanceMetrics>>
}

interface ProjectTaskStatistics {
  totalTasks: number
  completedTasks: number
  inProgressTasks: number
  overdueTasks: number
  averageCompletionTime: number
  tasksByPriority: Record<TaskPriority, number>
  tasksByStatus: Record<TaskStatus, number>
}

interface UserProductivityStatistics {
  userId: string
  userName: string
  totalTasksCompleted: number
  totalHoursWorked: number
  averageTaskCompletionTime: number
  productivityScore: number
  tasksCompletedOnTime: number
  tasksCompletedLate: number
}

interface TimeTrackingReport {
  projectId: string
  projectName: string
  totalHoursTracked: number
  totalEstimatedHours: number
  efficiencyPercentage: number
  userTimeBreakdown: Array<{
    userId: string
    userName: string
    hoursWorked: number
    tasksCompleted: number
  }>
  dailyTimeBreakdown: Array<{
    date: string
    hoursWorked: number
    tasksCompleted: number
  }>
}

interface TaskCompletionTrends {
  projectId: string
  periodDays: number
  dailyCompletions: Array<{
    date: string
    tasksCompleted: number
    tasksCreated: number
  }>
  weeklyAverages: {
    completionsPerWeek: number
    creationsPerWeek: number
  }
  trends: {
    completionTrend: 'INCREASING' | 'DECREASING' | 'STABLE'
    velocityChange: number
  }
}

interface TeamPerformanceMetrics {
  projectId: string
  teamSize: number
  totalTasksAssigned: number
  totalTasksCompleted: number
  averageTasksPerUser: number
  topPerformers: Array<{
    userId: string
    userName: string
    tasksCompleted: number
    completionRate: number
  }>
  bottlenecks: Array<{
    userId: string
    userName: string
    overdueTasks: number
    averageDelayDays: number
  }>
}
```

### 2.13 Common Types
```typescript
interface PaginationParams {
  page?: number
  size?: number
  sort?: string
  direction?: 'ASC' | 'DESC'
}

interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

type ProjectStatus = 'PLANNING' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED' | 'CANCELLED'
type TaskStatus = 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
type UserRole = 'ADMIN' | 'PROJECT_MANAGER' | 'TRADIE' | 'CUSTOMER'
```

## 3. UI/UX Requirements

### 3.1 Design System Requirements
- **Modern, Clean Interface**: Material Design or similar design system
- **Responsive Design**: Mobile-first approach, works on tablets and desktops
- **Accessibility**: WCAG 2.1 AA compliance
- **Dark/Light Mode**: User preference support
- **Color Scheme**: Professional construction industry appropriate colors
- **Typography**: Clear, readable fonts suitable for data-heavy interfaces

### 3.2 Core Application Structure
```
src/
├── components/           # Reusable UI components
│   ├── common/          # Generic components (buttons, forms, etc.)
│   ├── layout/          # Layout components (header, sidebar, etc.)
│   └── domain/          # Domain-specific components
├── pages/               # Page components
│   ├── auth/           # Authentication pages
│   ├── dashboard/      # Dashboard and overview
│   ├── projects/       # Project management pages
│   ├── tasks/          # Task management pages
│   ├── documents/      # Document management pages
│   ├── invoices/       # Invoice management pages
│   └── settings/       # Settings and configuration
├── hooks/              # Custom React hooks
├── services/           # API service layer
├── store/              # State management (Redux/Zustand)
├── utils/              # Utility functions
├── types/              # TypeScript type definitions
└── constants/          # Application constants
```

### 3.3 Key Pages and Features

#### 3.3.1 Authentication Pages
- **Login Page**: 
  - Email/password form with validation
  - "Remember Me" checkbox
  - "Forgot Password" link
  - Company logo and branding
  - Loading states and error handling

- **Password Reset**: 
  - Email-based password recovery
  - Reset confirmation page
  - New password form with strength indicator

- **Session Management**: 
  - Auto-refresh JWT tokens
  - Logout on token expiry
  - Session timeout warnings

#### 3.3.2 Dashboard (Role-based)

**Admin Dashboard:**
- System overview metrics
- User management quick actions
- Company settings access
- System health indicators

**Project Manager Dashboard:**
- Active projects overview
- Team workload distribution
- Financial summaries
- Overdue tasks and projects
- Recent project activities

**Tradie Dashboard:**
- My assigned tasks
- Today's schedule
- Time tracking interface
- Recent document uploads
- Task completion progress

**Customer Dashboard:**
- My projects overview
- Project progress visualization
- Recent updates and communications
- Document access
- Invoice and payment status

#### 3.3.3 Project Management

**Project List Page:**
- Filterable project grid/list view
- Search functionality
- Status-based filtering
- Sorting options (date, status, budget)
- Pagination
- Quick actions (view, edit, delete)

**Project Details Page:**
- Comprehensive project overview with tabs:
  - **Overview**: Basic info, progress, team, key metrics
  - **Stages & Steps**: Workflow visualization with progress indicators
  - **Tasks**: Integrated task management interface
  - **Documents**: File management with preview capabilities
  - **Invoices**: Financial tracking and payment history
  - **Timeline**: Gantt chart or timeline view
  - **Team**: Team member assignments and roles

**Project Creation Wizard:**
- Step-by-step project setup
- Customer selection/creation
- Workflow template selection
- Budget and timeline configuration
- Team assignment
- Initial document upload

#### 3.3.4 Task Management

**Task Board (Kanban):**
- Drag-and-drop task management
- Status columns (Open, In Progress, Completed)
- Priority indicators
- Due date warnings
- Quick task creation
- Filtering by assignee, priority, project

**Task List View:**
- Detailed list with sorting and filtering
- Bulk actions (assign, update status)
- Export functionality
- Advanced search

**Task Details Modal/Page:**
- Comprehensive task information
- Time tracking interface
- File attachments
- Comments and activity log
- Subtask management
- Dependency visualization

#### 3.3.5 Document Management

**Document Library:**
- Grid and list view options
- File type filtering
- Search by name, tags, content
- Folder organization by project/task
- Bulk upload and actions
- Preview functionality

**Document Viewer:**
- In-browser preview for images, PDFs
- Download and share options
- Version history
- Comments and annotations
- Metadata editing

#### 3.3.6 Invoice Management

**Invoice List:**
- Status-based filtering
- Search by invoice number, customer
- Due date sorting
- Payment status indicators
- Bulk actions

**Invoice Details:**
- Comprehensive invoice view
- Payment history
- PDF generation and download
- Send invoice functionality
- Payment recording

**Invoice Creation:**
- Project-based invoice generation
- Line item management
- Tax calculations
- Template support
- Preview before sending

### 3.4 Mobile Responsiveness Requirements

**Breakpoints:**
- Mobile: 320px - 768px
- Tablet: 768px - 1024px
- Desktop: 1024px+

**Mobile-Specific Features:**
- Touch-friendly interface elements
- Swipe gestures for navigation
- Optimized forms for mobile input
- Camera integration for document capture
- Offline capability for basic functions

### 3.5 Performance Requirements

- **Initial Load Time**: < 3 seconds
- **Page Transitions**: < 500ms
- **API Response Handling**: Loading states, error boundaries
- **Image Optimization**: Lazy loading, compression
- **Bundle Size**: Code splitting, tree shaking
- **Caching Strategy**: API response caching, asset caching

## 4. Technical Implementation Requirements

### 4.1 Recommended Technology Stack

**Core Framework:**
- React 18+ with TypeScript
- Next.js (for SSR/SSG capabilities) or Vite (for SPA)

**State Management:**
- Redux Toolkit or Zustand for global state
- React Query/TanStack Query for server state
- React Hook Form for form state

**UI Framework:**
- Material-UI (MUI) or Ant Design
- Tailwind CSS for custom styling
- Framer Motion for animations

**Development Tools:**
- ESLint + Prettier for code quality
- Husky for git hooks
- Jest + React Testing Library for testing
- Storybook for component development

### 4.2 API Service Layer

```typescript
// services/api.ts
class ApiService {
  private baseURL: string
  private token: string | null = null

  constructor(baseURL: string) {
    this.baseURL = baseURL
  }

  setToken(token: string) {
    this.token = token
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    const url = `${this.baseURL}${endpoint}`
    const headers = {
      'Content-Type': 'application/json',
      ...(this.token && { Authorization: `Bearer ${this.token}` }),
      ...options.headers,
    }

    const response = await fetch(url, { ...options, headers })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return response.json()
  }

  // CRUD methods
  async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint)
  }

  async post<T>(endpoint: string, data: any): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    })
  }

  async put<T>(endpoint: string, data: any): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    })
  }

  async delete<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>(endpoint, { method: 'DELETE' })
  }
}
```

### 4.3 Authentication Implementation

```typescript
// hooks/useAuth.ts
interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isLoading: boolean
}

export const useAuth = () => {
  const [state, setState] = useState<AuthState>({
    user: null,
    token: null,
    isAuthenticated: false,
    isLoading: true,
  })

  const login = async (credentials: LoginRequest) => {
    try {
      const response = await authAPI.login(credentials)
      if (response.success) {
        const { token, user } = response.data
        localStorage.setItem('token', token)
        setState({
          user,
          token,
          isAuthenticated: true,
          isLoading: false,
        })
      }
    } catch (error) {
      // Handle error
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    setState({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
    })
  }

  return { ...state, login, logout }
}
```

### 4.4 Error Handling Strategy

```typescript
// components/ErrorBoundary.tsx
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo)
    // Log to error reporting service
  }

  render() {
    if (this.state.hasError) {
      return <ErrorFallback error={this.state.error} />
    }

    return this.props.children
  }
}

// Global error handling for API calls
const handleApiError = (error: any) => {
  if (error.response?.status === 401) {
    // Redirect to login
    window.location.href = '/login'
  } else if (error.response?.status >= 500) {
    // Show server error message
    toast.error('Server error. Please try again later.')
  } else {
    // Show specific error message
    toast.error(error.message || 'An error occurred')
  }
}
```

## 5. Security Considerations

### 5.1 Authentication & Authorization
- JWT token storage in httpOnly cookies (preferred) or localStorage
- Automatic token refresh before expiry
- Role-based route protection
- API endpoint authorization checks

### 5.2 Data Protection
- Input validation and sanitization
- XSS prevention
- CSRF protection
- Secure file upload handling
- Sensitive data masking in logs

### 5.3 API Security
- Request/response interceptors for token management
- Rate limiting on client side
- Secure headers implementation
- Environment variable management

## 6. Testing Strategy

### 6.1 Unit Testing
- Component testing with React Testing Library
- Hook testing
- Utility function testing
- API service testing

### 6.2 Integration Testing
- User flow testing
- API integration testing
- Form submission testing
- Authentication flow testing

### 6.3 E2E Testing
- Critical user journeys
- Cross-browser compatibility
- Mobile responsiveness testing
- Performance testing

## 7. Deployment and DevOps

### 7.1 Build Configuration
- Environment-specific builds
- Asset optimization
- Bundle analysis
- Source map generation for debugging

### 7.2 Deployment Strategy
- Static hosting (Netlify, Vercel) for SPA
- Docker containerization for enterprise deployment
- CDN configuration for assets
- Environment variable management

### 7.3 Monitoring and Analytics
- Error tracking (Sentry, Bugsnag)
- Performance monitoring
- User analytics
- API usage tracking

## 8. Development Guidelines

### 8.1 Code Standards
- TypeScript strict mode
- ESLint configuration with React rules
- Prettier for code formatting
- Conventional commit messages

### 8.2 Component Guidelines
- Functional components with hooks
- Props interface definitions
- Default props and prop validation
- Component composition over inheritance

### 8.3 State Management
- Local state for component-specific data
- Global state for shared application data
- Server state management with React Query
- Form state with React Hook Form

### 8.4 Performance Optimization
- React.memo for expensive components
- useMemo and useCallback for expensive calculations
- Code splitting with React.lazy
- Image optimization and lazy loading

## 9. Accessibility Requirements

### 9.1 WCAG 2.1 AA Compliance
- Keyboard navigation support
- Screen reader compatibility
- Color contrast requirements
- Focus management
- Alternative text for images

### 9.2 Semantic HTML
- Proper heading hierarchy
- Form labels and descriptions
- ARIA attributes where needed
- Landmark roles for navigation

## 10. Browser Support

### 10.1 Supported Browsers
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+
- Mobile Safari (iOS 14+)
- Chrome Mobile (Android 10+)

### 10.2 Progressive Enhancement
- Core functionality without JavaScript
- Graceful degradation for older browsers
- Feature detection over browser detection

## 11. Future Considerations

### 11.1 Scalability
- Micro-frontend architecture consideration
- Component library extraction
- Multi-tenant support
- Internationalization (i18n) preparation

### 11.2 Advanced Features
- Real-time notifications with WebSockets
- Offline functionality with service workers
- Push notifications
- Advanced reporting and analytics
- Mobile app development (React Native)

## 12. Getting Started Checklist

### 12.1 Project Setup
- [ ] Initialize React project with TypeScript
- [ ] Configure build tools and development environment
- [ ] Set up linting and formatting
- [ ] Configure testing framework
- [ ] Set up state management
- [ ] Implement authentication system

### 12.2 Core Implementation
- [ ] Create API service layer
- [ ] Implement routing and navigation
- [ ] Build authentication pages
- [ ] Create dashboard layouts
- [ ] Implement project management features
- [ ] Build task management interface
- [ ] Add document management
- [ ] Implement invoice management

### 12.3 Polish and Deployment
- [ ] Add error handling and loading states
- [ ] Implement responsive design
- [ ] Add accessibility features
- [ ] Performance optimization
- [ ] Testing implementation
- [ ] Deployment configuration

This specification provides a comprehensive foundation for building a modern, scalable React frontend for the ProjectMaster construction management system. The implementation should prioritize user experience, performance, and maintainability while ensuring seamless integration with the existing Spring Boot backend.