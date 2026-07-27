package io.github.sinri.keel.aigc.api.llm.openai.responses;

import io.github.sinri.keel.aigc.api.internal.SSE2Chunk;
import io.github.sinri.keel.aigc.api.llm.catholic.AuthMethod;
import io.github.sinri.keel.aigc.api.internal.openai.OpenAiVertxSupport;
import io.github.sinri.keel.aigc.api.internal.openai.responses.OpenAIResponsesRequestConverter;
import io.github.sinri.keel.aigc.api.internal.openai.responses.OpenAIResponsesResponseConverter;
import io.github.sinri.keel.aigc.api.internal.openai.responses.OpenAIResponsesStreamHandler;
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
 * OpenAI Responses API 客户端，实现 {@link CatholicLLM}。
 */
public class OpenAIResponsesLLM implements CatholicLLM {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final AuthMethod authMethod;
    private final Keel keel;
    private final CatholicLLMObserver observer;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final AuthMethod DEFAULT_AUTH_METHOD = AuthMethod.Bearer;
    private static final String RESPONSES_PATH = "/responses";

    private static final String RESPONSES_API_ERROR = "OpenAI Responses API error";

    @Deprecated
    public OpenAIResponsesLLM(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_BASE_URL, DEFAULT_AUTH_METHOD);
    }

    @Deprecated
    public OpenAIResponsesLLM(HttpClient httpClient, String apiKey, String baseUrl) {
        this(httpClient, apiKey, baseUrl, DEFAULT_AUTH_METHOD);
    }

    @Deprecated
    public OpenAIResponsesLLM(HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod) {
        this(Keel.shared(), httpClient, apiKey, baseUrl, authMethod);
    }

    public OpenAIResponsesLLM(Keel keel, HttpClient httpClient, String apiKey) {
        this(keel, httpClient, apiKey, DEFAULT_BASE_URL, DEFAULT_AUTH_METHOD);
    }

    public OpenAIResponsesLLM(Keel keel, HttpClient httpClient, String apiKey, String baseUrl) {
        this(keel, httpClient, apiKey, baseUrl, DEFAULT_AUTH_METHOD);
    }

    public OpenAIResponsesLLM(Keel keel, HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod) {
        this(keel, httpClient, apiKey, baseUrl, authMethod, CatholicLLMObserver.noop());
    }

    public OpenAIResponsesLLM(
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
        JsonObject responsesRequest = new OpenAIResponsesRequestConverter().convert(request);
        responsesRequest.put("stream", false);
        var exchange = observeRequest(responsesRequest, false);

        return sendJsonPost(responsesRequest, false)
            .compose(response -> SSE2Chunk.requireSuccessAndReadBody(response, RESPONSES_API_ERROR, observer, exchange))
            .map(body -> new OpenAIResponsesResponseConverter().convert(body.toJsonObject()))
            .andThen(ar -> observeFailure(exchange, ar.cause()));
    }

    @Override
    public Future<Void> callStream(
        CatholicLLMRequest request,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        JsonObject responsesRequest = new OpenAIResponsesRequestConverter().convert(request);
        responsesRequest.put("stream", true);
        var exchange = observeRequest(responsesRequest, true);

        OpenAIResponsesStreamHandler streamHandler = new OpenAIResponsesStreamHandler();

        return sendJsonPost(responsesRequest, true)
            .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(
                keel, response, RESPONSES_API_ERROR, streamHandler::processSseLine, chunkAsyncProcessor,
                observer, exchange
            ))
            .andThen(ar -> observeFailure(exchange, ar.cause()));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject responsesRequest = new OpenAIResponsesRequestConverter().convert(request);
        responsesRequest.put("stream", true);
        var exchange = observeRequest(responsesRequest, true);

        OpenAIResponsesStreamHandler streamHandler = new OpenAIResponsesStreamHandler();

        Future<Void> streamFuture = sendJsonPost(responsesRequest, true)
            .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(
                keel, response, RESPONSES_API_ERROR, streamHandler::processSseLine,
                chunk -> Future.succeededFuture(), observer, exchange
            ))
            .andThen(ar -> observeFailure(exchange, ar.cause()));
        return SSE2Chunk.buildResponseOnSuccess(streamFuture, streamHandler::buildFinalResponse);
    }

    private CatholicLLMObservationSupport.Exchange observeRequest(JsonObject body, boolean stream) {
        String endpoint = baseUrl + RESPONSES_PATH;
        var exchange = CatholicLLMObservationSupport.exchange("openai-responses", endpoint, stream);
        var headers = new java.util.LinkedHashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put(authMethod == AuthMethod.Bearer ? "Authorization" : "api-key",
            authMethod == AuthMethod.Bearer ? "Bearer " + apiKey : apiKey);
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
        return OpenAiVertxSupport.sendJsonPost(
            httpClient,
            baseUrl + RESPONSES_PATH,
            apiKey,
            authMethod,
            requestBody,
            stream
        );
    }

    public static Builder builder() {
        return new Builder();
    }

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

        public OpenAIResponsesLLM build() {
            if (!lateKeel.isInitialized()) {
                throw new IllegalArgumentException("keel is required");
            }
            if (!lateHttpClient.isInitialized()) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (!lateApiKey.isInitialized() || lateApiKey.get().isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new OpenAIResponsesLLM(
                lateKeel.get(), lateHttpClient.get(), lateApiKey.get(), baseUrl, authMethod, observer
            );
        }
    }
}
