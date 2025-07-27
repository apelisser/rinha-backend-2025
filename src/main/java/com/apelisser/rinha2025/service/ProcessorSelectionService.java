package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.enums.ProcessorType;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class ProcessorSelectionService {

    private final AtomicReference<ProcessorType> bestProcessor = new AtomicReference<>();
    private final HealthStatusHolder healthStatusHolder;

    public ProcessorSelectionService(HealthStatusHolder healthStatusHolder) {
        this.healthStatusHolder = healthStatusHolder;
    }

    public void updateBestProcessor() {
        ProcessorType processor = this.chooseBestProcessor();
        this.bestProcessor.set(processor);
    }

    public ProcessorType getBestProcessor() {
        return this.bestProcessor.get();
    }

    private ProcessorType chooseBestProcessor() {
        HealthStatusHolder.HealthInfo defaultHealth = healthStatusHolder.getDefaultStatus();
        HealthStatusHolder.HealthInfo fallbackHealth = healthStatusHolder.getFallbackStatus();

        if (defaultHealth == null && fallbackHealth == null) {
            return ProcessorType.DEFAULT;
        }

        long defaultScore = calculateScore(defaultHealth, true);
        long fallbackScore = calculateScore(fallbackHealth, false);

        return defaultScore <= fallbackScore
            ? ProcessorType.DEFAULT
            : ProcessorType.FALLBACK;
    }

    private long calculateScore(HealthStatusHolder.HealthInfo healthInfo, boolean isDefault) {
        if (healthInfo == null || healthInfo.isFailing()) {
            return Long.MAX_VALUE;
        }

        long score = healthInfo.minResponseTime();

        if (isDefault) {
            long advantage = (long) (score * 0.10);
            advantage = Math.max(1, advantage);
            score -= advantage;
        }

        return score;
    }

}
