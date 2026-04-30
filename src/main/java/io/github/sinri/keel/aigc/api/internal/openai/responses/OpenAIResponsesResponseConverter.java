package io.github.sinri.keel.aigc.api.internal.openai.responses;

import io.github.sinri.keel.aigc.api.internal.catholic.response.CatholicLLMResponseImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.CatholicLLMResponse;
import io.github.sinri.keel.aigc.api.llm.catholic.message.CatholicAssistantMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.response.CatholicLLMUsage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCall;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.CatholicFunctionToolCallImpl;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.FunctionCall;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 OpenAI Responses API 的非流式响应 JSON 转为 {@link CatholicLLMResponse}。
 */
public class OpenAIResponsesResponseConverter {

    /**
     * 转换完整响应对象（通常为 {@code response} 根对象或等价结构）。
     */
    public CatholicLLMResponse convert(JsonObject responseJson) {
        String id = responseJson.getString("id");
        JsonArray output = responseJson.getJsonArray("output");

        StringBuilder textBuilder = new StringBuilder();
        List<CatholicFunctionToolCall> toolCalls = new ArrayList<>();

        if (output != null) {
            for (int i = 0; i < output.size(); i++) {
                JsonObject item = output.getJsonObject(i);
                if (item == null) {
                    continue;
                }
                String type = item.getString("type");
                if ("message".equals(type)) {
                    appendAssistantMessageText(textBuilder, item);
                } else if ("function_call".equals(type)) {
                    toolCalls.add(convertFunctionCall(item));
                }
            }
        }

        String text = !textBuilder.isEmpty() ? textBuilder.toString() : null;
        List<CatholicFunctionToolCall> calls = toolCalls.isEmpty() ? null : toolCalls;
        CatholicAssistantMessage assistant = CatholicAssistantMessage.ofMixed(text, calls);
        if (!assistant.hasText() && !assistant.hasToolCalls()) {
            assistant = CatholicAssistantMessage.ofText("");
        }

        CatholicLLMUsage usage = convertUsage(responseJson.getJsonObject("usage"));

        return CatholicLLMResponseImpl.builder()
            .id(id)
            .message(assistant)
            .usage(usage)
            .build();
    }

    private void appendAssistantMessageText(StringBuilder textBuilder, JsonObject messageItem) {
        if (!"assistant".equals(messageItem.getString("role"))) {
            return;
        }
        JsonArray content = messageItem.getJsonArray("content");
        if (content == null) {
            return;
        }
        for (int i = 0; i < content.size(); i++) {
            JsonObject part = content.getJsonObject(i);
            if (part == null) {
                continue;
            }
            String partType = part.getString("type");
            if ("output_text".equals(partType)) {
                String t = part.getString("text");
                if (t != null && !t.isEmpty()) {
                    if (!textBuilder.isEmpty()) {
                        textBuilder.append('\n');
                    }
                    textBuilder.append(t);
                }
            } else if ("refusal".equals(partType)) {
                String refusal = part.getString("refusal");
                if (refusal != null && !refusal.isEmpty()) {
                    if (!textBuilder.isEmpty()) {
                        textBuilder.append('\n');
                    }
                    textBuilder.append(refusal);
                }
            }
        }
    }

    private CatholicFunctionToolCall convertFunctionCall(JsonObject item) {
        String callId = item.getString("call_id");
        String itemId = item.getString("id", callId);
        String name = item.getString("name");
        String arguments = item.getString("arguments", "{}");
        return new CatholicFunctionToolCallImpl(
            callId != null ? callId : itemId,
            new FunctionCall(name, arguments)
        );
    }

    private CatholicLLMUsage convertUsage(JsonObject usage) {
        if (usage == null) {
            return CatholicLLMUsage.empty();
        }
        Integer prompt = firstNonNull(
            usage.getInteger("input_tokens"),
            usage.getInteger("prompt_tokens")
        );
        Integer completion = firstNonNull(
            usage.getInteger("output_tokens"),
            usage.getInteger("completion_tokens")
        );
        Integer total = usage.getInteger("total_tokens");
        return new CatholicLLMUsage(prompt, completion, total);
    }

    private static Integer firstNonNull(Integer a, Integer b) {
        return a != null ? a : b;
    }
}
