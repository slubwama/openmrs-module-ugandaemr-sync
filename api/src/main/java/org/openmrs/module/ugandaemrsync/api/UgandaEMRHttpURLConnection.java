package org.openmrs.module.ugandaemrsync.api;

/**
 * Created by lubwamasamuel on 11/10/16.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.server.Facility;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig;
import org.openmrs.notification.Alert;

import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Date;

import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_FACILITY_NAME;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.CONNECTION_SUCCESS_200;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.CONNECTION_SUCCESS_201;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.HEALTH_CENTER_SYNC_ID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_USERNAME;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_PASSWORD;

public class UgandaEMRHttpURLConnection {

    Log log = LogFactory.getLog(UgandaEMRHttpURLConnection.class);

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
     * HTTP Get request
     *
     * @param url
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public Map getByWithBasicAuth(String url, String username, String password, String resultType) throws Exception {


        HttpResponse response = null;


        HttpUriRequest httpGet = new HttpGet(url);
        httpGet.setHeader("Method", "GET");

        Map map = new HashMap();
        try {
            SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
            CloseableHttpClient client = createAcceptSelfSignedCertificateClient();

            httpGet.addHeader(UgandaEMRSyncConfig.HEADER_EMR_DATE, new Date().toString());

            if (username != null && password != null) {
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

                httpGet.addHeader(new BasicScheme().authenticate(credentials, httpGet, null));
            }
            httpGet.addHeader("x-ugandaemr-facilityname", syncGlobalProperties.getGlobalProperty(GP_FACILITY_NAME));
            httpGet.addHeader("x-ugandaemr-dhis2uuid", syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID));


            response = client.execute(httpGet);

            int responseCode = response.getStatusLine().getStatusCode();
            String responseMessage = response.getStatusLine().getReasonPhrase();
            //reading the response
            map.put("responseCode", responseCode);
            if ((responseCode == CONNECTION_SUCCESS_200 || responseCode == CONNECTION_SUCCESS_201)) {
                InputStream inputStreamReader = response.getEntity().getContent();
                HttpEntity entityResponse = response.getEntity();
                if (resultType.equals("String")) {
                    map.put("result", getStringOfResults(inputStreamReader));
                } else if (resultType.equals("Map")) {
                    map = getMapOfResults(entityResponse, responseCode);
                }
            } else {
                map.put("responseCode", responseCode);
                log.info(responseMessage);
            }
            map.put("responseMessage", responseMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return map;

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
    public Map  sendPostByWithBasicAuth(String contentType, String content, String facilityId, String url, String username, String password, String token) throws Exception {

        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        HttpResponse response = null;

        HttpPost post = new HttpPost(url);

        Map map = new HashMap();


        try {
            CloseableHttpClient client = createAcceptSelfSignedCertificateClient();

            post.addHeader(UgandaEMRSyncConfig.HEADER_EMR_DATE, new Date().toString());

            if (username != null && password != null) {
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

                post.addHeader(new BasicScheme().authenticate(credentials, post, null));
            }

                    if (token != null && !token.equals("")) {
                post.addHeader("Authorization", token);
            }

            post.addHeader("x-ugandaemr-facilityname", syncGlobalProperties.getGlobalProperty(GP_FACILITY_NAME));

            post.addHeader("x-ugandaemr-dhis2uuid", syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID));

            HttpEntity httpEntity = new StringEntity(content, ContentType.APPLICATION_JSON);

            if (contentType != null && contentType != "") {
                ((StringEntity) httpEntity).setContentType(contentType);
            }

            post.setEntity(httpEntity);

            response = client.execute(post);

            int responseCode = response.getStatusLine().getStatusCode();
            String responseMessage = response.getStatusLine().getReasonPhrase();
            //reading the response
            if ((responseCode == CONNECTION_SUCCESS_200 || responseCode == CONNECTION_SUCCESS_201)) {
                HttpEntity responseEntity = response.getEntity();
                map = getMapOfResults(responseEntity, responseCode);
            } else {
                map.put("responseCode", responseCode);
                log.info(responseMessage);
            }
            map.put("responseMessage", responseMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
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
    public Map sendPostBy(String url, String username, String password, String token, String data, boolean facilityIdRequired) throws Exception {
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        String contentTypeJSON = SyncConstant.JSON_CONTENT_TYPE;

        String facilitySyncId = "";
        if (facilityIdRequired) {
            facilitySyncId = syncGlobalProperties.getGlobalProperty(HEALTH_CENTER_SYNC_ID);
        }


        return sendPostByWithBasicAuth(contentTypeJSON, data, facilitySyncId, url, username, password, token);
    }

    public Map<String, Object> getMapOfResults(HttpEntity httpEntity, int responseCode) throws IOException {
        final String responseString = EntityUtils.toString(httpEntity);
        Map<String, Object> resultMap = isJSONValid(responseString)
                ? deserializeToMap(responseString)
                : new HashMap<>();

        resultMap.put("responseCode", responseCode);
        return resultMap;
    }

    private Map<String, Object> deserializeToMap(String jsonString) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode rootNode = objectMapper.readTree(jsonString);

        if (rootNode.isObject()) {
            return objectMapper.convertValue(rootNode, new TypeReference<Map<String, Object>>() {});
        } else {
            // Optionally log or warn here if non-object input is unexpected
            return new HashMap<>();
        }
    }

    public String getStringOfResults(InputStream inputStreamReader) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStreamReader);
        StringBuilder buf = new StringBuilder();
        char[] cbuf = new char[2048];
        int num;
        while (true) {
            if (!(-1 != (num = reader.read(cbuf)))) break;
            buf.append(cbuf, 0, num);
        }
        String result = buf.toString();
        return result;
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
        String serverIP = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_IP);
        String serverProtocol = syncGlobalProperties.getGlobalProperty(SyncConstant.SERVER_PROTOCOL);
        String facilityURL = serverProtocol + serverIP + "/" + "api/facility";

        Location location = service.getLocation(Integer.valueOf(2));

        Facility facility = new Facility(location.getName());

        ObjectMapper mapper = new ObjectMapper();

        String jsonInString = mapper.writeValueAsString(facility);

        Map facilityMap = sendPostBy(facilityURL, syncGlobalProperties.getGlobalProperty(SERVER_USERNAME), syncGlobalProperties.getGlobalProperty(SERVER_PASSWORD), "", jsonInString, true);

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
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
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
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.info(UgandaEMRSyncConfig.SERVER_CONNECTION_FAILED);
            return false;
        }
    }

    public HttpResponse post(String url, String bodyText,String username,String password) {
        HttpResponse response = null;
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        try {

            post.addHeader(UgandaEMRSyncConfig.HEADER_EMR_DATE, new Date().toString());

            UsernamePasswordCredentials credentials
                    = new UsernamePasswordCredentials(username,password);
            post.addHeader(new BasicScheme().authenticate(credentials, post, null));

            HttpEntity multipart = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE).addTextBody(UgandaEMRSyncConfig.DHIS_ORGANIZATION_UUID, syncGlobalProperties.getGlobalProperty(UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID)).addTextBody(UgandaEMRSyncConfig.HTTP_TEXT_BODY_DATA_TYPE_KEY, bodyText, ContentType.APPLICATION_JSON)// Current implementation uses plain text due to decoding challenges on the receiving server.
                    .build();
            post.setEntity(multipart);

            response = client.execute(post);
        } catch (IOException | AuthenticationException e) {
            log.info("Exception sending Recency data " + e.getMessage());
        }
        return response;
    }

    public HttpResponse httpPost(String serverUrl, String bodyText, String username, String password) {
        HttpResponse response = null;

        HttpPost post = new HttpPost(serverUrl);
        SyncGlobalProperties syncGlobalProperties = new SyncGlobalProperties();
        try {
            CloseableHttpClient client = createAcceptSelfSignedCertificateClient();
            post.addHeader(UgandaEMRSyncConfig.HEADER_EMR_DATE, new Date().toString());

            UsernamePasswordCredentials credentials
                    = new UsernamePasswordCredentials(username, password);
            post.addHeader(new BasicScheme().authenticate(credentials, post, null));


            post.addHeader("x-ugandaemr-facilityname", syncGlobalProperties.getGlobalProperty(GP_FACILITY_NAME));

            post.addHeader("x-ugandaemr-dhis2uuid", syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID));

            HttpEntity httpEntity = new StringEntity(bodyText, ContentType.APPLICATION_JSON);

            post.setEntity(httpEntity);

            response = client.execute(post);
        } catch (Exception e) {
            log.error("Exception sending Analytics data " + e.getMessage());
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
        } catch (MalformedURLException e) {
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

        ObjectMapper objectMapper=new ObjectMapper();
        try {
            objectMapper.readTree(test);
        } catch (IOException e) {
            return  false;
        }
        return true;
    }

    public static CloseableHttpClient createAcceptSelfSignedCertificateClient()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        // use the TrustSelfSignedStrategy to allow Self Signed Certificates
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();

        // we can optionally disable hostname verification.
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

        // finally create the HttpClient using HttpClient factory methods and assign the ssl socket factory
        return HttpClients
                .custom()
                .setSSLSocketFactory(connectionFactory)
                .build();
    }

    public String getJson(String url) {
        URLConnection request = null;
        try {
            URL u = new URL(url);
            request = u.openConnection();
            request.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            return sb.toString();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Map getTokenFromServer(String url, String username, String password) {
        Map map = new HashMap<>();
        try {
            URI uri = new URI(url);
            URL connectionUrl = uri.toURL();

            // Create a connection
            HttpURLConnection connection = (HttpURLConnection) connectionUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            // Define the parameters
            String parameters = "username=" + username + "&password=" + password + "&grant_type=password";

            // Write the parameters to the connection
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = parameters.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            String responseMessage = connection.getResponseMessage();
            //reading the response
            map.put("responseCode", responseCode);
            if ((responseCode == CONNECTION_SUCCESS_200 || responseCode == CONNECTION_SUCCESS_201)) {
                InputStream inputStreamReader = connection.getInputStream();
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> parsedMap = objectMapper.readValue(getStringOfResults(inputStreamReader), new TypeReference<Map<String, Object>>() {});
                map.putAll(parsedMap);
            } else {
                map.put("responseCode", responseCode);
                log.info(responseMessage);
            }
            map.put("responseMessage", responseMessage);
            connection.disconnect();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return map;
    }
}
