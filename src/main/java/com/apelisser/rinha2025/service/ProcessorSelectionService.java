package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.config.ProcessorProperties;
import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.service.health_check.HealthStatusHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ProcessorSelectionService {

    private final AtomicReference<PaymentProcessor> bestProcessor = new AtomicReference<>();
    private final HealthStatusHolder healthStatusHolder;
    private final ProcessorProperties processorProperties;

    public ProcessorSelectionService(HealthStatusHolder healthStatusHolder, ProcessorProperties processorProperties) {
        this.healthStatusHolder = healthStatusHolder;
        this.processorProperties = processorProperties;
    }

    public Optional<PaymentProcessor> getBestProcessor() {
        return Optional.ofNullable(bestProcessor.get());
    }

    public void updateBestProcessor() {
        PaymentProcessor processor = this.chooseBestProcessor();
        this.bestProcessor.set(processor);
    }

    private PaymentProcessor chooseBestProcessor() {
        if (!processorProperties.isFallbackEnabled()) {
            if (healthStatusHolder.isDefaultFailing()) {
                return null;
            }
            return PaymentProcessor.DEFAULT;
        }

        long defaultScore = this.calculateScore(healthStatusHolder.getDefaultStatus(), true);
        long fallbackScore = this.calculateScore(healthStatusHolder.getFallbackStatus(), false);

        if (defaultScore == Long.MAX_VALUE && fallbackScore == Long.MAX_VALUE) {
            return null;
        }

        return defaultScore <= fallbackScore
            ? PaymentProcessor.DEFAULT
            : PaymentProcessor.FALLBACK;
    }

    private long calculateScore(HealthStatusHolder.HealthInfo healthInfo, boolean isDefault) {
        if (healthInfo == null || healthInfo.isFailing()) {
            return Long.MAX_VALUE;
        }

        long score = healthInfo.minResponseTime();

        if (isDefault && processorProperties.getDefaultAdvantage() > 0) {
            long advantage = (long) (score * processorProperties.getDefaultAdvantage());
            advantage = Math.max(1, advantage);
            score -= advantage;
        }

        return score;
    }

}
