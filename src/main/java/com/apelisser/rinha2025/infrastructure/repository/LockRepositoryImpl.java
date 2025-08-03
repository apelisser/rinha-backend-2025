package com.apelisser.rinha2025.infrastructure.repository;

import com.apelisser.rinha2025.domain.repository.LockRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LockRepositoryImpl implements LockRepository {

    private final JdbcTemplate jdbcTemplate;

    public LockRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean tryAcquireLock(String lockName, int lockIntervalInMillis) {
        final String leadershipQuery = """
                UPDATE scheduler_locks
                SET last_execution = NOW()
                WHERE lock_name = ? AND last_execution < NOW() - INTERVAL '%d milliseconds'
            """.formatted(lockIntervalInMillis);

        int rowsUpdated = jdbcTemplate.update(leadershipQuery, lockName);
        return rowsUpdated > 0;
    }

}
