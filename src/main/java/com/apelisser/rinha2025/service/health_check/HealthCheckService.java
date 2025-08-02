package com.apelisser.rinha2025.service.health_check;

import com.apelisser.rinha2025.config.ProcessorProperties;
import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.PaymentProcessorClient;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.model.HealthCheckResponse;
import com.apelisser.rinha2025.repository.HealthStatusRepository;
import com.apelisser.rinha2025.repository.SchedulerLockRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class HealthCheckService {

    private final SchedulerLockRepository schedulerLockRepository;
    private final HealthStatusRepository healthStatusRepository;
    private final PaymentProcessorClient defaultPaymentProcessor;
    private final PaymentProcessorClient fallbackPaymentProcessor;
    private final ProcessorProperties processorProperties;

    public HealthCheckService(
            SchedulerLockRepository schedulerLockRepository,
            HealthStatusRepository healthStatusRepository,
            @Qualifier("defaultPaymentClient") PaymentProcessorClient defaultPaymentProcessor,
            @Qualifier("fallbackPaymentClient") PaymentProcessorClient fallbackPaymentProcessor,
        ProcessorProperties processorProperties) {
        this.schedulerLockRepository = schedulerLockRepository;
        this.healthStatusRepository = healthStatusRepository;
        this.defaultPaymentProcessor = defaultPaymentProcessor;
        this.fallbackPaymentProcessor = fallbackPaymentProcessor;
        this.processorProperties = processorProperties;
    }

    public void performAndUpdateHealthCheck() {
        boolean acquiredLock = schedulerLockRepository.tryAcquireLock("health_check_leader", 5_010);
        if (acquiredLock) {
            this.updateHealthInfo(PaymentProcessor.DEFAULT, defaultPaymentProcessor);
            if (processorProperties.isFallbackEnabled()) {
                this.updateHealthInfo(PaymentProcessor.FALLBACK, fallbackPaymentProcessor);
            }
        }
    }

    private void updateHealthInfo(PaymentProcessor processor, PaymentProcessorClient client) {
        try {
            HealthCheckResponse healthCheckResponse = client.healthCheck();
            healthStatusRepository.update(
                processor.isDefaultProcessor(),
                healthCheckResponse.failing(),
                healthCheckResponse.minResponseTime(),
                Instant.now());
        } catch (Exception e) {
            healthStatusRepository.update(
                processor.isDefaultProcessor(),
                true,
                Integer.MAX_VALUE,
                Instant.now());
        }
    }

}
