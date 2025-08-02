package com.apelisser.rinha2025.config;

import com.apelisser.rinha2025.service.PaymentProcessorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcessorProperties {

    // vantagem para o processador default
    @Value("${payment-processor-selection.default.advantage}")
    private float defaultAdvantage;

    // habilita/desabilita o processador de fallback
    @Value("${payment-processor-selection.fallback.enabled}")
    private boolean isFallbackEnabled;

    // tipo de concorrencia para o processador
    @Value("${payment-processor.concurrency.type}")
    private PaymentProcessorService.ProcessorType processorType;

    // (%) para reduzir quantidade maxima quando o default esta fora - é aplicado somente se isFallbackEnabled=true
    @Value("${payment-processor.max-quantity.reduction-percentage}")
    private float reductionWhenDefaultIsOut;

    @Value("${payment-processor.max-requeue}")
    private int maxRetries;

    public float getDefaultAdvantage() {
        return defaultAdvantage;
    }

    public boolean isFallbackEnabled() {
        return isFallbackEnabled;
    }

    public PaymentProcessorService.ProcessorType getProcessorType() {
        return processorType;
    }

    public float getReductionPercentage() {
        if (reductionWhenDefaultIsOut > 1) return 1;
        if (reductionWhenDefaultIsOut < 0) return 0;
        return reductionWhenDefaultIsOut;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public boolean hasReductionWhenDefaultIsOut() {
        return reductionWhenDefaultIsOut > 0;
    }

}
