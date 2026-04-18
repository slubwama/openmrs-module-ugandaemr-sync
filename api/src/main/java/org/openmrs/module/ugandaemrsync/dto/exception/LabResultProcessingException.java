package org.openmrs.module.ugandaemrsync.dto.exception;

/**
 * Base exception for lab result processing errors.
 * Provides structured error information for better error handling and reporting.
 */
public class LabResultProcessingException extends Exception {

    private final String orderUuid;
    private final ErrorType errorType;
    private final boolean retryable;

    public enum ErrorType {
        CONNECTION_FAILED,
        INVALID_DATA,
        ORDER_NOT_FOUND,
        SYNC_TASK_FAILED,
        VALIDATION_FAILED,
        PROCESSING_FAILED
    }

    public LabResultProcessingException(String message, ErrorType errorType, String orderUuid) {
        super(message);
        this.errorType = errorType;
        this.orderUuid = orderUuid;
        this.retryable = determineRetryability(errorType);
    }

    public LabResultProcessingException(String message, ErrorType errorType, String orderUuid, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.orderUuid = orderUuid;
        this.retryable = determineRetryability(errorType);
    }

    private boolean determineRetryability(ErrorType errorType) {
        return errorType == ErrorType.CONNECTION_FAILED ||
               errorType == ErrorType.SYNC_TASK_FAILED;
    }

    public String getOrderUuid() {
        return orderUuid;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
