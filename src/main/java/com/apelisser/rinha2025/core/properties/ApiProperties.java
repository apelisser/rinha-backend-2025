package com.apelisser.rinha2025.core.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiProperties {

    @Value("${payment.api.async-input}")
    private boolean asyncProcessing;

    @Value("${payment.api.async-thread-type}")
    private ThreadType threadType;

    @Value("${payment.api.async-input.pool-size}")
    private int platformThreadPoolSize;

    @Value("${payment.api.summary-delay.ms}")
    private int summaryDelayMillis;

    public boolean isAsyncProcessing() {
        return asyncProcessing;
    }

    public ThreadType getThreadType() {
        return threadType;
    }

    public int getPlatformThreadPoolSize() {
        return platformThreadPoolSize;
    }

    public int getSummaryDelayMillis() {
        return summaryDelayMillis;
    }

    public enum ThreadType {
        VIRTUAL, PLATFORM
    }

}
