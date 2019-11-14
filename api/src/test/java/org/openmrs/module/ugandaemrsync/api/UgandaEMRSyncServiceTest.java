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
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.dao.UgandaEMRSyncDao;
import org.openmrs.module.ugandaemrsync.api.impl.UgandaEMRSyncServiceImpl;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * This is a unit test, which verifies logic in UgandaEMRSyncService. It doesn't extend
 * BaseModuleContextSensitiveTest, thus it is run without the in-memory DB and Spring context.
 */
public class UgandaEMRSyncServiceTest extends BaseModuleContextSensitiveTest {
    protected static final String UGANDAEMRSYNC_GLOBALPROPERTY_DATASET_XML = "org/openmrs/module/ugandaemrsync/include/globalPropertiesDataSet.xml";
    @InjectMocks
    UgandaEMRSyncServiceImpl basicModuleService;

    @Mock
    UgandaEMRSyncDao dao;

    @Mock
    UserService userService;

    @Mock
    AdministrationService administrationService;

    @Before
    public void initialize() throws Exception {
        executeDataSet(UGANDAEMRSYNC_GLOBALPROPERTY_DATASET_XML);
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
        UgandaEMRSyncService ugandaEMRSyncService=Context.getService(UgandaEMRSyncService.class);
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

        List<SyncTaskType> syncTaskTypes=ugandaEMRSyncService.getAllSyncTaskType();

        Assert.assertEquals(1,syncTaskTypes.size());
    }

}
