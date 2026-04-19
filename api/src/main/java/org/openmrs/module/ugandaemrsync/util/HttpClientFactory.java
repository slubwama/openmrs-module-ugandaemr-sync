package org.openmrs.module.ugandaemrsync.util;

import org.apache.http.impl.client.CloseableHttpClient;
import org.openmrs.module.ugandaemrsync.api.RetryPolicy;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRHttpURLConnection;

/**
 * Factory for creating HTTP clients and configurations
 * Implements factory pattern to centralize HTTP client creation and reduce duplication
 */
public class HttpClientFactory {

    private static volatile HttpClientFactory instance;
    private final CloseableHttpClient sharedHttpClient;

    private HttpClientFactory() {
        this.sharedHttpClient = UgandaEMRHttpURLConnection.getPooledHttpClient();
    }

    /**
     * Get singleton instance of HTTP client factory
     */
    public static HttpClientFactory getInstance() {
        if (instance == null) {
            synchronized (HttpClientFactory.class) {
                if (instance == null) {
                    instance = new HttpClientFactory();
                }
            }
        }
        return instance;
    }

    /**
     * Get shared HTTP client with connection pooling
     */
    public CloseableHttpClient getSharedHttpClient() {
        return sharedHttpClient;
    }

    /**
     * Create HTTP client configuration for specific use cases
     */
    public static class HttpClientConfig {
        private final int maxConnections;
        private final int maxConnectionsPerRoute;
        private final int connectionTimeoutMs;
        private final int socketTimeoutMs;
        private final RetryPolicy retryPolicy;

        private HttpClientConfig(Builder builder) {
            this.maxConnections = builder.maxConnections;
            this.maxConnectionsPerRoute = builder.maxConnectionsPerRoute;
            this.connectionTimeoutMs = builder.connectionTimeoutMs;
            this.socketTimeoutMs = builder.socketTimeoutMs;
            this.retryPolicy = builder.retryPolicy;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public int getMaxConnectionsPerRoute() {
            return maxConnectionsPerRoute;
        }

        public int getConnectionTimeoutMs() {
            return connectionTimeoutMs;
        }

        public int getSocketTimeoutMs() {
            return socketTimeoutMs;
        }

        public RetryPolicy getRetryPolicy() {
            return retryPolicy;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int maxConnections = 20;
            private int maxConnectionsPerRoute = 10;
            private int connectionTimeoutMs = 10000; // 10 seconds
            private int socketTimeoutMs = 30000; // 30 seconds
            private RetryPolicy retryPolicy = RetryPolicy.getDefaultHttpRetryPolicy();

            public Builder maxConnections(int maxConnections) {
                this.maxConnections = maxConnections;
                return this;
            }

            public Builder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
                this.maxConnectionsPerRoute = maxConnectionsPerRoute;
                return this;
            }

            public Builder connectionTimeout(int timeout, java.util.concurrent.TimeUnit unit) {
                this.connectionTimeoutMs = (int) unit.toMillis(timeout);
                return this;
            }

            public Builder socketTimeout(int timeout, java.util.concurrent.TimeUnit unit) {
                this.socketTimeoutMs = (int) unit.toMillis(timeout);
                return this;
            }

            public Builder retryPolicy(RetryPolicy retryPolicy) {
                this.retryPolicy = retryPolicy;
                return this;
            }

            public HttpClientConfig build() {
                return new HttpClientConfig(this);
            }
        }

        /**
         * Default configuration for general HTTP operations
         */
        public static HttpClientConfig getDefault() {
            return HttpClientConfig.builder().build();
        }

        /**
         * High-throughput configuration for bulk operations
         */
        public static HttpClientConfig getHighThroughput() {
            return HttpClientConfig.builder()
                    .maxConnections(50)
                    .maxConnectionsPerRoute(25)
                    .connectionTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                    .socketTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .retryPolicy(RetryPolicy.getAggressiveRetryPolicy())
                    .build();
        }

        /**
         * Low-latency configuration for critical operations
         */
        public static HttpClientConfig getLowLatency() {
            return HttpClientConfig.builder()
                    .maxConnections(10)
                    .maxConnectionsPerRoute(5)
                    .connectionTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                    .socketTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .retryPolicy(RetryPolicy.getConservativeRetryPolicy())
                    .build();
        }
    }

    /**
     * Factory for creating common response transformers
     */
    public static class ResponseTransformers {

        /**
         * Transform HTTP response to JSON
         */
        public static java.util.function.Function<String, Object> toJson() {
            return response -> {
                try {
                    return new org.json.JSONObject(response);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse JSON response", e);
                }
            };
        }

        /**
         * Transform HTTP response to JSON array
         */
        public static java.util.function.Function<String, Object> toJsonArray() {
            return response -> {
                try {
                    return new org.json.JSONArray(response);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse JSON array response", e);
                }
            };
        }

        /**
         * Transform HTTP response to string (identity function)
         */
        public static java.util.function.Function<String, String> asString() {
            return java.util.function.Function.identity();
        }
    }
}