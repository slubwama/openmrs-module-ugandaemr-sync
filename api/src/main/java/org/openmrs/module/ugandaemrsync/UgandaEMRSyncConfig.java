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
	
	public static String EndPoint = "";
}
