package com.apelisser.rinha2025.core.web.filter;

import com.apelisser.rinha2025.core.task.SimpleTaskExecutor;
import com.apelisser.rinha2025.domain.service.PaymentInputService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.function.Consumer;

public class PaymentFilter implements Filter {

    private final Consumer<byte[]> paymentConsumer;
    private final SimpleTaskExecutor taskExecutor;
    private final PaymentInputService paymentInputService;

    public PaymentFilter(PaymentInputService paymentInputService, SimpleTaskExecutor taskExecutor, boolean paymentsAsync) {
        this.taskExecutor = taskExecutor;
        this.paymentInputService = paymentInputService;

        this.paymentConsumer = paymentsAsync
            ? asyncPaymentInputConsumer()
            : paymentInputConsumer();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        paymentConsumer.accept(getRequestBody(request));
        updateResponse(response);
    }

    private byte[] getRequestBody(ServletRequest request) throws IOException {
        return request.getInputStream().readAllBytes();
    }

    private void updateResponse(ServletResponse response) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(202);
    }

    public Consumer<byte[]> asyncPaymentInputConsumer() {
        return input -> taskExecutor.submit(() -> paymentInputService.convertAndSendToQueue(input));
    }

    public Consumer<byte[]> paymentInputConsumer() {
        return paymentInputService::convertAndSendToQueue;
    }

}
