package org.openmrs.module.ugandaemrsync.server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public class SyncFHIRRecordTest extends BaseModuleContextSensitiveTest {

    protected static final String UGANDAEMR_STANDARD_DATASET_XML = "org/openmrs/module/ugandaemrsync/include/globalPropertiesDataSet.xml";


    @Before
    public void setup() throws Exception {
        executeDataSet(UGANDAEMR_STANDARD_DATASET_XML);
    }

    @Test
    public void addOrganizationToRecord_shouldReturnJsonStringWithAManagingOrganization() {
        SyncFHIRRecord syncFHIRRecord=new SyncFHIRRecord();

        String managingOrganizationJsonString = syncFHIRRecord.addOrganizationToRecord("{}");

        Assert.assertNotEquals(managingOrganizationJsonString,"{}");
        Assert.assertEquals(managingOrganizationJsonString,"{\"managingOrganization\":{\"reference\":\"Organization/7744yxP\"}}");
    }
}
