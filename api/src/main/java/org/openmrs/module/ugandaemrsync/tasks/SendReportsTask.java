package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
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
     boolean sent=false;

     String previewBody;

     SimpleObject response;

    public SendReportsTask() {
        super();
    }

    public SendReportsTask(String previewBody) {
        this.previewBody = previewBody;
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

        String reportsServerUrlEndPoint = syncGlobalProperties.getGlobalProperty(GP_SEND_NEXT_GEN_REPORTS_SERVER_URL);
        String reportsBaseUrl = ugandaEMRHttpURLConnection.getBaseURL(reportsServerUrlEndPoint);


        //Check internet connectivity
        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            return;
        }

        //Check destination server availability
        if (!ugandaEMRHttpURLConnection.isServerAvailable(reportsBaseUrl)) {
            return;
        }
        log.info("Sending Report to server ");

        if(previewBody!="") {
            HttpResponse httpResponse = ugandaEMRHttpURLConnection.httpPost(reportsServerUrlEndPoint, previewBody, syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID), syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID));
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                sent = true;
                log.info("Report  has been sent to central server");
            } else {
                log.info("Http response status code: " + httpResponse.getStatusLine().getStatusCode() + ". Reason: " + httpResponse.getStatusLine().getReasonPhrase());
            }
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

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public String getPreviewBody() {
        return previewBody;
    }

    public void setPreviewBody(String previewBody) {
        this.previewBody = previewBody;
    }


    public boolean isGpDhis2OrganizationUuidSet() {
        if (isBlank(syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID))) {
            log.info("DHIS2 Organization UUID is not set");
            return false;
        }
        return true;
    }



}
