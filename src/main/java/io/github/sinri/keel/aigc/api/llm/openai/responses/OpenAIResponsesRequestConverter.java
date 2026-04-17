package io.github.sinri.keel.aigc.api.llm.openai.responses;

import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMRequest;
import io.github.sinri.keel.aigc.api.llm.catholic.message.*;
import io.github.sinri.keel.aigc.api.llm.catholic.request.CatholicLLMRequestOptions;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicTool;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.CatholicToolCall;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * 将 {@link CatholicLLMRequest} 转换为 OpenAI Responses API（{@code POST /v1/responses}）请求体。
 */
public class OpenAIResponsesRequestConverter {

    /**
     * 转换为 Responses API 的 JSON 请求体（不含 {@code stream}，由客户端写入）。
     */
    public JsonObject convert(CatholicLLMRequest request) {
        JsonObject body = new JsonObject();
        body.put("model", request.model());

        JsonArray input = convertInput(request.messages());
        body.put("input", input);

        if (request.hasTools()) {
            body.put("tools", convertTools(request.tools()));
        }

        convertOptions(body, request.options());

        return body;
    }

    private JsonArray convertInput(List<CatholicChatMessage> messages) {
        JsonArray input = new JsonArray();
        for (CatholicChatMessage message : messages) {
            appendCatholicMessage(input, message);
        }
        return input;
    }

    private void appendCatholicMessage(JsonArray input, CatholicChatMessage message) {
        switch (message.role()) {
            case CatholicSystemMessage.ROLE -> {
                CatholicSystemMessage system = (CatholicSystemMessage) message;
                input.add(easyMessage("system", system.text()));
            }
            case CatholicUserMessage.ROLE -> {
                CatholicUserMessage user = (CatholicUserMessage) message;
                input.add(easyMessage("user", convertUserContent(user)));
            }
            case CatholicAssistantMessage.ROLE -> {
                CatholicAssistantMessage assistant = (CatholicAssistantMessage) message;
                if (assistant.hasText()) {
                    input.add(easyMessage("assistant", assistant.text()));
                }
                if (assistant.hasToolCalls()) {
                    for (CatholicToolCall toolCall : assistant.toolCalls()) {
                        input.add(functionCallInputItem(toolCall));
                    }
                }
            }
            case CatholicToolCallMessage.ROLE -> {
                CatholicToolCallMessage tool = (CatholicToolCallMessage) message;
                input.add(new JsonObject()
                    .put("type", "function_call_output")
                    .put("call_id", tool.toolCallId())
                    .put("output", tool.content()));
            }
            default -> input.add(fallbackEasyMessage(message));
        }
    }

    private JsonObject easyMessage(String role, Object content) {
        return new JsonObject()
            .put("type", "message")
            .put("role", role)
            .put("content", content);
    }

    private JsonObject fallbackEasyMessage(CatholicChatMessage message) {
        JsonArray contentArray = new JsonArray();
        for (CatholicChatContent content : message.contents()) {
            if (content instanceof CatholicTextContent textContent) {
                contentArray.add(new JsonObject()
                    .put("type", "input_text")
                    .put("text", textContent.text()));
            }
        }
        Object content = contentArray.isEmpty()
            ? ""
            : (contentArray.size() == 1 ? contentArray.getJsonObject(0) : contentArray);
        return easyMessage(message.role(), content);
    }

    private JsonObject functionCallInputItem(CatholicToolCall toolCall) {
        return new JsonObject()
            .put("type", "function_call")
            .put("call_id", toolCall.id())
            .put("name", toolCall.function().name())
            .put("arguments", toolCall.function().arguments());
    }

    private Object convertUserContent(CatholicUserMessage userMessage) {
        List<CatholicChatContent> contents = userMessage.contents();
        if (contents.size() == 1 && contents.get(0) instanceof CatholicTextContent textContent) {
            return textContent.text();
        }
        JsonArray contentArray = new JsonArray();
        for (CatholicChatContent content : contents) {
            if (content instanceof CatholicTextContent textContent) {
                contentArray.add(new JsonObject()
                    .put("type", "input_text")
                    .put("text", textContent.text()));
            } else if (content instanceof CatholicImageByUrl imageByUrl) {
                contentArray.add(new JsonObject()
                    .put("type", "input_image")
                    .put("image_url", imageByUrl.url()));
            } else if (content instanceof CatholicImageByBase64 imageByBase64) {
                String dataUrl = "data:" + imageByBase64.mediaType() + ";base64," + imageByBase64.base64Data();
                contentArray.add(new JsonObject()
                    .put("type", "input_image")
                    .put("image_url", dataUrl));
            }
        }
        return contentArray;
    }

    private JsonArray convertTools(List<CatholicTool> tools) {
        JsonArray array = new JsonArray();
        for (CatholicTool tool : tools) {
            if (!"function".equals(tool.type())) {
                continue;
            }
            array.add(new JsonObject()
                .put("type", "function")
                .put("name", tool.function().name())
                .put("description", tool.function().description())
                .put("parameters", tool.function().parameters()));
        }
        return array;
    }

    private void convertOptions(JsonObject body, CatholicLLMRequestOptions options) {
        if (options.temperature() != null) {
            body.put("temperature", options.temperature());
        }
        if (options.maxTokens() != null) {
            body.put("max_output_tokens", options.maxTokens());
        }
        if (options.topP() != null) {
            body.put("top_p", options.topP());
        }
        if (options.extra() != null && !options.extra().isEmpty()) {
            body.mergeIn(options.extra());
        }
    }
}
