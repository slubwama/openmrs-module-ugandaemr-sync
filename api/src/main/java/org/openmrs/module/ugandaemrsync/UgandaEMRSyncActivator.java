/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrsync;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class UgandaEMRSyncActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(UgandaEMRSyncActivator.class);
	
	/**
	 * @see #started()
	 */
	public void started() {
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		
		try {
			log.info("Setting Global Properties For Sync Module");
			syncGlobalProperties.setSyncFacilityProperties();
		}
		catch (Exception e) {
			
		}
		log.info("Started UgandaemrSync");
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		log.info("Shutdown UgandaemrSync");
	}
	
}
