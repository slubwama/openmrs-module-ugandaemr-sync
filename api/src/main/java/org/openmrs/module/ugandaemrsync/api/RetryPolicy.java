package org.openmrs.module.ugandaemrsync.api;

import org.openmrs.module.ugandaemrsync.exception.UgandaEMRSyncException;

import java.util.concurrent.TimeUnit;

/**
 * Retry policy configuration for handling transient failures
 * Implements exponential backoff strategy for resilient operations
 */
public class RetryPolicy {

    private final int maxAttempts;
    private final long initialDelayMs;
    private final long maxDelayMs;
    private final double backoffMultiplier;
    private final Class<? extends Throwable>[] retryableExceptions;

    private RetryPolicy(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.initialDelayMs = builder.initialDelayMs;
        this.maxDelayMs = builder.maxDelayMs;
        this.backoffMultiplier = builder.backoffMultiplier;
        this.retryableExceptions = builder.retryableExceptions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    public long getMaxDelayMs() {
        return maxDelayMs;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    /**
     * Calculate delay for a given attempt using exponential backoff
     * @param attemptNumber The attempt number (0-based)
     * @return Delay in milliseconds
     */
    public long getDelayForAttempt(int attemptNumber) {
        if (attemptNumber < 0) {
            return initialDelayMs;
        }

        long delay = (long) (initialDelayMs * Math.pow(backoffMultiplier, attemptNumber));
        return Math.min(delay, maxDelayMs);
    }

    /**
     * Determine if an exception is retryable based on policy configuration
     */
    public boolean isRetryable(Throwable exception) {
        // Check UgandaEMRSyncException retryable flag
        if (exception instanceof UgandaEMRSyncException) {
            return ((UgandaEMRSyncException) exception).isRetryable();
        }

        // Check against configured retryable exceptions
        if (retryableExceptions != null) {
            for (Class<? extends Throwable> retryableType : retryableExceptions) {
                if (retryableType.isInstance(exception)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static class Builder {
        private int maxAttempts = 3;
        private long initialDelayMs = 1000; // 1 second
        private long maxDelayMs = 30000; // 30 seconds
        private double backoffMultiplier = 2.0;
        private Class<? extends Throwable>[] retryableExceptions;

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder initialDelay(long delay, TimeUnit unit) {
            this.initialDelayMs = unit.toMillis(delay);
            return this;
        }

        public Builder maxDelay(long delay, TimeUnit unit) {
            this.maxDelayMs = unit.toMillis(delay);
            return this;
        }

        public Builder backoffMultiplier(double multiplier) {
            this.backoffMultiplier = multiplier;
            return this;
        }

        @SafeVarargs
        public final Builder retryOnExceptions(Class<? extends Throwable>... exceptions) {
            this.retryableExceptions = exceptions;
            return this;
        }

        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }

    /**
     * Default retry policy for HTTP operations
     */
    public static RetryPolicy getDefaultHttpRetryPolicy() {
        return RetryPolicy.builder()
                .maxAttempts(3)
                .initialDelay(1, TimeUnit.SECONDS)
                .maxDelay(30, TimeUnit.SECONDS)
                .backoffMultiplier(2.0)
                .build();
    }

    /**
     * Conservative retry policy for critical operations
     */
    public static RetryPolicy getConservativeRetryPolicy() {
        return RetryPolicy.builder()
                .maxAttempts(2)
                .initialDelay(2, TimeUnit.SECONDS)
                .maxDelay(10, TimeUnit.SECONDS)
                .backoffMultiplier(1.5)
                .build();
    }

    /**
     * Aggressive retry policy for non-critical operations
     */
    public static RetryPolicy getAggressiveRetryPolicy() {
        return RetryPolicy.builder()
                .maxAttempts(5)
                .initialDelay(500, TimeUnit.MILLISECONDS)
                .maxDelay(60, TimeUnit.SECONDS)
                .backoffMultiplier(2.0)
                .build();
    }
}