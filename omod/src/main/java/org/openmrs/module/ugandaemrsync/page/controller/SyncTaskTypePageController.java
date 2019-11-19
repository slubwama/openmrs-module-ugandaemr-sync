package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class SyncTaskTypePageController {

    protected final org.apache.commons.logging.Log log = LogFactory.getLog(SyncTaskPageController.class);

    public SyncTaskTypePageController() {
    }

    public void controller(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride, UiSessionContext sessionContext, PageModel model, UiUtils ui) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        List<SyncTaskType> syncTaskTypes = ugandaEMRSyncService.getAllSyncTaskType();
        pageModel.put("syncTaskTypes", syncTaskTypes);
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
    }

    public void post(@SpringBean PageModel pageModel, @RequestParam(value = "returnUrl", required = false) String returnUrl, @RequestParam(value = "syncTaskTypeName", required = false) String name, @RequestParam(value = "syncTaskTypeId", required = false) String syncTaskTypeId, @RequestParam(value = "dataType", required = false) String dataType, @RequestParam(value = "url", required = false) String url, @RequestParam(value = "username", required = false) String username, @RequestParam(value = "password", required = false) String password, @RequestParam(value = "token", required = false) String token, @RequestParam(value = "dataTypeId", required = false) String dataTypeId, UiSessionContext uiSessionContext, UiUtils uiUtils, HttpServletRequest request) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);


        if (syncTaskTypeId.equals("")) {
            SyncTaskType neSyncTaskType = new SyncTaskType();
            neSyncTaskType.setDateCreated(new Date());
            neSyncTaskType.setName(name);
            neSyncTaskType.setDataType(dataType);
            neSyncTaskType.setUrl(url);
            neSyncTaskType.setUrlUserName(username);
            neSyncTaskType.setUrlPassword(password);
            neSyncTaskType.setUrlToken(token);
            neSyncTaskType.setDataTypeId(dataTypeId);
            neSyncTaskType.setCreator(Context.getAuthenticatedUser());
            ugandaEMRSyncService.saveSyncTaskType(neSyncTaskType);
        } else {
            SyncTaskType syncTaskType = Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(syncTaskTypeId);
            syncTaskType.setSyncTaskTypeId(syncTaskType.getSyncTaskTypeId());
            syncTaskType.setName(name);
            syncTaskType.setDataType(dataType);
            syncTaskType.setUrl(url);
            syncTaskType.setUrlUserName(username);
            syncTaskType.setUrlPassword(password);
            syncTaskType.setUrlToken(token);
            syncTaskType.setDataTypeId(dataTypeId);
            syncTaskType.setDateChanged(new Date());
            syncTaskType.setChangedBy(Context.getAuthenticatedUser());
            ugandaEMRSyncService.saveSyncTaskType(syncTaskType);
        }

        pageModel.put("syncTaskTypes", ugandaEMRSyncService.getAllSyncTaskType());
    }
}
