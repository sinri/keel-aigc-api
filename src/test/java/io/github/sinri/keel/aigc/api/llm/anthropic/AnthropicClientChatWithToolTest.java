package io.github.sinri.keel.aigc.api.llm.anthropic;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 非流式 + 工具多轮（需 {@code anthropic.test1.*} 配置）。
 */
public class AnthropicClientChatWithToolTest extends KeelInstantRunner {
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

        JsonObject weatherToolParams = new JsonObject()
            .put("type", "object")
            .put("properties", new JsonObject()
                .put("city", new JsonObject()
                    .put("type", "string")
                    .put("description", "城市名称，如：北京、上海、东京")))
            .put("required", new JsonArray().add("city"));

        CatholicToolDefinition weatherTool = CatholicToolDefinition.function(
            "get_weather",
            "获取指定城市的当前天气信息，包括温度、天气状况等",
            weatherToolParams
        );

        CatholicLLMRequest request = CatholicLLMRequest.builder()
            .model(model)
            .addMessage(CatholicSystemMessage.of("You are a helpful assistant. Use the provided tools when needed."))
            .addMessage(CatholicUserMessage.ofText("我想知道北京和上海今天的天气情况，请帮我查询。"))
            .addTool(weatherTool)
            .build();

        getLogger().info("Sending initial request with tools...");

        return client.call(request)
            .compose(response -> handleResponse(client, model, response, new ArrayList<>()));
    }

    private Future<Void> handleResponse(
        AnthropicLLM client,
        String model,
        CatholicLLMResponse response,
        List<CatholicToolCallMessage> toolResults
    ) {
        getLogger().info("Response ID: " + response.id());

        if (response.hasText()) {
            getLogger().info("Text response: " + response.text());
        }

        if (response.hasToolCalls()) {
            getLogger().info("Tool calls requested: " + response.message().toolCalls().size());

            List<CatholicFunctionToolCall> toolCalls = response.message().toolCalls();
            for (CatholicFunctionToolCall toolCall : toolCalls) {
                getLogger().info("Tool: " + toolCall.functionName() +
                    ", Args: " + toolCall.function().arguments());

                String toolResult = executeTool(toolCall.functionName(), toolCall.parseArguments());
                getLogger().info("Tool result: " + toolResult);

                toolResults.add(CatholicToolCallMessage.of(toolCall.id(), toolResult));
            }

            var requestBuilder = CatholicLLMRequest.builder()
                .model(model)
                .addMessage(CatholicSystemMessage.of("You are a helpful assistant. Use the provided tools when needed."))
                .addMessage(CatholicUserMessage.ofText("我想知道北京和上海今天的天气情况，请帮我查询。"))
                .addMessage(response.message());

            for (CatholicToolCallMessage toolResult : toolResults) {
                requestBuilder.addMessage(toolResult);
            }

            CatholicLLMRequest continueRequest = requestBuilder.build();

            getLogger().info("Sending continue request with tool results...");

            return client.call(continueRequest)
                .compose(finalResponse -> {
                    getLogger().info("Final Response ID: " + finalResponse.id());
                    if (finalResponse.hasText()) {
                        getLogger().info("Final response: " + finalResponse.text());
                    }

                    var usage = finalResponse.usage();
                    if (usage.totalTokens() != null) {
                        getLogger().info("Total tokens: " + usage.totalTokens());
                    }

                    return Future.succeededFuture();
                });
        } else {
            var usage = response.usage();
            if (usage.totalTokens() != null) {
                getLogger().info("Total tokens: " + usage.totalTokens());
            }
            return Future.succeededFuture();
        }
    }

    private String executeTool(String functionName, JsonObject args) {
        if ("get_weather".equals(functionName)) {
            String city = args.getString("city", "未知城市");
            JsonObject weatherData = new JsonObject()
                .put("city", city)
                .put("temperature", 25)
                .put("condition", "晴天")
                .put("humidity", 60)
                .put("wind", "微风");
            return weatherData.encode();
        }
        return "{\"error\": \"Unknown tool: " + functionName + "\"}";
    }
}
