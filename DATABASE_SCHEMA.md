# ProjectMaster Database Schema Design

## 1. Database Configuration

### 1.1 PostgreSQL Version
- **Minimum Version**: PostgreSQL 14+
- **Recommended Version**: PostgreSQL 15+
- **Extensions**: uuid-ossp, pg_trgm, pg_stat_statements

### 1.2 Database Setup Script

```sql
-- Create database
CREATE DATABASE projectmaster_db;

-- Connect to database
\c projectmaster_db;

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS reporting;
```

## 2. Core Tables

### 2.1 Company Management

```sql
-- Companies table
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    address TEXT,
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(255),
    tax_number VARCHAR(100),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for companies
CREATE INDEX idx_companies_name ON companies USING gin(name gin_trgm_ops);
CREATE INDEX idx_companies_active ON companies(active);
```

### 2.2 User Management

```sql
-- User roles enum
CREATE TYPE user_role AS ENUM ('ADMIN', 'PROJECT_MANAGER', 'TRADIE', 'CUSTOMER');

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    role user_role NOT NULL,
    active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for users
CREATE INDEX idx_users_company_id ON users(company_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_users_name ON users USING gin((first_name || ' ' || last_name) gin_trgm_ops);
```

### 2.3 Customer Management

```sql
-- Customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    secondary_contact_name VARCHAR(200),
    secondary_contact_phone VARCHAR(50),
    notes TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for customers
CREATE INDEX idx_customers_company_id ON customers(company_id);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_name ON customers USING gin((first_name || ' ' || last_name) gin_trgm_ops);
CREATE INDEX idx_customers_active ON customers(active);
```

### 2.4 Workflow Templates

```sql
-- Workflow templates table
CREATE TABLE workflow_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100), -- e.g., 'RESIDENTIAL', 'COMMERCIAL', 'RENOVATION'
    active BOOLEAN DEFAULT true,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Workflow stages table
CREATE TABLE workflow_stages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_template_id UUID NOT NULL REFERENCES workflow_templates(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    parallel_execution BOOLEAN DEFAULT false,
    required_approvals INTEGER DEFAULT 0,
    estimated_duration_days INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Workflow steps table
CREATE TABLE workflow_steps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_stage_id UUID NOT NULL REFERENCES workflow_stages(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    order_index INTEGER NOT NULL,
    estimated_hours INTEGER,
    required_skills JSONB, -- Array of required skills
    requirements JSONB, -- Custom requirements and conditions
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for workflow tables
CREATE INDEX idx_workflow_templates_company_id ON workflow_templates(company_id);
CREATE INDEX idx_workflow_templates_active ON workflow_templates(active);
CREATE INDEX idx_workflow_stages_template_id ON workflow_stages(workflow_template_id);
CREATE INDEX idx_workflow_stages_order ON workflow_stages(workflow_template_id, order_index);
CREATE INDEX idx_workflow_steps_stage_id ON workflow_steps(workflow_stage_id);
CREATE INDEX idx_workflow_steps_order ON workflow_steps(workflow_stage_id, order_index);
```

### 2.5 Project Management

```sql
-- Project status enum
CREATE TYPE project_status AS ENUM ('PLANNING', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED');

-- Projects table
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    workflow_template_id UUID NOT NULL REFERENCES workflow_templates(id) ON DELETE RESTRICT,
    project_number VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address TEXT NOT NULL,
    budget DECIMAL(15,2),
    start_date DATE,
    expected_end_date DATE,
    actual_end_date DATE,
    status project_status DEFAULT 'PLANNING',
    progress_percentage INTEGER DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Project assignments table (many-to-many between projects and users)
CREATE TABLE project_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL, -- 'MANAGER', 'SUPERVISOR', 'WORKER'
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(project_id, user_id, role)
);

-- Indexes for projects
CREATE INDEX idx_projects_company_id ON projects(company_id);
CREATE INDEX idx_projects_customer_id ON projects(customer_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_dates ON projects(start_date, expected_end_date);
CREATE INDEX idx_projects_number ON projects(project_number);
CREATE INDEX idx_project_assignments_project_id ON project_assignments(project_id);
CREATE INDEX idx_project_assignments_user_id ON project_assignments(user_id);
```

### 2.6 Project Execution

```sql
-- Stage status enum
CREATE TYPE stage_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED', 'SKIPPED');

-- Project stages table
CREATE TABLE project_stages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    workflow_stage_id UUID NOT NULL REFERENCES workflow_stages(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    status stage_status DEFAULT 'NOT_STARTED',
    start_date DATE,
    end_date DATE,
    actual_start_date DATE,
    actual_end_date DATE,
    notes TEXT,
    approvals_received INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Step status enum
CREATE TYPE step_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED', 'BLOCKED');

-- Project steps table
CREATE TABLE project_steps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_stage_id UUID NOT NULL REFERENCES project_stages(id) ON DELETE CASCADE,
    workflow_step_id UUID NOT NULL REFERENCES workflow_steps(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    status step_status DEFAULT 'NOT_STARTED',
    start_date DATE,
    end_date DATE,
    actual_start_date DATE,
    actual_end_date DATE,
    notes TEXT,
    quality_check_passed BOOLEAN,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for project execution
CREATE INDEX idx_project_stages_project_id ON project_stages(project_id);
CREATE INDEX idx_project_stages_status ON project_stages(status);
CREATE INDEX idx_project_steps_stage_id ON project_steps(project_stage_id);
CREATE INDEX idx_project_steps_status ON project_steps(status);
```

### 2.7 Task Management

```sql
-- Task priority enum
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

-- Task status enum
CREATE TYPE task_status AS ENUM ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

-- Tasks table
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_step_id UUID NOT NULL REFERENCES project_steps(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority task_priority DEFAULT 'MEDIUM',
    status task_status DEFAULT 'OPEN',
    due_date DATE,
    estimated_hours INTEGER,
    actual_hours INTEGER DEFAULT 0,
    completion_percentage INTEGER DEFAULT 0 CHECK (completion_percentage >= 0 AND completion_percentage <= 100),
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Task assignments table
CREATE TABLE task_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) DEFAULT 'ASSIGNEE', -- 'ASSIGNEE', 'REVIEWER', 'APPROVER'
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(task_id, user_id, role)
);

-- Time tracking table
CREATE TABLE time_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    duration_minutes INTEGER,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for tasks
CREATE INDEX idx_tasks_project_step_id ON tasks(project_step_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_task_assignments_task_id ON task_assignments(task_id);
CREATE INDEX idx_task_assignments_user_id ON task_assignments(user_id);
CREATE INDEX idx_time_entries_task_id ON time_entries(task_id);
CREATE INDEX idx_time_entries_user_id ON time_entries(user_id);
CREATE INDEX idx_time_entries_date ON time_entries(DATE(start_time));
```

### 2.8 Document Management

```sql
-- Document types enum
CREATE TYPE document_type AS ENUM ('IMAGE', 'PDF', 'DOCUMENT', 'SPREADSHEET', 'VIDEO', 'OTHER');

-- Documents table
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
    uploaded_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    document_type document_type NOT NULL,
    description TEXT,
    tags JSONB,
    is_public BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_document_reference CHECK (
        (project_id IS NOT NULL AND task_id IS NULL) OR 
        (project_id IS NULL AND task_id IS NOT NULL)
    )
);

-- Indexes for documents
CREATE INDEX idx_documents_project_id ON documents(project_id);
CREATE INDEX idx_documents_task_id ON documents(task_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_uploaded_by ON documents(uploaded_by);
CREATE INDEX idx_documents_tags ON documents USING gin(tags);
```

### 2.9 Invoicing and Billing

```sql
-- Invoice status enum
CREATE TYPE invoice_status AS ENUM ('DRAFT', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED');

-- Invoices table
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE RESTRICT,
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    status invoice_status DEFAULT 'DRAFT',
    notes TEXT,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Invoice line items table
CREATE TABLE invoice_line_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 1,
    unit_price DECIMAL(15,2) NOT NULL,
    line_total DECIMAL(15,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE RESTRICT,
    amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(50), -- 'CASH', 'CHEQUE', 'BANK_TRANSFER', 'CREDIT_CARD'
    reference_number VARCHAR(100),
    notes TEXT,
    created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for invoicing
CREATE INDEX idx_invoices_project_id ON invoices(project_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
CREATE INDEX idx_invoices_number ON invoices(invoice_number);
CREATE INDEX idx_invoice_line_items_invoice_id ON invoice_line_items(invoice_id);
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX idx_payments_date ON payments(payment_date);
```

### 2.10 Notifications

```sql
-- Notification types enum
CREATE TYPE notification_type AS ENUM ('EMAIL', 'SMS', 'PUSH', 'IN_APP');

-- Notification status enum
CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'DELIVERED', 'FAILED');

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status notification_status DEFAULT 'PENDING',
    scheduled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT,
    metadata JSONB, -- Additional data like project_id, task_id, etc.
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for notifications
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_scheduled_at ON notifications(scheduled_at);
CREATE INDEX idx_notifications_type ON notifications(type);
```

## 3. Audit Tables

```sql
-- Audit log table
CREATE TABLE audit.audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    table_name VARCHAR(100) NOT NULL,
    record_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL, -- 'INSERT', 'UPDATE', 'DELETE'
    old_values JSONB,
    new_values JSONB,
    changed_by UUID REFERENCES users(id),
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT
);

-- Indexes for audit log
CREATE INDEX idx_audit_log_table_name ON audit.audit_log(table_name);
CREATE INDEX idx_audit_log_record_id ON audit.audit_log(record_id);
CREATE INDEX idx_audit_log_changed_by ON audit.audit_log(changed_by);
CREATE INDEX idx_audit_log_changed_at ON audit.audit_log(changed_at);
```

## 4. Views for Reporting

```sql
-- Project summary view
CREATE VIEW reporting.project_summary AS
SELECT 
    p.id,
    p.project_number,
    p.name,
    p.status,
    p.budget,
    p.start_date,
    p.expected_end_date,
    p.actual_end_date,
    p.progress_percentage,
    c.name as customer_name,
    comp.name as company_name,
    COUNT(DISTINCT ps.id) as total_stages,
    COUNT(DISTINCT CASE WHEN ps.status = 'COMPLETED' THEN ps.id END) as completed_stages,
    COUNT(DISTINCT t.id) as total_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END) as completed_tasks,
    COALESCE(SUM(te.duration_minutes), 0) as total_time_minutes
FROM projects p
JOIN customers c ON p.customer_id = c.id
JOIN companies comp ON p.company_id = comp.id
LEFT JOIN project_stages ps ON p.id = ps.project_id
LEFT JOIN project_steps pst ON ps.id = pst.project_stage_id
LEFT JOIN tasks t ON pst.id = t.project_step_id
LEFT JOIN time_entries te ON t.id = te.task_id
GROUP BY p.id, p.project_number, p.name, p.status, p.budget, 
         p.start_date, p.expected_end_date, p.actual_end_date, 
         p.progress_percentage, c.name, comp.name;

-- User workload view
CREATE VIEW reporting.user_workload AS
SELECT 
    u.id,
    u.first_name || ' ' || u.last_name as full_name,
    u.role,
    COUNT(DISTINCT ta.task_id) as assigned_tasks,
    COUNT(DISTINCT CASE WHEN t.status = 'IN_PROGRESS' THEN ta.task_id END) as active_tasks,
    COALESCE(SUM(t.estimated_hours), 0) as estimated_hours,
    COALESCE(SUM(te.duration_minutes), 0) / 60.0 as actual_hours
FROM users u
LEFT JOIN task_assignments ta ON u.id = ta.user_id
LEFT JOIN tasks t ON ta.task_id = t.id
LEFT JOIN time_entries te ON t.id = te.task_id AND te.user_id = u.id
WHERE u.active = true
GROUP BY u.id, u.first_name, u.last_name, u.role;
```

## 5. Triggers and Functions

```sql
-- Function to update project progress
CREATE OR REPLACE FUNCTION update_project_progress()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE projects 
    SET progress_percentage = (
        SELECT COALESCE(
            ROUND(
                (COUNT(CASE WHEN ps.status = 'COMPLETED' THEN 1 END) * 100.0) / 
                NULLIF(COUNT(*), 0)
            ), 0
        )
        FROM project_stages ps 
        WHERE ps.project_id = COALESCE(NEW.project_id, OLD.project_id)
    ),
    updated_at = CURRENT_TIMESTAMP
    WHERE id = COALESCE(NEW.project_id, OLD.project_id);
    
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Trigger to update project progress when stages change
CREATE TRIGGER trigger_update_project_progress
    AFTER INSERT OR UPDATE OR DELETE ON project_stages
    FOR EACH ROW
    EXECUTE FUNCTION update_project_progress();

-- Function to update timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply updated_at trigger to all relevant tables
CREATE TRIGGER trigger_companies_updated_at BEFORE UPDATE ON companies FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_customers_updated_at BEFORE UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_projects_updated_at BEFORE UPDATE ON projects FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER trigger_tasks_updated_at BEFORE UPDATE ON tasks FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

## 6. Performance Optimization

### 6.1 Partitioning Strategy

```sql
-- Partition audit log by month
CREATE TABLE audit.audit_log_y2025m01 PARTITION OF audit.audit_log
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

-- Create partitions for time_entries by month
CREATE TABLE time_entries_y2025m01 PARTITION OF time_entries
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
```

### 6.2 Additional Indexes for Performance

```sql
-- Composite indexes for common queries
CREATE INDEX idx_projects_company_status ON projects(company_id, status);
CREATE INDEX idx_tasks_assignee_status ON task_assignments(user_id, task_id) 
    WHERE role = 'ASSIGNEE';
CREATE INDEX idx_project_stages_project_status ON project_stages(project_id, status);

-- Partial indexes for active records
CREATE INDEX idx_users_active_company ON users(company_id) WHERE active = true;
CREATE INDEX idx_projects_active ON projects(company_id, status) 
    WHERE status IN ('PLANNING', 'IN_PROGRESS');
```

## 7. Data Constraints and Validation

```sql
-- Add check constraints
ALTER TABLE projects ADD CONSTRAINT chk_project_dates 
    CHECK (start_date <= expected_end_date);

ALTER TABLE project_stages ADD CONSTRAINT chk_stage_dates 
    CHECK (start_date <= end_date);

ALTER TABLE tasks ADD CONSTRAINT chk_task_hours 
    CHECK (estimated_hours >= 0 AND actual_hours >= 0);

ALTER TABLE invoices ADD CONSTRAINT chk_invoice_amounts 
    CHECK (subtotal >= 0 AND tax_amount >= 0 AND total_amount >= 0);
```

## 8. Sample Data Seeding

```sql
-- Insert sample company
INSERT INTO companies (name, address, phone, email) 
VALUES ('ABC Construction', '123 Builder St, Construction City', '+1-555-0123', 'info@abcconstruction.com');

-- Insert sample workflow template
INSERT INTO workflow_templates (company_id, name, description, category, is_default)
SELECT id, 'Standard Residential Build', 'Standard workflow for residential construction', 'RESIDENTIAL', true
FROM companies WHERE name = 'ABC Construction';
```

This database schema provides a robust foundation for your construction management system with proper indexing, constraints, and optimization for PostgreSQL.