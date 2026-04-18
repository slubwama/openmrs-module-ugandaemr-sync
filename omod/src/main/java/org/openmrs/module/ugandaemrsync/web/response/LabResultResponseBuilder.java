package org.openmrs.module.ugandaemrsync.web.response;

import org.openmrs.module.ugandaemrsync.dto.exception.ConnectionException;
import org.openmrs.module.ugandaemrsync.dto.exception.LabResultProcessingException;
import org.openmrs.module.ugandaemrsync.dto.exception.ValidationException;
import org.openmrs.module.ugandaemrsync.web.resource.DTO.LabResultErrorResponse;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds consistent responses for lab result processing operations.
 * Creates error response DTOs that can be thrown as ResponseException by the resource layer.
 */
@Component
public class LabResultResponseBuilder {

    /**
     * Builds a success response.
     *
     * @param orderCount Number of orders processed
     * @param messages List of response messages
     * @return SimpleObject representing the success response
     */
    public SimpleObject buildSuccessResponse(int orderCount, Map<String, Object> messages) {
        SimpleObject response = new SimpleObject();
        response.put("status", "success");
        response.put("timestamp", new Date());
        response.put("ordersProcessed", orderCount);
        response.put("responses", messages);
        return response;
    }

    /**
     * Builds a validation error response DTO.
     *
     * @param exception The validation exception
     * @return LabResultErrorResponse containing validation error details
     */
    public LabResultErrorResponse buildValidationErrorResponse(ValidationException exception) {
        LabResultErrorResponse errorResponse = new LabResultErrorResponse("validation_error");
        errorResponse.setMessage("Validation failed: " + exception.getMessage());
        errorResponse.setErrors(buildValidationErrors(exception));
        return errorResponse;
    }

    /**
     * Builds a connection error response DTO.
     *
     * @param exception The connection exception
     * @return LabResultErrorResponse containing connection error details
     */
    public LabResultErrorResponse buildConnectionErrorResponse(ConnectionException exception) {
        LabResultErrorResponse errorResponse = new LabResultErrorResponse("connection_error");
        errorResponse.setMessage("Connection failed: " + exception.getMessage());
        errorResponse.addDetail("endpoint", exception.getEndpoint());
        errorResponse.addDetail("retryable", exception.isRetryable());
        return errorResponse;
    }

    /**
     * Builds a generic processing error response DTO.
     *
     * @param exception The processing exception
     * @return LabResultErrorResponse containing processing error details
     */
    public LabResultErrorResponse buildProcessingErrorResponse(LabResultProcessingException exception) {
        LabResultErrorResponse errorResponse = new LabResultErrorResponse(
                exception.getErrorType().name().toLowerCase());
        errorResponse.setMessage("Processing failed: " + exception.getMessage());
        errorResponse.addDetail("orderUuid", exception.getOrderUuid());
        errorResponse.addDetail("retryable", exception.isRetryable());
        return errorResponse;
    }

    /**
     * Builds a generic error response DTO for unexpected errors.
     *
     * @param exception The unexpected exception
     * @return LabResultErrorResponse containing generic error details
     */
    public LabResultErrorResponse buildGenericErrorResponse(Exception exception) {
        LabResultErrorResponse errorResponse = new LabResultErrorResponse("internal_error");
        errorResponse.setMessage("An unexpected error occurred");
        return errorResponse;
    }

    /**
     * Determines the HTTP status code for a given exception.
     *
     * @param exception The exception to evaluate
     * @return HTTP status code
     */
    public int determineStatusCode(LabResultProcessingException exception) {
        switch (exception.getErrorType()) {
            case ORDER_NOT_FOUND:
                return HttpStatus.NOT_FOUND.value();
            case INVALID_DATA:
            case VALIDATION_FAILED:
                return HttpStatus.BAD_REQUEST.value();
            case CONNECTION_FAILED:
                return HttpStatus.SERVICE_UNAVAILABLE.value();
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }

    private Map<String, Object> buildValidationErrors(ValidationException exception) {
        Map<String, Object> errors = new HashMap<>();
        for (ValidationException.ValidationError error : exception.getErrors()) {
            Map<String, Object> errorDetail = new HashMap<>();
            errorDetail.put("message", error.getMessage());
            errorDetail.put("rejectedValue", error.getRejectedValue());
            errors.put(error.getField(), errorDetail);
        }
        return errors;
    }
}
