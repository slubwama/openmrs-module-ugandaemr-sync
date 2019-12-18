package org.openmrs.module.ugandaemrsync.tasks;

import org.openmrs.module.ugandaemrsync.server.SyncDataRecord;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * A Task to Sync all data to the central server.
 */
public class SendAllDataCentralServerTask extends AbstractTask {

	public void execute() {
		UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
		if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
			return;
		}
		SyncDataRecord syncDataRecord = new SyncDataRecord();

		syncDataRecord.syncData();

	}
}
