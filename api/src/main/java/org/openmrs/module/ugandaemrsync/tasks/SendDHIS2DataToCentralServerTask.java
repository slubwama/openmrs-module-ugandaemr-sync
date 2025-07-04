package org.openmrs.module.ugandaemrsync.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;
import org.openmrs.scheduler.tasks.AbstractTask;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_DHIS2_SERVER_URL;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_DHIS2_SERVER_USERNAME;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_DHIS2_SERVER_PASSWORD;

/**
 * Posts DHIS 2 data data to the central server
 */

public class SendDHIS2DataToCentralServerTask extends AbstractTask  {

	protected Log log = LogFactory.getLog(getClass());
	SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
	protected byte[] data ;

	UgandaEMRHttpURLConnection ugandaEMRHttpURLConnection = new UgandaEMRHttpURLConnection();


	public SendDHIS2DataToCentralServerTask() {}

	public SendDHIS2DataToCentralServerTask(byte[] data) {
		this.data = data;
	}


    public void execute() {
        Map map = new HashMap();
        ObjectMapper objectMapper = new ObjectMapper();
        int responseCode = 0;
        String baseUrl = ugandaEMRHttpURLConnection.getBaseURL(syncGlobalProperties.getGlobalProperty(GP_DHIS2_SERVER_URL));
        if (isBlank(syncGlobalProperties.getGlobalProperty(GP_DHIS2_SERVER_URL))) {
            log.error("DHIS 2 server URL is not set");
            log.error("DHIS 2 server URL is not set");
            return;
        }

        //Check internet connectivity
        if (!ugandaEMRHttpURLConnection.isConnectionAvailable()) {
            map.put("responseCode", responseCode);
            map.put("responseMessage", "Failed to connect to the internet. Check connection");

            return;
        }

        //Check destination server availability
        if (!ugandaEMRHttpURLConnection.isServerAvailable(baseUrl)) {
            map.put("responseCode", responseCode);
            map.put("responseMessage", "Server Not Available");

            return;
        }

        log.error("Sending DHIS2 data to central server ");
        String bodyText = new String(this.data);
        try {
            HttpResponse httpResponse = ugandaEMRHttpURLConnection.httpPost(syncGlobalProperties.getGlobalProperty(GP_DHIS2_SERVER_URL), bodyText, syncGlobalProperties.getGlobalProperty(GP_DHIS2_SERVER_USERNAME), syncGlobalProperties.getGlobalProperty(GP_DHIS2_SERVER_PASSWORD));
            responseCode = httpResponse.getStatusLine().getStatusCode();
            String responseMessage = httpResponse.getStatusLine().getReasonPhrase();
            if ((responseCode == 200 || responseCode == 201)) {
                InputStream inputStreamReader = httpResponse.getEntity().getContent();
                map = getMapOfResults(inputStreamReader, responseCode);
            } else {
                map.put("responseCode", responseCode);
                log.info(responseMessage);
            }
            map.put("responseMessage", responseMessage);
        } catch (IOException e) {
            log.error(e);
        }

    }

	public Map getMapOfResults(InputStream inputStreamReader, int responseCode) throws IOException {
		Map map = new HashMap();
		InputStreamReader reader = new InputStreamReader(inputStreamReader);
		StringBuilder buf = new StringBuilder();
		char[] cbuf = new char[2048];
		int num;
		while (true) {
			if (!(-1 != (num = reader.read(cbuf)))) break;
			buf.append(cbuf, 0, num);
		}
		String result = buf.toString();
		ObjectMapper mapper = new ObjectMapper();
		if (isJSONValid(result)) {
			map = mapper.readValue(result, Map.class);
		}
		map.put("responseCode", responseCode);
		return map;
	}
	public boolean isJSONValid(String test) {
		try {
			new JSONObject(test);
		} catch (JSONException ex) {
			try {
				new JSONArray(test);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}

}