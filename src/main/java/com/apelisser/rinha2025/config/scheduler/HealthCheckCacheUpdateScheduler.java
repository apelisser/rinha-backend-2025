package com.apelisser.rinha2025.config.scheduler;

import com.apelisser.rinha2025.service.health_check.HealthStatusHolder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckCacheUpdateScheduler {

    private final HealthStatusHolder healthStatusHolder;

    public HealthCheckCacheUpdateScheduler(HealthStatusHolder healthStatusHolder) {
        this.healthStatusHolder = healthStatusHolder;
    }

    @Scheduled(
        fixedDelayString = "${health-check-cache.scheduler.interval.ms}",
        initialDelayString = "${health-check-cache.scheduler.initial-delay.ms}")
    private void execute() {
        healthStatusHolder.refreshCache();
    }

}
