package org.openmrs.module.ugandaemrsync.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.*;

public class NationalRegistryFragmentController {

    UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

    SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
    protected final Log log = LogFactory.getLog(NationalRegistryFragmentController.class);

    public void controller(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride",
            required = false) String breadcrumbOverride) {

    }

    public SimpleObject saveFacilityDetails(HttpServletRequest request, @RequestParam("name") String name, @RequestParam("id")String id,@RequestParam("dhisuuid")String dhisuuid){

        String status="";

        if(name!="" & id!=""& dhisuuid!=""){
            syncGlobalProperties.setGlobalProperty(GP_NHFR_UNIQUE_IDENTIFIER,id);
            syncGlobalProperties.setGlobalProperty(GP_DHIS2_ORGANIZATION_UUID,dhisuuid);
            syncGlobalProperties.setGlobalProperty(GP_FACILITY_NAME,name);
            status="success";

        }
        return SimpleObject.create("status", status);
    }


}
