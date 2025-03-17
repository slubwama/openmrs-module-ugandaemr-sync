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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
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
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.util.UgandaEMRSyncUtil;
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


    /**
     * Injected in moduleApplicationContext.xml
     */
    public void setDao(UgandaEMRSyncDao dao) {
        this.dao = dao;
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncTaskType()
     */
    @Override
    public List<SyncTaskType> getAllSyncTaskType() throws APIException {
        return dao.getAllSyncTaskType();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncTaskTypeByUUID(java.lang.String)
     */
    @Override
    public SyncTaskType getSyncTaskTypeByUUID(String uuid) throws APIException {
        return dao.getSyncTaskTypeByUUID(uuid);
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncTaskType(org.openmrs.module.ugandaemrsync.model.SyncTaskType)
     */
    @Override
    public SyncTaskType saveSyncTaskType(SyncTaskType syncTaskType) throws APIException {
        if (syncTaskType.getCreator() == null) {
            syncTaskType.setCreator(Context.getUserService().getUser(1));
        }
        return dao.saveSyncTaskType(syncTaskType);
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncTaskBySyncTaskId(java.lang.String)
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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncTask()
     */
    @Override
    public List<SyncTask> getAllSyncTask() {
        return dao.getAllSyncTask();
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncTask(org.openmrs.module.ugandaemrsync.model.SyncTask)
     */
    @Override
    public SyncTask saveSyncTask(SyncTask syncTask) throws APIException {
        if (syncTask.getCreator() == null) {
            syncTask.setCreator(Context.getUserService().getUser(1));
        }
        return dao.saveSyncTask(syncTask);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getIncompleteActionSyncTask(java.lang.String)
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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#addVLToEncounter(java.lang.String, java.lang.String, java.lang.String, org.openmrs.Encounter, org.openmrs.Order)
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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getPatientByPatientIdentifier(java.lang.String)
     */
    public Patient getPatientByPatientIdentifier(String patientIdentifier) {
        try {
            return Context.getPatientService().getPatientIdentifiers(patientIdentifier, null, null, null, null).get(0).getPatient();
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#validateFacility(java.lang.String)
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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getHealthCenterCode()
     */
    public String getHealthCenterCode() {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        return syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getHealthCenterName()
     */
    public String getHealthCenterName() {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        return syncGlobalProperties.getGlobalProperty("aijar.healthCenterName");
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getPatientIdentifier(org.openmrs.Patient, java.lang.String)
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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncFhirProfile(SyncFhirProfile)
     */
    @Override
    public SyncFhirProfile saveSyncFhirProfile(SyncFhirProfile syncFhirProfile) {
        return dao.saveSyncFhirProfile(syncFhirProfile);
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileById(java.lang.Integer)
     */
    @Override
    public SyncFhirProfile getSyncFhirProfileById(Integer id) {
        return dao.getSyncFhirProfileById(id);
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileByUUID(java.lang.String)
     */
    @Override
    public SyncFhirProfile getSyncFhirProfileByUUID(String uuid) {
        return dao.getSyncFhirProfileByUUID(uuid);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileByScheduledTaskName(java.lang.String)
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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveFHIRResource(SyncFhirResource)
     */
    @Override
    public SyncFhirResource saveFHIRResource(SyncFhirResource syncFHIRResource) {
        return dao.saveSyncFHIRResource(syncFHIRResource);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveFHIRResource(SyncFhirResource)
     */
    @Override
    public List<SyncFhirResource> getSyncFHIRResourceBySyncFhirProfile(SyncFhirProfile syncFhirProfile, boolean includeSynced) {
        return dao.getSyncResourceBySyncFhirProfile(syncFhirProfile, includeSynced);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFHIRResourceById(java.lang.Integer)
     */
    @Override
    public SyncFhirResource getSyncFHIRResourceById(Integer id) {
        return dao.getSyncFHIRResourceById(id);
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#markSyncFHIRResourceSynced(SyncFhirResource)
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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getExpiredSyncFHIRResources(java.util.Date)
     */
    @Override
    public List<SyncFhirResource> getExpiredSyncFHIRResources(Date date) {
        return dao.getExpiredSyncFHIRResources(date);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getUnSyncedFHirResources(org.openmrs.module.ugandaemrsync.model.SyncFhirProfile)
     */
    @Override
    public List<SyncFhirResource> getUnSyncedFHirResources(SyncFhirProfile syncFhirProfile) {
        return dao.getUnSyncedFHirResources(syncFhirProfile);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#purgeExpiredFHIRResource(java.util.Date)
     */
    @Override
    public void purgeExpiredFHIRResource(Date date) {
        for (SyncFhirResource syncFHIRResource : getExpiredSyncFHIRResources(date)) {
            dao.purgeExpiredFHIRResource(syncFHIRResource);
        }
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncFhirProfileLog(SyncFhirProfileLog)
     */
    @Override
    public SyncFhirProfileLog saveSyncFhirProfileLog(SyncFhirProfileLog syncFhirProfileLog) {
        return dao.saveSyncFhirProfileLog(syncFhirProfileLog);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile, java.lang.String)
     */
    @Override
    public List<SyncFhirProfileLog> getSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile syncFhirProfile, String resourceType) {
        return dao.getSyncFhirProfileLogByProfileAndResourceName(syncFhirProfile, resourceType);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getLatestSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile, java.lang.String)
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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFHIRCaseBySyncFhirProfileAndPatient(SyncFhirProfile, org.openmrs.Patient, java.lang.String)
     */
    @Override
    public SyncFhirCase getSyncFHIRCaseBySyncFhirProfileAndPatient(SyncFhirProfile syncFhirProfile, Patient patient, String caseIdentifier) {
        return dao.getSyncFHIRCaseBySyncFhirProfileAndPatient(syncFhirProfile, patient, caseIdentifier);
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#saveSyncFHIRCase(SyncFhirCase)
     */
    @Override
    public SyncFhirCase saveSyncFHIRCase(SyncFhirCase syncFHIRCase) {
        return dao.saveSyncFHIRCase(syncFHIRCase);
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncFhirProfile()
     */
    @Override
    public List<SyncFhirProfile> getAllSyncFhirProfile() {
        return dao.getAllSyncFhirProfile();
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirCasesByProfile(org.openmrs.module.ugandaemrsync.model.SyncFhirProfile)
     */
    @Override
    public List<SyncFhirCase> getSyncFhirCasesByProfile(SyncFhirProfile syncFhirProfile) {
        return dao.getSyncFhirCasesByProfile(syncFhirProfile);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#testOrderHasResults(org.openmrs.Order)
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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileByName(java.lang.String)
     */
    public List<SyncFhirProfile> getSyncFhirProfileByName(String name) {
        return dao.getSyncFhirProfileByName(name);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirCaseByUUDI(java.lang.String)
     */
    @Override
    public SyncFhirCase getSyncFhirCaseByUUDI(String uuid) {
        return dao.getSyncFhirCaseByUUDI(uuid);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncFhirCase()
     */
    @Override
    public List<SyncFhirCase> getAllSyncFhirCase() {
        return dao.getAllSyncFhirCase();
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirCaseById(java.lang.Integer)
     */
    @Override
    public SyncFhirCase getSyncFhirCaseById(Integer id) {
        return dao.getSyncFhirCaseById(id);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllSyncFhirProfileLog()
     */
    @Override
    public List<SyncFhirProfileLog> getAllSyncFhirProfileLog() {
        return dao.getAllSyncFhirProfileLog();
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileLogByUUID(java.lang.String)
     */
    @Override
    public SyncFhirProfileLog getSyncFhirProfileLogByUUID(String uuid) {
        return dao.getSyncFhirProfileLogByUUID(uuid);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileLogById(java.lang.Integer)
     */
    @Override
    public SyncFhirProfileLog getSyncFhirProfileLogById(Integer id) {
        return dao.getSyncFhirProfileLogById(id);
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getAllFHirResources()
     */
    @Override
    public List<SyncFhirResource> getAllFHirResources() {
        return dao.getAllFHirResources();
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirResourceByUUID(java.lang.String)
     */
    @Override
    public SyncFhirResource getSyncFhirResourceByUUID(String uuid) {
        return dao.getSyncFhirResourceByUUID(uuid);
    }


    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncFhirProfileLogByProfile(org.openmrs.module.ugandaemrsync.model.SyncFhirProfile)
     */
    @Override
    public List<SyncFhirProfileLog> getSyncFhirProfileLogByProfile(SyncFhirProfile syncFhirProfile) {
        return dao.getSyncFhirProfileLogByProfile(syncFhirProfile);
    }

    /**
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#addTestResultsToEncounter(org.json.JSONObject, org.openmrs.Order)
     */
    public List<Encounter> addTestResultsToEncounter(JSONObject bundleResults, Order order) {
        Encounter encounter = null;

        if (order != null) {
            encounter = order.getEncounter();
        }

        List<Encounter> returningEncounter = new ArrayList<>();
        JSONArray jsonArray = bundleResults.getJSONArray("entry");

        JSONArray filteredDiagnosticReportArray = searchForJSONOBJECTSByKey(jsonArray, "resourceType", "DiagnosticReport");

        JSONArray filteredObservationArray = searchForJSONOBJECTSByKey(jsonArray, "resourceType", "Observation");

        for (Object jsonObject : filteredDiagnosticReportArray) {
            JSONObject diagnosticReport = new JSONObject(jsonObject.toString());

            returningEncounter = processTestResults(diagnosticReport, encounter, filteredObservationArray, order);
        }

        return returningEncounter;
    }


    private JSONArray searchForJSONOBJECTSByKey(JSONArray array, String key, String searchValue) {
        JSONArray filtedArray = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = null;
            try {
                obj = array.getJSONObject(i);
                if (obj.getJSONObject("resource").getString(key).equals(searchValue)) {
                    filtedArray.put(obj);
                }
            } catch (JSONException e) {
                log.error(e);
            }
        }

        return filtedArray;
    }

    private JSONObject searchForJSONOBJECTByKey(JSONArray array, String key, String searchValue) {

        JSONObject obj = null;
        for (int i = 0; i < array.length(); i++) {

            try {
                obj = array.getJSONObject(i);
                if (obj.getJSONObject("resource").getString(key).equals(searchValue)) {
                    return obj;
                }
            } catch (JSONException e) {
                log.error(e);
            }
        }

        return obj;
    }

    private Order getOrderFromFHIRObs(JSONObject jsonObject) {
        String orderUUID = jsonObject.getJSONArray("basedOn").getJSONObject(0).getString("reference").replace("ServiceRequest/", "");
        return Context.getOrderService().getOrderByUuid(orderUUID);
    }

    private Concept getConceptFromCodableConcept(JSONObject codableConcept) {
        for (int i = 0; i < codableConcept.getJSONArray("coding").length(); i++) {
            JSONObject orderFHIRConcept = codableConcept.getJSONArray("coding").getJSONObject(i);
            String orderCode = orderFHIRConcept.getString("code");
            Concept concept = null;
            String orderCodeSystem = orderFHIRConcept.getString("system");
            if (getConceptSourceBySystemURL(orderCodeSystem) != null) {
                concept = Context.getConceptService().getConceptByMapping(orderCode, getConceptSourceBySystemURL(orderCodeSystem).getName());
            }
            return concept;
        }
        return null;
    }

    private List<Encounter> processTestResults(JSONObject diagonisisReportJsonObject, Encounter encounter, JSONArray observationArray, Order order) {
        JSONObject diagnosticReport = diagonisisReportJsonObject.getJSONObject("resource");

        List<Encounter> encounters = new ArrayList<>();

        JSONArray jsonArray = diagnosticReport.getJSONArray("result");
        for (Object resultReferenceObject : jsonArray) {
            JSONObject resultReference = new JSONObject(resultReferenceObject.toString());

            String searchKey = resultReference.getString("reference").replace("Observation/", "");

            JSONObject observation = searchForJSONOBJECTByKey(observationArray, "id", searchKey).getJSONObject("resource");

            if (order == null) {
                order = getOrderFromFHIRObs(observation);

                encounter = order.getEncounter();
            }

            if (!resultsEnteredOnEncounter(order)) {

                Obs obs = createObsFromFHIRObervation(observation, order, observation.has("hasMember"));

                if (!order.getConcept().getSetMembers().isEmpty() && observation.has("hasMember") && observation.getJSONArray("hasMember").length() > 0) {
                    for (int i = 0; i < observation.getJSONArray("hasMember").length(); i++) {
                        String paramReference = observation.getJSONArray("hasMember").getJSONObject(i).getString("reference").replace("Observation/", "");
                        JSONObject parameters = searchForJSONOBJECTByKey(observationArray, "id", paramReference).getJSONObject("resource");
                        Obs parameterObs = createObsFromFHIRObervation(parameters, order, parameters.has("hasMember"));
                        if (parameterObs != null) {
                            obs.addGroupMember(parameterObs);
                            encounter.addObs(parameterObs);
                        }
                    }
                }
                encounter.addObs(obs);

                Context.getEncounterService().saveEncounter(encounter);

                List<Order> activeOrdersWithResults = encounter.getAllObs().stream().filter(obs1 -> obs1.getOrder() != null && obs1.getOrder().isActive()).map(Obs::getOrder).collect(Collectors.toList());

                for (Order resultedOrder : activeOrdersWithResults) {
                    try {
                        Context.getOrderService().discontinueOrder(resultedOrder, "Completed", new Date(), resultedOrder.getOrderer(), resultedOrder.getEncounter());
                        logResultsRecieved(resultedOrder);
                    } catch (Exception e) {
                        log.error(e);
                    }
                }

                encounters.add(encounter);
            }
        }
        return encounters;
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

    private Obs createObsFromFHIRObervation(JSONObject observation, Order order, boolean isSet) {

        Concept concept = getConceptFromCodableConcept(observation.getJSONObject("code"));
        if (concept != null) {
            Obs obs = createObs(order.getEncounter(), order, concept, null, null, null);
            assert obs != null;
            if (!isSet) {
                switch (concept.getDatatype().getUuid()) {
                    case ConceptDatatype.CODED_UUID:
                        Concept valueCodedConcept = getConceptFromCodableConcept(observation.getJSONObject("valueCodeableConcept"));
                        obs.setValueCoded(valueCodedConcept);
                        break;
                    case ConceptDatatype.NUMERIC_UUID:
                        obs.setValueNumeric(observation.getJSONObject("valueQuantity").getDouble("value"));

                        break;
                    case ConceptDatatype.BOOLEAN_UUID:
                        obs.setValueBoolean(observation.getBoolean("valueBoolean"));

                        break;
                    case ConceptDatatype.TEXT_UUID:
                        obs.setValueText(observation.getString("valueString"));
                        break;
                }
                if (obs.getValueAsString(Locale.ENGLISH).isEmpty()) {
                    return null;
                }
            }

            return obs;
        } else {
            return null;
        }

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
     * @see org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService#getSyncedFHirResources(org.openmrs.module.ugandaemrsync.model.SyncFhirProfile)
     */
    @Override
    public List<SyncFhirResource> getSyncedFHirResources(SyncFhirProfile syncFhirProfile) {
        return dao.getSyncedFHirResources(syncFhirProfile);
    }

    @Override
    public Patient createPatientsFromFHIR(JSONObject patientData) throws ParseException {
        PatientService patientService = Context.getPatientService();
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(patientData.get("birthDate").toString());

        String gender = String.valueOf(patientData.get("gender"));

        PersonName patientName = getPatientNames(patientData);

        Patient patient = new Patient();
        patient.addName(patientName);
        patient.setBirthdate(date);
        patient.setGender(gender);
        JSONArray patientIdentifiersObject = (JSONArray) patientData.getJSONArray("identifier");

        patient = getPatientIdentifiers(patientIdentifiersObject, patient);

        return patientService.savePatient(patient);
    }

    public Patient updatePatientsFromFHIR(JSONObject bundle, String identifierUUID, String identifierName) {
        Patient patient = null;
        PatientService patientService = Context.getPatientService();
        if (bundle.has("resourceType") && bundle.getString("resourceType").equals("Bundle") && bundle.getJSONArray("entry").length() > 0) {
            JSONArray bundleResourceObjects = bundle.getJSONArray("entry");

            for (int i = 0; i < bundleResourceObjects.length(); i++) {
                JSONObject patientResource = bundleResourceObjects.getJSONObject(i).getJSONObject("resource");
                patient = patientService.getPatientByUuid(patientResource.getString("id"));


                if (patient != null && patient.getPatientIdentifiers(patientService.getPatientIdentifierTypeByUuid(PATIENT_ID_TYPE_UIC_UUID)).size() > 0) {
                    if (patientResource.getJSONObject("type").get("text").toString().equals(PATIENT_ID_TYPE_UIC_NAME)) {
                        patient.addIdentifier(createPatientIdentifierByIdentifierTypeName(patientResource.get("value").toString(), patientResource.getJSONObject("type").get("text").toString()));
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

    private PersonName getPatientNames(JSONObject jsonObject) {
        JSONObject patientNamesObject = jsonObject.getJSONArray("name").getJSONObject(0);
        PersonName personName = new PersonName();

        if (patientNamesObject.get("family") != null) {
            personName.setFamilyName(patientNamesObject.get("family").toString());
        }

        if (jsonObject.getJSONArray("name").getJSONObject(0).getJSONArray("given").length() >= 2
                && jsonObject.getJSONArray("name").getJSONObject(0).getJSONArray("given").get(1) != null) {
            personName.setMiddleName(jsonObject.getJSONArray("name").getJSONObject(0).getJSONArray("given").get(1)
                    .toString());
        }

        if (jsonObject.getJSONArray("name").getJSONObject(0).getJSONArray("given").length() >= 1
                && jsonObject.getJSONArray("name").getJSONObject(0).getJSONArray("given").get(0) != null) {
            personName.setGivenName(jsonObject.getJSONArray("name").getJSONObject(0).getJSONArray("given").get(0).toString());
        }

        return personName;
    }

    private List<Identifier> getPatientIdentifiers(JSONArray jsonArray) {
        List<Identifier> identifiers = new ArrayList<Identifier>();
        for (Object idenrifierobject : jsonArray) {
            JSONObject jsonObject = new JSONObject(idenrifierobject.toString());
            Identifier identifier = new Identifier();
            identifier.setIdentifier(jsonObject.get("identifier").toString());
            identifier.setIdentifierType(jsonObject.get("identifierType").toString());
            identifier.setIdentifierTypeName(Context.getPatientService()
                    .getPatientIdentifierTypeByUuid(jsonObject.get("identifierType").toString()).getName());
            identifiers.add(identifier);
        }
        return identifiers;
    }

    private Patient getPatientIdentifiers(JSONArray jsonArray, Patient patient) {
        PatientService patientService = Context.getPatientService();
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        if (jsonArray.length() > 0) {
            for (Object o : jsonArray) {
                JSONObject jsonObject = new JSONObject(o.toString());
                if (jsonObject.getJSONObject("type").get("text").toString().equals(PATIENT_ID_TYPE_NIN_NAME) || jsonObject.getJSONObject("type").get("text").toString().equals(PATIENT_ID_TYPE_UIC_NAME)) {
                    patient.addIdentifier(createPatientIdentifierByIdentifierTypeName(
                            jsonObject.get("value").toString(), jsonObject.getJSONObject("type").get("text").toString()));
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

    public boolean patientFromFHIRExists(JSONObject patientData) {
        boolean patientExists = false;
        for (Object o : patientData.getJSONArray("identifier")) {
            JSONObject jsonObject = new JSONObject(o.toString());
            PatientService patientService = Context.getPatientService();
            List<PatientIdentifier> patientIdentifier = patientService.getPatientIdentifiers(jsonObject.get("value").toString(), null, null, null, null);

            if (patientIdentifier.size() > 0) {
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


    private List<JSONObject> processRequisitionsToSync() {
        String storeId = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.storeId");
        String sourceStoreId = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.sourceStoreId");
        StockManagementService stockManagementService = Context.getService(StockManagementService.class);
        ProviderAttributeType providerAttributeType = Context.getProviderService().getProviderAttributeTypeByUuid("d376f27c-cb93-45b4-be0a-6be88c520233");
        StockSource stockSource = stockManagementService.getStockSourceByUuid("9babcc02-fc0b-11ef-ab84-28977ca9db4b");
        StockOperationType stockOperationType = stockManagementService.getStockOperationTypeByUuid("7073e8f4-eb6b-11ef-80d7-730ad71afb9c");
        StockOperationSearchFilter filter = new StockOperationSearchFilter();
        filter.setStatus(new ArrayList<>(Collections.singletonList(StockOperationStatus.SUBMITTED)));
        filter.setStockSourceId(stockSource.getId());
        filter.setOperationTypeId(Collections.singletonList(stockOperationType.getId()));

        Result<StockOperationDTO> results = stockManagementService.findStockOperations(filter);
        List<JSONObject> requisitions = new ArrayList<>();
        for (StockOperationDTO stockOperationDTO : results.getData()) {
            JSONObject requisition = new JSONObject();
            StockOperation stockOperation = stockManagementService.getStockOperationByUuid(stockOperationDTO.getUuid());
            if (getSyncTaskTypeByName(stockOperation.getUuid()).isEmpty()) {
                Provider provider = null;
                try {
                    Collection<Provider> providers = Context.getProviderService().getProvidersByPerson(Context.getUserService().getUser(stockOperationDTO.getCreator()).getPerson(), false);
                    if (!providers.isEmpty()) {
                        provider = new ArrayList<>(providers).get(0);
                    }
                } catch (Exception e) {
                    log.error(e);
                }
                JSONArray items = processRequisitionItems(stockOperation.getStockOperationItems(), stockSource, provider, providerAttributeType);
                if (!items.isEmpty()) {
                    String eaFYAID = getProviderAttributeByType(provider.getAttributes(), providerAttributeType);
                    if (eaFYAID != null && isInteger(eaFYAID)) {
                        requisition.put("created_by_id", Integer.parseInt(eaFYAID));
                    } else {
                        requisition.put("created_by_id", eaFYAID);
                    }
                    requisition.put("destination_store_id", Integer.parseInt(storeId));
                    requisition.put("source_store_id", Integer.parseInt(sourceStoreId));
                    requisition.put("requester_comment", stockOperation.getOperationNumber() + ": " + stockOperation.getRemarks());
                    requisition.put("internal_requisition_no", stockOperation.getOperationNumber());
                    requisition.put("internal_requisition_uuid", stockOperation.getUuid());
                    requisition.put("items", items);
                }
            }
            requisitions.add(requisition);
        }

        return requisitions;
    }

    public List<String> getSendRequisitionStock() {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
        Map response;
        StockManagementService stockManagementService = Context.getService(StockManagementService.class);
        SyncTaskType syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(EAFYA_STOCK_SYNC_TASK_UUID);
        String api = Context.getAdministrationService().getGlobalProperty(MODULE_ID + ".eafya.SendRequisitionStock");
        String url = syncTaskType.getUrl() + api;


        if (syncTaskType.getTokenExpiryDate() == null || (syncTaskType.getTokenExpiryDate() != null && syncTaskType.getTokenExpiryDate().before(new Date()))) {
            syncTaskType = setAccessTokenToSyncTaskType();
        }
        String token = syncTaskType.getTokenType() + " " + syncTaskType.getUrlToken();
        try {
            for (JSONObject jsonObject : processRequisitionsToSync()) {
                if (getSyncTaskBySyncTaskId(jsonObject.getString("internal_requisition_no")) == null && jsonObject.getString("internal_requisition_uuid") != null) {
                    StockOperation stockOperation = stockManagementService.getStockOperationByUuid(jsonObject.getString("internal_requisition_uuid"));
                    response = ugandaEMRHttpURLConnection.sendPostBy(url, syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword(), null, jsonObject.toString(), false);
                    if (!response.isEmpty() && (response.get("responseCode").equals(200) || response.get("responseCode").equals(201))) {
                        JSONObject jsonResponse = new JSONObject(response);
                        String internalRequisitionNumber = jsonObject.getString("internal_requisition_no");
                        String externalRequisitionNumber = jsonResponse.getJSONObject("data").getString("requisition_number");
                        String statusMessage = jsonResponse.getString("message");
                        Integer responseCode = Integer.parseInt(response.get("responseCode").toString());
                        logTransaction(syncTaskType, responseCode, null, externalRequisitionNumber, statusMessage, new Date(), syncTaskType.getUrl() + api, true, false);
                        logTransaction(syncTaskType, responseCode, null, internalRequisitionNumber, statusMessage, new Date(), syncTaskType.getUrl() + api, false, false);
                        StockOperationDTO stockOperationDTO = generateStockOperationDTO(stockOperation);
                        stockOperationDTO.setExternalReference(externalRequisitionNumber);
                        stockManagementService.saveStockOperation(stockOperationDTO);
                    } else if (!response.isEmpty()) {
                        logTransaction(syncTaskType, Integer.parseInt(response.get("responseCode").toString()), null, EAFYA_SMART_ERP_RECEIVE_STOCK, response.get("responseMessage").toString() + "while syncing requisition:" + jsonObject.getString("internal_requisition_no"), new Date(), syncTaskType.getUrl() + api, false, false);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getCause());
            logTransaction(syncTaskType, 500, "Internal Server Error: failed to sync receive stock items from erp", EAFYA_SMART_ERP_RECEIVE_STOCK, e.getCause().getMessage(), new Date(), syncTaskType.getUrl() + api, false, false);
        }
        return null;
    }

    private JSONArray processRequisitionItems(Set<StockOperationItem> stockOperationItems, StockSource stockSource, Provider provider, ProviderAttributeType providerAttributeType) {
        StockManagementService stockManagementService = Context.getService(StockManagementService.class);
        JSONArray jsonArray = new JSONArray();
        for (StockOperationItem stockOperationItem : stockOperationItems) {
            StockItemReference stockItemReference = stockOperationItem.getStockItem().getReferences().stream().filter(reference -> reference.getReferenceSource().equals(stockSource)).collect(Collectors.toList()).get(0);
            if (stockItemReference != null) {
                String eaFYAID = getProviderAttributeByType(provider.getAttributes(), providerAttributeType);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("quantity", stockOperationItem.getQuantity());
                jsonObject.put("expected_quantity", stockOperationItem.getQuantity());

                if (isInteger(stockItemReference.getStockReferenceCode())) {
                    jsonObject.put("product_id", Integer.parseInt(stockItemReference.getStockReferenceCode()));
                } else {
                    jsonObject.put("product_id", stockItemReference.getStockReferenceCode());
                }

                if (eaFYAID != null && isInteger(eaFYAID)) {
                    jsonObject.put("created_by_id", Integer.parseInt(eaFYAID));
                } else {
                    jsonObject.put("created_by_id", eaFYAID);
                }

                jsonArray.put(jsonObject);
            }
        }
        return jsonArray;
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
                JSONObject requisition = new JSONObject();
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
        SyncTask syncTask = new SyncTask();
        syncTask.setSyncTask(logName);
        syncTask.setStatus(status);
        syncTask.setStatusCode(statusCode);
        syncTask.setDateSent(date);
        syncTask.setSyncTaskType(syncTaskType);
        syncTask.setRequireAction(actionRequired);
        syncTask.setActionCompleted(actionCompleted);
        syncTask.setSentToUrl(url);
        Context.getService(UgandaEMRSyncService.class).saveSyncTask(syncTask);
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

    private Date getDateFromString(String dateString, String format) {
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

}

