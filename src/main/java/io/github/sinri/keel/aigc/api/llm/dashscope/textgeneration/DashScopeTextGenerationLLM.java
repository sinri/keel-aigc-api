package io.github.sinri.keel.aigc.api.llm.dashscope.textgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.AuthMethod;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.internal.SSE2Chunk;
import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeRequestConverter;
import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeResponseConverter;
import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeStreamHandler;
import io.github.sinri.keel.aigc.api.llm.dashscope.AbstractDashScopeLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObserver;
import io.github.sinri.keel.aigc.api.llm.catholic.observation.CatholicLLMObservationStage;
import io.github.sinri.keel.aigc.api.internal.catholic.observation.CatholicLLMObservationSupport;
import io.github.sinri.keel.base.async.Keel;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * DashScope 文→文 API 客户端，对应端点 /services/aigc/text-generation/generation。
 * 适用于纯文本模型（如 qwen-plus, qwen-turbo, qwen-max）。
 */
public class DashScopeTextGenerationLLM extends AbstractDashScopeLLM {

    private static final String TEXT_GENERATION_PATH = "/services/aigc/text-generation/generation";

    @Deprecated
    public DashScopeTextGenerationLLM(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_BASE_URL);
    }

    @Deprecated
    public DashScopeTextGenerationLLM(HttpClient httpClient, String apiKey, String baseUrl) {
        this(httpClient, apiKey, baseUrl, DEFAULT_AUTH_METHOD);
    }

    @Deprecated
    public DashScopeTextGenerationLLM(HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod) {
        super(httpClient, apiKey, baseUrl, authMethod);
    }

    public DashScopeTextGenerationLLM(Keel keel, HttpClient httpClient, String apiKey) {
        this(keel, httpClient, apiKey, DEFAULT_BASE_URL, DEFAULT_AUTH_METHOD);
    }

    public DashScopeTextGenerationLLM(Keel keel, HttpClient httpClient, String apiKey, String baseUrl) {
        this(keel, httpClient, apiKey, baseUrl, DEFAULT_AUTH_METHOD);
    }

    public DashScopeTextGenerationLLM(
        Keel keel, HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod
    ) {
        super(keel, httpClient, apiKey, baseUrl, authMethod);
    }

    public DashScopeTextGenerationLLM(
        Keel keel, HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod,
        CatholicLLMObserver observer
    ) {
        super(keel, httpClient, apiKey, baseUrl, authMethod, observer);
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        JsonObject dashscopeRequest = new DashScopeRequestConverter().convert(request);
        dashscopeRequest.getJsonObject("parameters").put("stream", false);
        var exchange = observeRequest("dashscope-text-generation", TEXT_GENERATION_PATH, dashscopeRequest, false);

        return sendJsonPost(dashscopeRequest, TEXT_GENERATION_PATH, false)
            .compose(response -> SSE2Chunk.requireSuccessAndReadBody(
                response, "DashScope Text Generation API error", getObserver(), exchange
            ))
            .map(body -> new DashScopeResponseConverter().convert(body.toJsonObject()))
            .andThen(ar -> observeFailure(exchange, ar.cause()));
    }

    @Override
    public Future<Void> callStream(
        CatholicLLMRequest request,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        JsonObject dashscopeRequest = new DashScopeRequestConverter().convert(request);
        JsonObject parameters = dashscopeRequest.getJsonObject("parameters");
        parameters.put("stream", true);
        parameters.put("incremental_output", true);
        var exchange = observeRequest("dashscope-text-generation", TEXT_GENERATION_PATH, dashscopeRequest, true);

        DashScopeStreamHandler streamHandler = new DashScopeStreamHandler();

        return sendJsonPost(dashscopeRequest, TEXT_GENERATION_PATH, true)
            .compose(response -> SSE2Chunk.processDashScopeSSEStream(
                getKeel(), response, "DashScope Text Generation API error", streamHandler, chunkAsyncProcessor,
                getObserver(), exchange
            ))
            .andThen(ar -> observeFailure(exchange, ar.cause()));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject dashscopeRequest = new DashScopeRequestConverter().convert(request);
        JsonObject parameters = dashscopeRequest.getJsonObject("parameters");
        parameters.put("stream", true);
        parameters.put("incremental_output", true);
        var exchange = observeRequest("dashscope-text-generation", TEXT_GENERATION_PATH, dashscopeRequest, true);

        DashScopeStreamHandler streamHandler = new DashScopeStreamHandler();

        Future<Void> streamFuture = sendJsonPost(dashscopeRequest, TEXT_GENERATION_PATH, true)
            .compose(response -> SSE2Chunk.processDashScopeSSEStream(
                getKeel(), response, "DashScope Text Generation API error", streamHandler,
                chunk -> Future.succeededFuture(), getObserver(), exchange
            ))
            .andThen(ar -> observeFailure(exchange, ar.cause()));
        return SSE2Chunk.buildResponseOnSuccess(streamFuture, streamHandler::buildFinalResponse);
    }

    private void observeFailure(CatholicLLMObservationSupport.Exchange exchange, Throwable cause) {
        if (cause != null) CatholicLLMObservationSupport.failure(
            getObserver(), exchange, CatholicLLMObservationStage.HTTP_RESPONSE, cause
        );
    }

    // === Builder ===

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

        public DashScopeTextGenerationLLM build() {
            if (!lateKeel.isInitialized()) {
                throw new IllegalArgumentException("keel is required");
            }
            if (!lateHttpClient.isInitialized()) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (!lateApiKey.isInitialized() || lateApiKey.get().isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new DashScopeTextGenerationLLM(
                lateKeel.get(), lateHttpClient.get(), lateApiKey.get(), baseUrl, authMethod, observer
            );
        }
    }
}
