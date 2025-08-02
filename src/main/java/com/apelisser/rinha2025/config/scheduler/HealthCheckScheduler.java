package com.apelisser.rinha2025.config.scheduler;

import com.apelisser.rinha2025.service.health_check.HealthCheckService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckScheduler {

    private final HealthCheckService healthCheckService;

    public HealthCheckScheduler(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @Scheduled(
        fixedDelayString = "${health-check-execution.scheduler.interval.ms}",
        initialDelayString = "${health-check-execution.scheduler.initial-delay.ms}")
    private void execute() {
        healthCheckService.performAndUpdateHealthCheck();
    }

}
