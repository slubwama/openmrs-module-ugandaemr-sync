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
	
	public static final String MIRTH_URL = "http://mirth-tcp.globalhealthapp.net:6001";
	
	public static final String GOOGLE_COM = "http://www.google.com";
	
	public static final String SERVER_URL = "http://mirth-tcp.globalhealthapp.net";
	
	public static final String GOOGLE_SUCCESS = "Successful connection to the internet.";
	
	public static final String MIRHT_SUCCESS = "Successfully established connecton to Mirth server.";
	
	public static final String GOOGLE_FAILED = "Cannot establish internet connectivity.";
	
	public static final String MIRTH_FAILED = "Cannot establish connection to mirth server.";
	
	public static final String MIRTH_USERNAME = "admin";
	
	public static final String MIRTH_PASSWORD = "admin";
	
	public static final String HEADER_EMR_DATE = "x-emr-date";
	
	public static final String FACILITY_UUID = "ugandaemr.dhis2.organizationuuid";
	
	public static final String GP_RECENCY_SERVER_URL = "ugandaemrsync.recency.server.url";
	
	public static final String CONNECTIVITY_CHECK_URL = "http://www.google.com";
	
	public static final String CONNECTIVITY_CHECK_SUCCESS = "Successful connection to the internet.";
	
	public static final String SERVER_CONNECTION_SUCCESS = "Successfully established connecton to the server.";
	
	public static final String CONNECTIVITY_CHECK_FAILED = "Internet connection is not available at this time, will try again later";
	
	public static final String SERVER_CONNECTION_FAILED = "Server is not available at this time, will try again later";
	
	public static final String GP_RECENCY_SERVER_PASSWORD = "ugandaemrsync.recency.server.password";
	
	public static final String GP_DHIS2_ORGANIZATION_UUID = "ugandaemr.dhis2.organizationuuid";
	
	public static final String RECENCY_CSV_FILE_NAME = "HTS_Recency_Client_Card_Data_Export_2019.csv";
	
	public static final String RECENCY_DATA_EXPORT_REPORT_DEFINITION_UUID = "662d4c00-d6bb-4494-8180-48776f415802";
	
	public static final String REPORT_CSV_DESIGN_UUID = "152a4845-37e1-40c0-8fa8-5ef343e65ba5";
	
	public static final String REPORT_RENDERER_TYPE = "org.openmrs.module.reporting.report.renderer.CsvReportRenderer";
	
	public static final String GP_RECENCY_TASK_LAST_SUCCESSFUL_SUBMISSION_DATE = "ugandaemrsync.recency.last.successful.submission.date";
	
	public static final String DHIS_ORGANIZATION_UUID = "dhis2_organization_uuid";
	
	public static final String HTTP_TEXT_BODY_DATA_TYPE_KEY = "data";
	
	public static final String GP_SUBMIT_RECENCY_DATA_ONCE_DAILY = "ugandaemrsync.recency.submit.data.once.daily";
	
	public static String EndPoint = "";
}
