/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync.api.dao;

import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;
import org.openmrs.api.db.hibernate.DbSession;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("ugandaemrsync.UgandaEMRSyncDao")
public class UgandaEMRSyncDao {
	
	@Autowired
	DbSessionFactory sessionFactory;
	
	/**
	 * @return
	 */
	private DbSession getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * Getting All Sync task types
	 * 
	 * @return
	 */
	public List<SyncTaskType> getAllSyncTaskType() {
		return (List<SyncTaskType>) getSession().createCriteria(SyncTaskType.class).list();
	}
	
	/**
	 * Get Sync Task Type by uuid
	 * 
	 * @param uuid
	 * @return
	 */
	public SyncTaskType getSyncTaskTypeByUUID(String uuid) {
		return (SyncTaskType) getSession().createCriteria(SyncTaskType.class).add(Restrictions.eq("uuid", uuid))
		        .uniqueResult();
	}
	
	/**
	 * @param syncTaskType
	 * @return
	 */
	public SyncTaskType saveSyncTaskType(SyncTaskType syncTaskType) {
		getSession().saveOrUpdate(syncTaskType);
		return syncTaskType;
	}
	
	/**
	 * @param syncTask
	 * @return
	 */
	public SyncTask saveSyncTask(SyncTask syncTask) {
		getSession().saveOrUpdate(syncTask);
		return syncTask;
	}
	
	/**
	 * @param query
	 * @return
	 */
	public List getDatabaseRecord(String query) {
		SQLQuery sqlQuery = getSession().createSQLQuery(query);
		return sqlQuery.list();
	}
	
	/**
	 * @param columns
	 * @param finalQuery
	 * @return
	 */
	public List getFinalResults(List<String> columns, String finalQuery) {
		SQLQuery sqlQuery = getSession().createSQLQuery(finalQuery);
		for (String column : columns) {
			sqlQuery.addScalar(column, StringType.INSTANCE);
		}
		return sqlQuery.list();
	}
	
}
