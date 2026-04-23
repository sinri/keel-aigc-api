package io.github.sinri.keel.aigc.api.internal.dashscope;

import io.github.sinri.keel.aigc.api.llm.catholic.AuthMethod;
import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;

/**
 * DashScope API HTTP 通信辅助类，封装了与 DashScope API 交互的 HTTP 请求逻辑。
 * 供 DashScopeTextGenerationClient 和 DashScopeMultimodalGenerationClient 共用。
 */
public class DashScopeClientHelper {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final AuthMethod authMethod;
    private final Keel keel;

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/api/v1";
    private static final AuthMethod DEFAULT_AUTH_METHOD = AuthMethod.Bearer;

    public DashScopeClientHelper(HttpClient httpClient, String apiKey, String baseUrl) {
        this(httpClient, apiKey, baseUrl, DEFAULT_AUTH_METHOD);
    }

    public DashScopeClientHelper(HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.authMethod = authMethod;
        this.keel = Keel.shared();
    }

    public Keel getKeel() {
        return keel;
    }

    /**
     * 发送 JSON POST 请求到 DashScope API
     */
    public Future<HttpClientResponse> sendJsonPost(JsonObject requestBody, String path, boolean stream) {
        RequestOptions options = new RequestOptions()
            .setMethod(HttpMethod.POST)
            .setAbsoluteURI(baseUrl + path)
            .putHeader("Content-Type", "application/json");

        if (authMethod == AuthMethod.Bearer) {
            options.putHeader("Authorization", "Bearer " + apiKey);
        } else if (authMethod == AuthMethod.ApiKey) {
            options.putHeader("api-key", apiKey);
        } else {
            throw new IllegalArgumentException("authMethod must be Bearer or ApiKey");
        }

        if (stream) {
            options
                .putHeader("Accept", "text/event-stream")
                .putHeader("X-DashScope-SSE", "enable");
        }

        return httpClient.request(options)
            .compose(httpClientRequest -> sendJsonBody(httpClientRequest, requestBody));
    }

    private Future<HttpClientResponse> sendJsonBody(HttpClientRequest httpClientRequest, JsonObject requestBody) {
        return httpClientRequest.send(Buffer.buffer(requestBody.encode()));
    }

    // === Builder ===

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HttpClient httpClient;
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private AuthMethod authMethod = DEFAULT_AUTH_METHOD;

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder authMethod(AuthMethod authMethod) {
            this.authMethod = authMethod;
            return this;
        }

        public DashScopeClientHelper build() {
            if (httpClient == null) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new DashScopeClientHelper(httpClient, apiKey, baseUrl, authMethod);
        }
    }
}