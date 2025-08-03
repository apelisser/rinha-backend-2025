package com.apelisser.rinha2025.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfirmationProperties {

    @Value("${payment-processed.queue.length}")
    private int queueSize;

    @Value("${payment-processed.dequeue.max-size}")
    private int maxSize;

    @Value("${payment-processed.number-of-workers}")
    private int numberOfWorkers;

    @Value("${payment-processed.worker-interval-millis}")
    private int workerIntervalMillis;

    @Value("${payment-processed.max-retries}")
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

    public int getMaxRetries() {
        return maxRetries;
    }

}
