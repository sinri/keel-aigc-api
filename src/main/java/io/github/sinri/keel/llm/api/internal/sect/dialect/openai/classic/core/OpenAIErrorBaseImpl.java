package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic.core;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.error.OpenAIErrorBase;
import io.vertx.core.json.JsonObject;


public final class OpenAIErrorBaseImpl extends UnmodifiableJsonifiableEntityImpl implements OpenAIErrorBase {
    public OpenAIErrorBaseImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
