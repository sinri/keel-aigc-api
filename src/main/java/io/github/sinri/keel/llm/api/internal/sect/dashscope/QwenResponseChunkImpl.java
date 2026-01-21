package io.github.sinri.keel.llm.api.internal.sect.dashscope;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dashscope.qwen.response.stream.QwenResponseChunk;
import io.vertx.core.json.JsonObject;


public class QwenResponseChunkImpl extends UnmodifiableJsonifiableEntityImpl implements QwenResponseChunk {
    public QwenResponseChunkImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
