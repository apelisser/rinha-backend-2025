package com.apelisser.rinha2025.config;

import com.apelisser.rinha2025.infrastructure.paymentprocessor.PaymentProcessorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${payment.processor.default.url}")
    private String defaultClientBaseUrl;

    @Value("${payment.processor.default.connect-timeout}")
    private Duration defaultConnectTimeout;

    @Value("${payment.processor.default.read-timeout}")
    private Duration defaultReadTimeout;

    @Value("${payment.processor.fallback.url}")
    private String fallbackClientBaseUrl;

    @Value("${payment.processor.fallback.connect-timeout}")
    private Duration fallbackConnectTimeout;

    @Value("${payment.processor.fallback.read-timeout}")
    private Duration fallbackReadTimeout;

    @Bean("defaultPaymentClient")
    public PaymentProcessorClient defaultPaymentClient(RestClient.Builder builder) {
        RestClient restClient = builder.clone()
            .baseUrl(defaultClientBaseUrl)
            .requestFactory(generateClientHttpRequestFactory(defaultConnectTimeout, defaultReadTimeout))
            .build();
        
        RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return proxyFactory.createClient(PaymentProcessorClient.class);
    }

    @Bean("fallbackPaymentClient")
    public PaymentProcessorClient fallbackPaymentClient(RestClient.Builder builder) {
        RestClient restClient = builder.clone()
            .baseUrl(fallbackClientBaseUrl)
            .requestFactory(generateClientHttpRequestFactory(fallbackConnectTimeout, fallbackReadTimeout))
            .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return proxyFactory.createClient(PaymentProcessorClient.class);
    }

    private ClientHttpRequestFactory generateClientHttpRequestFactory(Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }

}
