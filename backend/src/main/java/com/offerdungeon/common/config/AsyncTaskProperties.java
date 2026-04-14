package com.offerdungeon.common.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.async-task")
public class AsyncTaskProperties {

    private String statusBasePath = "/api/tasks";
    private Duration pollInterval = Duration.ofSeconds(3);
    private Duration timeout = Duration.ofMinutes(10);

    public String getStatusBasePath() {
        return statusBasePath;
    }

    public void setStatusBasePath(String statusBasePath) {
        this.statusBasePath = statusBasePath;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
