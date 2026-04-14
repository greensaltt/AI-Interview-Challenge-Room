package com.offerdungeon.common.controller;

import com.offerdungeon.common.config.AsyncTaskProperties;
import com.offerdungeon.common.service.InfrastructureProbeService;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final AsyncTaskProperties asyncTaskProperties;
    private final InfrastructureProbeService infrastructureProbeService;
    private final Environment environment;

    public HealthController(
            AsyncTaskProperties asyncTaskProperties,
            InfrastructureProbeService infrastructureProbeService,
            Environment environment) {
        this.asyncTaskProperties = asyncTaskProperties;
        this.infrastructureProbeService = infrastructureProbeService;
        this.environment = environment;
    }

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "backend");
        response.put("activeProfiles", resolveProfiles());
        response.put("taskStatusEndpoint", asyncTaskProperties.getStatusBasePath() + "/{taskId}");
        return response;
    }

    @GetMapping("/dependencies")
    public Map<String, Object> dependencies() {
        return infrastructureProbeService.probe();
    }

    private List<String> resolveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return Arrays.stream(activeProfiles).toList();
        }
        return Arrays.stream(environment.getDefaultProfiles()).toList();
    }
}
