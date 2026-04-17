package io.github.sinri.keel.aigc.api.llm.dashscope.textgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLM;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.dashscope.DashScopeClientHelper;
import io.github.sinri.keel.aigc.api.llm.dashscope.DashScopeRequestConverter;
import io.github.sinri.keel.aigc.api.llm.dashscope.DashScopeResponseConverter;
import io.github.sinri.keel.aigc.api.llm.dashscope.DashScopeStreamHandler;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

/**
 * DashScope 文→文 API 客户端，对应端点 /services/aigc/text-generation/generation。
 * 适用于纯文本模型（如 qwen-plus, qwen-turbo, qwen-max）。
 */
public class DashScopeTextGenerationClient implements CatholicLLM {

    private static final String TEXT_GENERATION_PATH = "/services/aigc/text-generation/generation";
    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/api/v1";

    private final DashScopeClientHelper helper;

    public DashScopeTextGenerationClient(DashScopeClientHelper helper) {
        this.helper = helper;
    }

    @Override
    public Future<CatholicLLMResponse> call(CatholicLLMRequest request) {
        JsonObject dashscopeRequest = new DashScopeRequestConverter().convert(request);
        dashscopeRequest.getJsonObject("parameters").put("stream", false);

        return helper.sendJsonPost(dashscopeRequest, TEXT_GENERATION_PATH, false)
            .compose(response -> helper.requireSuccessAndReadBody(response, "DashScope Text Generation API error"))
            .map(body -> new DashScopeResponseConverter().convert(body.toJsonObject()));
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

        DashScopeStreamHandler streamHandler = new DashScopeStreamHandler();

        return helper.sendJsonPost(dashscopeRequest, TEXT_GENERATION_PATH, true)
            .compose(response -> helper.consumeDashScopeStream(response, streamHandler, chunkAsyncProcessor));
    }

    @Override
    public Future<CatholicLLMResponse> callStream(CatholicLLMRequest request) {
        JsonObject dashscopeRequest = new DashScopeRequestConverter().convert(request);
        JsonObject parameters = dashscopeRequest.getJsonObject("parameters");
        parameters.put("stream", true);
        parameters.put("incremental_output", true);

        DashScopeStreamHandler streamHandler = new DashScopeStreamHandler();

        return helper.sendJsonPost(dashscopeRequest, TEXT_GENERATION_PATH, true)
            .compose(response -> helper.consumeDashScopeStream(response, streamHandler, chunk -> Future.succeededFuture()))
            .map(v -> streamHandler.buildFinalResponse());
    }

    // === Builder ===

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

        public DashScopeTextGenerationClient build() {
            DashScopeClientHelper helper = DashScopeClientHelper.builder()
                .httpClient(httpClient)
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
            return new DashScopeTextGenerationClient(helper);
        }
    }
}