package io.github.sinri.keel.aigc.api.llm.sect;

import io.github.sinri.keel.aigc.api.llm.catholic.message.MixChatMessage;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class MessageUtils {
    public static JsonObject toCommonChatRequestJsonObject(MixChatMessage mixChatMessage) {
        JsonObject j = new JsonObject();
        j.put("role", mixChatMessage.getRole());
        String textContent = mixChatMessage.getTextContent();
        if (textContent != null) {
            j.put("content", textContent);
        }
        var reasoningContent = mixChatMessage.getReasoningContent();
        if (reasoningContent != null) {
            j.put("reasoning_content", reasoningContent);
        }
        List<ToolCall> toolCalls = mixChatMessage.getToolCalls();
        if (!toolCalls.isEmpty()) {
            JsonArray array = new JsonArray();
            toolCalls.forEach(toolCall -> array.add(new JsonObject()
                    .put("id", toolCall.getId())
                    .put("type", toolCall.getType())
                    .put("function",
                            toolCall.getFunction() == null ? null
                                    : new JsonObject()
                                    .put("name", toolCall.getFunction().getName())
                                    .put("arguments", toolCall.getFunction().getArguments()))
                    .put("index", toolCall.getIndex())));
            j.put("tool_calls", array);
        }
        String toolCallId = mixChatMessage.getToolCallId();
        if (toolCallId != null) {
            j.put("tool_call_id", toolCallId);
        }
        return j;
    }
}
