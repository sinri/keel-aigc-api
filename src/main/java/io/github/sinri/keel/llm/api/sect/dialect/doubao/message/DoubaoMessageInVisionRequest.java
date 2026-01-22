package io.github.sinri.keel.llm.api.sect.dialect.doubao.message;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.catholic.tool.call.ToolCall;
import io.github.sinri.keel.llm.api.internal.sect.dialect.doubao.DoubaoMessageImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.message.vision.DoubaoVisionContent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface DoubaoMessageInVisionRequest extends DoubaoMessage {
    static DoubaoMessageInVisionRequest create() {
        return new DoubaoMessageImpl();
    }

    static DoubaoMessageInVisionRequest wrap(JsonObject jsonObject) {
        return new DoubaoMessageImpl(jsonObject);
    }

    /**
     * 开发人员提供的指令，模型应遵循这些指令。如模型扮演的角色或者目标等。
     *
     * @param content 系统信息内容。
     */
    static DoubaoMessageInVisionRequest createAsSystemMessage(String content) {
        return new DoubaoMessageImpl(new JsonObject()
                .put("role", "system")
                .put("content", content)
        );
    }

    /**
     * 用户发送的消息，包含提示或附加上下文信息。不同模型支持的字段类型不同，最多支持文本、图片、视频（需要工单申请）形式的消息。
     *
     * @param contentList 纯文本消息内容，大语言模型支持传入此类型。
     */
    static DoubaoMessageInVisionRequest createAsUserMessage(List<DoubaoVisionContent> contentList) {
        return new DoubaoMessageImpl(new JsonObject()
                .put("role", "user")
                .put("content", new JsonArray(contentList.stream().map(UnmodifiableJsonifiableEntity::cloneAsJsonObject)
                                                         .toList()))
        );
    }

    /**
     * 历史对话中，模型回复的非发起工具调用消息。
     * 往往在多轮对话传入历史对话记录以及Prefill Response时让模型按照预置的回复内容继续回复时使用。
     *
     * @param content 模型回复的消息。
     */
    static DoubaoMessageInVisionRequest createAsAssistantMessage(String content) {
        return new DoubaoMessageImpl(new JsonObject()
                .put("role", "assistant")
                .put("content", new JsonArray()
                        .add(DoubaoVisionContent.create()
                                                .setText("text")
                                                .setText(content)
                                                .toJsonObject()
                        )
                )
        );
    }

    /**
     * 历史对话中，模型回复发起工具调用的消息。
     * 往往在多轮对话传入历史对话记录以及Prefill Response时让模型按照预置的回复内容继续回复时使用。
     *
     * @param content   模型回复的消息。
     * @param toolCalls 模型回复的工具调用信息。
     */
    static DoubaoMessageInVisionRequest createAsToolCallMessage(@Nullable String content, List<ToolCall> toolCalls) {
        var a = new JsonArray();
        toolCalls.forEach(tc -> a.add(tc.toJsonObject()));
        var j = new JsonObject()
                .put("role", "assistant")
                .put("tool_calls", a);
        if (content != null) {
            j.put("content", content);
        }
        return new DoubaoMessageImpl(j);
    }

    /**
     * 模型调用工具的消息。往往在多轮对话传入历史对话记录
     *
     * @param content      工具返回的消息
     * @param tool_call_id 模型调用的工具的 ID。
     */
    static DoubaoMessageInVisionRequest createAsToolOutputMessage(String content, String tool_call_id) {
        return new DoubaoMessageImpl(new JsonObject()
                .put("role", "tool")
                .put("content", content)
                .put("tool_call_id", tool_call_id)
        );
    }

}
