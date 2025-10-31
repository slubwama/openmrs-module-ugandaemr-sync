package org.openmrs.module.ugandaemrsync.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.server.SyncFHIRRecord;
import org.openmrs.scheduler.tasks.AbstractTask;


import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.PATIENT_ID_TYPE_CROSS_BORDER_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.PATIENT_ID_TYPE_CROSS_BORDER_NAME;

public class CrossBorderIntegrationSyncTask extends AbstractTask {
    protected final Log log = LogFactory.getLog(CrossBorderIntegrationSyncTask.class);

    @Override
    public void execute() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

        SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileByScheduledTaskName(this.taskDefinition.getName());
        SyncFHIRRecord syncFHIRRecord = new SyncFHIRRecord();
        if (syncFhirProfile.getProfileEnabled()) {
            log.info("Generating Resources and cases for Profile " + syncFhirProfile.getName());
            syncFHIRRecord.generateCaseBasedFHIRResourceBundles(syncFhirProfile);
            syncFHIRRecord.sendFhirResourcesTo(syncFhirProfile);
        }

    }

    public void updatePatientWithCBI(String patientDataObject) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

            JsonNode results=objectMapper.readTree(patientDataObject);

            for (JsonNode jsonObject : (ArrayNode) results.get("entry")) {
                Patient patient = null;
                if (!ugandaEMRSyncService.patientFromFHIRExists(jsonObject.get("resource").toString())) {
                    patient = ugandaEMRSyncService.updatePatientsFromFHIR(jsonObject.get("resource").toString(), PATIENT_ID_TYPE_CROSS_BORDER_UUID, PATIENT_ID_TYPE_CROSS_BORDER_NAME);
                }
                if (patient != null) {
                    log.info("Patient " + patient.getNames() + "Successfully Updated");
                }
            }

        } catch (Exception e) {
            log.error("There was a problem updating in patient cross boarder ID",e);
        }
    }
}
