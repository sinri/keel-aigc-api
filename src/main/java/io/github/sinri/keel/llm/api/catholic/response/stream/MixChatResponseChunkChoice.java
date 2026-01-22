package io.github.sinri.keel.llm.api.catholic.response.stream;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCall;
import io.github.sinri.keel.llm.api.internal.catholic.response.MixChatResponseChunkChoiceImpl;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonToolCall;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface MixChatResponseChunkChoice extends JsonifiableDataUnit {
    static MixChatResponseChunkChoice create() {
        return new MixChatResponseChunkChoiceImpl();
    }

    static MixChatResponseChunkChoice wrap(JsonObject x) {
        return new MixChatResponseChunkChoiceImpl(x);
    }

    default @Nullable String getRole() {
        return readString("role");
    }

    default MixChatResponseChunkChoice setRole(@Nullable String role) {
        ensureEntry("role", role);
        return this;
    }

    default @Nullable String getContent() {
        return readString("content");
    }

    default MixChatResponseChunkChoice setContent(@Nullable String content) {
        ensureEntry("content", content);
        return this;
    }

    default @Nullable String getReasoningContent() {
        return readString("reasoning_content");
    }

    default MixChatResponseChunkChoice setReasoningContent(@Nullable String reasoningContent) {
        ensureEntry("reasoning_content", reasoningContent);
        return this;
    }

    default @Nullable String getFinishReason() {
        return readString("finish_reason");
    }

    default MixChatResponseChunkChoice setFinishReason(@Nullable String finishReason) {
        ensureEntry("finish_reason", finishReason);
        return this;
    }

    default List<ToolCall> getToolCalls() {
        var array = readJsonObjectArray("tool_calls");
        if (array == null)
            return List.of();
        return array.stream()
                    .map(CommonToolCall::new)
                    .map(x -> (ToolCall) x)
                    .toList();
    }

    default MixChatResponseChunkChoice setToolCalls(List<ToolCall> toolCalls) {
        JsonArray array = new JsonArray();
        toolCalls.stream().map(ToolCall::toJsonObject).forEach(array::add);
        ensureEntry("tool_calls", new JsonArray(toolCalls.stream().map(ToolCall::toJsonObject).toList()));
        return this;
    }

    default @Nullable Integer getIndex() {
        return readInteger("index");
    }

    default MixChatResponseChunkChoice setIndex(@Nullable Integer index) {
        ensureEntry("index", index);
        return this;
    }
}
