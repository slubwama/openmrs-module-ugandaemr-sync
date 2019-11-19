package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public class SyncTaskPageController {

	protected final org.apache.commons.logging.Log log = LogFactory.getLog(SyncTaskPageController.class);

	public SyncTaskPageController() {
	}

	public void controller(@SpringBean PageModel pageModel,
	        @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
	        UiSessionContext sessionContext, PageModel model, UiUtils ui) {
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
		List<SyncTask> syncTasks = ugandaEMRSyncService.getAllSyncTask();
		pageModel.put("syncTask", syncTasks);
		pageModel.put("breadcrumbOverride", breadcrumbOverride);
	}
}
