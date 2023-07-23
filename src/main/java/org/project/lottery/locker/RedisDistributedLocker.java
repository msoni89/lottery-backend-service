package org.project.lottery.locker;

import lombok.extern.slf4j.Slf4j;
import org.project.lottery.exceptions.FailedToAcquireLockException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RedisDistributedLocker {

    private static final long DEFAULT_RETRY_TIME = 100L;
    private static final int DEFAULT_RETRY_MAX_ATTEMPT = 5;

    private final ValueOperations<String, Object> valueOps;

    public RedisDistributedLocker(final RedisTemplate<String, Object> redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
    }

    /**
     * Acquires a lock for the given key and executes the given task. If the lock cannot be acquired, the task will not be executed.
     *
     * @param key                The key to lock.
     * @param lockTimeoutSeconds The number of seconds to wait for the lock to be acquired.
     * @param task               The task to execute.
     * @param <T>                The type of the task result.
     * @return A `LockExecutionResult` object that indicates whether the lock was acquired successfully and the result of the task.
     */
    @Retryable(
            retryFor = FailedToAcquireLockException.class,
            maxAttempts = DEFAULT_RETRY_MAX_ATTEMPT,
            backoff = @Backoff(delay = DEFAULT_RETRY_TIME)
    )
    public <T> LockExecutionResult<T> lock(final String key,
                                           final int lockTimeoutSeconds,
                                           final Callable<T> task) {
        // Try to acquire the lock.
        final Boolean lockAcquired = valueOps.setIfAbsent(key, key, lockTimeoutSeconds, TimeUnit.SECONDS);

        // If the lock was not acquired, return an error.
        if (lockAcquired == Boolean.FALSE) {
            log.error("Failed to acquire lock for key '{}'", key);
            throw new FailedToAcquireLockException(String.format("Failed to acquire lock for key '{}'", key));
        }

        // The lock was acquired, so execute the task.
        log.info("Successfully acquired lock for key '{}'", key);
        try {
            T taskResult = task.call();
            return LockExecutionResult.buildLockAcquiredResult(taskResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return LockExecutionResult.buildLockAcquiredWithException(e);
        } finally {
            // Release the lock.
            releaseLock(key);
        }
    }

    /**
     * Releases the lock for the given key.
     *
     * @param key The key to release the lock for.
     */
    private void releaseLock(final String key) {
        valueOps.getOperations().delete(key);
    }
}
