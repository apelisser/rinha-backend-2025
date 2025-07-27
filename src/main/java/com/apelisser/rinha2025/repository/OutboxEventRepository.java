package com.apelisser.rinha2025.repository;

import com.apelisser.rinha2025.entity.OutboxEvent;
import com.apelisser.rinha2025.model.ProcessablePaymentEvent;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends ListCrudRepository<OutboxEvent, Long> {

    @Query(
        value = """
            SELECT
                p.id as payment_id, 
                p.correlation_id, 
                p.amount, 
                p.requested_at,
                p.status,
                o.id as outbox_event_id
            FROM
                payment p
            JOIN
                outbox_event o ON p.id = o.payment_id
            WHERE
                p.status IN ('PENDING'::payment_status, 'FAILED'::payment_status)
            ORDER BY
                o.created_at
            LIMIT :maxQuantity
            FOR UPDATE OF p SKIP LOCKED
        """,
        rowMapperRef = "processablePaymentEventRowMapper")
    List<ProcessablePaymentEvent> findAndLockProcessableEvents(int maxQuantity);

    @Modifying
    @Query("""
            DELETE FROM outbox_event
            WHERE
                payment_id IN (
                    SELECT
                        id
                    FROM
                        payment
                    WHERE
                        status = 'PROCESSED'::payment_status
                )
        """)
    void deleteAllAlreadyProcessed();

}
