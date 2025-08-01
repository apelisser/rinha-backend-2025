package com.apelisser.rinha2025.config.threadpool;

import com.apelisser.rinha2025.service.PaymentConfirmationWorker;
import com.apelisser.rinha2025.util.ThreadPool;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentConfirmationPool {

    private static final Logger log = LoggerFactory.getLogger(PaymentConfirmationPool.class);

    @Value("${payment-confirmation.thread-pool.size}")
    private int poolSize;

    @Value("${payment-confirmation.thread-interval-execution.ms}")
    private int intervalExecution;

    private final PaymentConfirmationWorker paymentConfirmationWorker;
    private final Runnable paymentConfirmation;

    public PaymentConfirmationPool(
            PaymentConfirmationWorker paymentConfirmationWorker,
            @Value("${payment-confirmation.max-quantity}") int maxQuantity) {
        this.paymentConfirmationWorker = paymentConfirmationWorker;

        this.paymentConfirmation = maxQuantity > 0
            ? this.withMaxQuantity(maxQuantity)
            : this.withoutMaxQuantity();
    }

    @PostConstruct
    public void initPool() {
        List<Thread> threads = ThreadPool.initPool(paymentConfirmation, poolSize, intervalExecution);
        log.info("PaymentConfirmationPool initialized with {} threads", threads.size());
    }

    private Runnable withMaxQuantity(int maxQuantity) {
        return () -> paymentConfirmationWorker.process(maxQuantity);
    }

    private Runnable withoutMaxQuantity () {
        return paymentConfirmationWorker::process;
    }

}
