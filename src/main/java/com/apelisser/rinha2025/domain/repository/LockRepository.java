package com.apelisser.rinha2025.domain.repository;

public interface LockRepository {

    boolean tryAcquireLock(String lockName, int lockIntervalInMillis);

}
