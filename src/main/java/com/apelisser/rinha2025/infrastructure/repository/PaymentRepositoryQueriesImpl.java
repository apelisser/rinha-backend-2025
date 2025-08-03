package com.apelisser.rinha2025.infrastructure.repository;

import com.apelisser.rinha2025.domain.model.PaymentProcessed;
import com.apelisser.rinha2025.domain.repository.PaymentRepositoryQueries;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
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

    private static final String INSERT_IGNORE_SQL = """
    INSERT INTO payment (correlation_id, amount, requested_at, default_processor)
    VALUES (?, ?, ?, ?)
    ON CONFLICT (correlation_id) DO NOTHING
    """;

    private static final String COPY_INSERT_POSTGRES = """
    COPY payment (correlation_id, amount, requested_at, payment_processor) FROM STDIN WITH (FORMAT text)
    """;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public PaymentRepositoryQueriesImpl(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int batchSave(List<PaymentProcessed> payments) {
        List<Object[]> args = payments.stream()
            .map(payment -> new Object[]{
                payment.getPaymentInput().getCorrelationId(),
                payment.getPaymentInput().getAmount(),
                Timestamp.from(payment.getPaymentInput().getRequestedAt()),
                payment.getProcessor().isDefaultProcessor()
            })
            .toList();

        int[] rows = jdbcTemplate.batchUpdate(INSERT_IGNORE_SQL, args);
        return Arrays.stream(rows).sum();
    }

    @Override
    public int saveIndividually(List<PaymentProcessed> payments) {
        int totalRows = 0;
        for (PaymentProcessed payment : payments) {
            int rows = jdbcTemplate.update(INSERT_IGNORE_SQL,
                payment.getPaymentInput().getCorrelationId(),
                payment.getPaymentInput().getAmount(),
                Timestamp.from(payment.getPaymentInput().getRequestedAt()),
                payment.getProcessor().isDefaultProcessor()
            );
            totalRows += rows;
        }
        return totalRows;
    }

    @Override
    public int saveWithPostgresCopy(List<PaymentProcessed> payments) {
        StringBuilder sb = new StringBuilder();
        for (PaymentProcessed payment : payments) {
            sb.append(payment.getPaymentInput().getCorrelationId()).append('\t');
            sb.append(payment.getPaymentInput().getAmount()).append('\t');
            sb.append(payment.getPaymentInput().getRequestedAt()).append('\t');
            sb.append(payment.getProcessor().isDefaultProcessor()).append('\n');
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);

        try (Connection conn = dataSource.getConnection()) {
            CopyManager copyManager = new CopyManager(conn.unwrap(BaseConnection.class));

            long rowsInserted = copyManager.copyIn(COPY_INSERT_POSTGRES, new ByteArrayInputStream(data));
            return (int) rowsInserted;
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error inserting payments with COPY", e);
        }
    }

}
