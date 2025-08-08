package com.apelisser.rinha2025.infrastructure.paymentprocessor;

import com.apelisser.rinha2025.infrastructure.paymentprocessor.model.HealthCheckResponse;
import com.apelisser.rinha2025.domain.model.PaymentInput;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/payments")
public interface PaymentProcessorClient {

    @PostExchange
    String processPayment(@RequestBody PaymentInput paymentRequest);

    @GetExchange("/service-health")
    HealthCheckResponse healthCheck();

}
