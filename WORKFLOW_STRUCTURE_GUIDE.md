# Australian Residential Construction Workflow Structure

## Overview

This document outlines the comprehensive workflow structure for Australian residential construction projects, designed specifically for construction companies managing single-story and double-story residential builds.

## Workflow Hierarchy

The system uses a **3-layer hierarchy**:
- **Stages** → **Tasks** → **Steps**

### 1. Stages (High-Level Project Phases)
Each stage represents a major phase of construction:
- Site Preparation & Foundation
- Frame & Roof Structure  
- External Works
- Internal Works
- Final Finishes & Handover

### 2. Tasks (Specialized Work Areas)
Tasks within each stage represent specific work areas:
- Site Survey & Marking
- Excavation
- Foundation Installation
- Wall Frame Construction
- Roof Truss Installation
- Brickwork
- Plumbing Rough-in
- Electrical Rough-in
- etc.

### 3. Steps (Individual Tradie Tasks)
Steps represent individual tasks that can be assigned to a single skilled worker:
- Site Boundary Survey
- Foundation Trenches
- Concrete Pouring
- Wall Studs Installation
- Bathroom Tiling
- Kitchen Cabinet Installation
- etc.

## Workflow Templates

### Single Story Construction
- **Duration**: ~112 days (16 weeks)
- **Stages**: 5 major phases
- **Tasks**: 25 specialized work areas
- **Steps**: 85+ individual tradie tasks

### Double Story Construction  
- **Duration**: ~196 days (28 weeks)
- **Stages**: 8 major phases
- **Tasks**: 35+ specialized work areas
- **Steps**: 120+ individual tradie tasks

## Key Features

### 1. Australian Building Standards Compliance
- Follows Australian Building Codes
- Includes all required inspections and approvals
- Covers both single and double story construction
- Addresses local climate and material requirements

### 2. Tradie-Level Task Granularity
Each step is designed to be:
- **Assignable to a single skilled worker**
- **Measurable in hours** (typically 2-24 hours)
- **Independent enough** to be completed by one person
- **Sequential** with proper dependencies

### 3. Realistic Time Estimates
- Based on Australian construction industry standards
- Accounts for weather delays and site conditions
- Includes buffer time for quality control
- Reflects actual tradie productivity rates

## Step-Level Examples (Tradie Tasks)

### Site Preparation
- **Site Boundary Survey** (4 hours) - Surveyor
- **Vegetation Removal** (8 hours) - Landscaper
- **Foundation Trenches** (12 hours) - Excavator operator

### Foundation Work
- **Reinforcement Installation** (8 hours) - Steel fixer
- **Concrete Pouring** (16 hours) - Concreter
- **Curing & Protection** (8 hours) - Laborer

### Frame Construction
- **Floor Bearers** (8 hours) - Carpenter
- **Wall Studs** (16 hours) - Carpenter
- **Wall Bracing** (8 hours) - Carpenter

### External Works
- **Foundation Bricks** (8 hours) - Bricklayer
- **Wall Bricklaying** (24 hours) - Bricklayer
- **Lintel Installation** (8 hours) - Bricklayer

### Internal Works
- **Water Supply Pipes** (8 hours) - Plumber
- **Drainage Pipes** (16 hours) - Plumber
- **Electrical Wiring** (16 hours) - Electrician

### Finishes
- **Bathroom Tiling** (16 hours) - Tiler
- **Kitchen Cabinets** (12 hours) - Cabinet maker
- **Internal Painting** (16 hours) - Painter

## Database Structure

### Tables Used
1. **standard_workflow_templates** - Main workflow definitions
2. **standard_workflow_stages** - Major project phases
3. **standard_workflow_tasks** - Specialized work areas
4. **standard_workflow_steps** - Individual tradie tasks

### Key Fields
- **order_index** - Ensures proper sequencing
- **estimated_hours** - For step-level time tracking
- **estimated_duration_days** - For stage-level planning
- **description** - Detailed work instructions

## Usage Instructions

### 1. Running the SQL Scripts
```sql
-- Run the single story workflow
\i australian_construction_workflows.sql

-- Run the double story workflow  
\i double_story_construction_workflows.sql
```

### 2. Workflow Selection
- **Single Story**: Use template ID `a1b2c3d4-e5f6-7890-abcd-ef1234567890`
- **Double Story**: Use template ID `b2c3d4e5-f6a7-8901-bcde-f23456789012`

### 3. Step Assignment
Each step can be assigned to:
- **Individual tradies** (e.g., "John Smith - Bricklayer")
- **Specialized teams** (e.g., "Plumbing Team")
- **Subcontractors** (e.g., "ABC Electrical")

### 4. Progress Tracking
- **Step Level**: Track individual task completion
- **Task Level**: Monitor work area progress
- **Stage Level**: Track major phase completion

## Benefits

### 1. Precise Resource Management
- Know exactly how many hours each step requires
- Assign the right skilled worker to each task
- Track productivity and identify bottlenecks

### 2. Quality Control
- Each step has clear completion criteria
- Inspections can be scheduled at appropriate points
- Defects can be traced to specific steps

### 3. Project Planning
- Realistic timelines based on actual work hours
- Proper sequencing prevents conflicts
- Resource allocation optimization

### 4. Cost Control
- Accurate time estimates for pricing
- Labor cost tracking at step level
- Change order impact assessment

## Integration with Workflow Engine

The workflow engine supports:
- **Step-level actions**: START_STEP, COMPLETE_STEP
- **Task-level actions**: START_TASK, COMPLETE_TASK  
- **Stage-level actions**: START_STAGE, COMPLETE_STAGE
- **Automatic progression**: Steps → Tasks → Stages
- **Validation**: Ensures prerequisites are met
- **Real-time updates**: Live progress tracking

## Customization

### Adding New Steps
```sql
INSERT INTO standard_workflow_steps (
    id, standard_workflow_task_id, name, description, 
    order_index, estimated_hours, created_at, updated_at
) VALUES (
    'unique-uuid-here', 'task-uuid-here', 'Step Name', 
    'Detailed description', 1, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
```

### Modifying Time Estimates
```sql
UPDATE standard_workflow_steps 
SET estimated_hours = 12 
WHERE name = 'Bathroom Tiling';
```

### Adding New Tasks
```sql
INSERT INTO standard_workflow_tasks (
    id, standard_workflow_stage_id, name, description,
    order_index, estimated_hours, created_at, updated_at
) VALUES (
    'unique-uuid-here', 'stage-uuid-here', 'Task Name',
    'Detailed description', 1, 24, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);
```

This workflow structure provides a comprehensive, realistic, and practical framework for managing Australian residential construction projects with proper tradie-level task granularity. 