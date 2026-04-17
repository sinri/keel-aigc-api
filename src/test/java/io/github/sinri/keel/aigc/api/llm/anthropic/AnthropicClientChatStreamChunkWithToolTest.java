package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicTool;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * 流式逐 chunk + 工具（需 {@code anthropic.test1.*} 配置）。
 */
public class AnthropicClientChatStreamChunkWithToolTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        HttpClient httpClient = getKeel().createHttpClient();

        String baseUrl = ConfigElement.root().readProperty("anthropic.test1.api");
        String apiKey = ConfigElement.root().readProperty("anthropic.test1.key");
        String model = ConfigElement.root().readProperty("anthropic.test1.model");

        Objects.requireNonNull(apiKey, "Anthropic API key must be set (anthropic.test1.key)");
        Objects.requireNonNull(model, "Anthropic model must be set (anthropic.test1.model)");
        Objects.requireNonNull(baseUrl, "Base URL must be set (anthropic.test1.api)");

        AnthropicClient client = new AnthropicClient(
            httpClient,
            apiKey,
            baseUrl
        );

        JsonObject weatherToolParams = new JsonObject()
            .put("type", "object")
            .put("properties", new JsonObject()
                .put("city", new JsonObject()
                    .put("type", "string")
                    .put("description", "城市名称，如：北京、上海、东京")))
            .put("required", new JsonArray().add("city"));

        CatholicTool weatherTool = CatholicTool.function(
            "get_weather",
            "获取指定城市的当前天气信息，包括温度、天气状况等",
            weatherToolParams
        );

        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model(model)
            .addMessage(CatholicSystemMessage.of("You are a helpful assistant. Use the provided tools when needed."))
            .addMessage(CatholicUserMessage.ofText("我想知道北京和上海今天的天气情况，请帮我查询。"))
            .addTool(weatherTool)
            .enableStream()
            .build();

        getLogger().info("Sending stream request with tools (processing chunks)...");

        return client.callStream(request, chunk -> {
            if (chunk.id() != null) {
                getLogger().info("Chunk ID: " + chunk.id() + ", Index: " + chunk.index());
            }

            if (chunk.hasDeltaText()) {
                getLogger().info("Delta text: " + chunk.deltaText());
            }

            if (chunk.hasDeltaToolCalls()) {
                chunk.deltaToolCalls().forEach(tcDelta -> {
                    StringBuilder info = new StringBuilder("Tool call delta: ");
                    if (tcDelta.id() != null) {
                        info.append("id=").append(tcDelta.id()).append(", ");
                    }
                    info.append("index=").append(tcDelta.index());
                    if (tcDelta.function() != null) {
                        if (tcDelta.function().name() != null) {
                            info.append(", function=").append(tcDelta.function().name());
                        }
                        if (tcDelta.function().argumentsDelta() != null) {
                            info.append(", args=").append(tcDelta.function().argumentsDelta());
                        }
                    }
                    getLogger().info(info.toString());
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
