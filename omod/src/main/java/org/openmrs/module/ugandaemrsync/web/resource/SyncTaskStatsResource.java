package org.openmrs.module.ugandaemrsync.web.resource;

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + SyncTaskStatsResource.DATASET)

public class SyncTaskStatsResource {
    public static final String DATASET = "/synctaskstats";

    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getSyncTaskLogsByType(@RequestParam String startDate, @RequestParam String endDate,
                                        @RequestParam(required = true, value = "type") String type) {
        List<SyncTask> syncTasks = new ArrayList<>();
        int failures = 0;
        int successes = 0;
        try {
            if (!validateDateIsValidFormat(endDate)) {
                SimpleObject message = new SimpleObject();
                message.put("error", "given date " + endDate + "is not valid");
                return new ResponseEntity<SimpleObject>(message, HttpStatus.BAD_REQUEST);

            }

            UgandaEMRSyncService ugandaEMRSyncService = Context.getService(UgandaEMRSyncService.class);

            SyncTaskType syncTaskType = ugandaEMRSyncService.getSyncTaskTypeByUUID(type);
            if (syncTaskType != null) {
                Date synceDateFrom = DateUtil.parseYmd(startDate);
                Date synceDateTo = DateUtil.parseYmd(endDate);

                syncTasks = ugandaEMRSyncService.getSyncTasksByType(syncTaskType, synceDateFrom, synceDateTo);

                for (SyncTask syncTask : syncTasks) {
                    int statusCode = syncTask.getStatusCode();
                    if (statusCode == 200 || statusCode == 201) {
                        successes++;
                    } else {
                        failures++;
                    }
                }
            }
            String result ="{\"total\":" + syncTasks.size() + "," +
                    "\"successes\":" + successes+ "," +
                    "\"failures\":" + failures+ "}";

            return new ResponseEntity<Object>(result, HttpStatus.OK);

        } catch (Exception ex) {
            return new ResponseEntity<String>("{Error: " + ex.getMessage() + "}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
