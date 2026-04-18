package org.openmrs.module.ugandaemrsync.dto.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when input validation fails.
 * Collects multiple validation errors for comprehensive error reporting.
 */
public class ValidationException extends LabResultProcessingException {

    private final List<ValidationError> errors;

    public static class ValidationError {
        private final String field;
        private final String message;
        private final Object rejectedValue;

        public ValidationError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }
    }

    public ValidationException(List<ValidationError> errors) {
        super("Validation failed with " + errors.size() + " error(s)",
              ErrorType.VALIDATION_FAILED, null);
        this.errors = new ArrayList<>(errors);
    }

    public ValidationException(String field, String message, Object rejectedValue) {
        super("Validation failed for field: " + field,
              ErrorType.VALIDATION_FAILED, null);
        this.errors = new ArrayList<>();
        this.errors.add(new ValidationError(field, message, rejectedValue));
    }

    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
