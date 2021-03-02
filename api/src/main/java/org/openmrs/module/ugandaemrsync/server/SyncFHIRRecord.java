package org.openmrs.module.ugandaemrsync.server;

import ca.uhn.hl7v2.model.v25.datatype.ST;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;

import java.io.IOException;
import java.util.*;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

/**
 * Created by lubwamasamuel on 07/11/2016.
 */
public class SyncFHIRRecord {

    UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

    Log log = LogFactory.getLog(SyncFHIRRecord.class);

    public SyncFHIRRecord() {
    }

    private List getDatabaseRecordWithOutFacility(String query, String from, String to, int datesToBeReplaced, List<String> columns) {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        String lastSyncDate = syncGlobalProperties.getGlobalProperty(LAST_SYNC_DATE);

        String finalQuery;
        if (datesToBeReplaced == 1) {
            finalQuery = String.format(query, lastSyncDate, from, to);
        } else if (datesToBeReplaced == 2) {
            finalQuery = String.format(query, lastSyncDate, lastSyncDate, from, to);
        } else if (datesToBeReplaced == 3) {
            finalQuery = String.format(query, lastSyncDate, lastSyncDate, lastSyncDate, from, to);
        } else {
            finalQuery = String.format(query, from, to);
        }
        List list = ugandaEMRSyncService.getFinalList(columns, finalQuery);
        return list;
    }

    private List getDatabaseRecord(String query) {
        Session session = Context.getRegisteredComponent("sessionFactory", SessionFactory.class).getCurrentSession();
        SQLQuery sqlQuery = session.createSQLQuery(query);
        return sqlQuery.list();
    }


    public List<Map> processFHIRData(List<String> dataToProcess, String dataType, boolean addOrganizationToRecord) throws Exception {
        List<Map> maps = new ArrayList<>();
        SyncTaskType syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(FHIRSERVER_SYNC_TASK_TYPE_UUID);
        String localhostServerPort=Context.getAdministrationService().getGlobalProperty(GP_CBS_LOCALHOST_PORT);
        String localhostServerUsername=Context.getAdministrationService().getGlobalProperty(GP_CBS_LOCALHOST_USERNAME);
        String localhostServerPassword=Context.getAdministrationService().getGlobalProperty(GP_CBS_LOCALHOST_PASSWORD);
        for (String data : dataToProcess) {
            Map result = ugandaEMRHttpURLConnection.getByWithBasicAuth("", "", "", "http://localhost:"+localhostServerPort+"/openmrs/ws/fhir2/R4/" + dataType + "/" + data, localhostServerUsername, localhostServerPassword, "String");
            if (result.get("responseCode").equals(200) || result.get("responseCode").equals(201)) {
                String jsonData = result.get("result").toString();

                if (addOrganizationToRecord) {
                    jsonData = addOrganizationToRecord(jsonData);
                }

                Map map = ugandaEMRHttpURLConnection.sendPostBy(syncTaskType.getUrl() + dataType, syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), "", jsonData, false);
                map.put("DataType", dataType);
                map.put("uuid", data);
                maps.add(map);
            }
        }
        return maps;
    }

    public String addOrganizationToRecord(String payload) {
        String healthCenterIdentifier = Context.getAdministrationService().getGlobalProperty("ugandaemr.dhis2.organizationuuid");
        String managingOrganizationStirng = String.format("{\"reference\": \"Organization/%s\"}", healthCenterIdentifier);
        JSONObject finalPayLoadJson = new JSONObject(payload);
        JSONObject managingOrganizationJson = new JSONObject(managingOrganizationStirng);

        finalPayLoadJson.put("managingOrganization", managingOrganizationJson);
        return finalPayLoadJson.toString();
    }


    public List<Map> syncFHIRData() {

        List<Map> mapList = new ArrayList<>();

        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();

        if (syncGlobalProperties.getGlobalProperty(GP_ENABLE_SYNC_CBS_FHIR_DATA).equals("true")) {

            try {
                mapList.addAll(processFHIRData(getDatabaseRecordWithOutFacility(PERSON_UUID_QUERY, "", "", 3, Arrays.asList("uuid")), "Person", false));

                mapList.addAll(processFHIRData(getDatabaseRecordWithOutFacility(PRACTITIONER_UUID_QUERY, "", "", 3, Arrays.asList("uuid")), "Practitioner", true));

                mapList.addAll(processFHIRData(getDatabaseRecordWithOutFacility(PATIENT_UUID_QUERY, "", "", 3, Arrays.asList("uuid")), "Patient", true));

                mapList.addAll(processFHIRData(getDatabaseRecordWithOutFacility(ENCOUNTER_UUID_QUERY, "", "", 3, Arrays.asList("uuid")), "Encounter", false));

                mapList.addAll(processFHIRData(getDatabaseRecordWithOutFacility(OBSERVATION_UUID_QUERY, "", "", 2, Arrays.asList("uuid")), "Observation", false));

                Date now = new Date();
                if (!mapList.isEmpty()) {
                    String newSyncDate = SyncConstant.DEFAULT_DATE_FORMAT.format(now);

                    syncGlobalProperties.setGlobalProperty(SyncConstant.LAST_SYNC_DATE, newSyncDate);
                }
            } catch (Exception e) {
                log.error("Failed to process sync records central server", e);
            }
        } else {
            Map map = new HashMap();
            map.put("error", "Syncing of CBS Data is not enabled. Please enable it and proceed");
            mapList.add(map);
        }

        return mapList;
    }
}
