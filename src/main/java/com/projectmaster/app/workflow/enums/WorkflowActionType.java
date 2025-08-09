package com.projectmaster.app.workflow.enums;

public enum WorkflowActionType {
    START_STAGE,
    COMPLETE_STAGE,
    START_TASK,
    COMPLETE_TASK,
    START_STEP,
    COMPLETE_STEP,
    PAUSE_STAGE,
    RESUME_STAGE,
    PAUSE_TASK,
    RESUME_TASK,
    SKIP_STEP,
    APPROVE_STAGE,
    REJECT_STAGE,
    APPROVE_TASK,
    REJECT_TASK,
    BLOCK_WORKFLOW,
    UNBLOCK_WORKFLOW,
    CANCEL_WORKFLOW
}