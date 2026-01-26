package io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.response.stream;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen.QwenResponseChunkImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.response.sync.QwenResponseOutput;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.Nullable;

public interface QwenResponseChunk extends UnmodifiableJsonifiableEntity {
    static QwenResponseChunk wrap(JsonObject jsonObject) {
        return new QwenResponseChunkImpl(jsonObject);
    }

    default @Nullable QwenResponseOutput getOutput() {
        JsonObject jsonObject = readJsonObject("output");
        if(jsonObject == null) return null;
        return QwenResponseOutput.wrap(jsonObject);
    }

    default @Nullable String getRequestId() {
        return readString("request_id");
    }

    default @Nullable JsonObject getUsage() {
        return readJsonObject("usage");
    }
}
