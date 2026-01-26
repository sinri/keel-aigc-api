package io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.tool.CommonToolCall;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.GPTResponseChunkChoiceDeltaImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface GPTResponseChunkChoiceDelta extends UnmodifiableJsonifiableEntity {
    static GPTResponseChunkChoiceDelta wrap(JsonObject jsonObject) {
        return new GPTResponseChunkChoiceDeltaImpl(jsonObject);
    }

    default @Nullable String getContent() {
        return readString("content");
    }

    default @Nullable String getRefusal() {
        return readString("refusal");
    }

    default @Nullable String getRole() {
        return readString("role");
    }

    default List<ToolCall> getToolCalls() {
        List<JsonObject> a = readJsonObjectArray("tool_calls");
        if (a == null) return List.of();
        return a.stream().map(CommonToolCall::new)
                .map(x -> (ToolCall) x)
                .toList();
    }
}
