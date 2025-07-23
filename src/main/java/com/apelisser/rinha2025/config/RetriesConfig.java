package com.apelisser.rinha2025.config;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RetriesConfig {

    @Value("${payment.processor.retry.max-attempts}")
    private int maxAttempts;

    @Value("${payment.processor.retry.wait-duration}")
    private Duration waitDuration;

    @Bean("paymentRetry")
    public Retry paymentRetry(RetryRegistry retryRegistry) {
        RetryConfig paymentRetryConfig = RetryConfig.custom()
            .maxAttempts(maxAttempts)
            .waitDuration(waitDuration)
            .build();

        return retryRegistry.retry("payment-orchestrator-retry", paymentRetryConfig);
    }

}
