package org.openmrs.module.ugandaemrsync.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_PROTOCOL;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_PROTOCOL_PLACE_HOLDER;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.INITIAL_SYNC;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.INITIAL_SYNC_PLACE_HOLDER;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_IP;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_IP_PLACE_HOLDER;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.HEALTH_CENTER_SYNC_ID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.HEALTH_CENTER_SYNC_ID_PLACE_HOLDER;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.LAST_SYNC_DATE;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.LAST_SYNC_DATE_PLACE_HOLDER;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.MAX_NUMBER_OF_ROWS;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.MAX_NUMBER_OF_ROWS_PLACE_HOLDER;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.GP_DEFAULT_LOCATION_UUID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.DEFAULT_LOCATION_UUID_PLACE_HOLDER;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.GP_VIRAL_LOAD_SYNC_DAYS_BOUNDARY;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.VIRAL_LOAD_SYNC_DAYS_BOUNDARY_PLACE_HOLDER;

/**
 * Created by lubwamasamuel on 10/11/2016.
 */
public class SyncGlobalProperties {
	
	public SyncGlobalProperties() {
	}
	
	protected Log log = LogFactory.getLog(SyncGlobalProperties.class);
	
	public void setSyncFacilityProperties() {
		/**
		 * Setting Server IP Address
		 */
		
		if (getGlobalProperty(SERVER_PROTOCOL) == null) {
			setGlobalProperty(SERVER_PROTOCOL, SERVER_PROTOCOL_PLACE_HOLDER);
			log.info("Default Server IP is Set");
		}
		
		if (getGlobalProperty(INITIAL_SYNC) == null) {
			setGlobalProperty(INITIAL_SYNC, INITIAL_SYNC_PLACE_HOLDER);
			log.info("Default Initial Sync State is Set");
		}
		
		if (getGlobalProperty(SERVER_IP) == null) {
			setGlobalProperty(SERVER_IP, SERVER_IP_PLACE_HOLDER);
			log.info("Default Server IP is Set");
		}
		
		if (getGlobalProperty(HEALTH_CENTER_SYNC_ID) == null) {
			setGlobalProperty(HEALTH_CENTER_SYNC_ID, HEALTH_CENTER_SYNC_ID_PLACE_HOLDER);
			log.info("Place Holder for HC Sync ID is set");
		}
		
		if (getGlobalProperty(LAST_SYNC_DATE) == null) {
			setGlobalProperty(LAST_SYNC_DATE, LAST_SYNC_DATE_PLACE_HOLDER);
			log.info("Place Holder for last sync date is set");
		}
		
		if (getGlobalProperty(MAX_NUMBER_OF_ROWS) == null) {
			setGlobalProperty(MAX_NUMBER_OF_ROWS, MAX_NUMBER_OF_ROWS_PLACE_HOLDER);
			log.info("Place Holder for max number of row is set");
		}

		if (getGlobalProperty(GP_DEFAULT_LOCATION_UUID) == null) {
			setGlobalProperty(GP_DEFAULT_LOCATION_UUID, DEFAULT_LOCATION_UUID_PLACE_HOLDER);
			log.info("Place Holder for default location UUID is set");
		}

		if (getGlobalProperty(GP_VIRAL_LOAD_SYNC_DAYS_BOUNDARY) == null) {
			setGlobalProperty(GP_VIRAL_LOAD_SYNC_DAYS_BOUNDARY, VIRAL_LOAD_SYNC_DAYS_BOUNDARY_PLACE_HOLDER);
			log.info("Place Holder for viral load sync days boundary is set to " + VIRAL_LOAD_SYNC_DAYS_BOUNDARY_PLACE_HOLDER + " days");
		}

	}
	
	public GlobalProperty setGlobalProperty(String property, String propertyValue) {
		GlobalProperty globalProperty = new GlobalProperty();
		
		globalProperty.setProperty(property);
		globalProperty.setPropertyValue(propertyValue);
		
		return Context.getAdministrationService().saveGlobalProperty(globalProperty);
	}
	
	public String getGlobalProperty(String property) {
		return Context.getAdministrationService().getGlobalProperty(property);
	}
	
}
