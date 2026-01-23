package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.response.stream.GPTResponseChunkChoice;
import io.vertx.core.json.JsonObject;


public class GPTResponseChunkChoiceImpl extends UnmodifiableJsonifiableEntityImpl implements GPTResponseChunkChoice {
    public GPTResponseChunkChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
