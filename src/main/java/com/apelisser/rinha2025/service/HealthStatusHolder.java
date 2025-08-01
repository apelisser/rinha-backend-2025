package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.entity.HealthCheckStatus;
import com.apelisser.rinha2025.repository.HealthStatusRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class HealthStatusHolder {

    private final AtomicReference<HealthInfo> defaultStatusCache = new AtomicReference<>();
    private final AtomicReference<HealthInfo> fallbackStatusCache = new AtomicReference<>();

    private final HealthStatusRepository healthStatusRepository;

    public HealthStatusHolder(HealthStatusRepository healthStatusRepository) {
        this.healthStatusRepository = healthStatusRepository;
    }

    public void refreshCache() {
        List<HealthCheckStatus> statusesFromDb = healthStatusRepository.findAll();

        for (HealthCheckStatus dbStatus : statusesFromDb) {
            HealthInfo healthInfo = new HealthInfo(
                dbStatus.isFailing(),
                dbStatus.minResponseTime(),
                dbStatus.lastChecked()
            );

            if (dbStatus.defaultProcessor()) {
                defaultStatusCache.set(healthInfo);
            } else {
                fallbackStatusCache.set(healthInfo);
            }
        }
    }

    public HealthInfo getDefaultStatus() {
        return defaultStatusCache.get();
    }

    public HealthInfo getFallbackStatus() {
        return fallbackStatusCache.get();
    }

    public record HealthInfo(
        boolean isFailing,
        long minResponseTime,
        Instant lastChecked
    ) {}

}
