package org.openmrs.module.ugandaemrsync.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;

import java.io.IOException;
import java.util.*;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

public class SyncFingerPrintDataRecord {

    UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

    Log log = LogFactory.getLog(SyncFingerPrintDataRecord.class);

    public SyncFingerPrintDataRecord() {
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
            Iterator it = newSyncRecords.iterator();
            while (it.hasNext()) {
                Object row[] = (Object[]) it.next();
                try {
                    Map response = syncData(String.valueOf(row[1]));
                    Integer uuid = Integer.valueOf(String.valueOf(row[0]));
                    String success = String.valueOf(response.get("response"));
                    if (success.equalsIgnoreCase("successful")) {
                        size += 1;
                    }
                } catch (Exception e) {
                    log.error("Failed to Sync data", e);
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

        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

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
        Iterator it = list.iterator();
        Map<String, String> vals = new HashMap<String, String>();
        while (it.hasNext()) {
            Object rows[] = (Object[]) it.next();
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
            Map<String, String> data = SyncFingerPrintDataRecord.convertListOfMapsToJsonString(records, columns);
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
        Map<String, Integer> result = new HashMap<String, Integer>();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Object rows[] = (Object[]) it.next();
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

        Integer fingerprints = numbers.get("fingerprint");

        try {
            processData(fingerprints, "api/fingerprints", SyncConstant.FINGERPRINT_QUERY, 1, SyncConstant.FINGERPRINT_COLUMNS, max);
            Date now = new Date();
            String newSyncDate = SyncConstant.DEFAULT_DATE_FORMAT.format(now);
            syncGlobalProperties.setGlobalProperty(SyncConstant.LAST_SYNC_DATE, newSyncDate);
        } catch (Exception e) {
            log.error("Failed to process sync records central server", e);
        }

        return totals;
    }
}
