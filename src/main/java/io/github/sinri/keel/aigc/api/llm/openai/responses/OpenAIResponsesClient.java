package io.github.sinri.keel.aigc.api.llm.openai.responses;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
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
 * OpenAI Responses API 客户端，实现 {@link CatholicLLM}。
 */
public class OpenAIResponsesClient implements CatholicLLM {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final String RESPONSES_PATH = "/responses";

    public OpenAIResponsesClient(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_BASE_URL);
    }

    public OpenAIResponsesClient(HttpClient httpClient, String apiKey, String baseUrl) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        JsonObject responsesRequest = new OpenAIResponsesRequestConverter().convert(request);
        responsesRequest.put("stream", false);

        return sendJsonPost(responsesRequest, false)
            .compose(response -> requireSuccessAndReadBody(response, "OpenAI Responses API error"))
            .map(body -> new OpenAIResponsesResponseConverter().convert(body.toJsonObject()));
    }

    @Override
    public Future<Void> callStream(
        CatholicLLMRequest request,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        JsonObject responsesRequest = new OpenAIResponsesRequestConverter().convert(request);
        responsesRequest.put("stream", true);

        OpenAIResponsesStreamHandler streamHandler = new OpenAIResponsesStreamHandler();

        return sendJsonPost(responsesRequest, true)
            .compose(response -> consumeResponsesSseStream(response, streamHandler, chunkAsyncProcessor));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject responsesRequest = new OpenAIResponsesRequestConverter().convert(request);
        responsesRequest.put("stream", true);

        OpenAIResponsesStreamHandler streamHandler = new OpenAIResponsesStreamHandler();

        return sendJsonPost(responsesRequest, true)
            .compose(response -> consumeResponsesSseStream(response, streamHandler, chunk -> Future.succeededFuture()))
            .map(v -> streamHandler.buildFinalResponse());
    }

    private Future<HttpClientResponse> sendJsonPost(JsonObject requestBody, boolean stream) {
        RequestOptions options = new RequestOptions()
            .setMethod(HttpMethod.POST)
            .setAbsoluteURI(baseUrl + RESPONSES_PATH)
            .putHeader("Content-Type", "application/json")
            .putHeader("Authorization", "Bearer " + apiKey);

        if (stream) {
            options.putHeader("Accept", "text/event-stream");
        }

        return httpClient.request(options)
            .compose(httpClientRequest -> sendJsonBody(httpClientRequest, requestBody));
    }

    private Future<HttpClientResponse> sendJsonBody(HttpClientRequest httpClientRequest, JsonObject requestBody) {
        return httpClientRequest.send(Buffer.buffer(requestBody.encode()));
    }

    private Future<Buffer> requireSuccessAndReadBody(HttpClientResponse response, String serviceName) {
        if (response.statusCode() == 200) {
            return response.body();
        }
        return response.body().compose(body -> Future.failedFuture(
            new RuntimeException(serviceName + ": " + response.statusCode() + " - " + body)
        ));
    }

    private Future<Void> consumeResponsesSseStream(
        HttpClientResponse response,
        OpenAIResponsesStreamHandler streamHandler,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        if (response.statusCode() != 200) {
            return requireSuccessAndReadBody(response, "OpenAI Responses API error").mapEmpty();
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

        public OpenAIResponsesClient build() {
            if (httpClient == null) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new OpenAIResponsesClient(httpClient, apiKey, baseUrl);
        }
    }
}
