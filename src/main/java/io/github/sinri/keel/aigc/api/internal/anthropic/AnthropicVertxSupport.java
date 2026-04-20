package io.github.sinri.keel.aigc.api.internal.anthropic;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;

/**
 * Anthropic Messages API 的 Vert.x HTTP 发送（含鉴权与版本头）。
 */
public final class AnthropicVertxSupport {

    private static final String MESSAGES_PATH = "/messages";

    private AnthropicVertxSupport() {
    }

    /**
     * {@code POST /v1/messages}，可选 {@code Accept: text/event-stream}。
     */
    public static Future<HttpClientResponse> sendMessagesPost(
        HttpClient httpClient,
        String baseUrl,
        String apiKey,
        String anthropicVersion,
        JsonObject requestBody,
        boolean stream
    ) {
        RequestOptions options = new RequestOptions()
            .setMethod(HttpMethod.POST)
            .setAbsoluteURI(baseUrl + MESSAGES_PATH)
            .putHeader("Content-Type", "application/json")
            .putHeader("x-api-key", apiKey)
            .putHeader("Authorization", "Bearer " + apiKey)
            .putHeader("anthropic-version", anthropicVersion);

        if (stream) {
            options.putHeader("Accept", "text/event-stream");
        }

        return httpClient.request(options)
            .compose(req -> req.send(Buffer.buffer(requestBody.encode())));
    }
}
