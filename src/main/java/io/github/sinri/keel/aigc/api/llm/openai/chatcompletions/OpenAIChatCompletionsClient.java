package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.internal.openai.OpenAiVertxSupport;
import io.github.sinri.keel.aigc.api.internal.openai.chatcompletions.OpenAIChatCompletionsRequestConverter;
import io.github.sinri.keel.aigc.api.internal.openai.chatcompletions.OpenAIChatCompletionsResponseConverter;
import io.github.sinri.keel.aigc.api.internal.openai.chatcompletions.OpenAIChatCompletionsStreamHandler;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * OpenAI Chat Completions API 客户端，实现 CatholicLLM 接口。
 */
public class OpenAIChatCompletionsClient implements CatholicLLM {

    private final HttpClient httpClient;
    private final String apiKey;
    private final String baseUrl;

    // 默认 OpenAI API 端点
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    private static final String CHAT_API_ERROR = "OpenAI API error";

    /**
     * 创建客户端
     *
     * @param httpClient Vert.x HttpClient
     * @param apiKey    OpenAI API Key
     */
    public OpenAIChatCompletionsClient(HttpClient httpClient, String apiKey) {
        this(httpClient, apiKey, DEFAULT_BASE_URL);
    }

    /**
     * 创建客户端（支持自定义 baseUrl）
     *
     * @param httpClient Vert.x HttpClient
     * @param apiKey    OpenAI API Key
     * @param baseUrl   API 基础 URL（支持 OpenAI 兼容的第三方服务）
     */
    public OpenAIChatCompletionsClient(HttpClient httpClient, String apiKey, String baseUrl) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", false);

        return sendJsonPost(openaiRequest, false)
            .compose(response -> OpenAiVertxSupport.requireSuccessAndReadBody(response, CHAT_API_ERROR))
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
            .compose(response -> OpenAiVertxSupport.consumeOpenAiStyleSseStream(
                response,
                CHAT_API_ERROR,
                streamHandler::processSseLine,
                chunkAsyncProcessor
            ));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", true);

        OpenAIChatCompletionsStreamHandler streamHandler = new OpenAIChatCompletionsStreamHandler();

        return sendJsonPost(openaiRequest, true)
            .compose(response -> OpenAiVertxSupport.consumeOpenAiStyleSseStream(
                response,
                CHAT_API_ERROR,
                streamHandler::processSseLine,
                chunk -> Future.succeededFuture()
            ))
            .map(v -> streamHandler.buildFinalResponse());
    }

    private Future<HttpClientResponse> sendJsonPost(JsonObject requestBody, boolean stream) {
        return OpenAiVertxSupport.sendJsonPost(
            httpClient,
            baseUrl + CHAT_COMPLETIONS_PATH,
            apiKey,
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

        public OpenAIChatCompletionsClient build() {
            if (httpClient == null) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new OpenAIChatCompletionsClient(httpClient, apiKey, baseUrl);
        }
    }

    public JsonObject convert(CatholicLLMRequest request) {
        return new OpenAIChatCompletionsRequestConverter().convert(request);
    }
}
