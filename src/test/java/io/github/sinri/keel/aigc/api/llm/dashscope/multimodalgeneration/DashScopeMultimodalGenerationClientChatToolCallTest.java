package io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicToolCallMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicUserMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DashScope 多模态 API 工具调用（Function Calling）测试。
 * 测试完整的工具调用流程：
 * 1. 发送带 tools 的请求 → 收到 tool_calls 响应
 * 2. 构造工具执行结果（Tool Message）
 * 3. 发送包含工具结果的历史消息 → 收到最终文本回复
 */
public class DashScopeMultimodalGenerationClientChatToolCallTest extends KeelInstantRunner {

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

        DashScopeMultimodalGenerationLLM client = DashScopeMultimodalGenerationLLM.builder()
                                                                                  .httpClient(httpClient)
                                                                                  .apiKey(apiKey)
                                                                                  .baseUrl(baseUrl)
                                                                                  .build();

        // 定义工具
        JsonObject getWeatherParams = new JsonObject()
                .put("type", "object")
                .put("properties", new JsonObject()
                        .put("location", new JsonObject()
                                .put("type", "string")
                                .put("description", "城市或县区，比如北京市、杭州市、余杭区等。")))
                .put("required", new JsonArray().add("location"));

        JsonObject getTimeParams = new JsonObject();

        CatholicToolDefinition getWeatherTool = CatholicToolDefinition.function("get_current_weather", "当你想查询指定城市的天气时非常有用。", getWeatherParams);
        CatholicToolDefinition getTimeTool = CatholicToolDefinition.function("get_current_time", "当你想知道现在的时间时非常有用。", getTimeParams);

        // 步骤1: 发送带 tools 的请求
        CatholicLLMRequest request1 = CatholicLLMRequest.builder()
                .model(model)
                .addMessage(CatholicUserMessage.ofText("杭州天气怎么样"))
                .addTool(getWeatherTool)
                .addTool(getTimeTool)
                .build();

        getLogger().info("=== Step 1: Sending request with tools (Multimodal) ===");

        return client.callMultimodal(request1)
                .compose(multimodalResponse1 -> {
                    var response1 = multimodalResponse1.catholicResponse();
                    getLogger().info("Response 1 ID: " + response1.id());
                    getLogger().info("Response 1 has text: " + response1.hasText());
                    getLogger().info("Response 1 has tool calls: " + response1.hasToolCalls());

                    // 多模态特有字段
                    if (multimodalResponse1.reasoningContent() != null) {
                        getLogger().info("Reasoning Content:\n" + multimodalResponse1.reasoningContent());
                    }

                    if (response1.hasToolCalls()) {
                        List<CatholicFunctionToolCall> toolCalls = response1.message().toolCalls();
                        getLogger().info("Tool calls count: " + toolCalls.size());

                        for (CatholicFunctionToolCall tc : toolCalls) {
                            getLogger().info("ToolCall: id=" + tc.id()
                                    + " type=" + tc.type()
                                    + " function.name=" + tc.function().name()
                                    + " function.arguments=" + tc.function().arguments());
                        }

                        // 步骤2: 构造工具执行结果并发送第二轮请求
                        List<CatholicChatMessage> messages = new ArrayList<>();
                        messages.add(CatholicUserMessage.ofText("杭州天气怎么样"));
                        messages.add(response1.message());

                        for (CatholicFunctionToolCall tc : toolCalls) {
                            String toolResult = simulateToolExecution(tc);
                            getLogger().info("Tool result for " + tc.function().name() + ": " + toolResult);
                            messages.add(new CatholicToolCallMessage(tc.id(), toolResult));
                        }

                        CatholicLLMRequest request2 = CatholicLLMRequest.builder()
                                .model(model)
                                .messages(messages)
                                .addTool(getWeatherTool)
                                .addTool(getTimeTool)
                                .build();

                        getLogger().info("=== Step 2: Sending tool results ===");

                        return client.callMultimodal(request2)
                                .compose(multimodalResponse2 -> {
                                    var response2 = multimodalResponse2.catholicResponse();
                                    getLogger().info("Response 2 ID: " + response2.id());
                                    getLogger().info("Response 2 has text: " + response2.hasText());
                                    getLogger().info("Response 2 has tool calls: " + response2.hasToolCalls());

                                    // 多模态特有字段
                                    if (multimodalResponse2.reasoningContent() != null) {
                                        getLogger().info("Reasoning Content:\n" + multimodalResponse2.reasoningContent());
                                    }

                                    if (response2.hasText()) {
                                        getLogger().info("Final Answer:\n" + response2.text());
                                    }

                                    var usage2 = response2.usage();
                                    if (usage2.totalTokens() != null) {
                                        getLogger().info("Token Usage (round 2): input=" + usage2.promptTokens()
                                                + ", output=" + usage2.completionTokens()
                                                + ", total=" + usage2.totalTokens());
                                    }

                                    return Future.succeededFuture();
                                });
                    } else {
                        // 模型直接回答了，没有调用工具
                        if (response1.hasText()) {
                            getLogger().info("Direct answer (no tool call):\n" + response1.text());
                        }
                        return Future.succeededFuture();
                    }
                });
    }

    /**
     * 模拟工具执行，返回结果字符串
     */
    private String simulateToolExecution(CatholicFunctionToolCall toolCall) {
        String functionName = toolCall.function().name();
        switch (functionName) {
            case "get_current_weather" -> {
                try {
                    JsonObject args = new JsonObject(toolCall.function().arguments());
                    String location = args.getString("location", "杭州");
                    return location + "今天天气晴朗，温度25°C，湿度60%，空气质量优良。";
                } catch (Exception e) {
                    return "杭州今天天气晴朗，温度25°C";
                }
            }
            case "get_current_time" -> {
                return "当前时间是2026年4月16日 14:30:00";
            }
            default -> {
                return "Unknown tool: " + functionName;
            }
        }
    }
}