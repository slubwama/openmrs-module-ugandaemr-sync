package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.*;

/**
 * Posts Analytics data to the central server
 */

@Component
public class SendReportsTask extends AbstractTask {

    protected Log log = LogFactory.getLog(getClass());
     boolean sent= false;
     String responseMessage="";
     int responseCode ;

     String previewBody;

     SyncTaskType syncTaskType;

     SimpleObject response;

    public SendReportsTask() {
        super();
    }


    public SendReportsTask(String previewBody,SyncTaskType syncTaskType) {
        this.previewBody = previewBody;
        this.syncTaskType = syncTaskType;
    }

    UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
    SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();

    @Override
    public void execute() {


        if (!isGpReportsServerUrlSet()) {
            return;
        }
        if (!isGpDhis2OrganizationUuidSet()) {
            return;
        }

        //Check internet connectivity
        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            return;
        }

        log.info("Sending Report to server ");


        if(previewBody!="") {
            JSONArray array = new JSONArray(previewBody);
            for(int i =0 ; i < array.length();i++){
                String payload = array.getJSONObject(i).toString();
                sendPost(payload);
            }
        }
    }

    public void sendPost(String requestBody){
        HttpResponse httpResponse = ugandaEMRHttpURLConnection.httpPost(syncTaskType.getUrl(), requestBody, syncTaskType.getUrlUserName(), syncTaskType.getUrlPassword());
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            sent = true;
            log.info("Report  has been sent to central server");
            responseMessage = " Data Successfully Sent";
        } else {
            log.info("Http response status code: " + httpResponse.getStatusLine().getStatusCode() + ". Reason: " + httpResponse.getStatusLine().getReasonPhrase());
            responseMessage = httpResponse.getStatusLine().getReasonPhrase();
            responseCode = httpResponse.getStatusLine().getStatusCode();
        }
    }



    public boolean isGpReportsServerUrlSet() {
        if (isBlank(syncGlobalProperties.getGlobalProperty(GP_SEND_NEXT_GEN_REPORTS_SERVER_URL))) {
            log.info("Send Reports server URL is not set");
            return false;
        }
        return true;
    }

    public boolean isSent() {
        return sent;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getPreviewBody() {
        return previewBody;
    }

    public void setPreviewBody(String previewBody) {
        this.previewBody = previewBody;
    }

    public int getResponseCode() { return responseCode; }

    public void setResponseCode(int responseCode) { this.responseCode = responseCode; }

    public boolean isGpDhis2OrganizationUuidSet() {
        if (isBlank(syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID))) {
            log.info("DHIS2 Organization UUID is not set");
            return false;
        }
        return true;
    }



}
