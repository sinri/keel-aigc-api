package io.github.sinri.keel.llm.api.internal.sect.dialect.openai.core;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.llm.api.sect.dialect.openai.core.filter.OpenAIContentFilterSeverityResult;
import io.vertx.core.json.JsonObject;


public final class ContentFilterSeverityResultImpl extends UnmodifiableJsonifiableEntityImpl implements OpenAIContentFilterSeverityResult {
    public ContentFilterSeverityResultImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
