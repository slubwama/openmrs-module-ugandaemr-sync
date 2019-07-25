package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.server.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.util.UgandaEMRSyncUtil;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.*;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.CONNECTION_SUCCESS_201;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.CONNECTION_SUCCESS_200;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_SYNC_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PATIENT_IDENTIFIER_TYPE;

/**
 * Posts Viral load data to the central server
 */

public class SendViralLoadRequestToCentralServerTask extends AbstractTask {
	
	protected Log log = LogFactory.getLog(getClass());
	
	@Override
	public void execute() {
		UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
		List<List<Object>> result = getViralLoadRequestData();
		for (List<Object> row : result) {
			
			Encounter encounter = Context.getEncounterService().getEncounter((Integer) row.get(0));
			SyncTask syncTask = ugandaEMRSyncService.getSyncTask(encounter.getEncounterId());
			if (syncTask == null) {
				Map<String, String> dataOutput = generateRecordToSync(encounter);
				String json = dataOutput.get("json");
				try {
					Map map = ugandaEMRHttpURLConnection.sendPostBy("vlsync/", json, false);
					if ((map != null) && UgandaEMRSyncUtil.getSuccessCodeList().contains(map.get("responseCode"))) {
						SyncTask newSyncTask = new SyncTask();
						newSyncTask.setDateSent(new Date());
						newSyncTask.setCreator(Context.getUserService().getUser(1));
						newSyncTask.setSentToUrl("vlsync");
						newSyncTask.setRequireAction(true);
						newSyncTask.setActionCompleted(false);
						newSyncTask.setSyncTask(encounter.getEncounterId());
						newSyncTask.setStatusCode((Integer) map.get("responseCode"));
						newSyncTask.setStatus("SUCCESS");
						newSyncTask.setSyncTaskType(ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID));
						ugandaEMRSyncService.saveSyncTask(newSyncTask);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @return
	 */
	private List<List<Object>> getViralLoadRequestData() {
		return Context.getAdministrationService().executeSQL(SyncConstant.VIRAL_LOAD_ENCOUNTER_QUERY, false);
		
	}
	
	/**
	 * @param encounter
	 * @return
	 */
	private Map<String, String> generateRecordToSync(Encounter encounter) {
		
		JSONObject row = new JSONObject();
		Map<String, String> vals = new HashMap<String, String>();
		
		row.put("facility_code", getHealthCenterCode());
		row.put("destination_id", "UgandaEMR");
		row.put("sample_id", encounter.getEncounterId().toString());
		row.put("phlebotomist_contact", "None");
		row.put("patient_id", getPatientARTNO(encounter.getPatient(), PATIENT_IDENTIFIER_TYPE));
		row.put("collection_date", encounter.getEncounterDatetime());
		row.put("test_type", encounter.getEncounterType().getName());
		
		for (Obs obs : encounter.getAllObs()) {
			switch (obs.getConcept().getConceptId()) {
				case 165153:
					row.put("sample_type", obs.getValueCoded().getName().getName());
					break;
				case 159635:
					row.put("requester_contact", obs.getValueText());
					break;
			}
		}
		vals.put("json", row.toString());
		return vals;
	}
	
	/**
	 * @param patient
	 * @param patientIdentifierTypeUUID
	 * @return
	 */
	private String getPatientARTNO(Patient patient, String patientIdentifierTypeUUID) {
		String query = "select patient_identifier.identifier from patient_identifier inner join patient_identifier_type on(patient_identifier.identifier_type=patient_identifier_type.patient_identifier_type_id) where patient_identifier_type.uuid in ('"
		        + patientIdentifierTypeUUID + "') AND patient_id=" + patient.getPatientId() + "";
		return Context.getAdministrationService().executeSQL(query, true).get(0).toString();
	}
	
	/**
	 * @return
	 */
	public String getHealthCenterCode() {
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		return syncGlobalProperties.getGlobalProperty("ugandaemr.dhis2.organizationuuid");
	}
	
	/**
	 * Gets the Address from the global properties
	 * 
	 * @return
	 */
	private String getIPAddress() {
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		
		String serverIP = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_IP);
		String serverProtocol = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_PROTOCOL);
		
		return serverProtocol + serverIP;
	}
	
}
