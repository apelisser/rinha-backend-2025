package com.apelisser.rinha2025.config.threadpool;

import com.apelisser.rinha2025.service.PaymentProcessorService;
import com.apelisser.rinha2025.util.ThreadPool;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentProcessorPool {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessorPool.class);

    @Value("${payment-processor.thread-pool.size}")
    private int poolSize;

    @Value("${payment-processor.thread-interval-execution.ms}")
    private int intervalExecution;

    private final PaymentProcessorService paymentProcessorService;
    private final Runnable paymentProcessor;

    public PaymentProcessorPool(
            PaymentProcessorService paymentProcessorService,
            @Value("${payment-processor.max-quantity}") int maxQuantity) {
        this.paymentProcessorService = paymentProcessorService;

        this.paymentProcessor = maxQuantity > 0
            ? this.withMaxQuantity(maxQuantity)
            : this.withoutMaxQuantity();
    }

    @PostConstruct
    private void initPool() {
        List<Thread> threads = ThreadPool.initPool(paymentProcessor, poolSize, intervalExecution);
        log.info("PaymentProcessorPool initialized with {} threads", threads.size());
    }

    private Runnable withMaxQuantity(int maxQuantity) {
        return () -> paymentProcessorService.process(maxQuantity);
    }

    private Runnable withoutMaxQuantity () {
        return paymentProcessorService::process;
    }

}
