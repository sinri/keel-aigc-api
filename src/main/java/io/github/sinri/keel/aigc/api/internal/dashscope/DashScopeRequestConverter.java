package io.github.sinri.keel.aigc.api.internal.dashscope;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.*;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicFunctionToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * 将 CatholicLLMRequest 转换为 DashScope API 请求格式。
 */
public class DashScopeRequestConverter {

    /**
     * 转换请求为 DashScope API JSON 格式
     */
    public JsonObject convert(CatholicLLMRequest request) {
        JsonObject dashscopeRequest = new JsonObject();

        // model
        dashscopeRequest.put("model", request.model());

        // input
        JsonObject input = new JsonObject();
        JsonArray messages = convertMessages(request.messages());
        input.put("messages", messages);
        dashscopeRequest.put("input", input);

        // parameters
        JsonObject parameters = convertParameters(request.options());
        // stream 和 incremental_output 放入 parameters 中
        parameters.put("stream", request.stream());
        if (request.stream()) {
            parameters.put("incremental_output", true);
        }
        dashscopeRequest.put("parameters", parameters);

        // tools 放入 parameters 对象中（DashScope HTTP API 规范）
        if (request.hasTools()) {
            JsonArray tools = convertTools(request.tools());
            parameters.put("tools", tools);
        }

        
        return dashscopeRequest;
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
                msg.put("content", ((CatholicSystemMessage) message).text());
            }
            case CatholicUserMessage.ROLE -> {
                Object content = convertUserContent((CatholicUserMessage) message);
                msg.put("content", content);
            }
            case CatholicAssistantMessage.ROLE -> {
                CatholicAssistantMessage assistantMsg = (CatholicAssistantMessage) message;
                if (assistantMsg.hasText()) {
                    msg.put("content", assistantMsg.text());
                }
                if (assistantMsg.hasToolCalls()) {
                    msg.put("tool_calls", convertToolCallsForMessage(assistantMsg.toolCalls()));
                }
            }
            case CatholicToolCallMessage.ROLE -> {
                CatholicToolCallMessage toolMsg = (CatholicToolCallMessage) message;
                msg.put("tool_call_id", toolMsg.toolCallId());
                msg.put("content", toolMsg.content());
            }
            default -> {
                // 通用处理
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
                    msg.put("content", first.getString("text"));
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

        // 多模态内容返回数组（DashScope 多模态格式）
        JsonArray contentArray = new JsonArray();
        for (CatholicChatContent content : contents) {
            if (content instanceof CatholicTextContent textContent) {
                contentArray.add(new JsonObject()
                    .put("type", "text")
                    .put("text", textContent.text()));
            } else if (content instanceof CatholicImageByUrl imageByUrl) {
                contentArray.add(new JsonObject()
                    .put("type", "image_url")
                    .put("image_url", imageByUrl.url()));
            } else if (content instanceof CatholicImageByBase64 imageByBase64) {
                contentArray.add(new JsonObject()
                    .put("type", "image_url")
                    .put("image_url", "data:" + imageByBase64.mediaType() + ";base64," + imageByBase64.base64Data()));
            }
        }
        return contentArray;
    }

    /**
     * 转换消息中的工具调用列表
     */
    private JsonArray convertToolCallsForMessage(java.util.List<CatholicFunctionToolCall> toolCalls) {
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
            JsonObject toolDef = new JsonObject()
                .put("type", tool.type())
                .put("function", new JsonObject()
                    .put("name", ((CatholicFunctionToolDefinition) tool).function().name())
                    .put("description", ((CatholicFunctionToolDefinition) tool).function().description()));

            // parameters
            if (((CatholicFunctionToolDefinition) tool).function().parameters() != null && !((CatholicFunctionToolDefinition) tool).function().parameters().isEmpty()) {
                toolDef.getJsonObject("function").put("parameters", ((CatholicFunctionToolDefinition) tool).function().parameters());
            }

            array.add(toolDef);
        }
        return array;
    }

    /**
     * 转换生成参数
     */
    private JsonObject convertParameters(CatholicLLMRequestOptions options) {
        JsonObject params = new JsonObject();

        // 必须设置 result_format 为 message，否则 DashScope 默认返回旧格式（output.text）
        // 旧格式不含 choices/message 结构，无法正确解析
        params.put("result_format", "message");

        if (options.temperature() != null) {
            params.put("temperature", options.temperature());
        }
        if (options.maxTokens() != null) {
            params.put("max_tokens", options.maxTokens());
        }
        if (options.topP() != null) {
            params.put("top_p", options.topP());
        }
        if (options.stop() != null && !options.stop().isEmpty()) {
            params.put("stop", options.stop());
        }

        // extra 参数合并到 parameters
        if (options.extra() != null && !options.extra().isEmpty()) {
            params.mergeIn(options.extra());
        }

        return params;
    }

    }