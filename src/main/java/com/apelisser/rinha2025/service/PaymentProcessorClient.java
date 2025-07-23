package com.apelisser.rinha2025.service;

import com.apelisser.rinha2025.model.HealthCheckResponse;
import com.apelisser.rinha2025.model.PaymentApiRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/payments")
public interface PaymentProcessorClient {

    @PostExchange
    String processPayment(@RequestBody PaymentApiRequest paymentRequest);

    @GetExchange("/service-health")
    HealthCheckResponse healthCheck();

}
