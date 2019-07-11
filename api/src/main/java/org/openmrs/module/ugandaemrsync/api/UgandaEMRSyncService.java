/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The main service of this module, which is exposed for other modules. See
 * moduleApplicationContext.xml on how it is wired up.
 */
public interface UgandaEMRSyncService extends OpenmrsService {
	
	/**
	 * Getting all Sync Task Types
	 * 
	 * @return
	 * @throws APIException
	 */
	List<SyncTaskType> getAllSyncTaskType() throws APIException;
	
	/**
	 * @param uuid
	 * @return
	 * @throws APIException
	 */
	@Transactional
	SyncTaskType getSyncTaskTypeByUUID(String uuid) throws APIException;
	
	/**
	 * @param syncTaskType
	 * @return
	 * @throws APIException
	 */
	@Transactional
	SyncTaskType saveSyncTaskType(SyncTaskType syncTaskType) throws APIException;
	
	/**
	 * Get SyncTask
	 * 
	 * @param syncTask
	 * @return
	 * @throws APIException
	 */
	@Transactional
	SyncTask getSyncTask(int syncTask) throws APIException;
	
	/**
	 * @param syncTask
	 * @return
	 * @throws APIException
	 */
	@Transactional
	SyncTask saveSyncTask(SyncTask syncTask) throws APIException;
	
	/**
	 * @param query
	 * @return
	 */
	public List getDatabaseRecord(String query);
	
	/**
	 * @param columns
	 * @param query
	 * @return
	 */
	public List getFinalList(List<String> columns, String query);
}
