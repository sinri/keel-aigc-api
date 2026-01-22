package io.github.sinri.keel.llm.api.internal.catholic.message;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.catholic.message.MixChatMessage;
import io.github.sinri.keel.llm.api.catholic.message.MixChatVisionContentElement;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCall;
import io.github.sinri.keel.llm.api.internal.catholic.tool.CommonToolCall;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class MixChatMessageImpl extends JsonifiableDataUnitImpl implements MixChatMessage {
    private final static String KEY_ROLE = "role";
    private final static String KEY_TEXT_CONTENT = "text_content";
    private final static String KEY_VISION_CONTENT = "vision_content";
    private final static String KEY_TOOL_CALL_ID = "tool_call_id";
    private final static String KEY_TOOL_CALLS = "tool_calls";
    private final static String KEY_REASONING_CONTENT = "reasoning_content";

    public MixChatMessageImpl() {
        super();
    }

    public MixChatMessageImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

    @Override
    public MixChatMessage setRole(@Nullable String role) {
        ensureEntry(KEY_ROLE, role);
        return this;
    }

    @Override
    public String getRole() {
        return readStringRequired(KEY_ROLE);
    }

    @Override
    public MixChatMessage setTextContent(String content) {
        ensureEntry(KEY_TEXT_CONTENT, content);
        return this;
    }

    @Override
    public @Nullable String getTextContent() {
        return readString(KEY_TEXT_CONTENT);
    }

    @Override
    public MixChatMessage setReasoningContent(String reasoningContent) {
        ensureEntry(KEY_REASONING_CONTENT, reasoningContent);
        return this;
    }

    @Override
    public @Nullable String getReasoningContent() {
        return readString(KEY_REASONING_CONTENT);
    }

    @Override
    public MixChatMessage setToolCalls(List<ToolCall> toolCalls) {
        JsonArray array = new JsonArray();
        toolCalls.stream().map(ToolCall::toJsonObject).forEach(array::add);
        ensureEntry(KEY_TOOL_CALLS, array);
        return this;
    }

    @Override
    public List<ToolCall> getToolCalls() {
        var a = readJsonObjectArray(KEY_TOOL_CALLS);
        if (a == null) {
            return List.of();
        } else {
            return a.stream().map(CommonToolCall::new).collect(Collectors.toList());
        }
    }

    @Override
    public @Nullable String getToolCallId() {
        return readString(KEY_TOOL_CALL_ID);
    }

    @Override
    public Integer getIndex() {
        return readIntegerRequired("index");
    }

    @Override
    public MixChatMessage setIndex(Integer index) {
        ensureEntry("index", index);
        return this;
    }

    @Override
    public String getFinishReason() {
        return readStringRequired("finish_reason");
    }

    @Override
    public MixChatMessage setFinishReason(String finishReason) {
        ensureEntry("finish_reason", finishReason);
        return this;
    }


    @Override
    public MixChatMessage setToolCallId(String toolCallId) {
        ensureEntry(KEY_TOOL_CALL_ID, toolCallId);
        return this;
    }

    @Override
    public List<MixChatVisionContentElement> getVisionContent() {
        var a = readJsonObjectArray(KEY_VISION_CONTENT);
        if (a == null) {
            return List.of();
        } else {
            return a.stream().map(MixChatVisionContentElement::wrap).toList();
        }
    }

    @Override
    public MixChatMessage setVisionContent(List<MixChatVisionContentElement> content) {
        JsonArray array=new JsonArray();
        content.forEach(element->array.add(element.toJsonObject()));
        ensureEntry(KEY_VISION_CONTENT, array);
        return this;
    }
}
