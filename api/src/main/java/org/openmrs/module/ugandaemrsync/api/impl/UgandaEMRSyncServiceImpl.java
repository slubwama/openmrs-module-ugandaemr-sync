/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api.impl;

import ca.uhn.fhir.parser.StrictErrorHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.EncounterRole;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAddress;
import org.openmrs.Provider;
import org.openmrs.Person;
import org.openmrs.ProviderAttributeType;
import org.openmrs.ProviderAttribute;
import org.openmrs.ConceptMap;
import org.openmrs.TestOrder;
import org.openmrs.Location;
import org.openmrs.CareSetting;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.dto.*;
import org.openmrs.module.stockmanagement.api.model.*;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.api.dao.UgandaEMRSyncDao;
import org.openmrs.module.ugandaemrsync.mapper.Identifier;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfileLog;
import org.openmrs.module.ugandaemrsync.model.SyncFhirCase;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncFHIRRecord;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.util.UgandaEMRSyncUtil;
import org.openmrs.parameter.OrderSearchCriteria;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.context.ApplicationContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.SYNC_METRIC_DATA;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.PATIENT_ID_TYPE_UIC_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.PATIENT_ID_TYPE_UIC_NAME;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.PATIENT_ID_TYPE_NIN_NAME;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

public class UgandaEMRSyncServiceImpl extends BaseOpenmrsService implements UgandaEMRSyncService {

    UgandaEMRSyncDao dao;
    Log log = LogFactory.getLog(UgandaEMRSyncServiceImpl.class);
    private List<Object> unproccesedItems = new ArrayList<>();
    private List<Object> processedItems = new ArrayList<>();

    private StockOperation stockOperation = null;

    private List<ObjectNode> productCatelogList;
    private List<SyncTask> eAFYATaskListForToday;


    /**
     * Injected in moduleApplicationContext.xml
     */
    public void setDao(UgandaEMRSyncDao dao) {
        this.dao = dao;
    }

    /**
     * @see UgandaEMRSyncService#getAllSyncTaskType()
     */
    @Override
    public List<SyncTaskType> getAllSyncTaskType() throws APIException {
        return dao.getAllSyncTaskType();
    }

    /**
     * @see UgandaEMRSyncService#getSyncTaskTypeByUUID(String)
     */
    @Override
    public SyncTaskType getSyncTaskTypeByUUID(String uuid) throws APIException {
        return dao.getSyncTaskTypeByUUID(uuid);
    }

    /**
     * @see UgandaEMRSyncService#saveSyncTaskType(SyncTaskType)
     */
    @Override
    public SyncTaskType saveSyncTaskType(SyncTaskType syncTaskType) throws APIException {
        if (syncTaskType.getCreator() == null) {
            syncTaskType.setCreator(Context.getUserService().getUser(1));
        }
        return dao.saveSyncTaskType(syncTaskType);
    }

    /**
     * @see UgandaEMRSyncService#getSyncTaskBySyncTaskId(String)
     */
    @Override
    public SyncTask getSyncTaskBySyncTaskId(String syncTaskId) throws APIException {
        return dao.getSyncTask(syncTaskId);
    }

    @Override
    public List<SyncTask> getSyncTasksBySyncTaskId(String syncTaskId) throws APIException {
        return dao.getSyncTasks(syncTaskId);
    }

    /**
     * @see UgandaEMRSyncService#getAllSyncTask()
     */
    @Override
    public List<SyncTask> getAllSyncTask() {
        return dao.getAllSyncTask();
    }


    /**
     * @see UgandaEMRSyncService#saveSyncTask(SyncTask)
     */
    @Override
    public SyncTask saveSyncTask(SyncTask syncTask) throws APIException {
        if (syncTask.getCreator() == null) {
            syncTask.setCreator(Context.getUserService().getUser(1));
        }
        return dao.saveSyncTask(syncTask);
    }


    /**
     * @see UgandaEMRSyncService#getIncompleteActionSyncTask(String)
     */
    @Override
    public List<SyncTask> getIncompleteActionSyncTask(String syncTaskTypeUuid) throws APIException {
        return dao.getIncompleteActionSyncTask(syncTaskTypeUuid);
    }

    /**
     * @param query
     * @return
     */
    @Override
    public List getDatabaseRecord(String query) {
        return dao.getDatabaseRecord(query);
    }

    /**
     * @param columns
     * @param finalQuery
     * @return
     */
    @Override
    public List getFinalList(List<String> columns, String finalQuery) {
        return dao.getFinalResults(columns, finalQuery);
    }


    /**
     * @see UgandaEMRSyncService#addVLToEncounter(String, String, String, Encounter, Order)
     */
    public Encounter addVLToEncounter(String vlQualitative, String vlQuantitative, String vlDate, Encounter encounter, Order order) {
        if (!encounterHasVLDataAlreadySaved(encounter, order)) {
            Concept dateSampleTaken = Context.getConceptService().getConcept("163023");
            Concept viralLoadQualitative = Context.getConceptService().getConcept("1305");
            Concept viralLoadQuantitative = Context.getConceptService().getConcept("856");
            Concept viralLoadReturnDate = Context.getConceptService().getConcept("167944");
            Concept valueCoded = null;

            String dateFormat = getDateFormat(vlDate);

            String vlQualitativeString = vlQualitative.replaceAll("\"", "");

            if (vlQualitativeString.contains("Target Not Detected") || vlQualitativeString.contains("Not detected")) {
                valueCoded = Context.getConceptService().getConcept("1306");
            } else if (vlQualitativeString.contains("FAILED")) {
                valueCoded = Context.getConceptService().getConcept("1304");
            } else {
                valueCoded = Context.getConceptService().getConcept("1301");
            }
            Concept viralLOadTestGroupConcept = null;
            if (order != null) {
                viralLOadTestGroupConcept = order.getConcept();
            } else {
                viralLOadTestGroupConcept = Context.getConceptService().getConcept(165412);
            }

            // Creating Obs group and adding test Date
            Obs viralLoadTestGroupObs = createObs(encounter, order, viralLOadTestGroupConcept, null, null, null);
            Obs dateSampleTakenObs = createObs(encounter, order, dateSampleTaken, null, convertStringToDate(vlDate, "00:00:00", dateFormat), null);
            viralLoadTestGroupObs.addGroupMember(dateSampleTakenObs);

            Obs viralLoadQualitativeObs = null;
            Obs viralLoadQuantitativeObs = null;
            Obs viralLoadReturnDateObs = null;

            if (viralLoadQualitative != null && valueCoded != null) {
                viralLoadQualitativeObs = createObs(encounter, order, viralLoadQualitative, valueCoded, null, null);
                viralLoadTestGroupObs.addGroupMember(viralLoadQualitativeObs);
            }


            if (vlQuantitative != null) {
                Double quantitativeValue = 1.0;
                quantitativeValue = Double.valueOf(vlQuantitative);
                try {
                    quantitativeValue = Double.valueOf(vlQuantitative);
                } catch (Exception exception) {
                    log.error("failed to get quantitative value due to " + exception.getMessage() + " will be assigned 1 as the default value");
                }

                viralLoadQuantitativeObs = createObs(encounter, order, viralLoadQuantitative, null, null, Double.valueOf(vlQuantitative));
                viralLoadTestGroupObs.addGroupMember(viralLoadQuantitativeObs);
            }

            if (viralLoadQualitativeObs == null && viralLoadQuantitativeObs == null)
                return null;

            viralLoadReturnDateObs = createObs(encounter, order, viralLoadReturnDate, null, new Date(), null);

            viralLoadTestGroupObs.addGroupMember(viralLoadReturnDateObs);

            //Void Similar observation
            voidObsFound(encounter, dateSampleTaken);
            voidObsFound(encounter, viralLoadQualitative);
            voidObsFound(encounter, viralLoadQuantitative);
            voidObsFound(encounter, viralLoadReturnDate);

            encounter.addObs(viralLoadTestGroupObs);
            Context.getEncounterService().saveEncounter(encounter);

            return encounter;
        } else {
            if (order != null && order.isActive()) {
                try {
                    Context.getOrderService().discontinueOrder(order, "Completed", new Date(), order.getOrderer(), order.getEncounter());
                } catch (Exception e) {
                    log.error("Failed to discontinue order", e);
                }
            }
            return encounter;
        }
    }

    public String getDateFormat(String date) {
        String dateFormat = "";
        if (date.contains("-")) {
            dateFormat = "yyyy-MM-dd";
        } else if (date.contains("/")) {
            dateFormat = "dd/MM/yyyy";
        }
        return dateFormat;
    }


    /**
     * @see UgandaEMRSyncService#getPatientByPatientIdentifier(String)
     */
    public Patient getPatientByPatientIdentifier(String patientIdentifier) {
        try {
            return Context.getPatientService().getPatientIdentifiers(patientIdentifier, null, null, null, null).get(0).getPatient();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * @see UgandaEMRSyncService#validateFacility(String)
     */
    public boolean validateFacility(String facilityDHIS2UUID) {
        try {
            String globalProperty = Context.getAdministrationService().getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID);
            return facilityDHIS2UUID.contentEquals(globalProperty);
        } catch (Exception e) {
            log.error("Failed to validate facility uuid", e);
            return false;
        }

    }

    public Collection<EncounterType> getEcounterTypes(String encounterTypesUUID) {
        Collection<EncounterType> encounterTypes = new ArrayList<>();
        encounterTypes.add(Context.getEncounterService().getEncounterTypeByUuid(encounterTypesUUID));
        return encounterTypes;
    }

    /**
     * Appends a time to a date
     *
     * @param dateString the date in string which will be
     * @param time
     * @param dateFormat
     * @return
     */
    public Date convertStringToDate(String dateString, String time, String dateFormat) {

        DateFormat format = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        SimpleDateFormat formatterExt = new SimpleDateFormat("dd/MM/yyyy");

        try {
            date = format.parse(dateString);
            if (date != null && time != "") {
                date = formatter.parse(formatterExt.format(date) + " " + time);

            }
        } catch (ParseException e) {
            log.error("failed to convert date to string", e);
        }

        return date;
    }


    /**
     * This Method is used to generate an observation.
     *
     * @param encounter     the encounter which is has to be assigned to the observation
     * @param order         the order which has to be assigned to the observation
     * @param concept       the concept which is the question to the observation
     * @param valueCoded    concept which may be the answer to the question
     * @param valueDatetime the value date which may be the answer to the question.
     * @param valueNumeric  a numeric value which may be assigned to
     * @return
     */
    private Obs createObs(Encounter encounter, Order order, Concept concept, Concept valueCoded, Date valueDatetime, Double valueNumeric) {
        Obs newObs = new Obs();
        newObs.setConcept(concept);
        newObs.setValueCoded(valueCoded);
        newObs.setValueNumeric(valueNumeric);
        newObs.setValueDatetime(valueDatetime);
        newObs.setCreator(encounter.getCreator());
        newObs.setDateCreated(encounter.getDateCreated());
        newObs.setEncounter(encounter);
        newObs.setOrder(order);
        newObs.setPerson(encounter.getPatient());

        if (encounter.getEncounterDatetime() != null) {
            newObs.setObsDatetime(encounter.getEncounterDatetime());
        }
        return newObs;
    }

    /**
     * This method is used to void any observation that is similar to what is being added
     *
     * @param encounter the encounter for the observation that will be voided
     * @param concept   the concept for the encounter that will be voided
     */
    private void voidObsFound(Encounter encounter, Concept concept) {
        ObsService obsService = Context.getObsService();
        List<Obs> obsListToVoid = obsService.getObservationsByPersonAndConcept(encounter.getPatient(), concept);
        for (Obs obsToVoid : obsListToVoid) {
            if (obsToVoid.getEncounter() == encounter) {
                obsService.voidObs(obsToVoid, "Observation has been replaced or updated.");
            }
        }
    }

    /**
     * /**
     *
     * @see UgandaEMRSyncService#getHealthCenterCode()
     */
    public String getHealthCenterCode() {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        return syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID);
    }


    /**
     * @see UgandaEMRSyncService#getHealthCenterName()
     */
    public String getHealthCenterName() {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        return syncGlobalProperties.getGlobalProperty("aijar.healthCenterName");
    }

    /**
     * @see UgandaEMRSyncService#getPatientIdentifier(Patient, String)
     */
    public String getPatientIdentifier(Patient patient, String patientIdentifierTypeUUID) {
        String query = "select patient_identifier.identifier from patient_identifier inner join patient_identifier_type on(patient_identifier.identifier_type=patient_identifier_type.patient_identifier_type_id) where patient_identifier_type.uuid in ('" + patientIdentifierTypeUUID + "') AND patient_id=" + patient.getPatientId() + "";
        List list = Context.getAdministrationService().executeSQL(query, true);
        String patientARTNO = "";
        if (!list.isEmpty()) {
            patientARTNO = list.get(0).toString().replace("[", "").replace("]", "");
        }
        return patientARTNO;
    }

    public boolean encounterHasVLDataAlreadySaved(Encounter encounter, Order order) {

        if (encounter != null && order == null) {
            Set<Obs> obs = encounter.getAllObs(false);
            return obs.stream().map(Obs::getConcept).collect(Collectors.toSet()).contains(Context.getConceptService().getConcept(165412));
        } else {
            return testOrderHasResults(order);
        }
    }

    public Properties getUgandaEMRProperties() {
        Properties properties = new Properties();
        String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
        String facilityDHIS2ID = Context.getAdministrationService().getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID);
        if (!appDataDir.endsWith(System.getProperty("file.separator")))
            appDataDir = appDataDir + System.getProperty("file.separator");
        String filePath = appDataDir + "ugandaemr-setting.properties";

        try {
            File newUgandaEMRSettingFile = new File(filePath);
            if (!newUgandaEMRSettingFile.exists()) {
                newUgandaEMRSettingFile.createNewFile();

                FileInputStream fileInputStream = new FileInputStream(filePath);
                if (facilityDHIS2ID != null && !facilityDHIS2ID.equalsIgnoreCase("")) {
                    properties.setProperty(GP_DHIS2_ORGANIZATION_UUID, facilityDHIS2ID);
                    properties.setProperty(SYNC_METRIC_DATA, "true");
                } else {
                    properties.setProperty(GP_DHIS2_ORGANIZATION_UUID, "");
                    properties.setProperty(SYNC_METRIC_DATA, "false");
                }
                properties.load(fileInputStream);

                properties.store(new FileOutputStream(filePath), null);


            } else {
                FileReader reader = new FileReader(filePath);
                properties.load(reader);
            }
        } catch (FileNotFoundException e) {
            log.error("ugandaemr-setting.properties file Not found", e);
        } catch (IOException e) {
            log.error(e);
        }
        return properties;

    }

    /**
     * @see UgandaEMRSyncService#saveSyncFhirProfile(SyncFhirProfile)
     */
    @Override
    public SyncFhirProfile saveSyncFhirProfile(SyncFhirProfile syncFhirProfile) {
        return dao.saveSyncFhirProfile(syncFhirProfile);
    }

    /**
     * @see UgandaEMRSyncService#getSyncFhirProfileById(Integer)
     */
    @Override
    public SyncFhirProfile getSyncFhirProfileById(Integer id) {
        return dao.getSyncFhirProfileById(id);
    }

    /**
     * @see UgandaEMRSyncService#getSyncFhirProfileByUUID(String)
     */
    @Override
    public SyncFhirProfile getSyncFhirProfileByUUID(String uuid) {
        return dao.getSyncFhirProfileByUUID(uuid);
    }


    /**
     * @see UgandaEMRSyncService#getSyncFhirProfileByScheduledTaskName(String)
     */
    @Override
    public SyncFhirProfile getSyncFhirProfileByScheduledTaskName(String scheduledTaskName) {

        TaskDefinition taskDefinition = Context.getSchedulerService().getTaskByName(scheduledTaskName);

        if (taskDefinition != null) {
            String syncFhirProfileUUID = taskDefinition.getProperty("syncFhirProfileUUID");
            SyncFhirProfile syncFhirProfile = getSyncFhirProfileByUUID(syncFhirProfileUUID);
            if (syncFhirProfile != null) {
                return syncFhirProfile;
            }
        }
        return null;
    }


    /**
     * @see UgandaEMRSyncService#saveFHIRResource(SyncFhirResource)
     */
    @Override
    public SyncFhirResource saveFHIRResource(SyncFhirResource syncFHIRResource) {
        return dao.saveSyncFHIRResource(syncFHIRResource);
    }


    /**
     * @see UgandaEMRSyncService#saveFHIRResource(SyncFhirResource)
     */
    @Override
    public List<SyncFhirResource> getSyncFHIRResourceBySyncFhirProfile(SyncFhirProfile syncFhirProfile, boolean includeSynced) {
        return dao.getSyncResourceBySyncFhirProfile(syncFhirProfile, includeSynced);
    }


    /**
     * @see UgandaEMRSyncService#getSyncFHIRResourceById(Integer)
     */
    @Override
    public SyncFhirResource getSyncFHIRResourceById(Integer id) {
        return dao.getSyncFHIRResourceById(id);
    }

    /**
     * @see UgandaEMRSyncService#markSyncFHIRResourceSynced(SyncFhirResource)
     */
    @Override
    public SyncFhirResource markSyncFHIRResourceSynced(SyncFhirResource syncFhirResources) {
        Date today = new Date();
        syncFhirResources.setSynced(true);
        syncFhirResources.setDateSynced(today);
        syncFhirResources.setExpiryDate(UgandaEMRSyncUtil.addDaysToDate(today, syncFhirResources.getGeneratorProfile().getDurationToKeepSyncedResources()));
        return dao.saveSyncFHIRResource(syncFhirResources);
    }

    /**
     * @see UgandaEMRSyncService#getExpiredSyncFHIRResources(Date)
     */
    @Override
    public List<SyncFhirResource> getExpiredSyncFHIRResources(Date date) {
        return dao.getExpiredSyncFHIRResources(date);
    }


    /**
     * @see UgandaEMRSyncService#getUnSyncedFHirResources(SyncFhirProfile)
     */
    @Override
    public List<SyncFhirResource> getUnSyncedFHirResources(SyncFhirProfile syncFhirProfile) {
        return dao.getUnSyncedFHirResources(syncFhirProfile);
    }


    /**
     * @see UgandaEMRSyncService#purgeExpiredFHIRResource(Date)
     */
    @Override
    public void purgeExpiredFHIRResource(Date date) {
        for (SyncFhirResource syncFHIRResource : getExpiredSyncFHIRResources(date)) {
            dao.purgeExpiredFHIRResource(syncFHIRResource);
        }
    }


    /**
     * @see UgandaEMRSyncService#saveSyncFhirProfileLog(SyncFhirProfileLog)
     */
    @Override
    public SyncFhirProfileLog saveSyncFhirProfileLog(SyncFhirProfileLog syncFhirProfileLog) {
        return dao.saveSyncFhirProfileLog(syncFhirProfileLog);
    }


    /**
     * @see UgandaEMRSyncService#getSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile, String)
     */
    @Override
    public List<SyncFhirProfileLog> getSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile syncFhirProfile, String resourceType) {
        return dao.getSyncFhirProfileLogByProfileAndResourceName(syncFhirProfile, resourceType);
    }


    /**
     * @see UgandaEMRSyncService#getLatestSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile, String)
     */
    @Override
    public SyncFhirProfileLog getLatestSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile syncFhirProfile, String resourceType) {

        List<SyncFhirProfileLog> syncFhirProfileLogs = getSyncFhirProfileLogByProfileAndResourceName(syncFhirProfile, resourceType);

        if (syncFhirProfileLogs.size() > 0)
            return syncFhirProfileLogs.get(0);
        else
            return null;
    }


    /**
     * @see UgandaEMRSyncService#getSyncFHIRCaseBySyncFhirProfileAndPatient(SyncFhirProfile, Patient, String)
     */
    @Override
    public SyncFhirCase getSyncFHIRCaseBySyncFhirProfileAndPatient(SyncFhirProfile syncFhirProfile, Patient patient, String caseIdentifier) {
        return dao.getSyncFHIRCaseBySyncFhirProfileAndPatient(syncFhirProfile, patient, caseIdentifier);
    }

    /**
     * @see UgandaEMRSyncService#saveSyncFHIRCase(SyncFhirCase)
     */
    @Override
    public SyncFhirCase saveSyncFHIRCase(SyncFhirCase syncFHIRCase) {
        return dao.saveSyncFHIRCase(syncFHIRCase);
    }

    /**
     * @see UgandaEMRSyncService#getAllSyncFhirProfile()
     */
    @Override
    public List<SyncFhirProfile> getAllSyncFhirProfile() {
        return dao.getAllSyncFhirProfile();
    }


    /**
     * @see UgandaEMRSyncService#getSyncFhirCasesByProfile(SyncFhirProfile)
     */
    @Override
    public List<SyncFhirCase> getSyncFhirCasesByProfile(SyncFhirProfile syncFhirProfile) {
        return dao.getSyncFhirCasesByProfile(syncFhirProfile);
    }


    /**
     * @see UgandaEMRSyncService#testOrderHasResults(Order)
     */
    public boolean testOrderHasResults(Order order) {
        boolean hasOrder = false;

        List list = Context.getAdministrationService().executeSQL("select obs_id from obs where order_id=" + order.getOrderId() + "", true);

        if (!list.isEmpty()) {
            hasOrder = true;
        } else if (resultsEnteredOnEncounter(order)) {

            hasOrder = true;
        }
        return hasOrder;
    }


    /**
     * Checks if the test ordered already has detached results entered on separately on the encounter the encounter
     *
     * @param order order to be checked.
     * @return true when results have already been entered or false when results have not yet been entered
     */
    private boolean resultsEnteredOnEncounter(Order order) {

        boolean resultsEnteredOnEncounter = false;

        Set<Obs> allObs = order.getEncounter().getAllObs(false);
        for (Obs obs1 : allObs) {
            if (obs1.getConcept().getConceptId().equals(order.getConcept().getConceptId()) && (!obs1.getValueAsString(Locale.ENGLISH).equals("") || obs1.getValueAsString(Locale.ENGLISH) != null)) {
                resultsEnteredOnEncounter = true;
                return true;
            }
        }

        Set<Concept> conceptSet = allObs.stream().map(Obs::getConcept).collect(Collectors.toSet());
        List<Concept> members = order.getConcept().getSetMembers();

        if (members.size() > 0) {
            for (Concept concept : members) {
                if (conceptSet.contains(concept)) {
                    resultsEnteredOnEncounter = true;
                    return true;
                }
            }
        }

        return resultsEnteredOnEncounter;
    }

    /**
     * @see UgandaEMRSyncService#getSyncFhirProfileByName(String)
     */
    public List<SyncFhirProfile> getSyncFhirProfileByName(String name) {
        return dao.getSyncFhirProfileByName(name);
    }


    /**
     * @see UgandaEMRSyncService#getSyncFhirCaseByUUDI(String)
     */
    @Override
    public SyncFhirCase getSyncFhirCaseByUUDI(String uuid) {
        return dao.getSyncFhirCaseByUUDI(uuid);
    }


    /**
     * @see UgandaEMRSyncService#getAllSyncFhirCase()
     */
    @Override
    public List<SyncFhirCase> getAllSyncFhirCase() {
        return dao.getAllSyncFhirCase();
    }


    /**
     * @see UgandaEMRSyncService#getSyncFhirCaseById(Integer)
     */
    @Override
    public SyncFhirCase getSyncFhirCaseById(Integer id) {
        return dao.getSyncFhirCaseById(id);
    }


    /**
     * @see UgandaEMRSyncService#getAllSyncFhirProfileLog()
     */
    @Override
    public List<SyncFhirProfileLog> getAllSyncFhirProfileLog() {
        return dao.getAllSyncFhirProfileLog();
    }


    /**
     * @see UgandaEMRSyncService#getSyncFhirProfileLogByUUID(String)
     */
    @Override
    public SyncFhirProfileLog getSyncFhirProfileLogByUUID(String uuid) {
        return dao.getSyncFhirProfileLogByUUID(uuid);
    }


    /**
     * @see UgandaEMRSyncService#getSyncFhirProfileLogById(Integer)
     */
    @Override
    public SyncFhirProfileLog getSyncFhirProfileLogById(Integer id) {
        return dao.getSyncFhirProfileLogById(id);
    }

    /**
     * @see UgandaEMRSyncService#getAllFHirResources()
     */
    @Override
    public List<SyncFhirResource> getAllFHirResources() {
        return dao.getAllFHirResources();
    }

    /**
     * @see UgandaEMRSyncService#getSyncFhirResourceByUUID(String)
     */
    @Override
    public SyncFhirResource getSyncFhirResourceByUUID(String uuid) {
        return dao.getSyncFhirResourceByUUID(uuid);
    }


    /**
     * @see UgandaEMRSyncService#getSyncFhirProfileLogByProfile(SyncFhirProfile)
     */
    @Override
    public List<SyncFhirProfileLog> getSyncFhirProfileLogByProfile(SyncFhirProfile syncFhirProfile) {
        return dao.getSyncFhirProfileLogByProfile(syncFhirProfile);
    }

    /**
     * @see UgandaEMRSyncService#addTestResultsToEncounter(String, Order)
     */
    public List<Encounter> addTestResultsToEncounter(String bundleResultsJson, Order order) {
        ObjectMapper objectMapper = new ObjectMapper();
        Encounter encounter = null;
        if (order != null) {
            encounter = order.getEncounter();
        }

        List<Encounter> returningEncounters = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(bundleResultsJson);
            ArrayNode entryArray = (ArrayNode) root.get("entry");

            // Filter DiagnosticReport and Observation entries
            List<JsonNode> diagnosticReports = searchJsonObjectsByKey(entryArray, "resourceType", "DiagnosticReport");

            List<JsonNode> observations = searchJsonObjectsByKey(entryArray, "resourceType", "Observation");

            for (JsonNode diagnosticReport : diagnosticReports) {
                returningEncounters = processTestResults(diagnosticReport, encounter, observations, order);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse bundle results JSON", e);
        }

        return returningEncounters;
    }

    /**
     * Filters entries by a field value.
     */
    private List<JsonNode> searchJsonObjectsByKey(ArrayNode array, String key, String value) {
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode entry : array) {
            JsonNode resource = entry.get("resource");
            if (resource != null && value.equals(resource.path(key).asText())) {
                result.add(resource);

                convertStringToFHIRResource(entry.asText());
            }
        }
        return result;
    }

    private List<IBaseResource> searchResourceByKey(ArrayNode array, String key, String value) {
        List<IBaseResource> result = new ArrayList<>();
        for (JsonNode entry : array) {
            JsonNode resource = entry.get("resource");
            if (resource != null && value.equals(resource.path(key).asText())) {
                result.add(convertStringToFHIRResource(entry.asText())) ;
            }
        }
        return result;
    }

    /**
     * Processes one DiagnosticReport and its associated Observations.
     * Replace this with your real processing logic.
     */
    private List<Encounter> processTestResults(JsonNode diagnosticReport, Encounter encounter, List<JsonNode> observations, Order order) {
        List<Encounter> result = new ArrayList<>();

        if (diagnosticReport == null || !diagnosticReport.has("result")) {
            return result;
        }

        // Try to resolve encounter from basedOn if not already provided
        if (order == null) {
            order = getEncounterFromServiceRequestReference(diagnosticReport);
        }

        if (encounter == null) {
            encounter = (order != null) ? order.getEncounter() : null;
        }

        if (encounter == null || order == null) {
            return result;
        }

        JsonNode resultArray = diagnosticReport.get("result");

        for (JsonNode ref : resultArray) {
            String reference = ref.get("reference").asText(); // e.g. Observation/p1

            JsonNode parentObservation = findObservationByReference(reference, observations);
            if (parentObservation == null) continue;

            // Handle hasMember: multiple child observations
            if (parentObservation.has("hasMember")) {
                for (JsonNode member : parentObservation.get("hasMember")) {
                    String childRef = member.get("reference").asText(); // e.g. Observation/observation-1
                    JsonNode childObservation = findObservationByReference(childRef, observations);
                    if (childObservation != null) {
                        saveObservationToEncounter(childObservation, encounter, order);
                    }
                }
            } else {
                // If no hasMember, treat the observation itself as standalone
                saveObservationToEncounter(parentObservation, encounter, order);
            }
        }

        Context.getEncounterService().saveEncounter(encounter);
        result.add(encounter);
        return result;
    }

    private void saveObservationToEncounter(JsonNode observationNode, Encounter encounter, Order order) {
        if (observationNode == null || encounter == null || order == null) return;

        try {

            Obs obs = createObsFromFHIRObervation(observationNode, order, false);

            if (obs != null) {
                Context.getObsService().saveObs(obs, null);
                encounter.addObs(obs);
            }
        } catch (Exception e) {
            log.warn("Failed to save observation to encounter: " + observationNode, e);
        }
    }


    private JsonNode findObservationByReference(String reference, List<JsonNode> observations) {
        if (reference == null) return null;
        String refId = reference.contains("/") ? reference.substring(reference.lastIndexOf("/") + 1) : reference;
        for (JsonNode obs : observations) {
            if (obs.has("id") && refId.equals(obs.get("id").asText())) {
                return obs;
            }
        }
        return null;
    }


    private Order getEncounterFromServiceRequestReference(JsonNode diagnosticReport) {
        if (diagnosticReport == null || !diagnosticReport.has("basedOn")) {
            return null;
        }

        JsonNode basedOnArray = diagnosticReport.get("basedOn");
        if (!basedOnArray.isArray()) {
            return null;
        }

        for (JsonNode basedOn : basedOnArray) {
            if (basedOn.has("reference")) {
                String reference = basedOn.get("reference").asText();
                if (reference.startsWith("ServiceRequest/")) {
                    String orderUuid = reference.substring("ServiceRequest/".length());
                    OrderService orderService = Context.getOrderService();
                    Order order = orderService.getOrderByUuid(orderUuid);
                    if (order != null && order.getEncounter() != null) {
                        return order;
                    }
                }
            }
        }

        return null;
    }


    private List<JsonNode> searchForObjectsByKey(ArrayNode array, String key, String searchValue) {
        List<JsonNode> filteredList = new ArrayList<>();

        if (array == null || key == null || searchValue == null) {
            return filteredList;
        }

        for (JsonNode node : array) {
            try {
                JsonNode resource = node.path("resource");
                if (resource.has(key) && searchValue.equals(resource.get(key).asText())) {
                    filteredList.add(resource);
                }
            } catch (Exception e) {
                log.error(String.format("Failed to filter object by key [%s]: %s", key, e.getMessage(), e));
            }
        }

        return filteredList;
    }


    private Order getOrderFromFHIRObs(JsonNode observationNode) {
        if (observationNode == null || !observationNode.has("basedOn")) {
            return null;
        }

        JsonNode basedOnArray = observationNode.get("basedOn");
        if (!basedOnArray.isArray() || basedOnArray.size() == 0) {
            return null;
        }

        JsonNode referenceNode = basedOnArray.get(0).get("reference");
        if (referenceNode == null || referenceNode.isNull()) {
            return null;
        }

        String reference = referenceNode.asText();
        if (!reference.startsWith("ServiceRequest/")) {
            return null;
        }

        String orderUUID = reference.replace("ServiceRequest/", "");
        return Context.getOrderService().getOrderByUuid(orderUUID);
    }

    private Concept getConceptFromCodableConcept(JsonNode codableConcept) {
        if (codableConcept == null || !codableConcept.has("coding")) {
            return null;
        }

        JsonNode codingArray = codableConcept.get("coding");
        if (!codingArray.isArray()) {
            return null;
        }

        for (JsonNode codeNode : codingArray) {
            String code = codeNode.path("code").asText(null);
            String system = codeNode.path("system").asText(null);

            if (code != null && system != null) {
                ConceptSource conceptSource = getConceptSourceBySystemURL(system);
                if (conceptSource != null) {
                    Concept concept = Context.getConceptService().getConceptByMapping(code, conceptSource.getName());
                    if (concept != null) {
                        return concept;
                    }
                }
            }
        }

        return null;
    }

    /*private List<Encounter> processTestResults(JsonNode diagnosisReportJson, Encounter encounter, List<JsonNode> observationList, Order order) {
        List<Encounter> encounters = new ArrayList<>();

        if (diagnosisReportJson == null || !diagnosisReportJson.has("resource")) {
            return encounters;
        }

        JsonNode diagnosticReport = diagnosisReportJson.get("resource");
        if (!diagnosticReport.has("result")) {
            return encounters;
        }

        for (JsonNode resultRef : diagnosticReport.get("result")) {
            String obsId = resultRef.get("reference").asText().replace("Observation/", "");
            JsonNode observationNode = findObservationById(obsId, observationList);

            if (observationNode == null) continue;

            // Load order and encounter if not already provided
            if (order == null) {
                order = getOrderFromFHIRObs(observationNode);
                if (order == null) continue;
                encounter = order.getEncounter();
            }

            if (encounter == null) continue;

            if (resultsEnteredOnEncounter(order)) continue;

            boolean isSet = observationNode.has("hasMember");
            Obs obs = createObsFromFHIRObervation(observationNode, order, isSet);
            if (obs == null) continue;

            if (isSet) {
                addGroupMembersToObs(obs, observationNode.get("hasMember"), observationList, order, encounter);
            }

            encounter.addObs(obs);
            Context.getEncounterService().saveEncounter(encounter);

            // Discontinue active orders now fulfilled
            discontinueCompletedOrders(encounter);

            logResultsRecieved(order);
            encounters.add(encounter);
        }

        return encounters;
    }*/

    private JsonNode findObservationById(String id, List<JsonNode> observations) {
        for (JsonNode obs : observations) {
            JsonNode resource = obs.get("resource");
            if (resource != null && resource.has("id") && id.equals(resource.get("id").asText())) {
                return resource;
            }
        }
        return null;
    }

    private void addGroupMembersToObs(Obs parentObs, JsonNode hasMemberArray, List<JsonNode> allObservations, Order order, Encounter encounter) {
        for (JsonNode member : hasMemberArray) {
            String refId = member.get("reference").asText().replace("Observation/", "");
            JsonNode childObsNode = findObservationById(refId, allObservations);

            if (childObsNode != null) {
                Obs childObs = createObsFromFHIRObervation(childObsNode, order, false);
                if (childObs != null) {
                    parentObs.addGroupMember(childObs);
                    encounter.addObs(childObs);
                }
            }
        }
    }


    private void discontinueCompletedOrders(Encounter encounter) {
        List<Order> ordersToStop = encounter.getAllObs().stream()
                .filter(o -> o.getOrder() != null && o.getOrder().isActive())
                .map(Obs::getOrder)
                .distinct()
                .collect(Collectors.toList());

        for (Order o : ordersToStop) {
            try {
                Context.getOrderService().discontinueOrder(o, "Completed", new Date(), o.getOrderer(), o.getEncounter());
            } catch (Exception e) {
                log.error("Failed to discontinue order: " + o.getUuid(), e);
            }
        }
    }


    private SyncTask logResultsRecieved(Order order) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncTask syncTask = new SyncTask();
        syncTask.setActionCompleted(true);
        syncTask.setStatusCode(CONNECTION_SUCCESS_200);
        syncTask.setSyncTaskType(ugandaEMRSyncService.getSyncTaskTypeByUUID(ALIS_SYNC_TASK_TYPE_UUID));
        syncTask.setDateSent(order.getDateActivated());
        syncTask.setStatus("Completed");
        syncTask.setSyncTask(order.getConcept().getName().getName());
        syncTask.setRequireAction(false);

        return ugandaEMRSyncService.saveSyncTask(syncTask);

    }

    private Obs createObsFromFHIRObervation(JsonNode observation, Order order, boolean isSet) {
        if (observation == null || !observation.has("code") || order == null || order.getEncounter() == null) {
            return null;
        }

        Concept concept = getConceptFromSource(observation.get("code"));
        if (concept == null) {
            return null;
        }

        Obs obs = createObs(order.getEncounter(), order, concept, null, null, null);
        if (obs == null) {
            return null;
        }

        if (!isSet) {
            String datatypeUuid = concept.getDatatype().getUuid();

            try {
                switch (datatypeUuid) {
                    case ConceptDatatype.CODED_UUID:
                        if (observation.has("valueCodeableConcept")) {
                            Concept valueCoded = getConceptFromSource(observation.get("valueCodeableConcept"));
                            if (valueCoded != null) {
                                obs.setValueCoded(valueCoded);
                            }
                        }
                        break;

                    case ConceptDatatype.NUMERIC_UUID:
                        if (observation.has("valueQuantity") && observation.get("valueQuantity").has("value")) {
                            obs.setValueNumeric(observation.get("valueQuantity").get("value").asDouble());
                        }
                        break;

                    case ConceptDatatype.BOOLEAN_UUID:
                        if (observation.has("valueBoolean")) {
                            obs.setValueBoolean(observation.get("valueBoolean").asBoolean());
                        }
                        break;

                    case ConceptDatatype.TEXT_UUID:
                        if (observation.has("valueString")) {
                            obs.setValueText(observation.get("valueString").asText());
                        }
                        break;
                }

                // Ensure the obs has a value before returning
                String valueStr = obs.getValueAsString(Locale.ENGLISH);
                if (valueStr == null || valueStr.trim().isEmpty()) {
                    return null;
                }

                return obs;

            } catch (Exception e) {
                log.warn("Failed to create Obs from FHIR Observation: " + observation.toPrettyString(), e);
                return null;
            }
        }

        return obs;
    }


    private ConceptSource getConceptSourceBySystemURL(String systemURL) {
        FhirConceptSourceService fhirConceptSourceService = null;
        ConceptSource conceptSource = null;
        try {
            Field serviceContextField = Context.class.getDeclaredField("serviceContext");
            serviceContextField.setAccessible(true);

            ApplicationContext applicationContext = ((ServiceContext) serviceContextField.get(null)).getApplicationContext();
            fhirConceptSourceService = applicationContext.getBean(FhirConceptSourceService.class);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error(e);
        }
        assert fhirConceptSourceService != null;
        if (fhirConceptSourceService.getFhirConceptSourceByUrl(systemURL).isPresent()) {
            conceptSource = fhirConceptSourceService.getFhirConceptSourceByUrl(systemURL).get().getConceptSource();
        }

        return conceptSource;
    }

    /**
     * @see UgandaEMRSyncService#getSyncedFHirResources(SyncFhirProfile)
     */
    @Override
    public List<SyncFhirResource> getSyncedFHirResources(SyncFhirProfile syncFhirProfile) {
        return dao.getSyncedFHirResources(syncFhirProfile);
    }

    @Override
    public Patient createPatientsFromFHIR(JsonNode patientData) throws ParseException {
        PatientService patientService = Context.getPatientService();

        // Parse birthdate
        Date date = null;
        if (patientData.has("birthDate")) {
            String birthDateStr = patientData.get("birthDate").asText();
            date = new SimpleDateFormat("yyyy-MM-dd").parse(birthDateStr);
        }

        // Parse gender
        String gender = patientData.path("gender").asText();

        // Extract name (assumes you have a Jackson-compatible getPatientNames(JsonNode))
        PersonName patientName = getPatientNames(patientData);

        // Build patient
        Patient patient = new Patient();
        patient.addName(patientName);
        patient.setBirthdate(date);
        patient.setGender(gender);

        // Handle identifiers
        ArrayNode identifierArray = (ArrayNode) patientData.path("identifier");
        if (identifierArray.isArray()) {
            patient = getPatientIdentifiers(identifierArray, patient);
        }

        return patientService.savePatient(patient);
    }


    public Patient updatePatientsFromFHIR(JsonNode bundle, String identifierUUID, String identifierName) {
        Patient patient = null;
        PatientService patientService = Context.getPatientService();
        if (bundle.has("resourceType") && bundle.get("resourceType").equals("Bundle") && bundle.get("entry").size() > 0) {
            ArrayNode bundleResourceObjects = (ArrayNode) bundle.get("entry");

            for (int i = 0; i < bundleResourceObjects.size(); i++) {
                JsonNode patientResource = bundleResourceObjects.get(i).get("resource");
                patient = patientService.getPatientByUuid(patientResource.get("id").asText());


                if (patient != null && patient.getPatientIdentifiers(patientService.getPatientIdentifierTypeByUuid(PATIENT_ID_TYPE_UIC_UUID)).size() > 0) {
                    if (patientResource.get("type").get("text").toString().equals(PATIENT_ID_TYPE_UIC_NAME)) {
                        patient.addIdentifier(createPatientIdentifierByIdentifierTypeName(patientResource.get("value").toString(), patientResource.get("type").get("text").toString()));
                    }
                }
                patientService.savePatient(patient);
            }
        }
        return patient;
    }

    @Override
    public List<SyncFhirResource> getSyncedFHirResources(SyncFhirProfile syncFhirProfile, Date dateSyncedFrom, Date dateSyncedTo) {
        return dao.getSyncedFHirResources(syncFhirProfile, dateSyncedFrom, dateSyncedTo);
    }

    private PersonName getPatientNames(JsonNode jsonObject) {
        PersonName personName = new PersonName();

        JsonNode namesArray = jsonObject.path("name");
        if (!namesArray.isArray() || namesArray.size() == 0) {
            return personName; // return empty name object if no names found
        }

        JsonNode nameObject = namesArray.get(0);

        // Set family name
        JsonNode family = nameObject.path("family");
        if (!family.isMissingNode() && !family.isNull()) {
            personName.setFamilyName(family.asText());
        }

        // Set given and middle names
        JsonNode givenArray = nameObject.path("given");
        if (givenArray.isArray()) {
            if (givenArray.size() >= 1 && !givenArray.get(0).isNull()) {
                personName.setGivenName(givenArray.get(0).asText());
            }
            if (givenArray.size() >= 2 && !givenArray.get(1).isNull()) {
                personName.setMiddleName(givenArray.get(1).asText());
            }
        }

        return personName;
    }

    private List<Identifier> getPatientIdentifiers(ArrayNode jsonArray) {
        List<Identifier> identifiers = new ArrayList<Identifier>();
        for (JsonNode jsonObject : jsonArray) {
            ;
            Identifier identifier = new Identifier();
            identifier.setIdentifier(jsonObject.get("identifier").toString());
            identifier.setIdentifierType(jsonObject.get("identifierType").toString());
            identifier.setIdentifierTypeName(Context.getPatientService()
                    .getPatientIdentifierTypeByUuid(jsonObject.get("identifierType").toString()).getName());
            identifiers.add(identifier);
        }
        return identifiers;
    }

    private Patient getPatientIdentifiers(ArrayNode jsonArray, Patient patient) {
        PatientService patientService = Context.getPatientService();
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        if (jsonArray != null && jsonArray.size() > 0) {
            for (JsonNode jsonObject : jsonArray) {
                JsonNode typeNode = jsonObject.path("type");
                String idTypeText = typeNode.path("text").asText();

                if (PATIENT_ID_TYPE_NIN_NAME.equals(idTypeText) || PATIENT_ID_TYPE_UIC_NAME.equals(idTypeText)) {
                    String value = jsonObject.path("value").asText();
                    PatientIdentifier identifier = createPatientIdentifierByIdentifierTypeName(value, idTypeText);
                    patient.addIdentifier(identifier);
                }
            }
        }

        patient.addIdentifier(generatePatientIdentifier());

        return patient;
    }


    private PatientIdentifier createPatientIdentifierByIdentifierTypeName(String identifier, String identifierTypeName) {
        PatientService patientService = Context.getPatientService();
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifierType(patientService.getPatientIdentifierTypeByName(identifierTypeName));
        patientIdentifier.setIdentifier(identifier);
        return patientIdentifier;
    }

    private PatientIdentifier generatePatientIdentifier() {
        IdentifierSourceService identifierSourceService = Context.getService(IdentifierSourceService.class);
        IdentifierSource idSource = identifierSourceService.getIdentifierSource(1);
        PatientService patientService = Context.getPatientService();

        UUID uuid = UUID.randomUUID();

        PatientIdentifierType patientIdentifierType = patientService.getPatientIdentifierTypeByUuid("05a29f94-c0ed-11e2-94be-8c13b969e334");

        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifierType(patientIdentifierType);
        String identifier = identifierSourceService.generateIdentifier(idSource, "New OpenMRS ID with CheckDigit");
        patientIdentifier.setIdentifier(identifier);
        patientIdentifier.setPreferred(true);
        patientIdentifier.setUuid(String.valueOf(uuid));

        return patientIdentifier;
    }

    public boolean patientFromFHIRExists(JsonNode patientData) {
        boolean patientExists = false;
        for (JsonNode jsonObject : (ArrayNode) patientData.get("identifier")) {
            PatientService patientService = Context.getPatientService();
            List<PatientIdentifier> patientIdentifier = patientService.getPatientIdentifiers(jsonObject.get("value").toString(), null, null, null, null);

            if (!patientIdentifier.isEmpty()) {
                patientExists = true;
            }
        }
        return patientExists;
    }

    @Override
    public List<SyncTaskType> getSyncTaskTypeByName(String name) {
        return dao.getSyncTaskTypeByName(name);
    }

    @Override
    public SyncTaskType getSyncTaskTypeById(Integer id) {
        return dao.getSyncTaskTypeById(id);
    }

    @Override
    public List<SyncTask> getSyncTasksByType(SyncTaskType syncTaskType, Date synceDateFrom, Date synceDateTo) {
        return dao.getSyncTasksByType(syncTaskType, synceDateFrom, synceDateTo);
    }

    public SyncTask getSyncTaskByUUID(String uniqueId) {
        return dao.getSyncTaskByUUID(uniqueId);
    }

    public SyncTask getSyncTaskById(Integer uniqueId) {
        return dao.getSyncTaskById(uniqueId);
    }

    @Override
    public List<SyncFhirResource> getSyncFHIRResourceBySyncFhirProfile(SyncFhirProfile syncFhirProfile, String synceDateFrom, String synceDateTo) {
        return dao.getSyncResourceBySyncFhirProfile(syncFhirProfile, synceDateFrom, synceDateTo);
    }

    public List<SyncTask> getSyncTasksByType(SyncTaskType syncTaskType) {
        return dao.getSyncTasksByType(syncTaskType);
    }

    public List<SyncTask> searchSyncTask(SyncTaskType syncTaskType, Integer statusCode, Date fromDate, Date toDate) {
        return dao.searchSyncTask(syncTaskType, statusCode, fromDate, toDate);
    }

    @Override
    public void deleteSyncTask(String syncTask, SyncTaskType syncTaskType) {
        dao.deleteUnSuccessfulSyncTasks(syncTask, syncTaskType);
    }


    /**
     * Processes and retrieves a list of stock requisitions that need to be synchronized.
     *
     * @return A list of JSON objects representing requisitions to sync.
     */
    private List<ObjectNode> processRequisitionsToSync() {
        try {
            // Fetch required global properties
            String storeId = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.storeId");
            String sourceStoreId = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.sourceStoreId");

            // Ensure store IDs are valid numbers
            int destinationStoreId = parseIntegerOrLog(storeId, "storeId");
            int sourceStoreIdInt = parseIntegerOrLog(sourceStoreId, "sourceStoreId");

            // Fetch required services and attributes
            StockManagementService stockManagementService = Context.getService(StockManagementService.class);
            ProviderAttributeType providerAttributeType = Objects.requireNonNull(
                    Context.getProviderService().getProviderAttributeTypeByUuid("d376f27c-cb93-45b4-be0a-6be88c520233"),
                    "ProviderAttributeType is required"
            );
            StockSource stockSource = Objects.requireNonNull(
                    stockManagementService.getStockSourceByUuid("9babcc02-fc0b-11ef-ab84-28977ca9db4b"),
                    "StockSource is required"
            );
            StockOperationType stockOperationType = Objects.requireNonNull(
                    stockManagementService.getStockOperationTypeByUuid("7073e8f4-eb6b-11ef-80d7-730ad71afb9c"),
                    "StockOperationType is required"
            );

            // Set up search filter
            StockOperationSearchFilter filter = new StockOperationSearchFilter();
            filter.setStatus(Collections.singletonList(StockOperationStatus.SUBMITTED));
            filter.setStockSourceId(stockSource.getId());
            filter.setOperationTypeId(Collections.singletonList(stockOperationType.getId()));

            // Fetch stock operations
            Result<StockOperationDTO> results = stockManagementService.findStockOperations(filter);
            List<ObjectNode> requisitions = new ArrayList<>();

            for (StockOperationDTO stockOperationDTO : results.getData()) {
                StockOperation stockOperation = stockManagementService.getStockOperationByUuid(stockOperationDTO.getUuid());

                // Check if the requisition has already been processed
                if (!getSyncTaskTypeByName(stockOperation.getUuid()).isEmpty()) {
                    continue;
                }

                Provider provider = fetchProviderForUser(stockOperationDTO.getCreator());

                // Process stock operation items
                ArrayNode items = processRequisitionItems(stockOperation.getStockOperationItems(), stockSource, provider, providerAttributeType);

                if (!items.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();

                    ObjectNode requisition = mapper.createObjectNode();

                    String eaFYAID = getProviderAttributeByType(provider.getAttributes(), providerAttributeType);

                    requisition.put("created_by_id", String.valueOf(parseIntegerOrReturnString(eaFYAID)));
                    requisition.put("destination_store_id", destinationStoreId);
                    requisition.put("source_store_id", sourceStoreIdInt);
                    requisition.put("requester_comment", stockOperation.getOperationNumber() + ": " + stockOperation.getRemarks());
                    requisition.put("internal_requisition_no", stockOperation.getOperationNumber());
                    requisition.put("internal_requisition_uuid", stockOperation.getUuid());
                    requisition.put("items", items);

                    requisitions.add(requisition);
                }
            }

            return requisitions;
        } catch (Exception e) {
            log.error("Error processing requisitions to sync", e);
            return Collections.emptyList();
        }
    }

    private String generateSpecimen(Order order) {
        try {
            if (order != null) {
                String specimenString = "{\"fullUrl\":\"urn:uuid:%s\",\"resource\":{\"resourceType\":\"Specimen\",\"id\":\"%s\",\"type\":{\"coding\":[{\"system\":\"http://snomed.info/sct\",\"code\":\"%s\",\"display\":\"Plasma specimen\"}]},\"subject\":{\"reference\":\"urn:uuid:%s\"},\"collection\":{\"collectedDateTime\":\"%s\",\"collector\":{\"reference\":\"Practitioner/%s\"}},\"processing\":[{\"description\":\"Centrifugation\",\"timeDateTime\":\"%s\"}]},\"request\":{\"method\":\"POST\",\"url\":\"Specimen\"}}";
                TestOrder testOrder = (TestOrder) order;
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode jsonObject = (ObjectNode) objectMapper.readTree(specimenString);

                ArrayNode coding = objectMapper.createArrayNode();
                ObjectNode resource = (ObjectNode) jsonObject.get("resource");

                if (testOrder.getAccessionNumber() != null) {
                    jsonObject.put("fullUrl", "urn:uuid:" + testOrder.getAccessionNumber());

                    JsonNode resourceNode = jsonObject.get("resource");

                    if (resourceNode != null && resourceNode.isObject()) {
                        ((ObjectNode) resourceNode).put("id", order.getAccessionNumber());
                    } else {
                        // Optionally create 'resource' if missing
                        resource = jsonObject.putObject("resource");
                        resource.put("id", order.getAccessionNumber());
                    }
                }

                for (ConceptMap conceptMap : testOrder.getSpecimenSource().getConceptMappings()) {
                    if (conceptMap != null && conceptMap.getConceptReferenceTerm() != null && conceptMap.getConceptReferenceTerm().getConceptSource() != null) {
                        String system = conceptMap.getConceptReferenceTerm().getConceptSource().getHl7Code();
                        if (system == null) {
                            system = conceptMap.getConceptReferenceTerm().getConceptSource().getName();
                        }

                        ObjectNode codingEntry = objectMapper.createObjectNode();
                        codingEntry.put("system", system);
                        codingEntry.put("code", conceptMap.getConceptReferenceTerm().getCode());
                        codingEntry.put("display", conceptMap.getConceptReferenceTerm().getName());

                        coding.add(codingEntry);
                    }
                }


                ObjectNode collection = objectMapper.createObjectNode();
                collection.put("collectedDateTime", testOrder.getDateActivated().toInstant().toString());

                ObjectNode collector = objectMapper.createObjectNode();
                collector.put("reference", "Practitioner/" + testOrder.getOrderer().getUuid());
                collection.set("collector", collector);

                resource.set("collection", collection);


                ArrayNode processingArray = objectMapper.createArrayNode();
                ObjectNode processing = objectMapper.createObjectNode();
                processing.put("description", "Centrifugation");
                processing.put("timeDateTime", testOrder.getDateActivated().toInstant().toString());
                processingArray.add(processing);
                resource.set("processing", processingArray);


                ObjectNode type = (ObjectNode) resource.get("type");
                type.set("coding", coding);


                ObjectNode subject = (ObjectNode) resource.get("subject");
                subject.put("reference", "urn:uuid:" + order.getPatient().getUuid());
                return jsonObject.toString();
            }
        } catch (Exception exception) {
            log.error(exception);
        }

        return null;
    }

    /**
     * Parses a string into an integer, logs an error if parsing fails, and returns 0 as default.
     */
    private int parseIntegerOrLog(String value, String fieldName) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("Invalid value for " + fieldName + ":" + value, e);
            return 0; // Return a default or handle appropriately
        }
    }

    /**
     * Fetches the provider associated with the given user ID.
     */
    private Provider fetchProviderForUser(Integer userId) {
        try {
            Collection<Provider> providers = Context.getProviderService()
                    .getProvidersByPerson(Context.getUserService().getUser(userId).getPerson(), false);
            return providers.stream().findFirst().orElse(null);
        } catch (Exception e) {
            log.error("Error fetching provider for user ID:" + userId, e);
            return null;
        }
    }


    /**
     * Sends stock requisition data to an external system and logs the response.
     *
     * @return A list of sent requisition numbers, or an empty list if none were sent.
     */
    public List<String> getSendRequisitionStock() {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
        StockManagementService stockManagementService = Context.getService(StockManagementService.class);
        UgandaEMRSyncService syncService = Context.getService(UgandaEMRSyncService.class);

        SyncTaskType syncTaskType = syncService.getSyncTaskTypeByUUID(EAFYA_STOCK_SYNC_TASK_UUID);
        if (syncTaskType == null) {
            log.error(String.format("Sync task type not found for UUID: %s", EAFYA_STOCK_SYNC_TASK_UUID));
            return Collections.emptyList();
        }

        String api = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.SendRequisitionStock");
        if (api == null || api.isEmpty()) {
            log.error(String.format("Global property %s is not set", MODULE_ID + ".eafya.SendRequisitionStock"));
            return Collections.emptyList();
        }

        String url = syncTaskType.getUrl() + api;
        String token = syncTaskType.getTokenType() + " " + syncTaskType.getUrlToken();

        List<String> successfulRequisitions = new ArrayList<>();

        try {
            for (ObjectNode jsonObject : processRequisitionsToSync()) {
                if (jsonObject.get("internal_requisition_uuid").isEmpty()) {
                    log.warn(String.format("Skipping requisition due to missing UUID: %s", jsonObject));
                    continue;
                }

                if (getSyncTaskBySyncTaskId(jsonObject.get("internal_requisition_no").asText()) != null) {
                    log.info(String.format("Requisition %s already synced, skipping.", jsonObject.get("internal_requisition_no")));
                    continue;
                }

                boolean success = sendRequisition(jsonObject, syncTaskType, stockManagementService, ugandaEMRHttpURLConnection, url, token);
                if (success) {
                    successfulRequisitions.add(jsonObject.get("internal_requisition_no").asText());
                }
            }
        } catch (Exception e) {
            log.error("Error syncing requisitions", e);
            logTransaction(syncTaskType, 500, "Internal Server Error", EAFYA_SMART_ERP_SEND_STOCK, e.getMessage(), new Date(), url, false, false);
        }

        return successfulRequisitions;
    }

    /**
     * Sends a single requisition to the external system.
     */
    private boolean sendRequisition(JsonNode jsonObject, SyncTaskType syncTaskType, StockManagementService stockManagementService,
                                    UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection, String url, String token) {
        try {
            StockOperation stockOperation = stockManagementService.getStockOperationByUuid(jsonObject.get("internal_requisition_uuid").asText());
            Map<String, Object> response = ugandaEMRHttpURLConnection.sendPostBy(url, syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), null, jsonObject.toString(), false);

            return parseResponse(response, jsonObject, syncTaskType, stockManagementService, stockOperation, url);
        } catch (Exception e) {
            log.error(String.format("Error sending requisition %s to ", jsonObject.get("internal_requisition_no").asText(), url), e);
            logTransaction(syncTaskType, 500, "Error sending requisition", EAFYA_SMART_ERP_SEND_STOCK, e.getMessage(), new Date(), url, false, false);
            return false;
        }
    }

    /**
     * Parses and handles the response from the external system.
     */
    private boolean parseResponse(Map<String, Object> response, JsonNode jsonObject, SyncTaskType syncTaskType,
                                  StockManagementService stockManagementService, StockOperation stockOperation, String url) {
        if (response.isEmpty()) {
            log.warn(String.format("Empty response received while syncing requisition %s", jsonObject.get("internal_requisition_no")));
            return false;
        }

        int responseCode = Integer.parseInt(response.get("responseCode").toString());
        if (responseCode == 200 || responseCode == 201) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.valueToTree(response);
            String externalRequisitionNumber = jsonResponse.get("data").get("requisition_number").asText();
            String statusMessage = jsonResponse.get("message").asText();

            log.info(String.format("Requisition %s synced successfully. External ID: %s", jsonObject.get("internal_requisition_no").asText(), externalRequisitionNumber));

            logTransaction(syncTaskType, responseCode, null, externalRequisitionNumber, statusMessage, new Date(), url, true, false);
            logTransaction(syncTaskType, responseCode, null, jsonObject.get("internal_requisition_no").asText(), statusMessage, new Date(), url, false, false);

            // Update stock operation with external reference
            StockOperationDTO stockOperationDTO = generateStockOperationDTO(stockOperation);
            stockOperationDTO.setExternalReference(externalRequisitionNumber);
            stockManagementService.saveStockOperation(stockOperationDTO);

            return true;
        } else {
            String errorMessage = String.format("Failed to sync requisition %s: %s",
                    jsonObject.get("internal_requisition_no"),
                    response.get("responseMessage").toString());
            log.error(errorMessage);

            logTransaction(syncTaskType, responseCode, null, EAFYA_SMART_ERP_SEND_STOCK, errorMessage, new Date(), url, false, false);
            return false;
        }
    }


    /**
     * Processes a set of stock operation items and converts them into a JSON array format.
     * Each stock operation item is matched with a stock reference, and relevant details
     * are extracted to create a structured JSON object.
     *
     * @param stockOperationItems   The set of stock operation items to process.
     * @param stockSource           The source of the stock references.
     * @param provider              The provider associated with the stock operation.
     * @param providerAttributeType The attribute type used to fetch provider details.
     * @return A ArrayNode containing the processed stock operation items.
     */
    private ArrayNode processRequisitionItems(Set<StockOperationItem> stockOperationItems,
                                              StockSource stockSource,
                                              Provider provider,
                                              ProviderAttributeType providerAttributeType) {

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        for (StockOperationItem stockOperationItem : stockOperationItems) {
            try {
                Optional<StockItemReference> stockItemReferenceOpt = stockOperationItem.getStockItem().getReferences().stream()
                        .filter(reference -> reference.getReferenceSource().equals(stockSource))
                        .findFirst();

                if (stockItemReferenceOpt.isPresent()) {
                    StockItemReference stockItemReference = stockItemReferenceOpt.get();
                    String eaFYAID = getProviderAttributeByType(provider.getAttributes(), providerAttributeType);

                    ObjectNode jsonObject = mapper.createObjectNode();
                    jsonObject.put("quantity", stockOperationItem.getQuantity());
                    jsonObject.put("expected_quantity", stockOperationItem.getQuantity());

                    // Handle integer or string fallback for product_id and created_by_id
                    String productId = stockItemReference.getStockReferenceCode();
                    if (isInteger(productId)) {
                        jsonObject.put("product_id", Integer.parseInt(productId));
                    } else {
                        jsonObject.put("product_id", productId);
                    }

                    if (isInteger(eaFYAID)) {
                        jsonObject.put("created_by_id", Integer.parseInt(eaFYAID));
                    } else {
                        jsonObject.put("created_by_id", eaFYAID);
                    }

                    arrayNode.add(jsonObject);
                }
            } catch (Exception e) {
                log.error(String.format("Error processing stock operation item: %s", stockOperationItem), e);
            }
        }

        return arrayNode;
    }

    /**
     * Parses an integer if the input is numeric; otherwise, returns the string.
     */
    private Object parseIntegerOrReturnString(String value) {
        return (value != null && isInteger(value)) ? Integer.parseInt(value) : value;
    }

    public List<String> getIssuedStock() {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
        SyncTaskType syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(EAFYA_STOCK_SYNC_TASK_UUID);
        String api = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.getIssuedStock");
        String stockOperation = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.operation");

        List<SyncTask> syncTasks = getIncompleteActionSyncTask(syncTaskType.getUuid());

        String url = syncTaskType.getUrl() + api;
        Map response = null;

        for (SyncTask syncTask : syncTasks) {
            this.stockOperation = null;
            try {
                ObjectMapper objectMapper=new ObjectMapper();
                ObjectNode requisition = objectMapper.createObjectNode();
                requisition.put("store_requisition_number", syncTask.getSyncTask());
                response = ugandaEMRHttpURLConnection.sendPostBy(url, syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), null, requisition.toString(), false);
                if (!response.isEmpty() && response.get("responseCode").equals(200)) {
                    List receivedItems = (List) response.get("data");
                    if (!receivedItems.isEmpty()) {
                        processStockOperation(response, stockOperation);
                        if (this.stockOperation != null) {
                            try {
                                if (stockOperation.equals("Submitted")) {
                                    submitStockOperation(this.stockOperation);
                                } else if (stockOperation.equals("Completed")) {
                                    completeStockOperation(this.stockOperation);
                                }
                            } catch (Exception exception) {
                                log.error(exception);
                            }
                            syncTask.setActionCompleted(true);
                            saveSyncTask(syncTask);
                            logTransaction(syncTaskType, Integer.parseInt(response.get("responseCode").toString()), "Successfully received to stock from receive " + processedItems.size() + " Stock Items", EAFYA_SMART_ERP_RECEIVE_STOCK, response.get("responseMessage").toString(), new Date(), syncTaskType.getUrl() + api, false, false);

                            List<StockOperation> requisitionStockOperations = getStockOperationsByExternalReference(syncTask.getSyncTask());
                            if (requisitionStockOperations != null) {
                                for (StockOperation requisitionStockOperation : requisitionStockOperations) {
                                    if (requisitionStockOperation != null) {
                                        Context.getService(StockManagementService.class).approveStockOperation(generateStockOperationDTO(requisitionStockOperation));
                                    }
                                }
                            }
                        }
                    }
                } else if (!response.isEmpty()) {
                    logTransaction(syncTaskType, Integer.parseInt(response.get("responseCode").toString()), "failed to sync receive stock items from eAFYA", EAFYA_SMART_ERP_RECEIVE_STOCK, response.get("responseMessage").toString(), new Date(), syncTaskType.getUrl() + api, false, false);
                }
            } catch (Exception e) {
                log.error(e.getCause());
                logTransaction(syncTaskType, 500, "Internal Server Error: failed to sync receive stock items from erp", EAFYA_SMART_ERP_RECEIVE_STOCK, e.getCause().getMessage(), new Date(), syncTaskType.getUrl() + api, false, false);
            }
        }
        return Collections.emptyList();
    }

    private List<StockOperation> getStockOperationsByExternalReference(String externalReference) {
        List<StockOperation> stockOperations = new ArrayList<>();
        StockManagementService stockManagementService = Context.getService(StockManagementService.class);
        try {
            List list = Context.getAdministrationService().executeSQL(String.format("select uuid from stockmgmt_stock_operation where external_reference=\"%s\"", externalReference), true);
            if (list.size() > 0) {
                for (Object stockOperationUuid : list) {
                    StockOperation stockOperation = stockManagementService.getStockOperationByUuid(((ArrayList) stockOperationUuid).get(0).toString());
                    stockOperations.add(stockOperation);
                }
            }
        } catch (Exception exception) {
            log.error(exception);
        }
        return stockOperations;
    }

    private void logTransaction(SyncTaskType syncTaskType, Integer statusCode, String statusMessage, String logName, String status, Date date, String url, boolean actionRequired, boolean actionCompleted) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        List<SyncTask> syncTasks = ugandaEMRSyncService.getSyncTasksBySyncTaskId(logName).stream().filter(syncTask -> syncTask.getSyncTaskType().equals(syncTaskType)).collect(Collectors.toList());
        if (!syncTasks.isEmpty()) {
            SyncTask existingTask = syncTasks.get(0);
            existingTask.setRequireAction(actionRequired);
            existingTask.setStatus(status);
            existingTask.setStatusCode(statusCode);
            existingTask.setActionCompleted(actionCompleted);
            existingTask.setDateSent(new Date());
            ugandaEMRSyncService.saveSyncTask(existingTask);
        } else {
            SyncTask newSyncTask = new SyncTask();
            newSyncTask.setCreator(Context.getUserService().getUser(1));
            newSyncTask.setSentToUrl(syncTaskType.getUrl());
            newSyncTask.setRequireAction(actionRequired);
            newSyncTask.setStatus(status);
            newSyncTask.setStatusCode(statusCode);
            newSyncTask.setActionCompleted(actionCompleted);
            newSyncTask.setSyncTask(logName);
            newSyncTask.setSyncTaskType(syncTaskType);
            newSyncTask.setSyncTaskType(syncTaskType);
            newSyncTask.setDateSent(new Date());
            ugandaEMRSyncService.saveSyncTask(newSyncTask);
        }
    }

    private StockOperation submitStockOperation(StockOperation stockOperation) {
        StockManagementService stockManagementService = Context.getService(StockManagementService.class);
        StockOperationDTO stockOperationDTO = generateStockOperationDTO(stockOperation);
        if (stockOperationDTO != null) {
            stockManagementService.submitStockOperation(stockOperationDTO);
        }

        return stockOperation;
    }

    private StockOperation completeStockOperation(StockOperation stockOperation) {
        StockManagementService stockManagementService = Context.getService(StockManagementService.class);
        StockOperationDTO stockOperationDTO = generateStockOperationDTO(stockOperation);
        if (stockOperationDTO != null) {
            stockOperationDTO.setCompletedBy(Context.getAuthenticatedUser().getUserId());
            stockOperationDTO.setCompletedDate(new Date());
            stockManagementService.completeStockOperation(stockOperationDTO);
        }

        return stockOperation;
    }

    private StockOperationDTO generateStockOperationDTO(StockOperation stockOperation) {
        StockOperationSearchFilter filter = new StockOperationSearchFilter();
        filter.setStockOperationUuid(stockOperation.getUuid());
        Result<StockOperationDTO> result = Context.getService(StockManagementService.class).findStockOperations(filter);
        return result.getData().isEmpty() ? null : result.getData().get(0);
    }

    public SyncTaskType setAccessTokenToSyncTaskType() {
        UgandaEMRHttpURLConnection httpURLConnection = new UgandaEMRHttpURLConnection();
        SyncTaskType syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(EAFYA_STOCK_SYNC_TASK_UUID);
        Map results = null;
        if (syncTaskType.getTokenExpiryDate() == null
                || (syncTaskType.getTokenExpiryDate() != null && syncTaskType.getTokenExpiryDate().before(new Date()))) {

            String tokenAPI = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".erp.getToken");

            String username = syncTaskType.getUrlUserName();
            String password = syncTaskType.getUrlPassword();
            String url = syncTaskType.getUrl();
            try {
                results = httpURLConnection.getTokenFromServer(url + tokenAPI, username, password);
                String access_token = results.get("access_token").toString();
                String token_type = results.get("token_type").toString();
                String expires_in = results.get("expires_in").toString();
                String refresh_token = results.get("refresh_token").toString();
                syncTaskType.setUrlToken(access_token);
                syncTaskType.setTokenType(token_type);
                syncTaskType.setTokenRefreshKey(refresh_token);
                if (expires_in != null) {
                    syncTaskType.setTokenExpiryDate(addTimeToDate(0, -1, 0, Integer.parseInt(expires_in), new Date()));
                }
                Context.getService(UgandaEMRSyncService.class).saveSyncTaskType(syncTaskType);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return syncTaskType;
    }

    private void processStockOperation(Map results, String operation) {
        StockManagementService stockManagementService = Context.getService(StockManagementService.class);
        StockOperationDTO stockOperationDTO = new StockOperationDTO();
        List items = (List) results.get("data");
        Location recieptLOcation = Context.getLocationService().getLocationByUuid(MAIN_STORE_LOCATION_UUID);
        StockSource stockSource = stockManagementService.getStockSourceByUuid(EAFYA_STOCK_SOURCE_UUID);
        Party partySource = stockManagementService.getPartyByStockSource(stockSource);
        Party partyDestination = stockManagementService.getPartyByLocation(recieptLOcation);
        stockOperationDTO.setOperationType(StockOperationType.RECEIPT);
        stockOperationDTO.setOperationTypeUuid(stockManagementService.getStockOperationTypeByType(StockOperationType.RECEIPT).getUuid());
        stockOperationDTO.setDateCreated(new Date());
        stockOperationDTO.setAtLocationUuid(recieptLOcation.getUuid());
        stockOperationDTO.setAtLocationName(recieptLOcation.getName());
        stockOperationDTO.setApprovalRequired(false);
        stockOperationDTO.setStockOperationItems(processStockOperationItems(items, stockSource));
        stockOperationDTO.setDestinationUuid(partyDestination.getUuid());
        stockOperationDTO.setDestinationName(partyDestination.getLocation().getName());
        stockOperationDTO.setSourceUuid(partySource.getUuid());
        stockOperationDTO.setSourceName(partySource.getStockSource().getName());
        stockOperationDTO.setRemarks("Received for  from eAFYA");
        stockOperationDTO.setLocked(true);
        stockOperationDTO.setOperationDate(new Date());
        stockOperationDTO.setResponsiblePersonUuid(Context.getAuthenticatedUser().getUuid());
        stockOperationDTO.setCreator(Context.getAuthenticatedUser().getUserId());
        stockOperationDTO.setStatus(StockOperationStatus.COMPLETED);
        stockOperationDTO.setCompletedBy(Context.getAuthenticatedUser().getUserId());
        stockOperationDTO.setCompletedDate(new Date());
        if (!stockOperationDTO.getStockOperationItems().isEmpty()) {
            this.stockOperation = stockManagementService.saveStockOperation(stockOperationDTO);
        }
    }

    private List<StockOperationItemDTO> processStockOperationItems(List itemReceived, StockSource stockSource) {
        unproccesedItems = new ArrayList<>();
        processedItems = new ArrayList<>();
        StockManagementService stockManagementService = Context.getService(StockManagementService.class);
        List<StockOperationItemDTO> stockOperationItemDTOS = new ArrayList<>();
        for (Object itemObject : itemReceived) {
            try {
                Map item = ((HashMap) itemObject);
                if (validStockItemFromERP(item)) {
                    try {
                        StockItem stockItem = stockManagementService.getStockItemByReference(stockSource, item.getOrDefault("product_id", null).toString());
                        Concept stockItemUoMConcept = getStockItemUOM(item.get("unit_of_measure").toString());

                        StockItemPackagingUOM stockItemPackagingUOM = null;
                        if (stockItem != null && stockItemUoMConcept != null) {
                            stockItemPackagingUOM = stockManagementService.getStockItemPackagingUOMByConcept(stockItem.getId(), stockItemUoMConcept.getConceptId());
                        }

                        if (stockItem != null && stockItemPackagingUOM != null) {
                            StockOperationItemDTO stockOperationItemDTO = new StockOperationItemDTO();

                            stockOperationItemDTO.setBatchNo(item.get("batch_number").toString());
                            stockOperationItemDTO.setQuantityReceived(BigDecimal.valueOf(Double.parseDouble(item.get("quantity_issued").toString())));
                            stockOperationItemDTO.setQuantity(BigDecimal.valueOf(Double.parseDouble(item.get("quantity_issued").toString())));
                            stockOperationItemDTO.setQuantityReceivedPackagingUOMFactor(stockItemPackagingUOM.getFactor());
                            stockOperationItemDTO.setQuantityReceivedPackagingUOMUuid(stockItemPackagingUOM.getUuid());
                            stockOperationItemDTO.setQuantityReceivedPackagingUOMUoMId(stockItemPackagingUOM.getId());
                            stockOperationItemDTO.setQuantityReceivedPackagingUOMName(stockItemPackagingUOM.getPackagingUom().getDisplayString());
                            if (!item.get("expiry_date").equals("") && stockItem.getHasExpiration()) {
                                stockOperationItemDTO.setHasExpiration(true);
                                stockOperationItemDTO.setExpiration(getDateFromString(item.get("expiry_date").toString(), "yyyy-MM-dd'T'HH:mm:ss.SSSX"));
                            } else {
                                stockOperationItemDTO.setHasExpiration(true);
                                stockOperationItemDTO.setExpiration(addTimeToDate(300, 0, 0, 0, new Date()));
                            }
                            stockOperationItemDTO.setStockItemId(stockItem.getId());
                            stockOperationItemDTO.setStockItemUuid(stockItem.getUuid());
                            stockOperationItemDTO.setStockItemName(stockItem.getCommonName());
                            stockOperationItemDTO.setCommonName(stockItem.getCommonName());
                            stockOperationItemDTO.setPackagingUoMId(stockItemPackagingUOM.getId());
                            stockOperationItemDTO.setStockItemPackagingUOMFactor(stockItemPackagingUOM.getFactor());
                            stockOperationItemDTO.setStockItemPackagingUOMUuid(stockItemPackagingUOM.getUuid());
                            stockOperationItemDTO.setStockItemPackagingUOMName(stockItemPackagingUOM.getPackagingUom().getDisplayString());
                            stockOperationItemDTO.setStockItemConceptId(stockItem.getConcept().getConceptId());
                            if (stockItem.getIsDrug()) {
                                stockOperationItemDTO.setStockItemDrugId(stockItem.getDrug().getDrugId());
                            }
                            stockOperationItemDTOS.add(stockOperationItemDTO);
                            processedItems.add(item);
                        } else {
                            unproccesedItems.add(item);
                        }
                    } catch (Exception exception) {
                        log.error(exception);
                    }
                } else {
                    unproccesedItems.add(item);
                }
            } catch (Exception exception) {
                log.error(exception);
            }
        }
        return stockOperationItemDTOS;
    }

    private Boolean validStockItemFromERP(Map item) {
        if (item.get("batch_number").equals("") || item.get("expiry_date").equals("") || item.get("quantity_issued").equals("") || item.get("unit_of_measure").equals("") || item.get("product_id").equals("")) {
            return false;
        }
        return true;
    }

    private Concept getStockItemUOM(String packageUnit) {
        ConceptService conceptService = Context.getConceptService();
        switch (packageUnit.toLowerCase()) {
            case "pieces":
                return conceptService.getConcept(PIECES);
            case "tablets":
            case "tab":
                return conceptService.getConcept(TABLETS);
            case "vial":
                return conceptService.getConcept(VIAL);
            case "capsule":
                return conceptService.getConcept(CAPSULE);
            case "packs":
                return conceptService.getConcept(PACKS);
            case "ampoule":
                return conceptService.getConcept(AMPOULE);
            case "bottle":
                return conceptService.getConcept(BOTTLE);
            case "litres":
                return conceptService.getConcept(LITERS);
            case "blister":
                return conceptService.getConcept(BLISTER);
            case "box":
                return conceptService.getConcept(BOX);
            case "tin":
                return conceptService.getConcept(TIN);
            case "cap":
                return conceptService.getConcept(CAP);
            case "gram":
                return conceptService.getConcept(GRAM);
            case "amule":
                return conceptService.getConcept(AMPULE);
            case "bar":
                return conceptService.getConcept(BAR);
            case "can":
                return conceptService.getConcept(CAN);
            case "kilogram":
                return conceptService.getConcept(KILOGRAM);
            case "tube":
                return conceptService.getConcept(TUBE);
            case "bag":
                return conceptService.getConcept(BAG);
            case "packet":
                return conceptService.getConcept(PACKET);
            case "pessary":
                return conceptService.getConcept(PESSARY);
            case "pack":
                return conceptService.getConcept(PACK);
            case "cylinder":
                return conceptService.getConcept(CYLINDER);
            case "cycle":
                return conceptService.getConcept(CYCLE);
            default:
                return null;
        }

    }

    public Date getDateFromString(String dateString, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        try {
            return dateFormat.parse(dateString);
        } catch (Exception e) {
            log.error(e);
        }
        return new Date();
    }

    private Date addTimeToDate(Integer days, Integer hours, Integer minutes, Integer seconds, Date currentDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        if (seconds != null && seconds != 0) {
            calendar.add(Calendar.SECOND, seconds);
        }

        if (minutes != null && minutes != 0) {
            calendar.add(Calendar.MINUTE, minutes);
        }

        if (hours != null && hours != 0) {
            calendar.add(Calendar.HOUR_OF_DAY, hours);
        }

        if (days != null && days != 0) {
            calendar.add(Calendar.DAY_OF_MONTH, days);
        }
        return calendar.getTime();
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String getProviderAttributeByType(Set<ProviderAttribute> providerAttributes, ProviderAttributeType providerAttributeType) {
        for (ProviderAttribute providerAttribute : providerAttributes) {
            if (providerAttributeType.equals(providerAttribute.getAttributeType())) {
                return providerAttribute.getValue().toString();
            }
        }
        return null;
    }

    /**
     * Generates a list of drug orders formatted for export to another system.
     * The method collects active drug orders for a specified set of drugs and formats them into EAFYA-compatible JSON objects.
     * <p>
     * The process involves:
     * - Fetching the care setting, order type, and drug orders.
     * - Processing the drug orders and translating them to the required format.
     * - Adding clinic and provider information to the orders.
     * The result is a list of JSON objects representing patient orders with associated drug information.
     *
     * @param drugs A collection of drug concepts to filter the orders by.
     * @return A list of JSON objects representing patient orders with drug prescriptions.
     */
    public List<ObjectNode> generateDrugOrderToOtherSystem(Collection<Concept> drugs) {
        List<ObjectNode> patientOrders = new ArrayList<>();

        OrderService orderService = Context.getOrderService();
        CareSetting careSetting = orderService.getCareSettingByUuid(CARE_SETTING_UUID_OPD);
        OrderType orderType = orderService.getOrderTypeByUuid(ORDER_TYPE_DRUG_UUID);

        OrderSearchCriteria criteria = new OrderSearchCriteria(
                null,
                careSetting,
                drugs,
                Collections.singletonList(orderType),
                null,
                null,
                OpenmrsUtil.getLastMomentOfDay(new Date()),
                OpenmrsUtil.firstSecondOfDay(new Date()),
                false,
                null,
                null,
                Order.Action.NEW,
                Order.FulfillerStatus.IN_PROGRESS,
                true,
                true,
                true,
                false
        );

        List<Order> orders = orderService.getOrders(criteria);
        List<Integer> syncedEncounterIds = extractEncounterIdFromSyncTasks(eAFYATaskListForToday);

        // Filter unique drug order encounters not already synced
        Set<Encounter> uniqueEncounters = new HashSet<>();
        for (Order order : orders) {
            Encounter encounter = order.getEncounter();
            if (order.isActive() && encounter != null && !syncedEncounterIds.contains(encounter.getEncounterId())) {
                uniqueEncounters.add(encounter);
            }
        }

        ObjectMapper mapper = new ObjectMapper();

        for (Encounter encounter : uniqueEncounters) {
            ObjectNode patientOrder = mapper.createObjectNode();

            try {
                // Translate patient details
                translatePatientToEAFYAFormat(encounter.getPatient(), patientOrder);

                // Translate drug order details
                translateDrugOrderToEAFYAFormat(encounter.getOrders(), patientOrder);

                // Add clinic and provider information
                addClinicAndProviderInformation(encounter, patientOrder);

                // Ensure minimum data requirements
                if (patientOrder.has("prescription")
                        && patientOrder.get("prescription").isArray()
                        && patientOrder.get("prescription").size() > 0
                        && patientOrder.path("patient").has("id")) {

                    patientOrders.add(patientOrder);
                }

            } catch (Exception e) {
                log.warn("Skipping encounter " + encounter.getEncounterId() + " due to error: ", e);
            }
        }

        return patientOrders;
    }


    /**
     * Extracts the encounter ID from a given input string.
     * <p>
     * The method looks for a pattern in the format "encounterId: <number>" and returns
     * the numeric part as a string. If the pattern is not found, it returns {@code null}.
     * </p>
     *
     * @param syncTasks the input string containing the encounter information
     * @return the extracted encounter ID as a string, or {@code null} if not found
     */
    public List<Integer> extractEncounterIdFromSyncTasks(List<SyncTask> syncTasks) {
        List<Integer> encounterIds = new ArrayList<>();
        for (SyncTask syncTask : syncTasks) {
            try {
                encounterIds.add(Integer.parseInt(syncTask.getSyncTask()));
            } catch (Exception e) {
                log.error(e);
            }
        }
        return encounterIds; // If no encounter ID is found
    }

    /**
     * Translates a set of OpenMRS drug orders into the EAFYA prescription format and adds them to the given patient order JSON object.
     * Each valid drug order is converted into a JSON object with standardized fields and added to a "prescription" array.
     * Only drug orders with a valid stock item reference are included.
     *
     * @param orders       A set of orders (possibly including drug orders).
     * @param patientOrder The patient-level JSON object to which prescription details will be added.
     * @return The updated patient order JSON object containing the "prescription" array.
     */
    private ObjectNode translateDrugOrderToEAFYAFormat(Set<Order> orders, ObjectNode patientOrder) {
        if (orders == null || orders.isEmpty()) {
            return patientOrder;
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode prescriptionArray = mapper.createArrayNode();

        for (Order order : orders) {
            if (order instanceof DrugOrder) {
                DrugOrder drugOrder = (DrugOrder) order;
                String drugReferenceCode = getPharmacology(getStockItemReferenceFromDrug(drugOrder.getDrug()));

                if (drugReferenceCode != null) {
                    ObjectNode patientDrugOrder = mapper.createObjectNode();

                    patientDrugOrder.put("drug_name",
                            drugOrder.getDrug() != null ? drugOrder.getDrug().getName() : "");

                    patientDrugOrder.put("drug_id", drugReferenceCode);

                    patientDrugOrder.put("dosage", String.valueOf(drugOrder.getDose() != null ? drugOrder.getDose() : ""));

                    patientDrugOrder.put("duration", String.valueOf(drugOrder.getDuration() != null ? drugOrder.getDuration() : ""));

                    patientDrugOrder.put("frequency",
                            drugOrder.getFrequency() != null ? drugOrder.getFrequency().getName() : "");

                    patientDrugOrder.put("route",
                            (drugOrder.getRoute() != null && drugOrder.getRoute().getName() != null)
                                    ? drugOrder.getRoute().getName().getName() : "");

                    prescriptionArray.add(patientDrugOrder);
                }
            }
        }

        // Attach the prescription array to the patient order JSON
        patientOrder.set("prescription", prescriptionArray);
        return patientOrder;
    }

    private String getPharmacology(String referenceCode) {

        if (referenceCode == null) {
            return null;
        }

        if (productCatelogList == null || productCatelogList.isEmpty()) {
            return null;
        }
        for (Object object : productCatelogList) {
            try {
                ObjectMapper objectMapper=new ObjectMapper();
                JsonNode jsonObject =objectMapper.readTree(object.toString());

                if (jsonObject.has("product_id") &&
                        jsonObject.has("pharmacology_id") &&
                        referenceCode.equals(jsonObject.get("product_id"))) {

                    return jsonObject.get("pharmacology_id").asText();
                }
            } catch (Exception e) {
                log.warn("Invalid object in productCatalogList: " + object, e);
            }
        }

        return null;
    }


    /**
     * Translates an OpenMRS Patient object into the EAFYA-compliant JSON structure.
     * Ensures that all fields are populated with non-null values, using empty strings where applicable.
     *
     * @param patient The OpenMRS patient object containing demographic and identifier information.
     * @param patientOrder A JSON object representing an existing patient order to which patient data will be added.
     * @return A JSONObject containing the patientOrder enriched with patient demographic and identifier data.
     */
    /**
     * Translates patient data into the EAFYA-compatible format for export.
     * Handles demographic details, identifiers, and address information, with null checks for missing data.
     * If the OPD identifier is valid and contains "UG-", patient demographics are added; otherwise, only OPD identifier is used.
     *
     * @param patient      The patient whose data is to be translated.
     * @param patientOrder The JSON object where the patient data will be added.
     * @return The updated JSON object with patient information.
     */
    /**
     * Translates patient data into the EAFYA format and attaches it to the provided patientOrder.
     * The method checks for the validity of the OPD number. If OPD number is null or doesn't contain "UG-",
     * it generates full patient details. Otherwise, it uses the OPD number for patient identification.
     *
     * @param patient      The patient whose data is being translated.
     * @param patientOrder The JSONObject to which the translated patient data will be added.
     * @return The updated patientOrder containing the patient data in EAFYA format.
     */
    private ObjectNode translatePatientToEAFYAFormat(Patient patient, ObjectNode patientOrder) {
        PatientService patientService = Context.getPatientService();

        PatientIdentifier opdNumber = patient.getPatientIdentifier(
                patientService.getPatientIdentifierTypeByUuid(OPD_IDENTIFIER_TYPE_UUID));
        PatientIdentifier artNo = patient.getPatientIdentifier(
                patientService.getPatientIdentifierTypeByUuid(HIV_CLINIC_IDENTIFIER_TYPE_UUID));
        PatientIdentifier openmrsId = patient.getPatientIdentifier(
                patientService.getPatientIdentifierTypeByUuid(OPENMRS_IDENTIFIER_TYPE_UUID));
        PatientIdentifier nationalId = patient.getPatientIdentifier(
                patientService.getPatientIdentifierTypeByUuid(NATIONAL_ID_IDENTIFIER_TYPE_UUID));

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode patientObject = mapper.createObjectNode();

        if (opdNumber == null || (opdNumber.getIdentifier() != null && !opdNumber.getIdentifier().contains("UG-"))) {
            // Full patient details when OPD number is invalid or missing
            patientObject.put("birth_date", patient.getBirthdate() != null ? patient.getBirthdate().toString() : "");
            patientObject.put("first_name", patient.getGivenName() != null ? patient.getGivenName() : "");
            patientObject.put("last_name", patient.getFamilyName() != null ? patient.getFamilyName() : "");
            patientObject.put("gender", convertGenderToFullWord(patient.getGender() != null ? patient.getGender() : ""));

            // Address
            PersonAddress address = patient.getPersonAddress();
            patientObject.put("city", (address != null && address.getCountyDistrict() != null) ? address.getCountyDistrict() : "");
            patientObject.put("village", (address != null && address.getAddress5() != null) ? address.getAddress5() : "Not available");

            // Marital Status
            PersonAttribute maritalStatus = patient.getAttribute(Context.getPersonService()
                    .getPersonAttributeTypeByUuid(MARITAL_STATUS_ATTRIBUTE_TYPE));
            patientObject.put("marital_status", (maritalStatus != null && maritalStatus.getValue() != null) ? maritalStatus.getValue() : "");

            // Phone Number
            PersonAttribute phoneNo = patient.getAttribute(Context.getPersonService()
                    .getPersonAttributeTypeByUuid(PHONE_NO_ATTRIBUTE_TYPE));
            patientObject.put("phone_number", (phoneNo != null && phoneNo.getValue() != null) ? phoneNo.getValue() : "");

            // Identifiers
            String patientId = "";
            if (artNo != null && artNo.getIdentifier() != null) {
                patientId = artNo.getIdentifier();
            } else if (opdNumber != null && opdNumber.getIdentifier() != null) {
                patientId = opdNumber.getIdentifier();
            }

            patientObject.put("patient_id", patientId);
            patientObject.put("id", patientId);
            patientObject.put("id_no", (nationalId != null && nationalId.getIdentifier() != null) ? nationalId.getIdentifier() : "");
            patientObject.put("internal_patient_id", (openmrsId != null && openmrsId.getIdentifier() != null) ? openmrsId.getIdentifier() : "");

        } else {
            // Use only OPD number if it contains "UG-"
            String opdId = opdNumber.getIdentifier() != null ? opdNumber.getIdentifier() : "";
            patientObject.put("patient_id", opdId);
            patientObject.put("id", opdId);
            patientObject.put("internal_patient_id", (openmrsId != null && openmrsId.getIdentifier() != null) ? openmrsId.getIdentifier() : "");
        }

        // Attach the patientObject to the patientOrder JSON
        if (patientOrder instanceof ObjectNode) {
            ((ObjectNode) patientOrder).set("patient", patientObject);
        }

        return patientOrder;
    }


    /**
     * Converts a single-character gender code to its full word equivalent.
     *
     * @param gender A string representing gender, typically "M" for male or "F" for female.
     * @return "Male" if input is "M", "Female" if input is "F", or the original input if null or unrecognized.
     */
    private String convertGenderToFullWord(String gender) {
        if (gender == null) {
            return null;
        }

        switch (gender) {
            case "M":
                return "Male";
            case "F":
                return "Female";
            default:
                return gender;
        }
    }


    /**
     * Adds clinic ID and provider-specific EAFYA ID information to the given JSON object
     * based on the details of the provided encounter.
     *
     * @param encounter  The encounter containing metadata about the provider.
     * @param orderObject The JSON object to which clinic and provider information will be added.
     * @return The updated JSON object with clinic and provider EAFYA identifiers.
     */
    /**
     * Adds clinic ID and provider-specific EAFYA ID information to the given JSON object
     * based on the details of the provided encounter.
     *
     * @param encounter    The encounter containing metadata about the provider.
     * @param patientOrder The JSON object to which clinic and provider information will be added.
     * @return The updated JSON object with clinic and provider EAFYA identifiers.
     * @throws IllegalStateException if either the clinic ID or provider EAFYA ID is missing.
     */
    private ObjectNode addClinicAndProviderInformation(Encounter encounter, ObjectNode patientOrder) {
        // Retrieve the provider attribute type used for the EAFYA ID
        ProviderAttributeType providerAttributeType = Objects.requireNonNull(
                Context.getProviderService().getProviderAttributeTypeByUuid("d376f27c-cb93-45b4-be0a-6be88c520233"),
                "ProviderAttributeType (EAFYA ID) is required"
        );

        // Retrieve the clinic ID from global properties
        String clinicId = Context.getAdministrationService()
                .getGlobalProperty(MODULE_ID + ".eafya.clinicid");

        // Validate clinic ID
        if (clinicId == null || clinicId.trim().isEmpty()) {
            throw new IllegalStateException("Clinic ID global property (" + MODULE_ID + ".eafya.clinicid) is not configured.");
        }

        // Find the provider linked to the encounter's creator
        Provider provider = fetchProviderForUser(encounter.getCreator().getUserId());

        // Extract the EAFYA ID
        String eafyaId = getProviderAttributeByType(provider.getAttributes(), providerAttributeType);

        if (eafyaId == null || eafyaId.trim().isEmpty()) {
            throw new IllegalStateException("Provider EAFYA ID attribute is missing for provider: " + provider.getName());
        }

        // Add to JSON
        patientOrder.put("clinic_id", clinicId);
        patientOrder.put("created_by_id", eafyaId);
        patientOrder.put("encounter_id", encounter.getEncounterId());

        return patientOrder;
    }


    /**
     * Retrieves the first provider associated with a given encounter under a specific encounter role.
     *
     * @param encounter The encounter from which to retrieve the provider.
     * @return The first matching Provider if available; otherwise, null.
     */
    public Provider getProviderFromEncounter(Encounter encounter) {
        // Retrieve the specified encounter role using its UUID
        EncounterRole encounterRole = Context.getEncounterService().getEncounterRoleByUuid(ENCOUNTER_ROLE);

        // Get the providers assigned to the encounter under the specified role
        Set<Provider> providers = encounter.getProvidersByRole(encounterRole);

        // Return the first provider if available, otherwise return null
        return providers.stream().filter(provider -> !provider.getRetired()).findFirst().orElse(null);
    }


    private String getStockItemReferenceFromDrug(Drug drug) {
        List<StockItem> stockItem = Context.getService(StockManagementService.class).getStockItemByDrug(drug.getDrugId());
        if (!stockItem.isEmpty()) {
            List<StockItemReference> stockItemReferences = stockItem.get(0).getReferences().stream().filter(reference -> reference.getReferenceSource().getUuid().equals("9babcc02-fc0b-11ef-ab84-28977ca9db4b")).collect(Collectors.toList());
            if (!stockItemReferences.isEmpty()) {
                return stockItemReferences.get(0).getStockReferenceCode();
            }
        }
        return null;
    }

    /**
     * Sends prescription data to an external system (eAFYA) by:
     * - Retrieving drug orders based on specific concepts
     * - Matching each drug order to a patient
     * - Posting each prescription to the external system
     * - Updating the patient's OPD number based on the response
     */
    public void sendPrescription() {
        // Retrieve services
        UgandaEMRSyncService syncService = Context.getService(UgandaEMRSyncService.class);
        PatientService patientService = Context.getPatientService();
        UgandaEMRHttpURLConnection httpConnection = new UgandaEMRHttpURLConnection();

        // Fetch SyncTaskType by its configured UUID
        SyncTaskType syncTaskType = syncService.getSyncTaskTypeByUUID("8ca0ffd0-0fb0-11f0-9e19-da924fd23489");
        if (syncTaskType == null) {
            log.error("SyncTaskType with UUID '8ca0ffd0-0fb0-11f0-9e19-da924fd23489' not found.");
            return;
        }

        // Construct the full endpoint URL
        String apiPath = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.SendPrescription");
        String url = syncTaskType.getUrl() + apiPath;

        // Load the eAFYA product catalog to map drugs
        productCatelogList = getProductCatalogFromEAFYA(syncTaskType);
        // Lists of eAFYA Tasks logs generated today
        eAFYATaskListForToday = getSyncTasksByType(syncTaskType, OpenmrsUtil.firstSecondOfDay(new Date()), OpenmrsUtil.getLastMomentOfDay(new Date()));

        if (productCatelogList == null || productCatelogList.isEmpty()) {
            log.warn("Product catalog from eAFYA is empty or unavailable.");
            return;
        }

        // Parse configured concept IDs
        String[] conceptIdArray = syncTaskType.getDataTypeId() != null
                ? syncTaskType.getDataTypeId().split(",")
                : new String[0];

        List<Concept> concepts = Arrays.stream(conceptIdArray)
                .map(Context.getConceptService()::getConcept)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Generate prescription orders
        List<ObjectNode> drugOrders = generateDrugOrderToOtherSystem(concepts);

        try {
            for (ObjectNode drugOrder : drugOrders) {
                // Send prescription via POST
                Map<String, Object> response = httpConnection.sendPostBy(
                        url,
                        syncTaskType.getUrlUserName(),
                        syncTaskType.getUrlPassword(),
                        null,
                        drugOrder.toString(),
                        false
                );

                int responseCode = response.get("responseCode") instanceof Integer
                        ? (int) response.get("responseCode")
                        : Integer.parseInt(response.get("responseCode").toString());

                if (responseCode == 200 || responseCode == 201) {
                    String internalPatientId = drugOrder.has("internal_patient_id") && !drugOrder.get("internal_patient_id").isNull()
                            ? drugOrder.get("internal_patient_id").asText()
                            : null;
                    String externalPatientId = extractPatientIdFromResponse(response);

                    if (internalPatientId != null && !externalPatientId.isEmpty()) {
                        updatePatientOPDNumber(internalPatientId, externalPatientId);
                    }

                    // Log successful sync
                    logTransaction(syncTaskType, responseCode, null, drugOrder.get("encounter_id").toString(), "Patient: " + externalPatientId + "'s Prescription has been created in eAFYA. eAFYA Server responded back with message: (" + response.get("responseMessage").toString() + ")", new Date(), url, false, false);
                    log.info(String.format("Prescription for patient %s synced successfully. External ID: %s",
                            internalPatientId, externalPatientId));
                } else {
                    // Log failure
                    log.error("Failed to sync prescription. Response code: " + responseCode);
                    logTransaction(syncTaskType, responseCode, null, "Failed Prescription Sync",
                            response.get("responseMessage").toString(), new Date(), url, false, false);
                }
            }
        } catch (Exception e) {
            log.error("Error while syncing prescription data: ", e);
        }
    }

    @Override
    public List<Map<String, String>> generateAndSyncBulkViralLoadRequest() {
        List<Map<String, String>> responses = new ArrayList<>();
        UgandaEMRHttpURLConnection httpConnection = new UgandaEMRHttpURLConnection();
        UgandaEMRSyncService syncService = Context.getService(UgandaEMRSyncService.class);
        List<Order> orders;

        // Check internet connectivity
        if (!httpConnection.isConnectionAvailable()) {
            Map<String, String> noConnectionResponse = new HashMap<>();
            noConnectionResponse.put("message", "No internet connection. Unable to send orders to CPHL.");
            responses.add(noConnectionResponse);
            return responses;
        }

        // Attempt to fetch orders
        try {
            orders = getOrders();
        } catch (IOException | ParseException e) {
            log.error("Error retrieving orders", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error retrieving orders: " + e.getMessage());
            responses.add(errorResponse);
            return responses;
        }
        // Process each order
        for (Order order : orders) {
            responses.add(sendSingleViralLoadOrder(order));
        }

        return responses;
    }

    public Map<String, String> generateVLFHIRResultRequestBody(String jsonRequestString, String healthCenterCode, String patientIdentifier, String sampleIdentifier) {
        Map<String, String> jsonMap = new HashMap<>();
        String filledJsonFile = "";
        filledJsonFile = String.format(jsonRequestString, healthCenterCode, patientIdentifier, sampleIdentifier);
        jsonMap.put("json", filledJsonFile);
        return jsonMap;
    }


    @Override
    public Map sendSingleViralLoadOrder(Order order) {
        Map response = new HashMap<>();
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);

        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            response.put("responseMessage", "No Internet Connection to send order" + order.getAccessionNumber());
            return response;
        }

        if (!isOrderSynced(order, syncTaskType)) {
            if (order != null && order.getAccessionNumber() != null && !isValidCPHLBarCode(order.getAccessionNumber())) {
                String message = String.format("Order: %s does not have valid bar code.", order.getAccessionNumber());
                logTransaction(syncTaskType, 500, message, order.getAccessionNumber(), message, new Date(), syncTaskType.getUrl(), false, false);
                response.put("responseMessage", message);
                return response;
            }

            String payload = processResourceFromOrder(order);

            if (payload != null) {
                if (!validateVLFHIRBundle(payload)) {
                    String missingObsInPayload = String.format(
                            "Order: %s is not valid due to missing %s in the required field",
                            order.getAccessionNumber(),
                            getMissingVLFHIRCodesAsString(payload)
                    );
                    logTransaction(syncTaskType, 500, missingObsInPayload, order.getAccessionNumber(),
                            missingObsInPayload,
                            new Date(), syncTaskType.getUrl(), false, false);
                    response.put("responseMessage", missingObsInPayload);
                } else {
                    response = sendViralLoadToCPHL(syncTaskType, payload, ugandaEMRHttpURLConnection, order);
                }
            } else {
                String error = "UgandaEMR Internal Server error. There was an error processing order: " + order.getAccessionNumber();
                response.put("responseMessage", error);
                logTransaction(syncTaskType, 500, error, order.getAccessionNumber(), error, new Date(), syncTaskType.getUrl(), false, false);
            }
        } else {
            response.put("responseMessage", "Order: " + order.getAccessionNumber() + " is already synced");
        }
        return response;
    }


    @Override
    public Map requestLabResult(Order order, SyncTask syncTask) {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
        Map response = new HashMap<>();
        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            response.put("responseMessage", "No Internet Connection to send order" + order.getAccessionNumber());
            return response;
        }

        if (order == null && syncTask != null) {
            order = getOrderByAccessionNumber(syncTask.getSyncTask());
            if (order == null) {
                response.put("responseMessage", "Order Not found for accession number: " + syncTask.getSyncTask());
                log.info("Order Not found for accession number: " + syncTask.getSyncTask());
                return response;
            }
        }

        String dataOutput = generateVLFHIRResultRequestBody(VL_RECEIVE_RESULT_FHIR_JSON_STRING, getHealthCenterCode(), getPatientIdentifier(order.getEncounter().getPatient(), PATIENT_IDENTIFIER_TYPE), String.valueOf(syncTask.getSyncTask())).get("json");

        Map results = new HashMap();

        SyncTaskType syncTaskType = getSyncTaskTypeByUUID(VIRAL_LOAD_RESULT_PULL_TYPE_UUID);

        try {
            results = ugandaEMRHttpURLConnection.sendPostBy(syncTaskType.getUrl(), syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), "", dataOutput, false);
        } catch (Exception e) {
            log.error("Failed to fetch results", e);
            logTransaction(syncTaskType, 500, e.getMessage(), order.getAccessionNumber(), e.getMessage(), new Date(), syncTaskType.getUrl(), false, false);
            response.put("responseMessage", e.getMessage());
        }

        Integer responseCode = null;
        String responseMessage = null;

        // Parsing responseCode and responseMessage
        if (results.containsKey("responseCode") && results.containsKey("responseMessage")) {
            responseCode = Integer.parseInt(results.get("responseCode").toString());
            responseMessage = results.get("responseMessage").toString();
            response.put("responseMessage", responseMessage);
        }

        // Processing results if responseCode is valid and status is not pending
        if (responseCode != null && (responseCode == 200 || responseCode == 201) && !results.isEmpty() && results.containsKey("status") && !results.get("status").equals("pending")) {
            Map reasonReference = (Map) results.get("reasonReference");
            ArrayList<Map> result = (ArrayList<Map>) reasonReference.get("result");

            // Saving Viral Load Results
            if (order.getEncounter() != null && !result.isEmpty()) {
                Object qualitativeResult = result.get(0).get("valueString");
                Object quantitativeResult = result.get(0).get("valueInteger");

                if (quantitativeResult != null && qualitativeResult != null) {
                    try {
                        addVLToEncounter(qualitativeResult.toString(), quantitativeResult.toString(), order.getEncounter().getEncounterDatetime().toString(), order.getEncounter(), order);
                        syncTask.setActionCompleted(true);
                        saveSyncTask(syncTask);
                        logTransaction(syncTaskType, responseCode, result.get(0).get("valueString").toString(), order.getAccessionNumber(), result.get(0).get("valueString").toString(), new Date(), syncTaskType.getUrl(), false, false);
                        try {
                            Context.getOrderService().updateOrderFulfillerStatus(order, Order.FulfillerStatus.COMPLETED, result.get(0).get("valueString").toString());
                            Context.getOrderService().discontinueOrder(order, "Completed", new Date(), order.getOrderer(), order.getEncounter());
                        } catch (Exception e) {
                            log.error("Failed to discontinue order", e);
                            response.put("responseMessage", String.format("Failed to discontinue order %s", e.getMessage()));
                        }
                    } catch (Exception e) {
                        log.error("Failed to add results to patient encounter", e);
                        logTransaction(syncTaskType, 500, e.getMessage(), order.getAccessionNumber(), e.getMessage(), new Date(), syncTaskType.getUrl(), false, false);
                        response.put("responseMessage", String.format("Failed to add results to patient encounter %s", e.getMessage()));
                    }
                } else {
                    logTransaction(syncTaskType, 500, "Internal server error: Results of Viral load have a null value", order.getAccessionNumber(), "Internal server error: Results of Viral load have a null value", new Date(), syncTaskType.getUrl(), false, false);

                    response.put("responseMessage", String.format("Internal server error: Results of Viral load order %s have a null value", order.getAccessionNumber()));
                }
            }
        } else {
            // Logging based on responseCode or status
            if (responseCode != null && !results.containsKey("status")) {
                String detailedResponseMessage = String.format("CPHL Server Response for order: %s while fetching results:  %s", order.getAccessionNumber(), responseMessage);
                logTransaction(syncTaskType, responseCode, detailedResponseMessage, order.getAccessionNumber(), detailedResponseMessage, new Date(), syncTaskType.getUrl(), false, false);
                response.put("responseMessage", detailedResponseMessage);
            } else if (results.containsKey("status")) {

                String detailedResponseMessage = String.format("CPHL Response : Results for order: %s are %s", order.getAccessionNumber(), results.get("status").toString());
                logTransaction(syncTaskType, responseCode, detailedResponseMessage, order.getAccessionNumber(), detailedResponseMessage, new Date(), syncTaskType.getUrl(), false, false);
                response.put("responseMessage", detailedResponseMessage);
            }
        }
        return response;
    }


    public Order getOrderByAccessionNumber(String accessionNumber) {
        OrderService orderService = Context.getOrderService();
        List list = Context.getAdministrationService().executeSQL(String.format(VIRAL_LOAD_ORDER_QUERY, accessionNumber), true);
        if (list.size() > 0) {
            for (Object o : list) {
                return orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
            }
        }
        return null;
    }

    private boolean isOrderSynced(Order order, SyncTaskType syncTaskType) {

        boolean isOrderSynced = true;

        List<SyncTask> allSyncTasks = getSyncTasksByType(syncTaskType);
        List<SyncTask> syncTasks = allSyncTasks.stream().filter(syncTask -> order.getAccessionNumber().equals(syncTask.getSyncTask()) && syncTaskType.equals(syncTask.getSyncTaskType()) && (syncTask.getStatusCode() == 200 || syncTask.getStatusCode() == 201)).collect(Collectors.toList());
        if (syncTasks.size() < 1) {
            isOrderSynced = false;
        }
        return isOrderSynced;
    }

    public List<Order> getOrders() throws IOException, ParseException {
        OrderService orderService = Context.getOrderService();
        List<Order> orders = new ArrayList<>();
        List list = Context.getAdministrationService().executeSQL(VIRAL_LOAD_ORDERS_QUERY, true);
        if (list.size() > 0) {
            for (Object o : list) {
                Order order = orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
                if (order.getAccessionNumber() != null && order.isActive() && order.getInstructions().equalsIgnoreCase("REFER TO cphl")) {
                    orders.add(order);
                }
            }
        }
        return orders;
    }


    private Map sendViralLoadToCPHL(SyncTaskType syncTaskType, String payload, UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection, Order order) {
        Map<String, Object> syncResponse = new HashMap<>();

        try {
            syncResponse = ugandaEMRHttpURLConnection.sendPostBy(
                    syncTaskType.getUrl(),
                    syncTaskType.getUrlUserName(),
                    syncTaskType.getUrlPassword(),
                    "",
                    payload,
                    false
            );

            if (syncResponse != null) {
                String responseFromCPHLServer = String.format("Response From CPHL Server: %s", syncResponse.get("responseMessage"));
                Map responseType = handleReturnedResponses(order, syncResponse);
                int responseCode = Integer.parseInt(syncResponse.getOrDefault("responseCode", "500").toString());

                // Handle duplicate case with a 400 response
                if (responseCode == 400 && "Duplicate".equalsIgnoreCase(String.valueOf(responseType.get("responseType")))) {
                    responseCode = 200;
                    responseFromCPHLServer = String.format("Response From CPHL Server: %s", responseType.get("responseMessage"));
                }

                boolean isSuccess = responseCode == 200 || responseCode == 201 || responseCode == 202 || responseCode == 208;
                String accessionNumber = order.getAccessionNumber();
                String url = syncTaskType.getUrl();

                if (isSuccess) {
                    Context.getOrderService().updateOrderFulfillerStatus(order, Order.FulfillerStatus.RECEIVED, responseFromCPHLServer);
                }

                logTransaction(syncTaskType, responseCode, responseFromCPHLServer, accessionNumber, responseFromCPHLServer, new Date(), url, isSuccess, false);

                syncResponse.put("responseMessage", responseFromCPHLServer);
            }

        } catch (Exception e) {
            String errorMessage = "UgandaEMR Internal Server error. There was an error processing order: " + e.getMessage();
            log.error("Failed to create sync task", e);
            syncResponse.put("responseMessage", errorMessage);

            logTransaction(syncTaskType, 500, errorMessage, order.getAccessionNumber(), errorMessage, new Date(), syncTaskType.getUrl(), false, false);
        }

        return syncResponse;
    }

    private Map handleReturnedResponses(Order order, Map response) {
        Map responseType = new HashMap<>();
        OrderService orderService = Context.getOrderService();
        try {
            if (response.get("responseCode").equals(400) && response.get("responseMessage").toString().contains("The specimen ID:") && response.get("responseMessage").toString().contains("is not HIE compliant")) {
                orderService.discontinueOrder(order, response.get("responseMessage").toString(), new Date(), order.getOrderer(), order.getEncounter());
                responseType.put("responseType", "Not HIE compliant");
                responseType.put("responseMessage", response.get("responseMessage").toString().contains("is not HIE compliant"));
            } else if (response.get("responseCode").equals(400) && response.get("responseMessage").toString().toLowerCase().contains("duplicate")) {
                Context.getOrderService().updateOrderFulfillerStatus(order, Order.FulfillerStatus.RECEIVED, String.valueOf(response.get("responseMessage").toString()));
                responseType.put("responseType", "Duplicate");
                responseType.put("responseMessage", response.get("responseMessage"));
            }
        } catch (Exception e) {
            log.error(e);
            responseType.put("responseMessage", "Failed to handle response for CPHL");
        }

        return responseType;
    }


    private String addResourceToBundle(String resourceString) {

        return resourceString;
    }


    private String processResourceFromOrder(Order order) {
        SyncFHIRRecord syncFHIRRecord = new SyncFHIRRecord();
        Collection<String> resources = new ArrayList<>();
        String finalCaseBundle = null;

        List<Order> orderList = new ArrayList<>();
        orderList.add(order);
        List<Encounter> encounter = new ArrayList<>();

        List<PatientIdentifier> patientArrayList = new ArrayList<>();
        patientArrayList.add(order.getPatient().getPatientIdentifier());

        List<Person> personList = new ArrayList<>();
        personList.add(order.getPatient().getPerson());

        String specimenSource = generateSpecimen(order);

        encounter.add(order.getEncounter());

        resources.addAll(addSpecimenSource(syncFHIRRecord.groupInCaseBundle("ServiceRequest", syncFHIRRecord.getServiceRequestResourceBundle(orderList), "HIV Clinic No."), order));

        resources.addAll(syncFHIRRecord.groupInCaseBundle("Encounter", syncFHIRRecord.getEncounterResourceBundle(encounter), "HIV Clinic No."));

        resources.addAll(syncFHIRRecord.groupInCaseBundle("Patient", syncFHIRRecord.getPatientResourceBundle(null, patientArrayList, null), "HIV Clinic No."));

        resources.addAll(syncFHIRRecord.groupInCaseBundle("Practitioner", syncFHIRRecord.getPractitionerResourceBundle(null, encounter, orderList), "HIV Clinic No."));

        resources.addAll(syncFHIRRecord.groupInCaseBundle("Observation", syncFHIRRecord.getObservationResourceBundle(null, encounter, personList), "HIV Clinic No."));

        if (specimenSource != null) {
            resources.add(specimenSource);
        }

        if (!resources.isEmpty()) {
            finalCaseBundle = String.format(FHIR_BUNDLE_CASE_RESOURCE_TRANSACTION, resources.toString());
        }
        return finalCaseBundle;
    }

    private Collection<String> addSpecimenSource(Collection<String> serviceRequests, Order order) {
        ObjectMapper objectMapper = new ObjectMapper();
        TestOrder testOrder = (TestOrder) order;
        Collection<String> serviceRequestList = new ArrayList<>();

        for (String serviceRequest : serviceRequests) {
            try {
                if (testOrder.getAccessionNumber() != null) {
                    JsonNode node = objectMapper.readTree(serviceRequest);

                    if (node.isObject()) {
                        ObjectNode jsonObject = (ObjectNode) node;
                        ObjectNode resourceNode = (ObjectNode) jsonObject.get("resource");

                        ArrayNode specimenArray = objectMapper.createArrayNode();
                        ObjectNode specimenObject = objectMapper.createObjectNode();
                        specimenObject.put("reference", "Specimen/" + testOrder.getAccessionNumber());
                        specimenArray.add(specimenObject);

                        resourceNode.set("specimen", specimenArray);

                        serviceRequestList.add(objectMapper.writeValueAsString(jsonObject));
                    } else {
                        serviceRequestList.add(serviceRequest);
                    }
                } else {
                    serviceRequestList.add(serviceRequest);
                }
            } catch (Exception e) {
                // Log and fallback to original JSON if parsing or modification fails
                log.warn("Failed to add specimen to service request", e);
                serviceRequestList.add(serviceRequest);
            }
        }

        return serviceRequestList;
    }

    /**
     * Extracts the patient_id field from the response map's "data" object (if present).
     */
    private String extractPatientIdFromResponse(Map<String, Object> response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Convert Map to JSON string, then parse it into a JsonNode
            String jsonString = objectMapper.writeValueAsString(response);
            JsonNode rootNode = objectMapper.readTree(jsonString);

            JsonNode dataNode = rootNode.path("data");
            JsonNode patientNode = dataNode.path("patient");
            JsonNode idNode = patientNode.path("id");

            if (!idNode.isMissingNode() && !idNode.asText().trim().isEmpty()) {
                return idNode.asText().trim();
            }

        } catch (Exception e) {
            log.warn("Failed to extract patient ID from response.", e);
        }

        return "";
    }

    /**
     * Updates or adds an OPD number identifier to the patient, based on the external system ID.
     */
    private void updatePatientOPDNumber(String internalPatientId, String externalPatientId) {
        PatientService patientService = Context.getPatientService();
        PatientIdentifierType opdIdentifierType = patientService.getPatientIdentifierTypeByUuid(OPD_IDENTIFIER_TYPE_UUID);

        // Retrieve the patient using the internal patient identifier UUID
        Patient patient = patientService.getPatientIdentifierByUuid(internalPatientId).getPatient();

        if (patient == null || opdIdentifierType == null) return;

        PatientIdentifier existingOPD = patient.getPatientIdentifier(opdIdentifierType);

        if (existingOPD != null) {
            if (!existingOPD.getIdentifier().contains("UG-")) {
                existingOPD.setIdentifier(externalPatientId);
                patient.addIdentifier(existingOPD);
            }
        } else {
            PatientIdentifier newIdentifier = new PatientIdentifier();
            newIdentifier.setIdentifierType(opdIdentifierType);
            newIdentifier.setIdentifier(externalPatientId);
            patient.addIdentifier(newIdentifier);
        }

        patientService.savePatient(patient);
    }


    /**
     * Fetches the product catalog from the eAFYA API based on the given SyncTaskType configuration.
     *
     * <p>This method uses the URL, username, and password from the SyncTaskType to make an authenticated
     * HTTP GET request to the eAFYA product list endpoint, then parses the response into a JSONArray.</p>
     *
     * @param syncTaskType The synchronization task type containing connection details to eAFYA.
     * @return A JSONArray containing the product catalog data retrieved from eAFYA.
     * @throws RuntimeException if the request fails or an error occurs while processing the response.
     */
    private List<ObjectNode> getProductCatalogFromEAFYA(SyncTaskType syncTaskType) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<ObjectNode> eAFYAProductList = new ArrayList<>();

        String apiEndpoint = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.GetProductList");
        String fullUrl = syncTaskType.getUrl() + apiEndpoint;

        UgandaEMRHttpURLConnection httpConnection = new UgandaEMRHttpURLConnection();

        try {
            Map<String, Object> response = httpConnection.getByWithBasicAuth(
                    fullUrl,
                    syncTaskType.getUrlUserName(),
                    syncTaskType.getUrlPassword(),
                    "String"
            );

            int responseCode = Integer.parseInt(response.get("responseCode").toString());
            if (responseCode == 200 || responseCode == 201) {
                // Parse result string to ArrayNode
                String resultJson = response.get("result").toString();
                JsonNode resultNode = objectMapper.readTree(resultJson);

                if (resultNode.isArray()) {
                    for (JsonNode node : resultNode) {
                        if (node.isObject()) {
                            eAFYAProductList.add((ObjectNode) node);
                        }
                    }
                }
            } else {
                log.warn("Received unexpected response code: " + responseCode + " from URL: " + fullUrl);
            }

        } catch (Exception e) {
            log.error("Failed to fetch product catalog from eAFYA", e);
            throw new RuntimeException("Error while fetching product catalog from eAFYA", e);
        }

        return eAFYAProductList;
    }


    public boolean validateVLFHIRBundle(String bundleJson) {
        List<String> targetCodes = Arrays.asList(Context.getAdministrationService().getGlobalProperty("ugandaemrsync.viralloadRequiredProgramData").split(","));
        Set<String> foundCodes = new HashSet<>();

        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        Bundle bundle = null;

        try {
            bundle = parser.parseResource(Bundle.class, bundleJson);

            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof Observation) {
                    Observation obs = (Observation) entry.getResource();
                    for (Coding coding : obs.getCode().getCoding()) {
                        if (targetCodes.contains(coding.getCode())) {
                            foundCodes.add(coding.getCode());
                        }
                    }
                }
            }
        } catch (Exception exception) {
            log.error(exception);
        }

        return foundCodes.containsAll(targetCodes);
    }

    public String getMissingVLFHIRCodesAsString(String bundleJson) {
        List<String> targetCodes = Arrays.asList(
                Context.getAdministrationService()
                        .getGlobalProperty("ugandaemrsync.viralloadRequiredProgramData")
                        .split(",")
        );
        Set<String> foundCodes = new HashSet<>();

        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        Bundle bundle = null;
        List<String> missingCodes = new ArrayList<>();
        try {
            bundle = parser.parseResource(Bundle.class, bundleJson);

            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                if (entry.getResource() instanceof Observation) {
                    Observation obs = (Observation) entry.getResource();
                    for (Coding coding : obs.getCode().getCoding()) {
                        if (targetCodes.contains(coding.getCode())) {
                            foundCodes.add(coding.getCode());
                        }
                    }
                }
            }

            // Identify missing codes

            for (String code : targetCodes) {
                if (!foundCodes.contains(code)) {

                    Concept concept = getVLMissingCconcept(code);
                    if (concept != null) {
                        missingCodes.add(concept.getName().getName());
                    } else {
                        missingCodes.add(code);
                    }
                }
            }
        } catch (Exception exception) {
            log.error(exception);
        }
        return missingCodes.stream().collect(Collectors.joining(","));
    }

    public Concept getVLMissingCconcept(String code) {
        Concept loinc = Context.getConceptService().getConceptByMapping(code, "LOINC");
        Concept cphl = Context.getConceptService().getConceptByMapping(code, "UNHLS");
        Concept snomed = Context.getConceptService().getConceptByMapping(code, "SNOMED");

        if (loinc != null) {
            return loinc;
        }

        if (cphl != null) {
            return cphl;
        }
        if (snomed != null) {
            return snomed;
        }

        return null;
    }

    private Concept getConceptFromSource(JsonNode codeNode) {
        if (codeNode == null || !codeNode.has("coding")) {
            return null;
        }

        for (JsonNode innerCodeNode : codeNode.get("coding")) {
            if (!innerCodeNode.has("code")) {
                continue;
            }

            String code = innerCodeNode.get("code").asText();

            for (ConceptSource conceptSource : Context.getConceptService().getAllConceptSources(false)) {
                Concept concept = Context.getConceptService().getConceptByMapping(code, conceptSource.getName());
                if (concept != null) {
                    return concept;
                }
            }

            return Context.getConceptService().getConcept(code);
        }

        return null;
    }

    public boolean isValidCPHLBarCode(String accessionNumber) {
        Integer minimumCPHLBarCodeLength = Integer.parseInt(Context.getAdministrationService().getGlobalProperty("ugandaemrsync.minimumCPHLBarCodeLength"));
        if (accessionNumber == null || accessionNumber.length() < minimumCPHLBarCodeLength) {
            return false;
        }

        int currentYearSuffix = Year.now().getValue() % 100;

        try {
            int prefix = Integer.parseInt(accessionNumber.substring(0, 2));
            return prefix == currentYearSuffix || prefix == (currentYearSuffix - 1);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public List<Concept> getReferralOrderConcepts() {
        List<Concept> referralOrderConceptList = new ArrayList<>();

        String conceptIds = Context.getAdministrationService().getGlobalProperty("ugandaemrsync.cphlReferralOrderConceptIds");

        if (conceptIds != null && !conceptIds.trim().isEmpty()) {
            List<String> referralOrderConceptIDs = Arrays.asList(conceptIds.split(","));

            for (String conceptId : referralOrderConceptIDs) {
                conceptId = conceptId.trim();
                if (!conceptId.isEmpty()) {
                    Concept concept = Context.getConceptService().getConcept(conceptId);
                    if (concept != null) {
                        referralOrderConceptList.add(concept);
                    }
                }
            }
        }

        return referralOrderConceptList;
    }

    public IBaseResource convertStringToFHIRResource(String resourceJson) {
        if (resourceJson == null || resourceJson.trim().isEmpty()) {
            return null;
        }

        try {
            // Create FHIR context for R4
            FhirContext fhirContext = FhirContext.forR4();
            IParser parser = fhirContext.newJsonParser();

            // Configure parser
            parser.setPrettyPrint(true);
            parser.setParserErrorHandler(new StrictErrorHandler());

            // Parse the resource - HAPI FHIR will automatically detect the resource type
            return parser.parseResource(resourceJson);
        } catch (Exception e) {
            log.error("Error converting FHIR resource string to object: " + e.getMessage(), e);
            return null;
        }
    }



    /**
     * Helper method to get the resource type from a FHIR JSON string
     *
     * @param resourceJson The JSON string representing the FHIR resource
     * @return String representing the resource type, or null if not found
     */
    public String getResourceType(String resourceJson) {
        if (resourceJson == null || resourceJson.trim().isEmpty()) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(resourceJson);
            if (rootNode.has("resourceType")) {
                return rootNode.get("resourceType").asText();
            }
        } catch (Exception e) {
            log.error("Error extracting resource type from FHIR JSON: " + e.getMessage(), e);
        }
        return null;
    }


}


