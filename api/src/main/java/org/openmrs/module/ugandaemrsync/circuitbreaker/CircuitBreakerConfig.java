package org.openmrs.module.ugandaemrsync.circuitbreaker;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for circuit breaker behavior
 * Provides default configurations and builder pattern for customization
 */
public class CircuitBreakerConfig {

    private final int failureThreshold;
    private final long openTimeoutMs;
    private final int requiredSuccessCount;
    private final boolean enabled;

    private CircuitBreakerConfig(Builder builder) {
        this.failureThreshold = builder.failureThreshold;
        this.openTimeoutMs = builder.openTimeoutMs;
        this.requiredSuccessCount = builder.requiredSuccessCount;
        this.enabled = builder.enabled;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public long getOpenTimeoutMs() {
        return openTimeoutMs;
    }

    public int getRequiredSuccessCount() {
        return requiredSuccessCount;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int failureThreshold = 5;
        private long openTimeoutMs = 60000; // 1 minute
        private int requiredSuccessCount = 2;
        private boolean enabled = true;

        public Builder failureThreshold(int threshold) {
            this.failureThreshold = threshold;
            return this;
        }

        public Builder openTimeout(long timeout, TimeUnit unit) {
            this.openTimeoutMs = unit.toMillis(timeout);
            return this;
        }

        public Builder requiredSuccessCount(int count) {
            this.requiredSuccessCount = count;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public CircuitBreakerConfig build() {
            return new CircuitBreakerConfig(this);
        }
    }

    // Default configurations for different scenarios

    /**
     * Default configuration for general HTTP operations
     */
    public static CircuitBreakerConfig getDefault() {
        return CircuitBreakerConfig.builder()
                .failureThreshold(5)
                .openTimeout(1, TimeUnit.MINUTES)
                .requiredSuccessCount(2)
                .enabled(true)
                .build();
    }

    /**
     * Conservative configuration for critical operations
     * Fewer failures tolerated, longer timeout before retry
     */
    public static CircuitBreakerConfig getConservative() {
        return CircuitBreakerConfig.builder()
                .failureThreshold(3)
                .openTimeout(2, TimeUnit.MINUTES)
                .requiredSuccessCount(3)
                .enabled(true)
                .build();
    }

    /**
     * Aggressive configuration for non-critical operations
     * More failures tolerated, shorter timeout
     */
    public static CircuitBreakerConfig getAggressive() {
        return CircuitBreakerConfig.builder()
                .failureThreshold(10)
                .openTimeout(30, TimeUnit.SECONDS)
                .requiredSuccessCount(1)
                .enabled(true)
                .build();
    }

    /**
     * Configuration with circuit breaker disabled
     */
    public static CircuitBreakerConfig getDisabled() {
        return CircuitBreakerConfig.builder()
                .enabled(false)
                .build();
    }
}