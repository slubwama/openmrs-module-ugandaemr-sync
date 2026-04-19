package org.openmrs.module.ugandaemrsync.util;

import org.apache.commons.logging.Log;
import org.openmrs.module.ugandaemrsync.exception.UgandaEMRSyncException;
import org.openmrs.module.ugandaemrsync.logging.StructuredLogger;

import java.util.function.Supplier;

/**
 * Centralized exception handling utilities
 * Provides consistent error handling patterns and reduces code duplication
 */
public class ExceptionUtils {

    private static final StructuredLogger log = StructuredLogger.getLogger(ExceptionUtils.class);

    /**
     * Execute operation with standardized exception handling
     * @param operation The operation to execute
     * @param operationName Name for logging purposes
     * @param defaultValue Value to return on exception
     * @param <T> Return type
     * @return Operation result or default value on exception
     */
    public static <T> T executeWithErrorHandling(Supplier<T> operation, String operationName, T defaultValue) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("Unexpected error in operation '" + operationName + "': " + e.getMessage(), e);
            return defaultValue;
        }
    }

    /**
     * Execute operation with standardized exception handling (no default value)
     * @param operation The operation to execute
     * @param operationName Name for logging purposes
     * @param <T> Return type
     * @return Operation result
     * @throws Exception if operation fails
     */
    public static <T> T executeWithErrorHandling(Supplier<T> operation, String operationName)
            throws Exception {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("Unexpected error in operation '" + operationName + "': " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Execute void operation with standardized exception handling
     * @param operation The operation to execute
     * @param operationName Name for logging purposes
     * @return true if successful, false if exception occurred
     */
    public static boolean executeVoidWithErrorHandling(Runnable operation, String operationName) {
        try {
            operation.run();
            return true;
        } catch (Exception e) {
            log.error("Unexpected error in operation '" + operationName + "': " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Wrap checked exceptions into UgandaEMRSyncException
     * @param exception The exception to wrap
     * @param errorCode Error code to use
     * @param context Additional context information
     * @return UgandaEMRSyncException
     */
    public static UgandaEMRSyncException wrapException(Exception exception,
                                                       UgandaEMRSyncException.ErrorCode errorCode,
                                                       String context) {
        String message = String.format("%s: %s", context, exception.getMessage());
        return new UgandaEMRSyncException(errorCode, message, exception);
    }

    /**
     * Log and rethrow exception with additional context
     * @param logger Logger to use
     * @param exception The exception to log and rethrow
     * @param context Additional context information
     * @throws UgandaEMRSyncException always
     */
    public static void logAndRethrow(Log logger, Exception exception, String context)
            throws UgandaEMRSyncException {
        logger.error(context + ": " + exception.getMessage(), exception);
        if (exception instanceof UgandaEMRSyncException) {
            throw (UgandaEMRSyncException) exception;
        }
        throw new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.SYNC_FAILED,
                context + " failed",
                exception
        );
    }

    /**
     * Create a standardized error response map
     * @param errorCode Error code
     * @param message Error message
     * @param details Additional details
     * @return Map containing error information
     */
    public static java.util.Map<String, Object> createErrorResponse(String errorCode, String message, Object details) {
        java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("details", details);
        errorResponse.put("timestamp", new java.util.Date());
        return errorResponse;
    }

    /**
     * Create a standardized success response map
     * @param data Response data
     * @return Map containing success information
     */
    public static java.util.Map<String, Object> createSuccessResponse(Object data) {
        java.util.Map<String, Object> successResponse = new java.util.HashMap<>();
        successResponse.put("success", true);
        successResponse.put("data", data);
        successResponse.put("timestamp", new java.util.Date());
        return successResponse;
    }

    /**
     * Check if exception is retryable
     * @param exception Exception to check
     * @return true if exception should be retried
     */
    public static boolean isRetryable(Exception exception) {
        if (exception instanceof UgandaEMRSyncException) {
            return ((UgandaEMRSyncException) exception).isRetryable();
        }

        // Network-related exceptions are typically retryable
        String exceptionClassName = exception.getClass().getSimpleName();
        return exceptionClassName.contains("Timeout") ||
               exceptionClassName.contains("Connect") ||
               exceptionClassName.contains("Socket");
    }

    /**
     * Safe sleep with exception handling
     * @param millis Time to sleep in milliseconds
     */
    public static void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Thread interrupted during sleep");
        }
    }
}