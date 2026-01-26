package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.message;

import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.tool.CommonToolCall;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen.QwenMessageImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @since 5.0.0
 */
public interface QwenMessageInResponse extends QwenMessage {
    static QwenMessageInResponse wrap(JsonObject jsonObject) {
        return new QwenMessageImpl(jsonObject);
    }

    /**
     * 输出消息的角色，固定为assistant。
     */
    default @Nullable String getRole() {
        return readString("role");
    }

    /**
     * 输出消息的内容。
     * 当使用qwen-vl或qwen-audio系列模型时为array，其余情况为string。
     * 如果发起Function Calling，则该值为空。
     */
    default @Nullable String getContent() {
        return readString("content");
    }

    /**
     * 输出消息的内容。
     *
     * @return 当使用qwen-vl或qwen-audio系列模型时为array，抽取其中的元素的text字段组成列表。
     */
    default List<String> getContents() {
        var x = readJsonObjectArray("content");
        if (x == null) return List.of();
        return x.stream().map(j -> j.getString("text")).toList();
    }

    /**
     * @return QwQ 模型、QVQ模型的深度思考内容。
     */
    default @Nullable String getReasoningContent() {
        return readString("reasoning_content");
    }

    /**
     * 如果模型需要调用工具，则会生成tool_calls参数。
     */
    default List<ToolCall> getToolCalls() {
        List<JsonObject> x = readJsonObjectArray("tool_calls");
        if (x == null) return List.of();
        return x.stream().map(CommonToolCall::new).collect(Collectors.toUnmodifiableList());
    }
}
