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

public class GetPatientsFromSHRTask extends AbstractTask {
    protected final Log log = LogFactory.getLog(GetPatientsFromSHRTask.class);

    @Override
    public void execute() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();



        SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileByScheduledTaskName(this.taskDefinition.getName());

        if(syncFhirProfile.getProfileEnabled()) {

            log.info("Generating Resources and cases for Profile " + syncFhirProfile.getName());
            String results = null;
            try {


                Map resultMap = sendGET(syncFhirProfile.getUrl());
                results = (String) resultMap.get("result");
                transferIn(results);
            } catch (Exception e) {
                log.error("Failed to fetch results", e);
            }
        }

    }

    public SimpleObject transferIn(String patientDataObject) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
            JSONObject results = new JSONObject(patientDataObject);

            for (Object jsonObject: results.getJSONArray("entry")) {
                JSONObject patientData = new JSONObject(jsonObject.toString()).getJSONObject("resource");

                String healthCenterFrom = patientData.getJSONObject("managingOrganization").get("display").toString();
                Patient patient = ugandaEMRSyncService.createPatientsFromFHIR(patientData);

                if (patient != null) {
                    return SimpleObject.create("status", objectMapper.writeValueAsString("Patient Successfully Created "));
                } else {
                    return SimpleObject.create("status",
                            objectMapper.writeValueAsString("There was a problem transferring in patient"));
                }
            }

        } catch (Exception e) {
            return SimpleObject.create("status",
                    objectMapper.writeValueAsString("There was a problem transferring in patient"));
        }
        return null;
    }

    private Map sendGET(String url) throws IOException {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
        Map map = new HashMap();
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {

            map.put("result", ugandaEMRHttpURLConnection.getStringOfResults(con.getInputStream()));
        } else {
            System.out.println("GET request did not work.");
        }

        return map;
    }
}
