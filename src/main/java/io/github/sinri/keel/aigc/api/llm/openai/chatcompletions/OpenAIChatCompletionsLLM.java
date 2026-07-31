package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.internal.SSE2Chunk;
import io.github.sinri.keel.aigc.api.llm.catholic.AuthMethod;
import io.github.sinri.keel.aigc.api.internal.openai.OpenAiVertxSupport;
import io.github.sinri.keel.aigc.api.internal.openai.chatcompletions.OpenAIChatCompletionsRequestConverter;
import io.github.sinri.keel.aigc.api.internal.openai.chatcompletions.OpenAIChatCompletionsResponseConverter;
import io.github.sinri.keel.aigc.api.internal.openai.chatcompletions.OpenAIChatCompletionsStreamHandler;
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
 * OpenAI Chat Completions API 客户端，实现 CatholicLLM 接口。
 *
 * @see <a href="https://developers.openai.com/api/reference/resources/chat">OpenAI Chat API Reference</a>
 * @see <a href="https://help.aliyun.com/zh/model-studio/qwen-api-via-openai-chat-completions">
 *     Alibaba Cloud Model Studio: OpenAI-compatible Chat API</a>
 * @see <a href="https://api.volcengine.com/api-docs/view?action=ChatCompletions&amp;serviceCode=ark&amp;version=2024-01-01">
 *     Volcengine Ark ChatCompletions API</a>
 */
public class OpenAIChatCompletionsLLM implements CatholicLLM {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final AuthMethod authMethod;
    private final Keel keel;
    private final CatholicLLMObserver observer;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final AuthMethod DEFAULT_AUTH_METHOD = AuthMethod.Bearer;
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private static final String CHAT_API_ERROR = "OpenAI API error";

    @Deprecated
    public OpenAIChatCompletionsLLM(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_BASE_URL, DEFAULT_AUTH_METHOD);
    }

    @Deprecated
    public OpenAIChatCompletionsLLM(HttpClient httpClient, String apiKey, String baseUrl) {
        this(httpClient, apiKey, baseUrl, DEFAULT_AUTH_METHOD);
    }

    @Deprecated
    public OpenAIChatCompletionsLLM(HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod) {
        this(Keel.shared(), httpClient, apiKey, baseUrl, authMethod);
    }

    public OpenAIChatCompletionsLLM(Keel keel, HttpClient httpClient, String apiKey) {
        this(keel, httpClient, apiKey, DEFAULT_BASE_URL, DEFAULT_AUTH_METHOD);
    }

    public OpenAIChatCompletionsLLM(Keel keel, HttpClient httpClient, String apiKey, String baseUrl) {
        this(keel, httpClient, apiKey, baseUrl, DEFAULT_AUTH_METHOD);
    }

    public OpenAIChatCompletionsLLM(
        Keel keel, HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod
    ) {
        this(keel, httpClient, apiKey, baseUrl, authMethod, CatholicLLMObserver.noop());
    }

    public OpenAIChatCompletionsLLM(
        Keel keel, HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod,
        CatholicLLMObserver observer
    ) {
        this.keel = keel;
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.authMethod = authMethod;
        this.observer = CatholicLLMObservationSupport.orNoop(observer);
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", false);
        var exchange = observeRequest(openaiRequest, false);

        return sendJsonPost(openaiRequest, false)
            .compose(response -> SSE2Chunk.requireSuccessAndReadBody(response, CHAT_API_ERROR, observer, exchange))
            .map(body -> new OpenAIChatCompletionsResponseConverter().convert(body.toJsonObject()))
            .andThen(ar -> observeFailure(exchange, ar.cause()));
    }

    @Override
    public Future<Void> callStream(
        CatholicLLMRequest request,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", true);
        var exchange = observeRequest(openaiRequest, true);

        OpenAIChatCompletionsStreamHandler streamHandler = new OpenAIChatCompletionsStreamHandler();

        return sendJsonPost(openaiRequest, true)
            .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(
                keel, response, CHAT_API_ERROR, streamHandler::processSseLine, chunkAsyncProcessor,
                observer, exchange
            ))
            .andThen(ar -> observeFailure(exchange, ar.cause()));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", true);
        var exchange = observeRequest(openaiRequest, true);

        OpenAIChatCompletionsStreamHandler streamHandler = new OpenAIChatCompletionsStreamHandler();

        Future<Void> streamFuture = sendJsonPost(openaiRequest, true)
            .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(
                keel, response, CHAT_API_ERROR, streamHandler::processSseLine,
                chunk -> Future.succeededFuture(), observer, exchange
            ))
            .andThen(ar -> observeFailure(exchange, ar.cause()));
        return SSE2Chunk.buildResponseOnSuccess(streamFuture, streamHandler::buildFinalResponse);
    }

    private CatholicLLMObservationSupport.Exchange observeRequest(JsonObject body, boolean stream) {
        String endpoint = baseUrl + CHAT_COMPLETIONS_PATH;
        var exchange = CatholicLLMObservationSupport.exchange("openai-chat-completions", endpoint, stream);
        CatholicLLMObservationSupport.request(observer, exchange, requestHeaders(stream), body.encode());
        return exchange;
    }

    private Map<String, String> requestHeaders(boolean stream) {
        String authHeader = authMethod == AuthMethod.Bearer ? "Authorization" : "api-key";
        String authValue = authMethod == AuthMethod.Bearer ? "Bearer " + apiKey : apiKey;
        var headers = new java.util.LinkedHashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put(authHeader, authValue);
        if (stream) headers.put("Accept", "text/event-stream");
        return Map.copyOf(headers);
    }

    private void observeFailure(CatholicLLMObservationSupport.Exchange exchange, Throwable cause) {
        if (cause != null) CatholicLLMObservationSupport.failure(
            observer, exchange, CatholicLLMObservationStage.HTTP_RESPONSE, cause
        );
    }

    private Future<HttpClientResponse> sendJsonPost(JsonObject requestBody, boolean stream) {
        return OpenAiVertxSupport.sendJsonPost(
            httpClient,
            baseUrl + CHAT_COMPLETIONS_PATH,
            apiKey,
            authMethod,
            requestBody,
            stream
        );
    }

    // === Builder ===

    /**
     * 创建 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder 类
     */
    public static class Builder {
        private final LateObject<HttpClient> lateHttpClient = new LateObject<>();
        private final LateObject<String> lateApiKey = new LateObject<>();
        private final LateObject<Keel> lateKeel = new LateObject<>();
        private String baseUrl = DEFAULT_BASE_URL;
        private AuthMethod authMethod = DEFAULT_AUTH_METHOD;
        private CatholicLLMObserver observer = CatholicLLMObserver.noop();

        public Builder httpClient(HttpClient httpClient) {
            this.lateHttpClient.set(httpClient);
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.lateApiKey.set(apiKey);
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

        public Builder authMethod(AuthMethod authMethod) {
            this.authMethod = authMethod;
            return this;
        }

        public Builder observer(CatholicLLMObserver observer) {
            this.observer = observer;
            return this;
        }

        public OpenAIChatCompletionsLLM build() {
            if (!lateKeel.isInitialized()) {
                throw new IllegalArgumentException("keel is required");
            }
            if (!lateHttpClient.isInitialized()) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (!lateApiKey.isInitialized() || lateApiKey.get().isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new OpenAIChatCompletionsLLM(
                lateKeel.get(), lateHttpClient.get(), lateApiKey.get(), baseUrl, authMethod, observer
            );
        }
    }

    public JsonObject convert(CatholicLLMRequest request) {
        return new OpenAIChatCompletionsRequestConverter().convert(request);
    }
}
