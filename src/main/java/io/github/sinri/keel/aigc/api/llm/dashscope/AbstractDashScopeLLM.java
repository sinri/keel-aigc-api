package io.github.sinri.keel.aigc.api.llm.dashscope;

import io.github.sinri.keel.aigc.api.llm.catholic.AuthMethod;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
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
 * DashScope API 客户端抽象基类，封装共享的 HTTP 通信逻辑。
 */
public abstract class AbstractDashScopeLLM implements CatholicLLM {

    protected static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/api/v1";
    protected static final AuthMethod DEFAULT_AUTH_METHOD = AuthMethod.Bearer;

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final AuthMethod authMethod;
    private final Keel keel;

    protected AbstractDashScopeLLM(HttpClient httpClient, String apiKey, String baseUrl) {
        this(httpClient, apiKey, baseUrl, DEFAULT_AUTH_METHOD);
    }

    protected AbstractDashScopeLLM(HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.authMethod = authMethod;
        this.keel = Keel.shared();
    }

    protected Keel getKeel() {
        return keel;
    }

    /**
     * 发送 JSON POST 请求到 DashScope API。
     */
    protected Future<HttpClientResponse> sendJsonPost(JsonObject requestBody, String path, boolean stream) {
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
}
