package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.response.sync.GPTResponseChoice;
import io.vertx.core.json.JsonObject;


public class GPTResponseChoiceImpl extends UnmodifiableJsonifiableEntityImpl implements GPTResponseChoice {
    public GPTResponseChoiceImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
