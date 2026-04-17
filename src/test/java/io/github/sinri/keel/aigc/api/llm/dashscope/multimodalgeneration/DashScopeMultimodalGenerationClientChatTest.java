package io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;

import java.util.Objects;

public class DashScopeMultimodalGenerationClientChatTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        HttpClient httpClient = getKeel().createHttpClient();

        String baseUrl = ConfigElement.root().readProperty("dashscope.test2.api");
        String apiKey = ConfigElement.root().readProperty("dashscope.test2.key");
        String model = ConfigElement.root().readProperty("dashscope.test2.model");

        Objects.requireNonNull(apiKey, "DashScope API key must be set");
        Objects.requireNonNull(model, "DashScope model must be set");

        if (baseUrl == null) {
            baseUrl = "https://dashscope.aliyuncs.com/api/v1";
        }

        DashScopeMultimodalGenerationClient client = DashScopeMultimodalGenerationClient.builder()
                .httpClient(httpClient)
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        CatholicLLMRequest request = CatholicLLMRequest.builder()
                .model(model)
                .addMessage(CatholicSystemMessage.of("You are a helpful travel assistant. Please provide detailed and practical travel plans in Chinese."))
                .addMessage(CatholicUserMessage.ofText("请帮我制定一个下个月从中国到日本的旅行计划，包括：\n" +
                        "1. 推荐的目的地城市和景点\n" +
                        "2. 大致的行程安排（7天左右）\n" +
                        "3. 预估的费用预算\n" +
                        "4. 需要准备的物品和注意事项"))
                .build();

        getLogger().info("Sending request to DashScope Multimodal Generation API...");

        return client.callMultimodal(request)
                .compose(multimodalResponse -> {
                    var catholicResponse = multimodalResponse.catholicResponse();

                    getLogger().info("Response ID: " + catholicResponse.id());

                    if (catholicResponse.hasText()) {
                        getLogger().info("Generated Travel Plan:\n" + catholicResponse.text());
                    } else if (catholicResponse.hasToolCalls()) {
                        getLogger().info("Tool calls requested: " + catholicResponse.message().toolCalls().size());
                    }

                    // 多模态特有字段
                    if (multimodalResponse.reasoningContent() != null) {
                        getLogger().info("Reasoning Content:\n" + multimodalResponse.reasoningContent());
                    }
                    if (multimodalResponse.imageTokens() != null) {
                        getLogger().info("Image Tokens: " + multimodalResponse.imageTokens());
                    }

                    var usage = catholicResponse.usage();
                    if (usage.totalTokens() != null) {
                        getLogger().info("Token Usage: input=" + usage.promptTokens() +
                                ", output=" + usage.completionTokens() +
                                ", total=" + usage.totalTokens());
                    }

                    return Future.succeededFuture();
                });
    }
}