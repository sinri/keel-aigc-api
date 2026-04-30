package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.llm.catholic.AuthMethod;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.function.FunctionDefinition;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.http.HttpClient;

import java.util.Objects;

public class OpenAIChatCompletionsClientChatStreamChunkWithToolTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() {
        HttpClient httpClient = getKeel().createHttpClient();

        String baseUrl = ConfigElement.root().readProperty("openai.test1.api");
        String apiKey = ConfigElement.root().readProperty("openai.test1.key");
        String model = ConfigElement.root().readProperty("openai.test1.model");
        AuthMethod authMethod = AuthMethod.valueOf(ConfigElement.root().readProperty("openai.test1.authMethod") != null ? ConfigElement.root().readProperty("openai.test1.authMethod") : "Bearer");

        Objects.requireNonNull(apiKey, "OpenAI API key must be set");
        Objects.requireNonNull(model, "OpenAI model must be set");
        Objects.requireNonNull(baseUrl, "Base URL must be set");

        OpenAIChatCompletionsLLM client = new OpenAIChatCompletionsLLM(
                httpClient,
                apiKey,
                baseUrl,
                authMethod
        );

        // 定义工具：获取天气信息
        JsonObject weatherToolParams = new JsonObject()
                .put("type", "object")
                .put("properties", new JsonObject()
                        .put("city", new JsonObject()
                                .put("type", "string")
                                .put("description", "城市名称，如：北京、上海、东京")))
                .put("required", new io.vertx.core.json.JsonArray().add("city"));

        CatholicToolDefinition weatherTool = CatholicToolDefinition.function(
                FunctionDefinition.of(
                        "get_weather",
                        "获取指定城市的当前天气信息，包括温度、天气状况等",
                        weatherToolParams
                )
        );

        // 构建初始请求（启用流式）
        CatholicLLMRequest request = CatholicLLMRequest.builder()
                .model(model)
                .addMessage(CatholicSystemMessage.of("You are a helpful assistant. Use the provided tools when needed."))
                .addMessage(CatholicUserMessage.ofText("我想知道北京和上海今天的天气情况，请帮我查询。"))
                .addTool(weatherTool)
                .enableStream()
                .build();

        getLogger().info("Sending stream request with tools (processing chunks)...");

        // 流式调用，逐片段处理
        return client.callStream(request, chunk -> {
            // 打印每个片段的信息
            if (chunk.id() != null) {
                getLogger().info("Chunk ID: " + chunk.id() + ", Index: " + chunk.index());
            }

            // 打印增量文本内容
            if (chunk.hasDeltaText()) {
                getLogger().info("Delta text: " + chunk.deltaText());
            }

            // 打印增量工具调用（关键：工具调用在流式响应中是增量累积的）
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
