package org.openmrs.module.ugandaemrsync.server;

/**
 * Created by lubwamasamuel on 11/10/16.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;
import org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig;
import org.openmrs.notification.Alert;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Date;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

public class UgandaEMRHttpURLConnection {
	
	Log log = LogFactory.getLog(getClass());
	
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
			urlConnection.setConnectTimeout(60000); // long timeout, but not infinite
			urlConnection.setReadTimeout(120000);
			urlConnection.setUseCaches(false);
			urlConnection.setDefaultUseCaches(false);
			
			// tell the web server what we are sending
			urlConnection.setRequestProperty("Content-Type", contentType);
			urlConnection.setRequestProperty("User-Agent", USER_AGENT);
			urlConnection.setRequestProperty("Accept", contentType);
			
			OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
			writer.write(content);
			writer.flush();
			writer.close();
			
			Map map = new HashMap();
			int responseCode = ((HttpsURLConnectionImpl) urlConnection).getResponseCode();
			//reading the response
			if ((responseCode == CONNECTION_SUCCESS_200 || responseCode == CONNECTION_SUCCESS_201)) {
				InputStream inputStreamReader = urlConnection.getInputStream();
				map = getMapOfResults(inputStreamReader, responseCode);
			} else {
				map.put("responseCode", responseCode);
			}
			return map;
		}
		catch (Throwable t) {
			log.error(t);
			
		}
		return null;
	}
	
	public Map getMapOfResults(InputStream inputStreamReader, int responseCode) throws IOException {
		Map map = new HashMap();
		InputStreamReader reader = new InputStreamReader(inputStreamReader);
		StringBuilder buf = new StringBuilder();
		char[] cbuf = new char[2048];
		int num;
		while (true) {
			if (!(-1 != (num = reader.read(cbuf))))
				break;
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
	
	public boolean isConnectionAvailable() {
		try {
			final URL url = new URL(UgandaEMRSyncConfig.CONNECTIVITY_CHECK_URL);
			final URLConnection conn = url.openConnection();
			conn.connect();
			conn.getInputStream().close();
			log.info(UgandaEMRSyncConfig.CONNECTIVITY_CHECK_SUCCESS);
			return true;
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			log.info(UgandaEMRSyncConfig.CONNECTIVITY_CHECK_FAILED);
			return false;
		}
	}
	
	public boolean isServerAvailable(String strUrl) {
		try {
			final URL url = new URL(strUrl);
			final URLConnection conn = url.openConnection();
			conn.connect();
			conn.getInputStream().close();
			log.info(UgandaEMRSyncConfig.SERVER_CONNECTION_SUCCESS);
			return true;
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			log.info(UgandaEMRSyncConfig.SERVER_CONNECTION_FAILED);
			return false;
		}
	}
	
	public HttpResponse httpPost(String recencyServerUrl, String bodyText) {
		HttpResponse response = null;
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(recencyServerUrl);
		SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
		try{

		post.addHeader(UgandaEMRSyncConfig.HEADER_EMR_DATE, new Date().toString());

		UsernamePasswordCredentials credentials
				= new UsernamePasswordCredentials(syncGlobalProperties.getGlobalProperty(UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID), syncGlobalProperties.getGlobalProperty(UgandaEMRSyncConfig.GP_RECENCY_SERVER_PASSWORD));
		post.addHeader(new BasicScheme().authenticate(credentials, post, null));

		HttpEntity multipart = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addTextBody(UgandaEMRSyncConfig.DHIS_ORGANIZATION_UUID, syncGlobalProperties.getGlobalProperty(UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID))
				.addTextBody(UgandaEMRSyncConfig.HTTP_TEXT_BODY_DATA_TYPE_KEY, bodyText, ContentType.TEXT_PLAIN) // Current implementation uses plain text due to decoding challenges on the receiving server.
				.build();
		post.setEntity(multipart);

		response = client.execute(post);
		} catch (IOException | AuthenticationException e) {
			log.info("Exception sending Recency data "+ e.getMessage());
		}
		return response;
	}
	
	public void setAlertForAllUsers(String alertMessage) {
		List<User> userList = Context.getUserService().getAllUsers();
		Alert alert = new Alert();
		for (User user : userList) {
			alert.addRecipient(user);
		}
		alert.setText(alertMessage);
		Context.getAlertService().saveAlert(alert);
	}
	
	public String getBaseURL(String serverUrl) {
		try {
			URL url = new URL(serverUrl);
			serverUrl = url.getProtocol() + "://" + url.getHost();
		}
		catch (MalformedURLException e) {
			log.info("Unknown Protocol" + e);
		}
		return serverUrl;
	}
	
	/**
	 * Validate JSON String
	 * 
	 * @param test
	 * @return
	 */
	public boolean isJSONValid(String test) {
		try {
			new JSONObject(test);
		}
		catch (JSONException ex) {
			try {
				new JSONArray(test);
			}
			catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}
}
