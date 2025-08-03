package com.apelisser.rinha2025.core.scheduler;

import com.apelisser.rinha2025.core.task.TaskPoolManager;
import com.apelisser.rinha2025.domain.service.health_check.HealthCheckService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class HealthCheckScheduler {

    @Value("${health-check.scheduler-interval.execution}")
    private long intervalInMillis;

    private final TaskPoolManager taskManager;
    private final HealthCheckService healthCheckService;

    public HealthCheckScheduler(TaskPoolManager taskManager, HealthCheckService healthCheckService) {
        this.taskManager = taskManager;
        this.healthCheckService = healthCheckService;
    }

    @EventListener(ApplicationReadyEvent.class)
    void setUp() {
        taskManager.executeRecurring(
            healthCheckService::performAndUpdateHealthCheck,
            intervalInMillis,
            TimeUnit.MILLISECONDS);
    }

}
