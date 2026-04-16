package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponseChunk;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;

import java.util.Objects;

public class OpenAIChatCompletionsClientChatStreamChunkTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        HttpClient httpClient = getKeel().createHttpClient();

        String baseUrl = ConfigElement.root().readProperty("openai.test1.api");
        String apiKey = ConfigElement.root().readProperty("openai.test1.key");
        String model = ConfigElement.root().readProperty("openai.test1.model");

        Objects.requireNonNull(apiKey, "OpenAI API key must be set");
        Objects.requireNonNull(model, "OpenAI model must be set");
        Objects.requireNonNull(baseUrl, "Base URL must be set");

        OpenAIChatCompletionsClient client = new OpenAIChatCompletionsClient(
                httpClient,
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
                .enableStream() // 启用流式调用
                .build();

        getLogger().info("Starting stream request...");

        // 使用 callStream 方法，逐片段处理响应
        return client.callStream(request, chunk -> {
            // 处理每个响应片段
            if (chunk.id() != null) {
                getLogger().info("Chunk ID: " + chunk.id() + ", Index: " + chunk.index());
            }

            // 打印增量文本内容
            if (chunk.hasDeltaText()) {
                getLogger().info("Delta text: " + chunk.deltaText());
            }

            // 打印增量工具调用
            if (chunk.hasDeltaToolCalls()) {
                chunk.deltaToolCalls().forEach(tcDelta -> {
                    getLogger().info("Tool call delta: id=" + tcDelta.id() +
                            ", index=" + tcDelta.index() +
                            ", function=" + tcDelta.function().name() +
                            ", args=" + tcDelta.function().argumentsDelta());
                });
            }

            // 检查是否完成
            if (chunk.isFinished()) {
                getLogger().info("Stream finished!");

                // 打印最终的 token 使用情况
                var usage = chunk.usage();
                if (usage.promptTokens() != null) {
                    getLogger().info("Token Usage: prompt=" + usage.promptTokens() +
                            ", completion=" + usage.completionTokens() +
                            ", total=" + usage.totalTokens());
                }
            }

            return Future.succeededFuture();
        });
    }
}
