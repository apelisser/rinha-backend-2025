package com.apelisser.rinha2025.core.concurrency;

import com.apelisser.rinha2025.core.properties.ProcessorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Semaphore;

@Configuration
public class SemaphoreConfig {

    private final ProcessorProperties processorProps;

    public SemaphoreConfig(ProcessorProperties processorProps) {
        this.processorProps = processorProps;
    }

    @Bean
    public Semaphore paymentGatewaySemaphore() {
        return new Semaphore(processorProps.getMaxSemaphoreConcurrency(), true);
    }

}
