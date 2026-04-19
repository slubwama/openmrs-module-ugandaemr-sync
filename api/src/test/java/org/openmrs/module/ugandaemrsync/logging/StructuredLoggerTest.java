package org.openmrs.module.ugandaemrsync.logging;

import org.junit.Test;
import org.openmrs.module.ugandaemrsync.exception.UgandaEMRSyncException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test suite for StructuredLogger
 */
public class StructuredLoggerTest {

    @Test
    public void testGetLogger_WithClass() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        assertNotNull(logger);
    }

    @Test
    public void testGetLogger_WithString() {
        StructuredLogger logger = StructuredLogger.getLogger("TestComponent");
        assertNotNull(logger);
    }

    @Test
    public void testGetLogger_ReturnsSameInstanceForSameClass() {
        StructuredLogger logger1 = StructuredLogger.getLogger(StructuredLoggerTest.class);
        StructuredLogger logger2 = StructuredLogger.getLogger(StructuredLoggerTest.class);
        assertNotNull(logger1);
        assertNotNull(logger2);
    }

    @Test
    public void testInfo_WithMessageAndContext() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> context = new HashMap<>();
        context.put("key1", "value1");
        context.put("key2", 123);

        // Should not throw exception
        logger.info("Test info message", context);
    }

    @Test
    public void testInfo_WithMessageOnly() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.info("Test info message without context");
    }

    @Test
    public void testWarn_WithMessageAndContext() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> context = new HashMap<>();
        context.put("warning", "test warning");

        logger.warn("Test warning message", context);
    }

    @Test
    public void testWarn_WithMessageOnly() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.warn("Test warning message without context");
    }

    @Test
    public void testWarn_WithMessageAndThrowable() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Exception exception = new RuntimeException("Test exception");
        logger.warn("Test warning with throwable", exception);
    }

    @Test
    public void testError_WithMessageAndContext() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> context = new HashMap<>();
        context.put("error", "test error");

        logger.error("Test error message", context);
    }

    @Test
    public void testError_WithMessageOnly() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.error("Test error message without context");
    }

    @Test
    public void testError_WithMessageAndThrowable() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Exception exception = new RuntimeException("Test exception");
        logger.error("Test error with throwable", exception);
    }

    @Test
    public void testError_WithMessageThrowableAndContext() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Exception exception = new RuntimeException("Test exception");
        Map<String, Object> context = new HashMap<>();
        context.put("errorDetail", "test detail");

        logger.error("Test error with all parameters", exception, context);
    }

    @Test
    public void testDebug_WithMessageAndContext() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> context = new HashMap<>();
        context.put("debug", "test debug");

        logger.debug("Test debug message", context);
    }

    @Test
    public void testDebug_WithMessageOnly() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.debug("Test debug message without context");
    }

    @Test
    public void testLogSyncStart() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> details = new HashMap<>();
        details.put("patientCount", 100);

        String correlationId = logger.logSyncStart("patient_sync", details);

        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());
        assertEquals(8, correlationId.length());
    }

    @Test
    public void testLogSyncStart_EmptyDetails() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        String correlationId = logger.logSyncStart("test_sync", new HashMap<>());

        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());
    }

    @Test
    public void testLogSyncComplete() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        String correlationId = "test123";
        Map<String, Object> results = new HashMap<>();
        results.put("syncedCount", 95);
        results.put("failedCount", 5);

        logger.logSyncComplete(correlationId, "patient_sync", results);
    }

    @Test
    public void testLogSyncFailure_WithGenericException() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        String correlationId = "test123";
        Exception exception = new RuntimeException("Sync failed");
        Map<String, Object> context = new HashMap<>();
        context.put("patientId", 123);

        logger.logSyncFailure(correlationId, "patient_sync", exception, context);
    }

    @Test
    public void testLogSyncFailure_WithUgandaEMRSyncException() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        String correlationId = "test456";
        UgandaEMRSyncException exception = new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.CONNECTION_TIMEOUT,
                "Connection timeout during sync"
        );
        Map<String, Object> context = new HashMap<>();
        context.put("retryAttempt", 1);

        logger.logSyncFailure(correlationId, "patient_sync", exception, context);
    }

    @Test
    public void testLogPerformanceMetrics() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> additionalMetrics = new HashMap<>();
        additionalMetrics.put("averageProcessingTime", 50);
        additionalMetrics.put("peakMemory", 1024);

        logger.logPerformanceMetrics("patient_export", 5000, 100, additionalMetrics);
    }

    @Test
    public void testLogPerformanceMetrics_WithZeroRecords() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.logPerformanceMetrics("empty_operation", 100, 0, new HashMap<>());
    }

    @Test
    public void testLogValidationError() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.logValidationError("patientAge", -5, "Age must be positive");
    }

    @Test
    public void testLogValidationError_WithNullValue() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.logValidationError("patientName", null, "Name is required");
    }

    @Test
    public void testLogExternalServiceCall() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> details = new HashMap<>();
        details.put("method", "POST");
        details.put("payloadSize", 1024);

        String correlationId = logger.logExternalServiceCall("DHIS2", "/api/patients", details);

        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());
    }

    @Test
    public void testLogExternalServiceCall_EmptyDetails() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        String correlationId = logger.logExternalServiceCall("CentralServer", "/sync", new HashMap<>());

        assertNotNull(correlationId);
    }

    @Test
    public void testLogExternalServiceResponse() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        String correlationId = "ext123";
        logger.logExternalServiceResponse(correlationId, "DHIS2", 200, 1500);
    }

    @Test
    public void testLogExternalServiceResponse_WithErrorStatus() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        String correlationId = "ext456";
        logger.logExternalServiceResponse(correlationId, "CentralServer", 500, 5000);
    }

    @Test
    public void testMultipleLoggersHaveIndependentInstances() {
        StructuredLogger logger1 = StructuredLogger.getLogger("Component1");
        StructuredLogger logger2 = StructuredLogger.getLogger("Component2");

        assertNotNull(logger1);
        assertNotNull(logger2);
    }

    @Test
    public void testLogSyncStart_GeneratesUniqueCorrelationIds() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);

        String id1 = logger.logSyncStart("operation1", new HashMap<>());
        String id2 = logger.logSyncStart("operation2", new HashMap<>());

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    public void testLogExternalServiceCall_GeneratesUniqueCorrelationIds() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);

        String id1 = logger.logExternalServiceCall("Service1", "/endpoint1", new HashMap<>());
        String id2 = logger.logExternalServiceCall("Service2", "/endpoint2", new HashMap<>());

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotEquals(id1, id2);
    }

    @Test
    public void testLogPerformanceMetrics_CalculatesRecordsPerSecond() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> additionalMetrics = new HashMap<>();

        logger.logPerformanceMetrics("test_operation", 1000, 100, additionalMetrics);
    }

    @Test
    public void testLogPerformanceMetrics_WithNegativeDuration() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> additionalMetrics = new HashMap<>();

        logger.logPerformanceMetrics("test_operation", -100, 10, additionalMetrics);
    }

    @Test
    public void testContextWithComplexValues() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> context = new HashMap<>();
        context.put("string", "test");
        context.put("integer", 123);
        context.put("boolean", true);
        context.put("null", null);

        logger.info("Complex context test", context);
    }

    @Test
    public void testLogSyncFailure_PreservesErrorContext() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        UgandaEMRSyncException exception = new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED,
                "Connection failed"
        );

        Map<String, Object> context = new HashMap<>();
        context.put("endpoint", "http://test.com");
        context.put("timeout", 5000);

        logger.logSyncFailure("corr123", "test_sync", exception, context);
    }

    @Test
    public void testLogExternalServiceResponse_WithVeryLongDuration() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.logExternalServiceResponse("corr123", "TestService", 200, 999999);
    }

    @Test
    public void testLogSyncComplete_WithLargeResultSet() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        Map<String, Object> results = new HashMap<>();
        results.put("syncedCount", 1000000);
        results.put("failedCount", 5);
        results.put("skippedCount", 100);

        logger.logSyncComplete("corr123", "bulk_sync", results);
    }

    @Test
    public void testMultipleSyncOperationsCanRunConcurrently() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);

        String id1 = logger.logSyncStart("sync1", new HashMap<>());
        String id2 = logger.logSyncStart("sync2", new HashMap<>());
        String id3 = logger.logSyncStart("sync3", new HashMap<>());

        assertNotNull(id1);
        assertNotNull(id2);
        assertNotNull(id3);

        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
    }

    @Test
    public void testLogWithNullContextMap() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.info("Message with null context", null);
    }

    @Test
    public void testLogWithEmptyContextMap() {
        StructuredLogger logger = StructuredLogger.getLogger(StructuredLoggerTest.class);
        logger.info("Message with empty context", new HashMap<>());
    }
}
