package com.apelisser.rinha2025.domain.service;

import com.apelisser.rinha2025.domain.model.PaymentInput;
import com.apelisser.rinha2025.domain.queue.InputPaymentQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PaymentInputService {

    private final ObjectMapper mapper;
    private final InputPaymentQueue inputQueue;

    public PaymentInputService(ObjectMapper mapper, InputPaymentQueue inputQueue) {
        this.mapper = mapper;
        this.inputQueue = inputQueue;
    }

    public boolean convertAndSendToQueue(byte[] paymentInputBytes) {
        if (paymentInputBytes == null || paymentInputBytes.length == 0) {
            return false;
        }

        PaymentInput payment = convert(paymentInputBytes, PaymentInput.class);
        if (payment != null) {
            return inputQueue.enqueue(payment);
        }
        return false;
    }

    private <T> T convert(byte[] input, Class<T> clazz) {
        try {
            return mapper.readValue(input, clazz);
        } catch (IOException e) {
            return null;
        }
    }

}
