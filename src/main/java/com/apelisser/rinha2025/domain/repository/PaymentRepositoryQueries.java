package com.apelisser.rinha2025.domain.repository;

import com.apelisser.rinha2025.domain.model.PaymentProcessed;

import java.util.List;

public interface PaymentRepositoryQueries {

    int batchSave(List<PaymentProcessed> payments);

    int saveIndividually(List<PaymentProcessed> payments);

    int saveWithPostgresCopy(List<PaymentProcessed> payments);

}
