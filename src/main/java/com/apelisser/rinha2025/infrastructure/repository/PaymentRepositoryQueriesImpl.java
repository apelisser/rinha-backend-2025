package com.apelisser.rinha2025.infrastructure.repository;

import com.apelisser.rinha2025.enums.PaymentProcessor;
import com.apelisser.rinha2025.model.PaymentInput;
import com.apelisser.rinha2025.model.PaymentProcessed;
import com.apelisser.rinha2025.repository.PaymentRepositoryQueries;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Repository
public class PaymentRepositoryQueriesImpl implements PaymentRepositoryQueries {

    private static final String BATCH_INSERT_SQL = "INSERT INTO payment (correlation_id, amount, requested_at, default_processor) VALUES (?, ?, ?, ?)";

    @Value("${payment-confirmation.persistence.copy-threshold}")
    private int copyThreshold;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public PaymentRepositoryQueriesImpl(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int savePayments(List<PaymentProcessed> payments) {
        return payments.size() < copyThreshold
            ? this.saveWithJdbcBatch(payments)
            : this.saveWithPostgresCopy(payments);
    }

    private int saveWithJdbcBatch(List<PaymentProcessed> payments) {
        List<Object[]> args = payments.stream()
            .map(p -> new Object[]{
                p.paymentInput().getCorrelationId(),
                p.paymentInput().getAmount(),
                Timestamp.from(p.paymentInput().getRequestedAt()),
                p.processedAt().isDefaultProcessor()
            })
            .toList();

        int[] rows = jdbcTemplate.batchUpdate(BATCH_INSERT_SQL, args);
        return Arrays.stream(rows).sum();
    }

    private int saveWithPostgresCopy(List<PaymentProcessed> payments) {
        StringBuilder sb = new StringBuilder();

        for (PaymentProcessed p : payments) {
            PaymentInput input = p.paymentInput();
            PaymentProcessor processor = p.processedAt();

            sb.append(input.getCorrelationId()).append('\t');
            sb.append(input.getAmount()).append('\t');
            sb.append(input.getRequestedAt()).append('\t');
            sb.append(processor.isDefaultProcessor()).append('\n');
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);

        try (Connection conn = dataSource.getConnection()) {
            CopyManager copyManager = new CopyManager(conn.unwrap(BaseConnection.class));

            long rowsInserted = copyManager.copyIn(
                "COPY payment (correlation_id, amount, requested_at, payment_processor) FROM STDIN WITH (FORMAT text)",
                new ByteArrayInputStream(data)
            );

            return (int) rowsInserted;
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error inserting payments with COPY", e);
        }
    }

}
