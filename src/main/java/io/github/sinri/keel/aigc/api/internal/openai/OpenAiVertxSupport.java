package io.github.sinri.keel.aigc.api.internal.openai;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;

/**
 * OpenAI 兼容 HTTP 客户端（Vert.x）的共用实现：JSON POST 请求构造与鉴权。
 */
public final class OpenAiVertxSupport {

    private OpenAiVertxSupport() {
    }

    /**
     * {@code POST} JSON，可选 {@code Accept: text/event-stream}。
     */
    public static Future<HttpClientResponse> sendJsonPost(
            HttpClient httpClient,
            String absoluteUri,
            String apiKey,
            AuthMethod authMethod,
            JsonObject requestBody,
            boolean stream
    ) {
        RequestOptions options = new RequestOptions()
                .setMethod(HttpMethod.POST)
                .setAbsoluteURI(absoluteUri)
                .putHeader("Content-Type", "application/json");

        if (authMethod == AuthMethod.Bearer) {
            options.putHeader("Authorization", "Bearer " + apiKey);
        } else if (authMethod == AuthMethod.ApiKey) {
            options.putHeader("api-key", apiKey);
        } else {
            throw new IllegalArgumentException("authMethod must be Bearer or ApiKey");
        }
        if (stream) {
            options.putHeader("Accept", "text/event-stream");
        }

        return httpClient.request(options)
                         .compose(httpClientRequest -> sendJsonBody(httpClientRequest, requestBody));
    }

    private static Future<HttpClientResponse> sendJsonBody(HttpClientRequest httpClientRequest, JsonObject requestBody) {
        return httpClientRequest.send(Buffer.buffer(requestBody.encode()));
    }
}