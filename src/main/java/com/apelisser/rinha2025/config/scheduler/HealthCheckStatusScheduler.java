package com.apelisser.rinha2025.config.scheduler;

import com.apelisser.rinha2025.service.HealthCheckService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckStatusScheduler {

    private final HealthCheckService healthCheckService;

    public HealthCheckStatusScheduler(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @Scheduled(
        fixedDelayString = "${health-check-execution.scheduler.interval.ms}",
        initialDelayString = "${health-check-execution.scheduler.initial-delay.ms}")
    private void execute() {
        healthCheckService.performAndUpdateHealthCheck();
    }

}
