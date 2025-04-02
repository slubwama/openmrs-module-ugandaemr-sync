package org.openmrs.module.ugandaemrsync.tasks;

import netscape.javascript.JSObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncFHIRRecord;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SendDrugPrescriptionsToEAFYATask extends AbstractTask {

    Log log = LogFactory.getLog(SyncFHIRRecord.class);

    @Override
    public void execute() {
        UgandaEMRSyncService ugandaEMRSyncService=Context.getService(UgandaEMRSyncService.class);
        ugandaEMRSyncService.sendPrescription();
    }
}
