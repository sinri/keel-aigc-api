package io.github.sinri.keel.llm.api.sect.dashscope.qwen.request.input;

import io.github.sinri.keel.base.json.JsonifiableDataUnit;
import io.github.sinri.keel.llm.api.internal.sect.dashscope.QwenRequestInputImpl;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.QwenMessage;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.QwenMessageInChatRequest;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.QwenMessageInResponse;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.QwenMessageInVisionRequest;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.message.vision.QwenVisionContent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * @since 2.0.0
 */
public interface QwenRequestInput extends JsonifiableDataUnit {
    /**
     * Create a new QwenRequestInput instance.
     *
     * @return a new QwenRequestInput instance
     */
    static QwenRequestInput create() {
        return new QwenRequestInputImpl();
    }

    /**
     * Wrap an existing JsonObject as QwenRequestInput.
     *
     * @param jsonObject the JsonObject to wrap
     * @return a QwenRequestInput instance wrapping the given JsonObject
     */
    static QwenRequestInput wrap(JsonObject jsonObject) {
        return new QwenRequestInputImpl(jsonObject);
    }

    /**
     * 向由历史对话组成的消息列表中新增一个消息。
     */
    default QwenRequestInput addMessage(QwenMessage message) {
        JsonArray array = this.ensureJsonArray("messages");
        array.add(message.toJsonObject());
        return this;
    }

    default QwenRequestInput addSystemMessage(String content) {
        return addMessage(QwenMessageInChatRequest.createAsSystemInRequest(content));
    }

    default QwenRequestInput addUserChatMessage(String content) {
        return addMessage(QwenMessageInChatRequest.createAsUserInRequest(content));
    }

    default QwenRequestInput addUserVisionMessage(List<QwenVisionContent> contentList) {
        return addMessage(QwenMessageInVisionRequest.createAsUserInRequest(contentList));
    }

    default QwenRequestInput addAssistantMessage(String content) {
        return addMessage(QwenMessageInChatRequest.createAsAssistantInRequest(content));
    }

    default QwenRequestInput addToolCallMessage(QwenMessageInResponse toolCallMessageResponded) {
        return addMessage(toolCallMessageResponded);
    }

    default QwenRequestInput addToolOutputMessage(String content, String toolCallId) {
        return addMessage(QwenMessageInChatRequest.createAsToolOutputInRequest(content, toolCallId));
    }

    /**
     * @return 由历史对话组成的消息列表
     */
    default List<QwenMessageInChatRequest> getMessages() {
        List<JsonObject> messages = this.readJsonObjectArray("messages");
        if (messages == null) {
            return List.of();
        }
        return messages.stream().map(QwenMessageInChatRequest::wrap).toList();
    }
}
