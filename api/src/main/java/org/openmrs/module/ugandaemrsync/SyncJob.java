package org.openmrs.module.ugandaemrsync;

import org.openmrs.module.ugandaemrsync.server.SyncDataRecord;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 *
 */
public class SyncJob extends AbstractTask {
	
	public void execute() {
		SyncDataRecord syncDataRecord = new SyncDataRecord();
		
		syncDataRecord.syncData();
		
	}
}
