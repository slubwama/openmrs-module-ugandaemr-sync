package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.util.ReportUtil;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.server.TaskType;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.REPORT_RENDERER_TYPE;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_SMS_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.SMS_REPORT_CSV_DESIGN_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.SMS_DATA_EXPORT_REPORT_DEFINITION_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SMS_APPOINTMENT_TYPE_UUID;

/**
 * Posts SMS Appointment Remainder data to the central server
 */

@Component
public class SendSMSAppointmentTask extends AbstractTask {

    protected Log log = LogFactory.getLog(getClass());

    UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
    SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
    TaskType taskType = new TaskType();

    @Override
    public void execute() {
        Date todayDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        if (!isGpDhis2OrganizationUuidSet()) {
            return;
        }


        String SMSServerUrlEndPoint = taskType.getSyncTaskType(SMS_APPOINTMENT_TYPE_UUID).getUrl();
        String baseUrl = ugandaEMRHttpURLConnection.getBaseURL(SMSServerUrlEndPoint);
        String strSubmissionDate = Context.getAdministrationService()
                .getGlobalPropertyObject(GP_SMS_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE).getPropertyValue();


        if (!isBlank(strSubmissionDate)) {
            Date gpSubmissionDate = null;
            try {
                gpSubmissionDate = new SimpleDateFormat("yyyy-MM-dd").parse(strSubmissionDate);
            }
            catch (ParseException e) {
                log.info("Error parsing last successful submission date " + strSubmissionDate + e);
                e.printStackTrace();
                log.error(e);
            }

            long diff = todayDate.getTime() - gpSubmissionDate.getTime();

            TimeUnit time = TimeUnit.DAYS;
            long days_diff = time.convert(diff, TimeUnit.MILLISECONDS);

            if (days_diff  < 7) {
                log.info("Days for sending next batch not yet reached");
                return;
            }

        }
        //Check internet connectivity
        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            log.error("No network for server");
            return;
        }

        //Check destination server availability
        if (!ugandaEMRHttpURLConnection.isServerAvailable(baseUrl)) {
            log.error("Server cant be reached");
            return;
        }
        log.info("Sending SMS Appointment data to central server ");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.add(Calendar.DATE, 7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        Date firstDateOfNextWeek = cal.getTime();

        cal.add(Calendar.DATE, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        Date lastDateOfNextWeek = cal.getTime();

        String bodyText = getSMSAppointmentReminderDataExport(firstDateOfNextWeek,lastDateOfNextWeek);
        if(bodyText==""){
            log.info("Empty report design");
            return;
        }else {
            HttpResponse httpResponse = ugandaEMRHttpURLConnection.post(SMSServerUrlEndPoint, bodyText, taskType.getSyncTaskType(SMS_APPOINTMENT_TYPE_UUID).getUrlUserName(), taskType.getSyncTaskType(SMS_APPOINTMENT_TYPE_UUID).getUrlPassword());
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK || httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                ReportUtil.updateGlobalProperty(GP_SMS_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE,
                        dateTimeFormat.format(todayDate));
                taskType.saveSyncTaskForSyncTaskType(taskType.getSyncTaskType(SMS_APPOINTMENT_TYPE_UUID));
                log.info("SMS Appointment data has been sent to central server");
            } else {
                log.info("Http response status code: " + httpResponse.getStatusLine().getStatusCode() + ". Reason: "
                        + httpResponse.getStatusLine().getReasonPhrase());
            }
        }
    }

    private String getSMSAppointmentReminderDataExport(Date startDate, Date endDate) {
        ReportDefinitionService reportDefinitionService = Context.getService(ReportDefinitionService.class);
        String strOutput = new String();

        try {
            ReportDefinition rd = reportDefinitionService.getDefinitionByUuid(SMS_DATA_EXPORT_REPORT_DEFINITION_UUID);
            if (rd == null) {
                throw new IllegalArgumentException("unable to find SMS Appointment Reminder Data Export report with uuid "
                        + SMS_DATA_EXPORT_REPORT_DEFINITION_UUID);
            }
            String reportRendergingMode = REPORT_RENDERER_TYPE + "!" + SMS_REPORT_CSV_DESIGN_UUID;
            RenderingMode renderingMode = new RenderingMode(reportRendergingMode);
            if (!renderingMode.getRenderer().canRender(rd)) {
                throw new IllegalArgumentException("Unable to render CSV Data Export with " + reportRendergingMode);
            }

            Cohort SMSAppointmentReminder = Context.getCohortService().getCohortByUuid("c418984c-fe55-431a-90be-134da0a5ec67");
            Map<String, Object> parameterValues = new HashMap<String, Object>();

            parameterValues.put("endDate", endDate);
            parameterValues.put("startDate", startDate);
            EvaluationContext context = new EvaluationContext();
            context.setBaseCohort(SMSAppointmentReminder);
            context.setParameterValues(parameterValues);
            ReportData reportData = reportDefinitionService.evaluate(rd, context);
            ReportRequest reportRequest = new ReportRequest();
            reportRequest.setReportDefinition(new Mapped<ReportDefinition>(rd, context.getParameterValues()));
            reportRequest.setRenderingMode(renderingMode);
            File file = new File(OpenmrsUtil.getApplicationDataDirectory() + "SMS_Appointment_Remainder_Report.csv");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            renderingMode.getRenderer().render(reportData, renderingMode.getArgument(), fileOutputStream);

            strOutput = this.readOutputFile(strOutput);
            System.out.println(strOutput);
        }
        catch (Exception e) {
            log.info("Error rendering the contents of the Recency data export report to"
                    + OpenmrsUtil.getApplicationDataDirectory() + "SMS_Appointment_Remainder_Report.csv" + e.toString());
        }

        return strOutput;
    }


    public String readOutputFile(String strOutput) throws Exception {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        FileInputStream fstreamItem = new FileInputStream(OpenmrsUtil.getApplicationDataDirectory() + "SMS_Appointment_Remainder_Report.csv");
        DataInputStream inItem = new DataInputStream(fstreamItem);
        BufferedReader brItem = new BufferedReader(new InputStreamReader(inItem));
        String phraseItem;

        if (!(phraseItem = brItem.readLine()).isEmpty()) {
//            strOutput = strOutput + "\"dhis2_orgunit_uuid\"," + "\"encounter_uuid\"," + phraseItem + System.lineSeparator();
            while ((phraseItem = brItem.readLine()) != null) {
                strOutput = strOutput + "\"" + syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID)
                        + "\",\"\"," + phraseItem + System.lineSeparator();
            }
        }

        fstreamItem.close();

        return strOutput;
    }

    public boolean isGpDhis2OrganizationUuidSet() {
        if (isBlank(syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID))) {
            log.info("DHIS2 Organization UUID is not set");
            return false;
        }
        return true;
    }
}
