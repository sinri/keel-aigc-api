package io.github.sinri.keel.aigc.api.internal.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicLLMResponseImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicToolCall.CatholicToolCallFunction;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 OpenAI Chat Completions API 回复转换为 CatholicLLMResponse。
 */
public class OpenAIChatCompletionsResponseConverter {

    /**
     * 转换 OpenAI API 响应为 CatholicLLMResponse
     */
    public CatholicLLMResponse convert(JsonObject openaiResponse) {
        String id = openaiResponse.getString("id");

        // 提取 choices
        JsonArray choices = openaiResponse.getJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            return CatholicLLMResponseImpl.builder()
                .id(id)
                .message(CatholicAssistantMessage.ofText(""))
                .build();
        }

        JsonObject firstChoice = choices.getJsonObject(0);
        JsonObject message = firstChoice.getJsonObject("message");

        // 构建助手消息
        CatholicAssistantMessage assistantMessage = convertMessage(message);

        // 提取 usage
        CatholicLLMUsage usage = convertUsage(openaiResponse.getJsonObject("usage"));

        return CatholicLLMResponseImpl.builder()
            .id(id)
            .message(assistantMessage)
            .usage(usage)
            .build();
    }

    /**
     * 转换 OpenAI message 为 CatholicAssistantMessage
     */
    private CatholicAssistantMessage convertMessage(JsonObject message) {
        if (message == null) {
            return CatholicAssistantMessage.ofText("");
        }

        String content = message.getString("content");
        JsonArray toolCalls = message.getJsonArray("tool_calls");

        // 处理工具调用
        List<CatholicToolCall> catholicToolCalls = null;
        if (toolCalls != null && !toolCalls.isEmpty()) {
            catholicToolCalls = new ArrayList<>();
            for (int i = 0; i < toolCalls.size(); i++) {
                JsonObject toolCall = toolCalls.getJsonObject(i);
                catholicToolCalls.add(convertToolCall(toolCall));
            }
        }

        return CatholicAssistantMessage.ofMixed(content, catholicToolCalls);
    }

    /**
     * 转换 OpenAI tool_call 为 CatholicToolCall
     */
    private CatholicToolCall convertToolCall(JsonObject toolCall) {
        String id = toolCall.getString("id");
        String type = toolCall.getString("type", "function");
        JsonObject function = toolCall.getJsonObject("function");

        String name = function.getString("name");
        String arguments = function.getString("arguments", "{}");

        return new CatholicToolCall(
            id,
            type,
            new CatholicToolCallFunction(name, arguments)
        );
    }

    /**
     * 转换 OpenAI usage 为 CatholicLLMUsage
     */
    private CatholicLLMUsage convertUsage(JsonObject usage) {
        if (usage == null) {
            return CatholicLLMUsage.empty();
        }

        Integer promptTokens = usage.getInteger("prompt_tokens");
        Integer completionTokens = usage.getInteger("completion_tokens");
        Integer totalTokens = usage.getInteger("total_tokens");

        return new CatholicLLMUsage(promptTokens, completionTokens, totalTokens);
    }
}
