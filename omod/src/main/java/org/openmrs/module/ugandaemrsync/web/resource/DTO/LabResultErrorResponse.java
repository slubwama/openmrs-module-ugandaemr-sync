package org.openmrs.module.ugandaemrsync.web.resource.DTO;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard error response structure for lab result operations.
 * Provides consistent error information across all endpoints.
 */
public class LabResultErrorResponse {

    private String status = "error";
    private String errorType;
    private Date timestamp;
    private Map<String, Object> details;

    public LabResultErrorResponse(String errorType) {
        this.errorType = errorType;
        this.timestamp = new Date();
        this.details = new HashMap<>();
    }

    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }

    public void setMessage(String message) {
        this.details.put("message", message);
    }

    public void setErrors(Map<String, Object> errors) {
        this.details.put("errors", errors);
    }

    // Getters
    public String getStatus() {
        return status;
    }

    public String getErrorType() {
        return errorType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
