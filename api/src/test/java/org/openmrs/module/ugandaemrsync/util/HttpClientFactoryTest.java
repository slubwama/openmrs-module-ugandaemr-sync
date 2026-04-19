package org.openmrs.module.ugandaemrsync.util;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.openmrs.module.ugandaemrsync.api.RetryPolicy;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test suite for HttpClientFactory
 */
public class HttpClientFactoryTest {

    @Test
    public void testGetInstance_ReturnsSingleton() {
        HttpClientFactory instance1 = HttpClientFactory.getInstance();
        HttpClientFactory instance2 = HttpClientFactory.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }

    @Test
    public void testGetSharedHttpClient_ReturnsHttpClient() {
        HttpClientFactory factory = HttpClientFactory.getInstance();
        CloseableHttpClient client = factory.getSharedHttpClient();

        assertNotNull(client);
    }

    @Test
    public void testGetSharedHttpClient_ReturnsSameInstance() {
        HttpClientFactory factory = HttpClientFactory.getInstance();
        CloseableHttpClient client1 = factory.getSharedHttpClient();
        CloseableHttpClient client2 = factory.getSharedHttpClient();

        assertSame(client1, client2);
    }

    @Test
    public void testHttpClientConfigBuilder_DefaultValues() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder().build();

        assertEquals(20, config.getMaxConnections());
        assertEquals(10, config.getMaxConnectionsPerRoute());
        assertEquals(10000, config.getConnectionTimeoutMs());
        assertEquals(30000, config.getSocketTimeoutMs());
        assertNotNull(config.getRetryPolicy());
    }

    @Test
    public void testHttpClientConfigBuilder_CustomMaxConnections() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .maxConnections(50)
                .build();

        assertEquals(50, config.getMaxConnections());
        assertEquals(10, config.getMaxConnectionsPerRoute());
    }

    @Test
    public void testHttpClientConfigBuilder_CustomMaxConnectionsPerRoute() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .maxConnectionsPerRoute(25)
                .build();

        assertEquals(20, config.getMaxConnections());
        assertEquals(25, config.getMaxConnectionsPerRoute());
    }

    @Test
    public void testHttpClientConfigBuilder_CustomConnectionTimeout() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .connectionTimeout(5, TimeUnit.SECONDS)
                .build();

        assertEquals(5000, config.getConnectionTimeoutMs());
    }

    @Test
    public void testHttpClientConfigBuilder_CustomSocketTimeout() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .socketTimeout(15, TimeUnit.SECONDS)
                .build();

        assertEquals(15000, config.getSocketTimeoutMs());
    }

    @Test
    public void testHttpClientConfigBuilder_CustomRetryPolicy() {
        RetryPolicy customPolicy = RetryPolicy.getAggressiveRetryPolicy();
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .retryPolicy(customPolicy)
                .build();

        assertSame(customPolicy, config.getRetryPolicy());
    }

    @Test
    public void testHttpClientConfigBuilder_ChainedSetters() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .maxConnections(100)
                .maxConnectionsPerRoute(50)
                .connectionTimeout(2, TimeUnit.SECONDS)
                .socketTimeout(10, TimeUnit.SECONDS)
                .retryPolicy(RetryPolicy.getConservativeRetryPolicy())
                .build();

        assertEquals(100, config.getMaxConnections());
        assertEquals(50, config.getMaxConnectionsPerRoute());
        assertEquals(2000, config.getConnectionTimeoutMs());
        assertEquals(10000, config.getSocketTimeoutMs());
        assertNotNull(config.getRetryPolicy());
    }

    @Test
    public void testHttpClientConfigBuilder_TimeUnitMilliseconds() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .connectionTimeout(500, TimeUnit.MILLISECONDS)
                .socketTimeout(1000, TimeUnit.MILLISECONDS)
                .build();

        assertEquals(500, config.getConnectionTimeoutMs());
        assertEquals(1000, config.getSocketTimeoutMs());
    }

    @Test
    public void testHttpClientConfigBuilder_TimeUnitMinutes() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .connectionTimeout(1, TimeUnit.MINUTES)
                .socketTimeout(2, TimeUnit.MINUTES)
                .build();

        assertEquals(60000, config.getConnectionTimeoutMs());
        assertEquals(120000, config.getSocketTimeoutMs());
    }

    @Test
    public void testGetDefaultConfiguration() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.getDefault();

        assertNotNull(config);
        assertEquals(20, config.getMaxConnections());
        assertEquals(10, config.getMaxConnectionsPerRoute());
        assertEquals(10000, config.getConnectionTimeoutMs());
        assertEquals(30000, config.getSocketTimeoutMs());
    }

    @Test
    public void testGetHighThroughputConfiguration() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.getHighThroughput();

        assertNotNull(config);
        assertEquals(50, config.getMaxConnections());
        assertEquals(25, config.getMaxConnectionsPerRoute());
        assertEquals(5000, config.getConnectionTimeoutMs());
        assertEquals(60000, config.getSocketTimeoutMs());
    }

    @Test
    public void testGetLowLatencyConfiguration() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.getLowLatency();

        assertNotNull(config);
        assertEquals(10, config.getMaxConnections());
        assertEquals(5, config.getMaxConnectionsPerRoute());
        assertEquals(2000, config.getConnectionTimeoutMs());
        assertEquals(10000, config.getSocketTimeoutMs());
    }

    @Test
    public void testResponseTransformers_ToJson() {
        String jsonResponse = "{\"name\":\"John\",\"age\":30}";
        Object result = HttpClientFactory.ResponseTransformers.toJson().apply(jsonResponse);

        assertNotNull(result);
        assertTrue(result instanceof org.json.JSONObject);
    }

    @Test
    public void testResponseTransformers_ToJsonArray() {
        String jsonArray = "[{\"name\":\"John\"},{\"name\":\"Jane\"}]";
        Object result = HttpClientFactory.ResponseTransformers.toJsonArray().apply(jsonArray);

        assertNotNull(result);
        assertTrue(result instanceof org.json.JSONArray);
    }

    @Test
    public void testResponseTransformers_AsString() {
        String input = "test response";
        String result = HttpClientFactory.ResponseTransformers.asString().apply(input);

        assertEquals("test response", result);
    }

    @Test
    public void testResponseTransformers_AsStringIsIdentityFunction() {
        String input = "original string";
        String result = HttpClientFactory.ResponseTransformers.asString().apply(input);

        assertSame(input, result);
    }

    @Test
    public void testResponseTransformers_ToJsonParsesCorrectly() {
        String jsonResponse = "{\"patient\":{\"id\":123,\"name\":\"John Doe\"}}";
        org.json.JSONObject result = (org.json.JSONObject) HttpClientFactory.ResponseTransformers.toJson().apply(jsonResponse);

        assertTrue(result.has("patient"));
        assertEquals(123, result.getJSONObject("patient").getInt("id"));
        assertEquals("John Doe", result.getJSONObject("patient").getString("name"));
    }

    @Test
    public void testResponseTransformers_ToJsonArrayParsesCorrectly() {
        String jsonArray = "[1,2,3,4,5]";
        org.json.JSONArray result = (org.json.JSONArray) HttpClientFactory.ResponseTransformers.toJsonArray().apply(jsonArray);

        assertEquals(5, result.length());
        assertEquals(1, result.getInt(0));
        assertEquals(5, result.getInt(4));
    }

    @Test(expected = RuntimeException.class)
    public void testResponseTransformers_ToJsonThrowsOnInvalidJson() {
        HttpClientFactory.ResponseTransformers.toJson().apply("invalid json");
    }

    @Test(expected = RuntimeException.class)
    public void testResponseTransformers_ToJsonArrayThrowsOnInvalidJson() {
        HttpClientFactory.ResponseTransformers.toJsonArray().apply("not an array");
    }

    @Test
    public void testHttpClientConfigGetters() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .maxConnections(30)
                .maxConnectionsPerRoute(15)
                .connectionTimeout(3, TimeUnit.SECONDS)
                .socketTimeout(20, TimeUnit.SECONDS)
                .retryPolicy(RetryPolicy.getDefaultHttpRetryPolicy())
                .build();

        assertEquals(30, config.getMaxConnections());
        assertEquals(15, config.getMaxConnectionsPerRoute());
        assertEquals(3000, config.getConnectionTimeoutMs());
        assertEquals(20000, config.getSocketTimeoutMs());
    }

    @Test
    public void testMultipleBuilderInstancesCreateIndependentConfigs() {
        HttpClientFactory.HttpClientConfig config1 = HttpClientFactory.HttpClientConfig.builder()
                .maxConnections(10)
                .build();

        HttpClientFactory.HttpClientConfig config2 = HttpClientFactory.HttpClientConfig.builder()
                .maxConnections(20)
                .build();

        assertEquals(10, config1.getMaxConnections());
        assertEquals(20, config2.getMaxConnections());
    }

    @Test
    public void testHighThroughputUsesAggressiveRetryPolicy() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.getHighThroughput();

        assertNotNull(config.getRetryPolicy());
        assertEquals(5, config.getRetryPolicy().getMaxAttempts());
    }

    @Test
    public void testLowLatencyUsesConservativeRetryPolicy() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.getLowLatency();

        assertNotNull(config.getRetryPolicy());
        assertEquals(2, config.getRetryPolicy().getMaxAttempts());
    }

    @Test
    public void testDefaultConfigUsesDefaultRetryPolicy() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.getDefault();

        assertNotNull(config.getRetryPolicy());
        assertEquals(3, config.getRetryPolicy().getMaxAttempts());
    }

    @Test
    public void testResponseTransformersAreStateless() {
        java.util.function.Function<String, Object> transformer1 = HttpClientFactory.ResponseTransformers.toJson();
        java.util.function.Function<String, Object> transformer2 = HttpClientFactory.ResponseTransformers.toJson();

        String input = "{\"test\":\"value\"}";
        Object result1 = transformer1.apply(input);
        Object result2 = transformer2.apply(input);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.toString(), result2.toString());
    }

    @Test
    public void testHttpClientConfigBuilder_WithZeroConnections() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .maxConnections(0)
                .maxConnectionsPerRoute(0)
                .build();

        assertEquals(0, config.getMaxConnections());
        assertEquals(0, config.getMaxConnectionsPerRoute());
    }

    @Test
    public void testHttpClientConfigBuilder_WithLargeTimeouts() {
        HttpClientFactory.HttpClientConfig config = HttpClientFactory.HttpClientConfig.builder()
                .connectionTimeout(5, TimeUnit.MINUTES)
                .socketTimeout(10, TimeUnit.MINUTES)
                .build();

        assertEquals(300000, config.getConnectionTimeoutMs());
        assertEquals(600000, config.getSocketTimeoutMs());
    }

    @Test
    public void testGetInstance_IsThreadSafe() {
        HttpClientFactory[] instances = new HttpClientFactory[10];

        for (int i = 0; i < instances.length; i++) {
            instances[i] = HttpClientFactory.getInstance();
        }

        for (int i = 1; i < instances.length; i++) {
            assertSame(instances[0], instances[i]);
        }
    }

    @Test
    public void testResponseTransformers_ToJsonWithEmptyObject() {
        org.json.JSONObject result = (org.json.JSONObject) HttpClientFactory.ResponseTransformers
                .toJson()
                .apply("{}");

        assertNotNull(result);
        assertEquals(0, result.length());
    }

    @Test
    public void testResponseTransformers_ToJsonArrayWithEmptyArray() {
        org.json.JSONArray result = (org.json.JSONArray) HttpClientFactory.ResponseTransformers
                .toJsonArray()
                .apply("[]");

        assertNotNull(result);
        assertEquals(0, result.length());
    }

    @Test
    public void testResponseTransformers_AsStringWithEmptyString() {
        String result = HttpClientFactory.ResponseTransformers.asString().apply("");

        assertEquals("", result);
    }

    @Test
    public void testResponseTransformers_AsStringWithNull() {
        String result = HttpClientFactory.ResponseTransformers.asString().apply(null);

        assertNull(result);
    }
}
