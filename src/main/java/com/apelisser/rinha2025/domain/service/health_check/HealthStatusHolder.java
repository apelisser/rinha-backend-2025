package com.apelisser.rinha2025.domain.service.health_check;

import com.apelisser.rinha2025.core.properties.ProcessorProperties;
import com.apelisser.rinha2025.domain.entity.HealthCheckStatus;
import com.apelisser.rinha2025.domain.repository.HealthStatusRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class HealthStatusHolder {

    private final AtomicReference<HealthInfo> defaultStatusCache = new AtomicReference<>();
    private final AtomicReference<HealthInfo> fallbackStatusCache = new AtomicReference<>();

    private final HealthStatusRepository healthStatusRepository;
    private final ProcessorProperties processorProperties;

    public HealthStatusHolder(HealthStatusRepository healthStatusRepository, ProcessorProperties processorProperties) {
        this.healthStatusRepository = healthStatusRepository;
        this.processorProperties = processorProperties;
    }

    public void refreshCache() {
        List<HealthCheckStatus> statusesFromDb = healthStatusRepository.findAll();

        for (HealthCheckStatus dbStatus : statusesFromDb) {
            if (dbStatus.defaultProcessor()) {
                defaultStatusCache.set(this.mapToHealthInfo(dbStatus));
            } else if (processorProperties.isFallbackEnabled()) {
                fallbackStatusCache.set(this.mapToHealthInfo(dbStatus));
            }
        }
    }

    private HealthInfo mapToHealthInfo(HealthCheckStatus status) {
        return new HealthInfo(
            status.isFailing(),
            status.minResponseTime(),
            status.lastChecked()
        );
    }

    public HealthInfo getDefaultStatus() {
        return defaultStatusCache.get();
    }

    public HealthInfo getFallbackStatus() {
        return fallbackStatusCache.get();
    }

    public boolean isDefaultFailing() {
        return defaultStatusCache.get() == null || defaultStatusCache.get().isFailing();
    }

    public boolean isFallbackFailing() {
        return fallbackStatusCache.get() == null || fallbackStatusCache.get().isFailing();
    }

    public record HealthInfo(
        boolean isFailing,
        long minResponseTime,
        Instant lastChecked
    ) {}

}
