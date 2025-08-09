-- Clear existing Australian construction workflow data
-- Run this first to remove any existing data with duplicate UUIDs

DELETE FROM standard_workflow_steps WHERE standard_workflow_task_id IN (
    SELECT id FROM standard_workflow_tasks WHERE standard_workflow_stage_id IN (
        SELECT id FROM standard_workflow_stages WHERE standard_workflow_template_id IN (
            'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
            'b2c3d4e5-f6a7-8901-bcde-f23456789012'
        )
    )
);

DELETE FROM standard_workflow_tasks WHERE standard_workflow_stage_id IN (
    SELECT id FROM standard_workflow_stages WHERE standard_workflow_template_id IN (
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'b2c3d4e5-f6a7-8901-bcde-f23456789012'
    )
);

DELETE FROM standard_workflow_stages WHERE standard_workflow_template_id IN (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'b2c3d4e5-f6a7-8901-bcde-f23456789012'
);

DELETE FROM standard_workflow_templates WHERE id IN (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'b2c3d4e5-f6a7-8901-bcde-f23456789012'
);

-- Now you can run the australian_construction_workflows.sql file again
-- The duplicate UUIDs have been fixed in the updated file 