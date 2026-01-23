package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.classic.core;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.classic.core.filter.OpenAIContentFilterDetectedResult;
import io.vertx.core.json.JsonObject;


public final class ContentFilterDetectedResultImpl extends UnmodifiableJsonifiableEntityImpl implements OpenAIContentFilterDetectedResult {
    public ContentFilterDetectedResultImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
