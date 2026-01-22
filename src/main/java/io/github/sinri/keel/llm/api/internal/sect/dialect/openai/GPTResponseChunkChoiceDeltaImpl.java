package io.github.sinri.keel.llm.api.internal.sect.dialect.openai;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.response.stream.GPTResponseChunkChoiceDelta;
import io.vertx.core.json.JsonObject;


public class GPTResponseChunkChoiceDeltaImpl extends UnmodifiableJsonifiableEntityImpl implements GPTResponseChunkChoiceDelta {
    public GPTResponseChunkChoiceDeltaImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
