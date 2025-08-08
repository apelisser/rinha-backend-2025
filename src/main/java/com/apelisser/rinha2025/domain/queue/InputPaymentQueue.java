package com.apelisser.rinha2025.domain.queue;

import com.apelisser.rinha2025.core.properties.ProcessorProperties;
import com.apelisser.rinha2025.domain.model.PaymentInput;
import org.springframework.stereotype.Service;

@Service
public class InputPaymentQueue extends AbstractQueue<PaymentInput> {

    public InputPaymentQueue(ProcessorProperties processorProperties) {
        super(processorProperties.getQueueSize());
    }

}
