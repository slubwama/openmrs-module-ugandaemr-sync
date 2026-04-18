package org.openmrs.module.ugandaemrsync.util.validation;

import org.openmrs.module.ugandaemrsync.dto.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for lab result request inputs.
 * Provides comprehensive validation of order UUIDs and request parameters.
 */
@Component
public class LabResultRequestValidator {

    private static final Pattern UUID_PATTERN =
        Pattern.compile("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$");

    private static final int MAX_ORDERS_PER_REQUEST = 100;

    /**
     * Validates a lab result request.
     *
     * @param orders List of order UUIDs to validate
     * @throws ValidationException if validation fails
     */
    public void validateRequest(List<String> orders) throws ValidationException {
        List<ValidationException.ValidationError> errors = new ArrayList<>();

        // Check if orders list is null
        if (orders == null) {
            errors.add(new ValidationException.ValidationError(
                "orders", "Orders list cannot be null", null));
            throw new ValidationException(errors);
        }

        // Check if orders list is empty
        if (orders.isEmpty()) {
            errors.add(new ValidationException.ValidationError(
                "orders", "Orders list cannot be empty", orders));
        }

        // Check order count limit
        if (orders.size() > MAX_ORDERS_PER_REQUEST) {
            errors.add(new ValidationException.ValidationError(
                "orders", "Cannot process more than " + MAX_ORDERS_PER_REQUEST + " orders in a single request",
                orders.size()));
        }

        // Validate each order UUID
        for (int i = 0; i < orders.size(); i++) {
            String orderUuid = orders.get(i);
            try {
                validateOrderUuid(orderUuid);
            } catch (ValidationException e) {
                // Add field name with index for better error reporting
                ValidationException.ValidationError error = e.getErrors().get(0);
                errors.add(new ValidationException.ValidationError(
                    "orders[" + i + "]", error.getMessage(), error.getRejectedValue()));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Validates a single order UUID.
     *
     * @param orderUuid The order UUID to validate
     * @throws ValidationException if validation fails
     */
    public void validateOrderUuid(String orderUuid) throws ValidationException {
        if (orderUuid == null) {
            throw new ValidationException("orderUuid", "Order UUID cannot be null", null);
        }

        String trimmedUuid = orderUuid.trim();
        if (trimmedUuid.isEmpty()) {
            throw new ValidationException("orderUuid", "Order UUID cannot be empty", orderUuid);
        }

        if (!UUID_PATTERN.matcher(trimmedUuid).matches()) {
            throw new ValidationException("orderUuid",
                "Order UUID must be a valid UUID format (e.g., 550e8400-e29b-41d4-a716-446655440000)",
                orderUuid);
        }
    }

    /**
     * Validates that a value is not null or empty.
     *
     * @param fieldName Name of the field being validated
     * @param value Value to validate
     * @throws ValidationException if validation fails
     */
    public void requireNotEmpty(String fieldName, String value) throws ValidationException {
        if (value == null) {
            throw new ValidationException(fieldName, fieldName + " cannot be null", null);
        }
        if (value.trim().isEmpty()) {
            throw new ValidationException(fieldName, fieldName + " cannot be empty", value);
        }
    }
}
