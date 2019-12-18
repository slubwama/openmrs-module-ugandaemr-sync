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

import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncDataRecord;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageModel;

import java.util.*;

/**
 *  * Controller for a fragment that shows all users  
 */
public class HomeFragmentController {
	
	SyncDataRecord syncDataRecord = new SyncDataRecord();
	
	UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();
	
	Integer max = 500;
	
	public void controller(UiSessionContext sessionContext, FragmentModel model) {
	}
	
	public void get(@SpringBean PageModel pageModel) throws Exception {
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		String facilitySyncId = syncGlobalProperties.getGlobalProperty(SyncConstant.HEALTH_CENTER_SYNC_ID);
		String initialState = syncGlobalProperties.getGlobalProperty(SyncConstant.INITIAL_SYNC);
		
		if (initialState.equalsIgnoreCase("false")) {
			pageModel.put("initialSync", true);
		} else {
			pageModel.put("initialSync", false);
		}
		
		try {
			UUID uuid = UUID.fromString(facilitySyncId);
			pageModel.put("requestFacility", false);
		}
		catch (IllegalArgumentException exception) {
			pageModel.put("requestFacility", true);
		}
		
	}
	
}
