package com.offerdungeon.common.model;

public record AsyncTaskStatusResponse(
        String taskId,
        String status,
        int progress,
        String message,
        long pollAfterMillis,
        String errorCode,
        Object result) {}
