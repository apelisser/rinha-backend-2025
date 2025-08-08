package com.apelisser.rinha2025.domain.queue;

import com.apelisser.rinha2025.core.properties.ConfirmationProperties;
import com.apelisser.rinha2025.domain.model.PaymentProcessed;
import org.springframework.stereotype.Component;

@Component
public class ProcessedPaymentQueue extends AbstractQueue<PaymentProcessed> {

    public ProcessedPaymentQueue(ConfirmationProperties processorProperties) {
        super(processorProperties.getQueueSize());
    }

}
