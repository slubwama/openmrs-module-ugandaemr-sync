package org.openmrs.module.ugandaemrsync.tasks;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.server.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.util.UgandaEMRSyncUtil;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.*;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

public class ReceiveViralLoadResultFromCentralServerTask extends AbstractTask {
	
	@Override
	public void execute() {
		UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
		
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
		for (SyncTask syncTask : ugandaEMRSyncService.getIncompleteActionSyncTask()) {
			
			Order order = getOrder(syncTask.getSyncTask());
			
			String dataOutput = generateVLFHIRResultRequestBody(VL_RECEIVE_RESULT_FHIR_JSON_STRING,
			    ugandaEMRSyncService.getHealthCenterCode(),
			    ugandaEMRSyncService.getPatientARTNO(order.getEncounter().getPatient()),
			    String.valueOf(syncTask.getSyncTask())).get("json");
			
			Map results = new HashMap();
			
			try {
				results = ugandaEMRHttpURLConnection.sendPostBy("api/sample_result", dataOutput, false);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			if (results != null && results.size() > 0
			        && UgandaEMRSyncUtil.getSuccessCodeList().contains(results.get("responseCode"))) {
				
				Map result = (Map) results.get("result");
				
				String dateFormat = ugandaEMRSyncService.getDateFormat(order.getEncounter().getEncounterDatetime()
				        .toString());
				
				//Get Encounter Type for
				Collection<EncounterType> encounterTypes = ugandaEMRSyncService.getEcounterTypes(HIV_ENCOUNTER_PAGE_UUID);
				
				//Create Encounter Query Criteria
				EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteria(order.getPatient(), null,
				        ugandaEMRSyncService.convertStringToDate(order.getEncounter().getEncounterDatetime().toString(),
				            "00:00:00", dateFormat), ugandaEMRSyncService.convertStringToDate(order.getEncounter()
				                .getEncounterDatetime().toString(), "23:59:59", dateFormat), null, null, encounterTypes,
				        null, null, null, false);
				
				//Get Encounter to save ViralLoad Results to
				List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
				
				//Save Viral Load Results
				if (encounters.size() > 0) {
					ugandaEMRSyncService.addVLToEncounter(result.get("valueString").toString(), result.get("valueInteger")
					        .toString(), order.getEncounter().getEncounterDatetime().toString(), encounters.get(0), order);
					syncTask.setActionCompleted(true);
					ugandaEMRSyncService.saveSyncTask(syncTask);
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
		List list = Context.getAdministrationService().executeSQL(String.format(VIRAL_LOAD_ORDER_QUERY, assessionNumber),
		    true);
		if (list.size() > 0) {
			for (Object o : list) {
				return orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
			}
		}
		return null;
	}
}
