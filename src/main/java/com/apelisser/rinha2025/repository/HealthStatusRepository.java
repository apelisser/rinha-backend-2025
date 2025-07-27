package com.apelisser.rinha2025.repository;

import com.apelisser.rinha2025.entity.HealthCheckStatus;
import com.apelisser.rinha2025.enums.PaymentProcessor;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.time.OffsetDateTime;

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
            processor_name = :processorName
    """)
    void update(PaymentProcessor processorName, boolean isFailing, long minResponseTime, OffsetDateTime lastChecked);

}
