package com.apelisser.rinha2025.domain.service.health_check;

import com.apelisser.rinha2025.core.properties.ProcessorProperties;
import com.apelisser.rinha2025.domain.enums.PaymentProcessor;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.PaymentProcessorClient;
import com.apelisser.rinha2025.infrastructure.paymentprocessor.model.HealthCheckResponse;
import com.apelisser.rinha2025.domain.repository.HealthStatusRepository;
import com.apelisser.rinha2025.domain.repository.LockRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.apelisser.rinha2025.domain.enums.PaymentProcessor.DEFAULT;
import static com.apelisser.rinha2025.domain.enums.PaymentProcessor.FALLBACK;

@Service
public class HealthCheckService {

    private static final String LOCK_NAME = "health_check_leader";
    private static final int LOCK_INTERVAL_IN_MILLIS = 5_010;

    private final LockRepository lockRepository;
    private final HealthStatusRepository healthStatusRepository;
    private final PaymentProcessorClient defaultPaymentProcessor;
    private final PaymentProcessorClient fallbackPaymentProcessor;
    private final ProcessorProperties processorProperties;

    public HealthCheckService(
            LockRepository lockRepository,
            HealthStatusRepository healthStatusRepository,
            @Qualifier("defaultPaymentClient") PaymentProcessorClient defaultPaymentProcessor,
            @Qualifier("fallbackPaymentClient") PaymentProcessorClient fallbackPaymentProcessor,
            ProcessorProperties processorProperties) {
        this.lockRepository = lockRepository;
        this.healthStatusRepository = healthStatusRepository;
        this.defaultPaymentProcessor = defaultPaymentProcessor;
        this.fallbackPaymentProcessor = fallbackPaymentProcessor;
        this.processorProperties = processorProperties;
    }

    public void performAndUpdateHealthCheck() {
        boolean acquiredLock = lockRepository.tryAcquireLock(LOCK_NAME, LOCK_INTERVAL_IN_MILLIS);
        if (acquiredLock) {
            updateHealthInfo(DEFAULT, defaultPaymentProcessor);
            if (processorProperties.isFallbackEnabled()) {
                updateHealthInfo(FALLBACK, fallbackPaymentProcessor);
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
