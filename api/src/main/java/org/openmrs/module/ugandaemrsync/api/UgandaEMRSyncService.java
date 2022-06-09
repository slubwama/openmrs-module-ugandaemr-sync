/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api;

import org.json.JSONObject;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.Order;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfileLog;
import org.openmrs.module.ugandaemrsync.model.SyncFhirCase;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface UgandaEMRSyncService extends OpenmrsService {

    /**
     * Getting all sync task types
     * @return List<SyncTaskType> returns all sync task type in a list
     * @throws APIException
     */
    List<SyncTaskType> getAllSyncTaskType() throws APIException;

    /**
     * Get Sync Task Type By uuid
     * @param uuid the uuid of the sync task type to return
     * @return SyncTaskType that matched the uuid parameter
     * @throws APIException
     */
    @Transactional
    SyncTaskType getSyncTaskTypeByUUID(String uuid) throws APIException;

    /**
     * Saves the syn task Type
     * @param syncTaskType the sync task to be saved.
     * @return SyncTaskType saved sync task type
     * @throws APIException
     */
    @Transactional
    SyncTaskType saveSyncTaskType(SyncTaskType syncTaskType) throws APIException;

    /**
     * Gets SyncTask that matches the sync task parameter
     * @param syncTaskId a string containing an id or uuid of the sync task
     * @return SyncTask that matches the sync task id or uuid in the parameter set
     * @throws APIException
     */
    @Transactional
    SyncTask getSyncTaskBySyncTaskId(String syncTaskId) throws APIException;

    /**
     * Saves or updates the sync task set or given
     * @param syncTask the sync task that is to be saved
     * @return SyncTask the sync task that has been saved
     * @throws APIException
     */
    @Transactional
    SyncTask saveSyncTask(SyncTask syncTask) throws APIException;

    /**
     * Gets a list of sync task that require an action yet the action is not complete
     * @param syncTaskTypeIdentifier the sync task type identifier for the sync tasks that need to be checked
     * @return List<SyncTask> that are have an action that is not completed
     * @throws APIException
     */
    @Transactional
    public List<SyncTask> getIncompleteActionSyncTask(String syncTaskTypeIdentifier) throws APIException;

    /**
     * Get all sync task
     * @return List<SyncTask> a list of sync tasks found
     * @throws APIException
     */
    @Transactional
    public List<SyncTask> getAllSyncTask() throws APIException;

    /**
     * @param query
     * @return
     */
    public List getDatabaseRecord(String query);

    /**
     * @param columns
     * @param query
     * @return
     */
    public List getFinalList(List<String> columns, String query);

    /**
     /**
     * Gets patient Identifier by patientId
     * @param patientIdentifier this can be a uuid or patientId
     * @return Patient a patient that matches the patientId
     */
    public Patient getPatientByPatientIdentifier(String patientIdentifier);

    /**
     /**
     * This method is used to validate if the dhis2 code that is in the excel file matches the one set at facility
     * @param facilityDHIS2UUID the dhis2 code that will be validated
     * @return true is it is valid and false if it is invalid
     */
    public boolean validateFacility(String facilityDHIS2UUID);

    /**
     * @param s
     * @return
     */
    public Collection<EncounterType> getEcounterTypes(String s);

    /**
     * This method adds the viral load results to an encounter
     * @param vlQualitative the viral load qualitative value such as detected, not detected, sample rejected
     * @param vlQuantitative the viral load copies.
     * @param vlDate the date of sample collection date for the viral load results that are being returned
     * @param encounter the encounter where the viral load results will be written to
     * @param order the order which was used to order the viral load. this can be null.
     * @return Encounter the encounter where the viral load results have been added.
     */
    public Encounter addVLToEncounter(String vlQualitative, String vlQuantitative, String vlDate, Encounter encounter,
                                      Order order);

    /**
     * @param vlDate
     * @return
     */
    public String getDateFormat(String vlDate);

    /**
     * @param string
     * @param time
     * @param dateFormat
     * @return
     */
    public Date convertStringToDate(String string, String time, String dateFormat);

    /**
     /**
     * This method gets the dhis2 orgunit code for the facility
     * @return returns dhis2 orgunit code
     */
    public String getHealthCenterCode();

    /**
     * This method gets the health center name
     * @return
     */
    public String getHealthCenterName();

    /**
     * This Method gets the patient identifier that is based on patient Identifier type and the patient
     * @param patient the patient whose identifier is being searched
     * @param patientIdentifierTypeUUID the uuid of the patient identifier type that is being search
     * @return the identifier that matches both the patient and the patient identifier type UUID
     */
    public String getPatientIdentifier(Patient patient, String patientIdentifierTypeUUID);

    /**
     * Gets UgandaEMR Properties Set in the file.
     * @return
     */
    public Properties getUgandaEMRProperties();

    /**
     * This Method saves a Sync FHIR Profile
     * @param syncFhirProfile the Sync FHIR Profile to be saved
     * @return the saved Sync FHIR Profile
     */
    @Transactional
    public SyncFhirProfile saveSyncFhirProfile(SyncFhirProfile syncFhirProfile);

    /**
     * This Method returs a Sync FHIR Profile that matches the id given
     * @param id the Id that will be used to match a sync fhir profile
     * @return the matched Sync FHIR Profile
     */
    public SyncFhirProfile getSyncFhirProfileById(Integer id);


    /**
     * This Method returs a Sync FHIR Profile that matches the id given
     * @param uuid the uuid that will be used to match a sync fhir profile
     * @return the matched Sync FHIR Profile
     */
    public SyncFhirProfile getSyncFhirProfileByUUID(String uuid);


    /**
     * This Method gets a Sync FHIR Profile from a scheduled task
     * @param scheduledTaskName
     * @return the syncFhirProfile that is associated with a scheduled task that matches the scheduledTaskName
     */

    public SyncFhirProfile getSyncFhirProfileByScheduledTaskName(String scheduledTaskName);


    /**
     * This Method saves a Sync FHIR Resource
     * @param syncFHIRResource the resource to be saved
     * @return the saved sync fhir resource
     */
    @Transactional
    public SyncFhirResource saveFHIRResource(SyncFhirResource syncFHIRResource);


    /**
     * This Method gets a list of sync fhir resources by the profile that generated them
     * @param syncFhirProfile
     * @param includeSynced the check to determine if it has been sent to the destined server
     * @return
     */
    public List<SyncFhirResource> getSyncFHIRResourceBySyncFhirProfile(SyncFhirProfile syncFhirProfile, boolean includeSynced);


    /**
     * Gets a Sync FHIR Resource using an id
     * @param id the id that will be used to match the resource
     * @return the resource that matches the id
     */
    public SyncFhirResource getSyncFHIRResourceById(Integer id);

    /**
     * Marks resource Synced and sets expiry date based on the number of days to keep resource after sync set in profile
     * @param syncFhirResources the resource to be marked synced
     * @return the resource that is marked synced.
     */
    @Transactional
    public SyncFhirResource markSyncFHIRResourceSynced(SyncFhirResource syncFhirResources);


    /**
     * gets all expired resources based on date passed
     * @param date the date which will be used to match expired resources
     * @return a list of expired resources
     */
    public List<SyncFhirResource> getExpiredSyncFHIRResources(Date date);


    /**
     * gets all expired resources based on date passed
     * @param syncFhirProfile the profile that generated the resources
     * @return a list of expired resources
     */
    public List<SyncFhirResource> getUnSyncedFHirResources(SyncFhirProfile syncFhirProfile);

    /**
     * Purges all resources that have  expired
     */
    @Transactional
    public void purgeExpiredFHIRResource(Date date);

    /**
     * This Saves the Sync Profile Log
     * @param syncFhirProfileLog the log to be saved
     * @return the SyncFhirProfileLog that has been saved
     */
    @Transactional
    public SyncFhirProfileLog saveSyncFhirProfileLog(SyncFhirProfileLog syncFhirProfileLog);

    /**
     * This returns the latest sync fhir profile log
     * @param syncFhirProfile the sync fhir profile to be used search for the syncFhirProfile
     * @param resourceType a parameter to be used to search for the sync fhir log
     * @return syncFhirProfileLog that has matched the search
     */
    public List<SyncFhirProfileLog> getSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile syncFhirProfile, String resourceType);

    /**
     * This returns the latest sync fhir profile log
     * @param syncFhirProfile the sync fhir profile to be used search for the syncFhirProfile
     * @param resourceType a parameter to be used to search for the sync fhir log
     * @return syncFhirProfileLog that has matched the search
     */
    public SyncFhirProfileLog getLatestSyncFhirProfileLogByProfileAndResourceName(SyncFhirProfile syncFhirProfile, String resourceType);


    /**
     * This will get a FHIRCase which matches the parameters set
     * @param syncFhirProfile the profile that was is used to identify the case
     * @param patient the patient who belongs to the case
     * @return the case that matches the parameters
     */
    public SyncFhirCase getSyncFHIRCaseBySyncFhirProfileAndPatient(SyncFhirProfile syncFhirProfile, Patient patient, String caseIdentifier);


    /**
     * This Method saves a  FHIR Case
     * @param syncFHIRCase the case to be saved
     * @return the saved case
     */
    @Transactional
    public SyncFhirCase saveSyncFHIRCase(SyncFhirCase syncFHIRCase);

    /**
     * This Method gets a List of all Sync Fhir Profiles
     * @return a List of Sync Fhir Profiles
     */
    @Transactional
    public List<SyncFhirProfile> getAllSyncFhirProfile();


    /**
     * This gets all cases that belong to a sync fhir profile
     * @param syncFhirProfile the profile
     * @return all cases that belong to a profile.
     */
     List<SyncFhirCase> getSyncFhirCasesByProfile(SyncFhirProfile syncFhirProfile);

    /**
     * This Method Checks if a test order has results entered on it either through an encounter or on the order it self
     * @param order the order which is being checked
     * @return true is the order has results and false if it doesnt have results
     */
    public boolean testOrderHasResults(Order order);


    /**
     * This method adds the test results to an encounter from a fhir object
     * @param bundleResults The results that have returned for test(s) ordered
     * @param order the order which was used to order the test. this can be null.
     * @return Encounter the encounter where the test results have been added.
     */
    public Encounter addTestResultsToEncounter(JSONObject bundleResults, Order order);


    /**
     * Gets Profile By name of Profile
     * @param name the name to be matched to the profile
     * @return List<SyncFhirProfile> that match the name provided
     */
    public List<SyncFhirProfile> getSyncFhirProfileByName(String name);


    /**
     * Gets Sync Fhir Case by uuid
     * @param uuid the uuid to be matched to a case
     * @return a unique case that matches the uuid
     */
    public SyncFhirCase getSyncFhirCaseByUUDI(String uuid);


    /**
     * Get all Sync Fhir Cases.
     * @return a List of SyncFhirCase
     */
    public List<SyncFhirCase> getAllSyncFhirCase();


    /**
     * Gets Sync Fhir Case by id
     * @param id the uuid to be matched to a case
     * @return a unique case that matches the id
     */
    public SyncFhirCase getSyncFhirCaseById(Integer id);


    /**
     * Get all SyncFhirProfileLog.
     * @return a List of SyncFhirProfileLog
     */
    public List<SyncFhirProfileLog> getAllSyncFhirProfileLog();



    /**
     * Gets SyncFhirProfileLog by uuid
     * @param uuid the uuid to be matched to a profileLog
     * @return a unique Profile Log that matches the uuid
     */
    public SyncFhirProfileLog getSyncFhirProfileLogByUUID(String uuid);


    /**
     * Gets SyncFhirProfileLog by id
     * @param id the uuid to be matched to a profileLog
     * @return a unique Profile Log that matches the id
     */
    public SyncFhirProfileLog getSyncFhirProfileLogById(Integer id);



    /**
     * Get all SyncFhirResource.
     * @return a List of SyncFhirResource
     */
    public List<SyncFhirResource> getAllFHirResources();



    /**
     * Gets SyncFhirResource by uuid
     * @param uuid the uuid to be matched to a SyncFhirResource
     * @return a unique SyncFhirResource  that matches the uuid
     */
    public SyncFhirResource getSyncFhirResourceByUUID(String uuid);


    /**
     * This returns the latest sync fhir profile log
     *
     * @param syncFhirProfile the sync fhir profile to be used search for the syncFhirProfile
     * @return syncFhirProfileLog that has matched the search
     */
    public List<SyncFhirProfileLog> getSyncFhirProfileLogByProfile(SyncFhirProfile syncFhirProfile);


    /**
     * gets all expired resources based on date passed
     * @param syncFhirProfile the profile that generated the resources
     * @return a list of expired resources
     */
    public List<SyncFhirResource> getSyncedFHirResources(SyncFhirProfile syncFhirProfile);
}

