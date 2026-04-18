package org.openmrs.module.ugandaemrsync.web.resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.dto.exception.ConnectionException;
import org.openmrs.module.ugandaemrsync.dto.exception.LabResultProcessingException;
import org.openmrs.module.ugandaemrsync.dto.exception.ValidationException;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.web.resource.DTO.SyncTestOrderSync;
import org.openmrs.module.ugandaemrsync.web.response.LabResultResponseBuilder;
import org.openmrs.module.ugandaemrsync.util.validation.LabResultRequestValidator;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

/**
 * REST resource for requesting lab results from CPHL (Central Public Health Laboratories).
 * Handles validation, processing, and error management for lab result requests.
 */
@Resource(name = RestConstants.VERSION_1 + "/requestlabresult",
        supportedClass = SyncTestOrderSync.class,
        supportedOpenmrsVersions = {"1.9.* - 9.*"})
@Component
public class RequestLabResultsResource extends DelegatingCrudResource<SyncTestOrderSync> {

    private static final Log log = LogFactory.getLog(RequestLabResultsResource.class);

    // Validation and response components (would be injected via Spring in production)
    private final LabResultRequestValidator validator = new LabResultRequestValidator();
    private final LabResultResponseBuilder responseBuilder = new LabResultResponseBuilder();

    @Override
    public SyncTestOrderSync newDelegate() {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    @Override
    public SyncTestOrderSync save(SyncTestOrderSync TestResult) {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    /**
     * Creates and processes a lab result request.
     * This method is transactional to ensure data consistency.
     *
     * @param propertiesToCreate Request properties containing order UUIDs
     * @param context Request context
     * @return SimpleObject containing processing results
     * @throws ResponseException for validation or processing errors
     */
    @Override
    @Transactional
    public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
        String correlationId = UUID.randomUUID().toString();
        log.info(String.format("%s Starting lab result request processing",correlationId));

        try {
            // Step 1: Validate input
            validateInput(propertiesToCreate, correlationId);

            // Step 2: Check connection availability
            validateConnectionAvailability(correlationId);

            // Step 3: Process orders
            ProcessingResult result = processLabResultRequest(propertiesToCreate, correlationId);

            // Step 4: Build success response
            log.info(String.format("[%s] Successfully processed %d orders", correlationId, result.getProcessedCount()));
            return buildSuccessResponse(result);

        } catch (ValidationException e) {
            log.error(String.format("[%s] Validation error: %s", correlationId, e.getMessage()));
            throw new RuntimeException("Validation failed: " + e.getMessage(), e);
        } catch (ConnectionException e) {
            log.error(String.format("[%s] Connection error: %s", correlationId, e.getMessage()));
            throw new RuntimeException("Connection failed: " + e.getMessage(), e);
        } catch (LabResultProcessingException e) {
            log.error(String.format("[%s] Processing error: %s", correlationId, e.getMessage()));
            throw new RuntimeException("Processing failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error(String.format("[%s] Unexpected error: %s", correlationId, e.getMessage()), e);
            throw new RuntimeException("Internal error: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the input request properties.
     */
    private void validateInput(SimpleObject propertiesToCreate, String correlationId) throws ValidationException {
        log.debug(String.format("[%s] Validating input request", correlationId));

        if (propertiesToCreate == null) {
            throw new ValidationException("request", "Request body cannot be null", null);
        }

        Object ordersObject = propertiesToCreate.get("orders");
        if (!(ordersObject instanceof List)) {
            throw new ValidationException("orders", "Orders must be a list", ordersObject);
        }

        @SuppressWarnings("unchecked")
        List<String> orderUuids = (List<String>) ordersObject;

        // Convert to list of strings if needed
        List<String> stringUuids = orderUuids.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());

        validator.validateRequest(stringUuids);

        log.debug(String.format("[%s] Input validation passed for %d orders", correlationId, stringUuids.size()));
    }

    /**
     * Validates that network connection is available.
     */
    private void validateConnectionAvailability(String correlationId) throws ConnectionException {
        log.debug(String.format("[%s] Checking connection availability", correlationId));

        UgandaEMRHttpURLConnection connection = new UgandaEMRHttpURLConnection();
        if (!connection.isConnectionAvailable()) {
            String message = "No internet connection to get lab results from CPHL";
            log.warn(String.format("[%s] Connection unavailable", correlationId));
            throw new ConnectionException(message, "CPHL", -1, -1);
        }

        log.debug(String.format("[%s] Connection available", correlationId));
    }

    /**
     * Processes the lab result request for all orders.
     */
    private ProcessingResult processLabResultRequest(SimpleObject propertiesToCreate, String correlationId) {
        log.debug(String.format("[%s] Processing lab result request", correlationId));

        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TASK_TYPE_UUID);

        List<String> orderUuids = extractOrderUuids(propertiesToCreate);
        ProcessingResult result = new ProcessingResult();

        for (String orderUuid : orderUuids) {
            try {
                processSingleOrder(orderUuid, syncTaskType, ugandaEMRSyncService, result, correlationId);
            } catch (Exception e) {
                log.error(String.format("[%s] Failed to process order %s: %s", correlationId, orderUuid, e.getMessage()));
                result.addError(orderUuid, e.getMessage());
            }
        }

        log.info(String.format("[%s] Processing complete: %d successful, %d failed",
                correlationId, result.getSuccessCount(), result.getErrorCount()));

        return result;
    }

    /**
     * Extracts order UUIDs from the request properties.
     */
    private List<String> extractOrderUuids(SimpleObject propertiesToCreate) {
        Object ordersObject = propertiesToCreate.get("orders");
        if (ordersObject instanceof List) {
            return ((List<?>) ordersObject).stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Processes a single order for lab results.
     */
    private void processSingleOrder(String orderUuid, SyncTaskType syncTaskType,
                                   UgandaEMRSyncService ugandaEMRSyncService,
                                   ProcessingResult result, String correlationId) {
        log.debug(String.format("[%s] Processing order: %s", correlationId, orderUuid));

        Order order = Context.getOrderService().getOrderByUuid(orderUuid);
        if (order == null) {
            String message = String.format("Order not found: %s", orderUuid);
            log.warn(String.format("[%s] %s", correlationId, message));
            result.addError(orderUuid, message);
            return;
        }

        List<SyncTask> syncTasks = ugandaEMRSyncService.getSyncTasksBySyncTaskId(order.getAccessionNumber())
                .stream()
                .filter(task -> task.getSyncTaskType().equals(syncTaskType))
                .collect(Collectors.toList());

        if (syncTasks.isEmpty()) {
            String message = String.format("No sync task found for order: %s", order.getAccessionNumber());
            log.warn(String.format("[%s] %s", correlationId, message));
            result.addError(orderUuid, message);
            return;
        }

        SyncTask syncTask = syncTasks.get(0);
        if (!isValidSyncTask(syncTask)) {
            String message = String.format("Order %s does not qualify to receive results", order.getAccessionNumber());
            log.warn(String.format("[%s] %s", correlationId, message));
            result.addError(orderUuid, message);
            return;
        }

        Map<String, Object> response = ugandaEMRSyncService.requestLabResult(order, syncTask);
        result.addSuccess(order, response);
    }

    /**
     * Validates if a sync task qualifies for result processing.
     */
    private boolean isValidSyncTask(SyncTask syncTask) {
        return isSuccessStatusCode(syncTask.getStatusCode()) &&
               syncTask.getRequireAction() &&
               !syncTask.getActionCompleted();
    }

    /**
     * Checks if the status code indicates success.
     */
    private boolean isSuccessStatusCode(Integer statusCode) {
        return statusCode != null &&
               (statusCode == 200 || statusCode == 201 || statusCode == 202 || statusCode == 208);
    }

    /**
     * Builds a success response from the processing result.
     */
    private SimpleObject buildSuccessResponse(ProcessingResult result) {
        Map<String, Object> allResponses = new HashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry : result.getSuccessResults().entrySet()) {
            allResponses.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, String> entry : result.getErrors().entrySet()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", entry.getValue());
            allResponses.put(entry.getKey(), errorResponse);
        }

        return responseBuilder.buildSuccessResponse(result.getTotalProcessed(), allResponses);
    }

    @Override
    public Object update(String uuid, SimpleObject propertiesToUpdate, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    @Override
    public SyncTestOrderSync getByUniqueId(String uniqueId) {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    @Override
    public NeedsPaging<SyncTestOrderSync> doGetAll(RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    @Override
    public List<Representation> getAvailableRepresentations() {
        return Arrays.asList(Representation.DEFAULT, Representation.FULL);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if (rep instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("orderList");
            description.addProperty("responseList");
            description.addSelfLink();
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("orderList", Representation.REF);
            description.addProperty("responseList", Representation.REF);
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("orderList", Representation.REF);
            description.addProperty("responseList", Representation.REF);
            description.addSelfLink();
            return description;
        }
        return null;
    }

    @Override
    protected void delete(SyncTestOrderSync TestResult, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    @Override
    public void purge(SyncTestOrderSync TestResult, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("orders");
        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    /**
     * Internal class to track processing results.
     */
    private static class ProcessingResult {
        private final Map<String, Map<String, Object>> successResults = new HashMap<>();
        private final Map<String, String> errors = new HashMap<>();
        private final List<Order> orders = new ArrayList<>();

        public void addSuccess(Order order, Map<String, Object> response) {
            successResults.put(order.getUuid(), response);
            orders.add(order);
        }

        public void addError(String orderUuid, String errorMessage) {
            errors.put(orderUuid, errorMessage);
        }

        public Map<String, Map<String, Object>> getSuccessResults() {
            return successResults;
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public int getSuccessCount() {
            return successResults.size();
        }

        public int getErrorCount() {
            return errors.size();
        }

        public int getTotalProcessed() {
            return successResults.size() + errors.size();
        }

        public int getProcessedCount() {
            return successResults.size();
        }
    }
}
