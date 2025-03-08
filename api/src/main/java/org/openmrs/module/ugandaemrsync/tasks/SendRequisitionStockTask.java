package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.server.SyncFHIRRecord;
import org.openmrs.scheduler.tasks.AbstractTask;

public class SendRequisitionStockTask extends AbstractTask {
	
	Log log = LogFactory.getLog(SyncFHIRRecord.class);
	
	@Override
	public void execute() {
		UgandaEMRSyncService ugandaCaresService = Context.getService(UgandaEMRSyncService.class);
		ugandaCaresService.getSendRequisitionStock();
	}
}
