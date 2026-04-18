package org.openmrs.module.ugandaemrsync.dto.exception;

/**
 * Exception thrown when network/external service connections fail.
 * This exception is retryable and should trigger retry logic.
 */
public class ConnectionException extends LabResultProcessingException {

    private final String endpoint;
    private final int statusCode;
    private final long responseTime;

    public ConnectionException(String message, String endpoint, int statusCode, long responseTime) {
        super(message, ErrorType.CONNECTION_FAILED, null);
        this.endpoint = endpoint;
        this.statusCode = statusCode;
        this.responseTime = responseTime;
    }

    public ConnectionException(String message, String endpoint, Throwable cause) {
        super(message, ErrorType.CONNECTION_FAILED, null, cause);
        this.endpoint = endpoint;
        this.statusCode = -1;
        this.responseTime = -1;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getResponseTime() {
        return responseTime;
    }
}
