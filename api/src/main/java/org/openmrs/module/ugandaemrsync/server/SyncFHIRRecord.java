package org.openmrs.module.ugandaemrsync.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.json.JSONArray;
import org.json.JSONObject;

import org.openmrs.PatientProgram;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Patient;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Program;
import org.openmrs.Provider;
import org.openmrs.Person;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.OrderType;
import org.openmrs.Order;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.PatientState;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.fhir2.api.FhirPersonService;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.api.FhirObservationService;
import org.openmrs.module.fhir2.api.FhirEncounterService;
import org.openmrs.module.fhir2.api.FhirServiceRequestService;
import org.openmrs.module.ugandaemrsync.api.FhirEpisodeOfCareService;
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
import org.springframework.context.ApplicationContext;

import javax.validation.constraints.NotNull;
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

import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.*;
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
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIR_CODING_DATATYPE;

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
        if (payload.isEmpty()) {
            return "";
        }

        String organizationString = String.format("{\"reference\":\"Organization/%s\",\"type\":\"Organization\",\"identifier\":{\"use\":\"official\",\"value\":\"%s\",\"system\":\"https://hmis.health.go.ug/\"},\"display\":\"%s\"}", healthCenterIdentifier, healthCenterIdentifier, healthCenterName);
        JSONObject finalPayLoadJson = new JSONObject(payload);
        JSONObject organization = new JSONObject(organizationString);

        finalPayLoadJson.put(attributeName, organization);
        return finalPayLoadJson.toString();
    }

    public String addServiceType(String payload, String attributeName) {
        if (payload.isEmpty()) {
            return "";
        }
        JSONObject finalPayLoadJson = new JSONObject(payload);
        finalPayLoadJson.put(attributeName, new JSONObject("{\"coding\" : [{\"code\": \"dcd87b79-30ab-102d-86b0-7a5022ba4115\", \"display\": \"MEDICAL OUTPATIENT\"}],\"text\" : \"Out-Patient\"}"));
        return finalPayLoadJson.toString();
    }

    /**
     * Adds location to encounter Resource
     *
     * @param payload
     * @return
     */
    public String addLocationToEncounterResource(String payload) {
        if (payload.isEmpty()) {
            return "";
        }
        JSONObject finalPayLoadJson = new JSONObject(payload);


        return finalPayLoadJson.toString();
    }


    public Collection<String> proccessBuldeFHIRResources(String resourceType, String lastUpdateOnDate) {

        String finalQuery;

        StringBuilder currentBundleString = new StringBuilder();
        Integer currentNumberOfBundlesCollected = 0;
        Integer interval = 1000;
        List<String> resourceBundles = new ArrayList<>();

        DateRangeParam lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBound(lastUpdateOnDate);
        IParser iParser = FhirContext.forR4().newJsonParser();
        Collection<IBaseResource> results = null;
        List<String> jsoStrings = new ArrayList<>();

        String bundleWrapperString = "{\"resourceType\":\"Bundle\",\"type\":\"transaction\",\"entry\":%s}";

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


        if (resourceType == "Patient") {
            results = fhirPatientService.searchForPatients(null, null, null, null, null, null, null, null, null,
                    null, null, null, null, lastUpdated, null, null).getResources(0, Integer.MAX_VALUE);
        } else if (resourceType.equals("Person")) {
            results = fhirPersonService.searchForPeople(null, null, null, null,
                    null, null, null, null, lastUpdated, null, null).getResources(0, Integer.MAX_VALUE);
        } else if (resourceType.equals("Encounter")) {
            results = fhirEncounterService.searchForEncounters(null, null, null, null, null, lastUpdated, null, null).getResources(0, Integer.MAX_VALUE);
        } else if (resourceType.equals("Observation")) {
            results = fhirObservationService.searchForObservations(null,
                    null, null, null,
                    null, null, null,
                    null, null, null, null, lastUpdated, null, null, null).getResources(0, Integer.MAX_VALUE);
        } else if (resourceType.equals("Practitioner")) {
            results = fhirPractitionerService.searchForPractitioners(null, null, null, null, null,
                    null, null, null, null, lastUpdated, null).getResources(0, Integer.MAX_VALUE);
        }

        return groupInBundles(resourceType, results, interval, null);
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


    public Collection<SyncFhirResource> generateCaseBasedFHIRResourceBundles(SyncFhirProfile syncFhirProfile) {

        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        if (syncFhirProfile != null && (!syncFhirProfile.getCaseBasedProfile() || syncFhirProfile.getCaseBasedPrimaryResourceType() == null)) {
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

        List<org.openmrs.PatientProgram> patientProgramList;

        if (syncFhirProfile.getCaseBasedPrimaryResourceType().equals("EpisodeOfCare")) {
            Collection<Program> programs = new ArrayList<>();
            Program program = Context.getProgramWorkflowService().getProgramByUuid(syncFhirProfile.getCaseBasedPrimaryResourceTypeId());
            patientProgramList = Context.getProgramWorkflowService().getPatientPrograms(null, program, null, null, null, null, false);


            for (org.openmrs.PatientProgram patientProgram : patientProgramList) {
                org.openmrs.Patient patient = patientProgram.getPatient();
                if (!patient.getVoided()) {
                    String caseIdentifier = patientProgram.getUuid();
                    saveSyncFHIRCase(syncFhirProfile, currentDate, patient, caseIdentifier);
                }
            }
        } else if (syncFhirProfile.getCaseBasedPrimaryResourceType().equals("Encounter")) {
            List<org.openmrs.EncounterType> encounterTypes = new ArrayList<>();

            encounterTypes.add(Context.getEncounterService().getEncounterTypeByUuid(syncFhirProfile.getCaseBasedPrimaryResourceTypeId()));

            EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteria(null, null, null, null, null, null, encounterTypes, null, null, null, false);

            Collection<Patient> patientArrayList = Context.getEncounterService().getEncounters(encounterSearchCriteria).stream().map(Encounter::getPatient).collect(Collectors.toCollection(ArrayList::new));

            for (org.openmrs.Patient patient : patientArrayList) {

                PatientIdentifier patientIdentifier = getPatientIdentifierByType(patient, syncFhirProfile.getPatientIdentifierType());

                if (patientIdentifier != null) {
                    saveSyncFHIRCase(syncFhirProfile, currentDate, patient, patientIdentifier.getIdentifier());
                }

            }
        } else if (syncFhirProfile.getCaseBasedPrimaryResourceType().equals("ProgramWorkFlowState")) {
            ProgramWorkflowService programWorkflowService = Context.getProgramWorkflowService();

            ProgramWorkflowState programWorkflowState = programWorkflowService.getStateByUuid(syncFhirProfile.getCaseBasedPrimaryResourceTypeId());

            List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(null, programWorkflowState.getProgramWorkflow().getProgram(), null, null, null, null, false);

            for (PatientProgram patientProgram : patientPrograms) {
                PatientState patientState = patientProgram.getCurrentState(programWorkflowState.getProgramWorkflow());
                if (patientState != null && patientState.getState().equals(programWorkflowState)) {
                    org.openmrs.Patient patient = patientProgram.getPatient();
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

                List list = Context.getAdministrationService().executeSQL("select Distinct patient_id from  orders where order_type_id = " + orderType.getId() + "  AND date_activated >= " + formattedDate + " and date_stopped is NULL ;", true);
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
        }
    }

    public SyncFhirCase saveSyncFHIRCase(SyncFhirProfile syncFhirProfile, Date currentDate, org.openmrs.Patient patient, String caseIdentifier) {
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
        String resource = generateFHIRCaseResource(syncFhirProfile, syncFhirCase);
        if (resource != null && !resource.equals("")) {
            SyncFhirResource syncFHIRResource = new SyncFhirResource();
            syncFHIRResource.setGeneratorProfile(syncFhirProfile);
            syncFHIRResource.setResource(resource);
            syncFHIRResource.setSynced(false);
            ugandaEMRSyncService.saveFHIRResource(syncFHIRResource);
            syncFhirCase.setLastUpdateDate(syncFHIRResource.getDateCreated());
            return syncFHIRResource;
        } else {
            return null;
        }
    }


    private String generateFHIRCaseResource(SyncFhirProfile syncFhirProfile, SyncFhirCase syncFHIRCase) {

        Collection<String> resources = new ArrayList<>();
        List<org.openmrs.Encounter> encounters = new ArrayList<>();
        Date currentDate = new Date();
        Date lastUpdateDate;
        List<Order> orderList = new ArrayList<>();

        if (syncFHIRCase.getLastUpdateDate() == null) {
            lastUpdateDate = getDefaultLastSyncDate();
        } else {
            lastUpdateDate = syncFHIRCase.getLastUpdateDate();
        }


        String[] resourceTypes = syncFhirProfile.getResourceTypes().split(",");

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
                    resources.addAll(groupInCaseBundle("Person", getPersonResourceBundle(syncFhirProfile, personList, syncFHIRCase), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "EpisodeOfCare":
                    JSONArray jsonArray = getSearchParametersInJsonObject("EpisodeOfCare", syncFhirProfile.getResourceSearchParameter()).getJSONArray("type");

                    List<PatientProgram> patientProgramList = new ArrayList<>();

                    for (Object jsonObject : jsonArray) {
                        patientProgramList = Context.getProgramWorkflowService().getPatientPrograms(syncFHIRCase.getPatient(), Context.getProgramWorkflowService().getProgramByUuid(jsonObject.toString()), lastUpdateDate, currentDate, null, null, false);
                    }

                    if (patientProgramList.size() > 0) {
                        resources.addAll(groupInCaseBundle("EpisodeOfCare", getEpisodeOfCareResourceBundle(patientProgramList), syncFhirProfile.getPatientIdentifierType().getName()));
                    }
                    anyOtherObject.put("episodeOfCare", patientProgramList);
                    break;
                case "Encounter":
                    List<org.openmrs.EncounterType> encounterTypes = new ArrayList<>();
                    DateRangeParam encounterLastUpdated = new DateRangeParam().setUpperBoundInclusive(currentDate).setLowerBoundInclusive(lastUpdateDate);
                    JSONArray encounterUUIDS = getSearchParametersInJsonObject("Encounter", syncFhirProfile.getResourceSearchParameter()).getJSONArray("type");

                    for (Object jsonObject : encounterUUIDS) {
                        encounterTypes.add(Context.getEncounterService().getEncounterTypeByUuid(jsonObject.toString()));
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
                    OrderService orderService = Context.getOrderService();
                    List<Order> testOrders = orderService.getActiveOrders(syncFHIRCase.getPatient(), orderService.getOrderTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID), null, null).stream().filter(testOrder -> testOrder.getDateActivated().compareTo(lastUpdateDate) >= 0).collect(Collectors.toList());
                    resources.addAll(groupInCaseBundle("ServiceRequest", getServiceRequestResourceBundle(testOrders), syncFhirProfile.getPatientIdentifierType().getName()));
                    break;
                case "Practitioner":
                    List<Provider> providerList = new ArrayList<>();
                    for (Order order : orderList) {
                        providerList.add(order.getOrderer());
                    }

                    for (Encounter encounter : encounters) {
                        providerList.add(getProviderFromEncounter(encounter));
                    }

                    if (providerList.size() > 0) {
                        resources.addAll(groupInCaseBundle("Practitioner", getPractitionerResourceBundle(syncFhirProfile, encounters), syncFhirProfile.getPatientIdentifierType().getName()));
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


    public Collection<String> generateFHIRResourceBundles(SyncFhirProfile syncFhirProfile) {
        Collection<String> stringCollection = new ArrayList<>();
        List<org.openmrs.Encounter> encounters = new ArrayList<>();

        this.syncFhirProfile = syncFhirProfile;

        Date currentDate = new Date();

        String[] resourceTypes = syncFhirProfile.getResourceTypes().split(",");
        for (String resource : resourceTypes) {
            switch (resource) {
                case "Encounter":
                    List<org.openmrs.EncounterType> encounterTypes = new ArrayList<>();
                    DateRangeParam encounterLastUpdated = new DateRangeParam().setUpperBoundInclusive(currentDate).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "Encounter"));
                    JSONArray jsonArray = getSearchParametersInJsonObject("Encounter", syncFhirProfile.getResourceSearchParameter()).getJSONArray("type");

                    for (Object jsonObject : jsonArray) {
                        encounterTypes.add(Context.getEncounterService().getEncounterTypeByUuid(jsonObject.toString()));
                    }

                    EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteria(null, null, encounterLastUpdated.getLowerBoundAsInstant(), encounterLastUpdated.getUpperBoundAsInstant(), null, null, encounterTypes, null, null, null, false);
                    encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);

                    saveSyncFHIRResources(groupInBundles("Encounter", getEncounterResourceBundle(encounters), syncFhirProfile.getNumberOfResourcesInBundle(), null), resource, syncFhirProfile, currentDate);

                    break;
                case "Observation":
                    if (encounters.size() > 0) {
                        saveSyncFHIRResources(groupInBundles("Observation", getObservationResourceBundle(syncFhirProfile, encounters, getPersonsFromEncounterList(encounters)), syncFhirProfile.getNumberOfResourcesInBundle(), null), "Observation", syncFhirProfile, currentDate);
                    } else {
                        saveSyncFHIRResources(groupInBundles("Observation", getObservationResourceBundle(syncFhirProfile, null, null), syncFhirProfile.getNumberOfResourcesInBundle(), null), "Observation", syncFhirProfile, currentDate);
                    }
                    break;
                case "Patient":
                    if (encounters.size() > 0) {
                        saveSyncFHIRResources(groupInBundles("Patient", getPatientResourceBundle(syncFhirProfile, getPatientIdentifierFromEncounter(encounters, syncFhirProfile.getPatientIdentifierType()), null), syncFhirProfile.getNumberOfResourcesInBundle(), syncFhirProfile.getPatientIdentifierType().getName()), "Patient", syncFhirProfile, currentDate);
                    } else {
                        saveSyncFHIRResources(groupInBundles("Patient", getPatientResourceBundle(syncFhirProfile, null, null), syncFhirProfile.getNumberOfResourcesInBundle(), syncFhirProfile.getPatientIdentifierType().getName()), "Patient", syncFhirProfile, currentDate);
                    }
                    break;
                case "Practitioner":
                    if (encounters.size() > 0) {
                        saveSyncFHIRResources(groupInBundles("Practitioner", getPractitionerResourceBundle(syncFhirProfile, encounters), syncFhirProfile.getNumberOfResourcesInBundle(), null), "Practitioner", syncFhirProfile, currentDate);
                    } else {
                        saveSyncFHIRResources(groupInBundles("Practitioner", getPractitionerResourceBundle(syncFhirProfile, null), syncFhirProfile.getNumberOfResourcesInBundle(), null), "Practitioner", syncFhirProfile, currentDate);
                    }
                    break;
                case "Person":
                    if (encounters.size() > 0) {
                        saveSyncFHIRResources(groupInBundles("Person", getPersonResourceBundle(syncFhirProfile, getPersonsFromEncounterList(encounters), null), syncFhirProfile.getNumberOfResourcesInBundle(), null), "Person", syncFhirProfile, currentDate);
                    } else {
                        saveSyncFHIRResources(groupInBundles("Person", getPersonResourceBundle(syncFhirProfile, null, null), syncFhirProfile.getNumberOfResourcesInBundle(), null), "Person", syncFhirProfile, currentDate);
                    }
                    break;
            }
        }

        return stringCollection;
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


    private List<org.openmrs.PatientIdentifier> getPatientIdentifierFromEncounter(List<org.openmrs.Encounter> encounters, org.openmrs.PatientIdentifierType patientIdentifierType) {
        List<org.openmrs.PatientIdentifier> patientIdentifiers = new ArrayList<>();
        for (org.openmrs.Encounter encounter : encounters) {
            org.openmrs.PatientIdentifier patientIdentifier = encounter.getPatient().getPatientIdentifier(patientIdentifierType);
            if (patientIdentifier != null) {
                patientIdentifiers.add(patientIdentifier);
            }
        }
        return patientIdentifiers;
    }

    private List<org.openmrs.Person> getPersonFromEncounter(List<org.openmrs.Encounter> encounters) {
        List<Person> personList = new ArrayList<>();
        for (org.openmrs.Encounter encounter : encounters) {
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

    private Collection<String> groupInCaseBundle(String resourceType, Collection<IBaseResource> iBaseResources, String identifierTypeName) {

        Collection<String> resourceBundles = new ArrayList<>();

        for (IBaseResource iBaseResource : iBaseResources) {

            String jsonString = encodeResourceToString(resourceType, identifierTypeName, iBaseResource);

            resourceBundles.add(jsonString);
        }


        return resourceBundles;
    }

    private String encodeResourceToString(String resourceType, String identifierTypeName, IBaseResource iBaseResource) {
        IParser iParser = FhirContext.forR4().newJsonParser();

        String jsonString = "";
        try {
            jsonString = iParser.encodeResourceToString(iBaseResource);

            if (resourceType.equals("Patient") || resourceType.equals("Practitioner")) {
                jsonString = addOrganizationToRecord(jsonString, "managingOrganization");
                jsonString = addCodingToIdentifier(jsonString, "identifier");
                jsonString = addCodingToSystemToPrimaryIdentifier(jsonString, "identifier");
                jsonString = addUseOfficialToName(jsonString, "name");
                jsonString = jsonString.replace("address5", "village").replace("address4", "parish").replace("address3", "subcounty").replace("state", "city");
            }

            if (resourceType.equals("Patient") || resourceType.equals("Practitioner") || resourceType.equals("Person")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                String resourceIdentifier = "";
                resourceIdentifier = jsonObject.get("id").toString();
                jsonString = wrapResourceInPUTRequest(jsonString, resourceType, resourceIdentifier);
            } else if (resourceType.equals("Encounter")) {
                jsonString = addOrganizationToRecord(jsonString, "serviceProvider");
                jsonString = addServiceType(jsonString, "serviceType");
                if (anyOtherObject.get("episodeOfCare") != null) {
                    jsonString = addEpisodeOfCareToEncounter(jsonString, anyOtherObject.get("episodeOfCare"));
                }
                jsonString = wrapResourceInPostRequest(jsonString);

            } else if (resourceType.equals("Observation")) {
                jsonString = addReferencesMappingToObservation(wrapResourceInPostRequest(jsonString));
            } else {
                jsonString = wrapResourceInPostRequest(jsonString);
            }
        } catch (Exception e) {
            log.error(e);
        }
        return jsonString;
    }

    private String addUseOfficialToName(String payload, String attributeName) {
        JSONObject jsonObject = new JSONObject(payload);
        int objectCount = 0;
        for (Object jsonObject1 : jsonObject.getJSONArray(attributeName)) {
            jsonObject.getJSONArray(attributeName).getJSONObject(objectCount).put("use", "official");
            objectCount++;
        }
        return jsonObject.toString();
    }

    public String addCodingToIdentifier(String payload, String attributeName) {
        JSONObject jsonObject = new JSONObject(payload);
        int identifierCount = 0;
        for (Object jsonObject1 : jsonObject.getJSONArray(attributeName)) {
            JSONObject jsonObject2 = new JSONObject(jsonObject1.toString());
            PatientIdentifier patientIdentifier = Context.getPatientService().getPatientIdentifierByUuid(jsonObject2.get("id").toString());
            if (patientIdentifier.getPatient().getBirthdateEstimated()) {
                jsonObject.put("birthDate", patientIdentifier.getPatient().getBirthdate().toString().replace(" 00:00:00.0", ""));
            }
            jsonObject.getJSONArray(attributeName).getJSONObject(identifierCount).getJSONObject("type").put("coding", new JSONArray().put(new JSONObject().put("system", "UgandaEMR").put("code", patientIdentifier.getIdentifierType().getUuid())));
            identifierCount++;
        }
        return jsonObject.toString();
    }

    public String addCodingToSystemToPrimaryIdentifier(String payload, String attributeName) {
        JSONObject jsonObject = new JSONObject(payload);
        int identifierCount = 0;
        for (Object jsonObject1 : jsonObject.getJSONArray(attributeName)) {
            JSONObject jsonObject2 = new JSONObject(jsonObject1.toString());
            if (Context.getPatientService().getPatientIdentifierByUuid(jsonObject2.get("id").toString()).getIdentifierType().getUuid().equals(SyncConstant.OPENMRS_IDENTIFIER_TYPE_UUID)) {
                jsonObject.getJSONArray(attributeName).getJSONObject(identifierCount).put("system", "http://openclientregistry.org/fhir/sourceid");
            }

            identifierCount++;
        }
        return jsonObject.toString();

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

    private JSONObject getSearchParametersInJsonObject(String resourceType, String searchParameterString) {
        JSONObject jsonObject = new JSONObject(searchParameterString);
        if (jsonObject.isNull(resourceType)) {
            jsonObject = jsonObject.getJSONObject(resourceType.toLowerCase() + "Filter");
        }
        return jsonObject;
    }

    public String addSearchParameter(String resourceType, String searchParam, String searchParamString) {


        return searchParamString;
    }

    private Provider getProviderFromEncounter(org.openmrs.Encounter encounter) {
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

    private List<org.openmrs.Person> getPersonsFromEncounterList(List<org.openmrs.Encounter> encounters) {
        EncounterRole encounterClinicianRole = Context.getEncounterService().getEncounterRoleByUuid(ENCOUNTER_ROLE);
        List<org.openmrs.Person> person = new ArrayList<>();

        for (org.openmrs.Encounter encounter : encounters) {
            person.add(encounter.getPatient().getPerson());
            for (org.openmrs.Provider provider : encounter.getProvidersByRole(encounterClinicianRole)) {
                person.add(provider.getPerson());
            }
        }
        return person;
    }


    private Collection<IBaseResource> getPatientResourceBundle(SyncFhirProfile syncFhirProfile, List<PatientIdentifier> patientIdentifiers, SyncFhirCase syncFhirCase) {

        DateRangeParam lastUpdated = new DateRangeParam();

        if (syncFhirProfile.getCaseBasedProfile()) {
            if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
            } else if (syncFhirCase != null) {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
            }
        } else {
            lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "Patient"));

        }


        TokenAndListParam patientReference = new TokenAndListParam();
        if (patientIdentifiers != null) {
            for (org.openmrs.PatientIdentifier patientIdentifier : patientIdentifiers) {
                patientReference.addAnd(new TokenParam(patientIdentifier.getIdentifier()));
            }
        }


        return getApplicationContext().getBean(FhirPatientService.class).searchForPatients(null, null, null, patientReference, null, null, null, null, null,
                null, null, null, null, lastUpdated, null, null).getResources(0, Integer.MAX_VALUE);
    }

    private Collection<IBaseResource> getPractitionerResourceBundle(SyncFhirProfile syncFhirProfile, List<org.openmrs.Encounter> encounterList) {

        Collection<String> providerUUIDs = new ArrayList<>();
        for (org.openmrs.Encounter encounter : encounterList) {
            if (getProviderFromEncounter(encounter).getDateChanged().after(getLastSyncDate(syncFhirProfile, "Practitioner"))) {
                providerUUIDs.add(getProviderFromEncounter(encounter).getUuid());
            }
        }

        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        if (providerUUIDs.size() == 0 && !syncFhirProfile.getCaseBasedProfile()) {
            DateRangeParam lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "Practitioner"));

            iBaseResources = getApplicationContext().getBean(FhirPractitionerService.class).searchForPractitioners(null, null, null, null, null,
                    null, null, null, null, lastUpdated, null).getResources(0, Integer.MAX_VALUE);
        } else {
            iBaseResources.addAll(getApplicationContext().getBean(FhirPractitionerService.class).get(providerUUIDs));
        }


        return iBaseResources;

    }

    private Collection<IBaseResource> getPersonResourceBundle(SyncFhirProfile syncFhirProfile, List<org.openmrs.Person> personList, SyncFhirCase syncFhirCase) {


        DateRangeParam lastUpdated = new DateRangeParam();

        if (syncFhirProfile.getCaseBasedProfile()) {
            if (syncFhirCase != null && syncFhirCase.getLastUpdateDate() != null) {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(syncFhirCase.getLastUpdateDate());
            } else {
                lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getDefaultLastSyncDate());
            }
        } else {
            lastUpdated = new DateRangeParam().setUpperBoundInclusive(new Date()).setLowerBoundInclusive(getLastSyncDate(syncFhirProfile, "Patient"));

        }

        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        if (personList.size() > 0) {
            Collection<String> personListUUID = personList.stream().map(org.openmrs.Person::getUuid).collect(Collectors.toCollection(ArrayList::new));
            iBaseResources.addAll(getApplicationContext().getBean(FhirPersonService.class).get(personListUUID));

        } else if (!syncFhirProfile.getCaseBasedProfile()) {
            iBaseResources = getApplicationContext().getBean(FhirPersonService.class).searchForPeople(null, null, null, null,
                    null, null, null, null, lastUpdated, null, null).getResources(0, Integer.MAX_VALUE);
        }

        return iBaseResources;
    }


    private Collection<IBaseResource> getEncounterResourceBundle(List<org.openmrs.Encounter> encounters) {


        Collection<String> encounterUUIDS = new ArrayList<>();
        Collection<IBaseResource> iBaseResources = new ArrayList<>();
        TokenAndListParam encounterReference = new TokenAndListParam();

        for (org.openmrs.Encounter encounter : encounters) {
            encounterUUIDS.add(encounter.getUuid());
        }


        if (encounterUUIDS.size() > 0) {
            iBaseResources.addAll(getApplicationContext().getBean(FhirEncounterService.class).get(encounterUUIDS));
        }


        return iBaseResources;
    }

    private Collection<IBaseResource> getObservationResourceBundle(SyncFhirProfile syncFhirProfile, List<org.openmrs.Encounter> encounterList, List<Person> personList) {

        JSONObject searchParams = getSearchParametersInJsonObject("Observation", syncFhirProfile.getResourceSearchParameter());

        JSONArray codes = searchParams.getJSONArray("code");

        List<Concept> conceptQuestionList = new ArrayList<>();

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

        List<Obs> observationList = Context.getObsService().getObservations(personList, encounterList, conceptQuestionList, null, null, null, null, null, null, getLastSyncDate(syncFhirProfile, "Observation"), new Date(), false);

        Collection<String> obsListUUID = observationList.stream().map(Obs::getUuid).collect(Collectors.toCollection(ArrayList::new));


        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        if (obsListUUID.size() > 0) {
            iBaseResources.addAll(getApplicationContext().getBean(FhirObservationService.class).get(obsListUUID));
        }


        return iBaseResources;

    }


    private Collection<IBaseResource> getServiceRequestResourceBundle(List<org.openmrs.Order> testOrders) {

        Collection<String> testOrdersUUIDS = new ArrayList<>();
        Collection<IBaseResource> iBaseResources = new ArrayList<>();

        for (org.openmrs.Order testOrder : testOrders) {
            testOrdersUUIDS.add(testOrder.getUuid());
        }


        if (testOrdersUUIDS.size() > 0) {
            iBaseResources.addAll(getApplicationContext().getBean(FhirServiceRequestService.class).get(testOrdersUUIDS));
        }


        return iBaseResources;
    }

    private Collection<IBaseResource> getEpisodeOfCareResourceBundle(List<org.openmrs.PatientProgram> patientPrograms) {
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
                    if (map.get("responseCode").equals(SyncConstant.CONNECTION_SUCCESS_200)) {
                        maps.add(map);
                        syncFhirResource.setDateSynced(date);
                        syncFhirResource.setSynced(true);
                        syncFhirResource.setExpiryDate(UgandaEMRSyncUtil.addDaysToDate(date, syncFhirProfile.getDurationToKeepSyncedResources()));
                        if (syncFhirProfile.getUuid().equals(FSHR_SYNC_FHIR_PROFILE_UUID) || syncFhirProfile.getUuid().equals(CROSS_BORDER_CR_SYNC_FHIR_PROFILE_UUID)) {
                            ugandaEMRSyncService.updatePatientsFromFHIR(new JSONObject((String) map.get("result")),PATIENT_ID_TYPE_CROSS_BORDER_UUID,PATIENT_ID_TYPE_CROSS_BORDER_NAME);
                        }
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
        ConceptService conceptService = Context.getConceptService();
        JSONObject jsonObject = new JSONObject(observation);
        JSONObject observationResource = jsonObject.getJSONObject("resource");
        String conceptUUid = observationResource.getJSONObject("code").getJSONArray("coding").getJSONObject(0).getString("code");


        Concept concept = conceptService.getConceptByUuid(conceptUUid);

        JSONArray newQuestionJson = observationResource.getJSONObject("code").getJSONArray("coding");

        newQuestionJson.put(new JSONObject(String.format(FHIR_CODING_DATATYPE, "UgandaEMR", concept.getConceptId(), concept.getName().getName())));

        if (concept.getConceptMappings().size() > 0) {
            for (ConceptMap conceptQuestionMap : concept.getConceptMappings()) {
                newQuestionJson.put(new JSONObject(String.format(FHIR_CODING_DATATYPE, conceptQuestionMap.getConceptReferenceTerm().getConceptSource().getName(), conceptQuestionMap.getConceptReferenceTerm().getCode(), conceptQuestionMap.getConceptReferenceTerm().getName())));
            }
        }

        if (concept.getDatatype().equals(conceptService.getConceptDatatypeByUuid("8d4a48b6-c2cc-11de-8d13-0010c6dffd0f")) && !observationResource.isNull("valueCodeableConcept")) {
            JSONArray newValueCodeableJson = observationResource.getJSONObject("valueCodeableConcept").getJSONArray("coding");
            String valueCodedConceptUUid = observationResource.getJSONObject("valueCodeableConcept").getJSONArray("coding").getJSONObject(0).getString("code");
            Concept valueCodedConcept = conceptService.getConceptByUuid(valueCodedConceptUUid);
            newValueCodeableJson.put(new JSONObject(String.format(FHIR_CODING_DATATYPE, "UgandaEMR", valueCodedConcept.getConceptId(), valueCodedConcept.getName().getName())));
            for (ConceptMap conceptMap : valueCodedConcept.getConceptMappings()) {
                newValueCodeableJson.put(new JSONObject(String.format(FHIR_CODING_DATATYPE, conceptMap.getConceptReferenceTerm().getConceptSource().getName(), conceptMap.getConceptReferenceTerm().getCode(), conceptMap.getConceptReferenceTerm().getName())));
            }
        }

        return jsonObject.toString();
    }

    private String addEpisodeOfCareToEncounter(String encounter, Object episodeOfcare) {
        String episodeOfCareReference = "{\"reference\":\"EpisodeOfCare/%s\"}";

        JSONObject jsonObject = new JSONObject(encounter);

        Date encounterDate = null;
        try {
            encounterDate = new SimpleDateFormat("yyyy-MM-dd").parse(jsonObject.getJSONObject("period").getString("start"));
        } catch (ParseException e) {
            log.error(e);
        }

        List<PatientProgram> patientPrograms = (List<PatientProgram>) episodeOfcare;

        for (PatientProgram patientProgram : patientPrograms) {
            if ((encounterDate.equals(patientProgram.getDateEnrolled()) || encounterDate.after(patientProgram.getDateEnrolled())) && (patientProgram.getDateCompleted() == null || (patientProgram.getDateCompleted() != null && (encounterDate.equals(patientProgram.getDateCompleted()) || encounterDate.before(patientProgram.getDateCompleted()))))) {
                jsonObject.put("episodeOfCare", new JSONObject(String.format(episodeOfCareReference, patientProgram.getUuid())));
            }
        }


        return jsonObject.toString();
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
            JSONObject jsonObject = new JSONObject(syncFhirResource.getResource());

            JSONArray jsonArray = jsonObject.getJSONArray("entry");

            for (Object o : jsonArray) {
                JSONObject jsonObject1 = new JSONObject(o.toString());

                if (jsonObject1.getJSONObject("resource").get("resourceType").equals("ServiceRequest")) {
                    Order order = Context.getOrderService().getOrderByUuid(jsonObject1.getJSONObject("resource").getString("id"));

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
}
