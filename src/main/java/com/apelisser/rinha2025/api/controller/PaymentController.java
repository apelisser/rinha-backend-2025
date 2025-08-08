package com.apelisser.rinha2025.api.controller;

import com.apelisser.rinha2025.core.util.ThreadUtil;
import com.apelisser.rinha2025.domain.model.PaymentSummaryResponse;
import com.apelisser.rinha2025.domain.service.PaymentInputService;
import com.apelisser.rinha2025.domain.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@RestController
public class PaymentController {

    @Value("${payment.api.summary-delay.ms}")
    private int sleepTimeMillis;

    private final Consumer<byte[]> paymentConsumer;

    private final PaymentService paymentService;
    private final PaymentInputService paymentInputService;
    private final ExecutorService fixedThreadPool;

    public PaymentController(
            PaymentService paymentService,
            @Value("${payment.api.async-input}") boolean paymentsAsync,
            @Value("${payment.api.async-input.pool-size}") int fixedThreadPoolSize,
            PaymentInputService paymentInputService) {
        this.paymentService = paymentService;
        this.paymentInputService = paymentInputService;

        if (paymentsAsync) {
            fixedThreadPool = fixedThreadPoolSize >= 1
                ? Executors.newFixedThreadPool(fixedThreadPoolSize)
                : Executors.newFixedThreadPool(1);
            this.paymentConsumer = this.asyncPaymentInputConsumer();
        } else {
            fixedThreadPool = null;
            this.paymentConsumer = this.paymentInputConsumer();
        }
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void payment(@RequestBody byte[] input) {
        paymentConsumer.accept(input);
    }

    @GetMapping(value = "/payments-summary")
    public PaymentSummaryResponse getSummary(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        ThreadUtil.sleep(sleepTimeMillis);
        return paymentService.getSummary(from, to);
    }

    public Consumer<byte[]> asyncPaymentInputConsumer() {
        return input -> fixedThreadPool.submit(() -> paymentInputService.convertAndSendToQueue(input));
    }

    public Consumer<byte[]> paymentInputConsumer() {
        return paymentInputService::convertAndSendToQueue;
    }

}
