package com.apelisser.rinha2025.core.web.filter;

import com.apelisser.rinha2025.core.concurrency.SimpleTaskExecutor;
import com.apelisser.rinha2025.domain.service.PaymentInputService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class PaymentFilterBeanConfig {

    @Value("${payment.api.async-input}")
    private boolean paymentsAsync;

    private final SimpleTaskExecutor taskExecutor;
    private final PaymentInputService paymentInputService;

    public PaymentFilterBeanConfig(SimpleTaskExecutor taskExecutor, PaymentInputService paymentInputService) {
        this.taskExecutor = taskExecutor;
        this.paymentInputService = paymentInputService;
    }

    @Bean
    @ConditionalOnProperty(value = "payment.api.filter.process", havingValue = "true")
    public FilterRegistrationBean<PaymentFilter> requestIdContextFilter() {
        FilterRegistrationBean<PaymentFilter> bean = new FilterRegistrationBean<>();
        bean.addUrlPatterns("/payments");
        bean.setFilter(new PaymentFilter(paymentInputService, taskExecutor, paymentsAsync));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

}
