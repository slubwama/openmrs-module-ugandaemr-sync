package org.openmrs.module.ugandaemrsync.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class TaskType {

	public TaskType() {
	}
	
	protected Log log = LogFactory.getLog(TaskType.class);

	
	public GlobalProperty setGlobalProperty(String property, String propertyValue) {
		GlobalProperty globalProperty = new GlobalProperty();
		
		globalProperty.setProperty(property);
		globalProperty.setPropertyValue(propertyValue);
		
		return Context.getAdministrationService().saveGlobalProperty(globalProperty);
	}
	
	public SyncTaskType getSyncTaskType(String uuid) {
		return Context.getService(UgandaEMRSyncService.class).getSyncTaskTypeByUUID(uuid);
	}

	public void saveSyncTaskForSyncTaskType(SyncTaskType syncTaskType){
		SyncTask newSyncTask = new SyncTask();
		newSyncTask.setDateSent(new Date());
		newSyncTask.setCreator(Context.getUserService().getUser(1));
		newSyncTask.setSentToUrl(syncTaskType.getUrl());
		newSyncTask.setRequireAction(true);
		newSyncTask.setActionCompleted(false);
		newSyncTask.setSyncTask(syncTaskType.getName());
		newSyncTask.setStatusCode(200);
		newSyncTask.setStatus("SUCCESS");
		newSyncTask.setSyncTaskType(syncTaskType);
		Context.getService(UgandaEMRSyncService.class).saveSyncTask(newSyncTask);
	}
	
}
