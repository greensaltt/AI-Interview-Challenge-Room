package com.offerdungeon.common.controller;

import com.offerdungeon.common.config.AsyncTaskProperties;
import com.offerdungeon.common.model.AsyncTaskStatusResponse;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@Validated
@RestController
@RequestMapping("${app.async-task.status-base-path:/api/tasks}")
public class TaskStatusController {

    private final AsyncTaskProperties asyncTaskProperties;

    public TaskStatusController(AsyncTaskProperties asyncTaskProperties) {
        this.asyncTaskProperties = asyncTaskProperties;
    }

    @GetMapping("/{taskId}")
    public AsyncTaskStatusResponse getTaskStatus(
            @PathVariable
                    @Size(min = 4, max = 64, message = "taskId length must be between 4 and 64 characters.")
                    @Pattern(
                            regexp = "^[A-Za-z0-9_-]+$",
                            message = "taskId may only contain letters, numbers, underscores, and hyphens.")
                    String taskId) {
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
