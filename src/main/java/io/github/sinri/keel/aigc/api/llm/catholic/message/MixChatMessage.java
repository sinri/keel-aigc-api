package io.github.sinri.keel.aigc.api.llm.catholic.message;

import io.github.sinri.keel.base.json.JsonObjectConvertible;
import io.github.sinri.keel.aigc.api.llm.catholic.tool.call.ToolCall;
import io.github.sinri.keel.aigc.api.internal.llm.catholic.message.MixChatMessageImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * <p>
 * SAMPLE FOR TEXT CHAT: <br>
 * basic text chat request: <br>
 * {@code {"role":"user","text_content":"PROMPT"}}<br>
 * replied tool call response:<br>
 * {@code
 * {"role":"assistant","tool_calls":[{"id":"TOOL_CALL_ID","index":0,"type":"function","function":{"name":"FUNCTION_NAME","arguments":"FUNCTION_ARGUMENTS"}}]}}<br>
 * with tool call output to request:<br>
 * {@code {"role":"tool","text_content":"TOOL_CALL_OUTPUT","tool_call_id":"TOOL_CALL_ID"}}<br>
 * basic text chat response:<br>
 * {@code {"role":"assistant","text_content":"RESPONSE"}}<br>
 * thought text chat response:<br>
 * {@code {"role":"assistant","reasoning_content":"REASONING_CONTENT","text_content":"RESPONSE"}}<br>
 * <p>
 * SAMPLE FOR VISION CHAT: <br>
 * basic vision chat request: <br>
 * {@code
 * {"role":"user","vision_content":[{"type":"text","text":"TEXT_PROMPT"},{"type":"image","image":"IMAGE_PROMPT"}]}}<br>
 * with tool call output to request:<br>
 * {@code {"role":"tool","vision_content":[{"type":"text","text":"TEXT_PROMPT"}],"tool_call_id":"TOOL_CALL_ID"}}<br>
 */
public interface MixChatMessage extends JsonObjectConvertible {

    static MixChatMessage create() {
        return new MixChatMessageImpl();
    }

    static MixChatMessage wrap(JsonObject jsonObject) {
        return new MixChatMessageImpl(jsonObject);
    }

    @Nullable String getTextContent();

    MixChatMessage setTextContent(String content);

    String getRole();

    MixChatMessage setRole(@Nullable String role);

    @Nullable String getReasoningContent();

    MixChatMessage setReasoningContent(String reasoningContent);

    List<ToolCall> getToolCalls();

    MixChatMessage setToolCalls(List<ToolCall> toolCalls);

    @Nullable String getToolCallId();

    MixChatMessage setToolCallId(String toolCallId);

    List<MixChatVisionContentElement> getVisionContent();

    MixChatMessage setVisionContent(List<MixChatVisionContentElement> content);

    default boolean isForVision() {
        return !this.getVisionContent().isEmpty();
    }

    @Nullable Integer getIndex();

    MixChatMessage setIndex(@Nullable Integer index);

    @Nullable String getFinishReason();

    MixChatMessage setFinishReason(@Nullable String finishReason);

}
