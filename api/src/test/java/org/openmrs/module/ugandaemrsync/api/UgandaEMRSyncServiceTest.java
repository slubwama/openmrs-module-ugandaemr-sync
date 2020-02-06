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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.dao.UgandaEMRSyncDao;
import org.openmrs.module.ugandaemrsync.api.impl.UgandaEMRSyncServiceImpl;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
}
