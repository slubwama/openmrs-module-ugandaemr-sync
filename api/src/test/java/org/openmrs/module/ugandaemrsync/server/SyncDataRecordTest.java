package org.openmrs.module.ugandaemrsync.server;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SyncDataRecordTest {

    @Test
    public void isConnectionAvailable() {
        SyncDataRecord syncDataRecord = new SyncDataRecord();
        List<String> stringList = Arrays.asList("fbe1fd2b-7f1e-4878-83bf-14e57e7f037a");
/*		try {
			syncDataRecord.processFHIRData(stringList,"Encounter");
		} catch (Exception e) {
			e.printStackTrace();
		}*/
    }
}
