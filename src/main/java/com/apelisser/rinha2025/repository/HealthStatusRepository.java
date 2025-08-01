package com.apelisser.rinha2025.repository;

import com.apelisser.rinha2025.entity.HealthCheckStatus;
import com.apelisser.rinha2025.enums.PaymentProcessor;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.time.Instant;

public interface HealthStatusRepository extends ListCrudRepository<HealthCheckStatus, PaymentProcessor> {

    @Modifying
    @Query("""
        UPDATE 
            health_check_status
        SET
            is_failing = :isFailing,
            min_response_time = :minResponseTime,
            last_checked = :lastChecked
        WHERE
            default_processor = :isDefaultProcessor
    """)
    void update(boolean isDefaultProcessor, boolean isFailing, long minResponseTime, Instant lastChecked);

}
