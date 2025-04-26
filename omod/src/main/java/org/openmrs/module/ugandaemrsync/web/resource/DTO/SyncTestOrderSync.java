package org.openmrs.module.ugandaemrsync.web.resource.DTO;

import org.openmrs.Order;

import java.util.List;
import java.util.Map;

public class SyncTestOrderSync {
    List<Order> orderList;
    List<Map> responseList;

    String uuid;

    public List<Order> getOrderList() {
        return orderList;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
    }

    public List<Map> getResponseList() {
        return responseList;
    }

    public void setResponseList(List<Map> responseList) {
        this.responseList = responseList;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
