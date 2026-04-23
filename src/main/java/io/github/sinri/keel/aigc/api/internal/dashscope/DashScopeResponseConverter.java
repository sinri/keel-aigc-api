package io.github.sinri.keel.aigc.api.internal.dashscope;

import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicLLMResponseImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionCall;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 DashScope API 回复转换为 CatholicLLMResponse。
 */
public class DashScopeResponseConverter {

    /**
     * 转换 DashScope API 响应为 CatholicLLMResponse
     */
    public CatholicLLMResponse convert(JsonObject dashscopeResponse) {
        
        // request_id 作为 id
        String id = dashscopeResponse.getString("request_id");

        // 提取 output
        JsonObject output = dashscopeResponse.getJsonObject("output");
        if (output == null) {
                        return CatholicLLMResponseImpl.builder()
                .id(id)
                .message(CatholicAssistantMessage.ofText(""))
                .build();
        }

        JsonArray choices = output.getJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            // 旧格式（result_format 未设为 message 时）使用 output.text
            String text = output.getString("text");
            CatholicAssistantMessage assistantMessage = CatholicAssistantMessage.ofText(text != null ? text : "");
            CatholicLLMUsage usage = convertUsage(dashscopeResponse.getJsonObject("usage"));
            return CatholicLLMResponseImpl.builder()
                .id(id)
                .message(assistantMessage)
                .usage(usage)
                .build();
        }

        JsonObject firstChoice = choices.getJsonObject(0);
        JsonObject message = firstChoice.getJsonObject("message");
        
        // 构建助手消息
        CatholicAssistantMessage assistantMessage = convertMessage(message);

        // 提取 usage
        CatholicLLMUsage usage = convertUsage(dashscopeResponse.getJsonObject("usage"));

        return CatholicLLMResponseImpl.builder()
            .id(id)
            .message(assistantMessage)
            .usage(usage)
            .build();
    }

    /**
     * 转换 DashScope message 为 CatholicAssistantMessage
     */
    private CatholicAssistantMessage convertMessage(JsonObject message) {
        if (message == null) {
                        return CatholicAssistantMessage.ofText("");
        }

        String content = message.getString("content");
        JsonArray toolCalls = message.getJsonArray("tool_calls");
        
        // 处理工具调用
        List<CatholicFunctionToolCall> catholicFunctionToolCalls = null;
        if (toolCalls != null && !toolCalls.isEmpty()) {
            catholicFunctionToolCalls = new ArrayList<>();
            for (int i = 0; i < toolCalls.size(); i++) {
                JsonObject toolCall = toolCalls.getJsonObject(i);
                catholicFunctionToolCalls.add(convertToolCall(toolCall));
            }
        }

        return CatholicAssistantMessage.ofMixed(content, catholicFunctionToolCalls);
    }

    /**
     * 转换 DashScope tool_call 为 CatholicToolCall
     */
    private CatholicFunctionToolCall convertToolCall(JsonObject toolCall) {
        String id = toolCall.getString("id");
        String type = toolCall.getString("type", "function");
        JsonObject function = toolCall.getJsonObject("function");

        String name = function.getString("name");
        String arguments = function.getString("arguments", "{}");

        return new CatholicFunctionToolCall(
            id,
            type,
            new FunctionCall(name, arguments)
        );
    }

    /**
     * 转换 DashScope usage 为 CatholicLLMUsage
     */
    private CatholicLLMUsage convertUsage(JsonObject usage) {
        if (usage == null) {
                        return CatholicLLMUsage.empty();
        }

        // DashScope 使用 input_tokens/output_tokens，而非 prompt_tokens/completion_tokens
        Integer inputTokens = usage.getInteger("input_tokens");
        Integer outputTokens = usage.getInteger("output_tokens");
        Integer totalTokens = usage.getInteger("total_tokens");

        
        // 如果没有 total_tokens，尝试计算
        if (totalTokens == null && inputTokens != null && outputTokens != null) {
            totalTokens = inputTokens + outputTokens;
        }

        return new CatholicLLMUsage(inputTokens, outputTokens, totalTokens);
    }

    }