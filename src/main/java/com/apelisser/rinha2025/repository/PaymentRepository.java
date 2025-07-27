package com.apelisser.rinha2025.repository;

import com.apelisser.rinha2025.entity.Payment;
import com.apelisser.rinha2025.enums.PaymentStatus;
import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.model.AggregatedSummary;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PaymentRepository extends ListCrudRepository<Payment, Long> {

    @Modifying
    @Query("""
        UPDATE  
            payment
        SET
            status = 'PROCESSED'::payment_status, 
            processor = :processor::payment_processor
        WHERE
            id = :paymentId
    """)
    void confirm(Long paymentId, PaymentProcessor processor);

    @Modifying
    @Query("""
        UPDATE
            payment
        SET
            status = :status::payment_status
        WHERE
            id = :paymentId
    """)
    void updateStatus(Long paymentId, PaymentStatus status);

    @Modifying
    @Query("""
        UPDATE
            payment
        SET
            status = :status::payment_status
        WHERE
            id IN (:paymentIds)
    """)
    void updateStatus(List<Long> paymentIds, PaymentStatus status);

    @Query("""
        SELECT
            COALESCE(COUNT(*) FILTER (WHERE processor = 'DEFAULT'::payment_processor), 0) AS default_total_requests,
            COALESCE(SUM(amount) FILTER (WHERE processor = 'DEFAULT'::payment_processor), 0.0) AS default_total_amount,
            COALESCE(COUNT(*) FILTER (WHERE processor = 'FALLBACK'::payment_processor), 0) AS fallback_total_requests,
            COALESCE(SUM(amount) FILTER (WHERE processor = 'FALLBACK'::payment_processor), 0.0) AS fallback_total_amount
        FROM
            payment
        WHERE status = 'PROCESSED'::payment_status
            AND requested_at BETWEEN :from AND :to
    """)
    AggregatedSummary getAggregatedSummaryBetween(
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    @Query("""
        SELECT
            COALESCE(COUNT(*) FILTER (WHERE processor = 'DEFAULT'::payment_processor), 0) AS default_total_requests,
            COALESCE(SUM(amount) FILTER (WHERE processor = 'DEFAULT'::payment_processor), 0.0) AS default_total_amount,
            COALESCE(COUNT(*) FILTER (WHERE processor = 'FALLBACK'::payment_processor), 0) AS fallback_total_requests,
            COALESCE(SUM(amount) FILTER (WHERE processor = 'FALLBACK'::payment_processor), 0.0) AS fallback_total_amount
        FROM
            payment
        WHERE status = 'PROCESSED'::payment_status
    """)
    AggregatedSummary getFullAggregatedSummary();


}
