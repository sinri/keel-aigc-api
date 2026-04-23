package io.github.sinri.keel.aigc.api.llm.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.llm.catholic.AuthMethod;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicSystemMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicTool;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicToolCall;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.http.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OpenAIChatCompletionsClientChatStreamWithToolTest extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
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

        CatholicTool weatherTool = CatholicTool.function(
                "get_weather",
                "获取指定城市的当前天气信息，包括温度、天气状况等",
                weatherToolParams
        );

        // 构建初始请求（启用流式）
        CatholicLLMRequest request = CatholicLLMRequest.builder()
                .model(model)
                .addMessage(CatholicSystemMessage.of("You are a helpful assistant. Use the provided tools when needed."))
                .addMessage(CatholicUserMessage.ofText("我想知道北京和上海今天的天气情况，请帮我查询。"))
                .addTool(weatherTool)
                .enableStream()
                .build();

        getLogger().info("Sending stream request with tools (collecting all chunks)...");

        // 流式调用，收集所有片段后返回完整响应
        return client.callStream(request)
                .compose(response -> handleResponse(client, model, response, new ArrayList<>()));
    }

    /**
     * 处理响应，如果是工具调用则执行工具并继续对话
     */
    private Future<Void> handleResponse(
            OpenAIChatCompletionsLLM client,
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

            // 处理每个工具调用
            List<CatholicToolCall> toolCalls = response.message().toolCalls();
            for (CatholicToolCall toolCall : toolCalls) {
                getLogger().info("Tool: " + toolCall.functionName() +
                        ", Args: " + toolCall.function().arguments());

                // 执行工具（模拟）
                String toolResult = executeTool(toolCall.functionName(), toolCall.parseArguments());
                getLogger().info("Tool result: " + toolResult);

                // 添加工具调用结果消息
                toolResults.add(CatholicToolCallMessage.of(toolCall.id(), toolResult));
            }

            // 构建继续对话的请求（启用流式）
            var requestBuilder = CatholicLLMRequest.builder()
                    .model(model)
                    .addMessage(CatholicSystemMessage.of("You are a helpful assistant. Use the provided tools when needed."))
                    .addMessage(CatholicUserMessage.ofText("我想知道北京和上海今天的天气情况，请帮我查询。"))
                    .addMessage(response.message());

            // 添加工具执行结果
            for (CatholicToolCallMessage toolResult : toolResults) {
                requestBuilder.addMessage(toolResult);
            }

            CatholicLLMRequest continueRequest = requestBuilder.enableStream().build();

            getLogger().info("Sending continue stream request with tool results...");

            // 继续流式调用 LLM 获取最终响应
            return client.callStream(continueRequest)
                    .compose(finalResponse -> {
                        getLogger().info("Final Response ID: " + finalResponse.id());
                        if (finalResponse.hasText()) {
                            getLogger().info("Final response: " + finalResponse.text());
                        }

                        // 打印 token 使用情况
                        var usage = finalResponse.usage();
                        if (usage.totalTokens() != null) {
                            getLogger().info("Total tokens: " + usage.totalTokens());
                        }

                        getLogger().info("Stream completed, all chunks collected.");

                        return Future.succeededFuture();
                    });
        } else {
            // 没有 tool calls，对话结束
            var usage = response.usage();
            if (usage.totalTokens() != null) {
                getLogger().info("Total tokens: " + usage.totalTokens());
            }
            getLogger().info("Stream completed, no tool calls.");
            return Future.succeededFuture();
        }
    }

    /**
     * 模拟执行工具
     */
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
