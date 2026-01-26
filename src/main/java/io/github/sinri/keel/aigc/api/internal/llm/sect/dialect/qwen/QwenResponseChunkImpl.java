package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.qwen;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.qwen.response.stream.QwenResponseChunk;
import io.vertx.core.json.JsonObject;


public class QwenResponseChunkImpl extends UnmodifiableJsonifiableEntityImpl implements QwenResponseChunk {
    public QwenResponseChunkImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
