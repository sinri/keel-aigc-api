package io.github.sinri.keel.aigc.api.internal.anthropic;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.*;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicFunctionToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.definition.CatholicToolDefinition;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 将 {@link CatholicLLMRequest} 转为 Anthropic Messages API 请求体。
 *
 * @see <a href="https://platform.claude.com/docs/en/api/messages">Anthropic Messages API</a>
 */
public class AnthropicRequestConverter {

    private static final int DEFAULT_MAX_TOKENS = 4096;

    /**
     * 生成请求 JSON（不含 {@code stream}，由客户端写入）。
     */
    public JsonObject convert(CatholicLLMRequest request) {
        JsonObject body = new JsonObject();
        body.put("model", request.model());

        StringBuilder systemText = new StringBuilder();
        JsonArray messages = new JsonArray();
        for (CatholicChatMessage message : request.messages()) {
            if (CatholicSystemMessage.ROLE.equals(message.role())) {
                if (!systemText.isEmpty()) {
                    systemText.append("\n\n");
                }
                systemText.append(((CatholicSystemMessage) message).text());
            } else {
                messages.add(convertNonSystemMessage(message));
            }
        }
        if (!systemText.isEmpty()) {
            body.put("system", systemText.toString());
        }
        body.put("messages", messages);

        if (request.hasTools()) {
            body.put("tools", convertTools(request.tools()));
        }

        convertOptions(body, request.options());

        return body;
    }

    private JsonObject convertNonSystemMessage(CatholicChatMessage message) {
        return switch (message.role()) {
            case CatholicUserMessage.ROLE -> convertUserMessage((CatholicUserMessage) message);
            case CatholicAssistantMessage.ROLE -> convertAssistantMessage((CatholicAssistantMessage) message);
            case CatholicToolCallMessage.ROLE -> convertToolResultMessage((CatholicToolCallMessage) message);
            default -> convertUnknownRoleMessage(message);
        };
    }

    private JsonObject convertUserMessage(CatholicUserMessage user) {
        Object content = convertUserContent(user);
        return new JsonObject()
            .put("role", "user")
            .put("content", content);
    }

    private Object convertUserContent(CatholicUserMessage userMessage) {
        List<CatholicChatContent> contents = userMessage.contents();
        if (contents.size() == 1 && contents.get(0) instanceof CatholicTextContent textContent) {
            return textContent.text();
        }
        JsonArray blocks = new JsonArray();
        for (CatholicChatContent content : contents) {
            if (content instanceof CatholicTextContent textContent) {
                blocks.add(textBlock(textContent.text()));
            } else if (content instanceof CatholicImageByUrl imageByUrl) {
                blocks.add(new JsonObject()
                    .put("type", "image")
                    .put("source", new JsonObject()
                        .put("type", "url")
                        .put("url", imageByUrl.url())));
            } else if (content instanceof CatholicImageByBase64 imageByBase64) {
                blocks.add(new JsonObject()
                    .put("type", "image")
                    .put("source", new JsonObject()
                        .put("type", "base64")
                        .put("media_type", imageByBase64.mediaType())
                        .put("data", imageByBase64.base64Data())));
            }
        }
        return blocks;
    }

    private JsonObject convertAssistantMessage(CatholicAssistantMessage assistant) {
        JsonArray content = new JsonArray();
        if (assistant.hasText()) {
            content.add(textBlock(assistant.text()));
        }
        if (assistant.hasToolCalls()) {
            for (CatholicFunctionToolCall toolCall : assistant.toolCalls()) {
                content.add(toolUseBlock(toolCall));
            }
        }
        if (content.isEmpty()) {
            content.add(textBlock(""));
        }
        return new JsonObject()
            .put("role", "assistant")
            .put("content", content);
    }

    private JsonObject convertToolResultMessage(CatholicToolCallMessage toolMsg) {
        JsonArray content = new JsonArray();
        content.add(new JsonObject()
            .put("type", "tool_result")
            .put("tool_use_id", toolMsg.toolCallId())
            .put("content", toolMsg.content()));
        return new JsonObject()
            .put("role", "user")
            .put("content", content);
    }

    private JsonObject convertUnknownRoleMessage(CatholicChatMessage message) {
        JsonArray blocks = new JsonArray();
        for (CatholicChatContent content : message.contents()) {
            if (content instanceof CatholicTextContent textContent) {
                blocks.add(textBlock(textContent.text()));
            }
        }
        if (blocks.isEmpty()) {
            blocks.add(textBlock(""));
        }
        return new JsonObject()
            .put("role", "user")
            .put("content", blocks);
    }

    private static JsonObject textBlock(String text) {
        return new JsonObject()
            .put("type", "text")
            .put("text", text);
    }

    private static JsonObject toolUseBlock(CatholicFunctionToolCall toolCall) {
        JsonObject input = parseToolInput(toolCall.function().arguments());
        return new JsonObject()
            .put("type", "tool_use")
            .put("id", toolCall.id())
            .put("name", toolCall.function().name())
            .put("input", input);
    }

    private static JsonObject parseToolInput(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isEmpty()) {
            return new JsonObject();
        }
        try {
            return new JsonObject(argumentsJson);
        } catch (Exception ignored) {
            return new JsonObject();
        }
    }

    private JsonArray convertTools(List<CatholicToolDefinition> tools) {
        JsonArray array = new JsonArray();
        for (CatholicToolDefinition tool : tools) {
            if (!"function".equals(tool.type())) {
                continue;
            }
            JsonObject schema = ((CatholicFunctionToolDefinition) tool).function().parameters();
            if (schema == null) {
                schema = new JsonObject();
            }
            array.add(new JsonObject()
                .put("name", ((CatholicFunctionToolDefinition) tool).function().name())
                .put("description", ((CatholicFunctionToolDefinition) tool).function().description())
                .put("input_schema", schema));
        }
        return array;
    }

    private void convertOptions(JsonObject body, CatholicLLMRequestOptions options) {
        Integer maxTokens = options.maxTokens();
        body.put("max_tokens", maxTokens != null ? maxTokens : DEFAULT_MAX_TOKENS);
        if (options.temperature() != null) {
            body.put("temperature", options.temperature());
        }
        if (options.topP() != null) {
            body.put("top_p", options.topP());
        }
        if (options.stop() != null && !options.stop().isEmpty()) {
            body.put("stop_sequences", new JsonArray(options.stop()));
        }
        if (options.extra() != null && !options.extra().isEmpty()) {
            body.mergeIn(options.extra());
        }
    }
}
