package org.openmrs.module.ugandaemrsync.util;

import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.util.OpenmrsUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.CONNECTION_SUCCESS_200;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.CONNECTION_SUCCESS_201;

public class UgandaEMRSyncUtil {

	public static List<Object> getSuccessCodeList() {
        List<Object> success = new ArrayList<>();
        success.add(CONNECTION_SUCCESS_200);
        success.add(CONNECTION_SUCCESS_201);
        return success;
    }

    public static Date addDaysToDate(Date date, Integer numberOfDays) {
        if (date == null) {
            date = new Date(); // Default to current date if null
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date); // Use the provided date parameter
        cal.add(Calendar.DATE, numberOfDays);
        return cal.getTime();
    }


}
