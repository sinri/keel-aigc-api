package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.response.stream.DoubaoResponseChunk;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseChunkImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponseChunk {
    public DoubaoResponseChunkImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
