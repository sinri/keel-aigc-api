package io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.internal.SSE2Chunk;
import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeClientHelper;
import io.github.sinri.keel.aigc.api.internal.dashscope.DashScopeStreamHandler;
import io.github.sinri.keel.aigc.api.internal.dashscope.multimodalgeneration.DashScopeMultimodalRequestConverter;
import io.github.sinri.keel.aigc.api.internal.dashscope.multimodalgeneration.DashScopeMultimodalResponseConverter;
import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * DashScope 图&文→文 API 客户端，对应端点 /services/aigc/multimodal-generation/generation。
 * 适用于多模态模型（如 qwen3-vl-plus, qwen-vl-plus, qwen-audio, qvq）。
 * <p>
 * call() 方法返回 DashScopeMultimodalResponse，在 CatholicLLMResponse 基础上
 * 提供多模态特有的字段（reasoning_content, image_hw 等）。
 */
public class DashScopeMultimodalGenerationLLM implements CatholicLLM {

    private static final String MULTIMODAL_GENERATION_PATH = "/services/aigc/multimodal-generation/generation";
    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/api/v1";

    private final DashScopeClientHelper helper;

    public DashScopeMultimodalGenerationLLM(DashScopeClientHelper helper) {
        this.helper = helper;
    }

    /**
     * 非流式调用，返回 DashScopeMultimodalResponse（包含多模态特有字段）
     */
    public Future<DashScopeMultimodalResponse> callMultimodal(CatholicLLMRequest request) {
        JsonObject dashscopeRequest = new DashScopeMultimodalRequestConverter().convert(request);
        dashscopeRequest.getJsonObject("parameters").put("stream", false);

        return helper.sendJsonPost(dashscopeRequest, MULTIMODAL_GENERATION_PATH, false)
            .compose(response -> SSE2Chunk.requireSuccessAndReadBody(response, "DashScope Multimodal Generation API error"))
            .map(body -> new DashScopeMultimodalResponseConverter().convert(body.toJsonObject()));
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        return callMultimodal(request).map(DashScopeMultimodalResponse::catholicResponse);
    }

    @Override
    public Future<Void> callStream(
        CatholicLLMRequest request,
        Function<CatholicLLMResponseChunk, Future<Void>> chunkAsyncProcessor
    ) {
        JsonObject dashscopeRequest = new DashScopeMultimodalRequestConverter().convert(request);
        JsonObject parameters = dashscopeRequest.getJsonObject("parameters");
        parameters.put("stream", true);
        parameters.put("incremental_output", true);

        DashScopeStreamHandler streamHandler = new DashScopeStreamHandler();

        return helper.sendJsonPost(dashscopeRequest, MULTIMODAL_GENERATION_PATH, true)
            .compose(response -> SSE2Chunk.processDashScopeSSEStream(
                helper.getKeel(), response, "DashScope Multimodal Generation API error", streamHandler, chunkAsyncProcessor
            ));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject dashscopeRequest = new DashScopeMultimodalRequestConverter().convert(request);
        JsonObject parameters = dashscopeRequest.getJsonObject("parameters");
        parameters.put("stream", true);
        parameters.put("incremental_output", true);

        DashScopeStreamHandler streamHandler = new DashScopeStreamHandler();

        return helper.sendJsonPost(dashscopeRequest, MULTIMODAL_GENERATION_PATH, true)
            .compose(response -> SSE2Chunk.processDashScopeSSEStream(
                helper.getKeel(), response, "DashScope Multimodal Generation API error", streamHandler,
                chunk -> Future.succeededFuture()
            ))
            .map(v -> streamHandler.buildFinalResponse())
            .otherwise(err -> streamHandler.buildFinalResponse());
    }

    // === Builder ===

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final LateObject<HttpClient> lateHttpClient = new LateObject<>();
        private final LateObject<String> lateApiKey = new LateObject<>();
        private String baseUrl = DEFAULT_BASE_URL;

        public Builder httpClient(HttpClient httpClient) {
            this.lateHttpClient.set(httpClient);
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.lateApiKey.set(apiKey);
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public DashScopeMultimodalGenerationLLM build() {
            if (!lateHttpClient.isInitialized()) {
                throw new IllegalArgumentException("httpClient is required");
            }
            if (!lateApiKey.isInitialized() || lateApiKey.get().isEmpty()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            DashScopeClientHelper helper = DashScopeClientHelper.builder()
                .httpClient(lateHttpClient.get())
                .apiKey(lateApiKey.get())
                .baseUrl(baseUrl)
                .build();
            return new DashScopeMultimodalGenerationLLM(helper);
        }
    }
}
