package org.openmrs.module.ugandaemrsync.circuitbreaker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Circuit Breaker implementation for preventing cascading failures
 * Implements the standard circuit breaker pattern with states: CLOSED, OPEN, HALF_OPEN
 */
public class CircuitBreaker {

    private static final Log log = LogFactory.getLog(CircuitBreaker.class);

    private final String name;
    private final CircuitBreakerConfig config;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong lastStateChangeTime = new AtomicLong(System.currentTimeMillis());

    private volatile State state = State.CLOSED;

    public enum State {
        CLOSED("Circuit is closed - requests pass through"),
        OPEN("Circuit is open - requests are blocked"),
        HALF_OPEN("Circuit is half-open - testing service availability");

        private final String description;

        State(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public CircuitBreaker(String name, CircuitBreakerConfig config) {
        this.name = name;
        this.config = config;
    }

    /**
     * Attempt to execute an operation through the circuit breaker
     * @param operation The operation to execute
     * @param <T> Return type
     * @return Result of the operation
     * @throws Exception if circuit is open or operation fails
     */
    public <T> T execute(CircuitBreakerOperation<T> operation) throws Exception {
        if (!allowRequest()) {
            throw new CircuitBreakerOpenException(
                    String.format("Circuit breaker '%s' is OPEN - rejecting requests", name));
        }

        try {
            T result = operation.execute();
            recordSuccess();
            return result;

        } catch (Exception e) {
            recordFailure();
            throw e;
        }
    }

    /**
     * Determine if a request should be allowed through the circuit breaker
     */
    private boolean allowRequest() {
        long now = System.currentTimeMillis();

        synchronized (this) {
            switch (state) {
                case CLOSED:
                    return true; // Normal operation

                case OPEN:
                    // Check if we should transition to HALF_OPEN
                    if (now - lastStateChangeTime.get() >= config.getOpenTimeoutMs()) {
                        transitionTo(State.HALF_OPEN);
                        log.info(String.format("Circuit breaker '%s' transitioning from OPEN to HALF_OPEN", name));
                        return true;
                    }
                    return false; // Circuit is open, reject requests

                case HALF_OPEN:
                    return true; // Allow test requests

                default:
                    return false;
            }
        }
    }

    /**
     * Record a successful operation
     */
    private void recordSuccess() {
        int successes = successCount.incrementAndGet();
        failureCount.set(0);

        if (state == State.HALF_OPEN) {
            if (successes >= config.getRequiredSuccessCount()) {
                transitionTo(State.CLOSED);
                log.info(String.format("Circuit breaker '%s' transitioning from HALF_OPEN to CLOSED", name));
            }
        }
    }

    /**
     * Record a failed operation
     */
    private void recordFailure() {
        int failures = failureCount.incrementAndGet();
        successCount.set(0);
        lastFailureTime.set(System.currentTimeMillis());

        if (state == State.HALF_OPEN) {
            transitionTo(State.OPEN);
            log.warn(String.format("Circuit breaker '%s' transitioning from HALF_OPEN to OPEN", name));
        } else if (failures >= config.getFailureThreshold()) {
            transitionTo(State.OPEN);
            log.warn(String.format("Circuit breaker '%s' transitioning from CLOSED to OPEN after %d failures",
                    name, failures));
        }
    }

    /**
     * Transition to a new state
     */
    private void transitionTo(State newState) {
        if (this.state != newState) {
            log.info(String.format("Circuit breaker '%s' state change: %s -> %s",
                    name, this.state, newState));
            this.state = newState;
            lastStateChangeTime.set(System.currentTimeMillis());

            // Reset counters when leaving OPEN state
            if (newState == State.CLOSED || newState == State.HALF_OPEN) {
                failureCount.set(0);
                successCount.set(0);
            }
        }
    }

    public State getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public int getFailureCount() {
        return failureCount.get();
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public long getLastFailureTime() {
        return lastFailureTime.get();
    }

    /**
     * Reset the circuit breaker to CLOSED state
     */
    public void reset() {
        synchronized (this) {
            transitionTo(State.CLOSED);
            failureCount.set(0);
            successCount.set(0);
        }
    }

    @Override
    public String toString() {
        return String.format("CircuitBreaker{name='%s', state=%s, failures=%d, successes=%d}",
                name, state, failureCount.get(), successCount.get());
    }

    /**
     * Functional interface for operations executed through circuit breaker
     */
    @FunctionalInterface
    public interface CircuitBreakerOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Custom exception for when circuit breaker is open
     */
    public static class CircuitBreakerOpenException extends Exception {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}