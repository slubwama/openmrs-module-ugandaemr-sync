package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

public class SendReportsPageController {

    protected final Log log = LogFactory.getLog(SendReportsPageController.class);

    public void controller(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride",
            required = false) String breadcrumbOverride) {

        pageModel.put("breadcrumbOverride", breadcrumbOverride);
    }


}
