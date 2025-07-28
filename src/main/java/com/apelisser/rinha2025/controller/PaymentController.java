package com.apelisser.rinha2025.controller;

import com.apelisser.rinha2025.model.PaymentInput;
import com.apelisser.rinha2025.model.PaymentSummaryResponse;
import com.apelisser.rinha2025.service.PaymentService;
import com.apelisser.rinha2025.util.ThreadUtil;
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

@RestController
public class PaymentController {

    @Value("${payment.api.summary-delay.ms}")
    private int sleepTimeMillis;

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(value = "/payments", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void payment(@RequestBody PaymentInput paymentInput) {
        paymentService.createIntention(paymentInput);
    }

    @GetMapping(value = "/payments-summary")
    public PaymentSummaryResponse getSummary(
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        ThreadUtil.sleep(sleepTimeMillis);
        return paymentService.getSummary(from, to);
    }

}
