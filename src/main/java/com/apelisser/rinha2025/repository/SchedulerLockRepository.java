package com.apelisser.rinha2025.repository;

public interface SchedulerLockRepository {

    boolean tryAcquireLock(String lockName, int lockIntervalInSeconds);

}
