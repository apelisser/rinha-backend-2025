package com.apelisser.rinha2025.domain.service;

import com.apelisser.rinha2025.core.properties.ProcessorProperties;
import com.apelisser.rinha2025.domain.enums.PaymentProcessor;
import com.apelisser.rinha2025.domain.service.health_check.HealthStatusHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

import static com.apelisser.rinha2025.domain.enums.PaymentProcessor.DEFAULT;
import static com.apelisser.rinha2025.domain.enums.PaymentProcessor.FALLBACK;

@Service
public class ProcessorSelectionService {

    private final AtomicReference<PaymentProcessor> bestProcessor = new AtomicReference<>();

    private final HealthStatusHolder healthStatusHolder;
    private final ProcessorProperties processorProps;

    public ProcessorSelectionService(HealthStatusHolder healthStatusHolder, ProcessorProperties processorProps) {
        this.healthStatusHolder = healthStatusHolder;
        this.processorProps = processorProps;
    }

    public PaymentProcessor getBestProcessor() {
        return bestProcessor.get();
    }

    public void updateBestProcessor() {
        PaymentProcessor processor = chooseBestProcessor();
        bestProcessor.set(processor);
    }

    private PaymentProcessor chooseBestProcessor() {
        long defaultScore = calculateScore(healthStatusHolder.getDefaultStatus(), true);
        long fallbackScore = calculateScore(healthStatusHolder.getFallbackStatus(), false);

        if (defaultScore == Long.MAX_VALUE && fallbackScore == Long.MAX_VALUE) {
            return null;
        }

        return defaultScore <= fallbackScore
            ? DEFAULT
            : FALLBACK;
    }

    private long calculateScore(HealthStatusHolder.HealthInfo healthInfo, boolean isDefault) {
        if (healthInfo == null || healthInfo.isFailing()) {
            return Long.MAX_VALUE;
        }

        long score = healthInfo.minResponseTime();

        if (isDefault && score > processorProps.getAdvantageThreshold() && processorProps.getDefaultAdvantage() > 0) {
            long advantage = (long) (score * processorProps.getDefaultAdvantage());
            advantage = Math.max(1, advantage);
            score -= advantage;
        }

        return score;
    }

}
