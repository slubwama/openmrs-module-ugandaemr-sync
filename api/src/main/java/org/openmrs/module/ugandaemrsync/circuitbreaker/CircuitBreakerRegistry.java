package org.openmrs.module.ugandaemrsync.circuitbreaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Registry for managing circuit breakers
 * Provides centralized management and monitoring of circuit breakers
 */
public class CircuitBreakerRegistry {

    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    private final AtomicInteger circuitBreakerCounter = new AtomicInteger(0);

    private static volatile CircuitBreakerRegistry instance;
    private static final Object LOCK = new Object();

    private CircuitBreakerRegistry() {
    }

    /**
     * Get singleton instance of the registry
     */
    public static CircuitBreakerRegistry getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new CircuitBreakerRegistry();
                }
            }
        }
        return instance;
    }

    /**
     * Get or create a circuit breaker with default configuration
     * @param name Unique name for the circuit breaker
     * @return Circuit breaker instance
     */
    public CircuitBreaker getCircuitBreaker(String name) {
        return getCircuitBreaker(name, CircuitBreakerConfig.getDefault());
    }

    /**
     * Get or create a circuit breaker with custom configuration
     * @param name Unique name for the circuit breaker
     * @param config Configuration for the circuit breaker
     * @return Circuit breaker instance
     */
    public CircuitBreaker getCircuitBreaker(String name, CircuitBreakerConfig config) {
        if (!config.isEnabled()) {
            return null; // Circuit breaker disabled
        }

        return circuitBreakers.computeIfAbsent(name, k -> {
            circuitBreakerCounter.incrementAndGet();
            return new CircuitBreaker(name, config);
        });
    }

    /**
     * Get all registered circuit breakers
     */
    public Map<String, CircuitBreaker> getAllCircuitBreakers() {
        return new ConcurrentHashMap<>(circuitBreakers);
    }

    /**
     * Reset a specific circuit breaker
     */
    public void resetCircuitBreaker(String name) {
        CircuitBreaker cb = circuitBreakers.get(name);
        if (cb != null) {
            cb.reset();
        }
    }

    /**
     * Reset all circuit breakers
     */
    public void resetAll() {
        circuitBreakers.values().forEach(CircuitBreaker::reset);
    }

    /**
     * Get count of registered circuit breakers
     */
    public int getCircuitBreakerCount() {
        return circuitBreakerCounter.get();
    }

    /**
     * Check if any circuit breakers are currently open
     */
    public boolean hasOpenCircuitBreakers() {
        return circuitBreakers.values().stream()
                .anyMatch(cb -> cb.getState() == CircuitBreaker.State.OPEN);
    }

    /**
     * Get statistics about all circuit breakers
     */
    public CircuitBreakerStats getStats() {
        int total = circuitBreakers.size();
        int open = 0;
        int closed = 0;
        int halfOpen = 0;
        int totalFailures = 0;
        int totalSuccesses = 0;

        for (CircuitBreaker cb : circuitBreakers.values()) {
            switch (cb.getState()) {
                case OPEN:
                    open++;
                    break;
                case CLOSED:
                    closed++;
                    break;
                case HALF_OPEN:
                    halfOpen++;
                    break;
            }
            totalFailures += cb.getFailureCount();
            totalSuccesses += cb.getSuccessCount();
        }

        return new CircuitBreakerStats(total, open, closed, halfOpen, totalFailures, totalSuccesses);
    }

    /**
     * Statistics class for circuit breaker registry
     */
    public static class CircuitBreakerStats {
        private final int total;
        private final int open;
        private final int closed;
        private final int halfOpen;
        private final int totalFailures;
        private final int totalSuccesses;

        public CircuitBreakerStats(int total, int open, int closed, int halfOpen,
                                   int totalFailures, int totalSuccesses) {
            this.total = total;
            this.open = open;
            this.closed = closed;
            this.halfOpen = halfOpen;
            this.totalFailures = totalFailures;
            this.totalSuccesses = totalSuccesses;
        }

        public int getTotal() {
            return total;
        }

        public int getOpen() {
            return open;
        }

        public int getClosed() {
            return closed;
        }

        public int getHalfOpen() {
            return halfOpen;
        }

        public int getTotalFailures() {
            return totalFailures;
        }

        public int getTotalSuccesses() {
            return totalSuccesses;
        }

        @Override
        public String toString() {
            return String.format("CircuitBreakerStats{total=%d, open=%d, closed=%d, halfOpen=%d, failures=%d, successes=%d}",
                    total, open, closed, halfOpen, totalFailures, totalSuccesses);
        }
    }
}