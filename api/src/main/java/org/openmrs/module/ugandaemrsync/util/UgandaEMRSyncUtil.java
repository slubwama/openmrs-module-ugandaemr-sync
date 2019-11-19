package org.openmrs.module.ugandaemrsync.util;

import java.util.ArrayList;
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
}
