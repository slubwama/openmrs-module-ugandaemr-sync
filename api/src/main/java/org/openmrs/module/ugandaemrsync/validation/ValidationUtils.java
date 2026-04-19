package org.openmrs.module.ugandaemrsync.validation;

import org.apache.commons.lang.StringUtils;
import org.openmrs.module.ugandaemrsync.exception.UgandaEMRSyncException;
import org.openmrs.module.ugandaemrsync.logging.StructuredLogger;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Input validation utilities to prevent data corruption and security issues
 * Provides comprehensive validation at system boundaries
 */
public class ValidationUtils {

    private static final StructuredLogger log = StructuredLogger.getLogger(ValidationUtils.class);

    // Common regex patterns
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://[A-Za-z0-9.-]+(/.*)?$"
    );

    // SQL injection detection patterns
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
            Pattern.compile("(?i).*('\\s+|;|\\bor\\b|\\band\\b|\\bunion\\b|\\bselect\\b|\\binsert\\b|\\bupdate\\b|\\bdelete\\b|\\bdrop\\b|\\bexec\\b).*"),
            Pattern.compile("(?i).*('|--|/\\*|\\*/|xp_|sp_).*")
    };

    /**
     * Validate required string field
     * @throws UgandaEMRSyncException if validation fails
     */
    public static void requireNotEmpty(String fieldName, String value) throws UgandaEMRSyncException {
        if (StringUtils.isEmpty(value)) {
            log.logValidationError(fieldName, value, "Required field cannot be empty");
            throw new UgandaEMRSyncException(
                    UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                    "Required field '" + fieldName + "' cannot be empty"
            );
        }
    }

    /**
     * Validate string length
     * @throws UgandaEMRSyncException if validation fails
     */
    public static void requireLength(String fieldName, String value, int minLength, int maxLength)
            throws UgandaEMRSyncException {
        if (value != null && (value.length() < minLength || value.length() > maxLength)) {
            log.logValidationError(fieldName, value,
                    String.format("Length must be between %d and %d", minLength, maxLength));
            throw new UgandaEMRSyncException(
                    UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                    String.format("Field '%s' length must be between %d and %d characters", fieldName, minLength, maxLength)
            );
        }
    }

    /**
     * Validate UUID format
     * @throws UgandaEMRSyncException if validation fails
     */
    public static void requireValidUUID(String fieldName, String value) throws UgandaEMRSyncException {
        if (value != null && !UUID_PATTERN.matcher(value).matches()) {
            log.logValidationError(fieldName, value, "Invalid UUID format");
            throw new UgandaEMRSyncException(
                    UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                    "Field '" + fieldName + "' must be a valid UUID"
            );
        }
    }

    /**
     * Validate numeric range
     * @throws UgandaEMRSyncException if validation fails
     */
    public static void requireInRange(String fieldName, Number value, Number min, Number max)
            throws UgandaEMRSyncException {
        if (value != null) {
            double doubleValue = value.doubleValue();
            double doubleMin = min.doubleValue();
            double doubleMax = max.doubleValue();

            if (doubleValue < doubleMin || doubleValue > doubleMax) {
                log.logValidationError(fieldName, value,
                        String.format("Value must be between %s and %s", min, max));
                throw new UgandaEMRSyncException(
                        UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                        String.format("Field '%s' must be between %s and %s", fieldName, min, max)
                );
            }
        }
    }

    /**
     * Validate URL format
     * @throws UgandaEMRSyncException if validation fails
     */
    public static void requireValidURL(String fieldName, String value) throws UgandaEMRSyncException {
        if (value != null && !URL_PATTERN.matcher(value).matches()) {
            log.logValidationError(fieldName, value, "Invalid URL format");
            throw new UgandaEMRSyncException(
                    UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                    "Field '" + fieldName + "' must be a valid URL (http:// or https://)"
            );
        }
    }

    /**
     * Check for SQL injection patterns
     * @throws UgandaEMRSyncException if SQL injection detected
     */
    public static void requireNoSQLInjection(String fieldName, String value) throws UgandaEMRSyncException {
        if (value != null) {
            for (Pattern pattern : SQL_INJECTION_PATTERNS) {
                if (pattern.matcher(value).matches()) {
                    log.logValidationError(fieldName, "***REDACTED***", "Potential SQL injection detected");
                    throw new UgandaEMRSyncException(
                            UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                            "Field '" + fieldName + "' contains potentially malicious content"
                    );
                }
            }
        }
    }

    /**
     * Validate collection size
     * @throws UgandaEMRSyncException if validation fails
     */
    public static void requireCollectionSize(String fieldName, Collection<?> collection,
                                           int minSize, int maxSize) throws UgandaEMRSyncException {
        if (collection != null && (collection.size() < minSize || collection.size() > maxSize)) {
            log.logValidationError(fieldName, collection.size(),
                    String.format("Collection size must be between %d and %d", minSize, maxSize));
            throw new UgandaEMRSyncException(
                    UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                    String.format("Field '%s' collection size must be between %d and %d", fieldName, minSize, maxSize)
            );
        }
    }

    /**
     * Validate positive number
     * @throws UgandaEMRSyncException if validation fails
     */
    public static void requirePositive(String fieldName, Number value) throws UgandaEMRSyncException {
        if (value != null && value.doubleValue() <= 0) {
            log.logValidationError(fieldName, value, "Value must be positive");
            throw new UgandaEMRSyncException(
                    UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                    "Field '" + fieldName + "' must be a positive number"
            );
        }
    }

    /**
     * Validate non-negative number
     * @throws UgandaEMRSyncException if validation fails
     */
    public static void requireNonNegative(String fieldName, Number value) throws UgandaEMRSyncException {
        if (value != null && value.doubleValue() < 0) {
            log.logValidationError(fieldName, value, "Value must be non-negative");
            throw new UgandaEMRSyncException(
                    UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                    "Field '" + fieldName + "' must be non-negative"
            );
        }
    }

    /**
     * Validate map contains required keys
     * @throws UgandaEMRSyncException if validation fails
     */
    public static void requireContainsKeys(String fieldName, Map<?, ?> map, Object... requiredKeys)
            throws UgandaEMRSyncException {
        if (map != null) {
            for (Object key : requiredKeys) {
                if (!map.containsKey(key)) {
                    log.logValidationError(fieldName, map.keySet(),
                            String.format("Missing required key: %s", key));
                    throw new UgandaEMRSyncException(
                            UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                            String.format("Field '%s' must contain key '%s'", fieldName, key)
                    );
                }
            }
        }
    }

    /**
     * Generic validation with custom predicate
     * @throws UgandaEMRSyncException if validation fails
     */
    public static <T> void requireValid(String fieldName, T value, java.util.function.Predicate<T> predicate,
                                      String errorMessage) throws UgandaEMRSyncException {
        if (value != null && !predicate.test(value)) {
            log.logValidationError(fieldName, value, "Custom validation failed");
            throw new UgandaEMRSyncException(
                    UgandaEMRSyncException.ErrorCode.VALIDATION_ERROR,
                    String.format("Field '%s' validation failed: %s", fieldName, errorMessage)
            );
        }
    }

    /**
     * Safe validation that returns boolean instead of throwing exception
     */
    public static boolean isValid(String value, java.util.function.Predicate<String> predicate) {
        return predicate.test(value);
    }

    /**
     * Validate and sanitize input string
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        // Trim whitespace
        return input.trim();
    }
}