package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;

import java.util.Objects;

/**
 * 流式调用并逐 chunk 处理（需 {@code anthropic.test1.*} 配置）。
 */
public class AnthropicClientChatStreamChunkTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        HttpClient httpClient = getKeel().createHttpClient();

        String baseUrl = ConfigElement.root().readProperty("anthropic.test1.api");
        String apiKey = ConfigElement.root().readProperty("anthropic.test1.key");
        String model = ConfigElement.root().readProperty("anthropic.test1.model");

        Objects.requireNonNull(apiKey, "Anthropic API key must be set (anthropic.test1.key)");
        Objects.requireNonNull(model, "Anthropic model must be set (anthropic.test1.model)");
        Objects.requireNonNull(baseUrl, "Base URL must be set (anthropic.test1.api)");

        AnthropicLLM client = new AnthropicLLM(
            httpClient,
            apiKey,
            baseUrl
        );

        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model(model)
            .addMessage(CatholicSystemMessage.of("You are a helpful travel assistant. Please provide detailed and practical travel plans in Chinese."))
            .addMessage(CatholicUserMessage.ofText("请帮我制定一个下个月从中国到日本的旅行计划，包括：\n" +
                "1. 推荐的目的地城市和景点\n" +
                "2. 大致的行程安排（7天左右）\n" +
                "3. 预估的费用预算\n" +
                "4. 需要准备的物品和注意事项"))
            .enableStream()
            .build();

        getLogger().info("Starting stream request...");

        return client.callStream(request, chunk -> {
            if (chunk.id() != null) {
                getLogger().info("Chunk ID: " + chunk.id() + ", Index: " + chunk.index());
            }

            if (chunk.hasDeltaText()) {
                getLogger().info("Delta text: " + chunk.deltaText());
            }

            if (chunk.hasDeltaToolCalls()) {
                chunk.deltaToolCalls().forEach(tcDelta -> {
                    getLogger().info("Tool call delta: id=" + tcDelta.id() +
                        ", index=" + tcDelta.index() +
                        ", function=" + tcDelta.function().name() +
                        ", args=" + tcDelta.function().argumentsDelta());
                });
            }

            if (chunk.isFinished()) {
                getLogger().info("Stream finished!");

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
