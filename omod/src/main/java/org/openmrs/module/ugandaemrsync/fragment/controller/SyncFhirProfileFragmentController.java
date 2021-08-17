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

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/**
 *  * Controller for a fragment that shows all users  
 */
public class SyncFhirProfileFragmentController {

	public SimpleObject getSyncFhirProfile(@RequestParam(value = "profileId", required = false) String profileId) throws IOException {
		SyncFhirProfile syncFhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID(profileId);

		SimpleObject simpleObject = new SimpleObject();

		ObjectMapper objectMapper = new ObjectMapper();
		SimpleObject syncFhirProfileMapper=SimpleObject.create("profileId",syncFhirProfile.getId(),"name",syncFhirProfile.getName(),"generateBundle",syncFhirProfile.getGenerateBundle(),"resourceTypes",syncFhirProfile.getResourceTypes(),"durationToKeepSyncedResources",syncFhirProfile.getDurationToKeepSyncedResources(),"noOfResourcesInBundle",syncFhirProfile.getNumberOfResourcesInBundle(),"isCaseBasedProfile",syncFhirProfile.getCaseBasedProfile(),"caseBasedPrimaryResourceType",syncFhirProfile.getCaseBasedPrimaryResourceType(),"caseBasedPrimaryResourceUUID",syncFhirProfile.getCaseBasedPrimaryResourceTypeId(),"patientIdentifierType",syncFhirProfile.getPatientIdentifierType().getUuid(),"resourceSearchParameter",syncFhirProfile.getResourceSearchParameter(),"url",syncFhirProfile.getUrl()
		,"urlToken",syncFhirProfile.getUrlToken(),"urlUserName",syncFhirProfile.getUrlUserName(),"urlPassword",syncFhirProfile.getUrlPassword());
		simpleObject.put("syncFhirProfile", objectMapper.writeValueAsString(syncFhirProfileMapper));
		return simpleObject;
	}
}
