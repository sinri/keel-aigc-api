package io.github.sinri.keel.aigc.api.internal.anthropic;

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
 * 将 Anthropic Messages API 非流式响应 JSON 转为 {@link CatholicLLMResponse}。
 */
public class AnthropicResponseConverter {

    /**
     * 转换顶层响应对象（含 {@code id}、{@code content}、{@code usage} 等字段）。
     */
    public CatholicLLMResponse convert(JsonObject anthropicResponse) {
        String id = anthropicResponse.getString("id");

        JsonArray content = anthropicResponse.getJsonArray("content");
        StringBuilder textBuilder = new StringBuilder();
        List<CatholicFunctionToolCall> toolCalls = new ArrayList<>();

        if (content != null) {
            for (int i = 0; i < content.size(); i++) {
                JsonObject block = content.getJsonObject(i);
                if (block == null) {
                    continue;
                }
                String type = block.getString("type");
                if ("text".equals(type)) {
                    String t = block.getString("text");
                    if (t != null && !t.isEmpty()) {
                        if (textBuilder.length() > 0) {
                            textBuilder.append('\n');
                        }
                        textBuilder.append(t);
                    }
                } else if ("tool_use".equals(type)) {
                    toolCalls.add(convertToolUse(block));
                }
            }
        }

        String text = textBuilder.length() > 0 ? textBuilder.toString() : null;
        List<CatholicFunctionToolCall> calls = toolCalls.isEmpty() ? null : toolCalls;
        CatholicAssistantMessage assistant = CatholicAssistantMessage.ofMixed(text, calls);
        if (!assistant.hasText() && !assistant.hasToolCalls()) {
            assistant = CatholicAssistantMessage.ofText("");
        }

        CatholicLLMUsage usage = convertUsage(anthropicResponse.getJsonObject("usage"));

        return CatholicLLMResponseImpl.builder()
            .id(id)
            .message(assistant)
            .usage(usage)
            .build();
    }

    private CatholicFunctionToolCall convertToolUse(JsonObject block) {
        String id = block.getString("id");
        String name = block.getString("name");
        Object input = block.getValue("input");
        String arguments;
        if (input instanceof JsonObject jsonObject) {
            arguments = jsonObject.encode();
        } else if (input instanceof String s) {
            arguments = s;
        } else if (input != null) {
            arguments = String.valueOf(input);
        } else {
            arguments = "{}";
        }
        return new CatholicFunctionToolCall(
            id,
            "tool_use",
            new FunctionCall(name, arguments)
        );
    }

    private CatholicLLMUsage convertUsage(JsonObject usage) {
        if (usage == null) {
            return CatholicLLMUsage.empty();
        }
        Integer input = usage.getInteger("input_tokens");
        Integer output = usage.getInteger("output_tokens");
        Integer total = null;
        if (input != null && output != null) {
            total = input + output;
        }
        return new CatholicLLMUsage(input, output, total);
    }
}
