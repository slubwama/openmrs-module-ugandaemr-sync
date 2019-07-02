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
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Posts Viral load data to the central server
 */

public class SendViralLoadRequestTaskToCentralServer extends AbstractTask {
	
	protected Log log = LogFactory.getLog(getClass());
	
	@Override
	public void execute() {
		UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
		List<List<Object>> result = getViralLoadRequestData();
		
		for (List<Object> row : result) {
			
			Encounter encounter = Context.getEncounterService().getEncounter((Integer) result.get(0).get(0));
			
			Map<String, String> dataOutput = generateRecordToSync(encounter);
			
			String json = dataOutput.get("json");
			try {
				Map map = ugandaEMRHttpURLConnection.sendPostBy("vlsync/", json, false);
				if (map != null) {
					SyncTask syncTask = new SyncTask();
					syncTask.setDateSent(new Date());
					syncTask.setSentToUrl("vlsync");
					syncTask.setStatusCode((Integer) map.get("response"));
					syncTask.setStatus("SUCCESS");
					syncTask.setSyncTaskType(ugandaEMRSyncService
					        .getSyncTaskTypeByUUID("3551ca84-06c0-432b-9064-fcfeefd6f4ec"));
					ugandaEMRSyncService.saveSyncTask(syncTask);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
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
		
		row.put("facility_Code", getHealthCenterCode());
		row.put("destination_id", "UgandaEMR");
		row.put("sample_id", encounter.getEncounterId().toString());
		row.put("phlebotomist_contact", "None");
		row.put("patient_id", getPatientARTNO(encounter.getPatient(), "e1731641-30ab-102d-86b0-7a5022ba4115"));
		row.put("collection_date", encounter.getEncounterDatetime());
		for (Obs obs : encounter.getAllObs()) {
			
			switch (obs.getConcept().getConceptId()) {
				case 165153:
					row.put("sample_type", obs.getValueCoded().getName().getName());
					break;
				case 165148:
					row.put("test_type", obs.getValueCoded().getName().getName());
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
	private String getHealthCenterCode() {
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
