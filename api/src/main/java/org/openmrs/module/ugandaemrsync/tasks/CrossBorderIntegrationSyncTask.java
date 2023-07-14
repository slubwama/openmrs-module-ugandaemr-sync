package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.server.SyncFHIRRecord;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.ui.framework.SimpleObject;


import java.util.*;

import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.*;

public class CrossBorderIntegrationSyncTask extends AbstractTask {
    protected final Log log = LogFactory.getLog(CrossBorderIntegrationSyncTask.class);

    @Override
    public void execute() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

        SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileByScheduledTaskName(this.taskDefinition.getName());
        SyncFHIRRecord syncFHIRRecord = new SyncFHIRRecord();
        if (syncFhirProfile.getProfileEnabled()) {
            log.info("Transfering  patients from Facility SHR" + syncFhirProfile.getName());
            String results = null;
            try {
                Map resultMap = ugandaEMRHttpURLConnection.getByWithBasicAuth(syncFhirProfile.getUrl(), syncFhirProfile.getUrlUserName(), syncFhirProfile.getUrlPassword(), "String");
                results = (String) resultMap.get("result");
                updatePatientWithCBI(results);
            } catch (Exception e) {
                log.error("Failed to fetch results", e);
            }

            log.info("Generating Resources and cases for Profile " + syncFhirProfile.getName());
            syncFHIRRecord.generateCaseBasedFHIRResourceBundles(syncFhirProfile);
            syncFHIRRecord.sendFhirResourcesTo(syncFhirProfile);
        }

    }

    public SimpleObject updatePatientWithCBI(String patientDataObject) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
            JSONObject results = new JSONObject(patientDataObject);
            for (Object jsonObject : results.getJSONArray("entry")) {
                JSONObject patientData = new JSONObject(jsonObject.toString()).getJSONObject("resource");
                Patient patient = null;
                if (!ugandaEMRSyncService.patientFromFHIRExists(patientData)) {
                    patient = ugandaEMRSyncService.updatePatientsFromFHIR(patientData, PATIENT_ID_TYPE_CROSS_BORDER_UUID, PATIENT_ID_TYPE_CROSS_BORDER_NAME);
                }
                if (patient != null) {
                    log.info("Patient " + patient.getNames() + "Successfully Updated");
                    return SimpleObject.create("status", objectMapper.writeValueAsString("Patient Successfully Updated "));
                }
            }

        } catch (Exception e) {
            return SimpleObject.create("status",
                    objectMapper.writeValueAsString("There was a problem updating in patient cross boarder ID"));
        }
        return null;
    }
}
