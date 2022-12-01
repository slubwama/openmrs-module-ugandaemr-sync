package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID;

public class SentSmsPageController {

	protected final org.apache.commons.logging.Log log = LogFactory.getLog(SentSmsPageController.class);

	UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

	SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();

	public SentSmsPageController() {
	}

	public void controller(@SpringBean PageModel pageModel,
	        @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
	        UiSessionContext sessionContext, PageModel model, UiUtils ui) {
		if (isGpDhis2OrganizationUuidSet()&&ugandaEMRHttpURLConnection.isConnectionAvailable()) {

			UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
			SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID("d63cb4b5-97ba-4380-aba9-d3f60634cd7a");
			String username = syncTaskType.getUrlUserName();
			String password = syncTaskType.getUrlPassword();
			String url = syncTaskType.getUrl()+syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID);
			String results = "";
			try {

				Map resultMap = ugandaEMRHttpURLConnection.getByWithBasicAuth(url,username,password,"String");
				results = (String) resultMap.get("result");

			} catch (Exception e) {
				log.error("Failed to fetch results", e);
			}
			pageModel.put("results", results);
		}else{
			pageModel.put("results", "");
		}
	}

	public boolean isGpDhis2OrganizationUuidSet() {
		if (isBlank(syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID))) {
			log.info("DHIS2 Organization UUID is not set");
			return false;
		}
		return true;
	}
}
