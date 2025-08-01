package com.apelisser.rinha2025.repository;

import com.apelisser.rinha2025.model.PaymentProcessed;

import java.util.List;

public interface PaymentRepositoryQueries {

    int savePayments(List<PaymentProcessed> payments);

}
