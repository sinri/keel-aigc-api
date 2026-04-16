package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;

import java.util.Objects;

public class OpenAIChatCompletionsClientChatTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        WebClient webClient = WebClient.create(getKeel());

        String baseUrl = ConfigElement.root().readProperty("openai.test1.api");
        String apiKey = ConfigElement.root().readProperty("openai.test1.key");
        String model = ConfigElement.root().readProperty("openai.test1.model");

        Objects.requireNonNull(apiKey, "OpenAI API key must be set");
        Objects.requireNonNull(model, "OpenAI model must be set");
        Objects.requireNonNull(baseUrl, "Base URL must be set");

        OpenAIChatCompletionsClient client = new OpenAIChatCompletionsClient(
                webClient,
                apiKey,
                baseUrl
        );

        // 构建请求：让 LLM 生成下个月从中国到日本的旅行计划
        CatholicLLMRequest request = CatholicLLMRequest.builder()
                .model(model)
                .addMessage(CatholicSystemMessage.of("You are a helpful travel assistant. Please provide detailed and practical travel plans in Chinese."))
                .addMessage(CatholicUserMessage.ofText("请帮我制定一个下个月从中国到日本的旅行计划，包括：\n" +
                        "1. 推荐的目的地城市和景点\n" +
                        "2. 大致的行程安排（7天左右）\n" +
                        "3. 预估的费用预算\n" +
                        "4. 需要准备的物品和注意事项"))
                .build();

        // 调用 LLM 并获取响应
        return client.call(request)
                .compose(response -> {
                    // 打印响应结果
                    getLogger().info("LLM Response ID: " + response.id());

                    if (response.hasText()) {
                        String travelPlan = response.text();
                        getLogger().info("Generated Travel Plan:\n" + travelPlan);
                    } else if (response.hasToolCalls()) {
                        getLogger().info("LLM requested tool calls: " + response.message().toolCalls().size());
                        response.message().toolCalls().forEach(toolCall -> {
                            getLogger().info("Tool: " + toolCall.functionName() + ", Args: " + toolCall.function().arguments());
                        });
                    }

                    // 打印 token 使用情况
                    var usage = response.usage();
                    if (usage.promptTokens() != null) {
                        getLogger().info("Token Usage: prompt=" + usage.promptTokens() +
                                ", completion=" + usage.completionTokens() +
                                ", total=" + usage.totalTokens());
                    }

                    return Future.succeededFuture();
                });
    }
}
