package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream.GPTResponseChunk;
import io.vertx.core.json.JsonObject;


public class GPTResponseChunkImpl extends UnmodifiableJsonifiableEntityImpl implements GPTResponseChunk {
    public GPTResponseChunkImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
