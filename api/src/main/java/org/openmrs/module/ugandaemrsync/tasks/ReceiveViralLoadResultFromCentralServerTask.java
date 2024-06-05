package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;


import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_SYNC_TASK_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_RESULT_PULL_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_RECEIVE_RESULT_FHIR_JSON_STRING;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_ORDER_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PATIENT_IDENTIFIER_TYPE;

public class ReceiveViralLoadResultFromCentralServerTask extends AbstractTask {
    protected final Log log = LogFactory.getLog(ReceiveViralLoadResultFromCentralServerTask.class);

    @Override
    public void execute() {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            return;
        }

        for (SyncTask syncTask : ugandaEMRSyncService.getIncompleteActionSyncTask(VIRAL_LOAD_SYNC_TASK_TYPE_UUID)) {
            Order order = getOrder(syncTask.getSyncTask());

            String dataOutput = generateVLFHIRResultRequestBody(VL_RECEIVE_RESULT_FHIR_JSON_STRING, ugandaEMRSyncService.getHealthCenterCode(), ugandaEMRSyncService.getPatientIdentifier(order.getEncounter().getPatient(), PATIENT_IDENTIFIER_TYPE), String.valueOf(syncTask.getSyncTask())).get("json");

            Map results = new HashMap();

            SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_RESULT_PULL_TYPE_UUID);

            try {
                results = ugandaEMRHttpURLConnection.sendPostBy(syncTaskType.getUrl(), syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), "", dataOutput, false);
            } catch (Exception e) {
                log.error("Failed to fetch results", e);
                logSyncTask(500, e.getMessage(), order, syncTaskType, false, false);
            }
            Integer responseCode = null;
            String responseMessage = null;

            // Parsing responseCode and responseMessage
            if (results.containsKey("responseCode") && results.containsKey("responseMessage")) {
                responseCode = Integer.parseInt(results.get("responseCode").toString());
                responseMessage = results.get("responseMessage").toString();
            }

            // Processing results if responseCode is valid and status is not pending
            if (responseCode != null && (responseCode == 200 || responseCode == 201) && !results.isEmpty() && results.containsKey("status") && !results.get("status").equals("pending")) {
                Map reasonReference = (Map) results.get("reasonReference");
                ArrayList<Map> result = (ArrayList<Map>) reasonReference.get("result");

                // Saving Viral Load Results
                if (order.getEncounter() != null && !result.isEmpty()) {
                    Object qualitativeResult = result.get(0).get("valueString");
                    Object quantitativeResult = result.get(0).get("valueInteger");

                    if (quantitativeResult != null && qualitativeResult != null) {
                        try {
                            ugandaEMRSyncService.addVLToEncounter(qualitativeResult.toString(), quantitativeResult.toString(), order.getEncounter().getEncounterDatetime().toString(), order.getEncounter(), order);
                            syncTask.setActionCompleted(true);
                            ugandaEMRSyncService.saveSyncTask(syncTask);
                            logSyncTask(responseCode, result.get(0).get("valueString").toString(), order, syncTaskType, false, false);
                            try {
                                Context.getOrderService().discontinueOrder(order, "Completed", new Date(), order.getOrderer(), order.getEncounter());
                            } catch (Exception e) {
                                log.error("Failed to discontinue order", e);
                            }
                        } catch (Exception e) {
                            log.error("Failed to add results to patient encounter", e);
                            logSyncTask(500, e.getMessage(), order, syncTaskType, false, false);
                        }
                    } else {
                        logSyncTask(500, "Internal server error: Results of Viral load have a null value", order, syncTaskType, false, false);
                    }
                }
            } else {
                // Logging based on responseCode or status
                if (responseCode != null && !results.containsKey("status")) {
                    logSyncTask(responseCode, responseMessage, order, syncTaskType, false, false);
                } else if (results.containsKey("status")) {
                    logSyncTask(responseCode, results.get("status").toString(), order, syncTaskType, false, false);
                }
            }

        }
    }

    private SyncTask logSyncTask(Integer statusCode, String status, Order order, SyncTaskType syncTaskType, boolean anyFurtherAction, boolean anyFurtherActionCompleted) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        List<SyncTask> syncTasks = ugandaEMRSyncService.getSyncTasksBySyncTaskId(order.getAccessionNumber()).stream().filter(syncTask -> syncTask.getSyncTaskType().equals(syncTaskType)).collect(Collectors.toList());
        if (!syncTasks.isEmpty()) {
            SyncTask existingTask = syncTasks.get(0);
            existingTask.setRequireAction(anyFurtherAction);
            existingTask.setStatus(status);
            existingTask.setStatusCode(statusCode);
            existingTask.setActionCompleted(anyFurtherActionCompleted);
            existingTask.setDateSent(new Date());
            return ugandaEMRSyncService.saveSyncTask(existingTask);
        } else {
            SyncTask newSyncTask = new SyncTask();
            newSyncTask.setCreator(Context.getUserService().getUser(1));
            newSyncTask.setSentToUrl(syncTaskType.getUrl());
            newSyncTask.setRequireAction(anyFurtherAction);
            newSyncTask.setStatus(status);
            newSyncTask.setStatusCode(statusCode);
            newSyncTask.setActionCompleted(anyFurtherActionCompleted);
            newSyncTask.setSyncTask(order.getAccessionNumber());
            newSyncTask.setSyncTaskType(syncTaskType);
            newSyncTask.setSyncTaskType(syncTaskType);
            newSyncTask.setDateSent(new Date());
            return ugandaEMRSyncService.saveSyncTask(newSyncTask);
        }
    }

    public Map<String, String> generateVLFHIRResultRequestBody(String jsonRequestString, String healthCenterCode, String patientIdentifier, String sampleIdentifier) {
        Map<String, String> jsonMap = new HashMap<>();
        String filledJsonFile = "";
        filledJsonFile = String.format(jsonRequestString, healthCenterCode, patientIdentifier, sampleIdentifier);
        jsonMap.put("json", filledJsonFile);
        return jsonMap;
    }

    public Order getOrder(String assessionNumber) {
        OrderService orderService = Context.getOrderService();
        List list = Context.getAdministrationService().executeSQL(String.format(VIRAL_LOAD_ORDER_QUERY, assessionNumber), true);
        if (list.size() > 0) {
            for (Object o : list) {
                return orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
            }
        }
        return null;
    }
}
