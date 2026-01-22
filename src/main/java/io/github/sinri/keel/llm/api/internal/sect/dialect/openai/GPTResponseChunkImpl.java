package io.github.sinri.keel.llm.api.internal.sect.dialect.openai;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.response.stream.GPTResponseChunk;
import io.vertx.core.json.JsonObject;


public class GPTResponseChunkImpl extends UnmodifiableJsonifiableEntityImpl implements GPTResponseChunk {
    public GPTResponseChunkImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
