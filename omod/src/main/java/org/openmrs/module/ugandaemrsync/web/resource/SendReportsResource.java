package org.openmrs.module.ugandaemrsync.web.resource;

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.tasks.SendReportsTask;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_SEND_HMIS_REPORTS_SERVER_REPORT_UUIDS;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_SEND_NEXT_GEN_REPORTS_SERVER_REPORT_UUIDS;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SEND_HMIS_REPORTS_SYNC_TASK_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SEND_MER_REPORTS_SYNC_TASK_TYPE_UUID;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/sendreport")
public class SendReportsResource {


    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Object sendReport(@RequestParam("uuid")String reportUuid, @RequestBody String body) {
        return sendData(body,reportUuid);
    }
    public SimpleObject sendData(String body, String uuid){

        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();

        String hmisReportUuids=  syncGlobalProperties.getGlobalProperty(GP_SEND_HMIS_REPORTS_SERVER_REPORT_UUIDS);
        String merReportUuids=  syncGlobalProperties.getGlobalProperty(GP_SEND_NEXT_GEN_REPORTS_SERVER_REPORT_UUIDS);
        List<String> hmisReports = Arrays.asList(hmisReportUuids.split(","));
        List<String> merReports = Arrays.asList(merReportUuids.split(","));
        SendReportsTask sendReportsTask;
        String response="";
        String status="";
        String jsonData= body;

        if(jsonData!=null) {
            SyncTaskType MERsyncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(SEND_MER_REPORTS_SYNC_TASK_TYPE_UUID);
            SyncTaskType HMISsyncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(SEND_HMIS_REPORTS_SYNC_TASK_TYPE_UUID);

            SyncTaskType syncTaskType = new SyncTaskType();
            if (hmisReports.contains(uuid)) {
                syncTaskType = HMISsyncTaskType;
            } else if (merReports.contains(uuid)) {
                syncTaskType = MERsyncTaskType;
            }

            if (syncTaskType != null) {
                sendReportsTask = new SendReportsTask(jsonData, syncTaskType);
                sendReportsTask.execute();
                if (sendReportsTask.isSent()) {
                    response = "Report successfully sent";
                    status = "success";
                } else {
                    response = sendReportsTask.getResponseMessage();
                    status = "failure";
                }
                SyncTask newSyncTask = new SyncTask();
                newSyncTask.setDateSent(new Date());
                newSyncTask.setCreator(Context.getUserService().getUser(1));
                newSyncTask.setSentToUrl(syncTaskType.getUrl());
                newSyncTask.setRequireAction(true);
                newSyncTask.setActionCompleted(false);
                newSyncTask.setStatus(status);
                newSyncTask.setStatusCode(sendReportsTask.getResponseCode());
                newSyncTask.setSyncTask(getReportDefinitionService().getDefinitionByUuid(uuid).getName());
                newSyncTask.setSyncTaskType(syncTaskType);
                ugandaEMRSyncService.saveSyncTask(newSyncTask);
            }else {
                response = "Missing Global Properties for reports to be exchanged";
                status="failure";
            }

        }else{
            response = "No Available Data to send";
            status="failure";
        }
        return SimpleObject.create("status", status, "message", response);
    }

    private ReportDefinitionService getReportDefinitionService() {
        return Context.getService(ReportDefinitionService.class);
    }
}
