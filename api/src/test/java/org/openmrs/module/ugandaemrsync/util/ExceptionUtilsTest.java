package org.openmrs.module.ugandaemrsync.util;

import org.apache.commons.logging.Log;
import org.junit.Test;
import org.openmrs.module.ugandaemrsync.exception.UgandaEMRSyncException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test suite for ExceptionUtils
 */
public class ExceptionUtilsTest {

    @Test
    public void testExecuteWithErrorHandling_Success() {
        String result = ExceptionUtils.executeWithErrorHandling(
                () -> "success",
                "testOperation",
                "default"
        );

        assertEquals("success", result);
    }

    @Test
    public void testExecuteWithErrorHandling_ExceptionReturnsDefault() {
        String result = ExceptionUtils.executeWithErrorHandling(
                () -> { throw new RuntimeException("Test error"); },
                "testOperation",
                "default"
        );

        assertEquals("default", result);
    }

    @Test
    public void testExecuteWithErrorHandling_WithInteger() {
        Integer result = ExceptionUtils.executeWithErrorHandling(
                () -> 42,
                "testOperation",
                0
        );

        assertEquals(Integer.valueOf(42), result);
    }

    @Test
    public void testExecuteWithErrorHandling_WithNull() {
        String result = ExceptionUtils.executeWithErrorHandling(
                () -> null,
                "testOperation",
                "default"
        );

        assertNull(result);
    }

    @Test(expected = Exception.class)
    public void testExecuteWithErrorHandling_ThrowingException() throws Exception {
        ExceptionUtils.executeWithErrorHandling(
                () -> { throw new RuntimeException("Test error"); },
                "testOperation"
        );
    }

    @Test
    public void testExecuteWithErrorHandling_ThrowingSuccess() throws Exception {
        String result = ExceptionUtils.executeWithErrorHandling(
                () -> "success",
                "testOperation"
        );

        assertEquals("success", result);
    }

    @Test
    public void testExecuteVoidWithErrorHandling_Success() {
        boolean result = ExceptionUtils.executeVoidWithErrorHandling(
                () -> {},
                "testOperation"
        );

        assertTrue(result);
    }

    @Test
    public void testExecuteVoidWithErrorHandling_Exception() {
        boolean result = ExceptionUtils.executeVoidWithErrorHandling(
                () -> { throw new RuntimeException("Test error"); },
                "testOperation"
        );

        assertFalse(result);
    }

    @Test
    public void testExecuteVoidWithErrorHandling_WithActualOperation() {
        boolean result = ExceptionUtils.executeVoidWithErrorHandling(
                () -> {
                    int sum = 1 + 1;
                    assertEquals(2, sum);
                },
                "testOperation"
        );

        assertTrue(result);
    }

    @Test
    public void testWrapException() {
        IOException originalException = new IOException("IO error");
        UgandaEMRSyncException wrappedException = ExceptionUtils.wrapException(
                originalException,
                UgandaEMRSyncException.ErrorCode.SYNC_FAILED,
                "Failed to sync data"
        );

        assertEquals(UgandaEMRSyncException.ErrorCode.SYNC_FAILED, wrappedException.getErrorCode());
        assertTrue(wrappedException.getMessage().contains("Failed to sync data"));
        assertTrue(wrappedException.getMessage().contains("IO error"));
        assertSame(originalException, wrappedException.getCause());
    }

    @Test
    public void testLogAndRethrow_WithUgandaEMRSyncException() throws UgandaEMRSyncException {
        Log mockLogger = createSilentLogger();
        UgandaEMRSyncException originalException = new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                "Validation failed"
        );

        try {
            ExceptionUtils.logAndRethrow(mockLogger, originalException, "Validation step");
            fail("Should have thrown UgandaEMRSyncException");
        } catch (UgandaEMRSyncException e) {
            assertSame(originalException, e);
        }
    }

    @Test
    public void testLogAndRethrow_WithGenericException() throws UgandaEMRSyncException {
        Log mockLogger = createSilentLogger();
        IOException ioException = new IOException("IO error");

        try {
            ExceptionUtils.logAndRethrow(mockLogger, ioException, "File operation");
            fail("Should have thrown UgandaEMRSyncException");
        } catch (UgandaEMRSyncException e) {
            assertEquals(UgandaEMRSyncException.ErrorCode.SYNC_FAILED, e.getErrorCode());
            assertTrue(e.getMessage().contains("File operation failed"));
            assertSame(ioException, e.getCause());
        }
    }

    @Test
    public void testCreateErrorResponse() {
        Map<String, Object> response = ExceptionUtils.createErrorResponse(
                "ERR_001",
                "Operation failed",
                "Field validation failed"
        );

        assertFalse((Boolean) response.get("success"));
        assertEquals("ERR_001", response.get("errorCode"));
        assertEquals("Operation failed", response.get("message"));
        assertEquals("Field validation failed", response.get("details"));
        assertNotNull(response.get("timestamp"));
    }

    @Test
    public void testCreateErrorResponse_WithComplexDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("field", "patientName");
        details.put("value", "John123");
        Map<String, Object> response = ExceptionUtils.createErrorResponse(
                "VAL_001",
                "Validation error",
                details
        );

        assertEquals(details, response.get("details"));
    }

    @Test
    public void testCreateSuccessResponse() {
        Object data = "Success data";
        Map<String, Object> response = ExceptionUtils.createSuccessResponse(data);

        assertTrue((Boolean) response.get("success"));
        assertEquals("Success data", response.get("data"));
        assertNotNull(response.get("timestamp"));
    }

    @Test
    public void testCreateSuccessResponse_WithComplexData() {
        Map<String, Object> data = new HashMap<>();
        data.put("patientId", 123);
        data.put("name", "John Doe");
        Map<String, Object> response = ExceptionUtils.createSuccessResponse(data);

        assertEquals(data, response.get("data"));
    }

    @Test
    public void testIsRetryable_WithUgandaEMRSyncException() {
        UgandaEMRSyncException retryableException = new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.CONNECTION_TIMEOUT,
                "Connection timeout"
        );
        assertTrue(ExceptionUtils.isRetryable(retryableException));

        UgandaEMRSyncException nonRetryableException = new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                "Validation error"
        );
        assertFalse(ExceptionUtils.isRetryable(nonRetryableException));
    }

    @Test
    public void testIsRetryable_WithGenericException() {
        assertTrue(ExceptionUtils.isRetryable(new java.net.SocketTimeoutException()));
        assertTrue(ExceptionUtils.isRetryable(new java.net.ConnectException()));
        assertFalse(ExceptionUtils.isRetryable(new IllegalArgumentException()));
    }

    @Test
    public void testIsRetryable_WithTimeoutException() {
        assertTrue(ExceptionUtils.isRetryable(new java.net.SocketTimeoutException()));
    }

    @Test
    public void testIsRetryable_WithConnectionException() {
        assertTrue(ExceptionUtils.isRetryable(new java.net.ConnectException()));
    }

    @Test
    public void testIsRetryable_WithSocketException() {
        assertTrue(ExceptionUtils.isRetryable(new java.net.SocketException()));
    }

    @Test
    public void testSafeSleep_NormalExecution() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ExceptionUtils.safeSleep(100);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration >= 100);
    }

    @Test
    public void testSafeSleep_ZeroDelay() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ExceptionUtils.safeSleep(0);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(duration < 100);
    }

    @Test
    public void testSafeSleep_Interrupted() {
        Thread mainThread = Thread.currentThread();
        new Thread(() -> {
            try {
                Thread.sleep(50);
                mainThread.interrupt();
            } catch (InterruptedException e) {
                // Ignore
            }
        }).start();

        // Should not throw exception, just return gracefully
        ExceptionUtils.safeSleep(5000);

        // Thread should be interrupted
        assertTrue(Thread.interrupted() || Thread.currentThread().isInterrupted());
    }

    @Test
    public void testExecuteWithErrorHandling_WithComplexObject() {
        class TestObject {
            private final String value;

            TestObject(String value) {
                this.value = value;
            }

            String getValue() {
                return value;
            }
        }

        TestObject result = ExceptionUtils.executeWithErrorHandling(
                () -> new TestObject("test"),
                "createObject",
                null
        );

        assertNotNull(result);
        assertEquals("test", result.getValue());
    }

    @Test
    public void testExecuteVoidWithErrorHandling_MultipleOperations() {
        final int[] counter = {0};

        boolean result = ExceptionUtils.executeVoidWithErrorHandling(
                () -> {
                    counter[0]++;
                    counter[0] *= 2;
                },
                "testOperation"
        );

        assertTrue(result);
        assertEquals(2, counter[0]);
    }

    /**
     * Create a silent logger that doesn't output to console during tests
     * This prevents ERROR messages from cluttering test output
     */
    private Log createSilentLogger() {
        org.apache.commons.logging.impl.SimpleLog logger = new org.apache.commons.logging.impl.SimpleLog("test");
        logger.setLevel(org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_OFF);
        return logger;
    }
}
