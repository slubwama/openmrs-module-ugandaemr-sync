package org.openmrs.module.ugandaemrsync.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ugandaemrsync.exception.UgandaEMRSyncException;

import java.util.concurrent.Callable;

/**
 * Executes operations with retry logic and exponential backoff
 * Provides resilient execution for transient failure scenarios
 */
public class RetryExecutor {

    private static final Log log = LogFactory.getLog(RetryExecutor.class);

    private final RetryPolicy retryPolicy;

    public RetryExecutor(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    /**
     * Execute a callable with retry logic
     * @param callable The operation to execute
     * @param <T> Return type
     * @return Result of the operation
     * @throws Exception if all retry attempts fail
     */
    public <T> T execute(Callable<T> callable) throws Exception {
        return execute(callable, "Operation");
    }

    /**
     * Execute a callable with retry logic and custom operation name
     * @param callable The operation to execute
     * @param operationName Name of the operation for logging
     * @param <T> Return type
     * @return Result of the operation
     * @throws Exception if all retry attempts fail
     */
    public <T> T execute(Callable<T> callable, String operationName) throws Exception {
        Exception lastException = null;

        for (int attempt = 0; attempt < retryPolicy.getMaxAttempts(); attempt++) {
            try {
                if (attempt > 0) {
                    long delay = retryPolicy.getDelayForAttempt(attempt - 1);
                    log.warn(String.format("%s failed on attempt %d/%d. Retrying after %dms...",
                            operationName, attempt + 1, retryPolicy.getMaxAttempts(), delay));
                    Thread.sleep(delay);
                }

                T result = callable.call();
                if (attempt > 0) {
                    log.info(String.format("%s succeeded on attempt %d/%d", operationName, attempt + 1,
                            retryPolicy.getMaxAttempts()));
                }
                return result;

            } catch (Exception e) {
                lastException = e;

                // Check if exception is retryable
                if (!retryPolicy.isRetryable(e)) {
                    log.error(String.format("%s failed with non-retryable exception: %s",
                            operationName, e.getMessage()));
                    throw e;
                }

                // Log retry attempt
                if (attempt < retryPolicy.getMaxAttempts() - 1) {
                    log.warn(String.format("%s failed on attempt %d/%d: %s",
                            operationName, attempt + 1, retryPolicy.getMaxAttempts(), e.getMessage()));
                } else {
                    log.error(String.format("%s failed after %d attempts: %s",
                            operationName, retryPolicy.getMaxAttempts(), e.getMessage()));
                }
            }
        }

        // All retry attempts exhausted
        throw new Exception(String.format("%s failed after %d attempts", operationName,
                retryPolicy.getMaxAttempts()), lastException);
    }

    /**
     * Execute a runnable with retry logic (no return value)
     * @param runnable The operation to execute
     * @param operationName Name of the operation for logging
     * @throws Exception if all retry attempts fail
     */
    public void execute(Runnable runnable, String operationName) throws Exception {
        execute(() -> {
            runnable.run();
            return null;
        }, operationName);
    }

    /**
     * Create a retry executor with default HTTP retry policy
     */
    public static RetryExecutor withDefaultRetryPolicy() {
        return new RetryExecutor(RetryPolicy.getDefaultHttpRetryPolicy());
    }

    /**
     * Create a retry executor with conservative retry policy
     */
    public static RetryExecutor withConservativeRetryPolicy() {
        return new RetryExecutor(RetryPolicy.getConservativeRetryPolicy());
    }

    /**
     * Create a retry executor with aggressive retry policy
     */
    public static RetryExecutor withAggressiveRetryPolicy() {
        return new RetryExecutor(RetryPolicy.getAggressiveRetryPolicy());
    }
}