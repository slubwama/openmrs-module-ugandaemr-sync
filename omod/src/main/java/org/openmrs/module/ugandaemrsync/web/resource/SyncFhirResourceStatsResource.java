package org.openmrs.module.ugandaemrsync.web.resource;

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncFhirProfile;
import org.openmrs.module.ugandaemrsync.model.SyncFhirResource;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + SyncFhirResourceStatsResource.DATASET)

public class SyncFhirResourceStatsResource {
    public static final String DATASET = "/syncfhirresourcestats";

    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getCasesByProfile(@RequestParam String startDate, @RequestParam String endDate,
                                    @RequestParam(required = true, value = "profile") String profile) {
        List<SyncFhirResource> syncFhirResources = new ArrayList<>();
        try {
            if (!validateDateIsValidFormat(endDate)) {
                SimpleObject message = new SimpleObject();
                message.put("error", "given date " + endDate + "is not valid");
                return new ResponseEntity<SimpleObject>(message, HttpStatus.BAD_REQUEST);

            }

            UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);
            int successes = 0;
            int failures = 0;
            SyncFhirProfile syncFhirProfile = ugandaEMRSyncService.getSyncFhirProfileByUUID(profile);
            if (syncFhirProfile != null) {
                Date synceDateFrom = DateUtil.parseYmd(startDate);
                Date synceDateTo = DateUtil.parseYmd(endDate);

                synceDateTo =DateUtil.getEndOfDay(synceDateTo);

                syncFhirResources = ugandaEMRSyncService.getSyncedFHirResources(syncFhirProfile, synceDateFrom, synceDateTo);

                for (SyncFhirResource resource : syncFhirResources) {
                    Integer statusCode = 0;
                    statusCode =resource.getStatusCode();
                    if (statusCode == 200 || statusCode == 201) {

                        successes++;
                    } else {

                        failures++;
                    }
                }
            }
            String result ="{\"total\":" + syncFhirResources.size() + "," +
                    "\"successes\":" + successes+ "," +
                    "\"failures\":" + failures+ "}";

            return new ResponseEntity<Object>(result, HttpStatus.OK);

        } catch (Exception ex) {
            throw new RuntimeException(ex);        }
    }

    public Boolean validateDateIsValidFormat(String date) {
        try {
            DateUtil.parseYmd(date);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
