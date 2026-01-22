package io.github.sinri.keel.llm.api.sect.dialect.openai.message;

import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCall;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonToolCall;
import io.github.sinri.keel.llm.api.internal.sect.dialect.openai.GPTMessageImpl;
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
