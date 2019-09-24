package org.openmrs.module.ugandaemrsync.tasks;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.server.UgandaEMRHttpURLConnection;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.*;

/**
 * Posts recency data to the central server
 */

@Component
public class SendRecencyDataToCentralServerTask extends AbstractTask {
	
	protected Log log = LogFactory.getLog(getClass());
	
	UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
	
	SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
	
	@Autowired
	@Qualifier("reportingReportDefinitionService")
	protected ReportDefinitionService reportingReportDefinitionService;
	
	@Override
	public void execute() {
		Date todayDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (!isGpRecencyServerUrlSet()) {
			return;
		}
		if (!isGpDhis2OrganizationUuidSet()) {
			return;
		}
		if (!isGpRecencyServerPasswordSet()) {
			return;
		}
		
		String recencyServerUrlEndPoint = syncGlobalProperties.getGlobalProperty(GP_RECENCY_SERVER_URL);
		String recencyBaseUrl = ugandaEMRHttpURLConnection.getBaseURL(recencyServerUrlEndPoint);
		
		String strSubmissionDate = Context.getAdministrationService()
		        .getGlobalPropertyObject(GP_RECENCY_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE).getPropertyValue();
		String strSubmitOnceDaily = Context.getAdministrationService()
		        .getGlobalPropertyObject(GP_SUBMIT_RECENCY_DATA_ONCE_DAILY).getPropertyValue();
		
		if (!isBlank(strSubmissionDate)) {
			
			if (strSubmissionDate.equals(dateFormat.format(todayDate)) && strSubmitOnceDaily.equals("true")) {
				log.info("Last successful submission was on" + strSubmissionDate
				        + " and once data submission daily is set as " + strSubmitOnceDaily
				        + "so this task will not run again today. If you need to send data, run the task manually."
				        + System.lineSeparator());
				return;
			}
		}
		//Check internet connectivity
		if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
			return;
		}
		
		//Check destination server availability
		if (!ugandaEMRHttpURLConnection.isServerAvailable(recencyBaseUrl)) {
			return;
		}
		log.info("Sending recency data to central server ");
		String bodyText = getRecencyDataExport();
		HttpResponse httpResponse = ugandaEMRHttpURLConnection.httpPost(recencyServerUrlEndPoint, bodyText);
		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			ReportUtil.updateGlobalProperty(GP_RECENCY_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE, dateFormat.format(todayDate));
			log.info("Recency data has been sent to central server");
		} else {
			log.info("Http response status code: " + httpResponse.getStatusLine().getStatusCode() + ". Reason: "
			        + httpResponse.getStatusLine().getReasonPhrase());
			ugandaEMRHttpURLConnection.setAlertForAllUsers("Http request has returned a response status: "
			        + httpResponse.getStatusLine().getStatusCode() + " " + httpResponse.getStatusLine().getReasonPhrase()
			        + " error");
		}
	}
	
	private String getRecencyDataExport() {
		ReportDefinitionService reportDefinitionService = Context.getService(ReportDefinitionService.class);
		String strOutput = new String();
		
		try {
			ReportDefinition rd = reportDefinitionService.getDefinitionByUuid(RECENCY_DATA_EXPORT_REPORT_DEFINITION_UUID);
			if (rd == null) {
				throw new IllegalArgumentException("unable to find Recency Data Export report with uuid "
				        + RECENCY_DATA_EXPORT_REPORT_DEFINITION_UUID);
			}
			String reportRendergingMode = REPORT_RENDERER_TYPE + "!" + REPORT_CSV_DESIGN_UUID;
			RenderingMode renderingMode = new RenderingMode(reportRendergingMode);
			if (!renderingMode.getRenderer().canRender(rd)) {
				throw new IllegalArgumentException("Unable to render Recency Data Export with " + reportRendergingMode);
			}
			
			EvaluationContext context = new EvaluationContext();
			ReportData reportData = reportDefinitionService.evaluate(rd, context);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setReportDefinition(new Mapped<ReportDefinition>(rd, context.getParameterValues()));
			reportRequest.setRenderingMode(renderingMode);
			File file = new File(OpenmrsUtil.getApplicationDataDirectory() + RECENCY_CSV_FILE_NAME);
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			renderingMode.getRenderer().render(reportData, renderingMode.getArgument(), fileOutputStream);
			
			strOutput = this.readOutputFile(strOutput);
		}
		catch (Exception e) {
			log.info("Error rendering the contents of the Recency data export report to"
			        + OpenmrsUtil.getApplicationDataDirectory() + RECENCY_CSV_FILE_NAME + e.toString());
		}
		
		return strOutput;
	}
	
	/*
	Method: readOutputFile
	Pre condition: empty strOutput initialized
	Description:
		Read the recency exported report file in csv
		Create a string and prefix the dhis2_orgunit_uuid
		and encounter_uuid columns to the final output
	Post condition: strOutput assigned with csv file data prefixed with two additional columns
	* */
	
	public String readOutputFile(String strOutput) throws Exception {
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		FileInputStream fstreamItem = new FileInputStream(OpenmrsUtil.getApplicationDataDirectory() + RECENCY_CSV_FILE_NAME);
		DataInputStream inItem = new DataInputStream(fstreamItem);
		BufferedReader brItem = new BufferedReader(new InputStreamReader(inItem));
		String phraseItem;
		
		if (!(phraseItem = brItem.readLine()).isEmpty()) {
			strOutput = strOutput + "\"dhis2_orgunit_uuid\"," + "\"encounter_uuid\"," + phraseItem + System.lineSeparator();
			while ((phraseItem = brItem.readLine()) != null) {
				strOutput = strOutput + "\"" + syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID)
				        + "\",\"\"," + phraseItem + System.lineSeparator();
			}
		}
		
		fstreamItem.close();
		
		return strOutput;
	}
	
	public boolean isGpRecencyServerUrlSet() {
		if (isBlank(syncGlobalProperties.getGlobalProperty(GP_RECENCY_SERVER_URL))) {
			log.info("Recency server URL is not set");
			ugandaEMRHttpURLConnection
			        .setAlertForAllUsers("Recency server URL is not set please go to admin then Settings then Ugandaemrsync and set it");
			return false;
		}
		return true;
	}
	
	public boolean isGpDhis2OrganizationUuidSet() {
		if (isBlank(syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID))) {
			log.info("DHIS2 Organization UUID is not set");
			ugandaEMRHttpURLConnection
			        .setAlertForAllUsers("DHIS2 Organization UUID is not set please go to admin then Settings then Ugandaemr and set it");
			return false;
		}
		return true;
	}
	
	public boolean isGpRecencyServerPasswordSet() {
		if (isBlank(syncGlobalProperties.getGlobalProperty(GP_RECENCY_SERVER_PASSWORD))) {
			log.info("Recency server URL is not set");
			ugandaEMRHttpURLConnection
			        .setAlertForAllUsers("Recency server password is not set please go to admin then Settings then Ugandaemrsync and set it");
			return false;
		}
		return true;
	}
}
