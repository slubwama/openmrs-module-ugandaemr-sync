package org.openmrs.module.ugandaemrsync.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.Date;


import static org.openmrs.module.ugandaemrsync.server.SyncConstant.HEALTH_CENTER_SYNC_ID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.LAST_SYNC_DATE;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_USERNAME;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_PASSWORD;


public class SyncDataRecord {

    UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

    Log log = LogFactory.getLog(SyncDataRecord.class);

    public SyncDataRecord() {
    }

    public int syncRecords(List newSyncRecords) {
        int connectionStatus = 200;
        try {
            connectionStatus = ugandaEMRHttpURLConnection.getCheckConnection("google.com");
        } catch (Exception e) {
            log.error("Connection failed", e);
        }
        if (connectionStatus == SyncConstant.CONNECTION_SUCCESS_200) {
            int size = 0;
            // Batch process records in groups to reduce HTTP calls
            int batchSize = 100; // Process 100 records at a time
            int totalRecords = newSyncRecords.size();

            for (int i = 0; i < totalRecords; i += batchSize) {
                int end = Math.min(i + batchSize, totalRecords);
                List batch = newSyncRecords.subList(i, end);

                try {
                    Map response = syncBatchData(batch);
                    String success = String.valueOf(response.get("response"));
                    if (success.equalsIgnoreCase("successful")) {
                        size += batch.size();
                    }
                } catch (Exception e) {
                    log.error("Failed to Sync batch data", e);
                }
            }
            return size;
        } else {
            log.info("Connection to internet was not Successful. Code: " + connectionStatus);
            return 0;
        }
    }

    /**
     * @param syncRecord
     * @return
     * @throws Exception
     */
    public Map syncData(String syncRecord) throws Exception {
        String contentTypeXML = SyncConstant.XML_CONTENT_TYPE;

        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();

        String serverIP = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_IP);
        String serverProtocol = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_PROTOCOL);
        String facilitySyncId = syncGlobalProperties.getGlobalProperty(HEALTH_CENTER_SYNC_ID);

        String url = serverProtocol + serverIP + "/api";

        try {
            UUID uuid = UUID.fromString(facilitySyncId);

            facilitySyncId = uuid.toString();

            return ugandaEMRHttpURLConnection.sendPostByWithBasicAuth(contentTypeXML, syncRecord, facilitySyncId, url, syncGlobalProperties.getGlobalProperty(SERVER_USERNAME), syncGlobalProperties.getGlobalProperty(SERVER_PASSWORD), null);

        } catch (IllegalArgumentException exception) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("message", "No valid facility Id Found");
            return map;
        }
    }

    /**
     * Sync multiple records in a single batch to reduce HTTP calls
     * @param batchRecords List of records to sync as a batch
     * @return Response from the server
     * @throws Exception
     */
    private Map syncBatchData(List batchRecords) throws Exception {
        if (batchRecords == null || batchRecords.isEmpty()) {
            Map<String, String> emptyResponse = new HashMap<String, String>();
            emptyResponse.put("response", "failed");
            emptyResponse.put("message", "Empty batch");
            return emptyResponse;
        }

        // Build JSON array of all records in the batch
        JSONArray batchJson = new JSONArray();
        for (Object record : batchRecords) {
            Object row[] = (Object[]) record;
            if (row.length > 1) {
                batchJson.put(row[1]);
            }
        }

        String contentTypeJSON = SyncConstant.JSON_CONTENT_TYPE;
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        String serverIP = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_IP);
        String serverProtocol = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_PROTOCOL);
        String facilitySyncId = syncGlobalProperties.getGlobalProperty(HEALTH_CENTER_SYNC_ID);
        String url = serverProtocol + serverIP + "/api/batch";

        try {
            UUID uuid = UUID.fromString(facilitySyncId);
            facilitySyncId = uuid.toString();

            return ugandaEMRHttpURLConnection.sendPostByWithBasicAuth(contentTypeJSON, batchJson.toString(), facilitySyncId, url, syncGlobalProperties.getGlobalProperty(SERVER_USERNAME), syncGlobalProperties.getGlobalProperty(SERVER_PASSWORD), null);

        } catch (IllegalArgumentException exception) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("response", "failed");
            map.put("message", "No valid facility Id Found");
            return map;
        }
    }

    private List getDatabaseRecord(String query, String from, String to, int datesToBeReplaced, List<String> columns) {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        String facilityId = syncGlobalProperties.getGlobalProperty(HEALTH_CENTER_SYNC_ID);
        String lastSyncDate = syncGlobalProperties.getGlobalProperty(LAST_SYNC_DATE);

        String finalQuery;
        if (datesToBeReplaced == 1) {
            finalQuery = String.format(query, facilityId, lastSyncDate, from, to);
        } else if (datesToBeReplaced == 2) {
            finalQuery = String.format(query, facilityId, lastSyncDate, lastSyncDate, from, to);
        } else if (datesToBeReplaced == 3) {
            finalQuery = String.format(query, facilityId, lastSyncDate, lastSyncDate, lastSyncDate, from, to);
        } else {
            finalQuery = String.format(query, facilityId, from, to);
        }
        List list = ugandaEMRSyncService.getFinalList(columns, finalQuery);
        return list;
    }


    public static Map<String, String> convertListOfMapsToJsonString(List list, List<String> columns) throws IOException {
        JSONArray result = new JSONArray();
        Map<String, String> vals = new HashMap<String, String>();
        for (Object item : list) {
            Object rows[] = (Object[]) item;
            JSONObject row = new JSONObject();

            for (int i = 0; i < columns.size(); i++) {
                row.put(columns.get(i), rows[i]);
            }

            result.put(row);
        }
        vals.put("json", result.toString());
        return vals;
    }

    private void processData(Integer mySize, String url, String query, int datesToBeReplaced, List<String> columns, Integer max) throws Exception {
        int startIndex = 0;
        boolean entireListNotProcessed = true;
        int offset = 0;
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        String serverIP = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_IP);
        String serverProtocol = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_PROTOCOL);
        String facilityURL = serverProtocol + serverIP + "/" + url;
        while (entireListNotProcessed) {
            List records = getDatabaseRecord(query, String.valueOf(offset), String.valueOf(max), datesToBeReplaced, columns);
            Map<String, String> data = SyncDataRecord.convertListOfMapsToJsonString(records, columns);
            String json = data.get("json");
            ugandaEMRHttpURLConnection.sendPostBy(facilityURL, syncGlobalProperties.getGlobalProperty(SERVER_USERNAME), syncGlobalProperties.getGlobalProperty(SERVER_PASSWORD), "", json, true);
            if (offset >= mySize || mySize <= max) {
                entireListNotProcessed = false;
            } else {
                startIndex = startIndex + 1;
            }
            offset = (startIndex * max);
        }
    }

    private Map<String, Integer> convertListToMap(List list) {
        Map<String, Integer> result = new HashMap<String, Integer>(list.size());
        for (Object item : list) {
            Object rows[] = (Object[]) item;
            result.put(String.valueOf(rows[1]), Integer.valueOf(String.valueOf(rows[0])));
        }
        return result;
    }

    public List syncData() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();

        String lastSyncDate = syncGlobalProperties.getGlobalProperty(SyncConstant.LAST_SYNC_DATE);
        String totalsQuery = SyncConstant.TABLES_TOTAL_QUERY;

        List totals = ugandaEMRSyncService.getDatabaseRecord(totalsQuery.replaceAll("lastSync", lastSyncDate));

        Integer max = Integer.valueOf(syncGlobalProperties.getGlobalProperty(SyncConstant.MAX_NUMBER_OF_ROWS));

        Map<String, Integer> numbers = convertListToMap(totals);

        Integer encounters = numbers.get("encounter");
        Integer obs = numbers.get("obs");
        Integer persons = numbers.get("person");
        Integer person_names = numbers.get("person_name");
        Integer person_addresses = numbers.get("person_address");
        Integer person_attributes = numbers.get("person_attribute");
        Integer patients = numbers.get("patient");
        Integer patient_identifiers = numbers.get("patient_identifier");
        Integer visits = numbers.get("visit");
        Integer encounter_providers = numbers.get("encounter_provider");
        Integer providers = numbers.get("provider");
        Integer encounter_roles = numbers.get("encounter_role");

        Integer fingerprints = numbers.get("fingerprint");

        try {
            processData(encounters, "api/encounters", SyncConstant.ENCOUNTER_QUERY, 3, SyncConstant.ENCOUNTER_COLUMNS, max);
            processData(obs, "api/obs", SyncConstant.OBS_QUERY, 2, SyncConstant.OBS_COLUMNS, max);
            processData(persons, "api/persons", SyncConstant.PATIENT_QUERY, 3, SyncConstant.PATIENT_COLUMNS, max);
            processData(person_names, "api/person_names", SyncConstant.PERSON_NAME_QUERY, 3, SyncConstant.PERSON_NAME_COLUMNS, max);
            processData(person_addresses, "api/person_addresses", SyncConstant.PERSON_ADDRESS_QUERY, 3, SyncConstant.PERSON_ADDRESS_COLUMNS, max);
            processData(person_attributes, "api/person_attributes", SyncConstant.PERSON_ATTRIBUTE_QUERY, 3, SyncConstant.PERSON_ATTRIBUTE_COLUMNS, max);
            processData(patients, "api/patients", SyncConstant.PATIENT_QUERY, 3, SyncConstant.PATIENT_COLUMNS, max);
            processData(patient_identifiers, "api/patient_identifiers", SyncConstant.PATIENT_IDENTIFIER_QUERY, 3, SyncConstant.PATIENT_IDENTIFIER_COLUMNS, max);
            processData(visits, "api/visits", SyncConstant.VISIT_QUERY, 3, SyncConstant.VISIT_COLUMNS, max);
            processData(encounter_providers, "api/encounter_providers", SyncConstant.ENCOUNTER_PROVIDER_QUERY, 3, SyncConstant.ENCOUNTER_PROVIDER_COLUMNS, max);
            processData(providers, "api/providers", SyncConstant.PROVIDER_QUERY, 3, SyncConstant.PROVIDER_COLUMNS, max);
            processData(fingerprints, "api/fingerprints", SyncConstant.FINGERPRINT_QUERY, 1, SyncConstant.FINGERPRINT_COLUMNS, max);

            Date now = new Date();

            String newSyncDate = SyncConstant.DEFAULT_DATE_FORMAT.format(now);

            syncGlobalProperties.setGlobalProperty(SyncConstant.LAST_SYNC_DATE, newSyncDate);
        } catch (Exception e) {
            log.error("Faied to process sync records central server", e);
        }

        return totals;
    }
}
