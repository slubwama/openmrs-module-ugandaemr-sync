package org.openmrs.module.ugandaemrsync;

import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.server.SyncDataRecord;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * Created by lubwamasamuel on 21/02/2017.
 */
public class SyncJob extends AbstractTask {
	
	public void execute() {
		SyncDataRecord syncDataRecord = new SyncDataRecord();
		
		syncDataRecord.syncData();
		
	}
}
