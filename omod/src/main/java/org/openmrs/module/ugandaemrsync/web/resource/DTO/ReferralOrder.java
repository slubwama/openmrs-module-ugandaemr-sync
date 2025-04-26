package org.openmrs.module.ugandaemrsync.web.resource.DTO;

import org.openmrs.Order;
import org.openmrs.module.ugandaemrsync.model.SyncTask;

import java.io.Serializable;
import java.util.List;

public class ReferralOrder implements Serializable {

    String uuid;
    private Order order;

    private SyncTask syncTask;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public SyncTask getSyncTask() {
        return syncTask;
    }

    public void setSyncTask(SyncTask syncTask) {
        this.syncTask = syncTask;
    }
}
