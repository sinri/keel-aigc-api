package io.github.sinri.keel.llm.api.sect.dialect.doubao.response.stream;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCall;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonToolCall;
import io.github.sinri.keel.llm.api.internal.sect.dialect.doubao.DoubaoResponseChunkChoiceDeltaImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface DoubaoResponseChunkChoiceDelta extends UnmodifiableJsonifiableEntity {
    static DoubaoResponseChunkChoiceDelta wrap(JsonObject jsonObject) {
        return new DoubaoResponseChunkChoiceDeltaImpl(jsonObject);
    }

    /**
     * @return 内容输出的角色，此处固定为 assistant。
     */
    default @Nullable String getRole() {
        return readString("role");
    }

    /**
     * @return 模型生成的消息内容。
     */
    default @Nullable String getContent() {
        return readString("content");
    }

    /**
     * 仅深度推理模型支持返回此字段，深度推理模型请参见支持模型。
     *
     * @return 模型处理问题的思维链内容。。
     */
    default @Nullable String getReasoningContent() {
        return readString("reasoning_content");
    }

    /**
     * @return 模型生成的工具调用。
     */
    default List<ToolCall> getToolCalls() {
        List<JsonObject> a = readJsonObjectArray("tool_calls");
        if (a == null) return List.of();
        return a.stream().map(CommonToolCall::new)
                .map(x -> (ToolCall) x).toList();
    }
}
