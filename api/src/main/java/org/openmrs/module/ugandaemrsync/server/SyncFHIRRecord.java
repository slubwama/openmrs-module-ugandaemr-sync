package org.openmrs.module.ugandaemrsync.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.param.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.module.fhir2.api.*;
import org.openmrs.module.fhir2.api.FhirEpisodeOfCareService;
import org.openmrs.module.fhir2.api.search.param.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.model.SyncFhirCase;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfileLog;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.util.UgandaEMRSyncUtil;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.context.ApplicationContext;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hl7.fhir.r4.model.Patient.SP_IDENTIFIER;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.FSHR_SYNC_FHIR_PROFILE_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.CROSS_BORDER_CR_SYNC_FHIR_PROFILE_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.PATIENT_ID_TYPE_CROSS_BORDER_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.PATIENT_ID_TYPE_CROSS_BORDER_NAME;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.LAST_SYNC_DATE;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.GP_ENABLE_SYNC_CBS_FHIR_DATA;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PERSON_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PRACTITIONER_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PATIENT_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.ENCOUNTER_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.OBSERVATION_UUID_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIRSERVER_SYNC_TASK_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.GP_DHIS2;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIR_BUNDLE_RESOURCE_TRANSACTION;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIR_BUNDLE_CASE_RESOURCE_TRANSACTION;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIR_BUNDLE_RESOURCE_METHOD_POST;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIR_BUNDLE_RESOURCE_METHOD_PUT;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.ENCOUNTER_ROLE;

/**
 * Created by lubwamasamuel on 07/11/2016.
 */
public class SyncFHIRRecord {

    UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

    Log log = LogFactory.getLog(SyncFHIRRecord.class);

    private SyncFhirProfile syncFhirProfile;

    String healthCenterIdentifier;
    String healthCenterName;
    String lastSyncDate;

    SyncFhirCase syncFhirCase = null;

    private SyncFhirProfile profile = null;
    private List<PatientProgram> patientPrograms;

    Map<String, Object> anyOtherObject = new HashMap<>();


    public SyncFHIRRecord() {
        healthCenterIdentifier = Context.getAdministrationService().getGlobalProperty(GP_DHIS2);
        healthCenterName = Context.getLocationService().getLocationByUuid("629d78e9-93e5-43b0-ad8a-48313fd99117").getName();
        lastSyncDate = Context.getAdministrationService().getGlobalProperty(LAST_SYNC_DATE);
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


    public List<Map> processFHIRData(List<String> dataToProcess, String dataType, boolean addOrganizationToRecord) {
        List<Map> maps = new ArrayList<>();
        SyncTaskType syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(FHIRSERVER_SYNC_TASK_TYPE_UUID);

        FhirPersonService fhirPersonService;
        FhirPatientService fhirPatientService;
        FhirPractitionerService fhirPractitionerService;
        FhirEncounterService fhirEncounterService;
        FhirObservationService fhirObservationService;


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

        for (String data : dataToProcess) {
            try {

                IParser parser = FhirContext.forR4().newJsonParser();
                String jsonData = "";

                if (dataType == "Patient") {
                    jsonData = parser.encodeResourceToString(fhirPatientService.get(data));
                } else if (dataType.equals("Person")) {
                    jsonData = parser.encodeResourceToString(fhirPersonService.get(data));
                } else if (dataType.equals("Encounter")) {
                    jsonData = parser.encodeResourceToString(fhirEncounterService.get(data));
                } else if (dataType.equals("Observation")) {
                    jsonData = parser.encodeResourceToString(fhirObservationService.get(data));
                } else if (dataType.equals("Practitioner")) {
                    jsonData = parser.encodeResourceToString(fhirPractitionerService.get(data));
                }

                if (!jsonData.equals("")) {
                    if (addOrganizationToRecord) {
                        jsonData = addOrganizationToRecord(jsonData, "managingOrganization");
                    }
                    Map map = ugandaEMRHttpURLConnection.sendPostBy(syncTaskType.getUrl() + dataType, syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), "", jsonData, false);
                    map.put("DataType", dataType);
                    map.put("uuid", data);
                    maps.add(map);
                }

            } catch (Exception e) {
                log.error(e);
            }


        }
        return maps;
    }

    public String addOrganizationToRecord(String payload, String attributeName) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (payload.isEmpty()) {
            return "";
        }

        String organizationString = String.format("{\"reference\":\"Organization/%s\",\"type\":\"Organization\",\"identifier\":{\"use\":\"official\",\"value\":\"%s\",\"system\":\"https://hmis.health.go.ug/\"},\"display\":\"%s\"}", healthCenterIdentifier, healthCenterIdentifier, healthCenterName);
        ObjectNode finalPayLoadJson = null;
        try {
            finalPayLoadJson = (ObjectNode) objectMapper.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        JsonNode organization = null;
        try {
            organization = objectMapper.readTree(organizationString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        finalPayLoadJson.put(attributeName, organization);
        return finalPayLoadJson.toString();
    }

    public String addServiceType(String payload, String attributeName) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (payload.isEmpty()) {
            return "";
        }
        ObjectNode finalPayLoadJson = null;
        try {
            finalPayLoadJson = (ObjectNode) objectMapper.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ObjectNode valueNode = objectMapper.createObjectNode();
        ArrayNode codingArray = objectMapper.createArrayNode();

        ObjectNode codingEntry = objectMapper.createObjectNode();
        codingEntry.put("code", "dcd87b79-30ab-102d-86b0-7a5022ba4115");
        codingEntry.put("display", "MEDICAL OUTPATIENT");

        codingArray.add(codingEntry);
        valueNode.set("coding", codingArray);
        valueNode.put("text", "Out-Patient");

        finalPayLoadJson.set(attributeName, valueNode);
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

                    syncGlobalProperties.setGlobalProperty(LAST_SYNC_DATE, newSyncDate);
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


    public Collection<SyncFhirResource> generateCaseBasedFHIRResourceBundles(SyncFhirProfile syncFhirProfile) {
        this.profile = syncFhirProfile;
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        if (syncFhirProfile != null && (!syncFhirProfile.getIsCaseBasedProfile() || syncFhirProfile.getCaseBasedPrimaryResourceType() == null)) {
            return null;
        }

        this.syncFhirProfile = syncFhirProfile;

        Collection<SyncFhirResource> syncFhirResources = new ArrayList<>();


        List<Integer> savedResourcesIds = new ArrayList<>();

        Date currentDate = new Date();

        identifyNewCases(syncFhirProfile, currentDate);


        List<SyncFhirCase> syncFhirCases = ugandaEMRSyncService.getSyncFhirCasesByProfile(syncFhirProfile);

        for (SyncFhirCase syncFhirCase : syncFhirCases) {
            SyncFhirResource syncFhirResource = saveCaseResources(syncFhirProfile, syncFhirCase);
            if (syncFhirResource != null) {
                savedResourcesIds.add(syncFhirResource.getId());
            }
        }

        if (savedResourcesIds.size() > 0) {
            SyncFhirProfileLog syncFhirProfileLog = new SyncFhirProfileLog();
            syncFhirProfileLog.setNumberOfResources(savedResourcesIds.size());
            syncFhirProfileLog.setProfile(syncFhirProfile);
            assert syncFhirProfile != null;
            syncFhirProfileLog.setResourceType(syncFhirProfile.getCaseBasedPrimaryResourceType());
            syncFhirProfileLog.setLastGenerationDate(currentDate);
            ugandaEMRSyncService.saveSyncFhirProfileLog(syncFhirProfileLog);
        }

        return syncFhirResources;
    }


    /**
     * Create and Identify new cases based on a given Sync Fhir Profile
     *
     * @param syncFhirProfile the profile for which the cases belong to
     * @param currentDate     Date when this task is being executed,
     */
    public void identifyNewCases(SyncFhirProfile syncFhirProfile, Date currentDate) {

        List<PatientProgram> patientProgramList;

        if (syncFhirProfile.getCaseBasedPrimaryResourceType().equals("EpisodeOfCare")) {
            Collection<Program> programs = new ArrayList<>();
            Program program = Context.getProgramWorkflowService().getProgramByUuid(syncFhirProfile.getCaseBasedPrimaryResourceTypeId());
            patientProgramList = Context.getProgramWorkflowService().getPatientPrograms(null, program, null, null, null, null, false);


            for (PatientProgram patientProgram : patientProgramList) {
                Patient patient = patientProgram.getPatient();
                if (!patient.getVoided()) {
                    String caseIdentifier = patientProgram.getUuid();
                    saveSyncFHIRCase(syncFhirProfile, currentDate, patient, caseIdentifier);
                }
            }
        } else if (syncFhirProfile.getCaseBasedPrimaryResourceType().equals("Encounter")) {
            List<EncounterType> encounterTypes = new ArrayList<>();

            encounterTypes.add(Context.getEncounterService().getEncounterTypeByUuid(syncFhirProfile.getCaseBasedPrimaryResourceTypeId()));

            EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteria(null, null, null, null, null, null, encounterTypes, null, null, null, false);


            for (Encounter encounter : Context.getEncounterService().getEncounters(encounterSearchCriteria)) {

                PatientIdentifier patientIdentifier = getPatientIdentifierByType(encounter.getPatient(), syncFhirProfile.getPatientIdentifierType());

                if (patientIdentifier != null) {
                    saveSyncFHIRCase(syncFhirProfile, currentDate, encounter.getPatient(), patientIdentifier.getIdentifier());
                }

            }
        } else if (syncFhirProfile.getCaseBasedPrimaryResourceType().equals("ProgramWorkFlowState")) {
            ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();

            ProgramWorkflowState programWorkflowState = programWorkflowService.getStateByUuid(syncFhirProfile.getCaseBasedPrimaryResourceTypeId());

            List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(null, programWorkflowState.getProgramWorkflow().getProgram(), null, null, null, null, false);

            for (PatientProgram patientProgram : patientPrograms) {
                PatientState patientState = patientProgram.getCurrentState(programWorkflowState.getProgramWorkflow());
                if (patientState != null && patientState.getState().equals(programWorkflowState)) {
                    Patient patient = patientProgram.getPatient();
                    String caseIdentifier = patientProgram.getUuid();
                    saveSyncFHIRCase(syncFhirProfile, currentDate, patient, caseIdentifier);
                }
            }

        } else if (syncFhirProfile.getCaseBasedPrimaryResourceType().equals("Order")) {
            OrderService orderService = Context.getOrderService();
            OrderType orderType = orderService.getOrderTypeByUuid(syncFhirProfile.getCaseBasedPrimaryResourceTypeId());

            if (orderType != null) {

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = formatter.format(new Date());

                List list = Context.getAdministrationService().executeSQL("select Distinct patient_id from  orders where order_type_id = " + orderType.getId() + "  AND date_activated >= \"" + formattedDate + "\" and date_stopped is NULL ;", true);
                List<Patient> patientList = new ArrayList<>();

                if (list.size() > 0) {
                    for (Object o : list) {
                        patientList.add(Context.getPatientService().getPatient(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString())));
                    }
                }
                for (Patient patient : patientList) {
                    String patientIdentifier = patient.getPatientId().toString();
                    saveSyncFHIRCase(syncFhirProfile, currentDate, patient, patientIdentifier);
                }
            }
        } else if (syncFhirProfile.getCaseBasedPrimaryResourceType().equals("PatientIdentifierType")) {
            PatientService patientService = Context.getPatientService();
            PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByUuid(syncFhirProfile.getCaseBasedPrimaryResourceTypeId());

            if (patientIdentifierType != null) {

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = formatter.format(new Date());
                List<Patient> patients = new ArrayList<>();


                List list = Context.getAdministrationService().executeSQL("select patient_id from patient_identifier where identifier_type=" + patientIdentifierType.getId() + " and patient_id not  in (select patient from sync_fhir_case where profile=" + syncFhirProfile.getId() + ");", true);

                List<Patient> patientList = new ArrayList<>();

                if (list.size() > 0) {
                    for (Object o : list) {
                        patientList.add(patientService.getPatient(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString())));
                    }
                }
                for (Patient patient : patientList.stream().filter(patient -> !patient.getVoided()).collect(Collectors.toList())) {
                    String patientIdentifier = patient.getPatientId().toString();
                    saveSyncFHIRCase(syncFhirProfile, currentDate, patient, patientIdentifier);
                }
            }
        } else if (syncFhirProfile.getCaseBasedPrimaryResourceType().equals("CohortType")) {
            String uuid = syncFhirProfile.getCaseBasedPrimaryResourceTypeId();

            List<Patient> patientList = getPatientByCohortType(uuid);
            if (patientList.size() > 0) {
                for (Patient patient : patientList) {
                    String patientIdentifier = patient.getPatientId().toString();
                    saveSyncFHIRCase(syncFhirProfile, currentDate, patient, patientIdentifier);
                }
            }

        }
    }

    public SyncFhirCase saveSyncFHIRCase(SyncFhirProfile syncFhirProfile, Date currentDate, Patient patient, String caseIdentifier) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncFhirCase syncFhirCase = ugandaEMRSyncService.getSyncFHIRCaseBySyncFhirProfileAndPatient(syncFhirProfile, patient, caseIdentifier);
        if (syncFhirCase == null) {
            syncFhirCase = new SyncFhirCase();
            syncFhirCase.setCaseIdentifier(caseIdentifier);
            syncFhirCase.setPatient(patient);
            syncFhirCase.setProfile(syncFhirProfile);
            syncFhirCase.setDateCreated(currentDate);
            return ugandaEMRSyncService.saveSyncFHIRCase(syncFhirCase);
        }

        return null;

    }

    public SyncFhirResource saveCaseResources(SyncFhirProfile syncFhirProfile, SyncFhirCase syncFhirCase) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        try {
            String resource = generateFHIRCaseResource(syncFhirProfile, syncFhirCase);

            if (resource != null && !resource.isEmpty()) {
                SyncFhirResource syncFHIRResource = new SyncFhirResource();
                syncFHIRResource.setGeneratorProfile(syncFhirProfile);
                syncFHIRResource.setResource(resource);
                syncFHIRResource.setSynced(false);
                syncFHIRResource.setPatient(syncFhirCase.getPatient());
                ugandaEMRSyncService.saveFHIRResource(syncFHIRResource);
                syncFhirCase.setLastUpdateDate(syncFHIRResource.getDateCreated());

                return syncFHIRResource;
            }
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }


    private String generateFHIRCaseResource(SyncFhirProfile syncFhirProfile, SyncFhirCase syncFHIRCase) {

        Collection<String> resources = new ArrayList<>();
        List<Encounter> encounters = new ArrayList<>();
        Date currentDate = new Date();
        Date lastUpdateDate;
        List<Order> orderList = new ArrayList<>();

        if (syncFHIRCase.getLastUpdateDate() == null) {
            if (!syncFhirProfile.getSyncDataEverSince() && syncFhirProfile.getDataToSyncStartDate() == null) {
                lastUpdateDate = OpenmrsUtil.firstSecondOfDay(new Date());
            } else {
                lastUpdateDate = getDefaultLastSyncDate();
            }
        } else {
            lastUpdateDate = syncFHIRCase.getLastUpdateDate();
        }


        String[] resourceTypes = syncFhirProfile.getResourceTypes().split(",");
        OrderService orderService = Context.getOrderService();

        for (String resource : resourceTypes) {
            switch (resource) {
                case "Patient":
                    List<PatientIdentifier> patientIdentifiers = new ArrayList<>();
                    patientIdentifiers.add(getPatientIdentifierByType(syncFHIRCase.getPatient(), syncFhirProfile.getPatientIdentifierType()));
                    resources.addAll(groupInCaseBundle("Patient", getPatientResourceBundle(syncFhirProfile, patientIdentifiers, syncFHIRCase), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "Person":
                    List<Person> personList = new ArrayList<>();
                    Person person = syncFHIRCase.getPatient().getPerson();

                    if (syncFHIRCase.getLastUpdateDate() == null) {
                        personList.add(syncFHIRCase.getPatient().getPerson());
                    } else if ((person.getDateChanged() != null && person.getDateChanged().after(syncFHIRCase.getLastUpdateDate())) || (person.getDateCreated() != null && person.getDateCreated().after(syncFHIRCase.getLastUpdateDate()))) {
                        personList.add(syncFHIRCase.getPatient().getPerson());
                    }

                    if (!personList.isEmpty()) {
                        resources.addAll(groupInCaseBundle("Person", getPersonResourceBundle(syncFhirProfile, personList, syncFHIRCase), syncFhirProfile.getPatientIdentifierType().getName()));
                    }
                    break;
                case "EpisodeOfCare":
                    ArrayNode typeArray = null;
                    try {
                        typeArray = (ArrayNode) getSearchParametersInJsonObject("EpisodeOfCare", syncFhirProfile.getResourceSearchParameter()).get("type");
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    List<PatientProgram> patientProgramList = new ArrayList<>();

                    for (JsonNode typeNode : typeArray) {
                        String programUuid = typeNode.asText(); // or use typeNode.get("code").asText() if it’s an object
                        Program program = Context.getProgramWorkflowService().getProgramByUuid(programUuid);

                        List<PatientProgram> programs = Context.getProgramWorkflowService()
                                .getPatientPrograms(syncFHIRCase.getPatient(), program, lastUpdateDate, currentDate, null, null, false);

                        patientProgramList.addAll(programs);
                    }

                    if (patientProgramList.size() > 0) {
                        resources.addAll(groupInCaseBundle("EpisodeOfCare", getEpisodeOfCareResourceBundle(patientProgramList), syncFhirProfile.getPatientIdentifierType().getName()));
                    }
                    anyOtherObject.put("episodeOfCare", patientProgramList);
                    break;
                case "Encounter":
                    List<EncounterType> encounterTypes = new ArrayList<>();
                    DateRangeParam encounterLastUpdated = new DateRangeParam().setUpperBoundInclusive(currentDate).setLowerBoundInclusive(lastUpdateDate);

                    ArrayNode encounterUUIDs = null;
                    try {
                        encounterUUIDs = (ArrayNode) getSearchParametersInJsonObject("Encounter", syncFhirProfile.getResourceSearchParameter()).get("type");
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    for (JsonNode node : encounterUUIDs) {
                        String uuid = node.asText();
                        EncounterType encounterType = Context.getEncounterService().getEncounterTypeByUuid(uuid);
                        if (encounterType != null) {
                            encounterTypes.add(encounterType);
                        }
                    }

                    EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteria(syncFHIRCase.getPatient(), null, encounterLastUpdated.getLowerBoundAsInstant(), encounterLastUpdated.getUpperBoundAsInstant(), null, null, encounterTypes, null, null, null, false);
                    encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);
                    resources.addAll(groupInCaseBundle("Encounter", getEncounterResourceBundle(encounters), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "Observation":
                    if (encounters.size() > 0) {
                        resources.addAll(groupInCaseBundle("Observation", getObservationResourceBundle(syncFhirProfile, encounters, getPersonsFromEncounterList(encounters)), syncFhirProfile.getPatientIdentifierType().getName()));
                    }
                    break;
                case "ServiceRequest":

                    List<Order> testOrders = orderService.getActiveOrders(syncFHIRCase.getPatient(), orderService.getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID), null, null).stream().filter(testOrder -> testOrder.getDateActivated().compareTo(lastUpdateDate) >= 0).collect(Collectors.toList());
                    orderList = testOrders;
                    resources.addAll(groupInCaseBundle("ServiceRequest", getServiceRequestResourceBundle(testOrders), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "Condition":
                    resources.addAll(groupInCaseBundle("Condition", getConditionResourceBundle(syncFHIRCase, syncFhirProfile), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "AllergyIntolerance":
                    resources.addAll(groupInCaseBundle("AllergyIntolerance", getAllergyResourceBundle(syncFHIRCase, syncFhirProfile), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "Immunization":
                    resources.addAll(groupInCaseBundle("Immunization", getImmunizationResourceBundle(syncFHIRCase, syncFhirProfile), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "MedicationDispense":
                    resources.addAll(groupInCaseBundle("MedicationDispense", getMedicationDispenseResourceBundle(syncFHIRCase, syncFhirProfile), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "MedicationRequest":
                    resources.addAll(groupInCaseBundle("MedicationRequest", getMedicationRequestResourceBundle(syncFHIRCase, syncFhirProfile), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;

                case "DiagnosticReport":
                    resources.addAll(groupInCaseBundle("DiagnosticReport", getDiagnosticReportResourceBundle(syncFHIRCase, syncFhirProfile), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "Practitioner":
                    List<Provider> providerList = new ArrayList<>();
                    for (Order order : orderList) {
                        providerList.add(order.getOrderer());
                    }

                    for (Encounter encounter : encounters) {
                        Provider provider = getProviderFromEncounter(encounter);
                        if (provider != null) {
                            providerList.add(provider);
                        }
                    }

                    if (providerList.size() > 0) {
                        resources.addAll(groupInCaseBundle("Practitioner", getPractitionerResourceBundle(syncFhirProfile, encounters, orderList), syncFhirProfile.getPatientIdentifierType().getName()));
                    }
                    break;
            }
        }

        String finalCaseBundle = null;

        if (resources.size() > 0) {
            finalCaseBundle = String.format(FHIR_BUNDLE_CASE_RESOURCE_TRANSACTION, resources.toString());
        }


        return finalCaseBundle;
    }

    public List<SyncFhirResource> saveSyncFHIRResources(@NotNull Collection<String> resources, @NotNull String resourceType, @NotNull SyncFhirProfile syncFhirProfile, Date currentDate) {
        List<SyncFhirResource> syncFhirResources = new ArrayList<>();
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        for (String resource : resources) {
            SyncFhirResource syncFHIRResource = new SyncFhirResource();
            syncFHIRResource.setGeneratorProfile(syncFhirProfile);
            syncFHIRResource.setResource(resource);
            syncFHIRResource.setSynced(false);
            syncFhirResources.add(ugandaEMRSyncService.saveFHIRResource(syncFHIRResource));
        }


        if (syncFhirResources.size() > 0) {
            SyncFhirProfileLog syncFhirProfileLog = new SyncFhirProfileLog();
            syncFhirProfileLog.setNumberOfResources(syncFhirResources.size());
            syncFhirProfileLog.setProfile(syncFhirProfile);
            syncFhirProfileLog.setResourceType(resourceType);
            syncFhirProfileLog.setLastGenerationDate(currentDate);
            ugandaEMRSyncService.saveSyncFhirProfileLog(syncFhirProfileLog);
        }


        return syncFhirResources;
    }


    private List<PatientIdentifier> getPatientIdentifierFromEncounter(List<Encounter> encounters, PatientIdentifierType patientIdentifierType) {
        List<PatientIdentifier> patientIdentifiers = new ArrayList<>();
        for (Encounter encounter : encounters) {
            PatientIdentifier patientIdentifier = encounter.getPatient().getPatientIdentifier(patientIdentifierType);
            if (patientIdentifier != null) {
                patientIdentifiers.add(patientIdentifier);
            }
        }
        return patientIdentifiers;
    }

    private List<Person> getPersonFromEncounter(List<Encounter> encounters) {
        List<Person> personList = new ArrayList<>();
        for (Encounter encounter : encounters) {
            personList.add(encounter.getPatient().getPerson());
        }
        return personList;
    }

    private Collection<String> groupInBundles(String resourceType, Collection<IBaseResource> iBaseResources, Integer interval, String identifierTypeName) {
        List<String> resourceBundles = new ArrayList<>();
        List<String> currentBundleList = new ArrayList<>();

        for (IBaseResource iBaseResource : iBaseResources) {
            String jsonString = encodeResourceToString(resourceType, identifierTypeName, iBaseResource);

            if (currentBundleList.size() < interval) {
                currentBundleList.add(jsonString);
            } else {
                if (!currentBundleList.toString().equals("")) {
                    resourceBundles.add(String.format(FHIR_BUNDLE_RESOURCE_TRANSACTION, currentBundleList.toString()));
                }
                currentBundleList = new ArrayList<>();
                currentBundleList.add(jsonString);
            }
        }

        if (iBaseResources.size() > 0 && currentBundleList.size() < interval) {
            resourceBundles.add(String.format(FHIR_BUNDLE_RESOURCE_TRANSACTION, currentBundleList.toString()));
        }

        return resourceBundles;
    }

    public Collection<String> groupInCaseBundle(String resourceType, Collection<IBaseResource> iBaseResources, String identifierTypeName) {

        Collection<String> resourceBundles = new ArrayList<>();

        for (IBaseResource iBaseResource : iBaseResources) {

            String jsonString = encodeResourceToString(resourceType, identifierTypeName, iBaseResource);

            resourceBundles.add(jsonString);
        }


        return resourceBundles;
    }

    private String encodeResourceToString(String resourceType, String identifierTypeName, IBaseResource iBaseResource) {
        IParser parser = FhirContext.forR4().newJsonParser();
        String jsonString;

        try {
            jsonString = parser.encodeResourceToString(iBaseResource);

            switch (resourceType) {
                case "Patient":
                    jsonString = handlePatientResource(jsonString);
                    break;

                case "Practitioner":
                    jsonString = handlePractitionerResource(jsonString);
                    break;

                case "Person":
                    jsonString = wrapResourceWithId(jsonString, resourceType);
                    break;

                case "Encounter":
                    jsonString = handleEncounterResource(jsonString);
                    break;

                case "Observation":
                    jsonString = addReferencesMappingToObservation(wrapResourceInPostRequest(jsonString));
                    break;

                default:
                    jsonString = wrapResourceInPostRequest(jsonString);
                    break;
            }
        } catch (Exception e) {
            log.error("Error encoding resource: ", e);
            return "";
        }

        return jsonString;
    }

    private String handlePatientResource(String jsonString) {
        jsonString = correctEstimatedDOB(jsonString);
        if (profile != null && profile.getKeepProfileIdentifierOnly() != null && profile.getKeepProfileIdentifierOnly()) {
            try {
                jsonString = removeIdentifierExceptProfileId(jsonString, "identifier");
                jsonString = addCodingToIdentifier(jsonString, "identifier");
            } catch (Exception e) {
                log.error("Error processing patient identifiers: ", e);
            }
        }

        jsonString = commonPersonPractitionerTransformations(jsonString);
        return wrapResourceWithId(jsonString, "Patient");
    }

    private String handlePractitionerResource(String jsonString) {
        jsonString = commonPersonPractitionerTransformations(jsonString);
        return wrapResourceWithId(jsonString, "Practitioner");
    }

    private String commonPersonPractitionerTransformations(String jsonString) {
        jsonString = addAttributeToObject(jsonString, "telecom", "system", "phone");
        jsonString = addOrganizationToRecord(jsonString, "managingOrganization");
        jsonString = addUseOfficialToName(jsonString, "name");
        jsonString = removeAttribute(jsonString, "contained");
        jsonString = jsonString.replace("address5", "village")
                .replace("address4", "parish")
                .replace("address3", "subcounty")
                .replace("state", "city");
        return jsonString;
    }

    private String handleEncounterResource(String jsonString) {
        jsonString = addOrganizationToRecord(jsonString, "serviceProvider");
        jsonString = addServiceType(jsonString, "serviceType");

        if (anyOtherObject.get("episodeOfCare") != null) {
            jsonString = addEpisodeOfCareToEncounter(jsonString, anyOtherObject.get("episodeOfCare"));
        }

        return wrapResourceInPostRequest(jsonString);
    }

    private String wrapResourceWithId(String jsonString, String resourceType) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonObject = (ObjectNode) objectMapper.readTree(jsonString);
            String id = jsonObject.get("id").asText();
            return wrapResourceInPUTRequest(jsonString, resourceType, id);
        } catch (Exception e) {
            log.error("Error wrapping resource with ID: ", e);
            return wrapResourceInPostRequest(jsonString);
        }
    }

    private String addUseOfficialToName(String payload, String attributeName) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode jsonObject = null;
        try {
            jsonObject = (ObjectNode) objectMapper.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        int objectCount = 0;
        for (JsonNode jsonObject1 : (ArrayNode) jsonObject.get(attributeName)) {

            ArrayNode arrayNode = (ArrayNode) jsonObject.get(attributeName);


            ObjectNode elementNode = (ObjectNode) arrayNode.get(objectCount);


            elementNode.put("use", "official");
            objectCount++;
        }
        return jsonObject.toString();
    }

    private String removeIdentifierExceptProfileId(String payload, String attributeName) {
        if (profile != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonObject;
            try {
                jsonObject = (ObjectNode) objectMapper.readTree(payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            ArrayNode identifiers = (ArrayNode) jsonObject.get(attributeName);
            String expectedUuid = profile.getPatientIdentifierType().getUuid();

            for (int i = identifiers.size() - 1; i >= 0; i--) {
                JsonNode identifier = identifiers.get(i);
                JsonNode codeNode = identifier.path("type").path("coding").path(0).path("code");

                if (!expectedUuid.equals(codeNode.asText())) {
                    identifiers.remove(i);
                }
            }

            return jsonObject.toString();
        } else {
            return payload;
        }
    }

    private String removeAttribute(String payload, String attributeName) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonObject = (ObjectNode) objectMapper.readTree(payload);
            jsonObject.remove(attributeName);
            return objectMapper.writeValueAsString(jsonObject);
        } catch (IOException e) {
            throw new RuntimeException("Failed to remove attribute from payload", e);
        }
    }

    public String addCodingToIdentifier(String payload, String attributeName) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode jsonObject = (ObjectNode) mapper.readTree(payload);

            if (jsonObject.has(attributeName) && jsonObject.get(attributeName).isArray()) {
                ArrayNode identifiers = (ArrayNode) jsonObject.get(attributeName);

                for (int i = 0; i < identifiers.size(); i++) {
                    ObjectNode identifierNode = (ObjectNode) identifiers.get(i);
                    String id = identifierNode.get("id").asText();

                    PatientIdentifier patientIdentifier = Context.getPatientService().getPatientIdentifierByUuid(id);

                    ObjectNode codingNode = mapper.createObjectNode();
                    codingNode.put("system", "UgandaEMR");
                    codingNode.put("code", patientIdentifier.getIdentifierType().getUuid());

                    ArrayNode codingArray = mapper.createArrayNode();
                    codingArray.add(codingNode);

                    if (!identifierNode.has("type") || identifierNode.get("type").isNull()) {
                        identifierNode.set("type", mapper.createObjectNode());
                    }

                    ((ObjectNode) identifierNode.get("type")).set("coding", codingArray);
                }
            }

            return mapper.writeValueAsString(jsonObject);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add coding to identifier", e);
        }
    }

    public String correctEstimatedDOB(String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = (ObjectNode) mapper.readTree(payload);

            // Check for "id" field
            if (!root.has("id") || root.get("id").isNull()) {
                return payload;
            }

            String patientUuid = root.get("id").asText();
            Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);

            if (patient != null && Boolean.TRUE.equals(patient.getBirthdateEstimated()) && patient.getBirthdate() != null) {
                String formattedBirthdate = new SimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate());
                root.put("birthDate", formattedBirthdate);
            }

            return mapper.writeValueAsString(root);

        } catch (Exception e) {
            log.error("Failed to correct estimated DOB", e);
            return payload;
        }
    }


    private String addAttributeToObject(String payload, String targetObject, String attributeName, String attributeValue) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = (ObjectNode) mapper.readTree(payload);

            if (root.has(targetObject) && root.get(targetObject).isArray()) {
                ArrayNode array = (ArrayNode) root.get(targetObject);

                for (JsonNode node : array) {
                    if (node.isObject()) {
                        ((ObjectNode) node).put(attributeName, attributeValue);
                    }
                }
            }

            return mapper.writeValueAsString(root);

        } catch (Exception e) {
            log.error("Error adding attribute to object", e);
            throw new RuntimeException("Failed to add attribute to JSON array objects", e);
        }
    }


    private String getIdentifierSystemURL(String propertyName) {
        return Context.getAdministrationService().getGlobalProperty(propertyName);
    }

    public String wrapResourceInPostRequest(String payload) {
        if (payload.isEmpty()) {
            return "";
        }

        String wrappedResourceInRequest = String.format(FHIR_BUNDLE_RESOURCE_METHOD_POST, payload);
        return wrappedResourceInRequest;
    }

    public String wrapResourceInPUTRequest(String payload, String resourceType, String identifier) {
        if (payload.isEmpty()) {
            return "";
        }

        String wrappedResourceInRequest = String.format(FHIR_BUNDLE_RESOURCE_METHOD_PUT, payload, resourceType + "/" + identifier);
        return wrappedResourceInRequest;
    }


    private ApplicationContext getApplicationContext() {
        Field serviceContextField = null;
        ApplicationContext applicationContext = null;
        try {
            serviceContextField = Context.class.getDeclaredField("serviceContext");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        serviceContextField.setAccessible(true);

        try {
            applicationContext = ((ServiceContext) serviceContextField.get(null)).getApplicationContext();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return applicationContext;
    }

    private ObjectNode getSearchParametersInJsonObject(String resourceType, String searchParameterString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = (ObjectNode) objectMapper.readTree(searchParameterString);

        if (rootNode.get(resourceType) == null || rootNode.get(resourceType).isNull()) {
            return (ObjectNode) rootNode.get(resourceType.toLowerCase() + "Filter");
        }

        return (ObjectNode) rootNode.get(resourceType);
    }

    public String addSearchParameter(String resourceType, String searchParam, String searchParamString) {


        return searchParamString;
    }

    private Provider getProviderFromEncounter(Encounter encounter) {
        EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByUuid(ENCOUNTER_ROLE);

        Set<Provider> providers = encounter.getProvidersByRole(encounterRole);
        List<Provider> providerList = new ArrayList<>();
        for (Provider provider : providers) {
            providerList.add(provider);
        }

        if (!providerList.isEmpty()) {
            return providerList.get(0);
        } else {
            return null;
        }
    }

    private List<Person> getPersonsFromEncounterList(List<Encounter> encounters) {
        EncounterRole encounterClinicianRole = Context.getEncounterService().getEncounterRoleByUuid(ENCOUNTER_ROLE);
        List<Person> person = new ArrayList<>();

        for (Encounter encounter : encounters) {
            person.add(encounter.getPatient().getPerson());
            for (Provider provider : encounter.getProvidersByRole(encounterClinicianRole)) {
                person.add(provider.getPerson());
            }
        }
        return person;
    }


    public Collection<IBaseResource> getPatientResourceBundle(SyncFhirProfile syncFhirProfile, List<PatientIdentifier> patientIdentifiers, SyncFhirCase syncFhirCase) {

        DateRangeParam lastUpdated = new DateRangeParam();

        if (syncFhirProfile != null) {
            if (syncFhirProfile.getIsCaseBasedProfile()) {
                if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
                } else if (syncFhirCase != null) {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
                }
            } else {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "Patient"));

            }
        }


        TokenAndListParam patientReference = new TokenAndListParam();
        if (patientIdentifiers != null) {
            for (PatientIdentifier patientIdentifier : patientIdentifiers) {
                patientReference.addAnd(new TokenParam(patientIdentifier.getIdentifier()));
            }
        }


        PatientSearchParams patientSearchParams = new PatientSearchParams(null, null, null, patientReference, null, null, null, null, null, null, null, null, null, null, lastUpdated, null, null);

        return getApplicationContext().getBean(FhirPatientService.class).searchForPatients(patientSearchParams).getResources(0, Integer.MAX_VALUE);
    }

    public Collection<IBaseResource> getPractitionerResourceBundle(SyncFhirProfile syncFhirProfile, List<Encounter> encounterList, List<Order> orders) {
        PractitionerSearchParams practitionerSearchParams = new PractitionerSearchParams();

        if (syncFhirProfile != null) {
            if (!syncFhirProfile.getIsCaseBasedProfile()) {
                DateRangeParam lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "Practitioner"));
                practitionerSearchParams.setLastUpdated(lastUpdated);
            }
        }
        Collection<String> providerList = new ArrayList<>();
        for (Encounter encounter : encounterList) {
            Provider provider = getProviderFromEncounter(encounter);
            if (provider != null) {
                providerList.add(provider.getUuid());
            }
        }

        for (Order order : orders) {
            providerList.add(order.getOrderer().getUuid());
        }

        List<IBaseResource> iBaseResources = new ArrayList<>();

        if (!providerList.isEmpty()) {
            iBaseResources.addAll(getApplicationContext().getBean(FhirPractitionerService.class).get(providerList));
        }
        return iBaseResources;
    }

    public Collection<IBaseResource> getPersonResourceBundle(SyncFhirProfile syncFhirProfile, List<Person> personList, SyncFhirCase syncFhirCase) {


        DateRangeParam lastUpdated = new DateRangeParam();

        if (syncFhirProfile != null) {
            if (syncFhirProfile.getIsCaseBasedProfile()) {
                if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
                } else {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
                }
            } else {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "Patient"));

            }
        }

        PersonSearchParams personSearchParams = new PersonSearchParams();

        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        if (personList.size() > 0) {
            Collection<String> personListUUID = personList.stream().map(Person::getUuid).collect(Collectors.toCollection(ArrayList::new));
            iBaseResources.addAll(getApplicationContext().getBean(FhirPersonService.class).get(personListUUID));

        } else if (syncFhirProfile != null && !syncFhirProfile.getIsCaseBasedProfile()) {
            personSearchParams.setLastUpdated(lastUpdated);
        }
        return iBaseResources;
    }


    public Collection<IBaseResource> getEncounterResourceBundle(List<Encounter> encounters) {


        Collection<String> encounterUUIDS = new ArrayList<>();
        Collection<IBaseResource> iBaseResources = new ArrayList<>();
        TokenAndListParam encounterReference = new TokenAndListParam();

        for (Encounter encounter : encounters) {
            encounterUUIDS.add(encounter.getUuid());
        }


        if (encounterUUIDS.size() > 0) {
            iBaseResources.addAll(getApplicationContext().getBean(FhirEncounterService.class).get(encounterUUIDS));
        }


        return iBaseResources;
    }

    public Collection<IBaseResource> getObservationResourceBundle(SyncFhirProfile syncFhirProfile, List<Encounter> encounterList, List<Person> personList) {


        List<Concept> conceptQuestionList = new ArrayList<>();

        DateRangeParam lastUpdated = new DateRangeParam();
        Date lastSyncDate = null;

        if (syncFhirProfile != null) {
            ObjectNode searchParams = null;
            try {
                searchParams = getSearchParametersInJsonObject("Observation", syncFhirProfile.getResourceSearchParameter());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            ArrayNode codes = (ArrayNode) searchParams.get("code");

            lastSyncDate = getLastSyncDate(syncFhirProfile, "Observation");
            for (Object conceptUID : codes) {
                try {

                    Concept concept = Context.getConceptService().getConcept(conceptUID.toString());
                    if (concept != null) {
                        conceptQuestionList.add(concept);
                    }

                } catch (Exception e) {
                    log.error("Error while adding concept with uuid " + conceptUID, e);
                }

            }
            if (syncFhirProfile.getIsCaseBasedProfile()) {
                if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
                } else if (syncFhirCase != null) {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
                }
            } else {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "Observation"));

            }
        }

        List<Obs> observationList = Context.getObsService().getObservations(personList, encounterList, conceptQuestionList, null, null, null, null, null, null, lastSyncDate, new Date(), false);

        Collection<String> obsListUUID = observationList.stream().map(Obs::getUuid).collect(Collectors.toCollection(ArrayList::new));

        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        if (obsListUUID.size() > 0) {
            iBaseResources.addAll(getApplicationContext().getBean(FhirObservationService.class).get(obsListUUID));
        }
        return iBaseResources;

    }


    public Collection<IBaseResource> getServiceRequestResourceBundle(List<Order> testOrders) {


        Collection<String> testOrdersUUIDS = new ArrayList<>();
        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        for (Order testOrder : testOrders) {
            testOrdersUUIDS.add(testOrder.getUuid());
        }


        if (testOrdersUUIDS.size() > 0) {
            iBaseResources.addAll(getApplicationContext().getBean(FhirServiceRequestService.class).get(testOrdersUUIDS));
        }


        return iBaseResources;
    }

    private Collection<IBaseResource> getConditionResourceBundle(SyncFhirCase syncFhirCase, SyncFhirProfile syncFhirProfile) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode codes = objectMapper.createArrayNode();
        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        TokenAndListParam codeReference = new TokenAndListParam();
        ConditionSearchParams conditionSearchParams = new ConditionSearchParams();

        DateRangeParam lastUpdated = null;
        if (syncFhirProfile != null) {
            JsonNode searchParams = null;
            try {
                searchParams = getSearchParametersInJsonObject("Condition", syncFhirProfile.getResourceSearchParameter());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (searchParams.has("code") && searchParams.get("code").isArray()) {
                codes = (ArrayNode) searchParams.get("code");
            }

            if (syncFhirProfile != null && syncFhirProfile.getIsCaseBasedProfile()) {


                if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
                } else {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
                }
            } else {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "Condition"));

            }

        }

        for (Object conceptUID : codes) {
            try {
                TokenParam paramConcept = new TokenParam(conceptUID.toString());
                codeReference.addAnd(paramConcept);
            } catch (Exception e) {
                log.error("Error while adding concept with uuid " + conceptUID, e);
            }

        }

        ReferenceAndListParam patientReference = new ReferenceAndListParam();
        if (syncFhirCase != null) {
            patientReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, syncFhirCase.getPatient().getPatientIdentifier().getIdentifier())));
        }

        conditionSearchParams.setCode(codeReference);
        conditionSearchParams.setLastUpdated(lastUpdated);
        conditionSearchParams.setPatientParam(patientReference);

        iBaseResources.addAll(getApplicationContext().getBean(FhirConditionService.class).searchConditions(conditionSearchParams).getResources(0, Integer.MAX_VALUE));

        return iBaseResources;
    }


    private Collection<IBaseResource> getAllergyResourceBundle(SyncFhirCase syncFhirCase, SyncFhirProfile syncFhirProfile) {
        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        FhirAllergyIntoleranceSearchParams fhirAllergyIntoleranceSearchParams = new FhirAllergyIntoleranceSearchParams();

        DateRangeParam lastUpdated;

        if (syncFhirProfile != null && syncFhirProfile.getIsCaseBasedProfile()) {
            if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
            } else {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
            }
        } else {
            lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "AllergyIntolerance"));

        }

        ReferenceAndListParam patientReference = new ReferenceAndListParam();
        if (syncFhirCase != null) {
            patientReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, syncFhirCase.getPatient().getPatientIdentifier().getIdentifier())));
        }

        fhirAllergyIntoleranceSearchParams.setLastUpdated(lastUpdated);
        fhirAllergyIntoleranceSearchParams.setPatientReference(patientReference);

        iBaseResources.addAll(getApplicationContext().getBean(FhirAllergyIntoleranceService.class).searchForAllergies(fhirAllergyIntoleranceSearchParams).getResources(0, Integer.MAX_VALUE));

        return iBaseResources;
    }

    private Collection<IBaseResource> getImmunizationResourceBundle(SyncFhirCase syncFhirCase, SyncFhirProfile syncFhirProfile) {
        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        ReferenceAndListParam param = new ReferenceAndListParam();
        if (syncFhirCase != null) {
            param.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, syncFhirCase.getPatient().getPatientIdentifier().getIdentifier())));
        }

        iBaseResources.addAll(getApplicationContext().getBean(FhirImmunizationService.class).searchImmunizations(param, null).getResources(0, Integer.MAX_VALUE));

        return iBaseResources;
    }


    private Collection<IBaseResource> getMedicationDispenseResourceBundle(SyncFhirCase syncFhirCase, SyncFhirProfile syncFhirProfile) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode codes = objectMapper.createArrayNode();
        JsonNode searchParams = null;

        try {
            searchParams = getSearchParametersInJsonObject("medicationDispense", syncFhirProfile.getResourceSearchParameter());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (searchParams.has("code") && searchParams.get("code").isArray()) {
            codes = (ArrayNode) searchParams.get("code");
        }

        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        MedicationDispenseSearchParams medicationDispenseSearchParams = new MedicationDispenseSearchParams();
        DateRangeParam lastUpdated;

        if (syncFhirProfile != null && syncFhirProfile.getIsCaseBasedProfile()) {
            if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
            } else {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
            }
        } else {
            lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "MedicationRequest"));

        }

        ReferenceAndListParam patientReference = new ReferenceAndListParam();
        if (syncFhirCase != null) {
            patientReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, syncFhirCase.getPatient().getPatientIdentifier().getIdentifier())));
        }


        medicationDispenseSearchParams.setLastUpdated(lastUpdated);
        medicationDispenseSearchParams.setPatient(patientReference);

        iBaseResources.addAll(getApplicationContext().getBean(FhirMedicationDispenseService.class).searchMedicationDispenses(medicationDispenseSearchParams).getResources(0, Integer.MAX_VALUE));

        return iBaseResources;
    }

    private Collection<IBaseResource> getMedicationRequestResourceBundle(SyncFhirCase syncFhirCase, SyncFhirProfile syncFhirProfile) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode searchParams =objectMapper.createObjectNode();


        Collection < IBaseResource > iBaseResources = new ArrayList<>();

        MedicationRequestSearchParams medicationRequestSearchParams = new MedicationRequestSearchParams();
        TokenAndListParam codeReference = new TokenAndListParam();

        DateRangeParam lastUpdated = null;

        if (syncFhirProfile != null) {
            try {
                searchParams=getSearchParametersInJsonObject("medicationRequest", syncFhirProfile.getResourceSearchParameter());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if (syncFhirProfile.getIsCaseBasedProfile()) {
                if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
                } else {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
                }
            } else {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "MedicationRequest"));

            }

        }
        if (!searchParams.isEmpty() && searchParams.has("code")) {
            ArrayNode codes = (ArrayNode) searchParams.get("code");
            for (Object conceptUID : codes) {
                try {
                    TokenParam paramConcept = new TokenParam(conceptUID.toString());
                    codeReference.addAnd(paramConcept);
                } catch (Exception e) {
                    log.error("Error while adding concept with uuid " + conceptUID, e);
                }

            }
        }

        ReferenceAndListParam patientReference = new ReferenceAndListParam();
        if (syncFhirCase != null) {
            patientReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, syncFhirCase.getPatient().getPatientIdentifier().getIdentifier())));
        }


        medicationRequestSearchParams.setLastUpdated(lastUpdated);
        medicationRequestSearchParams.setCode(codeReference);
        medicationRequestSearchParams.setPatientReference(patientReference);

        iBaseResources.addAll(getApplicationContext().getBean(FhirMedicationRequestService.class).searchForMedicationRequests(medicationRequestSearchParams).getResources(0, Integer.MAX_VALUE));

        return iBaseResources;
    }

    private Collection<IBaseResource> getDiagnosticReportResourceBundle(SyncFhirCase syncFhirCase, SyncFhirProfile syncFhirProfile) {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode searchParams =objectMapper.createObjectNode();



        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        DiagnosticReportSearchParams diagnosticReportSearchParams = new DiagnosticReportSearchParams();
        TokenAndListParam codeReference = new TokenAndListParam();

        DateRangeParam lastUpdated = null;

        if (syncFhirProfile != null) {
            try {
                searchParams= getSearchParametersInJsonObject("diagnosticReport", syncFhirProfile.getResourceSearchParameter());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            if (syncFhirProfile.getIsCaseBasedProfile()) {
                if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
                } else {
                    lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
                }
            } else {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "MedicationRequest"));

            }
        }

        if (!searchParams.isEmpty() && searchParams.has("code")) {
            ArrayNode codes = (ArrayNode) searchParams.get("code");
            for (Object conceptUID : codes) {
                try {
                    TokenParam paramConcept = new TokenParam(conceptUID.toString());
                    codeReference.addAnd(paramConcept);
                } catch (Exception e) {
                    log.error("Error while adding concept with uuid " + conceptUID, e);
                }

            }
        }

        ReferenceAndListParam patientReference = new ReferenceAndListParam();
        if (syncFhirCase != null) {
            patientReference.addValue(new ReferenceOrListParam().add(new ReferenceParam(SP_IDENTIFIER, syncFhirCase.getPatient().getPatientIdentifier().getIdentifier())));
        }


        diagnosticReportSearchParams.setLastUpdated(lastUpdated);
        diagnosticReportSearchParams.setCode(codeReference);
        diagnosticReportSearchParams.setPatientReference(patientReference);

        iBaseResources.addAll(getApplicationContext().getBean(FhirDiagnosticReportService.class).searchForDiagnosticReports(diagnosticReportSearchParams).getResources(0, Integer.MAX_VALUE));

        return iBaseResources;
    }

    private Collection<IBaseResource> getEpisodeOfCareResourceBundle(List<PatientProgram> patientPrograms) {
        this.patientPrograms = patientPrograms;
        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        Collection<String> patientProgramUUIDs = patientPrograms.stream().map(PatientProgram::getUuid).collect(Collectors.toCollection(ArrayList::new));

        iBaseResources.addAll(getApplicationContext().getBean(FhirEpisodeOfCareService.class).get(patientProgramUUIDs));
        return iBaseResources;
    }


    private Date getLastSyncDate(SyncFhirProfile syncFhirProfile, String resourceType) {
        Date date;

        SyncFhirProfileLog syncFhirProfileLog = Context.getService(UgandaEMRSyncService.class).getLatestSyncFhirProfileLogByProfileAndResourceName(syncFhirProfile, resourceType);

        if (syncFhirProfileLog != null) {
            date = syncFhirProfileLog.getLastGenerationDate();
        } else if (syncFhirProfile.getDataToSyncStartDate() != null) {
            date = syncFhirProfile.getDataToSyncStartDate();
        } else {
            date = getDefaultLastSyncDate();
        }
        return date;
    }

    private Date getDefaultLastSyncDate() {
        try {
            return new SimpleDateFormat("yyyy/MM/dd").parse("1989/01/01");
        } catch (ParseException e) {
            log.error(e);
        }
        return null;
    }


    private PatientIdentifier getPatientIdentifierByPatientAndType(Patient patient, PatientIdentifierType patientIdentifierType) {
        List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<>();
        List<Patient> patients = new ArrayList<>();
        patientIdentifierTypes.add(patientIdentifierType);
        patients.add(patient);
        Context.getPatientService().getPatientIdentifiers(null, patientIdentifierTypes, null, patients, false);
        return null;
    }

    public List<Map> sendFhirResourcesTo(SyncFhirProfile syncFhirProfile) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
        List<Map> maps = new ArrayList<>();
        List<SyncFhirResource> syncFhirResources = ugandaEMRSyncService.getUnSyncedFHirResources(syncFhirProfile);

        for (SyncFhirResource syncFhirResource : syncFhirResources) {
            Date date = new Date();
            try {
                boolean connectionStatus = ugandaEMRHttpURLConnection.isConnectionAvailable();

                if (connectionStatus) {
                    Map map = ugandaEMRHttpURLConnection.sendPostBy(syncFhirProfile.getUrl(), syncFhirProfile.getUrlUserName(), syncFhirProfile.getUrlPassword(), syncFhirProfile.getUrlToken(), syncFhirResource.getResource(), false);
                    if (map.get("responseCode").equals(SyncConstant.CONNECTION_SUCCESS_200) || map.get("responseCode").equals(SyncConstant.CONNECTION_SUCCESS_201)) {
                        maps.add(map);
                        syncFhirResource.setDateSynced(date);
                        syncFhirResource.setSynced(true);
                        syncFhirResource.setResource(null);
                        syncFhirResource.setStatusCode(Integer.parseInt(map.get("responseCode").toString()));
                        syncFhirResource.setStatusCodeDetail(map.get("responseMessage").toString());
                        syncFhirResource.setExpiryDate(UgandaEMRSyncUtil.addDaysToDate(date, syncFhirProfile.getDurationToKeepSyncedResources()));
                        if (syncFhirProfile.getUuid().equals(FSHR_SYNC_FHIR_PROFILE_UUID) || syncFhirProfile.getUuid().equals(CROSS_BORDER_CR_SYNC_FHIR_PROFILE_UUID)) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode results = objectMapper.readTree(map.get("result").toString());
                            ugandaEMRSyncService.updatePatientsFromFHIR(results, PATIENT_ID_TYPE_CROSS_BORDER_UUID, PATIENT_ID_TYPE_CROSS_BORDER_NAME);
                        }
                        ugandaEMRSyncService.saveFHIRResource(syncFhirResource);
                    } else {
                        syncFhirResource.setStatusCode(Integer.parseInt(map.get("responseCode").toString()));
                        syncFhirResource.setStatusCodeDetail(map.get("responseMessage").toString());
                        ugandaEMRSyncService.saveFHIRResource(syncFhirResource);
                    }
                } else {
                    log.info("Connection to internet was not Successful. Code: " + connectionStatus);
                }

            } catch (Exception e) {
                log.error("Failed to Sync Fhir Resource: " + syncFhirResource.getUuid(), e);
            }

        }

        return maps;
    }


    private String addReferencesMappingToObservation(String observation) {
        try {
            ConceptService conceptService = Context.getConceptService();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = (ObjectNode) mapper.readTree(observation);
            ObjectNode observationResource = (ObjectNode) root.get("resource");

            // Get concept UUID from code.coding[0].code
            String conceptUuid = observationResource.path("code").path("coding").get(0).path("code").asText();
            Concept concept = conceptService.getConceptByUuid(conceptUuid);

            // Get the 'coding' array
            ArrayNode questionCodings = (ArrayNode) observationResource.path("code").withArray("coding");

            // Add UgandaEMR coding
            ObjectNode ugandaEmrCoding = mapper.createObjectNode();
            ugandaEmrCoding.put("system", "UgandaEMR");
            ugandaEmrCoding.put("code", concept.getConceptId().toString());
            ugandaEmrCoding.put("display", concept.getName().getName());
            questionCodings.add(ugandaEmrCoding);

            // Add concept mappings
            for (ConceptMap conceptMap : concept.getConceptMappings()) {
                if (conceptMap.getConceptReferenceTerm() != null) {
                    ObjectNode coding = mapper.createObjectNode();
                    coding.put("system", conceptMap.getConceptReferenceTerm().getConceptSource().getName());
                    coding.put("code", conceptMap.getConceptReferenceTerm().getCode());
                    coding.put("display", conceptMap.getConceptReferenceTerm().getName());
                    questionCodings.add(coding);
                }
            }

            // Handle valueCodeableConcept for coded data types
            if (concept.getDatatype().getUuid().equals("8d4a48b6-c2cc-11de-8d13-0010c6dffd0f") &&
                    observationResource.has("valueCodeableConcept")) {

                ArrayNode valueCodings = (ArrayNode) observationResource.path("valueCodeableConcept").withArray("coding");
                String valueCodedConceptUuid = valueCodings.get(0).path("code").asText();
                Concept valueCodedConcept = conceptService.getConceptByUuid(valueCodedConceptUuid);

                ObjectNode emrValueCoding = mapper.createObjectNode();
                emrValueCoding.put("system", "UgandaEMR");
                emrValueCoding.put("code", valueCodedConcept.getConceptId().toString());
                emrValueCoding.put("display", valueCodedConcept.getName().getName());
                valueCodings.add(emrValueCoding);

                for (ConceptMap conceptMap : valueCodedConcept.getConceptMappings()) {
                    ObjectNode coding = mapper.createObjectNode();
                    coding.put("system", conceptMap.getConceptReferenceTerm().getConceptSource().getName());
                    coding.put("code", conceptMap.getConceptReferenceTerm().getCode());
                    coding.put("display", conceptMap.getConceptReferenceTerm().getName());
                    valueCodings.add(coding);
                }
            }

            return mapper.writeValueAsString(root);

        } catch (Exception e) {
            log.error("Failed to enrich observation with concept mappings", e);
            throw new RuntimeException("Error processing observation JSON", e);
        }
    }


    private String addEpisodeOfCareToEncounter(String encounter, Object episodeOfcare) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            ObjectNode encounterNode = (ObjectNode) mapper.readTree(encounter);
            JsonNode periodNode = encounterNode.path("period");

            if (!periodNode.has("start")) {
                throw new IllegalArgumentException("Encounter JSON missing 'period.start'");
            }

            Date encounterDate = new SimpleDateFormat("yyyy-MM-dd").parse(periodNode.get("start").asText());
            List<PatientProgram> patientPrograms = (List<PatientProgram>) episodeOfcare;

            for (PatientProgram patientProgram : patientPrograms) {
                Date enrolled = patientProgram.getDateEnrolled();
                Date completed = patientProgram.getDateCompleted();

                boolean inProgram = (encounterDate.equals(enrolled) || encounterDate.after(enrolled))
                        && (completed == null || encounterDate.before(completed) || encounterDate.equals(completed));

                if (inProgram) {
                    ObjectNode referenceNode = mapper.createObjectNode();
                    referenceNode.put("reference", "EpisodeOfCare/" + patientProgram.getUuid());
                    encounterNode.set("episodeOfCare", referenceNode);
                    break; // Add only the first matching episode
                }
            }

            return mapper.writeValueAsString(encounterNode);
        } catch (Exception e) {
            log.error("Failed to add episodeOfCare to encounter", e);
            throw new RuntimeException("Error processing encounter JSON", e);
        }
    }


    private PatientIdentifier getPatientIdentifierByType(Patient patient, PatientIdentifierType patientIdentifierType) {
        for (PatientIdentifier patientIdentifier : patient.getActiveIdentifiers()) {
            if (patientIdentifier.getIdentifierType().equals(patientIdentifierType)) {
                return patientIdentifier;
            }

        }
        return null;
    }

    public void CollectTestOrdersFromSyncFHIRResource(SyncFhirProfile syncFhirProfile) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        List<SyncFhirResource> syncFhirResources = ugandaEMRSyncService.getSyncedFHirResources(syncFhirProfile);
        List<Order> orders = new ArrayList<>();
        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID("f947128e-93d7-46d5-aa32-645e38a125fe");
        for (SyncFhirResource syncFhirResource : syncFhirResources) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonObject;
            try {
                jsonObject = (ObjectNode) objectMapper.readTree(syncFhirResource.getResource());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


            ArrayNode jsonArray = (ArrayNode) jsonObject.get("entry");

            for (JsonNode jsonObject1 : jsonArray) {

                if (jsonObject1.get("resource").get("resourceType").equals("ServiceRequest")) {
                    Order order = Context.getOrderService().getOrderByUuid(jsonObject1.get("resource").get("id").asText());

                    if (!order.isActive() || !ugandaEMRSyncService.getSyncTaskBySyncTaskId(order.getOrderNumber()).equals(null)) {
                        continue;
                    }

                    SyncTask newSyncTask = new SyncTask();
                    newSyncTask.setDateSent(new Date());
                    newSyncTask.setCreator(Context.getUserService().getUser(1));
                    newSyncTask.setSentToUrl(syncTaskType.getUrl());
                    newSyncTask.setRequireAction(true);
                    newSyncTask.setActionCompleted(false);
                    newSyncTask.setSyncTask(order.getUuid());
                    newSyncTask.setStatusCode(200);
                    newSyncTask.setStatus("SUCCESS");
                    newSyncTask.setSyncTaskType(syncTaskType);
                    ugandaEMRSyncService.saveSyncTask(newSyncTask);
                }
            }


        }
    }

    private List<Patient> getPatientByCohortType(String cohortTypeUuid) {
        List list = Context.getAdministrationService().executeSQL("SELECT patient_id from cohort_member cm inner join cohort c on cm.cohort_id = c.cohort_id inner join cohort_type ct on c.cohort_type_id = ct.cohort_type_id where ct.uuid='" + cohortTypeUuid + "' and c.voided=0 and cm.voided=0;", true);
        PatientService patientService = Context.getPatientService();
        List<Patient> patientList = new ArrayList<>();

        if (list.size() > 0) {
            for (Object o : list) {
                patientList.add(patientService.getPatient(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString())));
            }
        }
        return patientList;
    }
}
