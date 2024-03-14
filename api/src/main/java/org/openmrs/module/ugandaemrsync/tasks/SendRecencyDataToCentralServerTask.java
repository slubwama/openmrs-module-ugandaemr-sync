package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.io.FileUtils;
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
import org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_RECENCY_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_RECENCY_SERVER_URL;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_SUBMIT_RECENCY_DATA_ONCE_DAILY;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.RECENCY_CSV_FILE_NAME;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.RECENCY_DATA_EXPORT_REPORT_DEFINITION_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.REPORT_RENDERER_TYPE;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_RECENCY_REPORT_DURATION;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.REPORT_CSV_DESIGN_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_RECENCY_SERVER_PASSWORD;

/**
 * Posts recency data to the central server
 */

@Component
public class SendRecencyDataToCentralServerTask extends AbstractTask {
	
	protected Log log = LogFactory.getLog(getClass());
	
	UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
	
	SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
	
	@Override
	public void execute() {
		Date todayDate = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
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
			Date gpSubmissionDate = null;
			try {
				gpSubmissionDate = new SimpleDateFormat("yyyy-MM-dd").parse(strSubmissionDate);
			}
			catch (ParseException e) {
				log.info("Error parsing last successful submission date " + strSubmissionDate + e);
				log.error(e);
				return;
			}
			if (dateFormat.format(gpSubmissionDate).equals(dateFormat.format(todayDate))
			        && strSubmitOnceDaily.equals("true")) {
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
		HttpResponse httpResponse = ugandaEMRHttpURLConnection.post(recencyServerUrlEndPoint, bodyText,syncGlobalProperties.getGlobalProperty(UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID),syncGlobalProperties.getGlobalProperty(UgandaEMRSyncConfig.GP_RECENCY_SERVER_PASSWORD));
		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			ReportUtil.updateGlobalProperty(GP_RECENCY_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE,
			    dateTimeFormat.format(todayDate));
			log.info("Recency data has been sent to central server");
		} else {
			log.info("Http response status code: " + httpResponse.getStatusLine().getStatusCode() + ". Reason: "
			        + httpResponse.getStatusLine().getReasonPhrase());
		}
	}
	
	private String getRecencyDataExport() {
		ReportDefinitionService reportDefinitionService = Context.getService(ReportDefinitionService.class);
		String strOutput = new String();

		LocalDate todayDate = LocalDate.now();

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

			// compute the end date for the report
			long reportDuration = Long.parseLong(syncGlobalProperties.getGlobalProperty(GP_RECENCY_REPORT_DURATION));

			LocalDate startDate = todayDate.minusMonths(reportDuration);
			log.info("Generating Recency data export with start date " + startDate + " and end date "
					+ todayDate + "with a report duration of " + reportDuration + " months");

			Map<String, Object> parameterValues = new HashMap<String, Object>();
			parameterValues.put("startDate", Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			parameterValues.put("endDate", Date.from(todayDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
			EvaluationContext context = new EvaluationContext();
			context.setParameterValues(parameterValues);
			ReportData reportData = reportDefinitionService.evaluate(rd, context);
			ReportRequest reportRequest = new ReportRequest();
			reportRequest.setReportDefinition(new Mapped<ReportDefinition>(rd, context.getParameterValues()));
			reportRequest.setRenderingMode(renderingMode);
			File file = new File(OpenmrsUtil.getApplicationDataDirectory() + RECENCY_CSV_FILE_NAME);
			FileOutputStream fileOutputStream = new FileOutputStream(file);

			// render the report and write the contents to a file
			renderingMode.getRenderer().render(reportData, renderingMode.getArgument(), fileOutputStream);

			return FileUtils.readFileToString(file, StandardCharsets.UTF_8.toString());
		}
		catch(NumberFormatException nfe) {
			log.error("The global property ugandaemr.hts.recency.surveillance_report_coverage_months parameter is not a valid integer with the value "
					+ syncGlobalProperties.getGlobalProperty(GP_RECENCY_REPORT_DURATION));
		}
		catch (Exception e) {
			log.error("Error rendering the contents of the Recency data export report to"
			        + OpenmrsUtil.getApplicationDataDirectory() + RECENCY_CSV_FILE_NAME + e.toString());
		}

		return strOutput;
	}
	
	public boolean isGpRecencyServerUrlSet() {
		if (isBlank(syncGlobalProperties.getGlobalProperty(GP_RECENCY_SERVER_URL))) {
			log.info("Recency server URL is not set");
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
	
	public boolean isGpRecencyServerPasswordSet() {
		if (isBlank(syncGlobalProperties.getGlobalProperty(GP_RECENCY_SERVER_PASSWORD))) {
			log.info("Recency server URL is not set");
			return false;
		}
		return true;
	}
}
