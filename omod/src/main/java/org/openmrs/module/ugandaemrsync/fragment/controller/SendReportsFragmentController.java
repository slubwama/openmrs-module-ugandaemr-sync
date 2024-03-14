/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.fragment.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.TextTemplateRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.tasks.SendReportsTask;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_SEND_NEXT_GEN_REPORTS_SERVER_REPORT_UUIDS;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_SEND_NEXT_GEN_REPORTS_SERVER_URL;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_SEND_HMIS_REPORTS_SERVER_URL;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_SEND_HMIS_REPORTS_SERVER_REPORT_UUIDS;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.JSON_REPORT_RENDERER_TYPE;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SEND_HMIS_REPORTS_SYNC_TASK_TYPE_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SEND_MER_REPORTS_SYNC_TASK_TYPE_UUID;


/**
 *  * Controller for a fragment that sends a report 
 */
public class SendReportsFragmentController {


	UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

	SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
	String reportUuids=  syncGlobalProperties.getGlobalProperty(GP_SEND_NEXT_GEN_REPORTS_SERVER_REPORT_UUIDS)+ ","+ syncGlobalProperties.getGlobalProperty(GP_SEND_HMIS_REPORTS_SERVER_REPORT_UUIDS);
	List<ReportDefinition> rds = getReportDefinitions(reportUuids);

	String merUrlEndPoint = syncGlobalProperties.getGlobalProperty(GP_SEND_NEXT_GEN_REPORTS_SERVER_URL);
	String hmisUrlEndPoint = syncGlobalProperties.getGlobalProperty(GP_SEND_HMIS_REPORTS_SERVER_URL);

	String hmisReportUuids=  syncGlobalProperties.getGlobalProperty(GP_SEND_HMIS_REPORTS_SERVER_REPORT_UUIDS);
	String merReportUuids=  syncGlobalProperties.getGlobalProperty(GP_SEND_NEXT_GEN_REPORTS_SERVER_REPORT_UUIDS);
	List<String> hmisReports = Arrays.asList(hmisReportUuids.split(","));
	List<String> merReports = Arrays.asList(merReportUuids.split(","));

	public void controller(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride",
			required = false) String breadcrumbOverride) {

		pageModel.put("breadcrumbOverride", breadcrumbOverride);
		pageModel.put("previewBody",null);
		pageModel.put("reportuuid",null);
		pageModel.put("reportDefinitions", rds);
		pageModel.put("errorMessage", "");
		pageModel.put("report_title","");
		pageModel.put("hmis_uuids","");
		pageModel.put("mer_uuids","");

	}

	public void post(@SpringBean PageModel pageModel,
					 @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
					 @RequestParam("startDate") String periodStartDate,
					 @RequestParam("endDate") String periodEndDate,
					 @RequestParam("reportDefinition") String uuid) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat displayDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

		try {
			Date startDate = dateFormat.parse(periodStartDate);
			Date endDate = dateFormat.parse(periodEndDate);
			String bodyText="";
			bodyText = generateReport(uuid,startDate,endDate);
			if(bodyText!=""){
				String displayTitle= getReportDefinitionService().getDefinitionByUuid(uuid).getName()+" For Period \n"+
						displayDateFormat.format(startDate) +" To " +displayDateFormat.format(endDate);
				pageModel.put("previewBody",bodyText);
				pageModel.put("reportuuid",uuid);
				pageModel.put("errorMessage", "");
				pageModel.put("report_title",displayTitle);

			}

		}catch (Exception e){
			pageModel.put("errorMessage", e.getMessage());
			pageModel.put("previewBody",null);
			pageModel.put("reportuuid",null);
			pageModel.put("report_title","");
		}
		pageModel.put("breadcrumbOverride", breadcrumbOverride);
		pageModel.put("reportDefinitions", rds);
		pageModel.put("hmis_uuids",hmisReportUuids);
		pageModel.put("mer_uuids",merReportUuids);
	}


	public SimpleObject sendData(HttpServletRequest request,@RequestParam("body") String body,@RequestParam("uuid")String uuid){
		SendReportsTask sendReportsTask;
		String response="";
		String status="";
		String jsonData= body;

		if(jsonData!=null){
			SyncTaskType MERsyncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(SEND_MER_REPORTS_SYNC_TASK_TYPE_UUID);
			SyncTaskType HMISsyncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(SEND_HMIS_REPORTS_SYNC_TASK_TYPE_UUID);

			SyncTaskType syncTaskType =new SyncTaskType();
			if(hmisReports.contains(uuid)){
				syncTaskType = HMISsyncTaskType;
			}else if(merReports.contains(uuid)){
				syncTaskType= MERsyncTaskType;
			}

			sendReportsTask= new SendReportsTask(jsonData,syncTaskType);
			sendReportsTask.execute();
			if(sendReportsTask.isSent()){
				response= "Report successfully sent";
				status="success";
			}else{
				response= sendReportsTask.getResponseMessage();
				status="failure";
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
		}else{
			response = "No Available Data to send";
			status="failure";
		}
		return SimpleObject.create("status", status, "message", response);
	}

	private ReportDefinitionService getReportDefinitionService() {
		return Context.getService(ReportDefinitionService.class);
	}

	private List<ReportDefinition> getReportDefinitions(String uuids){
		List<String> ids= new ArrayList<>();
		List<ReportDefinition> reportDefinitions = new ArrayList<>();
		if(!uuids.isEmpty()&& uuids!=""){
			ids = Arrays.asList(uuids.split(","));
		}
		if(ids !=null){
			for (String id:ids) {
				reportDefinitions.add(getReportDefinitionService().getDefinitionByUuid(id));
			}
		}
		return reportDefinitions;
	}

	private String generateReport(String uuid, Date startDate, Date endDate){
		String strOutput="";
		try{
			ReportDefinition rd= getReportDefinitionService().getDefinitionByUuid(uuid);
			if (rd == null) {
				throw new IllegalArgumentException("unable to find  report with uuid "
						+ uuid);
			}else {
				List<ReportDesign> reportDesigns = Context.getService(ReportService.class).getReportDesigns(rd, TextTemplateRenderer.class, false);
				ReportDesign reportDesign = reportDesigns.stream().filter(p -> "JSON".equals(p.getName())).findAny().orElse(null);
				String reportRendergingMode = JSON_REPORT_RENDERER_TYPE + "!" + reportDesign.getUuid();
				RenderingMode renderingMode = new RenderingMode(reportRendergingMode);
				if (!renderingMode.getRenderer().canRender(rd)) {
					throw new IllegalArgumentException("Unable to render Report with " + reportRendergingMode);
				}
				Map<String, Object> parameterValues = new HashMap<String, Object>();

				parameterValues.put("endDate", endDate);
				parameterValues.put("startDate", startDate);
				EvaluationContext context = new EvaluationContext();
				context.setParameterValues(parameterValues);
				ReportData reportData = getReportDefinitionService().evaluate(rd, context);
				ReportRequest reportRequest = new ReportRequest();
				reportRequest.setReportDefinition(new Mapped<ReportDefinition>(rd, context.getParameterValues()));
				reportRequest.setRenderingMode(renderingMode);
				File file = new File(OpenmrsUtil.getApplicationDataDirectory() + "sendReports");
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				renderingMode.getRenderer().render(reportData, renderingMode.getArgument(), fileOutputStream);

				strOutput = readOutputFile(strOutput);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return strOutput;
	}

	public String readOutputFile(String strOutput) throws Exception {
		FileInputStream fstreamItem = new FileInputStream(OpenmrsUtil.getApplicationDataDirectory() + "sendReports");
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
	
}
