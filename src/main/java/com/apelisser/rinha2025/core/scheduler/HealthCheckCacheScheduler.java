package com.apelisser.rinha2025.core.scheduler;

import com.apelisser.rinha2025.core.task.TaskPoolManager;
import com.apelisser.rinha2025.domain.service.health_check.HealthStatusHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class HealthCheckCacheScheduler {

    @Value("${health-check.scheduler-interval.cache}")
    private long intervalInMillis;

    private final TaskPoolManager taskManager;
    private final HealthStatusHolder healthStatusHolder;

    public HealthCheckCacheScheduler(TaskPoolManager taskManager, HealthStatusHolder healthStatusHolder) {
        this.taskManager = taskManager;
        this.healthStatusHolder = healthStatusHolder;
    }

    @EventListener(ApplicationReadyEvent.class)
    void setUp() {
        taskManager.executeRecurring(
            healthStatusHolder::refreshCache,
            intervalInMillis,
            TimeUnit.MILLISECONDS);
    }

}
