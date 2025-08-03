package com.apelisser.rinha2025.api.controller;

import com.apelisser.rinha2025.core.task.SimpleTaskExecutor;
import com.apelisser.rinha2025.domain.model.PaymentInput;
import com.apelisser.rinha2025.domain.model.PaymentSummaryResponse;
import com.apelisser.rinha2025.domain.queue.InputPaymentQueue;
import com.apelisser.rinha2025.domain.service.PaymentService;
import com.apelisser.rinha2025.core.util.ThreadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.function.Consumer;

@RestController
public class PaymentController {

    @Value("${payment.api.summary-delay.ms}")
    private int sleepTimeMillis;

    private final Consumer<PaymentInput> paymentConsumer;

    private final SimpleTaskExecutor taskExecutor;
    private final InputPaymentQueue inputPaymentQueue;
    private final PaymentService paymentService;

    public PaymentController(
            InputPaymentQueue inputPaymentQueue,
            PaymentService paymentService,
            @Value("${payment.api.async-input}") boolean paymentsAsync,
            SimpleTaskExecutor taskExecutor) {
        this.inputPaymentQueue = inputPaymentQueue;
        this.paymentService = paymentService;

        this.paymentConsumer = paymentsAsync
            ? this.asyncPaymentInputConsumer()
            : this.paymentInputConsumer();

        this.taskExecutor = taskExecutor;
    }

    @PostMapping(value = "/payments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void payment(@RequestBody PaymentInput paymentInput) {
        paymentConsumer.accept(paymentInput);
    }

    @GetMapping(value = "/payments-summary")
    public PaymentSummaryResponse getSummary(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        ThreadUtil.sleep(sleepTimeMillis);
        return paymentService.getSummary(from, to);
    }

    public Consumer<PaymentInput> asyncPaymentInputConsumer() {
        return paymentInput -> taskExecutor.submit(() -> inputPaymentQueue.enqueue(paymentInput));
    }

    public Consumer<PaymentInput> paymentInputConsumer() {
        return inputPaymentQueue::enqueue;
    }

}
