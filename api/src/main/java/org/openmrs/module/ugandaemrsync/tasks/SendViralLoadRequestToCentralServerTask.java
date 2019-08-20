package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.api.impl.UgandaEMRSyncServiceImpl;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.util.UgandaEMRSyncUtil;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_SYNC_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_SEND_SAMPLE_FHIR_JSON_STRING;

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
				Map<String, String> dataOutput = generateVLFHIRTestRequestBody(encounter, VL_SEND_SAMPLE_FHIR_JSON_STRING);
				String json = dataOutput.get("json");
				try {
					Map map = ugandaEMRHttpURLConnection.sendPostBy("api/send_sample", json, false);
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
	 * Generate VL test Request
	 * 
	 * @param encounter
	 * @return
	 */
	public Map<String, String> generateVLFHIRTestRequestBody(Encounter encounter, String jsonFhirMap) {
        Map<String, String> jsonMap = new HashMap<>();
        UgandaEMRSyncService ugandaEMRSyncService = new UgandaEMRSyncServiceImpl();
        String filledJsonFile = "";
        if (encounter != null) {
            String obsSampleType = "";
            String obsRequesterContact = "";
            String healthCenterName = ugandaEMRSyncService.getHealthCenterName();
            String healthCenterCode = ugandaEMRSyncService.getHealthCenterCode();
            String requestType = encounter.getEncounterType().getName();
            String sourceSystem = "UgandaEMR";
            String patientARTNO = ugandaEMRSyncService.getPatientARTNO(encounter.getPatient());
            String sampleID = encounter.getEncounterId().toString();
            String sampleCollectionDate = encounter.getEncounterDatetime().toString();
            String clinicianNames = getProviderByEncounterRole(encounter, "clinician");
            String labTechNames = getProviderByEncounterRole(encounter, "Lab Technician");


            for (Obs obs : encounter.getAllObs()) {
                if (obs.getConcept().getConceptId() == 165153) {
                    obsSampleType = obs.getValueCoded().getName().getName();
                }
                if (obs.getConcept().getConceptId() == 159635) {
                    obsRequesterContact = obs.getValueText();
                }
            }

            filledJsonFile = String.format(jsonFhirMap, healthCenterCode, healthCenterName, requestType, sourceSystem, patientARTNO, sampleID, obsSampleType, sampleCollectionDate, clinicianNames, obsRequesterContact, labTechNames, "None", "CPHL");
        }
        jsonMap.put("json", filledJsonFile);
        return jsonMap;
    }
	
	private String getProviderByEncounterRole(Encounter encounter, String encounterRoleName) {
		for (EncounterProvider provider : encounter.getActiveEncounterProviders()) {
			if (provider.getEncounterRole().getName() == encounterRoleName) {
				return provider.getProvider().getName();
			}
		}
		return null;
	}
}
