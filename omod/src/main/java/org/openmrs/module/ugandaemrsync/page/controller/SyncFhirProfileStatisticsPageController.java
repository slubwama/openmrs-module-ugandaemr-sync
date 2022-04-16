package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIR_FILTER_OBJECT_STRING;

public class SyncFhirProfileStatisticsPageController {

    protected final org.apache.commons.logging.Log log = LogFactory.getLog(SyncTaskPageController.class);

    public SyncFhirProfileStatisticsPageController() {
    }

    public void controller(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride, UiSessionContext sessionContext, PageModel model, UiUtils ui) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        pageModel.put("syncFhirProfiles", ugandaEMRSyncService.getAllSyncFhirProfile());
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
    }
}
