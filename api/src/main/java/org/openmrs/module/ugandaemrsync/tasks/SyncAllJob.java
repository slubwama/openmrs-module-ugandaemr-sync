package org.openmrs.module.ugandaemrsync.tasks;

import org.openmrs.module.ugandaemrsync.server.SyncDataRecord;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 *A Task to Sync all data to the central server.
 */
public class SyncAllJob extends AbstractTask {
	
	public void execute() {
		SyncDataRecord syncDataRecord = new SyncDataRecord();
		
		syncDataRecord.syncData();
		
	}
}
