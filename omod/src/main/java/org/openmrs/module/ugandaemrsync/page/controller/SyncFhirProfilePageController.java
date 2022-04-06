package org.openmrs.module.ugandaemrsync.page.controller;

import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.FHIR_FILTER_OBJECT_STRING;

public class SyncFhirProfilePageController {

    protected final org.apache.commons.logging.Log log = LogFactory.getLog(SyncTaskPageController.class);

    public SyncFhirProfilePageController() {
    }

    public void controller(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride, UiSessionContext sessionContext, PageModel model, UiUtils ui) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        pageModel.put("syncFhirProfiles", ugandaEMRSyncService.getAllSyncFhirProfile());
        pageModel.put("patientIdentifierType", Context.getPatientService().getAllPatientIdentifierTypes());
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
    }

    public void post(@SpringBean PageModel pageModel, @RequestParam(value = "returnUrl", required = false) String returnUrl,
                     @RequestParam(value = "profileId", required = false) String profileId,
                     @RequestParam(value = "syncFhirProfileName", required = false) String syncFhirProfileName,
                     @RequestParam(value = "profileEnabled", required = false, defaultValue = "false") String profileEnabled,
                     @RequestParam(value = "resourceType", required = false) String resourceType,
                     @RequestParam(value = "dataToSyncStartDate", required = false) Date dataToSyncStartDate,
                     @RequestParam(value = "durationToKeepSyncedResources", required = false) Integer durationToKeepSyncedResources,
                     @RequestParam(value = "generateBundle", required = false, defaultValue = "false") String generateBundle,
                     @RequestParam(value = "syncDataEverSince", required = false, defaultValue = "false") String syncDataEverSince,
                     @RequestParam(value = "isCaseBasedProfile", required = false, defaultValue = "false") String isCaseBasedProfile,
                     @RequestParam(value = "caseBasedPrimaryResourceType", required = false) String caseBasedPrimaryResourceType,
                     @RequestParam(value = "caseBasedPrimaryResourceUUID", required = false) String caseBasedPrimaryResourceUUID,
                     @RequestParam(value = "patientIdentifierType", required = false) String patientIdentifierType,
                     @RequestParam(value = "noOfResourcesInBundle", required = false) Integer noOfResourcesInBundle,
                     @RequestParam(value = "encounterTypeUUIDS", required = false) String encounterTypeUUIDS,
                     @RequestParam(value = "observationCodeUUIDS", required = false) ArrayList observationCodeUUIDs,
                     @RequestParam(value = "episodeOfCareUUIDS", required = false) String episodeOfCareUUIDS,
                     @RequestParam(value = "url", required = false) String url,
                     @RequestParam(value = "username", required = false) String username,
                     @RequestParam(value = "password", required = false) String password,
                     @RequestParam(value = "token", required = false) String token,
                     UiSessionContext uiSessionContext, UiUtils uiUtils, HttpServletRequest request) {
        UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

        JSONArray encounterArray = new JSONArray();
        if (encounterTypeUUIDS.split(",").length > 0) {
            encounterArray = new JSONArray(encounterTypeUUIDS.split(","));
        }

        JSONArray episodeOfCareArray = new JSONArray();
        if (episodeOfCareUUIDS.split(",").length > 0) {
            episodeOfCareArray = new JSONArray(episodeOfCareUUIDS.split(","));
        }

        String resourceSearchParams = FHIR_FILTER_OBJECT_STRING.replace("encounterTypeUUID", encounterArray.toString()).replace("conceptQuestionUUID", observationCodeUUIDs.toString()).replace("episodeOfCareTypeUUID", episodeOfCareArray.toString());
        SyncFhirProfile syncFhirProfile;

        if (profileId.equals("")) {
            syncFhirProfile = new SyncFhirProfile();
            syncFhirProfile.setCreator(Context.getAuthenticatedUser());
            syncFhirProfile.setDateCreated(new Date());
        } else {
            syncFhirProfile = Context.getService(UgandaEMRSyncService.class).getSyncFhirProfileByUUID(profileId);
            syncFhirProfile.setDateChanged(new Date());
            syncFhirProfile.setChangedBy(Context.getAuthenticatedUser());
        }

        syncFhirProfile.setName(syncFhirProfileName);
        syncFhirProfile.setProfileEnabled(Boolean.parseBoolean(profileEnabled));
        syncFhirProfile.setGenerateBundle(Boolean.parseBoolean(generateBundle));
        syncFhirProfile.setSyncDataEverSince(Boolean.parseBoolean(syncDataEverSince));
        syncFhirProfile.setDataToSyncStartDate(dataToSyncStartDate);
        syncFhirProfile.setNumberOfResourcesInBundle(noOfResourcesInBundle);
        syncFhirProfile.setResourceTypes(resourceType);
        syncFhirProfile.setDurationToKeepSyncedResources(durationToKeepSyncedResources);
        syncFhirProfile.setCaseBasedProfile(Boolean.parseBoolean(isCaseBasedProfile));
        syncFhirProfile.setCaseBasedPrimaryResourceType(caseBasedPrimaryResourceType);
        syncFhirProfile.setCaseBasedPrimaryResourceTypeId(caseBasedPrimaryResourceUUID);
        syncFhirProfile.setPatientIdentifierType(Context.getPatientService().getPatientIdentifierTypeByUuid(patientIdentifierType));
        syncFhirProfile.setResourceSearchParameter(resourceSearchParams);
        syncFhirProfile.setUrl(url);
        syncFhirProfile.setUrlUserName(username);
        syncFhirProfile.setUrlPassword(password);
        syncFhirProfile.setUrlToken(token);
        ugandaEMRSyncService.saveSyncFhirProfile(syncFhirProfile);

        pageModel.put("syncFhirProfiles", ugandaEMRSyncService.getAllSyncFhirProfile());
        pageModel.put("patientIdentifierType", Context.getPatientService().getAllPatientIdentifierTypes());
    }
}
