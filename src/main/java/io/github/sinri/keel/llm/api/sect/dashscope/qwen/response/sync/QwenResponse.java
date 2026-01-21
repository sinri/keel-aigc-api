package io.github.sinri.keel.llm.api.sect.dashscope.qwen.response.sync;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.llm.api.internal.sect.dashscope.QwenResponseImpl;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

/**
 * @since 2.0.0
 */
public interface QwenResponse extends UnmodifiableJsonifiableEntity {
    static QwenResponse wrap(JsonObject rawResponse) {
        return new QwenResponseImpl(rawResponse);
    }

    /**
     * 本次请求的状态码。
     *
     * @return 200 表示请求成功，否则表示请求失败。
     */
    default @Nullable Integer getStatusCode() {
        return readInteger("status_code");
    }

    /**
     * @return 本次调用的唯一标识符。
     */
    default @Nullable String getRequestId() {
        return readString("request_id");
    }

    /**
     * @return 错误码，调用成功时为空值。
     */
    default @Nullable String getCode() {
        return readString("code");
    }

    /**
     * @return 调用结果信息。
     */
    default @Nullable QwenResponseOutput getOutput() {
        JsonObject jsonObject = readJsonObject("output");
        if (jsonObject == null) {
            return null;
        }
        return QwenResponseOutput.wrap(jsonObject);
    }

    /**
     * @return 本次chat请求使用的Token信息。
     */
    default @Nullable JsonObject getUsage() {
        return readJsonObject("usage");
    }

}
