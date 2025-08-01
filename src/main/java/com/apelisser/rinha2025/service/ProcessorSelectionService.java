package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.PaymentProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class ProcessorSelectionService {

    @Value("${payment-processor-selection.default.advantage}")
    private float defaultAdvantage;

    @Value("${payment-processor-selection.fallback.enabled}")
    private boolean isFallbackEnabled;

    private final AtomicReference<PaymentProcessor> bestProcessor = new AtomicReference<>();
    private final HealthStatusHolder healthStatusHolder;

    public ProcessorSelectionService(HealthStatusHolder healthStatusHolder) {
        this.healthStatusHolder = healthStatusHolder;
    }

    public void updateBestProcessor() {
        PaymentProcessor processor = this.chooseBestProcessor();
        this.bestProcessor.set(processor);
    }

    public PaymentProcessor getBestProcessor() {
        return this.bestProcessor.get();
    }

    private PaymentProcessor chooseBestProcessor() {
        if (!isFallbackEnabled) {
            return PaymentProcessor.DEFAULT;
        }

        HealthStatusHolder.HealthInfo defaultHealth = healthStatusHolder.getDefaultStatus();
        HealthStatusHolder.HealthInfo fallbackHealth = healthStatusHolder.getFallbackStatus();

        if (defaultHealth == null && fallbackHealth == null) {
            return PaymentProcessor.DEFAULT;
        }

        long defaultScore = this.calculateScore(defaultHealth, true);
        long fallbackScore = this.calculateScore(fallbackHealth, false);

        return defaultScore <= fallbackScore
            ? PaymentProcessor.DEFAULT
            : PaymentProcessor.FALLBACK;
    }

    private long calculateScore(HealthStatusHolder.HealthInfo healthInfo, boolean isDefault) {
        if (healthInfo == null || healthInfo.isFailing()) {
            return Long.MAX_VALUE;
        }

        long score = healthInfo.minResponseTime();

        if (isDefault && defaultAdvantage > 0) {
            long advantage = (long) (score * defaultAdvantage);
            advantage = Math.max(1, advantage);
            score -= advantage;
        }

        return score;
    }

}
