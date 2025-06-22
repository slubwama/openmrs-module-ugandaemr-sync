package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.LAB_RESULT_PULL_SYNC_TASK_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.LAB_ORDER_QUERY;

public class ReceiveLabResultFromALISTask extends AbstractTask {
    protected final Log log = LogFactory.getLog(ReceiveLabResultFromALISTask.class);

    @Override
    public void execute() {
        UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            return;
        }

        for (SyncTask syncTask : ugandaEMRSyncService.getIncompleteActionSyncTask(LAB_RESULT_PULL_SYNC_TASK_TYPE_UUID)) {

            Order order = getOrder(syncTask.getSyncTask());
            Map results = new HashMap();

            if (results != null && results.size() > 0 ) {
                Map reasonReference = (Map) results.get("reasonReference");
                ArrayList<Map> result = (ArrayList<Map>) reasonReference.get("result");
                //Save Lab Results
                if (order.getEncounter() != null) {
                   ugandaEMRSyncService.addTestResultsToEncounter("", order);
                    syncTask.setActionCompleted(true);
                    ugandaEMRSyncService.saveSyncTask(syncTask);
                    try {
                        Context.getOrderService().discontinueOrder(order, "Completed", new Date(), order.getOrderer(), order.getEncounter());
                    } catch (Exception e) {
                        log.error("Failed to discontinue order", e);
                    }
                }
            }
        }
    }

    public Order getOrder(String orderNumber) {
        OrderService orderService = Context.getOrderService();
        List list = Context.getAdministrationService().executeSQL(String.format(LAB_ORDER_QUERY, orderNumber), true);
        if (list.size() > 0) {
            for (Object o : list) {
                return orderService.getOrder(Integer.parseUnsignedInt(((ArrayList) o).get(0).toString()));
            }
        }
        return null;
    }
}