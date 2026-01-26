package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.stream.GPTResponseChunkChoiceDelta;
import io.vertx.core.json.JsonObject;


public class GPTResponseChunkChoiceDeltaImpl extends UnmodifiableJsonifiableEntityImpl implements GPTResponseChunkChoiceDelta {
    public GPTResponseChunkChoiceDeltaImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
