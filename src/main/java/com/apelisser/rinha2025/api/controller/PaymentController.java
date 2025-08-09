package com.apelisser.rinha2025.api.controller;

import com.apelisser.rinha2025.core.concurrency.SimpleTaskExecutor;
import com.apelisser.rinha2025.core.properties.ApiProperties;
import com.apelisser.rinha2025.core.util.ThreadUtil;
import com.apelisser.rinha2025.domain.model.PaymentSummaryResponse;
import com.apelisser.rinha2025.domain.service.PaymentInputService;
import com.apelisser.rinha2025.domain.service.PaymentService;
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

import static com.apelisser.rinha2025.core.properties.ApiProperties.ThreadType.PLATFORM;
import static com.apelisser.rinha2025.core.properties.ApiProperties.ThreadType.VIRTUAL;

@RestController
public class PaymentController {

    private final Consumer<byte[]> paymentConsumer;

    private final PaymentService paymentService;
    private final PaymentInputService paymentInputService;
    private final SimpleTaskExecutor taskExecutor;
    private final ExecutorService fixedThreadPool;
    private final ApiProperties apiProps;

    public PaymentController(
            PaymentService paymentService,
            PaymentInputService paymentInputService,
            SimpleTaskExecutor taskExecutor,
            ApiProperties apiProps) {
        this.paymentService = paymentService;
        this.paymentInputService = paymentInputService;
        this.taskExecutor = taskExecutor;
        this.apiProps = apiProps;

        if (apiProps.isAsyncProcessing()) {
            if (apiProps.getThreadType() == VIRTUAL) {
                fixedThreadPool = null;
                this.paymentConsumer = this.virtualAsyncConsumer();
            } else if (apiProps.getThreadType() == PLATFORM) {
                fixedThreadPool = apiProps.getPlatformThreadPoolSize() > 1
                    ? Executors.newFixedThreadPool(apiProps.getPlatformThreadPoolSize())
                    : Executors.newFixedThreadPool(1);

                this.paymentConsumer = this.platformAsyncConsumer();
            } else {
                throw new IllegalArgumentException("Unknown thread type: " + apiProps.getThreadType());
            }
        } else {
            fixedThreadPool = null;
            this.paymentConsumer = this.syncConsumer();
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
        if (apiProps.getSummaryDelayMillis() > 0) {
            ThreadUtil.sleep(apiProps.getSummaryDelayMillis());
        }
        return paymentService.getSummary(from, to);
    }

    public Consumer<byte[]> virtualAsyncConsumer() {
        return input -> taskExecutor.submit(() -> paymentInputService.convertAndSendToQueue(input));
    }

    public Consumer<byte[]> platformAsyncConsumer() {
        return input -> fixedThreadPool.submit(() -> paymentInputService.convertAndSendToQueue(input));
    }

    public Consumer<byte[]> syncConsumer() {
        return paymentInputService::convertAndSendToQueue;
    }

}
