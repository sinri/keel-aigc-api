package io.github.sinri.keel.llm.api.sect.dialect.qwen.message;

import io.github.sinri.keel.llm.api.internal.sect.dialect.qwen.QwenMessageImpl;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.message.vision.QwenVisionContent;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface QwenMessageInVisionRequest extends QwenMessage {
    static QwenMessageInVisionRequest create() {
        return new QwenMessageImpl();
    }

    static QwenMessageInVisionRequest wrap(JsonObject jsonObject) {
        return new QwenMessageImpl(jsonObject);
    }

    /**
     * 用户发送给模型的消息。
     * 如果您的输入只有文本，则为string类型；如果您的输入包含图像等多模态数据，则为array类型。
     *
     * @param contentList 用户消息的内容。
     */
    static QwenMessage createAsUserInRequest(List<QwenVisionContent> contentList) {
        JsonArray jsonArray = new JsonArray();
        contentList.forEach(content -> {
            jsonArray.add(content.toJsonObject());
        });

        var x = create();
        x.ensureEntry("content", jsonArray);
        x.ensureEntry("role", "user");
        return x;
    }

}
