package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.github.sinri.keel.aigc.api.internal.SSE2Chunk;
import io.github.sinri.keel.aigc.api.internal.anthropic.AnthropicRequestConverter;
import io.github.sinri.keel.aigc.api.internal.anthropic.AnthropicResponseConverter;
import io.github.sinri.keel.aigc.api.internal.anthropic.AnthropicStreamHandler;
import io.github.sinri.keel.aigc.api.internal.anthropic.AnthropicVertxSupport;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObserver;
import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObservationStage;
import io.github.sinri.keel.aigc.api.internal.catholic.observation.CatholicLLMObservationSupport;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;
import java.util.Map;

/**
 * Anthropic Messages API 客户端，实现 {@link CatholicLLM}。
 *
 * @see <a href="https://platform.claude.com/docs/en/api/messages">Anthropic Messages API</a>
 * @see <a href="https://platform.claude.com/docs/en/build-with-claude/streaming">
 *     Anthropic streaming Messages</a>
 */
public class AnthropicLLM implements CatholicLLM {

    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com/v1";
    private static final String DEFAULT_ANTHROPIC_VERSION = "2023-06-01";
    private static final String ANTHROPIC_API_ERROR = "Anthropic API error";

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final String anthropicVersion;
    private final Keel keel;
    private final CatholicLLMObserver observer;

    @Deprecated
    public AnthropicLLM(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_BASE_URL, DEFAULT_ANTHROPIC_VERSION);
    }

    @Deprecated
    public AnthropicLLM(HttpClient httpClient, String apiKey, String baseUrl) {
        this(httpClient, apiKey, baseUrl, DEFAULT_ANTHROPIC_VERSION);
    }

    @Deprecated
    public AnthropicLLM(HttpClient httpClient, String apiKey, String baseUrl, String anthropicVersion) {
        this(Keel.shared(), httpClient, apiKey, baseUrl, anthropicVersion);
    }

    public AnthropicLLM(Keel keel, HttpClient httpClient, String apiKey) {
        this(keel, httpClient, apiKey, DEFAULT_BASE_URL, DEFAULT_ANTHROPIC_VERSION);
    }

    public AnthropicLLM(Keel keel, HttpClient httpClient, String apiKey, String baseUrl) {
        this(keel, httpClient, apiKey, baseUrl, DEFAULT_ANTHROPIC_VERSION);
    }

    public AnthropicLLM(Keel keel, HttpClient httpClient, String apiKey, String baseUrl, String anthropicVersion) {
        this(keel, httpClient, apiKey, baseUrl, anthropicVersion, CatholicLLMObserver.noop());
    }

    public AnthropicLLM(
        Keel keel, HttpClient httpClient, String apiKey, String baseUrl, String anthropicVersion,
        CatholicLLMObserver observer
    ) {
        this.keel = keel;
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.anthropicVersion = anthropicVersion;
        this.observer = CatholicLLMObservationSupport.orNoop(observer);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        JsonObject body = new AnthropicRequestConverter().convert(request);
        body.put("stream", false);
        var exchange = observeRequest(body, false);

        return sendJsonPost(body, false)
                .compose(response -> SSE2Chunk.requireSuccessAndReadBody(response, ANTHROPIC_API_ERROR, observer, exchange))
                .map(buf -> new AnthropicResponseConverter().convert(buf.toJsonObject()))
                .andThen(ar -> observeFailure(exchange, ar.cause()));
    }

    @Override
    public Future<Void> callStream(
            CatholicLLMRequest request,
            Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        JsonObject body = new AnthropicRequestConverter().convert(request);
        body.put("stream", true);
        var exchange = observeRequest(body, true);

        AnthropicStreamHandler streamHandler = new AnthropicStreamHandler();

        return sendJsonPost(body, true)
                .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(
                        keel, response, ANTHROPIC_API_ERROR, streamHandler::processSseLine, chunkAsyncProcessor,
                        observer, exchange
                ))
                .andThen(ar -> observeFailure(exchange, ar.cause()));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject body = new AnthropicRequestConverter().convert(request);
        body.put("stream", true);
        var exchange = observeRequest(body, true);

        AnthropicStreamHandler streamHandler = new AnthropicStreamHandler();

        Future<Void> streamFuture = sendJsonPost(body, true)
                .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(
                        keel, response, ANTHROPIC_API_ERROR, streamHandler::processSseLine,
                        chunk -> Future.succeededFuture(), observer, exchange
                ))
                .andThen(ar -> observeFailure(exchange, ar.cause()));
        return SSE2Chunk.buildResponseOnSuccess(streamFuture, streamHandler::buildFinalResponse);
    }

    private CatholicLLMObservationSupport.Exchange observeRequest(JsonObject body, boolean stream) {
        String endpoint = baseUrl + "/messages";
        var exchange = CatholicLLMObservationSupport.exchange("anthropic-messages", endpoint, stream);
        var headers = new java.util.LinkedHashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("x-api-key", apiKey);
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("anthropic-version", anthropicVersion);
        if (stream) headers.put("Accept", "text/event-stream");
        CatholicLLMObservationSupport.request(observer, exchange, Map.copyOf(headers), body.encode());
        return exchange;
    }

    private void observeFailure(CatholicLLMObservationSupport.Exchange exchange, Throwable cause) {
        if (cause != null) CatholicLLMObservationSupport.failure(
            observer, exchange, CatholicLLMObservationStage.HTTP_RESPONSE, cause
        );
    }

    private Future<HttpClientResponse> sendJsonPost(JsonObject requestBody, boolean stream) {
        return AnthropicVertxSupport.sendMessagesPost(
                httpClient,
                baseUrl,
                apiKey,
                anthropicVersion,
                requestBody,
                stream
        );
    }

    public static class Builder {
        private final LateObject<HttpClient> lateHttpClient = new LateObject<>();
        private final LateObject<String> lateApiKey=new LateObject<>();
        private final LateObject<Keel> lateKeel = new LateObject<>();
        private String baseUrl = DEFAULT_BASE_URL;
        private String anthropicVersion = DEFAULT_ANTHROPIC_VERSION;
        private CatholicLLMObserver observer = CatholicLLMObserver.noop();

        public Builder httpClient(HttpClient httpClient) {
            this.lateHttpClient.set(httpClient);
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.lateApiKey.set( apiKey);
            return this;
        }

        public Builder keel(Keel keel) {
            this.lateKeel.set(keel);
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

        public Builder observer(CatholicLLMObserver observer) {
            this.observer = observer;
            return this;
        }

        public AnthropicLLM build() {
            if (!lateKeel.isInitialized()) {
                throw new IllegalArgumentException("keel is required");
            }
            if (!lateHttpClient.isInitialized()) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (!lateApiKey.isInitialized()|| lateApiKey.get().isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new AnthropicLLM(
                lateKeel.get(), lateHttpClient.get(), lateApiKey.get(), baseUrl, anthropicVersion, observer
            );
        }
    }
}
