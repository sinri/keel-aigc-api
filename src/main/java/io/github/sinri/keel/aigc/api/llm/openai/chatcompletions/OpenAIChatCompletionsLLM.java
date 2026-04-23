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
import io.github.sinri.keel.base.async.Keel;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * OpenAI Chat Completions API 客户端，实现 CatholicLLM 接口。
 */
public class OpenAIChatCompletionsLLM implements CatholicLLM {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;
    private final AuthMethod authMethod;
    private final Keel keel;

    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final AuthMethod DEFAULT_AUTH_METHOD = AuthMethod.Bearer;
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private static final String CHAT_API_ERROR = "OpenAI API error";

    public OpenAIChatCompletionsLLM(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_BASE_URL, DEFAULT_AUTH_METHOD);
    }

    public OpenAIChatCompletionsLLM(HttpClient httpClient, String apiKey, String baseUrl) {
        this(httpClient, apiKey, baseUrl, DEFAULT_AUTH_METHOD);
    }

    public OpenAIChatCompletionsLLM(HttpClient httpClient, String apiKey, String baseUrl, AuthMethod authMethod) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.authMethod = authMethod;
        this.keel = Keel.shared();
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", false);

        return sendJsonPost(openaiRequest, false)
            .compose(response -> SSE2Chunk.requireSuccessAndReadBody(response, CHAT_API_ERROR))
            .map(body -> new OpenAIChatCompletionsResponseConverter().convert(body.toJsonObject()));
    }

    @Override
    public Future<Void> callStream(
        CatholicLLMRequest request,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", true);

        OpenAIChatCompletionsStreamHandler streamHandler = new OpenAIChatCompletionsStreamHandler();

        return sendJsonPost(openaiRequest, true)
            .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(
                keel, response, CHAT_API_ERROR, streamHandler::processSseLine, chunkAsyncProcessor
            ));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", true);

        OpenAIChatCompletionsStreamHandler streamHandler = new OpenAIChatCompletionsStreamHandler();

        return sendJsonPost(openaiRequest, true)
            .compose(response -> SSE2Chunk.processOpenAiStyleSSEStream(
                keel, response, CHAT_API_ERROR, streamHandler::processSseLine,
                chunk -> Future.succeededFuture()
            ))
            .map(v -> streamHandler.buildFinalResponse())
            .otherwise(err -> streamHandler.buildFinalResponse());
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
        private HttpClient httpClient;
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;
        private AuthMethod authMethod = DEFAULT_AUTH_METHOD;

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

        public Builder authMethod(AuthMethod authMethod) {
            this.authMethod = authMethod;
            return this;
        }

        public OpenAIChatCompletionsLLM build() {
            if (httpClient == null) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new OpenAIChatCompletionsLLM(httpClient, apiKey, baseUrl, authMethod);
        }
    }

    public JsonObject convert(CatholicLLMRequest request) {
        return new OpenAIChatCompletionsRequestConverter().convert(request);
    }
}
