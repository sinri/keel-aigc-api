package io.github.sinri.keel.aigc.api.internal.openai.chatcompletions;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.*;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicFunctionToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 将 CatholicLLMRequest 转换为 OpenAI Chat Completions API 请求格式。
 *
 * @see <a href="https://developers.openai.com/api/reference/resources/chat">OpenAI Chat API Reference</a>
 */
public class OpenAIChatCompletionsRequestConverter {

    /**
     * 转换请求为 OpenAI API JSON 格式
     *
     * @see <a href="https://help.aliyun.com/zh/model-studio/qwen-api-via-openai-chat-completions">
     *     Alibaba Cloud Model Studio: OpenAI-compatible Chat API</a>
     * @see <a href="https://api.volcengine.com/api-docs/view?action=ChatCompletions&amp;serviceCode=ark&amp;version=2024-01-01">
     *     Volcengine Ark ChatCompletions API</a>
     */
    public JsonObject convert(CatholicLLMRequest request) {
        JsonObject openaiRequest = new JsonObject();

        // model
        openaiRequest.put("model", request.model());

        // messages
        JsonArray messages = convertMessages(request.messages());
        openaiRequest.put("messages", messages);

        // tools (可选)
        if (request.hasTools()) {
            JsonArray tools = convertTools(request.tools());
            openaiRequest.put("tools", tools);
        }

        // options
        convertOptions(openaiRequest, request.options());

        // stream
        openaiRequest.put("stream", request.stream());

        return openaiRequest;
    }

    /**
     * 转换消息列表
     */
    private JsonArray convertMessages(java.util.List<CatholicChatMessage> messages) {
        JsonArray array = new JsonArray();
        for (CatholicChatMessage message : messages) {
            array.add(convertMessage(message));
        }
        return array;
    }

    /**
     * 转换单个消息
     */
    private JsonObject convertMessage(CatholicChatMessage message) {
        JsonObject msg = new JsonObject();
        msg.put("role", message.role());

        switch (message.role()) {
            case CatholicSystemMessage.ROLE -> {
                // system 消息只有文本
                msg.put("content", ((CatholicSystemMessage) message).text());
            }
            case CatholicUserMessage.ROLE -> {
                // user 消息可能多模态
                msg.put("content", convertUserContent((CatholicUserMessage) message));
            }
            case CatholicAssistantMessage.ROLE -> {
                CatholicAssistantMessage assistantMsg = (CatholicAssistantMessage) message;
                // assistant 可能有文本或工具调用
                if (assistantMsg.hasText()) {
                    msg.put("content", assistantMsg.text());
                }
                if (assistantMsg.hasToolCalls()) {
                    msg.put("tool_calls", convertToolCalls(assistantMsg.toolCalls()));
                }
            }
            case CatholicToolCallMessage.ROLE -> {
                CatholicToolCallMessage toolMsg = (CatholicToolCallMessage) message;
                msg.put("tool_call_id", toolMsg.toolCallId());
                msg.put("content", toolMsg.content());
            }
            default -> {
                // 未知角色，尝试通用处理
                JsonArray contentArray = new JsonArray();
                for (CatholicChatContent content : message.contents()) {
                    if (content instanceof CatholicTextContent textContent) {
                        contentArray.add(new JsonObject()
                            .put("type", "text")
                            .put("text", textContent.text()));
                    }
                }
                if (contentArray.size() == 1) {
                    JsonObject first = contentArray.getJsonObject(0);
                    if (first.getString("type").equals("text")) {
                        msg.put("content", first.getString("text"));
                    }
                } else {
                    msg.put("content", contentArray);
                }
            }
        }

        return msg;
    }

    /**
     * 转换用户消息内容（多模态）
     */
    private Object convertUserContent(CatholicUserMessage userMessage) {
        java.util.List<CatholicChatContent> contents = userMessage.contents();

        // 单文本内容直接返回字符串
        if (contents.size() == 1 && contents.get(0) instanceof CatholicTextContent textContent) {
            return textContent.text();
        }

        // 多模态内容返回数组
        JsonArray contentArray = new JsonArray();
        for (CatholicChatContent content : contents) {
            if (content instanceof CatholicTextContent textContent) {
                contentArray.add(new JsonObject()
                    .put("type", "text")
                    .put("text", textContent.text()));
            } else if (content instanceof CatholicImageByUrl imageByUrl) {
                contentArray.add(new JsonObject()
                    .put("type", "image_url")
                    .put("image_url", new JsonObject()
                        .put("url", imageByUrl.url())));
            } else if (content instanceof CatholicImageByBase64 imageByBase64) {
                String dataUrl = "data:" + imageByBase64.mediaType() + ";base64," + imageByBase64.base64Data();
                contentArray.add(new JsonObject()
                    .put("type", "image_url")
                    .put("image_url", new JsonObject()
                        .put("url", dataUrl)));
            }
        }
        return contentArray;
    }

    /**
     * 转换工具调用列表
     */
    private JsonArray convertToolCalls(java.util.List<CatholicFunctionToolCall> toolCalls) {
        JsonArray array = new JsonArray();
        for (CatholicFunctionToolCall toolCall : toolCalls) {
            array.add(new JsonObject()
                .put("id", toolCall.id())
                .put("type", toolCall.type())
                .put("function", new JsonObject()
                    .put("name", toolCall.function().name())
                    .put("arguments", toolCall.function().arguments())));
        }
        return array;
    }

    /**
     * 转换工具定义列表
     */
    private JsonArray convertTools(java.util.List<CatholicToolDefinition> tools) {
        JsonArray array = new JsonArray();
        for (CatholicToolDefinition tool : tools) {
            array.add(new JsonObject()
                .put("type", tool.type())
                .put("function", new JsonObject()
                    .put("name", ((CatholicFunctionToolDefinition) tool).function().name())
                    .put("description", ((CatholicFunctionToolDefinition) tool).function().description())
                    .put("parameters", ((CatholicFunctionToolDefinition) tool).function().parameters())));
        }
        return array;
    }

    /**
     * 转换生成参数选项
     */
    private void convertOptions(JsonObject openaiRequest, CatholicLLMRequestOptions options) {
        if (options.temperature() != null) {
            openaiRequest.put("temperature", options.temperature());
        }
        if (options.maxTokens() != null) {
            openaiRequest.put("max_tokens", options.maxTokens());
        }
        if (options.topP() != null) {
            openaiRequest.put("top_p", options.topP());
        }
        if (options.stop() != null && !options.stop().isEmpty()) {
            openaiRequest.put("stop", options.stop());
        }
        // extra 参数直接合并
        if (options.extra() != null && !options.extra().isEmpty()) {
            openaiRequest.mergeIn(options.extra());
        }
    }
}
