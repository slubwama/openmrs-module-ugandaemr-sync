package org.openmrs.module.ugandaemrsync.tasks;

import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.server.SyncFHIRRecord;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.List;

public class SendFhirResourceTask extends AbstractTask {
    @Override
    public void execute() {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncFHIRRecord syncFHIRRecord = new SyncFHIRRecord();
        List<SyncFhirProfile> syncFhirProfiles = ugandaEMRSyncService.getAllSyncFhirProfile();

        for (SyncFhirProfile syncFhirProfile : syncFhirProfiles) {
            syncFHIRRecord.sendFhirResourcesTo(syncFhirProfile);
        }
    }
}
