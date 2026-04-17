package io.github.sinri.keel.aigc.api.llm.dashscope;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * DashScope API HTTP 通信辅助类，封装了与 DashScope API 交互的 HTTP 和 SSE 流式处理逻辑。
 * 供 DashScopeTextGenerationClient 和 DashScopeMultimodalGenerationClient 共用。
 */
public class DashScopeClientHelper {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/api/v1";

    public DashScopeClientHelper(HttpClient httpClient, String apiKey, String baseUrl) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 发送 JSON POST 请求到 DashScope API
     *
     * @param requestBody 请求体 JSON
     * @param path        API 路径（如 /services/aigc/text-generation/generation）
     * @param stream      是否为流式请求
     * @return HTTP 响应
     */
    public Future<HttpClientResponse> sendJsonPost(JsonObject requestBody, String path, boolean stream) {
        RequestOptions options = new RequestOptions()
            .setMethod(HttpMethod.POST)
            .setAbsoluteURI(baseUrl + path)
            .putHeader("Content-Type", "application/json")
            .putHeader("Authorization", "Bearer " + apiKey);

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

    /**
     * 要求响应成功（HTTP 200）并读取响应体
     */
    public Future<Buffer> requireSuccessAndReadBody(HttpClientResponse response, String serviceName) {
        if (response.statusCode() == 200) {
            return response.body();
        }
        return response.body().compose(body -> Future.failedFuture(
            new RuntimeException(serviceName + ": " + response.statusCode() + " - " + body)
        ));
    }

    /**
     * 消费 DashScope SSE 流式响应
     */
    public Future<Void> consumeDashScopeStream(
        HttpClientResponse response,
        DashScopeStreamHandler streamHandler,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        if (response.statusCode() != 200) {
            return requireSuccessAndReadBody(response, "DashScope API error").mapEmpty();
        }

        Promise<Void> promise = Promise.promise();
        AtomicReference<Future<Void>> processingChain = new AtomicReference<>(Future.succeededFuture());
        StringBuilder pendingLine = new StringBuilder();

        response.exceptionHandler(promise::tryFail);
        response.handler(buffer -> {
            pendingLine.append(buffer);
            drainLines(pendingLine, line -> {
                CatholicLLMResponseChunk chunk = streamHandler.processSseLine(line);
                enqueueChunk(processingChain, chunk, chunkAsyncProcessor, promise);
            });
        });
        response.endHandler(v -> {
            if (!promise.future().isComplete() && pendingLine.length() > 0) {
                CatholicLLMResponseChunk chunk = streamHandler.processSseLine(normalizeLine(pendingLine.toString()));
                enqueueChunk(processingChain, chunk, chunkAsyncProcessor, promise);
            }

            CatholicLLMResponseChunk finalChunk = streamHandler.flush();
            enqueueChunk(processingChain, finalChunk, chunkAsyncProcessor, promise);

            processingChain.get().onComplete(ar -> {
                if (ar.failed()) {
                    promise.tryFail(ar.cause());
                } else {
                    promise.tryComplete();
                }
            });
        });
        response.resume();

        return promise.future();
    }

    private void drainLines(StringBuilder pendingLine, java.util.function.Consumer<String> lineConsumer) {
        int newlineIndex;
        while ((newlineIndex = pendingLine.indexOf("\n")) >= 0) {
            String line = pendingLine.substring(0, newlineIndex);
            pendingLine.delete(0, newlineIndex + 1);
            lineConsumer.accept(normalizeLine(line));
        }
    }

    private String normalizeLine(String line) {
        if (line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }

    private void enqueueChunk(
        AtomicReference<Future<Void>> processingChain,
        CatholicLLMResponseChunk chunk,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor,
        Promise<Void> promise
    ) {
        if (chunk == null || promise.future().isComplete()) {
            return;
        }

        Future<Void> next = processingChain.get().compose(v -> chunkAsyncProcessor.apply(chunk));
        next.onFailure(promise::tryFail);
        processingChain.set(next);
    }

    // === Builder ===

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private HttpClient httpClient;
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;

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

        public DashScopeClientHelper build() {
            if (httpClient == null) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new DashScopeClientHelper(httpClient, apiKey, baseUrl);
        }
    }
}