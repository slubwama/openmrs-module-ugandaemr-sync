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

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncDataRecord;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageModel;

import java.util.List;
import java.util.UUID;

/**
 *  * Controller for a fragment that shows all users  
 */
public class SyncRecordsFragmentController {
	
	public void controller(UiSessionContext sessionContext, FragmentModel model) {
	}
	
	public void get(@SpringBean PageModel pageModel) {
		
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		
		SyncDataRecord syncDataRecord = new SyncDataRecord();
		
		List numberSynced = syncDataRecord.syncData();
		
		pageModel.put("syncRecords", numberSynced.size());
	}
	
}
