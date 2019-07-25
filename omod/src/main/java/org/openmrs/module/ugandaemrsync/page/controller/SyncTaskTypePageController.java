package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

public class SyncTaskTypePageController {
	
	protected final org.apache.commons.logging.Log log = LogFactory.getLog(getClass());
	
	public SyncTaskTypePageController() {
	}
	
	public void controller(@SpringBean PageModel pageModel,
	        @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
	        UiSessionContext sessionContext, PageModel model, UiUtils ui) {
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
		List<SyncTaskType> syncTaskTypes = ugandaEMRSyncService.getAllSyncTaskType();
		pageModel.put("syncTaskTypes", syncTaskTypes);
		pageModel.put("breadcrumbOverride", breadcrumbOverride);
	}
	
	public void post(@SpringBean PageModel pageModel, @RequestParam(value = "returnUrl", required = false) String returnUrl,
	        @RequestParam(value = "name", required = false) String name,
	        @RequestParam(value = "datatype", required = false) String dataType,
	        @RequestParam(value = "datatypeid", required = false) String dataTypeId, UiSessionContext uiSessionContext,
	        UiUtils uiUtils, HttpServletRequest request) {
		UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
		SyncTaskType syncTaskType = new SyncTaskType();
		syncTaskType.setDateCreated(new Date());
		syncTaskType.setName(name);
		syncTaskType.setDataType(dataType);
		syncTaskType.setDataTypeId(dataTypeId);
		syncTaskType.setCreator(Context.getAuthenticatedUser());
		ugandaEMRSyncService.saveSyncTaskType(syncTaskType);
		pageModel.put("syncTaskTypes", ugandaEMRSyncService.getAllSyncTaskType());
	}
}
