package io.github.sinri.keel.aigc.api.llm.dashscope.multimodalgeneration;

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
 * 将 DashScope 多模态 API 响应转换为 DashScopeMultimodalResponse。
 *
 * 与文本 API 响应的主要区别：
 * - message.content 可以是数组格式（包含多个内容元素）
 * - message.reasoning_content 为思考内容
 * - choices[0].image_hw 为图片尺寸信息
 * - usage 包含 image_tokens, video_tokens, audio_tokens 等多模态计费字段
 */
public class DashScopeMultimodalResponseConverter {

    /**
     * 转换 DashScope 多模态 API 响应为 DashScopeMultimodalResponse
     */
    public DashScopeMultimodalResponse convert(JsonObject dashscopeResponse) {
        // request_id 作为 id
        String id = dashscopeResponse.getString("request_id");

        // 提取 output
        JsonObject output = dashscopeResponse.getJsonObject("output");
        if (output == null) {
            CatholicLLMResponse emptyResponse = CatholicLLMResponseImpl.builder()
                .id(id)
                .message(CatholicAssistantMessage.ofText(""))
                .build();
            return new DashScopeMultimodalResponse(emptyResponse, null, null, null, null, null);
        }

        JsonArray choices = output.getJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            CatholicLLMResponse emptyResponse = CatholicLLMResponseImpl.builder()
                .id(id)
                .message(CatholicAssistantMessage.ofText(""))
                .build();
            return new DashScopeMultimodalResponse(emptyResponse, null, null, null, null, null);
        }

        JsonObject firstChoice = choices.getJsonObject(0);
        JsonObject message = firstChoice.getJsonObject("message");

        // 构建助手消息
        CatholicAssistantMessage assistantMessage = convertMessage(message);

        // reasoning_content
        String reasoningContent = null;
        if (message != null) {
            reasoningContent = message.getString("reasoning_content");
        }

        // image_hw
        JsonArray imageHw = firstChoice.getJsonArray("image_hw");

        // 提取 usage
        JsonObject usageJson = dashscopeResponse.getJsonObject("usage");
        CatholicLLMUsage usage = convertUsage(usageJson);

        // 多模态 token 计费
        Integer imageTokens = usageJson != null ? usageJson.getInteger("image_tokens") : null;
        Integer videoTokens = usageJson != null ? usageJson.getInteger("video_tokens") : null;
        Integer audioTokens = usageJson != null ? usageJson.getInteger("audio_tokens") : null;

        CatholicLLMResponse catholicResponse = CatholicLLMResponseImpl.builder()
            .id(id)
            .message(assistantMessage)
            .usage(usage)
            .build();

        return new DashScopeMultimodalResponse(
            catholicResponse,
            reasoningContent,
            imageHw,
            imageTokens,
            videoTokens,
            audioTokens
        );
    }

    /**
     * 转换 DashScope 多模态 message 为 CatholicAssistantMessage
     * message.content 可以是字符串或数组格式
     */
    private CatholicAssistantMessage convertMessage(JsonObject message) {
        if (message == null) {
            return CatholicAssistantMessage.ofText("");
        }

        // content 可能是字符串或数组
        String textContent = extractTextContent(message);
        JsonArray toolCalls = message.getJsonArray("tool_calls");

        List<CatholicToolCall> catholicToolCalls = null;
        if (toolCalls != null && !toolCalls.isEmpty()) {
            catholicToolCalls = new ArrayList<>();
            for (int i = 0; i < toolCalls.size(); i++) {
                JsonObject toolCall = toolCalls.getJsonObject(i);
                catholicToolCalls.add(convertToolCall(toolCall));
            }
        }

        return CatholicAssistantMessage.ofMixed(textContent, catholicToolCalls);
    }

    /**
     * 从 message 中提取文本内容。
     * content 可以是字符串或 DashScope 多模态数组格式 [{"text": "..."}]
     */
    private String extractTextContent(JsonObject message) {
        Object content = message.getValue("content");
        if (content == null) {
            return "";
        }

        if (content instanceof String text) {
            return text;
        }

        // 多模态数组格式
        if (content instanceof JsonArray contentArray) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < contentArray.size(); i++) {
                Object item = contentArray.getValue(i);
                if (item instanceof String textItem) {
                    sb.append(textItem);
                } else if (item instanceof JsonObject jsonItem) {
                    String text = jsonItem.getString("text");
                    if (text != null) {
                        sb.append(text);
                    }
                }
            }
            return sb.toString();
        }

        return content.toString();
    }

    /**
     * 转换 DashScope tool_call 为 CatholicToolCall
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
     * 转换 DashScope usage 为 CatholicLLMUsage
     */
    private CatholicLLMUsage convertUsage(JsonObject usage) {
        if (usage == null) {
            return CatholicLLMUsage.empty();
        }

        Integer inputTokens = usage.getInteger("input_tokens");
        Integer outputTokens = usage.getInteger("output_tokens");
        Integer totalTokens = usage.getInteger("total_tokens");

        if (totalTokens == null && inputTokens != null && outputTokens != null) {
            totalTokens = inputTokens + outputTokens;
        }

        return new CatholicLLMUsage(inputTokens, outputTokens, totalTokens);
    }
}