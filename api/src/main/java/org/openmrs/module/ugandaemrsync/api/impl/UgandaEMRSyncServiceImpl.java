/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api.impl;

import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.api.dao.UgandaEMRSyncDao;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class UgandaEMRSyncServiceImpl extends BaseOpenmrsService implements UgandaEMRSyncService {
	
	UgandaEMRSyncDao dao;
	
	@Autowired
	UserService userService;
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setDao(UgandaEMRSyncDao dao) {
		this.dao = dao;
	}
	
	/**
	 * Injected in moduleApplicationContext.xml
	 */
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	/**
	 * @return
	 * @throws APIException
	 */
	@Override
	public List<SyncTaskType> getAllSyncTaskType() throws APIException {
		return dao.getAllSyncTaskType();
	}
	
	/**
	 * @param uuid
	 * @return
	 * @throws APIException
	 */
	@Override
	public SyncTaskType getSyncTaskTypeByUUID(String uuid) throws APIException {
		return dao.getSyncTaskTypeByUUID(uuid);
	}
	
	/**
	 * @param syncTaskType
	 * @return
	 * @throws APIException
	 */
	@Override
	public SyncTaskType saveSyncTaskType(SyncTaskType syncTaskType) throws APIException {
		if (syncTaskType.getCreator() == null) {
			syncTaskType.setCreator(userService.getUser(1));
		}
		return dao.saveSyncTaskType(syncTaskType);
	}
	
	@Override
	public SyncTask getSyncTask(int syncTask) throws APIException {
		return dao.getSyncTask(syncTask);
	}
	
	/**
	 * @param syncTask
	 * @return
	 * @throws APIException
	 */
	@Override
	public SyncTask saveSyncTask(SyncTask syncTask) throws APIException {
		if (syncTask.getCreator() == null) {
			syncTask.setCreator(userService.getUser(1));
		}
		return dao.saveSyncTask(syncTask);
	}
	
	/**
	 * @param query
	 * @return
	 */
	@Override
	public List getDatabaseRecord(String query) {
		return dao.getDatabaseRecord(query);
	}
	
	/**
	 * @param columns
	 * @param finalQuery
	 * @return
	 */
	@Override
	public List getFinalList(List<String> columns, String finalQuery) {
		return dao.getFinalResults(columns, finalQuery);
	}
}
