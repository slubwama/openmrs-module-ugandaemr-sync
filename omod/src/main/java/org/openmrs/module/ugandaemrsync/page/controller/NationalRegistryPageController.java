package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class NationalRegistryPageController {

    SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
    protected final Log log = LogFactory.getLog(NationalRegistryPageController.class);

    public void controller(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride",
            required = false) String breadcrumbOverride) {

        String districtUrl = syncGlobalProperties.getGlobalProperty("ugandaemrsync.nhfr.district.url");
        String facilityUrl = syncGlobalProperties.getGlobalProperty("ugandaemrsync.nhfr.facility.url");

        pageModel.put("breadcrumbOverride", breadcrumbOverride);
        pageModel.put("districtUrl", districtUrl);
        pageModel.put("facilityUrl", facilityUrl);
    }

}
