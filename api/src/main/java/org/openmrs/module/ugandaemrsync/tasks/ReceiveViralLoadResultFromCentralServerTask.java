package org.openmrs.module.ugandaemrsync.tasks;

import org.json.JSONObject;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
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
		String apiToken = "WiGcL3KOQOolhQ2IVHwbqLagIKuJpzbktLOY6ezalRZSozg6gJMZfIwjZ4nt";
		
		for (SyncTask syncTask : ugandaEMRSyncService.getIncompleteActionSyncTask()) {
			JSONObject dataOutput = new JSONObject();
			dataOutput.put("sample_id", String.valueOf(syncTask.getSyncTask()));
			dataOutput.put("facility_code", ugandaEMRSyncService.getHealthCenterCode());
			dataOutput.put("api_token", apiToken);
			
			Map results = new HashMap();
			
			try {
				results = ugandaEMRHttpURLConnection.sendPostBy("vlresult/", dataOutput.toString(), false);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			if (results.size() > 0 && UgandaEMRSyncUtil.getSuccessCodeList().contains(results.get("responseCode"))) {
				
				String dateFormat = ugandaEMRSyncService.getDateFormat(results.get("specimen_collection_date").toString());
				
				//Get Encounter Type for
				Collection<EncounterType> encounterTypes = ugandaEMRSyncService.getEcounterTypes(HIV_ENCOUNTER_PAGE_UUID);
				
				Encounter viralLoadRequestEncounter = Context.getEncounterService().getEncounter(syncTask.getSyncTask());
				
				//Create Encounter Query Criteria
				EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteria(
				        viralLoadRequestEncounter.getPatient(), null, ugandaEMRSyncService.convertStringToDate(
				            results.get("specimen_collection_date").toString(), "00:00:00", dateFormat),
				        ugandaEMRSyncService.convertStringToDate(results.get("specimen_collection_date").toString(),
				            "23:59:59", dateFormat), null, null, encounterTypes, null, null, null, false);
				
				//Get Encounter to save ViralLoad Results to
				List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
				
				//Save Viral Load Results
				ugandaEMRSyncService.addVLToEncounter(results.get("result_numeric").toString(),
				    results.get("result_alphanumeric").toString(), results.get("specimen_collection_date").toString(),
				    encounters.get(0));
				
				//Update Sync task
				syncTask.setActionCompleted(true);
				ugandaEMRSyncService.saveSyncTask(syncTask);
			}
		}
	}
}
