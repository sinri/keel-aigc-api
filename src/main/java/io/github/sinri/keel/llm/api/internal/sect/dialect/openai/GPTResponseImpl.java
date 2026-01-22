package io.github.sinri.keel.llm.api.internal.sect.dialect.openai;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.response.sync.GPTResponse;
import io.vertx.core.json.JsonObject;


public class GPTResponseImpl extends UnmodifiableJsonifiableEntityImpl implements GPTResponse {
    public GPTResponseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
