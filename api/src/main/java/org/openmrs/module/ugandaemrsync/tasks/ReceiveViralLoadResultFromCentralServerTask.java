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
            }

            if (results != null && results.size() > 0 && results.get("status") != null && !results.get("status").equals("pending")) {
                Map reasonReference = (Map) results.get("reasonReference");
                ArrayList<Map> result = (ArrayList<Map>) reasonReference.get("result");
                //Save Viral Load Results
                if (order.getEncounter() != null) {
                    ugandaEMRSyncService.addVLToEncounter(result.get(0).get("valueString").toString(), result.get(0).get("valueInteger").toString(), order.getEncounter().getEncounterDatetime().toString(), order.getEncounter(), order);
                    syncTask.setActionCompleted(true);
                    ugandaEMRSyncService.saveSyncTask(syncTask);
                    SyncTask newSyncTask = new SyncTask();
                    newSyncTask.setDateSent(new Date());
                    newSyncTask.setCreator(Context.getUserService().getUser(1));
                    newSyncTask.setSentToUrl(syncTaskType.getUrl());
                    newSyncTask.setRequireAction(true);
                    newSyncTask.setActionCompleted(true);
                    newSyncTask.setSyncTask(order.getAccessionNumber());
                    newSyncTask.setStatusCode(200);
                    /* hack to store  results qualitative  in logs for HIE metrics */
                    newSyncTask.setStatus(result.get(0).get("valueString").toString());
                    newSyncTask.setSyncTaskType(syncTaskType);
                    ugandaEMRSyncService.saveSyncTask(newSyncTask);
                    try {
                        Context.getOrderService().discontinueOrder(order, "Completed", new Date(), order.getOrderer(), order.getEncounter());
                    } catch (Exception e) {
                        log.error("Failed to discontinue order", e);
                    }
                }
            }
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
