package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Anthropic Messages API 客户端，实现 {@link CatholicLLM}。
 */
public class AnthropicClient implements CatholicLLM {

    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com/v1";
    private static final String DEFAULT_ANTHROPIC_VERSION = "2023-06-01";
    private static final String MESSAGES_PATH = "/messages";
    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final String anthropicVersion;

    public AnthropicClient(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_BASE_URL, DEFAULT_ANTHROPIC_VERSION);
    }

    public AnthropicClient(HttpClient httpClient, String apiKey, String baseUrl) {
        this(httpClient, apiKey, baseUrl, DEFAULT_ANTHROPIC_VERSION);
    }

    public AnthropicClient(HttpClient httpClient, String apiKey, String baseUrl, String anthropicVersion) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.anthropicVersion = anthropicVersion;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        JsonObject body = new AnthropicRequestConverter().convert(request);
        body.put("stream", false);

        return sendJsonPost(body, false)
                .compose(response -> requireSuccessAndReadBody(response, "Anthropic API error"))
                .map(buf -> new AnthropicResponseConverter().convert(buf.toJsonObject()));
    }

    @Override
    public Future<Void> callStream(
            CatholicLLMRequest request,
            Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        JsonObject body = new AnthropicRequestConverter().convert(request);
        body.put("stream", true);

        AnthropicStreamHandler streamHandler = new AnthropicStreamHandler();

        return sendJsonPost(body, true)
                .compose(response -> consumeAnthropicSse(response, streamHandler, chunkAsyncProcessor));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject body = new AnthropicRequestConverter().convert(request);
        body.put("stream", true);

        AnthropicStreamHandler streamHandler = new AnthropicStreamHandler();

        return sendJsonPost(body, true)
                .compose(response -> consumeAnthropicSse(response, streamHandler, chunk -> Future.succeededFuture()))
                .map(v -> streamHandler.buildFinalResponse());
    }

    private Future<HttpClientResponse> sendJsonPost(JsonObject requestBody, boolean stream) {
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

    private Future<Buffer> requireSuccessAndReadBody(HttpClientResponse response, String serviceName) {
        if (response.statusCode() == 200) {
            return response.body();
        }
        return response.body().compose(body -> Future.failedFuture(
                new RuntimeException(serviceName + ": " + response.statusCode() + " - " + body)
        ));
    }

    private Future<Void> consumeAnthropicSse(
            HttpClientResponse response,
            AnthropicStreamHandler streamHandler,
            Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        if (response.statusCode() != 200) {
            return requireSuccessAndReadBody(response, "Anthropic API error").mapEmpty();
        }

        Promise<Void> promise = Promise.promise();
        AtomicReference<Future<Void>> chain = new AtomicReference<>(Future.succeededFuture());
        StringBuilder pendingLine = new StringBuilder();

        response.exceptionHandler(promise::tryFail);
        response.handler(buffer -> {
            pendingLine.append(buffer);
            drainLines(pendingLine, line -> {
                CatholicLLMResponseChunk chunk = streamHandler.processSseLine(line);
                enqueueChunk(chain, chunk, chunkAsyncProcessor, promise);
            });
        });
        response.endHandler(v -> {
            if (!promise.future().isComplete() && pendingLine.length() > 0) {
                CatholicLLMResponseChunk chunk = streamHandler.processSseLine(normalizeLine(pendingLine.toString()));
                enqueueChunk(chain, chunk, chunkAsyncProcessor, promise);
            }
            chain.get().onComplete(ar -> {
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
        int nl;
        while ((nl = pendingLine.indexOf("\n")) >= 0) {
            String line = pendingLine.substring(0, nl);
            pendingLine.delete(0, nl + 1);
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
            AtomicReference<Future<Void>> chain,
            CatholicLLMResponseChunk chunk,
            Function<CatholicLLMResponseChunk, Future<Void>> processor,
            Promise<Void> promise
    ) {
        if (chunk == null || promise.future().isComplete()) {
            return;
        }
        Future<Void> next = chain.get().compose(v -> processor.apply(chunk));
        next.onFailure(promise::tryFail);
        chain.set(next);
    }

    public static class Builder {
        private HttpClient httpClient;
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private String anthropicVersion = DEFAULT_ANTHROPIC_VERSION;

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

        public Builder anthropicVersion(String anthropicVersion) {
            this.anthropicVersion = anthropicVersion;
            return this;
        }

        public AnthropicClient build() {
            if (httpClient == null) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new AnthropicClient(httpClient, apiKey, baseUrl, anthropicVersion);
        }
    }
}
