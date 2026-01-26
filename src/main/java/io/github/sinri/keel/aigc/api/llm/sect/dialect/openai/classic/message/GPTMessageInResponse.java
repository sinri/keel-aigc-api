package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.message;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.tool.CommonToolCall;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.GPTMessageImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface GPTMessageInResponse extends GPTMessage {
    static GPTMessageInResponse wrap(JsonObject jsonObject) {
        return new GPTMessageImpl(jsonObject);
    }

    // annotations: an array but unknown

    default @Nullable String getRefusal() {
        return readString("refusal");
    }

    default List<ToolCall> getToolCalls() {
        List<JsonObject> a = readJsonObjectArray("tool_calls");
        if (a == null) return List.of();
        return a.stream().map(CommonToolCall::new)
                .map(x -> (ToolCall) x)
                .toList();
    }
}
