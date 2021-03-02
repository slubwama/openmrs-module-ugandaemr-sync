package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.util.ReportUtil;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.*;

/**
 * Posts Analytics data to the central server
 */

@Component
public class SendAnalyticsDataToCentralServerTask extends AbstractTask {

    protected Log log = LogFactory.getLog(getClass());
    Date startDate;
    Date endDate;
    Date lastSubmissionDateSet;

    UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();

    SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();

    @Autowired
    @Qualifier("reportingReportDefinitionService")
    protected ReportDefinitionService reportingReportDefinitionService;

    @Override
    public void execute() {
        Date todayDate = new Date();

        Properties properties=Context.getService(UgandaEMRSyncService.class).getUgandaEMRProperties();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        LocalDate today = LocalDate.parse(dateFormat.format(todayDate));
        if (!isGpAnalyticsServerUrlSet()) {
            return;
        }
        if (!isGpDhis2OrganizationUuidSet()) {
            return;
        }

        String analyticsServerUrlEndPoint = syncGlobalProperties.getGlobalProperty(GP_ANALYTICS_SERVER_URL);
        String analyticsBaseUrl = ugandaEMRHttpURLConnection.getBaseURL(analyticsServerUrlEndPoint);

        String strSubmissionDate = Context.getAdministrationService()
                .getGlobalPropertyObject(GP_ANALYTICS_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE).getPropertyValue();


        if (!isBlank(strSubmissionDate)) {
            LocalDate gpSubmissionDate = null;

            try {
                gpSubmissionDate = LocalDate.parse(strSubmissionDate, dateFormatter);
            } catch (Exception e) {
                log.info("Error parsing last successful submission date " + strSubmissionDate + e);
                e.printStackTrace();
            }
            if (gpSubmissionDate.getMonth() == (today.getMonth()) && gpSubmissionDate.getYear() == today.getYear()) {
                log.info("Last successful submission was on" + strSubmissionDate
                        + "so this task will not run again today. If you need to send data, run the task manually."
                        + System.lineSeparator());
                return;
            } else {

                Period diff = Period.between(
                        gpSubmissionDate.withDayOfMonth(1), today.withDayOfMonth(1));
                if (diff.getMonths() >= 1) {

                    //setting start and end date for least month data to be generated
                    int monthsBetween = diff.getMonths();
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, -monthsBetween);
                    cal.set(Calendar.DATE, 1);
                    startDate = cal.getTime();
                    cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));

                    endDate = cal.getTime();

                    //setting last submission month to be the next month
                    Calendar lastSubmissionDate = Calendar.getInstance();
                    lastSubmissionDate.add(Calendar.MONTH, -(monthsBetween - 1));
                    lastSubmissionDate.set(Calendar.DATE, 1);
                    lastSubmissionDateSet = lastSubmissionDate.getTime();
                }
            }
        } else {
            //Formatting start and end dates of the report for first time running
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            cal.set(Calendar.DATE, 1);
            startDate = cal.getTime();

            cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));

            endDate = cal.getTime();
            lastSubmissionDateSet = todayDate;
        }
        //Check internet connectivity
        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            return;
        }

        //Check destination server availability
        if (!ugandaEMRHttpURLConnection.isServerAvailable(analyticsBaseUrl)) {
            return;
        }

        if (properties.getProperty(SYNC_METRIC_DATA).equalsIgnoreCase("true") && properties.getProperty(GP_DHIS2_ORGANIZATION_UUID).equalsIgnoreCase(syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID))) {
            log.info("Sending analytics data to central server ");
            String bodyText = getAnalyticsDataExport();
            HttpResponse httpResponse = ugandaEMRHttpURLConnection.httpPost(analyticsServerUrlEndPoint, bodyText, syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID), syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID));
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {

                ReportUtil.updateGlobalProperty(GP_ANALYTICS_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE,
                        dateTimeFormat.format(lastSubmissionDateSet));
                log.info("Analytics data has been sent to central server");
            } else {
                log.info("Http response status code: " + httpResponse.getStatusLine().getStatusCode() + ". Reason: "
                        + httpResponse.getStatusLine().getReasonPhrase());
            }
        } else {
            log.info("Analytics data has not been sent to central server. Check whether you have a ugandaemr-settings.properties file and syncmetrictsdata is set to true");
        }
    }

    private String getAnalyticsDataExport() {
        ReportDefinitionService reportDefinitionService = Context.getService(ReportDefinitionService.class);
        String strOutput = new String();

        try {
            ReportDefinition rd = reportDefinitionService.getDefinitionByUuid(ANALYTICS_DATA_EXPORT_REPORT_DEFINITION_UUID);
            if (rd == null) {
                throw new IllegalArgumentException("unable to find Analytics Data Export report with uuid "
                        + ANALYTICS_DATA_EXPORT_REPORT_DEFINITION_UUID);
            }
            String reportRendergingMode = JSON_REPORT_RENDERER_TYPE + "!" + ANALYTIC_REPORT_JSON_DESIGN_UUID;
            RenderingMode renderingMode = new RenderingMode(reportRendergingMode);
            if (!renderingMode.getRenderer().canRender(rd)) {
                throw new IllegalArgumentException("Unable to render Analytics Data Export with " + reportRendergingMode);
            }
            Map<String, Object> parameterValues = new HashMap<String, Object>();

            parameterValues.put("startDate", startDate);
            parameterValues.put("endDate", endDate);
            EvaluationContext context = new EvaluationContext();
            context.setParameterValues(parameterValues);
            ReportData reportData = reportDefinitionService.evaluate(rd, context);
            ReportRequest reportRequest = new ReportRequest();
            reportRequest.setReportDefinition(new Mapped<ReportDefinition>(rd, context.getParameterValues()));
            reportRequest.setRenderingMode(renderingMode);
            File file = new File(OpenmrsUtil.getApplicationDataDirectory() + ANALYTICS_JSON_FILE_NAME);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            renderingMode.getRenderer().render(reportData, renderingMode.getArgument(), fileOutputStream);

            strOutput = this.readOutputFile(strOutput);
            log.info(strOutput);
        } catch (Exception e) {
            log.info("Error rendering the contents of the Analytics data export report to"
                    + OpenmrsUtil.getApplicationDataDirectory() + ANALYTICS_JSON_FILE_NAME + e.toString());
            e.printStackTrace();
        }

        return strOutput;
    }


    public String readOutputFile(String strOutput) throws Exception {
        FileInputStream fstreamItem = new FileInputStream(OpenmrsUtil.getApplicationDataDirectory() + ANALYTICS_JSON_FILE_NAME);
        DataInputStream inItem = new DataInputStream(fstreamItem);
        BufferedReader brItem = new BufferedReader(new InputStreamReader(inItem));
        String phraseItem;

        if (!(phraseItem = brItem.readLine()).isEmpty()) {
            strOutput = strOutput + phraseItem + System.lineSeparator();
            while ((phraseItem = brItem.readLine()) != null) {
                strOutput = strOutput + phraseItem + System.lineSeparator();
            }
        }

        fstreamItem.close();

        return strOutput;
    }

    public boolean isGpAnalyticsServerUrlSet() {
        if (isBlank(syncGlobalProperties.getGlobalProperty(GP_ANALYTICS_SERVER_URL))) {
            log.info("Analytics server URL is not set");
            return false;
        }
        return true;
    }

    public boolean isGpDhis2OrganizationUuidSet() {
        if (isBlank(syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID))) {
            log.info("DHIS2 Organization UUID is not set");
            return false;
        }
        return true;
    }
}
