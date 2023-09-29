/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync;

import org.springframework.stereotype.Component;

/**
 * Contains module's config.
 */
@Component("ugandaemrsync.UgandaemrSyncConfig")
public class UgandaEMRSyncConfig {
	
	public final static String MODULE_PRIVILEGE = "UgandaemrSync Privilege";

	public static final String GP_RECENCY_SERVER_URL = "ugandaemrsync.recency.server.url";

	public static final String GP_ANALYTICS_SERVER_URL = "ugandaemrsync.analytics.server.url";
	public static final String GP_DHIS2_SERVER_URL = "ugandaemrsync.sendtoDHIS2.server.url";
	public static final String GP_DHIS2_SERVER_PASSWORD = "ugandaemrsync.sendtoDHIS2.server.password";
	public static final String GP_DHIS2_SERVER_USERNAME = "ugandaemrsync.sendtoDHIS2.server.username";
	public static final String GP_SEND_NEXT_GEN_REPORTS_SERVER_REPORT_UUIDS = "ugandaemrsync.sendNextGenReports.server.reportuuids";
	public static final String GP_SEND_HMIS_REPORTS_SERVER_REPORT_UUIDS = "ugandaemrsync.sendHMISReports.server.reportuuids";
	public static final String GP_SEND_NEXT_GEN_REPORTS_SERVER_URL = "ugandaemrsync.sendNextGenReports.server.url";
	public static final String GP_SEND_HMIS_REPORTS_SERVER_URL = "ugandaemrsync.sendHMISReports.server.url";


	public static final String CONNECTIVITY_CHECK_URL = "https://www.google.com/";
	
	public static final String CONNECTIVITY_CHECK_SUCCESS = "Successful connection to the internet.";
	
	public static final String SERVER_CONNECTION_SUCCESS = "Successfully established connecton to the server.";
	
	public static final String CONNECTIVITY_CHECK_FAILED = "Internet connection is not available at this time, will try again later";
	
	public static final String SERVER_CONNECTION_FAILED = "Server is not available at this time, will try again later";
	
	public static final String GP_RECENCY_SERVER_PASSWORD = "ugandaemrsync.recency.server.password";

	public static final String GP_ANALYTICS_SERVER_PASSWORD = "ugandaemrsync.analytics.server.password";
	public static final String GP_ANALYTICS_SERVER_USERNAME = "ugandaemrsync.analytics.server.username";

	public static final String HEADER_EMR_DATE = "x-emr-date";

	public static final String GP_DHIS2_ORGANIZATION_UUID = "ugandaemr.dhis2.organizationuuid";
	public static final String SYNC_METRIC_DATA = "ugandaemr.syncmetricsdata";

	public static final String GP_FACILITY_NAME = "ugandaemr.healthCenterName";

	public static final String RECENCY_CSV_FILE_NAME = "HTS_Recency_Client_Card_Data_Export_2019.csv";

	public static final String ANALYTICS_JSON_FILE_NAME = "Analytics_Metric_Report";

	public static final String RECENCY_DATA_EXPORT_REPORT_DEFINITION_UUID = "662d4c00-d6bb-4494-8180-48776f415802";
	public static final String GP_RECENCY_REPORT_DURATION = "ugandaemr.hts.recency.surveillance_report_coverage_months";
	public static final String SMS_DATA_EXPORT_REPORT_DEFINITION_UUID = "9a4bfceb-6205-4811-9a09-f95589249f65";

	public static final String ANALYTICS_DATA_EXPORT_REPORT_DEFINITION_UUID = "dcd1f91a-04c8-4ae1-ac44-6abfdc91c98a";

	public static final String REPORT_CSV_DESIGN_UUID = "152a4845-37e1-40c0-8fa8-5ef343e65ba5";
	public static final String SMS_REPORT_CSV_DESIGN_UUID = "e8cd7d9f-e30e-463f-8073-ea7ccf3d3574";

	public static final String ANALYTIC_REPORT_JSON_DESIGN_UUID = "13cf6468-07c2-40f5-a388-6fdc8fa8341e";

	public static final String REPORT_RENDERER_TYPE = "org.openmrs.module.reporting.report.renderer.CsvReportRenderer";

	public static final String JSON_REPORT_RENDERER_TYPE = "org.openmrs.module.reporting.report.renderer.TextTemplateRenderer";

	public static final String GP_RECENCY_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE = "ugandaemrsync.recency.last.successful.submission.date";
	public static final String GP_SMS_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE = "ugandaemrsync.smsAppointmentReminder.last.successful.submission.date";

	public static final String GP_ANALYTICS_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE = "ugandaemrsync.analytics.last.successful.submission.date";

	public static final String DHIS_ORGANIZATION_UUID = "dhis2_organization_uuid";
	
	public static final String HTTP_TEXT_BODY_DATA_TYPE_KEY = "data";
	
	public static final String GP_SUBMIT_RECENCY_DATA_ONCE_DAILY = "ugandaemrsync.recency.submit.data.once.daily";
	public static final String GP_ART_ACCESS_LAST_SYNC_DATE = "ugandaemrsync.ARTAccess.last.successful.submission.date";


	public static final String GP_NHFR_UNIQUE_IDENTIFIER = "ugandaemrsync.national.health.facility.registry.identifier";
  
	public static final String PATIENT_ID_TYPE_UIC_UUID = "877169c4-92c6-4cc9-bf45-1ab95faea242";

	public static final String PATIENT_ID_TYPE_UIC_NAME = "Patient Unique  ID Code (UIC)";

	public static final String PATIENT_ID_TYPE_PRISONID_UUID = "e7dad84a-9ae9-4be2-9ec2-62fc8832cffa";

	public static final String PATIENT_ID_TYPE_POIN_NAME = "POIN";

	public static final String PATIENT_ID_TYPE_NIN_UUID = "f0c16a6d-dc5f-4118-a803-616d0075d282";

	public static final String PATIENT_ID_TYPE_NIN_NAME = "National ID No.";

	public static final String PATIENT_ID_TYPE_PATIENT_POIN_UUID = "5ddcb840-e1d3-11ed-b5ea-0242ac120002";
	public static final String FSHR_SYNC_FHIR_PROFILE_UUID = "0b7eb397-4488-4a88-9967-a054b3c26d6f";
}
