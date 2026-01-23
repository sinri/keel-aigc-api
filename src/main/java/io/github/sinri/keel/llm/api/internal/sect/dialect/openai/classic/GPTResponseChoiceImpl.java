package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.response.sync.GPTResponseChoice;
import io.vertx.core.json.JsonObject;


public class GPTResponseChoiceImpl extends UnmodifiableJsonifiableEntityImpl implements GPTResponseChoice {
    public GPTResponseChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
