package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.function.Function;

/**
 * OpenAI Chat Completions API 客户端，实现 CatholicLLM 接口。
 */
public class OpenAIChatCompletionsClient implements CatholicLLM {

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;

    // 默认 OpenAI API 端点
    private static final String DEFAULT_BASE_URL = "https://api.openai.com/v1";
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

    /**
     * 创建客户端
     *
     * @param webClient Vert.x WebClient
     * @param apiKey    OpenAI API Key
     */
    public OpenAIChatCompletionsClient(WebClient webClient, String apiKey) {
        this(webClient, apiKey, DEFAULT_BASE_URL);
    }

    /**
     * 创建客户端（支持自定义 baseUrl）
     *
     * @param webClient Vert.x WebClient
     * @param apiKey    OpenAI API Key
     * @param baseUrl   API 基础 URL（支持 OpenAI 兼容的第三方服务）
     */
    public OpenAIChatCompletionsClient(WebClient webClient, String apiKey, String baseUrl) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        // 强制设置 stream = false
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", false);

        return webClient.postAbs(baseUrl + CHAT_COMPLETIONS_PATH)
            .putHeader("Content-Type", "application/json")
            .putHeader("Authorization", "Bearer " + apiKey)
            .sendJsonObject(openaiRequest)
            .map(response -> {
                if (response.statusCode() != 200) {
                    throw new RuntimeException("OpenAI API error: " + response.statusCode() + " - " + response.bodyAsString());
                }
                JsonObject openaiResponse = response.bodyAsJsonObject();
                return new OpenAIChatCompletionsResponseConverter().convert(openaiResponse);
            });
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

        // 使用 HttpClient 进行 SSE 流式请求
        return webClient.postAbs(baseUrl + CHAT_COMPLETIONS_PATH)
            .putHeader("Content-Type", "application/json")
            .putHeader("Authorization", "Bearer " + apiKey)
            .putHeader("Accept", "text/event-stream")
            .sendJsonObject(openaiRequest)
            .compose(response -> {
                // 处理 SSE 流
                if (response.statusCode() != 200) {
                    return Future.failedFuture(
                        new RuntimeException("OpenAI API error: " + response.statusCode() + " - " + response.bodyAsString())
                    );
                }

                // 解析 SSE 数据
                Buffer body = response.body();
                String sseContent = body.toString();
                String[] lines = sseContent.split("\n");

                // 处理每一行 SSE 数据
                Future<Void> processFuture = Future.succeededFuture();
                for (String line : lines) {
                    if (line.isEmpty() || line.startsWith(":")) {
                        // 空行或注释行跳过
                        continue;
                    }

                    CatholicLLMResponseChunk chunk = streamHandler.processSseLine(line);
                    if (chunk != null) {
                        processFuture = processFuture.compose(v -> chunkAsyncProcessor.apply(chunk));
                    }
                }

                return processFuture;
            });
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject openaiRequest = new OpenAIChatCompletionsRequestConverter()
            .convert(request);
        openaiRequest.put("stream", true);

        OpenAIChatCompletionsStreamHandler streamHandler = new OpenAIChatCompletionsStreamHandler();

        return webClient.postAbs(baseUrl + CHAT_COMPLETIONS_PATH)
            .putHeader("Content-Type", "application/json")
            .putHeader("Authorization", "Bearer " + apiKey)
            .putHeader("Accept", "text/event-stream")
            .sendJsonObject(openaiRequest)
            .map(response -> {
                if (response.statusCode() != 200) {
                    throw new RuntimeException("OpenAI API error: " + response.statusCode() + " - " + response.bodyAsString());
                }

                // 解析 SSE 数据并收集
                Buffer body = response.body();
                String sseContent = body.toString();
                String[] lines = sseContent.split("\n");

                for (String line : lines) {
                    if (line.isEmpty() || line.startsWith(":")) {
                        continue;
                    }
                    streamHandler.processSseLine(line);
                }

                return streamHandler.buildFinalResponse();
            });
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
        private WebClient webClient;
        private String apiKey;
        private String baseUrl = DEFAULT_BASE_URL;

        public Builder webClient(WebClient webClient) {
            this.webClient = webClient;
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
            if (webClient == null) {
                throw new IllegalArgumentException("webClient is required");
            }
            if (apiKey == null || apiKey.isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            return new OpenAIChatCompletionsClient(webClient, apiKey, baseUrl);
        }
    }
}