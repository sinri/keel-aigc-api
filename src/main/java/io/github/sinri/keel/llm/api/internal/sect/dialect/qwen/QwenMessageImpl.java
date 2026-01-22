package io.github.sinri.keel.llm.api.internal.sect.dialect.qwen;

import io.github.sinri.keel.base.json.JsonifiableDataUnitImpl;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.message.QwenMessageInChatRequest;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.message.QwenMessageInResponse;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.message.QwenMessageInVisionRequest;
import io.vertx.core.json.JsonObject;

/**
 * QwenMessageImpl 实现了 QwenMessage、QwenMessageInRequest、QwenMessageInResponse 接口。
 * 用于封装 Qwen 消息的数据结构，支持 JSON 序列化与反序列化。
 *
 * @since 2.0.0
 */
public class QwenMessageImpl extends JsonifiableDataUnitImpl
        implements QwenMessageInChatRequest, QwenMessageInResponse, QwenMessageInVisionRequest {
    public QwenMessageImpl() {
        super();
    }

    public QwenMessageImpl(JsonObject jsonObject) {
        super(jsonObject);
    }

}
