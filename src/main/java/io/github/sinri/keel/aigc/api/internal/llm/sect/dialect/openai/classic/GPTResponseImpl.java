package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.sync.GPTResponse;
import io.vertx.core.json.JsonObject;


public class GPTResponseImpl extends UnmodifiableJsonifiableEntityImpl implements GPTResponse {
    public GPTResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
