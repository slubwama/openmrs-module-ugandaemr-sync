package org.openmrs.module.ugandaemrsync.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Arrays;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.LAST_SYNC_DATE;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.LAST_SYNC_DATE_TO_FORMAT;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.GP_ENABLE_SYNC_CBS_FHIR_DATA;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PERSON_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PRACTITIONER_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PATIENT_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.ENCOUNTER_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.OBSERVATION_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIRSERVER_SYNC_TASK_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.GP_DHIS2;

/**
 * Created by lubwamasamuel on 07/11/2016.
 */
public class SyncFHIRRecord {

    UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

    Log log = LogFactory.getLog(SyncFHIRRecord.class);

    String healthCenterIdentifier;
    String lastSyncDate;

    FhirPersonService fhirPersonService;
    FhirPatientService fhirPatientService;
    FhirPractitionerService fhirPractitionerService;
    FhirEncounterService fhirEncounterService;
    FhirObservationService fhirObservationService;


    public SyncFHIRRecord() {
        healthCenterIdentifier = Context.getAdministrationService().getGlobalProperty(GP_DHIS2);
        lastSyncDate = Context.getAdministrationService().getGlobalProperty(LAST_SYNC_DATE);

        try {
            Field serviceContextField = Context.class.getDeclaredField("serviceContext");
            serviceContextField.setAccessible(true);
            try {
                ApplicationContext applicationContext = ((ServiceContext) serviceContextField.get(null))
                        .getApplicationContext();

                fhirPersonService = applicationContext.getBean(FhirPersonService.class);
                fhirPatientService = applicationContext.getBean(FhirPatientService.class);
                fhirEncounterService = applicationContext.getBean(FhirEncounterService.class);
                fhirObservationService = applicationContext.getBean(FhirObservationService.class);
                fhirPractitionerService = applicationContext.getBean(FhirPractitionerService.class);


            } finally {
                serviceContextField.setAccessible(false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List getDatabaseRecordWithOutFacility(String query, String from, String to, int datesToBeReplaced, List<String> columns, String dataType) {
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

    public List<Map> processFHIRData(List<String> dataToProcess, String dataType, boolean addOrganizationToRecord) {
        List<Map> maps = new ArrayList<>();
        SyncTaskType syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(FHIRSERVER_SYNC_TASK_TYPE_UUID);

        for (String uuid : dataToProcess) {
            try {

                IParser parser = FhirContext.forR4().newJsonParser();
                String jsonData = "";

                if (dataType.equals("Patient")) {
                    jsonData = addOrganizationToRecord(parser.encodeResourceToString(fhirPatientService.get(uuid)));
                } else if (dataType.equals("Person")) {
                    jsonData = addOrganizationToRecord(parser.encodeResourceToString(fhirPersonService.get(uuid)));
                } else if (dataType.equals("Encounter")) {
                    jsonData = parser.encodeResourceToString(fhirEncounterService.get(uuid));
                } else if (dataType.equals("Observation")) {
                    jsonData = parser.encodeResourceToString(fhirObservationService.get(uuid));
                } else if (dataType.equals("Practitioner")) {
                    jsonData = addOrganizationToRecord(parser.encodeResourceToString(fhirPractitionerService.get(uuid)));
                }

                log.info("Generating payload for " + dataType + " with uuid " + uuid);
                log.debug("JSON payload " + jsonData);

                if (jsonData.equals("")) {
                    log.info("Empty payload for " + dataType + " with uuid " + uuid);
                } else {
                    Map map = ugandaEMRHttpURLConnection.sendPostBy(syncTaskType.getUrl() + dataType, syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), "", jsonData, false);
                    map.put("DataType", dataType);
                    map.put("uuid", uuid);
                    maps.add(map);
                }

            } catch (Exception e) {
                log.error("Error processing " + dataType + " with uuid " + uuid, e);
            }


        }
        return maps;
    }

    private List<String> getDatabaseRecordWithFHIR(String query, String from, String to, int datesToBeReplaced, List<String> columns, String dataType) {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        String lastSyncDate = syncGlobalProperties.getGlobalProperty(String.format(LAST_SYNC_DATE_TO_FORMAT, dataType));
        List<String> processedFHIRJson = new ArrayList<>();

        IParser parser = FhirContext.forR4().newJsonParser();

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

        if (list.size() > 0) {
            for (Object uuid : list) {
                try {
                    String jsonData = "";
                    if (dataType.equals("Patient")) {
                        jsonData = addOrganizationToRecord(parser.encodeResourceToString(fhirPatientService.get(uuid.toString())));
                    } else if (dataType.equals("Person")) {
                        jsonData = addOrganizationToRecord(parser.encodeResourceToString(fhirPersonService.get(uuid.toString())));
                    } else if (dataType.equals("Encounter")) {
                        jsonData = parser.encodeResourceToString(fhirEncounterService.get(uuid.toString()));
                    } else if (dataType.equals("Observation")) {
                        jsonData = parser.encodeResourceToString(fhirObservationService.get(uuid.toString()));
                    } else if (dataType.equals("Practitioner")) {
                        jsonData = addOrganizationToRecord(parser.encodeResourceToString(fhirPractitionerService.get(uuid.toString())));
                    }


                    if (!jsonData.equals("")) {
                        processedFHIRJson.add(jsonData);
                    }

                    log.info("Generating payload for " + dataType + " with uuid " + uuid);
                    log.debug("JSON payload " + jsonData);

                } catch (Exception e) {
                    log.error("Error processing " + dataType + " with uuid " + uuid, e);
                }


            }
        }
        return processedFHIRJson;
    }


    public List<Map> sendFHIRDataToSync(List<String> fhirDataStrings, String resourceType, Integer interval) {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        if (fhirDataStrings.isEmpty()) {
            return null;
        }

        List<Map> maps = new ArrayList<>();
        SyncTaskType syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(FHIRSERVER_SYNC_TASK_TYPE_UUID);
        String bundleWrapperString = "{\"resourceType\":\"Bundle\",\"type\":\"transaction\",\"entry\":[%]}";
        List<String> resourceBundles = new ArrayList<>();
        StringBuilder currentBundleString = new StringBuilder();
        Integer currentNumberOfBundlesCollected = 0;

        for (String resource : fhirDataStrings) {
            if (currentNumberOfBundlesCollected < interval) {
                currentBundleString.append(resource);
            } else {
                if (currentBundleString.equals(new StringBuilder())) {
                    resourceBundles.add(String.format(bundleWrapperString, currentBundleString.toString()));
                }

                currentNumberOfBundlesCollected = 1;
                currentBundleString = new StringBuilder();
                currentBundleString.append(resource);
            }

        }

        if (currentBundleString != null) {
            resourceBundles.add(String.format(bundleWrapperString, currentBundleString.toString()));
        }

        for (String bundledResources : resourceBundles) {

            Map map;
            try {
                map = ugandaEMRHttpURLConnection.sendPostBy(syncTaskType.getUrl() + "Bundle", syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), "", bundledResources, false);
                map.put("DataType", resourceType);
                maps.add(map);
                String newSyncDate = SyncConstant.DEFAULT_DATE_FORMAT.format(new Date());
                syncGlobalProperties.setGlobalProperty(String.format(SyncConstant.LAST_SYNC_DATE, resourceType), newSyncDate);
            } catch (Exception e) {
                log.error("Failed to send Resources " + bundledResources + " with error", e);
            }
        }

        return maps;

    }

    public String addOrganizationToRecord(String payload) {
        if (payload.isEmpty()) {
            return "";
        }

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
                mapList.addAll(sendFHIRDataToSync(getDatabaseRecordWithFHIR(PERSON_UUID_QUERY, "", "", 2, Arrays.asList("uuid"), "Person"), "Person", 1000));
                mapList.addAll(sendFHIRDataToSync(getDatabaseRecordWithFHIR(PATIENT_UUID_QUERY, "", "", 2, Arrays.asList("uuid"), "Patient"), "Patient", 1000));
                mapList.addAll(sendFHIRDataToSync(getDatabaseRecordWithFHIR(PRACTITIONER_UUID_QUERY, "", "", 2, Arrays.asList("uuid"), "Practitioner"), "Practitioner", 1000));
                mapList.addAll(sendFHIRDataToSync(getDatabaseRecordWithFHIR(ENCOUNTER_UUID_QUERY, "", "", 2, Arrays.asList("uuid"), "Encounter"), "Encounter", 1000));
                mapList.addAll(sendFHIRDataToSync(getDatabaseRecordWithFHIR(OBSERVATION_UUID_QUERY, "", "", 1, Arrays.asList("uuid"), "Observation"), "Observation", 1000));

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
