package org.openmrs.module.ugandaemrsync.exception;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Test suite for UgandaEMRSyncException
 */
public class UgandaEMRSyncExceptionTest {

    @Test
    public void testExceptionWithErrorCodeAndMessage() {
        UgandaEMRSyncException exception = new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                "Test validation message"
        );

        assertEquals(UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR, exception.getErrorCode());
        assertEquals("Test validation message", exception.getMessage());
        assertFalse(exception.isRetryable());
        assertNull(exception.getContext());
        assertNull(exception.getCause());
    }

    @Test
    public void testExceptionWithErrorCodeMessageAndCause() {
        Throwable cause = new IOException("Underlying IO error");
        UgandaEMRSyncException exception = new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED,
                "Connection failed message",
                cause
        );

        assertEquals(UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED, exception.getErrorCode());
        assertEquals("Connection failed message", exception.getMessage());
        assertTrue(exception.isRetryable());
        assertSame(cause, exception.getCause());
        assertNull(exception.getContext());
    }

    @Test
    public void testExceptionWithErrorCodeMessageCauseAndContext() {
        Throwable cause = new RuntimeException("Connection error");
        Object context = "patientId: 12345";

        UgandaEMRSyncException exception = new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED,
                "Connection operation failed",
                cause,
                context
        );

        assertEquals(UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED, exception.getErrorCode());
        assertEquals("Connection operation failed", exception.getMessage());
        assertTrue(exception.isRetryable());
        assertSame(cause, exception.getCause());
        assertSame(context, exception.getContext());
    }

    @Test
    public void testRetryableConnectionErrors() {
        assertTrue(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED,
                "Connection failed"
        ).isRetryable());

        assertTrue(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.CONNECTION_TIMEOUT,
                "Connection timeout"
        ).isRetryable());

        assertTrue(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.READ_TIMEOUT,
                "Read timeout"
        ).isRetryable());
    }

    @Test
    public void testRetryableHttpServerErrors() {
        assertTrue(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.HTTP_SERVER_ERROR,
                "Server error"
        ).isRetryable());
    }

    @Test
    public void testRetryableExternalServiceErrors() {
        assertTrue(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                "Service unavailable"
        ).isRetryable());

        assertTrue(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.EXTERNAL_SERVICE_TIMEOUT,
                "Service timeout"
        ).isRetryable());
    }

    @Test
    public void testNonRetryableClientErrors() {
        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.HTTP_CLIENT_ERROR,
                "Client error"
        ).isRetryable());

        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.HTTP_UNAUTHORIZED,
                "Unauthorized"
        ).isRetryable());

        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.HTTP_FORBIDDEN,
                "Forbidden"
        ).isRetryable());
    }

    @Test
    public void testNonRetryableValidationErrors() {
        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                "Validation failed"
        ).isRetryable());

        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.DATA_INTEGRITY_ERROR,
                "Data integrity error"
        ).isRetryable());

        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.PARSING_ERROR,
                "Parsing error"
        ).isRetryable());
    }

    @Test
    public void testNonRetryableConfigurationErrors() {
        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.CONFIGURATION_ERROR,
                "Configuration error"
        ).isRetryable());

        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.MISSING_PROPERTY,
                "Missing property"
        ).isRetryable());
    }

    @Test
    public void testNonRetryableSyncErrors() {
        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.SYNC_FAILED,
                "Sync failed"
        ).isRetryable());

        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.BATCH_PROCESSING_ERROR,
                "Batch processing error"
        ).isRetryable());
    }

    @Test
    public void testErrorCodeEnumValues() {
        assertEquals("CON_001", UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED.getCode());
        assertEquals("Failed to establish connection", UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED.getDescription());

        assertEquals("VAL_001", UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR.getCode());
        assertEquals("Data validation failed", UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR.getDescription());

        assertEquals("HTTP_401", UgandaEMRSyncException.ErrorCode.HTTP_UNAUTHORIZED.getCode());
        assertEquals("Authentication failed", UgandaEMRSyncException.ErrorCode.HTTP_UNAUTHORIZED.getDescription());
    }

    @Test
    public void testToStringFormatting() {
        UgandaEMRSyncException exception = new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                "Patient validation failed"
        );

        String toString = exception.toString();
        assertTrue(toString.contains("[VAL_001]"));
        assertTrue(toString.contains("Data validation failed"));
        assertTrue(toString.contains("Patient validation failed"));
    }

    @Test
    public void testAllErrorCodesHaveValidFormat() {
        for (UgandaEMRSyncException.ErrorCode errorCode : UgandaEMRSyncException.ErrorCode.values()) {
            assertNotNull(errorCode.getCode());
            assertFalse(errorCode.getCode().isEmpty());
            assertNotNull(errorCode.getDescription());
            assertFalse(errorCode.getDescription().isEmpty());
        }
    }

    @Test
    public void testQueryTimeoutIsRetryable() {
        assertTrue(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.QUERY_TIMEOUT,
                "Query timeout"
        ).isRetryable());
    }

    @Test
    public void testDatabaseErrorIsNotRetryable() {
        assertFalse(new UgandaEMRSyncException(
                UgandaEMRSyncException.ErrorCode.DATABASE_ERROR,
                "Database error"
        ).isRetryable());
    }
}
