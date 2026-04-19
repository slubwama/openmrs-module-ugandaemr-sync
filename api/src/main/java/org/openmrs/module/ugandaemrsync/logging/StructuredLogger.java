package org.openmrs.module.ugandaemrsync.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ugandaemrsync.exception.UgandaEMRSyncException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Structured logging framework for UgandaEMR Sync module providing consistent logging format with contextual information.
 *
 * <p>This logger enhances standard logging by:</p>
 * <ul>
 *   <li>Adding correlation IDs for request tracing across components</li>
 *   <li>Providing structured context information with each log entry</li>
 *   <li>Tracking sync operations from start to completion</li>
 *   <li>Recording performance metrics automatically</li>
 *   <li>Monitoring external service calls with detailed timing</li>
 *   <li>Generating audit trails for compliance and debugging</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * StructuredLogger logger = StructuredLogger.getLogger(MyClass.class);
 *
 * // Start sync operation with correlation tracking
 * String correlationId = logger.logSyncStart("patient_sync", details);
 *
 * try {
 *     // Perform sync operation
 *     syncPatients();
 *     logger.logSyncComplete(correlationId, "patient_sync", results);
 * } catch (Exception e) {
 *     logger.logSyncFailure(correlationId, "patient_sync", e, context);
 * }
 * }</pre>
 *
 * <p><b>Performance Monitoring:</b></p>
 * <pre>{@code
 * long startTime = System.currentTimeMillis();
 * // ... perform operation ...
 * long duration = System.currentTimeMillis() - startTime;
 * logger.logPerformanceMetrics("patient_export", duration, patientCount, metrics);
 * }</pre>
 *
 * @author UgandaEMR Development Team
 * @version 2.0.6
 * @since 1.0
 */
public class StructuredLogger {

    private final Log log;
    private final String componentName;

    private StructuredLogger(String componentName) {
        this.log = LogFactory.getLog(componentName);
        this.componentName = componentName;
    }

    /**
     * Creates a structured logger for the given class.
     *
     * @param clazz the class to create a logger for
     * @return a new StructuredLogger instance
     */
    public static StructuredLogger getLogger(Class<?> clazz) {
        return new StructuredLogger(clazz.getSimpleName());
    }

    /**
     * Creates a structured logger with the specified component name.
     *
     * @param componentName the name of the logging component
     * @return a new StructuredLogger instance
     */
    public static StructuredLogger getLogger(String componentName) {
        return new StructuredLogger(componentName);
    }

    /**
     * Log structured information
     */
    public void info(String message, Map<String, Object> context) {
        if (log.isInfoEnabled()) {
            log.info(formatMessage(message, context));
        }
    }

    public void info(String message) {
        log.info(message);
    }

    /**
     * Log structured warning
     */
    public void warn(String message, Map<String, Object> context) {
        if (log.isWarnEnabled()) {
            log.warn(formatMessage(message, context));
        }
    }

    public void warn(String message) {
        log.warn(message);
    }

    public void warn(String message, Throwable throwable) {
        log.warn(message, throwable);
    }

    /**
     * Log structured error
     */
    public void error(String message, Map<String, Object> context) {
        if (log.isErrorEnabled()) {
            log.error(formatMessage(message, context));
        }
    }

    public void error(String message) {
        log.error(message);
    }

    public void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    public void error(String message, Throwable throwable, Map<String, Object> context) {
        if (log.isErrorEnabled()) {
            log.error(formatMessage(message, context), throwable);
        }
    }

    /**
     * Log structured debug information
     */
    public void debug(String message, Map<String, Object> context) {
        if (log.isDebugEnabled()) {
            log.debug(formatMessage(message, context));
        }
    }

    public void debug(String message) {
        log.debug(message);
    }

    /**
     * Log sync operation start
     */
    public String logSyncStart(String operationType, Map<String, Object> details) {
        String correlationId = generateCorrelationId();
        Map<String, Object> context = new HashMap<>(details);
        context.put("correlationId", correlationId);
        context.put("operationType", operationType);
        context.put("timestamp", new Date());

        info("SYNC_START: " + operationType, context);
        return correlationId;
    }

    /**
     * Log sync operation completion
     */
    public void logSyncComplete(String correlationId, String operationType,
                               Map<String, Object> results) {
        Map<String, Object> context = new HashMap<>(results);
        context.put("correlationId", correlationId);
        context.put("operationType", operationType);
        context.put("timestamp", new Date());

        info("SYNC_COMPLETE: " + operationType, context);
    }

    /**
     * Log sync operation failure
     */
    public void logSyncFailure(String correlationId, String operationType,
                              Exception exception, Map<String, Object> context) {
        Map<String, Object> errorContext = new HashMap<>(context);
        errorContext.put("correlationId", correlationId);
        errorContext.put("operationType", operationType);
        errorContext.put("timestamp", new Date());

        if (exception instanceof UgandaEMRSyncException) {
            UgandaEMRSyncException syncException = (UgandaEMRSyncException) exception;
            errorContext.put("errorCode", syncException.getErrorCode().getCode());
            errorContext.put("retryable", syncException.isRetryable());
        }

        error("SYNC_FAILED: " + operationType, exception, errorContext);
    }

    /**
     * Log performance metrics
     */
    public void logPerformanceMetrics(String operation, long durationMs,
                                     int recordCount, Map<String, Object> additionalMetrics) {
        Map<String, Object> metrics = new HashMap<>(additionalMetrics);
        metrics.put("operation", operation);
        metrics.put("durationMs", durationMs);
        metrics.put("recordCount", recordCount);
        metrics.put("recordsPerSecond", recordCount > 0 ? (recordCount * 1000.0 / durationMs) : 0);
        metrics.put("timestamp", new Date());

        info("PERFORMANCE_METRICS: " + operation, metrics);
    }

    /**
     * Log data validation failure
     */
    public void logValidationError(String field, Object value, String validationRule) {
        Map<String, Object> context = new HashMap<>();
        context.put("field", field);
        context.put("value", value);
        context.put("validationRule", validationRule);
        context.put("timestamp", new Date());

        warn("VALIDATION_ERROR: Field '" + field + "' failed validation", context);
    }

    /**
     * Log external service call
     */
    public String logExternalServiceCall(String serviceType, String endpoint,
                                        Map<String, Object> details) {
        String correlationId = generateCorrelationId();
        Map<String, Object> context = new HashMap<>(details);
        context.put("correlationId", correlationId);
        context.put("serviceType", serviceType);
        context.put("endpoint", endpoint);
        context.put("timestamp", new Date());

        info("EXTERNAL_CALL_START: " + serviceType + " -> " + endpoint, context);
        return correlationId;
    }

    /**
     * Log external service response
     */
    public void logExternalServiceResponse(String correlationId, String serviceType,
                                          int statusCode, long durationMs) {
        Map<String, Object> context = new HashMap<>();
        context.put("correlationId", correlationId);
        context.put("serviceType", serviceType);
        context.put("statusCode", statusCode);
        context.put("durationMs", durationMs);
        context.put("timestamp", new Date());

        info("EXTERNAL_CALL_COMPLETE: " + serviceType, context);
    }

    private String formatMessage(String message, Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return message;
        }

        StringBuilder sb = new StringBuilder(message);
        sb.append(" {");
        boolean first = true;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}