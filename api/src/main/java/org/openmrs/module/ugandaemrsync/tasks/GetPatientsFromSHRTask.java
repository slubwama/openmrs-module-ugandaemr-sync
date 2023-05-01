package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.server.SyncFHIRRecord;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.ui.framework.SimpleObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static org.apache.http.HttpHeaders.USER_AGENT;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.PATIENT_ID_TYPE_NIN_NAME;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.PATIENT_ID_TYPE_POIN_NAME;

public class GetPatientsFromSHRTask extends AbstractTask {
    protected final Log log = LogFactory.getLog(GetPatientsFromSHRTask.class);

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
                transferIn(results);
            } catch (Exception e) {
                log.error("Failed to fetch results", e);
            }

            log.info("Generating Resources and cases for Profile " + syncFhirProfile.getName());
            syncFHIRRecord.generateCaseBasedFHIRResourceBundles(syncFhirProfile);
            syncFHIRRecord.sendFhirResourcesTo(syncFhirProfile);
        }

    }

    public SimpleObject transferIn(String patientDataObject) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
            JSONObject results = new JSONObject(patientDataObject);
            for (Object jsonObject : results.getJSONArray("entry")) {
                JSONObject patientData = new JSONObject(jsonObject.toString()).getJSONObject("resource");
                Patient patient = null;
                String healthCenterFrom = patientData.getJSONObject("managingOrganization").get("display").toString();
                if (!ugandaEMRSyncService.patientFromFHIRExists(patientData)) {
                    patient = ugandaEMRSyncService.createPatientsFromFHIR(patientData);
                }
                if (patient != null) {
                    log.info("Patient " + patient.getNames() + "Successfully Created");
                    return SimpleObject.create("status", objectMapper.writeValueAsString("Patient Successfully Created "));
                }
            }

        } catch (Exception e) {
            return SimpleObject.create("status",
                    objectMapper.writeValueAsString("There was a problem transferring in patient"));
        }
        return null;
    }
}
