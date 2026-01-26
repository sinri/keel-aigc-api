package io.github.sinri.keel.aigc.api.internal.llm.sect.dialect.openai.classic.core;

import io.github.sinri.keel.base.json.UnmodifiableJsonifiableEntityImpl;
import io.github.sinri.keel.aigc.api.llm.sect.dialect.openai.classic.core.filter.OpenAIContentFilterSeverityResult;
import io.vertx.core.json.JsonObject;


public final class ContentFilterSeverityResultImpl extends UnmodifiableJsonifiableEntityImpl implements OpenAIContentFilterSeverityResult {
    public ContentFilterSeverityResultImpl(JsonObject jsonObject) {
        super(jsonObject);
    }
}
