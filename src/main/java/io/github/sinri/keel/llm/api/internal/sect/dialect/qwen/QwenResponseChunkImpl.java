package io.github.sinri.keel.llm.api.internal.sect.dialect.qwen;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.qwen.response.stream.QwenResponseChunk;
import io.vertx.core.json.JsonObject;


public class QwenResponseChunkImpl extends UnmodifiableJsonifiableEntityImpl implements QwenResponseChunk {
    public QwenResponseChunkImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
