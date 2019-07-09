package org.openmrs.module.ugandaemrsync.server;

/**
 * Created by lubwamasamuel on 11/10/16.
 */

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.CONNECTION_SUCCESS;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_USERNAME;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_PASSWORD;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.HEALTH_CENTER_SYNC_ID;


public class UgandaEMRHttpURLConnection {
	
	public UgandaEMRHttpURLConnection() {
	}
	
	private final String USER_AGENT = "Mozilla/5.0";
	
	/**
	 * HTTP GET request
	 * 
	 * @param content
	 * @param protocol
	 * @return
	 * @throws Exception
	 */
	public HttpURLConnection sendGet(String content, String protocol) throws Exception {
		
		URL obj = new URL(protocol + content);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		// optional default is GET
		con.setRequestMethod("GET");
		
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		
		return con;
		
	}
	
	/**
	 * Checking if there is a connection
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public int getCheckConnection(String url) throws Exception {
		return sendGet(url, SyncConstant.SERVER_PROTOCOL_PLACE_HOLDER).getResponseCode();
	}
	
	/**
	 * Getting Response String
	 * 
	 * @param bufferedReader
	 * @return
	 * @throws IOException
	 */
	public StringBuffer getResponseString(BufferedReader bufferedReader) throws IOException {
		String inputLine;
		
		StringBuffer response = new StringBuffer();
		
		while ((inputLine = bufferedReader.readLine()) != null) {
			response.append(inputLine);
		}
		return response;
	}
	
	/**
	 * HTTP POST request
	 * 
	 * @param contentType
	 * @param content
	 * @param facilityId
	 * @param url
	 * @param username
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public Map sendPostByWithBasicAuth(String contentType, String content, String facilityId, String url, String username,
	        String password) throws Exception {

		try {
			URL url1 = new URL(url);
			URLConnection urlConnection = url1.openConnection();
			
			if (username != "" && password != null) {
				String encoded = Base64.getEncoder().encodeToString(
				    (username + ":" + password).getBytes(StandardCharsets.UTF_8)); //Java 8
				urlConnection.setRequestProperty("Authorization", "Basic " + encoded);
			}
			
			if (facilityId != "") {
				urlConnection.setRequestProperty("Ugandaemr-Sync-Facility-Id", facilityId);
			}
			
			// specify that we will send output and accept input
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setConnectTimeout(20000); // long timeout, but not infinite
			urlConnection.setReadTimeout(20000);
			urlConnection.setUseCaches(false);
			urlConnection.setDefaultUseCaches(false);
			
			// tell the web server what we are sending
			urlConnection.setRequestProperty("Content-Type", contentType);
			urlConnection.setRequestProperty("User-Agent", USER_AGENT);
			urlConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			
			OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
			writer.write(content);
			writer.flush();
			writer.close();
			
			Map map = new HashMap();
			int responseCode = ((HttpsURLConnectionImpl) urlConnection).getResponseCode();
			// reading the response
			if (responseCode == CONNECTION_SUCCESS) {
				InputStreamReader reader = new InputStreamReader(urlConnection.getInputStream());
				StringBuilder buf = new StringBuilder();
				char[] cbuf = new char[2048];
				int num;
				while (-1 != (num = reader.read(cbuf))) {
					buf.append(cbuf, 0, num);
				}
				String result = buf.toString();
				
				ObjectMapper mapper = new ObjectMapper();
				map = mapper.readValue(result, Map.class);
			} else {
				map.put("responseCode", responseCode);
			}
			return map;
		}
		catch (Throwable t) {
			t.printStackTrace(System.out);
		}
		return null;
	}
	
	/**
	 * Send Post
	 * 
	 * @param url
	 * @param data
	 * @param facilityIdRequired
	 * @return
	 * @throws Exception
	 */
	public Map sendPostBy(String url, String data, boolean facilityIdRequired) throws Exception {
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		String contentTypeJSON = SyncConstant.JSON_CONTENT_TYPE;
		String serverIP = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_IP);
		String serverProtocol = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_PROTOCOL);
		String facilitySyncId = "";
		if (facilityIdRequired) {
			syncGlobalProperties.getGlobalProperty(HEALTH_CENTER_SYNC_ID);
		}
		String facilityURL = serverProtocol + serverIP + "/" + url;
		
		return sendPostByWithBasicAuth(contentTypeJSON, data, facilitySyncId, facilityURL,
		    syncGlobalProperties.getGlobalProperty(SERVER_USERNAME), syncGlobalProperties.getGlobalProperty(SERVER_PASSWORD));
	}
	
	/**
	 * Request for facility Id
	 * 
	 * @return
	 * @throws Exception
	 */
	public String requestFacilityId() throws Exception {
		LocationService service = Context.getLocationService();
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		
		Location location = service.getLocation(Integer.valueOf(2));
		
		Facility facility = new Facility(location.getName());
		
		ObjectMapper mapper = new ObjectMapper();
		
		String jsonInString = mapper.writeValueAsString(facility);
		
		Map facilityMap = sendPostBy("api/facility", jsonInString, true);
		
		String uuid = String.valueOf(facilityMap.get("uuid"));
		
		if (uuid != null) {
			syncGlobalProperties.setGlobalProperty(HEALTH_CENTER_SYNC_ID, uuid);
			return "Facility ID Generated Successfully";
			
		}
		return "Could not generate Facility ID";
	}
	
}
