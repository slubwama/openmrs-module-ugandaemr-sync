package org.openmrs.module.ugandaemrsync.exception;

/**
 * Base exception class for UgandaEMR Sync module providing structured error categorization and handling.
 *
 * <p>This exception class enhances error handling by:</p>
 * <ul>
 *   <li>Categorizing errors into specific types with error codes</li>
 *   <li>Automatically determining retry eligibility for each error type</li>
 *   <li>Preserving contextual information for debugging and monitoring</li>
 *   <li>Providing structured error codes for programmatic error handling</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * try {
 *     syncData();
 * } catch (UgandaEMRSyncException e) {
 *     if (e.isRetryable()) {
 *         // Implement retry logic
 *     }
 *     if (e.getErrorCode() == UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR) {
 *         // Handle validation errors specifically
 *     }
 *     log.error("Sync failed: " + e.getErrorCode().getCode(), e);
 * }
 * }</pre>
 *
 * <p><b>Error Code Format:</b> Each error code follows the format PREFIX_### where:</p>
 * <ul>
 *   <li>CON_* - Connection and network errors</li>
 *   <li>HTTP_* - HTTP protocol errors</li>
 *   <li>VAL_* - Data validation errors</li>
 *   <li>CFG_* - Configuration errors</li>
 *   <li>SYNC_* - Synchronization operation errors</li>
 *   <li>EXT_* - External service errors</li>
 *   <li>DB_* - Database operation errors</li>
 * </ul>
 *
 * @author UgandaEMR Development Team
 * @version 2.0.6
 * @see ErrorCode
 * @since 1.0
 */
public class UgandaEMRSyncException extends Exception {

    private final ErrorCode errorCode;
    private final boolean retryable;
    private final Object context;

    /**
     * Enumeration of all error codes used in the UgandaEMR Sync module.
     *
     * <p>Each error code represents a specific category of error that can occur during
     * synchronization operations. Error codes are used for:</p>
     * <ul>
     *   <li>Programmatic error handling and routing</li>
     *   <li>Monitoring and alerting systems</li>
     *   <li>Audit trail generation</li>
     *   <li>User-friendly error message display</li>
     * </ul>
     */
    public enum ErrorCode {
        // Network and connectivity errors
        /** Failed to establish network connection */
        CONNECTION_FAILED("CON_001", "Failed to establish connection"),
        /** Connection attempt timed out */
        CONNECTION_TIMEOUT("CON_002", "Connection timeout occurred"),
        /** Read operation timed out */
        READ_TIMEOUT("CON_003", "Read timeout occurred"),

        // HTTP errors
        /** HTTP client error (4xx status codes) */
        HTTP_CLIENT_ERROR("HTTP_4XX", "HTTP client error occurred"),
        /** HTTP server error (5xx status codes) */
        HTTP_SERVER_ERROR("HTTP_5XX", "HTTP server error occurred"),
        /** Authentication credentials invalid or missing */
        HTTP_UNAUTHORIZED("HTTP_401", "Authentication failed"),
        /** Access to resource forbidden despite valid authentication */
        HTTP_FORBIDDEN("HTTP_403", "Access forbidden"),

        // Data errors
        /** Input data failed validation rules */
        VALIDATION_ERROR("VAL_001", "Data validation failed"),
        /** Data integrity constraints violated */
        DATA_INTEGRITY_ERROR("VAL_002", "Data integrity check failed"),
        /** Failed to parse data from expected format */
        PARSING_ERROR("VAL_003", "Data parsing failed"),

        // Configuration errors
        /** Module or system configuration error */
        CONFIGURATION_ERROR("CFG_001", "Configuration error"),
        /** Required configuration property missing */
        MISSING_PROPERTY("CFG_002", "Required property missing"),

        // Business logic errors
        /** Synchronization operation failed */
        SYNC_FAILED("SYNC_001", "Synchronization operation failed"),
        /** Batch processing operation failed */
        BATCH_PROCESSING_ERROR("SYNC_002", "Batch processing failed"),

        // External service errors
        /** External service unavailable or unreachable */
        EXTERNAL_SERVICE_UNAVAILABLE("EXT_001", "External service unavailable"),
        /** External service request timed out */
        EXTERNAL_SERVICE_TIMEOUT("EXT_002", "External service timeout"),

        // Database errors
        /** Database operation failed */
        DATABASE_ERROR("DB_001", "Database operation failed"),
        /** Database query execution timed out */
        QUERY_TIMEOUT("DB_002", "Database query timeout");

        private final String code;
        private final String description;

        ErrorCode(String code, String description) {
            this.code = code;
            this.description = description;
        }

        /**
         * Gets the error code string (e.g., "CON_001").
         * @return the error code string
         */
        public String getCode() {
            return code;
        }

        /**
         * Gets the human-readable error description.
         * @return the error description
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * Creates a new UgandaEMRSyncException with the specified error code and message.
     *
     * @param errorCode the specific error code categorizing this error
     * @param message a detailed message describing what went wrong
     */
    public UgandaEMRSyncException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.retryable = determineRetryable(errorCode);
        this.context = null;
    }

    /**
     * Creates a new UgandaEMRSyncException with error code, message, and cause.
     *
     * @param errorCode the specific error code categorizing this error
     * @param message a detailed message describing what went wrong
     * @param cause the underlying exception that caused this error
     */
    public UgandaEMRSyncException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryable = determineRetryable(errorCode);
        this.context = null;
    }

    /**
     * Creates a new UgandaEMRSyncException with error code, message, cause, and context.
     *
     * @param errorCode the specific error code categorizing this error
     * @param message a detailed message describing what went wrong
     * @param cause the underlying exception that caused this error
     * @param context additional contextual information for debugging
     */
    public UgandaEMRSyncException(ErrorCode errorCode, String message, Throwable cause, Object context) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryable = determineRetryable(errorCode);
        this.context = context;
    }

    /**
     * Determines if an error code indicates a retryable error.
     *
     * <p>Retryable errors include:</p>
     * <ul>
     *   <li>Network timeouts and connection failures</li>
     *   <li>HTTP 5xx server errors</li>
     *   <li>External service availability issues</li>
     * </ul>
     *
     * <p>Non-retryable errors include:</p>
     * <ul>
     *   <li>HTTP 4xx client errors (except in specific retry scenarios)</li>
     *   <li>Validation and data integrity errors</li>
     *   <li>Configuration errors</li>
     * </ul>
     *
     * @param errorCode the error code to evaluate
     * @return true if the error should be retried, false otherwise
     */
    private boolean determineRetryable(ErrorCode errorCode) {
        // Network and timeout errors are generally retryable
        if (errorCode.name().contains("TIMEOUT") ||
            errorCode.name().contains("CONNECTION")) {
            return true;
        }

        // Server errors (5xx) are retryable, client errors (4xx) are not
        if (errorCode == ErrorCode.HTTP_SERVER_ERROR) {
            return true;
        }

        // External service issues are often retryable
        if (errorCode.name().contains("EXTERNAL_SERVICE")) {
            return true;
        }

        return false;
    }

    /**
     * Gets the error code that categorizes this exception.
     *
     * @return the error code, or null if not set
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Checks if this exception represents a retryable error.
     *
     * <p>Retryable errors are typically transient failures that may succeed
     * if the operation is attempted again after a delay.</p>
     *
     * @return true if the error should be retried, false otherwise
     */
    public boolean isRetryable() {
        return retryable;
    }

    /**
     * Gets additional context information associated with this exception.
     *
     * @return the context object, or null if no context was provided
     */
    public Object getContext() {
        return context;
    }

    /**
     * Returns a formatted string representation of this exception including
     * error code, description, and message.
     *
     * @return formatted string in format "[CODE] Description: message"
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", errorCode.getCode(), errorCode.getDescription(), getMessage());
    }
}