package com.apelisser.rinha2025.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcessorProperties {

    @Value("${payment-processor.queue-length}")
    private int queueSize;

    @Value("${payment-processor.max-quantity}")
    private int maxSize;

    @Value("${payment-processor.number-of-workers}")
    private int numberOfWorkers;

    @Value("${payment-processor.worker-interval-millis}")
    private int workerIntervalMillis;

    @Value("${payment-processor.extra-worker-interval-millis}")
    private int extraWorkerIntervalMillis;

    @Value("${payment-processor-selection.default.advantage-threshold}")
    private int advantageThreshold;

    @Value("${payment-processor-selection.default.advantage}")
    private float defaultAdvantage;

    @Value("${payment-processor-selection.fallback.enabled}")
    private boolean isFallbackEnabled;

    @Value("${payment-processor.max-quantity.reduction-percentage}")
    private float reductionWhenDefaultIsOut;

    @Value("${payment-processor.max-retries}")
    private int maxRetries;

    public int getQueueSize() {
        return queueSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getNumberOfWorkers() {
        return numberOfWorkers;
    }

    public int getWorkerIntervalMillis() {
        return workerIntervalMillis;
    }

    public int getExtraWorkerIntervalMillis() {
        return extraWorkerIntervalMillis;
    }

    public int getAdvantageThreshold() {
        return advantageThreshold;
    }

    public float getDefaultAdvantage() {
        return defaultAdvantage;
    }

    public boolean isFallbackEnabled() {
        return isFallbackEnabled;
    }

    public float getReductionPercentage() {
        if (reductionWhenDefaultIsOut > 1) return 1;
        if (reductionWhenDefaultIsOut < 0) return 0;
        return reductionWhenDefaultIsOut;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

}
