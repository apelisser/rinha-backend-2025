package com.apelisser.rinha2025.config;

import com.apelisser.rinha2025.enums.PaymentStatus;
import com.apelisser.rinha2025.model.ProcessablePaymentEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import java.time.OffsetDateTime;
import java.util.UUID;

@Configuration
public class JdbcConfig {

    @Bean("processablePaymentEventRowMapper")
    public RowMapper<ProcessablePaymentEvent> processablePaymentEventRowMapper() {
        return (rs, rowNum) -> new ProcessablePaymentEvent(
            rs.getLong("outbox_event_id"),
            rs.getLong("payment_id"),
            rs.getObject("correlation_id", UUID.class),
            rs.getBigDecimal("amount"),
            PaymentStatus.valueOf(rs.getString("status")),
            rs.getObject("requested_at", OffsetDateTime.class)
        );
    }

}
