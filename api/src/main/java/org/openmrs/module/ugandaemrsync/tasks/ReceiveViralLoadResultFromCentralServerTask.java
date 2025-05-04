package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;


import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_SYNC_TASK_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_RESULT_PULL_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VL_RECEIVE_RESULT_FHIR_JSON_STRING;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_ORDER_QUERY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.PATIENT_IDENTIFIER_TYPE;

public class ReceiveViralLoadResultFromCentralServerTask extends AbstractTask {
    protected final Log log = LogFactory.getLog(ReceiveViralLoadResultFromCentralServerTask.class);

    @Override
    public void execute() {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            return;
        }

        for (SyncTask syncTask : ugandaEMRSyncService.getIncompleteActionSyncTask(VIRAL_LOAD_SYNC_TASK_TYPE_UUID)) {
            ugandaEMRSyncService.requestLabResult(null, syncTask);

        }
    }
}
