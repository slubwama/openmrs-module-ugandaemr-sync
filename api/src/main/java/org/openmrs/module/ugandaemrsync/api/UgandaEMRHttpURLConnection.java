package org.openmrs.module.ugandaemrsync.api;

/**
 * Created by lubwamasamuel on 11/10/16.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
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
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.exception.UgandaEMRSyncException;
import org.openmrs.module.ugandaemrsync.logging.StructuredLogger;
import org.openmrs.module.ugandaemrsync.circuitbreaker.CircuitBreaker;
import org.openmrs.module.ugandaemrsync.circuitbreaker.CircuitBreakerConfig;
import org.openmrs.module.ugandaemrsync.circuitbreaker.CircuitBreakerRegistry;
import org.openmrs.module.ugandaemrsync.server.Facility;
import org.openmrs.module.ugandaemrsync.server.SyncConstant;
import org.openmrs.module.ugandaemrsync.server.SyncGlobalProperties;
import org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig;
import org.openmrs.notification.Alert;
import org.springframework.beans.factory.annotation.Autowired;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
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
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_DHIS2_ORGANIZATION_UUID;
import static org.openmrs.module.ugandaemrsync.UgandaEMRSyncConfig.GP_FACILITY_NAME;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.CONNECTION_SUCCESS_200;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.CONNECTION_SUCCESS_201;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.HEALTH_CENTER_SYNC_ID;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_USERNAME;
import static org.openmrs.module.ugandaemrsync.server.SyncConstant.SERVER_PASSWORD;

public class UgandaEMRHttpURLConnection {

    private final Log log = LogFactory.getLog(UgandaEMRHttpURLConnection.class);
    private final StructuredLogger structuredLogger = StructuredLogger.getLogger(UgandaEMRHttpURLConnection.class);

    // Shared HTTP client with connection pooling
    private static volatile CloseableHttpClient pooledHttpClient;
    private static final Object CLIENT_LOCK = new Object();

    // Default retry policies for different operation types
    private static final RetryPolicy CRITICAL_RETRY_POLICY = RetryPolicy.getConservativeRetryPolicy();
    private static final RetryPolicy STANDARD_RETRY_POLICY = RetryPolicy.getDefaultHttpRetryPolicy();

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
                HttpEntity entityResponse = response.getEntity();
                if (resultType.equals("String")) {
                    try (InputStream inputStream = entityResponse.getContent()) {
                        map.put("result", getStringOfResults(inputStream));
                    }
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

    public Map getMapOfResults(HttpEntity inputStreamReader, int responseCode) throws IOException {
        Map map = new HashMap();
        String responseString = EntityUtils.toString(inputStreamReader);

        if (isJSONValid(responseString)) {
            map = new JSONObject(responseString).toMap();
        }

        map.put("responseCode", responseCode);

        return map;
    }

    public String getStringOfResults(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            StringBuilder buf = new StringBuilder();
            char[] cbuf = new char[2048];
            int num;
            while ((num = reader.read(cbuf)) != -1) {
                buf.append(cbuf, 0, num);
            }
            return buf.toString();
        }
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
            try (InputStream is = conn.getInputStream()) {
                // Read and discard content to check connectivity
                byte[] buffer = new byte[1024];
                while (is.read(buffer) > 0) {
                    // Discard data
                }
            }
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
            try (InputStream is = conn.getInputStream()) {
                // Read and discard content to check connectivity
                byte[] buffer = new byte[1024];
                while (is.read(buffer) > 0) {
                    // Discard data
                }
            }
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

    /**
     * Get or create a shared HTTP client with connection pooling
     * This method implements double-checked locking for thread-safe lazy initialization
     * and uses connection pooling to improve performance and resource management.
     */
    public static CloseableHttpClient getPooledHttpClient() {
        if (pooledHttpClient == null) {
            synchronized (CLIENT_LOCK) {
                if (pooledHttpClient == null) {
                    try {
                        // Configure connection pooling for HTTP Client 4.5
                        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
                        connectionManager.setMaxTotal(20); // Maximum total connections
                        connectionManager.setDefaultMaxPerRoute(10); // Maximum connections per route

                        // Create SSL context that accepts self-signed certificates
                        SSLContext sslContext = SSLContextBuilder.create()
                                .loadTrustMaterial(new TrustSelfSignedStrategy())
                                .build();

                        // Create hostname verifier that accepts all hosts
                        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

                        // Create SSL socket factory
                        org.apache.http.conn.ssl.SSLConnectionSocketFactory connectionFactory =
                                new org.apache.http.conn.ssl.SSLConnectionSocketFactory(sslContext, allowAllHosts);

                        // Build the HTTP client with connection pooling
                        pooledHttpClient = HttpClients.custom()
                                .setConnectionManager(connectionManager)
                                .setSSLSocketFactory(connectionFactory)
                                .build();

                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create pooled HTTP client", e);
                    }
                }
            }
        }
        return pooledHttpClient;
    }

    public static CloseableHttpClient createAcceptSelfSignedCertificateClient()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        // Use the pooled client instead of creating new clients
        return getPooledHttpClient();
    }

    public String getJson(String url) {
        URLConnection request = null;
        try {
            URL u = new URL(url);
            request = u.openConnection();
            request.connect();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                return sb.toString();
            }

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
                try (InputStream inputStream = connection.getInputStream()) {
                    map.putAll(new JSONObject(getStringOfResults(inputStream)).toMap());
                }
            } else {
                map.put("responseCode", responseCode);
                log.info(responseMessage);
            }
            map.put("responseMessage", responseMessage);
            connection.disconnect();
        } catch (Exception e) {
            log.error("Error getting token from server: " + e.getMessage(), e);
            map.put("error", e.getMessage());
        }
        return map;
    }

    /**
     * Enhanced HTTP POST with retry logic and better error handling
     * @param url Target URL
     * @param content Request body content
     * @param username Basic auth username (can be null)
     * @param password Basic auth password (can be null)
     * @param contentType Content type header
     * @param operationName Operation name for logging
     * @return Response map with responseCode, responseMessage, and result
     */
    public Map sendPostWithRetry(String url, String content, String username, String password,
                                String contentType, String operationName) throws Exception {

        String correlationId = structuredLogger.logExternalServiceCall("HTTP_POST", url,
                java.util.Collections.singletonMap("operation", operationName));
        long startTime = System.currentTimeMillis();

        try {
            RetryExecutor retryExecutor = new RetryExecutor(STANDARD_RETRY_POLICY);

            return retryExecutor.execute(() -> {
                try {
                    return sendPostByWithBasicAuthInternal(url, content, username, password, contentType);
                } catch (Exception e) {
                    throw convertException(e, url);
                }
            }, "HTTP_POST: " + operationName);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            structuredLogger.logExternalServiceResponse(correlationId, "HTTP_POST", 0, duration);
        }
    }

    /**
     * Enhanced HTTP GET with retry logic and better error handling
     * @param url Target URL
     * @param username Basic auth username (can be null)
     * @param password Basic auth password (can be null)
     * @param resultType Expected result type ("String" or "Map")
     * @param operationName Operation name for logging
     * @return Response map with responseCode, responseMessage, and result
     */
    public Map sendGetWithRetry(String url, String username, String password,
                               String resultType, String operationName) throws Exception {

        String correlationId = structuredLogger.logExternalServiceCall("HTTP_GET", url,
                java.util.Collections.singletonMap("operation", operationName));
        long startTime = System.currentTimeMillis();

        try {
            RetryExecutor retryExecutor = new RetryExecutor(STANDARD_RETRY_POLICY);

            return retryExecutor.execute(() -> {
                try {
                    return getByWithBasicAuthInternal(url, username, password, resultType);
                } catch (Exception e) {
                    throw convertException(e, url);
                }
            }, "HTTP_GET: " + operationName);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            structuredLogger.logExternalServiceResponse(correlationId, "HTTP_GET", 0, duration);
        }
    }

    /**
     * Convert generic exceptions to UgandaEMRSyncException for better error handling
     */
    private UgandaEMRSyncException convertException(Exception e, String url) {
        UgandaEMRSyncException.ErrorCode errorCode;

        if (e instanceof ConnectTimeoutException || e instanceof ConnectionPoolTimeoutException) {
            errorCode = UgandaEMRSyncException.ErrorCode.CONNECTION_TIMEOUT;
        } else if (e instanceof NoHttpResponseException) {
            errorCode = UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED;
        } else if (e instanceof java.net.SocketTimeoutException) {
            errorCode = UgandaEMRSyncException.ErrorCode.READ_TIMEOUT;
        } else if (e instanceof java.net.UnknownHostException) {
            errorCode = UgandaEMRSyncException.ErrorCode.CONNECTION_FAILED;
        } else if (e instanceof org.apache.http.client.HttpResponseException) {
            int statusCode = ((org.apache.http.client.HttpResponseException) e).getStatusCode();
            if (statusCode >= 500) {
                errorCode = UgandaEMRSyncException.ErrorCode.HTTP_SERVER_ERROR;
            } else if (statusCode == 401) {
                errorCode = UgandaEMRSyncException.ErrorCode.HTTP_UNAUTHORIZED;
            } else if (statusCode == 403) {
                errorCode = UgandaEMRSyncException.ErrorCode.HTTP_FORBIDDEN;
            } else {
                errorCode = UgandaEMRSyncException.ErrorCode.HTTP_CLIENT_ERROR;
            }
        } else {
            errorCode = UgandaEMRSyncException.ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE;
        }

        return new UgandaEMRSyncException(errorCode,
                "HTTP operation failed for URL: " + url, e);
    }

    /**
     * Internal method for HTTP POST without retry logic
     * Used by retry executor
     */
    private Map sendPostByWithBasicAuthInternal(String url, String content, String username,
                                               String password, String contentType) throws Exception {

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

            post.addHeader("x-ugandaemr-facilityname", syncGlobalProperties.getGlobalProperty(GP_FACILITY_NAME));
            post.addHeader("x-ugandaemr-dhis2uuid", syncGlobalProperties.getGlobalProperty(GP_DHIS2_ORGANIZATION_UUID));

            if (contentType != null && !contentType.isEmpty()) {
                HttpEntity httpEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
                ((StringEntity) httpEntity).setContentType(contentType);
                post.setEntity(httpEntity);
            } else {
                HttpEntity httpEntity = new StringEntity(content, ContentType.APPLICATION_JSON);
                post.setEntity(httpEntity);
            }

            response = client.execute(post);

            int responseCode = response.getStatusLine().getStatusCode();
            String responseMessage = response.getStatusLine().getReasonPhrase();

            map.put("responseCode", responseCode);
            if ((responseCode == CONNECTION_SUCCESS_200 || responseCode == CONNECTION_SUCCESS_201)) {
                HttpEntity responseEntity = response.getEntity();
                map = getMapOfResults(responseEntity, responseCode);
            } else {
                log.info("HTTP POST response: " + responseMessage);
            }
            map.put("responseMessage", responseMessage);

        } catch (IOException e) {
            log.error("IOException during HTTP POST: " + e.getMessage(), e);
            throw e;
        } catch (AuthenticationException e) {
            log.error("Authentication exception during HTTP POST: " + e.getMessage(), e);
            throw e;
        }

        return map;
    }

    /**
     * Internal method for HTTP GET without retry logic
     * Used by retry executor
     */
    private Map getByWithBasicAuthInternal(String url, String username, String password,
                                         String resultType) throws Exception {

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

            map.put("responseCode", responseCode);
            if ((responseCode == CONNECTION_SUCCESS_200 || responseCode == CONNECTION_SUCCESS_201)) {
                InputStream inputStream = response.getEntity().getContent();
                HttpEntity entityResponse = response.getEntity();
                if (resultType.equals("String")) {
                    map.put("result", getStringOfResults(inputStream));
                } else if (resultType.equals("Map")) {
                    map = getMapOfResults(entityResponse, responseCode);
                }
            } else {
                log.info("HTTP GET response: " + responseMessage);
            }
            map.put("responseMessage", responseMessage);

        } catch (IOException e) {
            log.error("IOException during HTTP GET: " + e.getMessage(), e);
            throw e;
        } catch (AuthenticationException e) {
            log.error("Authentication exception during HTTP GET: " + e.getMessage(), e);
            throw e;
        }

        return map;
    }

    /**
     * Execute HTTP operation with circuit breaker protection
     * @param serviceType Type of external service (e.g., "CPHL", "DHIS2", "CENTRAL_SERVER")
     * @param url Target URL
     * @param operation The HTTP operation to execute
     * @param <T> Return type
     * @return Result of the operation
     * @throws Exception if circuit is open or operation fails
     */
    public <T> T executeWithCircuitBreaker(String serviceType, String url,
                                         CircuitBreaker.CircuitBreakerOperation<T> operation) throws Exception {

        CircuitBreaker circuitBreaker = CircuitBreakerRegistry.getInstance()
                .getCircuitBreaker(serviceType, CircuitBreakerConfig.getDefault());

        if (circuitBreaker == null) {
            // Circuit breaker disabled, execute directly
            return operation.execute();
        }

        try {
            return circuitBreaker.execute(operation);

        } catch (CircuitBreaker.CircuitBreakerOpenException e) {
            log.error("Circuit breaker is OPEN for service: " + serviceType);
            throw new UgandaEMRSyncException(
                    UgandaEMRSyncException.ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE,
                    "Service '" + serviceType + "' is temporarily unavailable due to repeated failures",
                    e
            );
        }
    }

    /**
     * Get status of all circuit breakers for monitoring
     */
    public String getCircuitBreakerStatus() {
        CircuitBreakerRegistry.CircuitBreakerStats stats =
                CircuitBreakerRegistry.getInstance().getStats();

        return String.format("CircuitBreaker Status: %s", stats.toString());
    }
}
