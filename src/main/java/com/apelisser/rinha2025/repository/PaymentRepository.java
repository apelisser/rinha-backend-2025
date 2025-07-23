package com.apelisser.rinha2025.repository;

import com.apelisser.rinha2025.entity.Payment;
import com.apelisser.rinha2025.model.AggregatedSummary;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PaymentRepository extends ListCrudRepository<Payment, Long> {

    @Query("""
        SELECT
            COALESCE(COUNT(*) FILTER (WHERE default_processor = true), 0) AS default_total_requests,
            COALESCE(SUM(amount) FILTER (WHERE default_processor = true), 0.0) AS default_total_amount,
            COALESCE(COUNT(*) FILTER (WHERE default_processor = false), 0) AS fallback_total_requests,
            COALESCE(SUM(amount) FILTER (WHERE default_processor = false), 0.0) AS fallback_total_amount
        FROM
            payment
        WHERE
            requested_at BETWEEN :from AND :to
    """)
    AggregatedSummary getAggregatedSummaryBetween(
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    @Query("""
        SELECT
            COALESCE(COUNT(*) FILTER (WHERE default_processor = true), 0) AS default_total_requests,
            COALESCE(SUM(amount) FILTER (WHERE default_processor = true), 0.0) AS default_total_amount,
            COALESCE(COUNT(*) FILTER (WHERE default_processor = false), 0) AS fallback_total_requests,
            COALESCE(SUM(amount) FILTER (WHERE default_processor = false), 0.0) AS fallback_total_amount
        FROM
            payment
    """)
    AggregatedSummary getFullAggregatedSummary();


}
