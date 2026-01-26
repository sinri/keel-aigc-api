package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.stream.DoubaoResponseChunk;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseChunkImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponseChunk {
    public DoubaoResponseChunkImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
