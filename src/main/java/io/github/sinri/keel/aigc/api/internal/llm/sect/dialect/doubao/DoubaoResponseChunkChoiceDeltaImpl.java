package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.doubao;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.doubao.response.stream.DoubaoResponseChunkChoiceDelta;
import io.vertx.core.json.JsonObject;


public class DoubaoResponseChunkChoiceDeltaImpl extends UnmodifiableJsonifiableEntityImpl implements DoubaoResponseChunkChoiceDelta {
    public DoubaoResponseChunkChoiceDeltaImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
