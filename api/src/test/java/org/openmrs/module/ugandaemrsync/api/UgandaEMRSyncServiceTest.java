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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.model.SyncFhirCase;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfileLog;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIR_FILTER_OBJECT_STRING;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_SYNC_TYPE_UUID;

/**
 * This is a unit test, which verifies logic in UgandaEMRSyncService. It doesn't extend
 * BaseModuleContextSensitiveTest, thus it is run without the in-memory DB and Spring context.
 */
public class UgandaEMRSyncServiceTest extends BaseModuleContextSensitiveTest {
    protected static final String UGANDAEMRSYNC_GLOBALPROPERTY_DATASET_XML = "org/openmrs/module/ugandaemrsync/include/globalPropertiesDataSet.xml";
    protected static final String UGANDAEMRSYNC_STANDARDTESTDATA = "org/openmrs/module/ugandaemrsync/include/standardTestDataset.xml";

    @Before
    public void initialize() throws Exception {
        executeDataSet(UGANDAEMRSYNC_GLOBALPROPERTY_DATASET_XML);
        executeDataSet(UGANDAEMRSYNC_STANDARDTESTDATA);
    }

    @Before
    public void setupMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFacilityConcatenation() {

        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        syncGlobalProperties.setSyncFacilityProperties();
        String facilityId = syncGlobalProperties.getGlobalProperty(SyncConstant.HEALTH_CENTER_SYNC_ID);

        String query = "SELECT\n" + "  name,\n" + "  description,\n" + "  (SELECT uuid\n" + "   FROM users AS u\n" + "   WHERE u.user_id = er.creator)    AS creator,\n" + "  date_created,\n" + "  (SELECT uuid\n" + "   FROM users AS u\n" + "   WHERE u.user_id = er.changed_by) AS changed_by,\n" + "  date_changed,\n" + "  retired,\n" + "  (SELECT uuid\n" + "   FROM users AS u\n" + "   WHERE u.user_id = er.retired_by) AS retired_by,\n" + "  date_retired,\n" + "  retire_reason,\n" + "  uuid,\n" + String.format("  '%s'                        AS facility,\n", facilityId) + "  'NEW'                             AS state\n" + "FROM encounter_role er";

        // assertNotNull(facilityId);
        //assertTrue(query.contains(facilityId));
    }

    @Test
    public void saveSyncTaskType_shouldSaveSyncTaskType() throws Exception {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        List<SyncTaskType> syncTaskTypesBeforeSavingingMore = ugandaEMRSyncService.getAllSyncTaskType();
        SyncTaskType neSyncTaskType = new SyncTaskType();
        neSyncTaskType.setDateCreated(new Date());
        neSyncTaskType.setName("SyncTaskType1");
        neSyncTaskType.setDataType("org.openmrs.Concepts");
        neSyncTaskType.setUrl("http://google.com");
        neSyncTaskType.setUrlUserName("samuel");
        neSyncTaskType.setUrlPassword("samule");
        neSyncTaskType.setUrlToken("agehgyryteghuteded");
        neSyncTaskType.setDataTypeId("4672");
        neSyncTaskType.setCreator(Context.getAuthenticatedUser());
        ugandaEMRSyncService.saveSyncTaskType(neSyncTaskType);

        List<SyncTaskType> syncTaskTypes = ugandaEMRSyncService.getAllSyncTaskType();

        Assert.assertEquals(syncTaskTypesBeforeSavingingMore.size() + 1, syncTaskTypes.size());
    }

    @Test
    public void saveSyncTask_shouldSaveSyncTask() throws Exception {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);
        SyncTask newSyncTask = new SyncTask();
        newSyncTask.setDateSent(new Date());
        newSyncTask.setCreator(Context.getUserService().getUser(1));
        newSyncTask.setSentToUrl(syncTaskType.getUrl());
        newSyncTask.setRequireAction(true);
        newSyncTask.setActionCompleted(false);
        newSyncTask.setSyncTask("1234");
        newSyncTask.setStatusCode(200);
        newSyncTask.setStatus("SUCCESS");
        newSyncTask.setSyncTaskType(ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID));
        ugandaEMRSyncService.saveSyncTask(newSyncTask);
        List<SyncTask> syncTasks = ugandaEMRSyncService.getAllSyncTask();

        Assert.assertEquals(2, syncTasks.size());
    }


    @Test
    public void getAllSyncTask_ShouldReturnAllsyncTaskTypes() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        List<SyncTaskType> syncTaskTypes = ugandaEMRSyncService.getAllSyncTaskType();

        Assert.assertEquals(2, syncTaskTypes.size());
    }

    @Before
    public void initializeSyncTask() throws Exception {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);
        SyncTask newSyncTask = new SyncTask();
        newSyncTask.setDateSent(new Date());
        newSyncTask.setCreator(Context.getUserService().getUser(1));
        newSyncTask.setSentToUrl(syncTaskType.getUrl());
        newSyncTask.setRequireAction(true);
        newSyncTask.setActionCompleted(false);
        newSyncTask.setSyncTask("1234");
        newSyncTask.setStatusCode(200);
        newSyncTask.setStatus("SUCCESS");
        newSyncTask.setSyncTaskType(ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID));
        ugandaEMRSyncService.saveSyncTask(newSyncTask);
    }

    @Test
    public void getAllSyncTask_ShouldReturnAllSyncTask() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        List<SyncTask> syncTask = ugandaEMRSyncService.getAllSyncTask();

        Assert.assertEquals(1, syncTask.size());
    }

    @Test
    public void getSyncTaskBySyncTaskId_ShouldReturnSyncTask() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncTask syncTask = ugandaEMRSyncService.getSyncTaskBySyncTaskId("1234");

        Assert.assertEquals("1234", syncTask.getSyncTask());
    }


    @Test
    public void getSyncTaskTypeByUUID_shouldReturnSyncTaskTypeThatMatchesUUID() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);

        Assert.assertEquals(VIRAL_LOAD_SYNC_TYPE_UUID, syncTaskType.getUuid());
    }

    @Test
    public void getSyncT_shouldReturnSyncTaskTypeThatMatchesUUID() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);
        List<SyncTask> syncTasks = ugandaEMRSyncService.getIncompleteActionSyncTask(syncTaskType.getDataTypeId());

        Assert.assertNotEquals(0, syncTasks.size());
        Assert.assertEquals(VIRAL_LOAD_SYNC_TYPE_UUID, syncTasks.get(0).getSyncTaskType().getUuid());
        Assert.assertEquals(false, syncTasks.get(0).getActionCompleted());
        Assert.assertEquals(true, syncTasks.get(0).getRequireAction());
    }

    @Test
    public void convertStringToDate_shouldReturnDate() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        Assert.assertNotNull(ugandaEMRSyncService.convertStringToDate("2013-08-02", "00:00:00", "yyyy-MM-dd"));
    }

    @Test
    public void getDateFormat_shouldGetDateFormatFromGivenDate() {
        Assert.assertEquals("yyyy-MM-dd", Context.getService(UgandaEMRSyncService.class).getDateFormat("2013-08-02"));
    }

    @Test
    public void getPatientIdentifier_shouldGetDateFormatFromGivenDate() {
        Patient patient = Context.getService(UgandaEMRSyncService.class).getPatientByPatientIdentifier("101-6");
        Assert.assertNotNull(patient);
        Assert.assertEquals("101-6", patient.getPatientIdentifier().getIdentifier());
    }

    @Test
    public void validateFacility_shouldReturnTrueWhenStringIsFacilityDHIS2UUID() {
        Assert.assertTrue(Context.getService(UgandaEMRSyncService.class).validateFacility("7744yxP"));
    }

    @Test
    public void addVLToEncounter_shouldSaveViralLoadResultToSelectedEncounter() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        Encounter encounter = Context.getEncounterService().getEncounter(1000);
        ugandaEMRSyncService.addVLToEncounter("Not detected", "400", "2009-08-01 00:00:00.0", encounter, null);
        Context.getObsService().getObservations("Anet Test Oloo");

        Assert.assertEquals(encounter, Context.getObsService().getObservations("Anet Test Oloo").get(1).getEncounter());
        List<Obs> obs=Context.getObsService().getObservationsByPersonAndConcept(encounter.getPatient().getPerson(),Context.getConceptService().getConcept(1305));
        Assert.assertTrue(obs.size()>0);
        Assert.assertEquals("1306",obs.get(0).getValueCoded().getConceptId().toString());

    }


    @Test
    public void getSyncFhirProfileById_ShouldReturnSyncFhirProfileByID() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileById(1);

        Assert.assertNotNull(syncFhirProfile);
        Assert.assertEquals("Example Profile", syncFhirProfile.getName());
    }

    @Test
    public void getSyncFhirProfileByUUID_ShouldReturnSyncFhirProfileByUUID() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa");

        Assert.assertNotNull(syncFhirProfile);
        Assert.assertEquals("Example Profile", syncFhirProfile.getName());
    }


    @Test
    public void saveSyncFhirProfile_shouldSaveSyncFhirProfile() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirProfile syncFhirProfile = new SyncFhirProfile();
        syncFhirProfile.setName("FHIR Profile to be saved");
        syncFhirProfile.setGenerateBundle(true);
        syncFhirProfile.setResourceTypes("Patient,Encounter,Observation");
        syncFhirProfile.setResourceSearchParameter(FHIR_FILTER_OBJECT_STRING);
        syncFhirProfile.setUrl("http://google.com");
        syncFhirProfile.setUrlUserName("username");
        syncFhirProfile.setUrlPassword("password");
        syncFhirProfile.setUrlToken("ZZZZAAAACCCC");

        syncFhirProfile = ugandaEMRSyncService.saveSyncFhirProfile(syncFhirProfile);

        Assert.assertNotNull(syncFhirProfile);
        Assert.assertNotNull(syncFhirProfile.getId());
        Assert.assertEquals(syncFhirProfile.getResourceSearchParameter(), FHIR_FILTER_OBJECT_STRING);
    }

    @Test
    public void getSyncFhirProfileByScheduledTaskName_ShouldReturnSyncFHIRFromScheduledTask() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        String encounterFilter = "{\"observationFilter\":{\"encounterReference\":[],\"patientReference\":[],\"hasMemberReference\":[],\"valueConcept\":[],\"valueDateParam\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"valueQuantityParam\":[],\"valueStringParam\":[],\"date\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"code\":[],\"category\":[],\"id\":[],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}},\"patientFilter\":{\"name\":[],\"given\":[],\"family\":[],\"identifier\":[],\"gender\":[],\"birthDate\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"deathDate\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"deceased\":[],\"city\":[],\"state\":[],\"postalCode\":[],\"country\":[],\"id\":[],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}},\"encounterFilter\":{\"date\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"location\":[],\"participant\":[],\"subject\":[],\"id\":[],\"type\":[\"8d5b2be0-c2cc-11de-8d13-0010c6dffd0f\"],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}},\"personFilter\":{\"name\":[],\"gender\":[],\"birthDate\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"},\"deceased\":[],\"city\":[],\"state\":[],\"postalCode\":[],\"country\":[],\"id\":[],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}},\"practitionerFilter\":{\"identifier\":[],\"name\":[],\"given\":[],\"family\":[],\"deceased\":[],\"city\":[],\"state\":[],\"postalCode\":[],\"country\":[],\"id\":[],\"lastUpdated\":{\"lowerBound\":\"\",\"myUpperBound\":\"\"}}}";
        SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileByScheduledTaskName("Example Task for FHIR Exchange Profile");
        Assert.assertEquals(syncFhirProfile.getResourceSearchParameter(), encounterFilter);
        Assert.assertEquals("Example Profile", syncFhirProfile.getName());
    }


    @Test
    public void saveSyncFHIRResource_shouldSaveSyncFHIRResource() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        String sampleResource = "{\"resourceType\":\"Bundle\",\"type\":\"transaction\",\"entry\":[{\"resource\":{\"resourceType\":\"Observation\",\"id\":\"071ef75b-713d-4f52-adee-138646226512\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"160288AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Reason for appointment/visit\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"ab4510ac-3feb-4abb-8653-5d252813798f\",\"display\":\"ART Initiation\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"0ae0da82-915e-48a4-8d4c-ac80f6fac274\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"162476AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Specimen sources\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"162476\",\"display\":\"Specimen sources\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"159994AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Urine\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"159994\",\"display\":\"Urine\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"12c89990-1618-402d-bdc3-58e313d1860e\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"b04eaf95-77c9-456a-99fb-f668f58a9386\",\"display\":\"OTHER MEDICATIONS DISPENSED\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueString\":\"DTG\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"154dc2f2-39f7-45b4-aa55-76c58efdfba0\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"ddcd8aad-9085-4a88-a411-f19521be4785\",\"display\":\"HIV TEST\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"6d7a7a98-2c57-4318-9961-8e61fb427781\",\"display\":\"ANTIBODY HIV TEST\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"173d7b24-364a-457c-8b0b-8a214778cd0c\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"laboratory\",\"display\":\"Laboratory\"}]}],\"code\":{\"coding\":[{\"code\":\"dca16e53-30ab-102d-86b0-7a5022ba4115\",\"display\":\"HEPATITIS B TEST - QUALITATIVE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dc85aa72-30ab-102d-86b0-7a5022ba4115\",\"display\":\"NEGATIVE\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"1e06a662-608d-46eb-82c2-9485a8d064a7\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"165050AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Nutrition Assesment\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dc9816bd-30ab-102d-86b0-7a5022ba4115\",\"display\":\"NORMAL\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"1ef87b0e-cf8f-44f9-a675-0e5409987099\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"159368AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Medication duration\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":30.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"218f81d7-464f-4552-99e7-f6e9afde3805\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"7593ede6-6574-4326-a8a6-3d742e843659\",\"display\":\"ARV REGIMEN DAYS DISPENSED\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":30.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"252463a7-1914-4398-b0bd-cbe516f88e63\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"dcda9857-30ab-102d-86b0-7a5022ba4115\",\"display\":\"SCHEDULED PATIENT VISIST\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueBoolean\":true},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"3a8d75ac-3c31-412c-b49c-91417a98d46e\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Pulse\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5087\",\"display\":\"Pulse\"},{\"system\":\"http://loinc.org\",\"code\":\"8867-4\",\"display\":\"Pulse\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":5.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"3ca1be24-51b2-4bcf-9d93-75e0570274e5\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"ab505422-26d9-41f1-a079-c3d222000440\",\"display\":\"BASELINE REGIMEN START DATE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueDateTime\":\"2021-04-01T00:00:00+03:00\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"3f653fad-f8d9-4385-8292-0fc5a21a6239\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"ffe9b82c-d341-47a9-a7ef-89c0f5abba97\",\"display\":\"ARV MED SET\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"hasMember\":[{\"reference\":\"Observation/46374bcb-b5f3-4caf-b336-a819558ffb5d\",\"type\":\"Observation\"},{\"reference\":\"Observation/fb08ae34-51c1-4379-b05a-93cff4a1c4fb\",\"type\":\"Observation\"},{\"reference\":\"Observation/218f81d7-464f-4552-99e7-f6e9afde3805\",\"type\":\"Observation\"}]},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"3fa65d9f-7f43-4d16-a1bd-dcd7a243aca8\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Height (cm)\"},{\"system\":\"http://loinc.org\",\"code\":\"8302-2\",\"display\":\"Height (cm)\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5090\",\"display\":\"Height (cm)\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":167.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"403477b3-53de-4584-a6c8-f60c947ff251\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dc7620b3-30ab-102d-86b0-7a5022ba4115\",\"display\":\"METHOD OF FAMILY PLANNING\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dc692ad3-30ab-102d-86b0-7a5022ba4115\",\"display\":\"CONDOMS\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"4103a799-40cb-4428-883e-50263ed5e03e\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"31c5c7aa-4948-473e-890b-67fe2fbbd71a\",\"display\":\"HIV ENROLLMENT DATE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueDateTime\":\"2021-04-01T00:00:00+03:00\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"46374bcb-b5f3-4caf-b336-a819558ffb5d\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"dd2b0b4d-30ab-102d-86b0-7a5022ba4115\",\"display\":\"CURRENT ARV REGIMEN\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"a779d984-9ccf-4424-a750-47506bf8212b\",\"display\":\"AZT/3TC/DTG\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"48398e3c-96a7-4e9c-bc8b-39c328d8dded\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Diastolic blood pressure\"},{\"system\":\"http://loinc.org\",\"code\":\"35094-2\",\"display\":\"Diastolic blood pressure\"},{\"system\":\"http://loinc.org\",\"code\":\"8462-4\",\"display\":\"Diastolic blood pressure\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5086\",\"display\":\"Diastolic blood pressure\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":120.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"4aa46edb-d254-4cc6-bda9-b21576c56dbc\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce0a659-30ab-102d-86b0-7a5022ba4115\",\"display\":\"FAMILY PLANNING STATUS\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdd6c4a-30ab-102d-86b0-7a5022ba4115\",\"display\":\"NOT PREGNANT AND ON FAMILY PLANNING\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"4b3d3816-2399-4e34-b0b0-fbdb113c758f\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Weight (kg)\"},{\"system\":\"http://loinc.org\",\"code\":\"3141-9\",\"display\":\"Weight (kg)\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5089\",\"display\":\"Weight (kg)\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":66.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"51de7299-0b09-49ca-950f-08860cb26e01\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dfc50562-da6a-4ce2-ab80-43c8f2d64d6f\",\"display\":\"Quantity Unit\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"1513AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Tablet\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"1513\",\"display\":\"Tablet\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"5a98e805-dc69-4361-8563-41089a8d3992\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dcdff274-30ab-102d-86b0-7a5022ba4115\",\"display\":\"WHO HIV CLINICAL STAGE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcda2bc2-30ab-102d-86b0-7a5022ba4115\",\"display\":\"HIV WHO CLINICAL STAGE 1\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"68d8bf28-8eb3-4ca5-81e8-a368a8d7769b\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"MID-UPPER ARM CIRCUMFERENCE\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"1343\",\"display\":\"MID-UPPER ARM CIRCUMFERENCE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":22.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"6924f8e9-b17f-460b-bd01-23bd81a53d58\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Temperature (C)\"},{\"system\":\"http://loinc.org\",\"code\":\"8310-5\",\"display\":\"Temperature (C)\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5088\",\"display\":\"Temperature (C)\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":36.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"74655372-0f65-4b23-b468-510fa1c3a5b0\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"d8bc9915-ed4b-4df9-9458-72ca1bc2cd06\",\"display\":\"Syphilis test result for partner\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"5bc3446f-c473-4f6c-ba58-a168ea79f096\",\"display\":\"No clinical Symptoms and Signs\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"787a96e9-9f9c-425e-b8ab-5e13a1d53a15\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dcac04cf-30ab-102d-86b0-7a5022ba4115\",\"display\":\"RETURN VISIT DATE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueDateTime\":\"2021-04-30T00:00:00+03:00\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"850eb0ac-adc7-4ea7-b04a-260c42755ad1\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"dce12b4f-30ab-102d-86b0-7a5022ba4115\",\"display\":\"DATE POSITIVE HIV TEST CONFIRMED\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueDateTime\":\"2021-04-01T00:00:00+03:00\"},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"861b0643-b417-4ef2-b8d9-c005c52048ab\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Systolic blood pressure\"},{\"system\":\"http://loinc.org\",\"code\":\"8480-6\",\"display\":\"Systolic blood pressure\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5085\",\"display\":\"Systolic blood pressure\"},{\"system\":\"http://loinc.org\",\"code\":\"53665-6\",\"display\":\"Systolic blood pressure\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":90.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"8661d536-9446-465c-bd74-64f06fc205a2\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"laboratory\",\"display\":\"Laboratory\"}]}],\"code\":{\"coding\":[{\"code\":\"dca16e53-30ab-102d-86b0-7a5022ba4115\",\"display\":\"HEPATITIS B TEST - QUALITATIVE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"159971AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"waiting for test results\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"8af49d68-dd89-4be1-853d-75d17a7da3b3\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce05b7f-30ab-102d-86b0-7a5022ba4115\",\"display\":\"MEDICATION OR OTHER SIDE EFFECTS\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdd99e5-30ab-102d-86b0-7a5022ba4115\",\"display\":\"NAUSEA\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"8f3f3e37-5e67-453a-831d-16f53290390d\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Duration units\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"1732\",\"display\":\"Duration units\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"1072AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Days\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"1072\",\"display\":\"Days\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"90d645ec-94f8-4e1f-ad1e-36ef0ffd262e\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"8531d1a7-9793-4c62-adab-f6716cf9fabb\",\"display\":\"NUTRITION SUPPORT AND INFANT FEEDING\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"598dba00-b878-474c-9a10-9998f1748228\",\"display\":\"THERAPEUTIC FEEDING\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"979ff44c-bf69-4b63-b25c-901d39552869\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"17def5f6-d6b4-444b-99ed-40eb05d2c4f8\",\"display\":\"Advanced Disease Status\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"7e5beae4-7244-4c73-b4b2-cbaf11771f21\",\"display\":\"No Advanced Disease\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"9a95ccd1-53b1-44f0-924e-1a42af59c3e8\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Respiratory rate\"},{\"system\":\"https://openconceptlab.org/orgs/CIEL/sources/CIEL\",\"code\":\"5242\",\"display\":\"Respiratory rate\"},{\"system\":\"http://loinc.org\",\"code\":\"9279-1\",\"display\":\"Respiratory rate\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":2.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"a47a4d90-ca96-479a-a123-8810f5786333\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"7c9bde8d-a5a7-473f-99d5-4991dc6feb01\",\"display\":\"Other Drug Dispensed Set\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueString\":\"Days, 30.0, Tablet, DTG, 30.0\",\"hasMember\":[{\"reference\":\"Observation/8f3f3e37-5e67-453a-831d-16f53290390d\",\"type\":\"Observation\"},{\"reference\":\"Observation/d5fb05ec-ba24-426d-adb3-ff5987e8c1e8\",\"type\":\"Observation\"},{\"reference\":\"Observation/51de7299-0b09-49ca-950f-08860cb26e01\",\"type\":\"Observation\"},{\"reference\":\"Observation/12c89990-1618-402d-bdc3-58e313d1860e\",\"type\":\"Observation\"},{\"reference\":\"Observation/1ef87b0e-cf8f-44f9-a675-0e5409987099\",\"type\":\"Observation\"}]},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"b4f464a3-9beb-4f2d-a051-8be528b87d74\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dcdfe3ce-30ab-102d-86b0-7a5022ba4115\",\"display\":\"ENTRY POINT INTO HIV CARE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcd7e8e5-30ab-102d-86b0-7a5022ba4115\",\"display\":\"PMTCT\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"b58d4292-722e-4a01-99e3-3ccc419ddfd0\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce02aa1-30ab-102d-86b0-7a5022ba4115\",\"display\":\"TUBERCULOSIS STATUS\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdaccc1-30ab-102d-86b0-7a5022ba4115\",\"display\":\"No signs or symptoms of TB\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"ba80aa99-b3e5-40b4-bc58-36ca617ae172\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"5f86d19d-9546-4466-89c0-6f80c101191b\",\"display\":\"MID-UPPER ARM CIRCUMFERENCE-CODE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"8846c03f-67bf-4aeb-8ca7-39bf79b4ebf3\",\"display\":\"GREEN\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"c2c628f7-516d-41c6-a870-bfd1e9bec618\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce03b2f-30ab-102d-86b0-7a5022ba4115\",\"display\":\"ANTI-RETROVIRAL DRUG ADHERENCE ASSESSMENT CODE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdf1708-30ab-102d-86b0-7a5022ba4115\",\"display\":\"GOOD ADHERENCE\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"cc8806af-a6c0-47c8-b7b6-3fe79c7a948f\",\"status\":\"final\",\"category\":[{\"coding\":[{\"system\":\"http://terminology.hl7.org/CodeSystem/observation-category\",\"code\":\"exam\",\"display\":\"Exam\"}]}],\"code\":{\"coding\":[{\"code\":\"39243cef-b375-44b1-9e79-cbf21bd10878\",\"display\":\"BASELINE STAGE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dc9b8cd1-30ab-102d-86b0-7a5022ba4115\",\"display\":\"WHO STAGE 1 ADULT\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"d5fb05ec-ba24-426d-adb3-ff5987e8c1e8\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"160856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Quantity of medication prescribed per dose\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":30.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"da1199d4-1fa0-4b88-bbd8-9d5751065177\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"c3332e8d-2548-4ad6-931d-6855692694a3\",\"display\":\"BASELINE REGIMEN\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"a779d984-9ccf-4424-a750-47506bf8212b\",\"display\":\"AZT/3TC/DTG\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"e39f9a83-1089-4020-89d4-afc8a3f6dd27\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"cabfa0e9-ddae-438b-a052-6d5c97164242\",\"display\":\"CARE ENTRY POINT SET\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueString\":\"PMTCT\",\"hasMember\":[{\"reference\":\"Observation/b4f464a3-9beb-4f2d-a051-8be528b87d74\",\"type\":\"Observation\"}]},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"e4344951-30f5-424a-8ea1-78b8e8885beb\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dca07f4a-30ab-102d-86b0-7a5022ba4115\",\"display\":\"TESTS ORDERED\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"1eb05918-f50c-4cad-a827-3c78f296a10a\",\"display\":\"Viral Load Test\"},{\"system\":\"http://loinc.org\",\"code\":\"315124004\",\"display\":\"Viral Load Test\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"e76e86ba-adc3-4e37-9d61-d00aaf1d0269\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"e525c286-74b2-4e30-84ac-c4d5f07c503c\",\"display\":\"BASELINE REGIMEN SET\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/d1695b14-cb49-4cab-ba3a-e45c0d77934d\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:03:44.000+03:00\",\"valueString\":\"AZT/3TC/DTG, 2021-04-01\",\"hasMember\":[{\"reference\":\"Observation/da1199d4-1fa0-4b88-bbd8-9d5751065177\",\"type\":\"Observation\"},{\"reference\":\"Observation/3ca1be24-51b2-4bcf-9d93-75e0570274e5\",\"type\":\"Observation\"}]},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"fb08ae34-51c1-4379-b05a-93cff4a1c4fb\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"b0e53f0a-eaca-49e6-b663-d0df61601b70\",\"display\":\"AR REGIMEN DOSE\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueQuantity\":{\"value\":30.0}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"fb9d5a15-c7cd-44f3-8206-df6d15e2b5d4\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"dce0e02a-30ab-102d-86b0-7a5022ba4115\",\"display\":\"SYMPTOM, DIAGNOSIS, OR OPPORTUNISTIC INFECTION\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"dcdebc02-30ab-102d-86b0-7a5022ba4115\",\"display\":\"ULCERS\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"fc1348bf-4eec-4276-83d9-24517ba88c6a\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"37d4ac43-b3b4-4445-b63b-e3acf47c8910\",\"display\":\"TPT STATUS\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueCodeableConcept\":{\"coding\":[{\"code\":\"1090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"display\":\"Never\"}]}},\"request\":{\"method\":\"POST\"}}, {\"resource\":{\"resourceType\":\"Observation\",\"id\":\"fdb56b9f-58ae-45bc-8521-ad6cc09ba4cd\",\"status\":\"final\",\"code\":{\"coding\":[{\"code\":\"0f998893-ab24-4ee4-922a-f197ac5fd6e6\",\"display\":\"Lab Number\"}]},\"subject\":{\"reference\":\"Patient/473b26be-3c41-4916-a9c7-96e8ceca466b\",\"type\":\"Patient\",\"display\":\"Peter Pan (OpenMRS ID: 10000X)\"},\"encounter\":{\"reference\":\"Encounter/71880817-d14e-4e38-9462-a9772d484091\",\"type\":\"Encounter\"},\"effectiveDateTime\":\"2021-04-01T00:00:00+03:00\",\"issued\":\"2021-04-29T16:08:18.000+03:00\",\"valueString\":\"224484864\"},\"request\":{\"method\":\"POST\"}}]}";

        SyncFhirResource syncFHIRResource = new SyncFhirResource();

        syncFHIRResource.setGeneratorProfile(Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa"));
        syncFHIRResource.setSynced(false);
        syncFHIRResource.setResource(sampleResource);
        syncFHIRResource = ugandaEMRSyncService.saveFHIRResource(syncFHIRResource);

        Assert.assertNotNull(syncFHIRResource);
        Assert.assertNotNull(syncFHIRResource.getId());
        Assert.assertEquals(syncFHIRResource.getGeneratorProfile().getName(), "Example Profile");
    }

    @Test
    public void getSyncFHIRResourceBySyncFhirProfileUUID_shouldGetResourcesGeneratedBySyncFhirProfile() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncFhirProfile syncFhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa");

        List<SyncFhirResource> syncFhirResources = ugandaEMRSyncService.getSyncFHIRResourceBySyncFhirProfile(syncFhirProfile, false);

        Assert.assertNotNull(syncFhirResources);
        Assert.assertEquals("Example Profile", syncFhirResources.get(0).getGeneratorProfile().getName());
    }

    @Test
    public void getSyncFHIRResourceById_shouldGetResources() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirResource syncFhirResources = ugandaEMRSyncService.getSyncFHIRResourceById(1);

        Assert.assertNotNull(syncFhirResources);
        Assert.assertEquals("Example Profile", syncFhirResources.getGeneratorProfile().getName());
    }

    @Test
    public void markSyncFHIRResourceSynced_shouldMarkSyncFHIRResourceSynced() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirResource syncFhirResources = ugandaEMRSyncService.getSyncFHIRResourceById(1);
        Assert.assertFalse(syncFhirResources.getSynced());

        SyncFhirResource markedSyncFhirResource = ugandaEMRSyncService.markSyncFHIRResourceSynced(syncFhirResources);

        Assert.assertTrue(markedSyncFhirResource.getSynced());
        Assert.assertNotNull(markedSyncFhirResource.getDateSynced());
        Assert.assertNotNull(markedSyncFhirResource.getExpiryDate());
        Integer daysToDeletion = Math.toIntExact(((markedSyncFhirResource.getExpiryDate().getTime() - markedSyncFhirResource.getDateSynced().getTime()) / (1000 * 60 * 60 * 24)));
        Assert.assertEquals(daysToDeletion, syncFhirResources.getGeneratorProfile().getDurationToKeepSyncedResources());

    }

    @Test
    public void saveSyncFhirProfileLog_shouldSaveSyncFhirProfileLog() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);


        SyncFhirProfileLog syncFhirProfileLog = new SyncFhirProfileLog();

        syncFhirProfileLog.setLastGenerationDate(new Date());
        syncFhirProfileLog.setResourceType("Encounter");
        syncFhirProfileLog.setProfile(Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa"));
        syncFhirProfileLog.setNumberOfResources(5);
        SyncFhirProfileLog syncFhirProfileLog1 = ugandaEMRSyncService.saveSyncFhirProfileLog(syncFhirProfileLog);
        Assert.assertNotNull(syncFhirProfileLog1);
        Assert.assertNotNull(syncFhirProfileLog1.getId());
        Assert.assertEquals(syncFhirProfileLog1.getResourceType(), "Encounter");
    }


    @Test
    public void getSyncFhirProfileLogByProfileAndResourceName_shouldGetSyncFhirProfileLog() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirProfileLog syncFhirProfileLog = ugandaEMRSyncService.getLatestSyncFhirProfileLogByProfileAndResourceName(Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa"), "Encounter");

        Assert.assertNotNull(syncFhirProfileLog);

    }

    @Test
    public void getSyncFHIRCaseBySyncFhirProfileAndPatient_ShouldGetSyncFHIRCase() {
        String patientUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
        String caseIdentifier = "ART-MALE-1";
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        Patient patient = Context.getPatientService().getPatientByUuid(patientUID);
        SyncFhirProfile syncFhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa");
        SyncFhirCase syncFHIRCase = ugandaEMRSyncService.getSyncFHIRCaseBySyncFhirProfileAndPatient(syncFhirProfile, patient, caseIdentifier);

        Assert.assertNotNull(syncFHIRCase);
        Assert.assertEquals(syncFHIRCase.getPatient().getUuid(), patientUID);

    }

    @Test
    public void saveSyncFHIRCase_shouldSyncFHIRCase() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        SyncFhirCase syncFHIRCase = new SyncFhirCase();
        Date date = new Date();

        syncFHIRCase.setPatient(Context.getPatientService().getPatient(2));
        syncFHIRCase.setProfile(ugandaEMRSyncService.getSyncFhirProfileByUUID("c91b12c3-65fe-4b1c-aba4-99e3a7e58cfa"));
        syncFHIRCase.setCaseIdentifier(Context.getPatientService().getPatient(2).getPatientIdentifier(4).getIdentifier());
        syncFHIRCase.setLastUpdateDate(date);

        SyncFhirCase syncFhirCase1 = ugandaEMRSyncService.saveSyncFHIRCase(syncFHIRCase);

        Assert.assertNotNull(syncFhirCase1);
        Assert.assertNotNull(syncFhirCase1.getCaseId());
        Assert.assertNotNull(syncFhirCase1.getCaseIdentifier());
        Assert.assertNotNull("ART-MALE-1", syncFhirCase1.getCaseIdentifier());
    }


}
