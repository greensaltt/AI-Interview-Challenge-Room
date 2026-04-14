package com.offerdungeon.common.controller;

import com.offerdungeon.common.config.AsyncTaskProperties;
import com.offerdungeon.common.model.AsyncTaskStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.async-task.status-base-path:/api/tasks}")
public class TaskStatusController {

    private final AsyncTaskProperties asyncTaskProperties;

    public TaskStatusController(AsyncTaskProperties asyncTaskProperties) {
        this.asyncTaskProperties = asyncTaskProperties;
    }

    @GetMapping("/{taskId}")
    public AsyncTaskStatusResponse getTaskStatus(@PathVariable String taskId) {
        return new AsyncTaskStatusResponse(
                taskId,
                "PENDING",
                0,
                "Async task contract reserved for future workflows.",
                asyncTaskProperties.getPollInterval().toMillis(),
                null,
                null);
    }
}
