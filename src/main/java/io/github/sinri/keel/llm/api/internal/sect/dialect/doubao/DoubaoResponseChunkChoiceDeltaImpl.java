package io.github.sinri.keel.llm.api.internal.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.doubao.response.stream.DoubaoResponseChunkChoiceDelta;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseChunkChoiceDeltaImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponseChunkChoiceDelta {
    public DoubaoResponseChunkChoiceDeltaImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
