package io.github.sinri.keel.llm.api.sect.dialect.qwen.response.sync;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dialect.qwen.QwenResponseOutputChoiceImpl;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.message.QwenMessageInResponse;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.0
 */
public interface QwenResponseOutputChoice extends UnmodifiableJsonifiableEntity {
    static QwenResponseOutputChoice wrap(JsonObject jsonObject) {
        return new QwenResponseOutputChoiceImpl(jsonObject);
    }

    /**
     * 当设置输入参数result_format为text时该参数不为空。
     * <p>
     * 有四种情况：<br>
     * 正在生成时为{@code null}；<br>
     * 因模型输出自然结束，或触发输入参数中的stop条件而结束时为{@code stop}；<br>
     * 因生成长度过长而结束为{@code length}；<br>
     * 因发生工具调用为{@code tool_calls}。<br>
     * </p>
     */
    default @Nullable String getFinishReason() {
        return readString("finish_reason");
    }

    /**
     * 模型输出的消息对象。
     */
    default @Nullable QwenMessageInResponse getMessage() {
        JsonObject jsonObject = readJsonObject("message");
        if (jsonObject == null) return null;
        return QwenMessageInResponse.wrap(jsonObject);
    }

}
